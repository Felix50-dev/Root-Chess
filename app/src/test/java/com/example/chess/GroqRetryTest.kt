package com.example.chess

import com.example.chess.data.remote.GroqApiException
import com.example.chess.data.remote.retryWithBackoff
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class GroqRetryTest {

    // Records every delay the retry loop asked for, without actually suspending — keeps
    // these tests fast and deterministic regardless of the real backoff durations.
    private fun recordingDelay(): Pair<MutableList<Long>, suspend (Long) -> Unit> {
        val recorded = mutableListOf<Long>()
        val delayFn: suspend (Long) -> Unit = { millis -> recorded.add(millis) }
        return Pair(recorded, delayFn)
    }

    @Test
    fun succeedsFirstAttempt_blockInvokedOnce_noDelay() = runBlocking {
        val (delays, delay) = recordingDelay()
        var invocations = 0

        val result = retryWithBackoff(delay) { invocations++; "ok" }

        assertEquals("ok", result)
        assertEquals(1, invocations)
        assertTrue(delays.isEmpty())
    }

    @Test
    fun retriesOnceOn429_thenSucceeds() = runBlocking {
        val (delays, delay) = recordingDelay()
        var invocations = 0

        val result = retryWithBackoff(delay) { attempt ->
            invocations++
            if (attempt == 1) throw GroqApiException(429, "API error 429: rate limited")
            "ok"
        }

        assertEquals("ok", result)
        assertEquals(2, invocations)
        assertEquals(1, delays.size)
    }

    @Test
    fun parsesRetryAfterHint_clampedIntoDelay() = runBlocking {
        val (delays, delay) = recordingDelay()

        retryWithBackoff(delay) { attempt ->
            if (attempt == 1) {
                throw GroqApiException(
                    429,
                    "API error 429: {\"error\":{\"message\":\"Rate limit reached ... " +
                        "Please try again in 3.7725s. Need more tokens?\"}}",
                )
            }
            "ok"
        }

        assertEquals(1, delays.size)
        assertEquals(3772L, delays[0]) // 3.7725s -> 3772ms, within [500, 5000] so unclamped
    }

    @Test
    fun malformedHint_fallsBackToFixedDelay() = runBlocking {
        val (delays, delay) = recordingDelay()

        retryWithBackoff(delay) { attempt ->
            if (attempt == 1) throw GroqApiException(429, "API error 429: no hint in this body")
            "ok"
        }

        assertEquals(1, delays.size)
        assertEquals(1200L, delays[0]) // DEFAULT_RATE_LIMIT_BACKOFF_MS
    }

    @Test
    fun absurdHint_clampedToMaxDelay() = runBlocking {
        val (delays, delay) = recordingDelay()

        retryWithBackoff(delay) { attempt ->
            if (attempt == 1) {
                throw GroqApiException(429, "API error 429: Please try again in 9999s.")
            }
            "ok"
        }

        assertEquals(1, delays.size)
        assertEquals(5000L, delays[0]) // clamped to MAX_BACKOFF_MS
    }

    @Test
    fun retriesOn503_thenSucceeds() = runBlocking {
        val (delays, delay) = recordingDelay()
        var invocations = 0

        val result = retryWithBackoff(delay) { attempt ->
            invocations++
            if (attempt == 1) throw GroqApiException(503, "API error 503: service unavailable")
            "ok"
        }

        assertEquals("ok", result)
        assertEquals(2, invocations)
        assertEquals(1, delays.size)
        assertEquals(800L, delays[0]) // DEFAULT_SERVER_ERROR_BACKOFF_MS, no hint parsed for 5xx
    }

    @Test
    fun doesNotRetryOn401_propagatesOriginalException() = runBlocking {
        val (delays, delay) = recordingDelay()
        var invocations = 0

        try {
            retryWithBackoff(delay) {
                invocations++
                throw GroqApiException(401, "API error 401: invalid API key")
            }
            fail("expected GroqApiException to propagate")
        } catch (e: GroqApiException) {
            assertEquals(401, e.statusCode)
            assertEquals("API error 401: invalid API key", e.message)
        }

        assertEquals(1, invocations)
        assertTrue(delays.isEmpty())
    }

    @Test
    fun doesNotRetryOnSocketTimeout_propagatesImmediately() = runBlocking {
        val (delays, delay) = recordingDelay()
        var invocations = 0

        try {
            retryWithBackoff(delay) {
                invocations++
                throw SocketTimeoutException("timed out")
            }
            fail("expected SocketTimeoutException to propagate")
        } catch (e: SocketTimeoutException) {
            // expected
        }

        assertEquals(1, invocations)
        assertTrue(delays.isEmpty())
    }

    @Test
    fun doesNotRetryOnGenericIOException_propagatesImmediately() = runBlocking {
        val (delays, delay) = recordingDelay()
        var invocations = 0

        try {
            retryWithBackoff(delay) {
                invocations++
                throw IOException("connection reset")
            }
            fail("expected IOException to propagate")
        } catch (e: IOException) {
            // expected
        }

        assertEquals(1, invocations)
        assertTrue(delays.isEmpty())
    }

    @Test
    fun exhaustsRetries_repeated429_rethrowsFinalExceptionUnchanged() = runBlocking {
        val (delays, delay) = recordingDelay()
        var invocations = 0

        try {
            retryWithBackoff(delay) {
                invocations++
                throw GroqApiException(429, "API error 429: still rate limited")
            }
            fail("expected GroqApiException to propagate after exhausting retries")
        } catch (e: GroqApiException) {
            assertEquals(429, e.statusCode)
            assertEquals("API error 429: still rate limited", e.message)
        }

        assertEquals(2, invocations) // MAX_ATTEMPTS
        assertEquals(1, delays.size) // one backoff between the two attempts, none after the last
    }

    @Test
    fun doesNotRetryOn400_blockInvokedOnce() = runBlocking {
        val (delays, delay) = recordingDelay()
        var invocations = 0

        try {
            retryWithBackoff(delay) {
                invocations++
                throw GroqApiException(400, "API error 400: bad request")
            }
            fail("expected GroqApiException to propagate")
        } catch (e: GroqApiException) {
            assertEquals(400, e.statusCode)
        }

        assertEquals(1, invocations)
        assertTrue(delays.isEmpty())
    }
}

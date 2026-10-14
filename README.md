# Root Chess

Root Chess is an Android chess app that doesn't just let you play — it teaches. Built with Jetpack Compose and MVVM, it pairs a real Stockfish engine with deterministic tactics detection and AI-narrated coaching, so you can play a friend locally, climb a roster of calibrated bots, review your games move-by-move with grounded explanations, and drill puzzles pulled straight from your own past mistakes.

## Features

**Play**
- **Two-Player Gameplay**: Local pass-and-play chess against another person on the same device.
- **Your Journey**: A bot roadmap of 14 named opponents across 7 difficulty worlds (roughly Elo 400–2600+), each backed by a real Stockfish engine at a calibrated skill level.

**Learn**
- **Game Analysis**: Post-game accuracy percentages and move categorization (Brilliant, Best, Great, Inaccuracy, Mistake, Blunder, Miss).
- **Game Review**: Move-by-move interactive playback with an evaluation graph and AI explanations grounded in deterministically-detected tactics (pins, forks, skewers, discovered attacks) and opening identification — not free-form LLM guessing.

**Practice**
- **Puzzles**: Auto-generated from the blunders and mistakes in your own past games.

**AI Coach**
- **Chat**: Ask questions about your games in a dedicated AI coach chat.

**Free tier**
- AI move explanations during review are limited to one reviewed game per day on the free tier. Unlimited review explanations and the AI Coach chat are part of a premium subscription.

## Screenshots

| Home | Game Play | Game Analysis |
| --- | --- | --- |
| <img src="screenshots/home_screen.jpeg" width="250"/> | <img src="screenshots/game_play.jpeg" width="250"/> | <img src="screenshots/analysis.jpeg" width="250"/> |

| Game Review | Your Journey |
| --- | --- |
| <img src="screenshots/review.jpeg" width="250"/> <img src="screenshots/review2.jpeg" width="250"/> | <img src="screenshots/journey.jpeg" width="250"/> |

## Tech Stack

- Kotlin, Jetpack Compose + Material 3, MVVM
- Hilt (DI), Room (local persistence), Navigation Compose, DataStore
- Native Stockfish chess engine built via CMake/NDK
- Groq-backed LLM for move explanations and coaching narration
- RevenueCat for subscription billing
- minSdk 24, targetSdk 34, compileSdk 35

## Project Structure

- `:app` — screens, game logic, and the native Stockfish build.
- `:payments` — isolates all RevenueCat billing/entitlement code from the rest of the app.
- `:baselineprofile` — startup benchmark module for generating a Baseline Profile (profile generation is still in progress).

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Felix50-dev/Root-Chess.git
   ```
2. Add a `local.properties` with the API keys the build requires.
3. Open the project in Android Studio.
4. Build and run the project on an emulator or a physical device.

## Usage

1. Launch the app and pick a mode from the home screen: play a friend, play a bot from your roadmap, solve puzzles, or chat with the AI coach.
2. After a game, open Game Analysis or Game Review to see accuracy, move quality, and AI-grounded explanations.
3. Puzzles drawn from your own past mistakes will appear in the Puzzles list as you play more games.

## Roadmap

- Finish Baseline Profile generation (`:baselineprofile` has the build config in place but no benchmark test written yet).
- Validate and tune bot Elo calibration against real playtesting.
- Play Store release preparation.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a pull request or open an issue if you have any suggestions or improvements.

For any questions or feedback, please reach out to [mwatufelix1@gmail.com] or open an issue on GitHub.

Thank you for checking out Root Chess! We hope you enjoy playing and contributing to its development.

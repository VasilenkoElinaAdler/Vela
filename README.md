# Vela Elina Cards
Native Android educational sailing card game. Version 0.1.0 source baseline, Spanish only, 30 draft cards, six categories, offline. Public repository by owner choice; do not commit secrets.

## Status
See [status](docs/STATUS.md), [approved master plan](docs/MASTER_PLAN.md), [decisions](docs/DECISIONS.md), [requirements](docs/REQUIREMENTS.md), [design](docs/DESIGN.md), [test plan](docs/TEST_PLAN.md), [release procedure](docs/RELEASE.md), [changelog](docs/CHANGELOG.md).

## Build
Java 17, Gradle 8.11.1, Android SDK 36. Run python3 scripts/validate_cards.py, then gradle :app:testDebugUnitTest :app:assembleDebug. GitHub Actions will automate these steps when configured. Download APK only from a successful run. No Android build has yet been confirmed.

Canonical cards: app/src/main/assets/cards/es.json. All cards are draft until coach review. Game rules and UI are provisional. No ads or network permissions. No Google Play release at this stage.

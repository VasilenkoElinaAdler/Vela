# Vela Elina Cards — Master plan
Approved 2026-11-27 by project owner.

## Approved decisions
1. Native Android: Kotlin + Jetpack Compose.
2. GitHub Actions cloud builds; no local Android Studio required for routine builds.
3. Single Bubble Gum visual identity.
4. 30 reference cards (VELA-001–030) in six categories, content still requires coach approval.
5. Four-language architecture; only Spanish is currently populated; remaining languages and translations pending.
6. Offline, no ads, registration, server or personal data collection.
7. Persistent documentation and decision history in this repository.
8. Test every significant stage; never claim an APK or successful build without verification.

## Milestones / exit gates
- 0.1.0: import existing baseline archive and preserve card IDs. Validate data and compile first debug APK in CI. Inspect logs and artifact. No Play upload.
- 0.2.0: stable option IDs instead of positional keys, versioned round persistence (including shuffled order and selected answer), filtering by difficulty, correct restoration and progression; regression tests. Confirm installation/update on Android. Debug signing continuity: do not promise in-place upgrades until stable debug signing is configured; never commit private keys.
- 0.3.0: coach approves all 30 cards by ID; review wording, plausible distractors, physical explanations and race rules.
- 0.4.0: approve Bubble Gum screens, accessible layouts, verified sailing diagrams and icon. Do not invent mechanics. Dark app backing, purple field, beige hull, yellow wind arrows, red boom, high-contrast rudder; exact unapproved colors provisional. Hearts represent lives, stars earned; equal visual row widths; Inicio always prominent; congratulations only after success.
- 0.5.0: four language packs and localized interface; stable IDs and identical answer semantics validated across languages; do not expose incomplete locales.
- 0.9.0: confirm permanent application ID and versionCode; signed AAB with Play App Signing/upload key; policy, Data Safety, age rating, Families, store assets and device tests. Check current Play requirements.
- 0.9.x: closed testing, feedback and corrections; meet applicable Play account requirements.
- 1.0.0: production submission only after approval and verified gates. Later updates use same application ID/signing lineage and increasing versionCode.

## Provisional, not approved
Three lives per round, one star per correct response, one answer per card, color HEX values, app icon, package ID com.velaelina.cards, minSdk 24, exact difficulty labels, design, illustrations and hints. No additional languages have been approved by name here. Keep all questions in draft state until individually reviewed.

## Workflow discipline
One canonical card database under app/src/main/assets/cards. Stable IDs, targeted edits, no silent changes to approved cards. Keep code, content and UI separate. Each meaningful change has a commit and tests. Preserve unrelated repositories. Repository is public by owner choice; do not publish secrets, credentials, signing keys or personal data.

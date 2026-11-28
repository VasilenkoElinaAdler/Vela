# Quality gate for every test APK
Owner direction: make basic functional/UX regressions a developer responsibility, not a repeated manual burden on the coach.
- Unit tests: stable IDs, initial health, correct/wrong events, all terms can start, all permutations possible in sampled seeds, no duplicate adjacent round order, no extra stars from repeated practice, exhaustion, save/resume model.
- Android emulator: install and launch application, title/art/Jugar/disabled Regata order, centred visible Jugar, real touch on diagram, wrong answer reduces one heart, correct answer advances, all four tasks complete, replay varies order and does not increase stars, return home and recreation preserve training state. Test-only dependencies and cloud emulator, no new runtime library or size overhead intended. 
- APK upload gated on successful unit tests and emulator journey; retain reports on failure. No APK labelled tested until workflow succeeds. 
- Human testing remains needed for quality of original art, real touch ergonomics across devices, sound quality and children's learning/motivation. Future automated scenarios must be added for newly found regressions.
- Before Google Play: add screen-size coverage, accessibility and offline device tests, release AAB/signing and policy verification. No expansion of content before this milestone passes.

## Curriculum correction — Decision 018
The 4-term technical prototype is not a complete lesson. See docs/PARTS_CURRICULUM.md for recovered original catalogue and independent coverage gate. Do not advance to other topics before all required terms and accurate targets/audio are validated.

## Decision 019
Independent catalogue and asset validation, all seven hit targets, randomness, repeated awards, exact reaction thresholds and 1500ms, completion priority, actual emulator reaction views. Compile Android test source before emulator startup; fix previous TouchInjectionScope.size compilation error using centre coordinates. Keep APK upload gated on all checks.

## Decision 020 regression gate
Check 3 initial hearts, provisional stars with zero permanent credit before success, third error clears attempt stars, full success awards exactly seven once, replay starts 3/0, successful replay earns +0, intentional failure after prior success displays zero attempt stars while home shows seven earned. Test persistence after exit/recreation, retry and legacy full vs incomplete migration. Keep all existing tests and APK upload gate.

## Decision 021
Independent validation of both catalogues and 19 assets; side zones and thin-part tolerance, separate task IDs, correct order randomization, six-star completion, thirteen permanent stars, replay, unlock and persistent resume on Android. APK gate unchanged.

## Decision 022
Check final side raster SHA-256, exact target centers, narrow-part tolerance, neighboring sail precedence, normalized coordinate invariance at five widths; Android journey includes side replay failure after earlier wins and restart with 13 permanent stars. Actual device ergonomics remain to be tested. No APK upload until all checks pass.

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
9. Owner-approved 2026-11-27: application size and resource efficiency are permanent quality requirements for mobile devices. Track size at every build/update and review regressions before release.

10. Owner-approved 2026-11-27: replace the quiz-only experience with a regatta, local virtual opponent, visual penalty manoeuvres, trophy achievements, a knowledge collection and earned-star cosmetic boat customization. Preserve offline operation, Bubble Gum identity and package-size quality gate.

## Milestones / exit gates
- 0.1.0: import existing baseline archive and preserve card IDs. Validate data and compile first debug APK in CI. Inspect logs and artifact. No Play upload.
- 0.2.0 Regata: prioritized gameplay redesign following owner's Android test of 0.1.0 (functional but monotonous; random tapping after repeated rounds; mastered-card count alone insufficient motivation). Keep 30 existing draft Spanish cards and stable card IDs. Deliver in testable increments: (a) preserve baseline and measure comparable APK sizes; automate CI size reports, stable option IDs and versioned persistence; (b) implement offline turn-based regatta with visible advancement, penalty loop, local opponent, hearts/stars and meaningful finish; (c) add durable trophies and six-category knowledge album with two correct confirmations in distinct regattas, incorrect cards requeued for learning, no loss of earned cards; (d) add earned-star cosmetic boat customization, no paid items or competitive advantage; (e) device and learning-engagement tests, optimized release-like size comparison and owner approval before more cards/translations. Preserve difficulty filtering (implement and validate before expanding content). Confirm installation/update on Android. Debug signing continuity: do not promise in-place upgrades until stable debug signing is configured; never commit private keys. Exact race balance, thresholds, economy, animations and UI require prototype testing; the owner approved the mechanics, not all numeric parameters.
- 0.2.x: iterate based on real-device feedback and voluntary supervised child testing; do not move to content expansion until the core loop is engaging and educational.
- 0.3.0: coach approves all 30 cards by ID; review wording, plausible distractors, physical explanations and race rules.
- 0.4.0: approve Bubble Gum screens, accessible layouts, verified sailing diagrams and icon. Do not invent mechanics. Dark app backing, purple field, beige hull, yellow wind arrows, red boom, high-contrast rudder; exact unapproved colors provisional. Hearts represent lives, stars earned; equal visual row widths; Inicio always prominent; congratulations only after success.
- 0.5.0: four language packs and localized interface; stable IDs and identical answer semantics validated across languages; do not expose incomplete locales.
- 0.9.0: confirm permanent application ID and versionCode; signed AAB with Play App Signing/upload key; policy, Data Safety, age rating, Families, store assets and device tests. Check current Play requirements.
- 0.9.x: closed testing, feedback and corrections; meet applicable Play account requirements.
- 1.0.0: production submission only after approval and verified gates. Later updates use same application ID/signing lineage and increasing versionCode.

## Regatta design and persistent rewards
- Core loop: choose/start a short regatta, answer one card at a time, show explanation and visible boat response, finish/result, then album/next regatta. No response-time penalty; opponent movement depends on turns. Correct answers advance toward the finish; errors show a short illustrative penalty loop without forward progress and cost a heart. Explain that this is a game consequence, not a universal sailing rule. No mandatory animation delay; reduced-motion support.
- Proposed prototype tuning (not final): five correct advances to finish, three initial hearts, one star per correct answer; distinct questions within a race where feasible, mistakes revisited in later races. Opponent pacing, tie handling, streak bonus, maximum round length and category selection to be explicitly tested; distinguish five advances from five total attempts. Loss of all hearts ends race; preserve previously earned progress. No fictitious wins or arbitrary rewards.
- Separate local durable state: unique card ID and confirmation evidence across different race IDs, unlocked cards, category achievements, trophies, total earned/spendable stars, owned/equipped cosmetic items, and active race state. First correct = discovered, second correct in another race = collected; repeated taps/retries or reopening a result must not duplicate rewards. Collection is permanent and reviewable offline. Existing single-answer mastery from 0.1.0 is not equivalent to confirmed mastery; preserve legacy data and migrate as discovered where feasible, test migration.
- Finish screen summarizes actual outcome and newly earned rewards. First/second place and failed race must be distinct; final thresholds and trophy catalog provisional. Cosmetics are earned only in-game; educational diagram color roles remain fixed. No timers, streak-loss pressure, ads, loot boxes, or purchases.
- Procedural/vector drawing and Compose animations only where practical; no new game engine or network dependency. Prioritize readable controls, always-visible Inicio, equal-width heart/star rows, no congratulations before actual success, and coach validation of sailing diagrams.
- Engagement gate: owner and supervised learners voluntarily choose another short race; observe thoughtful answers, explanation reading, collection understanding and reduced random tapping. No analytics collection. Record qualitative feedback and explicit pass/revise decision; keep card content draft until coach review.

## Application size and mobile efficiency (permanent quality gate)
- Preserve the native Kotlin/Compose architecture and offline functionality; avoid unnecessary dependencies, media and duplicated assets.
- Next technical milestone: measure the actual baseline debug APK (not the enclosing artifact ZIP), then produce a separate optimized release-like build using R8 and resource shrinking; verify that functionality is preserved. Keep the existing working debug build available for testing.
- Automate a CI size report for every successful build: exact bytes of comparable APK/AAB outputs, change versus the previous comparable baseline, and breakdown of code, resources, native libraries and assets. Track Google Play device-specific compressed download size separately from AAB size and installed storage. Record build type, version, commit and measurement method.
- Review unexplained size increases before accepting changes; before release record measurements and resolve or explicitly approve any budget overrun. Set enforceable thresholds after measuring the optimized baseline and verifying representative devices.
- Prefer reusable vector/programmatic sailing diagrams, appropriately sized compressed WebP for raster artwork, shared assets across languages, and compact text data. Do not sacrifice instructional accuracy, readability, accessibility or offline use merely to reduce size.
- Proposed engineering targets (not guaranteed or formally approved numeric caps): optimized base download up to 5 MB; expanded deck of 250 cards in four languages with illustrations up to 10 MB. Use decimal MB and explicitly state the measurement type; verify feasibility with device-specific Play download estimates. No mandatory extra content downloads.
- Monitor startup time, responsiveness and installed storage on representative lower-end Android devices as a separate check; small download size alone does not establish performance.

## Provisional, not approved
Three lives per round, one star per correct response, one answer per card, color HEX values, app icon, package ID com.velaelina.cards, minSdk 24, exact difficulty labels, design, illustrations and hints. No additional languages have been approved by name here. Keep all questions in draft state until individually reviewed.

## Workflow discipline
One canonical card database under app/src/main/assets/cards. Stable IDs, targeted edits, no silent changes to approved cards. Keep code, content and UI separate. Each meaningful change has a commit and tests. Preserve unrelated repositories. Repository is public by owner choice; do not publish secrets, credentials, signing keys or personal data.

## Approved artwork / UI separation — Decision 011
2026-11-27 — Decision 011 (owner approved): preserve the two supplied finished Regata and gulls illustrations as single, static, unaltered raster artwork layers (WebP compression permitted; no redrawing, cropping, separation, or vector substitution). Figma is for editable UI: base colors, panels, hearts, stars, buttons, progress track, small boat markers and finish flag. Compose animates the markers, handles gameplay, sound and persistence. Four boats in the illustration are static; only the two small progress markers move. Supersedes the rejected vector artwork preview under design/regata-vector-preview; do not package it. No change to the 30 cards, collection or agreed gameplay. Do not reopen visual design discussions. Current preview values remain provisional. See docs/REGATA_LAYERS.md.

## Unified system gate — Decision 014
All game modes must follow docs/GAME_SYSTEM.md. The first-lesson prototype's 10-task/10-star goal and Regata's 3-heart start are noncanonical prototype values; do not treat them as approved. New topics remain frozen. No runtime rule changes until the proposed shared lifecycle/reward rules are reviewed and integrated; do not build the first-lesson test with divergent systems.

## Current milestone — Decision 015
First interactive Parts lesson and shared training progress only; preserve existing cards/regatta. Final reward catalogue at docs/REWARDS.md. No further topics, passport design or photo feature until first lesson child testing passes.

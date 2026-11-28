# Partes del barco — first playable prototype
- The owner-supplied PROA screen (1010 × 1536) is the visual source. Original artwork field crop x=66..945, y=589..1488; unaltered field, compressed WebP. UI is separate and dynamic. Preserve aspect ratio. No replacement artwork.
- Prototype targets: PROA, POPA, BABOR, ESTRIBOR. Left/right defined looking toward bow. Other shapes are deliberately unlabelled because this schematic does not identify every rigging detail unambiguously. Coach must validate hit zones before extending the topic.
- Ten questions with at least two occurrences per part; initial PROA, randomized order without adjacent duplicates. Ten hearts and ten stars match reference; actual counts only. No timer, no false success. Retry same term on wrong tap, short feedback, no Continuar interruption. Leaving lesson resets its provisional round; existing collection remains untouched.
- Offline asset archive required at app/src/main/assets/vela_parts_assets.zip, entries boat.webp, proa.ogg, popa.ogg, babor.ogg, estribor.ogg, correct.ogg, ups.ogg, finish.ogg. Spanish synthetic recordings are temporary; validate intelligibility on Android. Correct/finish tones are original synthesized notes. No external download.
- All existing 30 cards unchanged: none ask identification of the four current terms as its answer. Reassess overlap when final lesson scope is approved.
- VelaTypography controls all UI. Test APK version 0.3.0-parts-preview (code 3); no unrelated feature changes, no R8 yet. Must verify compile, exact raster, hit regions, sound once per accepted tap, correct advances, wrong retries, no duplicate rewards, exit/retry, prior card game, size, and child engagement.
- The binary archive must be present in repo before the intended test build. Source commit initially skips CI to avoid building without assets.

## Unified system gate — Decision 014
All game modes must follow docs/GAME_SYSTEM.md. The first-lesson prototype's 10-task/10-star goal and Regata's 3-heart start are noncanonical prototype values; do not treat them as approved. New topics remain frozen. No runtime rule changes until the proposed shared lifecycle/reward rules are reviewed and integrated; do not build the first-lesson test with divergent systems.

## 0.3.1 supersedes prototype numbers
Four distinct targets; initial 10 hearts, unique task stars, save on every accepted answer, resume incomplete tasks. Repeat completed lesson for practice without duplicate rewards; at zero hearts Restart or Exit. Existing card collection preserved, Regata launch locked pending new timed mode. Version code 4. No additional content.

## Curriculum correction — Decision 018
The 4-term technical prototype is not a complete lesson. See docs/PARTS_CURRICULUM.md for recovered original catalogue and independent coverage gate. Do not advance to other topics before all required terms and accurate targets/audio are validated.

## 0.3.3 supersedes prior topic lists
Seven approved targets and three original gull reactions. Required archive: app/src/main/assets/vela_parts_assets_v2.zip. No build before asset upload. Earlier four-term progress remains compatible because IDs are unchanged. 

## 0.3.5 — second mission
Side-view original art and four new offline pronunciations in app/src/main/assets/vela_parts_side_assets.zip. Reuse shared original feedback/audio and rules. First successful TOP unlocks SIDE, six tasks, 13 permanent stars total. No race redesign. Build only after both archives present.

## Approved Tropical Bubble asset (Decision 022)
Upload only the updated vela_parts_side_assets.zip containing the full approved raster and the four unchanged pronunciation files. Old side asset must not be used. CI verifies the approved WebP hash. No other design changes.

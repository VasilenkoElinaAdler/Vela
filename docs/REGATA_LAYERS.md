# Regata / ¡UPS! — approved implementation layers
Owner confirmed 2026-11-27. Decision 011 supersedes the rejected vector scene. Keep existing Bubble Gum palette, game rules, card data, and offline architecture.
## REGATA
1. Base: near-black background, purple UI panels, pink/cyan accents.
2. Header: prominent Inicio; live hearts and earned stars, from Race state (not artwork).
3. Art: regata.webp, single static image. Source: DA0A4CF9-E836-4D31-B6E7-9CAD7B179A87(1).jpeg (922 × 598); keep the entire image and its aspect ratio. Four boats in the illustration are not independently animated.
4. Progress line: editable geometry and two small boat markers (cyan player, red rival), finish flag. Render with Compose Canvas or lightweight vector resources, independent of raster image. Turn-based distance, not elapsed time. The existing five advances and rival pacing remain prototype tuning. Correct answer advances player and earns a star; wrong answer costs one heart, shows illustrative penalty motion without forward progress, rival progresses by turn. No extra racing simulation.
5. Text: existing Spanish question and four answer controls; one answer per turn. Show explanation and Continuar after answer; do not congratulate before success.
## ¡UPS!
1. Shared base and Inicio; actual remaining hearts and stars.
2. Title ¡UPS! / Respuesta incorrecta.
3. Art: ups.webp, one static image. Source: 917D004A-5D81-402E-AE71-52B2FDEB4C45(2).jpeg (869 × 780); preserve whole image including its existing frame.
4. Correct answer, explanation, Continuar. Do not permit repeat credit for the same turn.
5. Play local ups.ogg once on new wrong answer; no network, no forced wait.
## Handoff
Figma screens: https://www.figma.com/design/07Nr8yUqak3MJSrGmbuAw0?node-id=1-4 and https://www.figma.com/design/07Nr8yUqak3MJSrGmbuAw0?node-id=8-7 . Original-image slots 8:6 and 8:15; direct image upload failed due to DNS, so slots remain unfilled, not artistic substitutes.
The latest supplied originals were converted in full resolution to WebP quality 90: regata.webp 78,996 bytes; ups.webp 62,306 bytes. Pack with existing ups.ogg: 146,193 bytes. Final repository runtime path must be app/src/main/assets/vela_regata_assets.zip. Binary archive not yet uploaded. The rejected SVG artwork remains only historical design material; no runtime usage.
Next: upload approved asset pack, build preview, verify rendering and sound on Android, check real APK size, then optimize and test. No new dependencies, no new features in this step.

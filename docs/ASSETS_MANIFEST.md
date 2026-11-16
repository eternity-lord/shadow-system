# Assets Manifest

## Status
No bitmap art is bundled in this build. Every art slot resolves through
`core/AssetRegistry.kt`:

1. If `res/drawable-nodpi/<name>.webp|png` exists, it is used.
2. Otherwise a procedural Compose Canvas silhouette is drawn (size and detail grow with stage, colour from the soldier's palette).

Adding art later requires only dropping files with these names; no code change.

## Naming convention (spec section 14)

| Asset | File name | Status |
|---|---|---|
| Shadow soldier, stage 1–5 | `shadow_<key>_stage<n>.webp` | placeholder (procedural) |
| Wisdom overlay, tier 1–3 | `wisdom_<key>_<tier>.webp` | not wired into screens yet |
| Avatar per rank E–S | `avatar_rank_<e..s>.webp` | placeholder (procedural) |
| Monster per rank | `monster_<rank>_<n>.webp` | not used yet |
| Gate per rank | `gate_<rank>.webp` | not used yet |
| Category icon | `icon_cat_<name>.webp` | not used yet |
| Background | `bg_<name>.webp` | not used yet |

Soldier keys: `igris, beru, iron, tank, tusk, kaisel, fang, jima, nova, blue, scribe, bellion`.

## Sources
- Typeface: **Vazirmatn v33.003** (OFL), `app/src/main/res/font/vazirmatn_{regular,medium,bold}.ttf`,
  from https://github.com/rastikerdar/vazirmatn (downloaded from the GitHub codeload archive).
- App icon: hand-written vector drawable `drawable-nodpi/ic_launcher_foreground.xml` (original, no third-party art).
- Shadow soldier and avatar art: none bundled (see status above).

# Assets manifest

Every visual asset the app references, and what it is. Missing assets fall back to a coloured placeholder (`AssetImage`), so the app never crashes on a missing file.

## Raster images (`app/src/main/res/drawable-nodpi/`, 512×512 PNG)

| Name pattern | Count | Used by | Status |
|---|---|---|---|
| `gate_{e,d,c,b,a,s}.png` | 6 | Dungeon gate cards | Generated |
| `shadow_{igris…bellion}_stage1.png` | 12 | Soldier roster (stage 1) | Generated for 4 soldiers, the rest use placeholders |
| `avatar_rank_{e,d,c,b,a,s}.png` | 6 | Hunter avatar and rank-up | Generated |

## Missing (placeholder only)

- `shadow_<key>_stage2..5`: soldier stages 2–5. `AssetRegistry.soldierBestAsset` uses the highest existing stage, so the stage 1 image is shown with a stage label.
- `wisdom_<key>_1..3`: wisdom overlays (tiers 1–3). Tier is shown as text.
- `egg_placeholder`: soldier egg. Shown as a coloured placeholder.
- `monster_<rank>_<n>`: dungeon monsters. Not yet used in the UI.
- Layered avatar parts (body, hair, face, outfit, weapon, headgear, aura), stat overlays, class art, shop item art, backgrounds, frames, mood-pose images.
- Icons for money categories (`icon_cat_*`) and stat icons (`stat_*`).

## Fonts (`app/src/main/res/font/`)

- Vazirmatn (Persian UI font), from rastikerdar/vazirmatn v33.003 (OFL licence).

## Launcher icon

- `mipmap` adaptive icon (generated, see `res/mipmap-*`).

## Sounds and haptics

- Ambient sound: generated in code (`util/AmbientSound.kt`), no file. Brown noise.
- Haptics: platform feedback only (no files).
- Notification sound: none (silent channels).


## Batch: egg, stat emblems, throne, stage 2 (Igris, Beru)

| File | Used by | Notes |
|---|---|---|
| `egg_placeholder.png` | Hunter screen (soldier egg) | 512×512 |
| `stat_str.png`, `stat_int.png`, `stat_agi.png`, `stat_vit.png`, `stat_sense.png` | Avatar, top two stats | 512×512; text label when missing |
| `bg_throne.png` | Heavy item `bg_throne` (weekly boss reward) | 768×768; shown at 35% behind the hunter card when equipped |
| `shadow_<key>_stage2.png` for all 12 soldiers | Soldier card at stage 2 | 512×512; made from each stage-1 image for consistency |

Stage 3 done for ten soldiers (igris, beru, blue, bellion, fang, iron, jima, kaisel, nova, scribe). Still missing: stage 3 for tank and tusk, stages 4–5 for all 12, wisdom overlays (`wisdom_<key>_<tier>`), monsters, category icons, other backgrounds.

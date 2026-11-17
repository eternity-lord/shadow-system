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
| `wisdom_igris_1.png`, `wisdom_beru_1.png`, `wisdom_blue_1.png`, `wisdom_bellion_1.png` | Wisdom overlay tier 1 | 512×512 |
| `shadow_<key>_stage2.png` for all 12 soldiers | Soldier card at stage 2 | 512×512; made from each stage-1 image for consistency |

Stage 3 done for all 12 soldiers. Stage 4 done for all 12 soldiers. Stage 5 done for all 12 soldiers. Wisdom tier 1 art done for all 12 soldiers (`wisdom_<key>_1.png`). Tier 2 done for all 12 soldiers (`wisdom_<key>_2.png`). The overlay is shown on soldier art (60% alpha) at the highest tier that has art; soldiers without art show no overlay. Tier 3 done for ten soldiers (igris, beru, blue, bellion, fang, iron, jima, kaisel, nova, scribe). Still missing: tier 3 for tank and tusk. wisdom overlays (`wisdom_<key>_<tier>`), monsters, category icons, other backgrounds.

## Batch: wisdom tiers 3 (last two), monsters, gate background

| File | Used by | Notes |
|---|---|---|
| `wisdom_tank_3.png`, `wisdom_tusk_3.png` | Soldier overlay at tier 3 | Wisdom tier 3 now done for all 12 soldiers |
| `monster_e_1.png` ... `monster_s_1.png` (6 files, one per gate rank) | Active dungeon run | 512×512 |
| `bg_gate.png` | Shop item `bg_gate` | 768×768; shown at 25% behind the dungeon screen when equipped |

Still missing: monster variants (`_2`, `_3`) if wanted, category icons (`icon_cat_*`), other backgrounds, soldier and wisdom art beyond this batch.

## Batch: category icons

| File | Used by | Notes |
|---|---|---|
| `icon_cat_outfit.png`, `icon_cat_weapon.png`, `icon_cat_aura.png`, `icon_cat_frame.png`, `icon_cat_background.png` | Avatar shop category headers | 512×512 |

Still missing: more background art (only `bg_gate` and `bg_throne` exist for the shop), monster variants beyond level 1.

## Outfit look images (PNG, 512×512)

`avatar_look_outfit_{street,sport,leather,armor,shadow}.png` (5 files). Each is the rank-A hunter redrawn in that outfit, made from `avatar_rank_a.png` as the reference so the figure, pose and style match. When an outfit is equipped and its look exists, it replaces the rank picture (no base tint).

## Weapon sprites (PNG, transparent, up to 256 px)

`avatar_weapon_{dagger,sword,blade}.png`. Generated on a flat green background and keyed to transparency. They are drawn at the lower right of the avatar (48 dp). Without a sprite, a vector blade is drawn.

## Aura and frame sprites (PNG, 512×512, transparent)

`avatar_aura_{blue,purple,gold,dark}.png` (behind the avatar, 112 dp) and `avatar_frame_{basic,neon,sovereign}.png` (over the avatar, 104 dp). Generated on green and keyed to transparency. Without a sprite, the coloured square border or vector frame is drawn.

## Vector fallbacks over the avatar

Used only when a sprite is missing. Drawn by `ui/components/AvatarLayers.kt`, with colours and sizes in `core/AvatarLayerRules.kt`:
- outfit (fallback only): a torso band in the item colour,
- weapon: a blade whose length grows from dagger to dark blade (3 weapons),
- aura: the border colour follows the equipped aura (4 auras),
- frame: the border width and colour follow the equipped frame (3 frames).

Auras and frames are still vector. Per-item PNG layers for them would need a matching base silhouette; the outfit looks above avoid that by redrawing the whole figure.

## Mood pose pictures (PNG, 512×512)

`avatar_pose_{standing,determined,tired}.png`, made from `avatar_rank_a.png` as reference. Chosen by the day's score (`TitleRules.pose`): 80 and up standing, 50 and up determined, 20 and up tired but satisfied. Priority on the avatar: equipped outfit look, then pose, then the rank picture.

Missing: the low-energy pose (score under 20). That case shows the rank picture. Its image was not generated because the image limit for the turn was reached. Add `avatar_pose_low.png` and map pose 4 in `poseAssetName` to finish it.


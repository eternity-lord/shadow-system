# Assets Manifest

All artwork is either generated for this project with the image generator or drawn in code. Nothing is downloaded at runtime (the app is offline).

## Bundled raster images (`app/src/main/res/drawable-nodpi/`)

| File | Source | Used by | Notes |
|---|---|---|---|
| `gate_e.png` … `gate_s.png` (6 files) | Generated with the image generator for this project (prompt: dark fantasy rune gate, one per rank, no text, no people) | Dungeon tab, gate card (`AssetRegistry.gateAssetName`) | Downscaled to 512×512 PNG. About 2.1 MB total. |
| `shadow_{igris,beru,iron,tank,tusk,fang,jima,blue,kaisel,nova,scribe,bellion}_stage1.png` (12 files) | Generated with the image generator (one dark bust portrait per soldier, no text) | Hunter tab, soldier tiles (`AssetRegistry.soldierAssetName(key, 1)`) | Downscaled to 512×512. About 4.5 MB total. |
| `avatar_rank_{e,d,c,b,a,s}.png` (6 files) | Generated with the image generator (one dark bust portrait per rank, no text) | Hunter tab avatar (`AvatarData.rankAssetName`) | Downscaled to 512×512. About 2.1 MB total. |

Higher soldier stages (`shadow_<key>_stage2..5`), wisdom art and monster art are not planned for this build. `AssetRegistry` falls back to a procedural placeholder for them.

The app works without these files: `AssetImage` draws a procedural silhouette when no drawable with the name exists. Adding the PNG with the same name replaces it with no code change.

## Fonts (`app/src/main/res/font/`)

| File | Source | License |
|---|---|---|
| `vazirmatn_regular.ttf`, `vazirmatn_medium.ttf`, `vazirmatn_bold.ttf` | Vazirmatn v33.003, rastikerdar/vazirmatn (codeload) | SIL Open Font License 1.1 |

## Launcher icon

`mipmap-anydpi-v26/ic_launcher.xml` and `drawable-nodpi/ic_launcher_foreground.xml`: vector drawables written in code.

## Procedural placeholders (no file)

`AssetRegistry` resolves names such as `shadow_<key>_stage<1-5>`, `wisdom_<key>_<1-3>`, `avatar_rank_<e..s>`, `monster_<rank>_<n>`, `rank_<rank>`, `stat_<stat>` and `icon_cat_<name>`. If a drawable with that name is missing, `AssetImage` draws a `ProceduralPlaceholder` (a Canvas silhouette). See "Still to generate" above for the exact files that remain.

## Not used

No photos, no third-party icons, no sounds, no network assets.

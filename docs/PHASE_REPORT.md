# Phase Report

Spec reference: `docs/original-prompt.pdf` section 16 (phase order).

## Phase 0 — Setup — DONE (commit `3ebccc9`)
- Original spec PDF committed to `docs/original-prompt.pdf`.
- Gradle wrapper 8.7, AGP 8.5.2, Kotlin 2.0.20, Compose BOM 2024.09.00, Room 2.6.1, minSdk 26, target 34.
- Vazirmatn v33.003 fonts bundled (Regular/Medium/Bold).
- Manifest without INTERNET permission, RTL support, Persian `strings.xml` and `values-fa`.
- Documentation skeleton: README, DECISIONS, ASSETS_MANIFEST, PROGRESS, this report.

## Phase 1 — MVP — MOSTLY DONE (latest green commit `44cd81f`)
Build status: `./gradlew assembleDebug testDebugUnitTest` **passes on GitHub Actions** (run 43833013809, commit `44cd81f`).

Added since the earlier report:
- Room schema v2 (`MIGRATION_1_2`) with budgets, recurring rules, goals, debts and key-value settings.
- Onboarding (4 screens), PIN lock gate, FLAG_SECURE on the whole window.
- Reports screen (donut, six-month bars, 30-day line) drawn with Compose Canvas.
- Settings screen: hide-balances toggle, PIN set/remove, encrypted backup export/restore (AES-256-GCM, PBKDF2), CSV export.
- Money plan screen: budgets, recurring rules, savings goals (bosses), debts and installments.
- Reports and settings are wired into the navigation; the Hunter tab links to Settings.

Delivered:
- Jalali calendar (own implementation, validated against jalaali-js constants) with leap-year, month-boundary, round-trip and weekday tests.
- Game math: XP curve `round(100·n^1.4)`, ranks E–S, stat and soldier curves, quest reward table, daily score (40/20/20/20), Shadow of Yesterday bonus, partial dungeon XP, comeback ×1.5 for 3 quests.
- Quest planner: Great 3+5, Normal 3+3, Hard 1+1 (low-energy); 48 Persian templates with 1–5 steps.
- Room database (7 tables, exported schema) and repositories for game and money (money stored in rials, displayed in toman).
- Screens: Now (score, level, mood, mandatory quests), Quests (all quests and steps), Dungeon (gate timer, early exit with confirmation), Money (expense/income, categories, month summary, delete with confirmation), Hunter (level, rank, stats, class, soldier roster).
- Single activity, bottom navigation with five tabs, RTL layout, Persian digits, 48dp touch targets.
- 40+ unit tests (Jalali, game math, planner, day rules, text/money).
- CI workflow that builds the APK, runs tests, and posts errors as a commit comment on failure.

Not yet delivered from Phase 1 in the spec:
- Morning and night reminders are added: AlarmManager, optional in Settings, `POST_NOTIFICATIONS` only.
- Swipe-complete with 5s undo (undo banner exists; swipe gesture not wired).
- Numpad quick-add, receipt photo, tags. Gold shop is a 'coming soon' card (see DECISIONS.md).
- Foreground-service dungeon timer, monster HP, loot journal, weekly boss.
- Full soldier system (summon ritual, XP, evolution, voice lines in UI, wisdom overlay).
- Avatar layering, stat-based class visuals, rank trial boss, rank-up cutscene.
- Generated or downloaded artwork (only placeholders; see ASSETS_MANIFEST.md).

## Phase 2 — Depth — PARTIALLY DONE
Done (compiled, CI green): budgets with 80/100% warnings, recurring rules with Jalali catch-up (`MAX_CATCH_UP` 60), savings goals as bosses, debts and installments, reports (donut/bars/lines), CSV export.
Not done: search and filters UI (search logic may exist in the repository but is not fully surfaced), notifications, rank trial boss, Gold shop.

## Phase 3 — Completion — PARTIALLY DONE
Done: AES-256-GCM backup with PBKDF2 (UI in Settings), PIN lock (PBKDF2 hash), FLAG_SECURE, hide-balances toggle.
Not done: biometric unlock, artwork integration beyond procedural placeholders, accessibility pass, R8 release build with signing placeholder.

## Phase 1 — MVP — DONE, except items listed under "Open"
Verified on CI (`assembleDebug assembleRelease testDebugUnitTest`) at `dcb6107`.
Delivered: onboarding, PIN lock, Jalali calendar, game math, quest planner, Now/Quests/Dungeon/Money/Hunter tabs, numpad quick-add, swipe to complete with a 5-second undo, daily reminders, gate artwork.

## Phase 2 — Depth — MOSTLY DONE
Delivered: gold shop with a dungeon boost, rank-up dialog, budgets (80%/100% warnings), recurring rules with Jalali catch-up, savings goals as bosses, debts and installments, reports (donut, bars, 30-day line), search and income/expense filter, tags (via `#` in notes), CSV export, rank trial (weekly, 5 quests in 7 days) that gates rank-up, reminders.

## Phase 3 — Completion — PARTLY DONE
Delivered: AES-256-GCM encrypted backup (UI in Settings), PIN lock (PBKDF2), biometric unlock, FLAG_SECURE, hide-balances toggle, R8 release build in CI, numpad.

## Open (honest list)
- Gold shop: one real item (dungeon boost ×1.5 XP, 40 gold). More items are not designed yet.
- Rank-up: a dialog appears when the rank increases. There is no full cutscene.
- Artwork: 6 gate images, 12 soldier portraits (stage 1) and 6 rank avatars are real. Higher soldier stages, wisdom art and monster art still use procedural placeholders (see ASSETS_MANIFEST.md).
- Accessibility: touch targets are 48dp and text scales, but no TalkBack audit has been run (no device in the sandbox).
- Release signing: a real key was generated and is used for local release builds from `release-keystore/` (git-ignored). CI release builds are still debug-signed because the sandbox cannot write repository secrets. See DECISIONS.md.
- Not run: emulator or device tests; no screenshots verified visually.

## Verification
- The sandbox has no JDK or Android SDK, so compilation and tests run on GitHub Actions only.
- Latest CI run: success on `dcb6107`.

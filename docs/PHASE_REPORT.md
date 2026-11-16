# Phase Report

Spec reference: `docs/original-prompt.pdf` section 16 (phase order).

## Phase 0 — Setup — DONE (commit `3ebccc9`)
- Original spec PDF committed to `docs/original-prompt.pdf`.
- Gradle wrapper 8.7, AGP 8.5.2, Kotlin 2.0.20, Compose BOM 2024.09.00, Room 2.6.1, minSdk 26, target 34.
- Vazirmatn v33.003 fonts bundled (Regular/Medium/Bold).
- Manifest without INTERNET permission, RTL support, Persian `strings.xml` and `values-fa`.
- Documentation skeleton: README, DECISIONS, ASSETS_MANIFEST, PROGRESS, this report.

## Phase 1 — MVP — PARTIALLY DONE (commits `f4fd82e` … latest on branch)
Build status: `./gradlew assembleDebug testDebugUnitTest` **passes on GitHub Actions** (run 43830642358).

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
- Onboarding (≤5 screens), morning briefing / night report as notifications, notification scheduler.
- "I'm stuck" (make smaller / later), swipe-complete with 5s undo.
- Numpad quick-add, receipt photo, tags.
- Foreground-service dungeon timer, monster HP, loot journal, weekly boss.
- Full soldier system (summon ritual, XP, evolution, voice lines in UI, wisdom overlay).
- Avatar layering, stat-based class visuals, rank trial boss, rank-up cutscene.
- Generated or downloaded artwork (only placeholders; see ASSETS_MANIFEST.md).

## Phase 2 — Depth — NOT STARTED
Planned: budgets with 80/100% warnings, recurring rules with Jalali catch-up, savings goals as bosses, debts and installments, reports (donut/bars/lines), search and filters, CSV export, notifications, onboarding, comeback polish, rank trial, Gold shop.

## Phase 3 — Completion — NOT STARTED
Planned: AES-256-GCM backup with PBKDF2, app lock (PIN + biometric), FLAG_SECURE on money screens, hide-balances toggle, artwork integration, accessibility pass, release build with R8 and signing placeholder.

## Verification
- Local sandbox has no JDK/Android SDK and Google Maven is unreachable, so all compilation and tests ran on GitHub Actions.
- Not run: emulator/device tests, crash testing on device, UI screenshots.
- Room schema JSON is generated into `app/schemas` on build; it is not committed yet.

## Honest status
The app is an **MVP of Phase 1**, not the full specification. Phases 2 and 3 are not implemented.

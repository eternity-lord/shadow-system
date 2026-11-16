# Phase report — Shadow System

Status is written from the code, not from plans. "Built" means the code exists and compiles on CI. Device testing has not been done.

Latest green CI on the branch: `5e` batch — run 43887565776 (weekly boss, budget and crypto tests). Earlier batches are listed below.

## Built (compiled on CI)

**Core game**
- Jalali calendar (own implementation), day boundary at 04:00 (`DayRules`).
- XP curves, ranks E–S, stat and soldier curves (`GameMath`), daily score, comeback ×1.5, rank trial (weekly, gates rank-up).
- Quest planner (Great / Normal / Hard), 48 Persian templates, step-by-step completion with 5-second undo, "I'm stuck" (make smaller).
- Numpad quick-add, swipe to complete, reminders (`POST_NOTIFICATIONS` only, optional).

**Dungeons (spec 5)**
- Foreground service timer (`DungeonService`, `specialUse` type) with a live notification. The run is stored in SharedPreferences, so it resumes after the process is killed.
- Pause and resume (pause time does not count as work). Monster HP drops with the timer, and regenerates slightly while paused (cosmetic).
- Haptic tick each full minute (toggle). Ambient brown-noise loop (toggle, off by default).
- Cancel gives partial XP. End gives loot: XP, gold, 10% rare-item roll, and an optional one-line journal note saved to the run.

**Soldiers (spec 6)**
- Roster of 12 (`SoldierData`). Rename (max 20 characters). Detail sheet with stats, state and voice line.
- Egg until 7 active days, then a skippable 5-second summoning ritual.
- Progress is derived from completed linked quests (`quests.soldierKey`, from templates). 20 XP per linked quest, level from `round(50·n^1.3)`.
- States: awake / tired / sleeping (2 and 5 days). Sleeping shows a gentle one-task hint.
- Stage 1–5 from level. Wisdom tier 0–3 from variety and steps (text label; overlay art not yet).
- Evolution cutscene: 3 s, skippable, with the new stage's voice line. Plays once per stage increase.
- Bellion locked until rank S and 100 total active days.

**Avatar and profile (spec 7)**
- Base look per rank (6 avatar PNGs). Class from the top two stats, fixed per week (recomputed when a new week starts).
- Stat radar (Canvas). Mood pose from today's score (4 text poses).
- Titles (5 of 6 unlockable; `early_bird` stays locked because time-of-day is not tracked).
- Achievements (9 of 10 unlockable; `boss_done` unlocks with the weekly boss).
- Avatar shop with Gold cosmetics (outfits, weapons, auras, frames, backgrounds). Heavy items are locked until boss or trial rewards. Items can be worn, one per category.
- Rank-up overlay: 5 seconds, tap to skip.

**Weekly boss (spec 5 / phase 3)**
- Target 120 dungeon minutes and 3 completed quests this week. Each is half of the HP bar.
- Defeated boss: one-time reward (200 XP, 150 gold) and a heavy cosmetic (alternates weekly).

**Money (spec 8–9)**
- Expenses and income in rials, shown in toman. Categories, month summary, search and income/expense filter.
- Budgets with 80% warning and 100% over state. Recurring rules with Jalali catch-up (cap 60). Savings goals as bosses. Debts and installments.
- Reports: donut, six-month bars, 30-day line (Compose Canvas). CSV export.
- Money entries never give XP or Gold (spending is separate from the game economy).
- Wallets with an opening balance. Each entry can be booked to a wallet; entries without one count toward the first wallet. Balances are live.
- Transfers between wallets. They are not spending: no XP, no Gold, and they do not count in budgets or reports.
- Cheques, issued or received, with a Jalali due date (day/month/year steppers), status (cleared, overdue, due in 3 days, later), mark-cleared and delete with confirmation.
- Encrypted backup and CSV include wallets, transfers, cheques and each transaction's wallet. Old backups (without these records) still restore.
- Budget rollover on the plan screen: unspent amount from last month carries forward, capped at one base limit; overspending does not carry a debt. Known approximation: last month's limit is taken as the current limit (budgets keep no history).
- Installments: a debt can be split into N installments. Each payment adds one installment (the last absorbs rounding) and the progress is shown.
- Reduce motion setting: shortens the rank-up overlay animation (`rememberReduceMotion`).
- Morning and night report cards and a five-screen onboarding (one question, soldier suggestion). Rule tests in `DailyReportTest`.
- Body doubling: the most recently summoned soldier bobs beside the user while the dungeon timer runs (stops when paused or with reduce-motion). Uses stage-1 art until higher stages exist.
- Savings estimate rule (`FinanceRules.savingsEstimateDays`), tested. Not yet shown on goal cards.
- Rank-up overlay has an explicit skip button (in addition to tap-to-dismiss).
- Unit tests: recurrence catch-up and cap, month-end anchors, installment split, rollover, wallet balances and transfers, cheque status, Jalali leap years, month lengths, clamping and round trip.

**Inbox, screens and settings**
- Inbox "park a thought" card on the Now tab, with delete confirmation.
- Onboarding (4 screens), PIN lock (PBKDF2 hash), biometric unlock, FLAG_SECURE on the whole window, hide-balances toggle.
- Encrypted backup and restore: AES-256-GCM, PBKDF2 key derivation, with a magic header.
- Debug-only 60-day seed generator (`app/src/debug`; a no-op stub in release).

**Data and quality**
- Room database v5 with migrations 1→2, 2→3, 3→4 and 4→5 (wallets, transfers, cheques, `transactions.walletId`). Exported schema.
- Unit tests: Jalali, game math, planner, rank trial, day rules, text and money, dungeon rules and timer math, soldier progress and roster, title rules, weekly boss, budget thresholds, backup crypto round trip.
- Release build signed with the project keystore (committed by the owner's decision; see DECISIONS.md).

## Not built yet (spec items still open)

These keep PR #1 open.

- **Monsters:** one image per gate rank (E to S) is shown in the active dungeon run. Background `bg_gate` shows behind the dungeon screen when equipped. Category icons show in the avatar shop.
- **Art (needs image generation, not code; done: egg, five stat emblems, throne background, stage 2 for all 12 soldiers, stage 3, 4 and 5 for all 12. Wisdom tier 1 for all 12 soldiers, tier 2 and 3 for all 12; shown on the soldier art. Still missing: wisdom overlays, monsters, icons, other backgrounds):** soldier stages 2–5 (only stage 1 exists; the app falls back to it), wisdom overlays, egg image, monsters, backgrounds and category icons. Placeholders are in `ASSETS_MANIFEST.md`.
- **Avatar layers:** the layer system exists in code (base image, aura ring when an aura item is equipped, stage badge, two stat overlays). Body, hair, face, outfit and weapon layers wait for art.
- **Evolution cutscene:** the old stage fades out while the new stage fades in and grows. A true silhouette morph needs the stage art.
- **Accessibility and security on a device:** TalkBack, PIN and biometric unlock are not checked on a real device from this sandbox. The code uses labels and 48 dp targets.

## Built in the last batches (summary)

- Dungeon: body-doubling soldier (the linked one, or the first summoned), gate length per rank (5–120 min, steps of 5), optional soldier and quest link saved on the session, rare loot flag per session.
- Quests: a quest can be relinked to another summoned soldier from its card (cycles through "no soldier" and each summoned soldier).
- Reports: morning and night card on the Now tab, and a full daily report screen (score and trend, quests, XP, dungeon time, rare loot, spending). Spending never gives XP or gold.
- Onboarding: four intro pages, then four question screens (focus, hardest part, daily time, reminders), with back and a required answer on each question. Reminders are switched on from the last answer. Answers are stored in kv for later use; the daily time is not yet used to change gameplay.
- Reminders: the morning and night notifications show today's real numbers (open quests, score) when available.
- Goals: savings estimate on each goal card (average daily saving since the goal was made).

## Verification
- The sandbox has no JDK or Android SDK. Compilation and unit tests run on GitHub Actions only.
- CI for every pushed batch is checked. Errors come from the commit comment (the `gh run view --log` path does not work from the sandbox).

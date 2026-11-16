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

**Inbox, screens and settings**
- Inbox "park a thought" card on the Now tab, with delete confirmation.
- Onboarding (4 screens), PIN lock (PBKDF2 hash), biometric unlock, FLAG_SECURE on the whole window, hide-balances toggle.
- Encrypted backup and restore: AES-256-GCM, PBKDF2 key derivation, with a magic header.
- Debug-only 60-day seed generator (`app/src/debug`; a no-op stub in release).

**Data and quality**
- Room database v4 with migrations 1→2, 2→3 and 3→4. Exported schema.
- Unit tests: Jalali, game math, planner, rank trial, day rules, text and money, dungeon rules and timer math, soldier progress and roster, title rules, weekly boss, budget thresholds, backup crypto round trip.
- Release build signed with the project keystore (committed by the owner's decision; see DECISIONS.md).

## Not built yet (spec items still open)

These keep PR #1 open.

- **Money:** wallets and transfers, receipt photos, cheques, and the Jalali date picker. Not verified in code.
- **Dungeon:** linking a dungeon to a quest or soldier, body-doubling soldier animation, and gate minutes that the user can change.
- **Soldiers:** user re-linking of quests to soldiers (only template links exist), the morph animation for evolution, and a wisdom overlay image.
- **Avatar:** layered body, hair, face, outfit, weapon and aura art (only rank avatars exist), stat overlays, and the old-avatar break-apart in the rank-up cutscene. Titles are not yet shown on the profile header.
- **Screens:** onboarding asks no questions and does not suggest soldiers. Night report and morning briefing use phrase banks but are not yet a full screen with delta and loot summary. Reduce-motion setting is not built.
- **Art:** soldier stages 2–5, egg image, wisdom overlays, monsters, backgrounds, icons. These are placeholders (see ASSETS_MANIFEST.md).
- **Quality:** TalkBack audit on a device, emulator or device tests, and a full process-death walkthrough. Accessibility has not been checked beyond the code.
- **Docs:** README and PROGRESS are updated to this state. Check them after the next batch.

## Verification
- The sandbox has no JDK or Android SDK. Compilation and unit tests run on GitHub Actions only.
- CI for every pushed batch is checked. Errors come from the commit comment (the `gh run view --log` path does not work from the sandbox).

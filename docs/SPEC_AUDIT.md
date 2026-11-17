# Spec audit (sections 3–16 of the original prompt)

Status key: **Done** = built and tested or checked in code · **Partial** = built, but a rule is missing · **Missing** = not built yet.
This audit was run on 2026-11-16 against the code on the branch. It is the list of work still to do before PR #1 can be merged.

## Section 3 — core rules
| Item | Status | Notes |
|---|---|---|
| XP to next level round(100·n^1.4), rank thresholds | Done | `GameMath`, `GameMathTest` |
| Rank Trial gates rank change | Done | `RankTrialTest` |
| Stat levels round(60·n^1.35) | Done | `GameMath.statXpForLevel` |
| Quest XP/Gold table E–S | Done | `QuestPlanner` / `GameMath` |
| 20% per step, 80% on finish | Partial | check the split is applied on step and finish (not yet covered by a test) |
| Comeback bonus (≥2 missed days, first 3 quests ×1.5) | Done | `ComebackTest` added this round |
| Daily score 40/20/20/20, hard-day scaling | Done | `DailyReportTest`; check finance 20% and VIT 20% wording |
| Shadow of Yesterday, +10 Gold, 7/30-day chart, best-week badges | Partial | yesterday score and delta shown; check the +10 Gold bonus is applied and tested; badges not verified |

## Section 4 — quests
| Item | Status | Notes |
|---|---|---|
| Types: daily mandatory/optional, special, weekly, chain | Partial | chain and weekly types not verified in UI |
| Fields incl. steps 1–5, first step ≤ 2 min, energy, soldier link | Done | planner tests |
| ≥ 40 Persian templates | Done | 51 templates |
| Manual creation, one screen | Done | |
| Auto-split into Prepare/Start/Main/Finish | Done | `QuestPlannerTest` |
| Daily generation by mood (Great 3+5, Normal 3+3, Hard 1+1) | Done | `QuestPlannerTest` |
| 'I'm stuck': Make it smaller and Later | Partial | Smaller is built; 'Later' (move to end of day, no penalty) to check |
| Swipe to complete, undo 5 s | Partial | swipe and undo exist; the 5-second window not verified |

## Section 5 — dungeons
| Item | Status | Notes |
|---|---|---|
| Gate by rank, user-changeable minutes | Done | `GateRules` |
| Optional quest or soldier link | Done | |
| Monster HP falls with time, timer | Done | |
| Foreground service | Done | `DungeonService`, `specialUse` |
| Ambient sound, haptic ticks | Done | `AmbientSound`, haptics |
| Pause, regen cosmetic, partial XP on cancel | Done | `DungeonTimerMathTest` |
| Loot, rare cosmetic, journal note | Done | |
| Weekly boss: minutes + 3 quests, heavy reward | Done | `WeeklyBossTest` |
| Body doubling | Done | `SoldierBodyDouble` |

## Section 6 — soldiers
| Item | Status | Notes |
|---|---|---|
| Roster of 12, rename, link to quests | Done | |
| Re-link quest to another soldier | Done | |
| Egg → 5-second summon after 7 active days | Done | |
| Soldier XP round(50·n^1.3) | Done | |
| States awake / tired / sleeping, no scolding text | Done | |
| Two-axis evolution: power (days) and wisdom (variety) | Partial | power = stage by level (spec: total active days); wisdom tiers done. Decide and record in DECISIONS whether power uses days or level |
| 5 stages, stage names | Done | |
| Evolution cutscene 3 s, skippable, voice line | Partial | fade/grow morph done; 3 s timing and skip to check; the spec wants the previous silhouette to morph |
| ≥ 3 Persian voice lines per soldier per stage | Done | 180 lines (12 × 5 × 3) |
| Art: stages 1–5 for all 12, wisdom 1–3 | Done | PNG 512 px, see manifest (spec asks WebP 1024 px) |

## Section 7 — avatar
| Item | Status | Notes |
|---|---|---|
| Layered avatar (body, hair, face, outfit, weapon, headgear, aura) | Partial | aura ring and badges done; body/hair/face/outfit/weapon layers need art |
| Base look by rank | Partial | 6 rank images exist; check that they match the rank descriptions |
| Stat overlays (top two stats) | Partial | stat emblems and text badges done; the per-stat overlays (shoulders, glasses etc.) need art |
| Classes (Warrior, Scholar, Assassin, Sage) weekly | Done | `class_week` |
| Mood pose from the day's score | Partial | rules done; pose art missing |
| Rank-up cutscene 5 s, skippable, once per rank | Partial | overlay exists; check the 5-second length and skip |
| Shop: outfits, weapons, auras, frames, backgrounds | Done | |
| Heavy items from boss/trial rewards only | Done | |
| Titles, profile radar, soldier grid, stage badges | Done | |

## Section 8 — money
| Item | Status | Notes |
|---|---|---|
| Store Long rials, show toman, setting to switch | Partial | toman shown; the switch setting not verified |
| Persian digits, separators, input in both digit sets | Done | |
| Wallets, opening balance, transfers | Done | |
| Transactions: type, wallet, category + subcategory, Jalali date, note, tags, receipt, location | Partial | tags and location text not in the model; subcategory not in the model |
| Quick add: 3 taps, numpad, frequent categories | Done | floating button opens a sheet with the numpad, categories ordered by use (all time), subcategory, location, optional date, and save (`MoneyScreen`) |
| Categories: default set with icons; add, edit, archive | Partial | table `categories` (v8) with the default set; add, rename (updates transactions, budgets, recurring rules) and archive in the Money tab. Missing: category icons and colours in the UI, and categories are not in the encrypted backup yet |
| Recurring rules daily/weekly/monthly/yearly, catch-up on open, pause | Done | `RecurrenceAndInstallmentTest` |
| Budgets per category and overall, 80%/100%, rollover | Done | |
| Savings goals with estimated finish | Done | `SavingsEstimateTest` |
| Debts (direction, partial payments, reminders) | Done | due date per debt (migration v9); payments; morning and night reminders count debts and cheques due within 3 days or overdue (`DueRulesTest`) |
| Installments and cheques with status and reminders | Done | installments fall on the same day each Jalali month from the first due date; cheque status; the same due-soon reminder count (`DueRulesTest`). Installment rows show the next due date by count, not as a full schedule list |
| Reports: monthly/yearly, donut, bars, trend, comparison, top expenses, daily average | Done | donut by category, six-month bars, 30-day line, this month vs last month (%), daily average, 12-month total, five largest expenses. The yearly view is a 12-month total, not a year-over-year chart |
| Search and filters: text, date range, category, wallet, amount range, tags | Done | text, income/expense, category, wallet, amount range (toman), date presets (7/30/90 days, all time), #tag in note. Missing: custom date picker for the range |
| CSV export and encrypted backup (AES-256-GCM, PBKDF2) | Done | `BudgetAndCryptoTest` |
| App lock PIN (salted hash), biometric, hide balances, FLAG_SECURE | Done | FLAG_SECURE set in `MainActivity` |

## Section 9 — integration
| Item | Status | Notes |
|---|---|---|
| Transaction gives small XP to INT and Iron soldier, max 10/day | Done | `MoneyRules` |
| 'Log all today's expenses' quest, confirm button | Done | |
| 'Stay under budget today' quest | Done | |
| Savings goal as boss, heavy reward on finish | Done | goal progress shown; the deposit that reaches the target grants heavy frame `frame_sovereign` once (`FinanceRules.goalJustCompleted`, tested) |
| Spending never gives XP or Gold | Done | |

## Section 10 — screens
| Item | Status | Notes |
|---|---|---|
| Five tabs: Now, Quests, Dungeon, Money, Hunter | Done | |
| Onboarding: 5 screens max; welcome, name/avatar base, 3 questions (goal, hardest area, wake time), notifications, done | Done | 4 intro pages, then name, avatar base (tint), wake time, focus, struggle, daily minutes, reminders. The wake hour sets the morning reminder. The override of the 5-screen limit is in DECISIONS 36. Avatar base is a tint until the layered body art exists |
| Morning briefing and night report | Done | |
| Notifications linked to reports | Done | real numbers |

## Section 11 — ADHD features
| Item | Status | Notes |
|---|---|---|
| Short varied notifications, quiet hours | Partial | fixed times 08:00 and 21:00; quiet hours built (default 22:00–07:00, set in Settings, `QuietHoursTest`); varied text partly |
| 'I'm stuck', hard day, comeback, body doubling | Done | |
| Low motion setting | Done | affects overlays |
| Haptics, large buttons, undo | Done | |
| 'Park a thought' inbox | Done | |
| Sound toggles | Partial | ambient sound toggle on the dungeon screen; no settings toggle |
| No streak-loss mechanics | Done | |

## Section 12 — tone
| Item | Status | Notes |
|---|---|---|
| Phrase banks ≥ 10 per category (9 categories) | Done | 12 each |
| Persian UI text, Jalali dates | Done | |

## Section 13 — data model
| Item | Status | Notes |
|---|---|---|
| Tables for profile, stat progress, quests, steps, daily scores, dungeon sessions, soldiers, wallets, transfers, cheques, transactions, budgets, recurring rules, savings goals, debts, inbox | Done | Room v7, migrations from v1, exported schema |
| Tables for Category, Tag, Item, InventoryItem, Title, Achievement, AvatarState, Boss, JournalNote, Settings, Installment, DebtPayment | Partial | stored in `kv` or in other tables (e.g. journal note on the session, installments inside debts). Record this in DECISIONS or split into tables |
| Foreign keys and indexes | Partial | verify indices on quest and transaction day columns |
| Debug-only 60-day seed | Done | `DebugSeed` |

## Section 14 — art
| Item | Status | Notes |
|---|---|---|
| AssetRegistry with placeholders | Done | |
| Manifest | Done | `ASSETS_MANIFEST.md` |
| Format and sizes: WebP, soldiers 1024 px, wisdom 1024 px, bg 1080×1920 | Partial | PNG at 512 px (soldiers, wisdom, monsters), 768 px backgrounds. Deviation: recorded in DECISIONS |
| Names: avatar_stat_<stat>_<1-3>.webp | Partial | files are stat_<stat>.png (one per stat, no tier) |
| Art priority: 6 rank images, 5 main soldiers with 5 stages | Done | all 12 soldiers have stages 1–5 |

## Section 15 — quality
| Item | Status | Notes |
|---|---|---|
| Unit tests for XP, rank, planner by mood, daily score, comeback, Jalali, budget, recurrence, savings, backup | Done | comeback added this round |
| Compose previews for main components | Partial | cards and controls: button, radar, avatar stack, daily report (morning and night), money summary, category chips, entry card, transaction row. Full screens are not previewed because they need ViewModels |
| LazyColumn keys, no main-thread blocking | Partial | keys used in some lists; check all lists |
| Process death, timezone change, 04:00 day boundary | Done | `DayRules` boundary 04:00 |
| Accessibility: content descriptions, font scaling, contrast | Partial | descriptions added to art; device check not done |
| Release build with R8 and signing config | Done | |
| README with art replacement section | Partial | check the README covers replacing placeholder art |

## Section 16 — phases
Phases 0–3 are built in code. The Phase 3 acceptance ("every feature in this document works") is not met until the Missing and Partial rows above are closed.

## Next steps, in order
1. Previews for the main cards and screens. (Section 15)
2. Verify the Partial rows that need a check in code, and fix any gaps found.
3. Art: WebP conversion and the stat overlay files named per tier.

## Open after section 7–9 review

Checked against the text of sections 7, 8 and 9 in `docs/original-prompt.pdf`.

- **Quick add (sec. 8):** done as a floating button with a bottom sheet, categories ordered by use, and a date option (DECISIONS 50, 51). Still open: "last used" wallet as default.
- **Transactions (sec. 8):** subcategory and location are stored (Room v11, backup fields 11 and 12) and shown in the row. Backdating through the Jalali picker is done (DECISIONS 51). Backdated entries earn no XP.
- **Savings goals (sec. 8, 9):** deadline (optional, Jalali picker) and linked wallet are stored and shown, with days left or passed. Still open: the goal's estimate does not yet check the linked wallet balance.
- **Categories icons and colours (sec. 8):** colours are stored but not shown; no icons yet.
- **Art and avatar (sec. 7):** outfit, weapon, aura and frame are drawn as vector layers (DECISIONS 52). Still open: PNG layer art for body, hair and headgear, the evolution silhouette, and the mood pose art. The mood pose is coded, but no pose art exists yet.
- **Verified in code:** class names from the top two stats (`GameMath.className`) with the weekly class stored; rank-up overlay of 5 s, skippable; rial/toman setting; daily, weekly, monthly and yearly recurrence; logging XP capped at 10 per day; budget-pace quest logic exists in `GameRepository` (not yet exercised on a device).
- **Reminder taps:** done. Morning opens the Now tab and night opens the daily report (`PendingRoute`, `ReminderReceiver`).
- **Onboarding:** done. Name, avatar base, wake hour (sets the morning reminder), and the four question screens are all in `OnboardingScreen`, and `AppNav` saves the answers.
- **Reduce motion:** done in `BodyDouble` (no bob when on).
- **Goal and wallet (sec. 9):** the goal row warns when the linked wallet balance cannot cover the rest of the goal.
- **Section 15:** day boundary at 04:00 has a test (`DayRulesTest`); category chips use stable keys.
- **Reports (sec. 11):** yearly chart (12 months) and a trend (last 3 months against the 3 before) are done.
- **PIN and biometric:** built earlier (PBKDF2 hash, biometric unlock). Not re-checked on a device in this round.

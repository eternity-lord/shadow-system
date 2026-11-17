# Decisions (شکاف‌های تصمیم)

The spec was followed phase by phase. Where the spec was ambiguous or too large for
one pass, the simplest reasonable option was chosen and recorded here.

| # | Topic | Decision | Reason |
|---|-------|----------|--------|
| 1 | Dependency injection | Manual `AppContainer` in `ShadowApp`, not Hilt | Fewer build-time risks (no KSP/Hilt codegen), smaller APK, still testable through constructors. Spec lists Hilt; deviation accepted. |
| 2 | Background work | No WorkManager/foreground service in this build | Dungeon timer uses wall-clock start, so it stays correct when backgrounded. Notifications are a Phase 2 item. |
| 3 | Biometric app lock | Not in this build | Needs biometric + FLAG_SECURE review; listed for Phase 2/3. |
| 4 | Jalali calendar | Own implementation (Borkowski, same constants as jalaali-js), unit tested | Spec requires own implementation with leap-year tests. |
| 5 | Money unit | Stored in rials (Long), shown in toman | Spec: long rials internally, toman by default. |
| 6 | Digits | Persian digits in output, Persian/English digits accepted on input | Spec. |
| 7 | Quest XP split | Each step pays 20% of the quest's XP split across its steps; the last step pays the remainder (so totals equal the reward exactly) | Spec says "each step 20%, finish 80%"; this is the only reading where the total equals the reward. |
| 8 | Rank-up trial | Rank follows level thresholds directly; the weekly Rank Trial boss is Phase 2 | Keeps MVP small; the level→rank table is correct. |
| 9 | Comeback bonus | Missed ≥2 full days → next 3 quests get ×1.5 XP (flagged per quest at generation) | Spec. |
| 10 | Day boundary | 04:00 local time (`DayRules`) | Spec default. The "today" key is fixed when the view model is created; a session open across 04:00 needs a restart (Phase 2 fix). |
| 11 | Dungeon | Gate chosen from the user's rank, default minutes from spec table, timer in app only, partial XP on early exit, completed run pays 2× rank quest XP to INT and gold | Spec sections 5 & 3. Foreground service deferred. |
| 12 | Soldiers | Roster and voice lines in code (12 soldiers × 5 stages × ≥3 lines). Soldier unlock = matching stat at level 5 (Bellion: rank S). Summoning ritual / XP per soldier deferred | MVP scope. |
| 13 | Assets | `AssetRegistry` resolves `shadow_<name>_stage<n>` etc. by name; falls back to a procedural Canvas silhouette. No bitmap art was generated in this build | No image generation/download was possible for the sandbox pipeline in this session; listed in ASSETS_MANIFEST.md. |
| 14 | Strings | Persian UI text in `res/values` and `res/values-fa` (identical, Persian is the only language). Phrase bank and quest templates are Persian content kept as Kotlin data (they are content, not UI chrome) | Spec says UI text in strings.xml; content libraries are data. |
| 15 | Money input | Plain numeric text field + category chips (not a 3-tap numpad) | Numpad is a Phase 2 polish item. |
| 16 | Onboarding | Not in this build; first launch goes straight to mood selection on Now | Onboarding is Phase 2. |
| 17 | Backup/CSV/PIN lock | Not in this build | Phase 2/3. |
| 18 | Network | No INTERNET permission in the manifest; no analytics/ads/accounts | Spec hard rule. |
| 19 | Room schema | `exportSchema = true`, schema JSON written to `app/schemas` on build; version 1, no migrations yet | Spec requires exported schema. |
| 20 | Build verification | Built and tested on GitHub Actions (`.github/workflows/android.yml`) because the sandbox has no JDK/Android SDK and Google Maven is blocked | Environment limit, see PHASE_REPORT.md. |
| 21 | Release signing | Release uses the debug signing config as a placeholder | Spec allows a placeholder; no keystore is stored in the repo. |
| 22 | Icons | Material core icons only (no extended icon set) | Keeps dependency small. |

## Reminders (phase 1 leftover)
- Morning (08:00) and night (21:00) reminders use `AlarmManager.setInexactRepeating`. This needs no exact-alarm permission. Alarms do not survive reboot, so they are re-applied at app start when the setting is on.
- The only added permission is `POST_NOTIFICATIONS` (Android 13+), requested when the user turns reminders on. No network permission.
- WorkManager was not added, to keep the dependency set small.

## Gold shop (phase 1 leftover)
- Shown as an honest "coming soon" card on the Hunter tab. Gold is collected now; spending it needs item design, so it is not faked.

## Swipe to complete (phase 1 leftover)
- Not implemented. Steps are completed with a tap (48dp rows), and the 5-second undo banner exists. Swipe is a later polish item.

## Rank trial (phase 2)
- Reaching a new rank's level is not enough: the player must first pass a weekly trial (5 finished quests within 7 days). Stored in the key-value table (`rank_unlocked_index`, `rank_trial_start`). No schema change.
- Unlocks are permanent. The effective rank is `min(level rank, unlocked rank)`, and the quest planner uses it as a cap.
- The rank-up cutscene is not implemented. The Now tab shows the trial card instead.

## Tags (phase 2)
- A tag is a `#word` in the transaction note. Search already matches note text, so `#food` finds those rows. A separate tag table was not added to avoid a schema migration for a small feature.

## Biometric unlock (phase 3)
- `androidx.biometric` 1.1.0 with `BIOMETRIC_WEAK`. The PIN stays the fallback. The button only appears when the device reports biometrics as available.

## Numpad quick-add (phase 1)
- An on-screen numpad under the amount field: digits, `000`, and an erase key. Each key is 48dp or taller.

## Swipe to complete (phase 1)
- A horizontal swipe of 120px or more on a step completes it, like a tap. The 5-second undo banner still applies.

## Release build (phase 3)
- `assembleRelease` runs in CI with R8 (`isMinifyEnabled`, resource shrinking).
- Release key: generated in the sandbox (RSA 2048, PKCS#12, alias `shadow`, 25-year self-signed certificate). **The keystore and its password are committed to `release-keystore/`.** The repository is public, so anyone with the repo can sign an app with this key. This is an explicit owner decision. Do not reuse this key for a store listing or for any other app. Use a separate key for that.
- Gradle reads `release-keystore/keystore.properties`, or `SHADOW_*` environment variables. If neither exists, it falls back to the debug key.
- CI therefore signs release builds with this key, with no GitHub secrets needed.

## Accessibility (phase 3)
- Touch targets are 48dp or taller. Text uses the Material type scale and the app supports large fonts through the system. A full TalkBack audit has not been run, because there is no device in the sandbox.

## Gold shop boosts (phase 3)
- Three items: dungeon XP ×1.5 for the next run that pays XP (40 gold), quest XP ×1.5 (60 gold), and quest gold ×2 (50 gold). Quest boosts are applied when the quest's first step is ticked, and are then used up.
- Undoing that first step does not give the boost back. This is accepted for simplicity and is noted here.

## Rank-up moment (phase 3)
- A full-screen overlay with an animated avatar grows in when the rank increases. Tap to continue.

## Soldier stages (phase 3)
- Stage comes from the linked stat level (the overall level for Bellion), using 1–9, 10–24, 25–49, 50–79, 80+. Art falls back to the highest stage that exists, so all stages show the stage 1 portrait until more art is made.

## Accessibility (phase 3, code review only)
- Icons and images have content descriptions. Gate and soldier images are described. Tab items have text labels. Touch targets are at least 48dp. A TalkBack run on a device has not been done.

## Batch: dungeon timer service and soldier system

- **Foreground service permissions.** The spec asks for a foreground service so the dungeon timer survives the app closing. This needs `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_SPECIAL_USE` (targetSdk 34). They are not network permissions, so the "no network permission" rule still holds. The service type is `specialUse`. Notifications are silent and low importance.
- **Timer state.** The active run lives in SharedPreferences (`DungeonTimerStore`), so a killed process resumes the timer. Pause time does not count as work. While paused, the monster regenerates 1% per 30 s, up to 10%. This is cosmetic only and does not change XP.
- **Haptics and ambient sound.** A haptic tick every full minute (toggleable). Ambient sound is generated brown noise, off by default, and plays only while the timer runs.
- **Soldier progress is derived.** XP, active days and wisdom come from completed quests linked to a soldier (`quests.soldierKey`, filled from quest templates). Nothing is counted twice and nothing can drift. Only the custom name and the summon time are stored in `soldiers`.
- **Soldier XP.** 20 XP per completed linked quest. Level uses the spec curve `round(50·n^1.3)` (`GameMath.soldierXpForLevel`).
- **Active day.** A game day with at least one completed linked quest. Summoning needs 7 active days (5 s ritual, skippable). The stage now comes from soldier level, replacing the earlier stat-level rule, because the spec defines stages by soldier level.
- **Two axes.** Power is the active-day count, shown in the detail sheet. Stage (1–5) follows level. Wisdom is `2 × distinct linked quest titles + completed steps`, with tiers at 10, 40 and 100 (simple, medium, full). Wisdom art is not in the repo yet, so the tier is shown as text.
- **States.** Gap since the last active day: 0–1 awake, 2–4 tired, 5 or more sleeping. The sleeping message is gentle and suggests one tiny task.
- **Bellion.** Locked until rank S and 100 total active days. Counts all linked quests.
- **Evolution cutscene.** 3 s, skippable, shows the new stage's voice line. It plays when a summoned soldier's stage passes the last stage the user saw (`kv` key `soldier_seen_<key>`). It does not play at summon time. Known gap: no morph animation from the old silhouette yet.
- **Voice lines.** Rotated by active days within the current stage (the existing data has 3 lines per stage).
- **Known gaps.** Users cannot re-link a quest to another soldier yet, and dungeons do not yet link to a soldier or quest. Soldier art for stages 2–5, wisdom overlays and the egg image are placeholders.
| 27 | Wallet on transactions | Nullable `walletId` on `transactions`. Null means the first wallet, so v4 data needs no backfill (migration 4→5). | Spec wants wallets without losing old entries. |
| 28 | Transfers | Separate `transfers` table. They never count as spending and give no XP or Gold. | Spec: spending must never give XP or Gold, and moving money is not spending. |
| 29 | Cheques | One table for issued and received cheques, due date stored as epoch day from the Jalali date. "Due soon" means within 3 days. | Keeps one list with a single status rule (`ChequeRules`). |
| 30 | Budget rollover | Unspent carries to the next month, capped at one base limit; overspend does not carry | Spec asks for rollover; the cap stops a saved-up month making the next unlimited. |
| 31 | Receipts | Photo picked with `OpenDocument` (no storage permission), URI stored on the transaction. Thumbnail decoded at 1/4 size off the main thread. | Keeps the app offline with no media permission. |
| 32 | Installments | Debt holds `installmentCount` and `installmentsPaid`. Each payment adds `installmentPart` (rounding goes to the last one). Manual payments still work. | Simple schedule without a separate table. |
| 33 | Gate length | Stored per rank in kv (`gate_minutes_<RANK>`), 5–120 min in steps of 5 | Lets the user pick a session length without changing the rank scale. |
| 34 | Dungeon links | Soldier and quest are stored on the run and on the session. They do not change XP. | Keeps XP rules unchanged; the link is for focus and history. |
| 35 | Daily report | Full report reads the day's data only. Spending is shown but never adds XP or gold. | Spec: money never gives XP or gold. |
| 36 | Onboarding questions | Four question screens (focus, hardest part, daily time, reminders). Answers stored in kv (`onboarding_struggle`, `onboarding_minutes`). Daily time does not yet change gameplay. | Spec asks for question screens; personalisation is a later step. |
| 37 | Reminder content | Morning and night notifications read today's numbers from the repository; plain text when there is no data. | Spec: morning briefing and night report linked to notifications. |
| 38 | Wisdom overlay | Overlay uses the soldier's wisdom tier (1–3); if art for that tier is missing, the highest lower tier with art is shown; no overlay when none exists. | Art is produced in batches; the app should work with partial art. |
| 39 | Onboarding length | The user asked for question screens, so onboarding is 8 screens (4 intro, 4 questions). This overrides the spec's "max 5 screens". | User request takes priority. |
| 40 | Image format | Art is PNG at 512 px (soldiers, wisdom, monsters, icons) and 768 px (backgrounds). The spec asks for WebP at 1024 px. | PNG is generated and checked here; WebP conversion is planned (see docs/SPEC_AUDIT.md). |
| 41 | Spec audit | `docs/SPEC_AUDIT.md` lists the spec items still partial or missing; it is the work list before merge. | Keeps the remaining work visible. |
| 42 | Quiet hours | Default window 22:00–07:00, set in Settings with two stepper rows (1-hour steps, wraps at midnight). The reminder receiver skips notifications inside the window. | Spec section 11. Stored in shared preferences. |
| 43 | Categories | Own `categories` table (v8) with name, kind (expense or income), archived flag, colour and order. The default set is seeded in the migration and on a fresh install. Rename updates transactions, budgets and recurring rules in one transaction. Archive hides a category from new entries only. | Spec section 8. Old transactions keep their text. |
| 44 | Search filters | Filters live in `MoneyFilter` (core, unit-tested). Tags are `#words` in the note, so no new table or migration. Date range uses presets (7, 30, 90 days, or all time) rather than a picker. Amounts are typed in toman. When any advanced filter is on, the list searches all time; the month totals stay on the month. | Spec section 8. A custom date picker is still open. |
| 45 | Onboarding name, base, wake | Name, avatar base (0–2) and wake hour (5–10) are stored in `AppPrefs`, not Room, so no migration is needed. The wake hour sets the morning reminder (default 08:00). The avatar base is a colour tint on the rank image until the layered body art exists. Name is required, max 20 characters. | Spec sections 7 and 10. Settings does not yet let the user change these later. |
| 46 | Reminder taps | A tap opens its screen. Morning opens Now (today's quests). Night opens the daily report. The report kind follows the clock (`DailyReport.isNightHour`), not the alarm kind. The target goes in the notification intent and is kept in `PendingRoute` until the nav graph is ready, so it works after the PIN lock or when the app is already open. | Spec sections 10 and 11. |
| 47 | Savings goal reward, reports, previews | Completing a savings goal (first deposit that reaches the target) grants the heavy frame `frame_sovereign`, once. Reports add this month vs last month, daily average, 12-month total and five largest expenses. Card previews are added for the components that take plain parameters. Screen previews are not added because screens need ViewModels. | Spec sections 8, 9 and 15. |
| 48 | Debt due dates and reminders | Debts get a first due date (Room v9, nullable column). Installments fall on the same day each Jalali month. Morning and night reminders add a count of cheques and debts due within 3 days or overdue. Installments are still stored as a count, not as a full schedule list. | Spec section 9. |
| 49 | Recurrence periods, money unit | Recurring rules store a `period` name (DAILY, WEEKLY, MONTHLY, YEARLY) in Room v10. The table is rebuilt; old `monthly` flags map to MONTHLY or WEEKLY. Backups accept the old true/false form. Daily catch-up is capped at 60 occurrences like the others. Money displays in toman by default; a settings switch shows rial (`PersianText.rialMode`). | Spec section 8. |
| 50 | Quick add, goal deadline and wallet, transaction details | Money entry moves to a floating button that opens a bottom sheet with the existing entry card. Transactions get optional subcategory and location (Room v11, nullable columns, backup fields 11 and 12). Savings goals get an optional deadline (Jalali date) and an optional linked wallet (savings_goals, same migration). Shown on the goal row; the goal estimate is unchanged. | Spec sections 8 and 9. |
| 51 | Backdating, category order, yearly report | The money form takes an optional day (Jalali picker, never after today). Backdated entries are recorded without XP, so XP still comes only from logging today. Category chips are ordered by how often each is used (all time), ties keep stored order. Reports show the last 12 months as bars and a trend: last 3 months against the 3 before, using the `changePercent` rule. | Spec sections 8, 11. |
| 52 | Last wallet, avatar layers | The money form starts on the last wallet used (kept in prefs), or the first wallet if that one was deleted. Equipped outfit, weapon, aura and frame are drawn over the avatar as vector shapes (`AvatarLayers`, colours in `AvatarLayerRules`, tested). This stands in for per-item PNG layers. | Spec sections 7, 8. |
| 53 | Outfit look images | Each outfit has a full 512 px look image made from the rank-A avatar as reference (so pose and style match). An equipped outfit with a look image replaces the rank picture and is not tinted. Weapons, auras and frames stay vector. | Spec section 7. |
| 54 | Weapon sprites, saved form | Weapons have transparent PNG sprites (green screen keyed), drawn at the lower right; vector blade is the fallback. The money entry form keeps its fields in `SavedStateHandle`, so it survives process death (lifecycle-viewmodel-savedstate added). Goal and plan forms are not yet saved. | Spec sections 7, 15. |
| 55 | Aura and frame sprites | Four aura sprites (behind the avatar) and three frame sprites (over the avatar) are transparent PNGs keyed from green. Vector border and square are fallbacks. | Spec section 7. |
| 56 | Mood pose pictures | Mood poses have PNG pictures (standing, determined, tired, low energy) chosen by the day's score. The outfit look is shown first, then the pose, then the rank picture. | Spec section 7. |
| 57 | Low-energy pose, plan forms survive process death | The low-energy pose picture is added, so all four moods have a picture. Budget, recurring and savings-goal form fields use rememberSaveable (the goal deadline is stored as an epoch day). | Spec sections 7, 15. |
| 58 | Base pictures, evolution check | The onboarding base has its own picture (blue or amber, made from the rank-A reference) instead of only a tint. Priority: outfit look, base, mood pose, rank picture. The evolution cutscene was checked in code: 3 s, old form fades out while the new one fades in and scales up, reduce-motion snaps it. All 60 soldier-stage voice-line sets have at least 3 lines. | Spec sections 6, 7. |

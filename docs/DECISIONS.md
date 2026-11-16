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

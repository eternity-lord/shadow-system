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

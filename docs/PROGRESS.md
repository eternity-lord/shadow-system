# Progress Tracker

Branch: `arena/05d6808d-shadow-system` (base `cb65486`)

| Phase | Status | Commit(s) | Notes |
|---|---|---|---|
| 0 Setup | Done | `3ebccc9` | PDF, Gradle, fonts, docs, manifest |
| 1 MVP | Partial, builds and tests green | `f4fd82e` + fixes | See PHASE_REPORT.md for what is missing |
| 2 Depth | Not started | — | Budgets, recurring, goals, debts, reports, notifications |
| 3 Completion | Not started | — | Backup, app lock, artwork, release build |

## Next steps (in order)
1. Notifications and onboarding (Phase 1 leftovers).
2. Quest "I'm stuck" + swipe-complete undo.
3. Soldier system and avatar visuals.
4. Phase 2 money features and reports.
5. Phase 3 backup, app lock, artwork, accessibility.

## Log
- Phase 0: scaffold, fonts, Gradle, PDF committed, pushed.
- Phase 1: core logic, data layer, five screens, tests, CI. Two CI fix cycles (missing `getValue` import). Final run green.


## Latest update
- Rank trial, numpad, biometric unlock, swipe complete, gate artwork, release build in CI: done (`dcb6107`, CI green).
- Still open: gold shop, rank-up cutscene, soldier/avatar/monster artwork, accessibility audit, real release signing key.
- PR #1 is NOT merged. It will be merged only when the open items are closed.

## Update 2
- Gold shop (dungeon boost), rank-up dialog, 8 soldier portraits, 6 gate images: done. CI green at `00f6e45`.
- Still open: 4 soldier portraits, 6 rank avatars (to generate next turn), accessibility audit, real release key, PR merge.

# Agent instructions — LibreTube-AI

Read this before working in this repository. It is loaded for every agent, so it
is kept short. **Read-only helper subagents:** use the codebase map and the
gotchas; the workflow sections are for the agent that changes code.

## What this repo is

An unofficial fork of [LibreTube](https://github.com/libre-tube/LibreTube)
(Android YouTube client, Kotlin). Changes are made with AI assistance and
verified by the owner on a real device. See CONTRIBUTING.md for the policy.

- `master` mirrors upstream. Never commit to it.
- `patched` is the default branch: upstream + this fork's changes.
- Upstream does NOT accept AI-assisted issues or PRs. Never open, comment on or
  push to anything in `libre-tube/*`, and never *mention* it either: in issues,
  PRs and commit messages write "upstream issue 8882", not
  `libre-tube/LibreTube#8882` or a link to it, because GitHub would add a
  "mentioned this" event to the upstream thread.

## How work is done

1. **Issue first.** Every change has an issue in this fork: symptom, steps to
   reproduce, device/Android version, logs, and (once known) the root cause.
2. **Research before editing.** Find the code paths and confirm the cause with
   file:line evidence. Write down what you verified and what you inferred.
3. **One branch per change**, from an up-to-date `patched`:
   `fix/<topic>`, `feat/<topic>`, `docs/<topic>`, `refactor/<topic>`.
4. **Minimal diff.** Change only what the issue needs. No reformatting, no
   drive-by refactors, no renames. Explain non-obvious choices in a short code
   comment (the *why*, not the *what*).
5. **Build and test** (below). New lint warnings in changed lines must be fixed;
   leave pre-existing ones alone.
6. **Commit** with a conventional-commit title (`fix(player): ...`,
   `feat(playlist): ...`), a body saying what changed and why, and `Fixes #N`.
7. **Pull request into `patched`** with: Fixes #N; what changed (files); why it
   was done this way; behaviour notes and known edge cases; tests run; device
   test steps for the owner; and a line disclosing AI use.
8. **The owner device-tests and merges.** Agents never merge their own PRs,
   never force-push, never push to `master`/`patched`, never delete branches.

## Build and verify

- JDK 17+ (CI uses 17; 21 works), Android SDK with platform 36.
  `local.properties` must contain `sdk.dir=...` (it is gitignored).
- Commands:
  - `./gradlew :app:assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`
    (installs as `com.github.libretube.debug`, beside the release app, with its
    own data)
  - `./gradlew :app:testDebugUnitTest` (few tests, in `app/src/test`)
  - `./gradlew :app:lintDebug` (report: `app/build/reports/lint-results-debug.xml`)
- Every build regenerates `app/src/main/res/values/languages.xml` (task in the
  root `build.gradle.kts`). Revert it before committing:
  `git checkout -- app/src/main/res/values/languages.xml`.
- There is no emulator in the agent environment. Behaviour changes are verified
  by the owner on a device; write exact steps (including settings such as
  battery usage "Optimized" for background behaviour) in the PR.
- Release builds are minified with R8: stack traces from release builds can
  show merged or misattributed class names (e.g. androidx frames).

## Codebase map (`app/src/main/java/com/github/libretube/`)

| Package | What lives there |
|---|---|
| `api/` | Media backend: `MediaServiceRepository` (interface; `instance` is `NewPipeMediaServiceRepository`), Piped auth API, `PlaylistsHelper`, `SubscriptionHelper`, poToken, LibreTube sync (`ltsync/`) |
| `repo/` | User data: `UserDataRepositoryHelper` picks local / Piped account / LibreTube sync server by preference. Use the `PlaylistsHelper`/`SubscriptionHelper` wrappers, not the repositories directly. Also feed and download providers |
| `db/` | Room: `AppDatabase` (version in the `@Database` annotation), DAOs in `dao/`, entities in `obj/`, migrations in `DatabaseHolder`; exported schemas in `app/schemas/` |
| `services/` | Playback: `AbstractPlayerService` (Media3 `MediaSessionService`), `OnlinePlayerService`, `OfflinePlayerService`; downloads |
| `player/` | SABR streaming sources and parsers |
| `util/` | `PlayingQueue` (queue of video IDs), `NowPlayingNotification`, track selection, link handling |
| `helpers/` | `PreferenceHelper` (read/write prefs), `PlayerHelper`, `ImageHelper`, `BackgroundHelper` (starts/stops player services), etc. |
| `constants/` | `PreferenceKeys` (all preference keys), intent/ID constants |
| `ui/` | ViewBinding UI: `activities/`, `fragments/`, `sheets/` (bottom sheets, e.g. playlist options), `dialogs/`, `adapters/`, `models/` (ViewModels), `preferences/` (settings screens) |
| `workers/` | WorkManager jobs (new-video notifications) |

## Codebase facts and gotchas

- **Playback transitions:** the queue holds video IDs; the next item's streams
  are fetched over the network when the current one ends
  (`navigateVideo` → `startPlayback`). Never empty the player's playlist
  before the next item is ready: an empty timeline makes Media3 drop the
  notification and the foreground service, and re-promoting it from the
  background crashes on Android 12+ (`ForegroundServiceStartNotAllowedException`,
  issue #1). `isTransitioning` marks a transition in progress.
- **Media3** is pinned at the upstream version (see `gradle/libs.versions.toml`).
  Dependency upgrades are separate PRs with their own device test: upstream
  rolled back a dependency batch after a crash (upstream PR 8416).
- **Room schema changes:** bump the version in `AppDatabase`, add an
  `AutoMigration` or a manual migration, and commit the exported schema JSON.
  Prefer avoiding a schema change when a preference will do.
- **Preferences:** add keys to `PreferenceKeys`, read/write via
  `PreferenceHelper`. Settings screens are XML in `res/xml/` plus classes in
  `ui/preferences/`.
- **Strings:** add new strings ONLY to `res/values/strings.xml` (English).
  Never edit `res/values-*/` translations; they come from Weblate via upstream.
- **Coroutines:** network and database work on `Dispatchers.IO`; in fragments
  use `viewLifecycleOwner.lifecycleScope`; no `runBlocking` or `GlobalScope` in
  new code. Avoid `!!` in new code.
- **Playlists** come in two kinds: local (Room, numeric IDs) and remote (Piped
  account, string IDs). Features that touch playlists must handle both.
- `.agent-tmp/` is for scratch files; `PROGRESS.md`, `FINDINGS.md` and
  `PLAN.md` are local agent working files. All are gitignored; never commit them.

# Contribution

This is **LibreTube-AI**, an unofficial fork of [LibreTube](https://github.com/libre-tube/LibreTube).
It carries fixes and features that are developed with AI assistance and verified by a human
before they are merged. Please respect the upstream [Code of Conduct](https://github.com/libre-tube/LibreTube/blob/master/CODE_OF_CONDUCT.md)
in order to keep all interactions and discussions healthy.

> [!IMPORTANT]
> **AI-assisted contributions are welcome in this fork**, on these conditions:
> - Say that AI was used, and how (e.g. "diagnosed and implemented with an AI coding agent").
> - A human has reproduced the problem and verified the change, on a real device for anything
>   that affects behaviour, and says so in the pull request.
> - The change goes through an issue and a pull request (see below), never a direct push.

> [!WARNING]
> **This policy applies to this fork only.** Upstream LibreTube does not permit AI-assisted
> contributions ("Contributions/Issues using AI will be closed and ignored"). Do not open
> issues or pull requests on [libre-tube/LibreTube](https://github.com/libre-tube/LibreTube)
> with work produced here.

## Branches

- `master` mirrors upstream LibreTube and is only ever synced from it. Never commit to it.
- `patched` is the default branch: upstream plus this fork's changes. Builds come from here.
- Every change is a short-lived branch (`fix/...`, `feat/...`, `docs/...`) merged into `patched`
  by pull request.

## How a change gets in

1. **Issue first.** Open an issue in this fork describing the bug (steps to reproduce, expected
   vs actual, device and Android version, logs) or the feature (what and why).
   Bugs that also exist in official LibreTube can be reported upstream as well, written by a human.
2. **Branch and fix.** Branch from `patched`. Keep the diff minimal and focused on the issue;
   explain non-obvious choices in a short code comment.
3. **Build and test.** The debug build, unit tests and lint must pass
   (`./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`).
4. **Pull request into `patched`.** Link the issue (`Fixes #N`), say what changed and why it was
   done that way, list the tests (including the device test), and disclose AI use.
5. **Merge.** The fork owner reviews and merges. Automated agents never merge their own PRs.

## Commit messages

Pull request titles and commit messages follow the [conventional commit
types](https://github.com/commitizen/conventional-commit-types/blob/master/index.json) (e.g. `feat: support
for xy`). The most common types are "feat", "fix", "refactor", "ci", "docs" and "chore".

## Translations

Translations are managed upstream using [WebLate](https://hosted.weblate.org/projects/libretube/#languages)
and arrive here when `master` is synced. New strings added by this fork are English-only until
they are translated.

## Building

You can open and build the project like any other normal Android project by using Android Studio.

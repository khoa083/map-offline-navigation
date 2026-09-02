# scripts/

Helpers that exist for one reason: **Claude can read and write every file in this repo, but it
cannot run the build.**

Claude reaches this project through a mounted folder from an isolated Linux VM. That VM has
Java 11 (the project needs 21), no Android SDK, and no network route to `services.gradle.org`,
`dl.google.com` or `repo1.maven.org`. The Windows toolchain — Android Studio's JBR, the SDK under
`%LOCALAPPDATA%\Android\Sdk`, the Gradle caches in `%USERPROFILE%\.gradle` — is not mounted and
would not run on Linux anyway, since those are Windows binaries.

So the loop is: **you run one command, the log lands in `build-log/`, Claude reads it from there.**
No copying, no pasting.

## Usage

```powershell
# fastest useful check - Kotlin type-check only
.\scripts\check-build.ps1

# or double-click scripts\check-build.bat
```

Then tell Claude: *"read scripts/build-log/latest.log"*.

## Scopes

| Scope | Gradle tasks | Catches |
|---|---|---|
| `quick` *(default)* | `:app:compileDebugKotlin` | Every Kotlin type error, unresolved reference, wrong API signature. The check that matters most after a refactor. |
| `full` | `:app:assembleDebug` | The above, plus resources, the R class, manifest merging and dexing. Catches broken vector XML and manifest problems the Kotlin task never sees. |
| `lint` | `ktlintCheck`, `:app:lintDebug` | The two gates CI's `static-analysis` job runs. Both are baseline-gated — see CLAUDE.md. |
| `tests` | `:app:test` | Unit tests. |
| `coverage` | `:app:jacocoTestReportAll` | Unit tests **plus** the aggregated JaCoCo report, then prints the instruction/branch/line percentages straight from the XML. The report task already depends on `testDebugUnitTest`, so it does not run the tests twice. |
| `all` | all of the above, in order | Stops at the first failure. |

```powershell
.\scripts\check-build.ps1 -Scope full
.\scripts\check-build.ps1 -Scope all -Clean
```

`-Clean` prepends a `clean`. Slower, but rules out stale output when a failure looks impossible.

## Why `quick` is the default

`:app:compileDebugKotlin` stops right after Kotlin compilation. On a warm build it finishes in
seconds while still type-checking every file — which is exactly the class of error a static review
cannot reach. `assembleDebug` does the same work and then several minutes more; run it when the
change touched resources, the manifest, or dependencies.

## Output

- `build-log/build_<timestamp>.log` — full output, one file per run, kept as history.
- `build-log/latest.log` — a copy of the most recent run. This is the file to point Claude at.

`build-log/` is git-ignored; nothing here is ever committed.

## `signing-fingerprint.ps1`

Prints the SHA-1 / SHA-256 of the app's signing certificate. Two modes, and the difference
matters:

```powershell
# what WILL sign a build - read from the keystore
.\scripts\signing-fingerprint.ps1                 # debug keystore
.\scripts\signing-fingerprint.ps1 -Target release # keystore named by release.properties

# what DID sign a build - read from the APK's own signature block (ground truth)
.\scripts\signing-fingerprint.ps1 -Apk app\build\outputs\apk\release\app-release.apk
```

Why it is worth having as a script rather than a remembered `keytool` incantation:

- Android accepts an update only if it is signed by the **same** certificate. If this fingerprint
  ever changes, every existing install becomes un-upgradable and users have to uninstall first.
  That is not a mistake you want to discover after a release.
- F-Droid metadata (`AllowedAPKSigningKeys`) and IzzyOnDroid's reproducible-build check both pin
  this SHA-256. Being able to read it back out of a finished APK in one command is how you verify
  a release *before* publishing it, not after someone reports a failed update.
- Neither `keytool` nor `apksigner` is on `PATH` in a normal Android Studio install - the JDK is
  the IDE's bundled JBR and `apksigner` lives in a version-numbered `build-tools` folder. The
  script finds both, picking the highest build-tools version.

It never writes to the keystore and never prints a password. `-Target release` needs
`release.properties`, which is git-ignored, so a fresh clone will report it missing rather than
fail obscurely.

`signing-fingerprint.bat` is the double-clickable wrapper (`signing-fingerprint.bat release`).

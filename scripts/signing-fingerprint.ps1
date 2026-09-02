<#
.SYNOPSIS
    Prints the SHA-1 / SHA-256 certificate fingerprints for this app's signing identity.

.DESCRIPTION
    Two different questions, one script:

      1. "What certificate WILL sign my build?"  -> read it from the keystore  (-Target)
      2. "What certificate DID sign this APK?"   -> read it from the APK       (-Apk)

    Question 2 is the one that matters for this project. IzzyOnDroid's reproducible-build check
    and F-Droid's `AllowedAPKSigningKeys` both pin the SHA-256 of the *APK signing certificate*.
    If that value ever changes, every existing install stops being upgradable - Android refuses
    an update signed by a different key. Both the colon form and the flat lowercase-hex form are
    printed; check which one the store you are publishing to expects.

    The keystore is never modified and no password is ever printed or logged.

.PARAMETER Target
    debug   (default) %USERPROFILE%\.android\debug.keystore, alias androiddebugkey, pass "android".
    release           The keystore named by release.properties in the repo root. That file is
                      git-ignored; if it is missing this script says so and stops.

.PARAMETER Apk
    Path to an APK. Reads the certificate actually embedded in the APK signature block via
    apksigner, which is the ground truth - a keystore only tells you what was *intended*.
    Overrides -Target.

.EXAMPLE
    .\scripts\signing-fingerprint.ps1
    .\scripts\signing-fingerprint.ps1 -Target release
    .\scripts\signing-fingerprint.ps1 -Apk app\build\outputs\apk\release\app-release.apk
#>
[CmdletBinding()]
param(
    [ValidateSet('debug', 'release')]
    [string]$Target = 'debug',

    [string]$Apk
)

$ErrorActionPreference = 'Stop'

$RepoRoot = Split-Path -Parent $PSScriptRoot

# ---------------------------------------------------------------------------------------------
# Tool discovery
#
# Deliberately does NOT assume keytool or apksigner are on PATH. On a normal Android Studio
# install neither is: the JDK lives inside the IDE as a bundled JBR, and apksigner lives in a
# version-numbered build-tools folder. Failing with "keytool not found" when the machine plainly
# has a working Android toolchain is a bad error, so look in the usual places first.
# ---------------------------------------------------------------------------------------------
function Find-Keytool {
    if ($env:JAVA_HOME) {
        $p = Join-Path $env:JAVA_HOME 'bin\keytool.exe'
        if (Test-Path $p) { return $p }
    }
    $onPath = Get-Command keytool -ErrorAction SilentlyContinue
    if ($onPath) { return $onPath.Source }

    $candidates = @(
        "$env:ProgramFiles\Android\Android Studio\jbr\bin\keytool.exe",
        "$env:LOCALAPPDATA\Programs\Android Studio\jbr\bin\keytool.exe",
        "${env:ProgramFiles(x86)}\Android\Android Studio\jbr\bin\keytool.exe"
    )
    foreach ($c in $candidates) { if (Test-Path $c) { return $c } }
    throw "keytool not found. Set JAVA_HOME, or add a JDK bin folder to PATH."
}

function Find-Apksigner {
    $sdkRoots = @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT, "$env:LOCALAPPDATA\Android\Sdk") |
        Where-Object { $_ -and (Test-Path $_) }

    foreach ($root in $sdkRoots) {
        $bt = Join-Path $root 'build-tools'
        if (-not (Test-Path $bt)) { continue }
        # Highest build-tools version wins. Sorted as [version] rather than as text, so 35.0.0
        # does not lose to 9.0.0 the way a string sort would have it.
        $best = Get-ChildItem $bt -Directory |
            Sort-Object { try { [version]$_.Name } catch { [version]'0.0.0' } } -Descending |
            ForEach-Object { Join-Path $_.FullName 'apksigner.bat' } |
            Where-Object { Test-Path $_ } |
            Select-Object -First 1
        if ($best) { return $best }
    }
    throw "apksigner not found. Set ANDROID_HOME, or install Android SDK Build-Tools."
}

# ---------------------------------------------------------------------------------------------
# Formatting
# ---------------------------------------------------------------------------------------------
function Show-Fingerprint {
    param([string]$Label, [string]$Value)
    if (-not $Value) { return }
    $colons = ($Value -replace '[^0-9A-Fa-f]', '').ToUpper() -replace '(..)(?!$)', '$1:'
    $flat   = ($Value -replace '[^0-9A-Fa-f]', '').ToLower()

    Write-Host ""
    Write-Host "$Label" -ForegroundColor Yellow
    Write-Host "  colon form : $colons"
    Write-Host "  flat form  : $flat" -ForegroundColor Cyan
}

# ---------------------------------------------------------------------------------------------
# Mode 1 - read the certificate out of a built APK (ground truth)
# ---------------------------------------------------------------------------------------------
if ($Apk) {
    if (-not (Test-Path $Apk)) { throw "APK not found: $Apk" }
    $apkPath = (Resolve-Path $Apk).Path
    $apksigner = Find-Apksigner

    Write-Host "APK       : $apkPath"
    Write-Host "apksigner : $apksigner"

    $prevEAP = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        # apksigner writes its scheme-version notes to stderr; fold them in so nothing is lost.
        $out = & $apksigner verify --print-certs --verbose $apkPath 2>&1 | ForEach-Object {
            if ($_ -is [System.Management.Automation.ErrorRecord]) { $_.Exception.Message } else { [string]$_ }
        }
    }
    finally { $ErrorActionPreference = $prevEAP }

    if ($LASTEXITCODE -ne 0) {
        $out | ForEach-Object { Write-Host $_ }
        throw "apksigner verify failed (exit $LASTEXITCODE)."
    }

    $sha1   = ($out | Select-String 'SHA-1 digest:'   | Select-Object -First 1) -replace '.*SHA-1 digest:\s*', ''
    $sha256 = ($out | Select-String 'SHA-256 digest:' | Select-Object -First 1) -replace '.*SHA-256 digest:\s*', ''
    $subject = ($out | Select-String 'Signer #1 certificate DN:' | Select-Object -First 1)

    if ($subject) { Write-Host "Subject   : $($subject -replace '.*DN:\s*', '')" }
    $out | Select-String 'Verified using v\d scheme' | ForEach-Object { Write-Host "  $_" -ForegroundColor DarkGray }

    Show-Fingerprint 'SHA-1  (certificate)'   $sha1
    Show-Fingerprint 'SHA-256 (certificate)'  $sha256

    Write-Host ""
    Write-Host "Android will only accept an update signed by this same certificate."                -ForegroundColor Gray
    Write-Host "Store metadata that pins a signing key (F-Droid AllowedAPKSigningKeys, and the"       -ForegroundColor Gray
    Write-Host "IzzyOnDroid RB check) uses this SHA-256 - confirm the exact format against their"     -ForegroundColor Gray
    Write-Host "current docs before pasting it anywhere."                                            -ForegroundColor Gray
    Write-Host ""
    exit 0
}

# ---------------------------------------------------------------------------------------------
# Mode 2 - read the certificate out of a keystore
# ---------------------------------------------------------------------------------------------
$keytool = Find-Keytool

if ($Target -eq 'debug') {
    $storeFile = Join-Path $env:USERPROFILE '.android\debug.keystore'
    $alias     = 'androiddebugkey'
    # Android's debug keystore password is a published constant, not a secret.
    $storePass = 'android'

    if (-not (Test-Path $storeFile)) {
        Write-Host "Debug keystore not found at $storeFile" -ForegroundColor Red
        Write-Host "Android Studio creates it on the first debug build. Build once, then re-run."
        exit 1
    }
} else {
    $propsFile = Join-Path $RepoRoot 'release.properties'
    if (-not (Test-Path $propsFile)) {
        Write-Host "release.properties not found at $propsFile" -ForegroundColor Red
        Write-Host "It is git-ignored on purpose, so a fresh clone will not have it."
        exit 1
    }

    # Same four keys app/build.gradle.kts reads. Parsed here rather than hardcoded so the two
    # cannot drift.
    $props = @{}
    foreach ($line in Get-Content $propsFile) {
        if ($line -match '^\s*#') { continue }
        if ($line -match '^\s*([^=]+?)\s*=\s*(.*)$') { $props[$Matches[1]] = $Matches[2] }
    }

    $storeFile = $props['RELEASE_STORE_FILE']
    $alias     = $props['RELEASE_KEY_ALIAS']
    $storePass = $props['RELEASE_STORE_PASSWORD']

    if (-not $storeFile -or -not $alias) {
        throw "release.properties is missing RELEASE_STORE_FILE or RELEASE_KEY_ALIAS."
    }

    # build.gradle.kts calls file(...) from inside the :app module, so a relative store path is
    # resolved against app/ - not the repo root. Try app/ first, then the root, then as given.
    if (-not [System.IO.Path]::IsPathRooted($storeFile)) {
        $tries = @(
            (Join-Path (Join-Path $RepoRoot 'app') $storeFile),
            (Join-Path $RepoRoot $storeFile)
        )
        $found = $tries | Where-Object { Test-Path $_ } | Select-Object -First 1
        if ($found) { $storeFile = $found }
    }

    if (-not (Test-Path $storeFile)) { throw "Keystore not found: $storeFile" }
}

Write-Host "keytool   : $keytool"
Write-Host "Keystore  : $storeFile"
Write-Host "Alias     : $alias"

$prevEAP = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
try {
    $out = & $keytool -list -v -keystore $storeFile -alias $alias -storepass $storePass 2>&1 |
        ForEach-Object {
            if ($_ -is [System.Management.Automation.ErrorRecord]) { $_.Exception.Message } else { [string]$_ }
        }
}
finally { $ErrorActionPreference = $prevEAP }

if ($LASTEXITCODE -ne 0) {
    Write-Host "keytool failed - wrong password, wrong alias, or not a keystore." -ForegroundColor Red
    $out | Select-Object -First 5 | ForEach-Object { Write-Host "  $_" }
    exit 1
}

# keytool localises its output, so match on the fingerprint labels (SHA1:, SHA256:) rather than
# on the surrounding prose, which is not stable across locales.
$sha1   = ($out | Select-String '(?m)^\s*SHA1:'   | Select-Object -First 1) -replace '.*SHA1:\s*',   ''
$sha256 = ($out | Select-String '(?m)^\s*SHA256:' | Select-Object -First 1) -replace '.*SHA256:\s*', ''

Show-Fingerprint 'SHA-1  (for Google Maps / OAuth / Firebase console)' $sha1
Show-Fingerprint 'SHA-256 (app signing identity)'                      $sha256

Write-Host ""
Write-Host "Note: this is the certificate that WILL sign a build with this keystore." -ForegroundColor Gray
Write-Host "To read what actually signed a built APK, use -Apk <path>."               -ForegroundColor Gray
Write-Host ""

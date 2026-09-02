<#

.PARAMETER Scope
    quick   (default) :app:compileDebugKotlin - Kotlin type-check only. Seconds on a warm build.
    full              :app:assembleDebug      - adds resources, R class, manifest merge, dexing.
    lint              ktlintCheck + :app:lintDebug - the two gates CI's static-analysis job runs.
    tests             :app:test               - unit tests.
    coverage          :app:jacocoTestReportAll - unit tests + aggregated JaCoCo report.
    all               everything above in that order, stopping at the first failure.

.PARAMETER Clean
    Prepends a clean. Slower, but rules out stale build output when a failure looks impossible.

.EXAMPLE
    .\scripts\check-build.ps1
    .\scripts\check-build.ps1 -Scope full
    .\scripts\check-build.ps1 -Scope all -Clean
#>
[CmdletBinding()]
param(
    [ValidateSet('quick', 'full', 'lint', 'tests', 'coverage', 'all')]
    [string]$Scope = 'quick',

    [switch]$Clean
)

$ErrorActionPreference = 'Stop'

# Repo root is this script's parent, so the script works from any working directory.
$RepoRoot = Split-Path -Parent $PSScriptRoot
$LogDir   = Join-Path $PSScriptRoot 'build-log'
$Stamp    = Get-Date -Format 'yyyy-MM-dd_HH-mm-ss'
$LogFile  = Join-Path $LogDir "build_$Stamp.log"
$Latest   = Join-Path $LogDir 'latest.log'

$Gradlew = Join-Path $RepoRoot 'gradlew.bat'
if (-not (Test-Path $Gradlew)) {
    Write-Host "gradlew.bat not found at $Gradlew - is this the repo root?" -ForegroundColor Red
    exit 1
}

New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

# Collected in memory and written once at the end.
#
# Deliberately NOT Tee-Object: on Windows PowerShell 5.1 its -FilePath writes UTF-16LE, and it
# has no -Encoding parameter to change that (added in PowerShell 6). A UTF-16 log is awkward for
# every tool that later reads it. Buffering and writing once with an explicit encoding also beats
# per-line Add-Content, which reopens the file for every line of a build log.
$Buffer = New-Object System.Collections.Generic.List[string]

function Write-Log {
    param([string]$Line, [string]$Color)
    $Buffer.Add($Line) | Out-Null
    if ($Color) { Write-Host $Line -ForegroundColor $Color } else { Write-Host $Line }
}

function Save-Log {
    # -Encoding utf8 is valid on both Windows PowerShell 5.1 and PowerShell 7+.
    Set-Content -Path $LogFile -Value $Buffer -Encoding utf8
    Copy-Item -Path $LogFile -Destination $Latest -Force
}

# ---------------------------------------------------------------------------------------------
# Task selection. Each entry is a string[] of Gradle task names run in one invocation.
#
# Built as List[string[]] rather than with a switch that emits nested arrays, and that is not
# stylistic. PowerShell UNROLLS collections written to the output stream, so
# `$x = switch (...) { 'quick' { ,@(':app:compileDebugKotlin') } }` collapses the array-of-array
# into a flat array of strings. `$tasks` then holds a STRING, and `& $exe @tasks` splats a string
# into its individual CHARACTERS - Gradle receives ':' and reports
# "Cannot locate tasks that match ':'". That is exactly how v3 of this script failed.
# A List[string[]] cannot unroll to strings: every element is a string[] by the type contract.
# ---------------------------------------------------------------------------------------------
$TaskSets = New-Object 'System.Collections.Generic.List[string[]]'

if ($Clean) { $TaskSets.Add([string[]]@('clean')) }

switch ($Scope) {
    'quick' { $TaskSets.Add([string[]]@(':app:compileDebugKotlin')) }
    'full'  { $TaskSets.Add([string[]]@(':app:assembleDebug')) }
    'lint'  {
        $TaskSets.Add([string[]]@('ktlintCheck'))
        $TaskSets.Add([string[]]@(':app:lintDebug'))
    }
    'tests' { $TaskSets.Add([string[]]@(':app:test')) }
    # jacocoTestReportAll already dependsOn :app:testDebugUnitTest (app/jacoco.gradle.kts),
    # so this runs the tests too - do NOT prepend :app:test or they run twice.
    'coverage' { $TaskSets.Add([string[]]@(':app:jacocoTestReportAll')) }
    'all'   {
        $TaskSets.Add([string[]]@(':app:compileDebugKotlin'))
        $TaskSets.Add([string[]]@('ktlintCheck'))
        $TaskSets.Add([string[]]@(':app:lintDebug'))
        $TaskSets.Add([string[]]@(':app:test'))
        $TaskSets.Add([string[]]@(':app:assembleDebug'))
    }
}

# ---------------------------------------------------------------------------------------------
# Run
# ---------------------------------------------------------------------------------------------
$failed   = $null
$exitCode = 0

Push-Location $RepoRoot
try {
    Write-Log "=== check-build.ps1 ==="
    Write-Log "when   : $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    Write-Log "scope  : $Scope"
    Write-Log "clean  : $($Clean.IsPresent)"
    Write-Log "repo   : $RepoRoot"
    Write-Log ("java   : " + $(if ($env:JAVA_HOME) { $env:JAVA_HOME } else { '(JAVA_HOME unset - Gradle resolves its own toolchain)' }))
    Write-Log ""

    foreach ($tasks in $TaskSets) {
        $label = $tasks -join ' '
        Write-Log ""
        Write-Log "### gradlew $label" 'Cyan'
        Write-Log ""

        # 2>&1 folds stderr into the pipeline so warnings and errors keep their order.
        #
        # $ErrorActionPreference MUST drop to 'Continue' around this call. Gradle writes ordinary
        # progress ("Welcome to Gradle 9.7.1!", "Starting a Gradle Daemon") to stderr, and with
        # 2>&1 merging, Windows PowerShell 5.1 turns every stderr line into an ErrorRecord. Under
        # 'Stop' the very first one becomes a terminating error and kills the script before the
        # build has done anything - which is exactly how v1 and v2 of this script failed, with a
        # NativeCommandError pointing at this line and a log containing only the header.
        # Arguments assembled into one flat string[] and passed directly, NOT with @-splatting.
        # Passing an array variable to a native command already forwards each element as its own
        # argument, and it does so without the string-vs-array ambiguity that broke v3.
        $gradleArgs = [string[]]($tasks + @('--console=plain', '--stacktrace'))

        $prevEAP = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'
        try {
            & $Gradlew $gradleArgs 2>&1 | ForEach-Object {
                # Merged stderr arrives as ErrorRecord, stdout as string. Normalise both.
                $line = if ($_ -is [System.Management.Automation.ErrorRecord]) {
                    $_.Exception.Message
                } else {
                    [string]$_
                }
                $Buffer.Add($line) | Out-Null
                Write-Host $line
            }
            $exitCode = $LASTEXITCODE
        }
        finally {
            $ErrorActionPreference = $prevEAP
        }
        if ($exitCode -ne 0) {
            $failed = $label
            Write-Log ""
            Write-Log "### FAILED: $label (exit $exitCode)" 'Red'
            break
        }
        Write-Log ""
        Write-Log "### OK: $label" 'Green'
    }
}
finally {
    Pop-Location
    Save-Log
}

# ---------------------------------------------------------------------------------------------
# Summary - the part you read. The log is the part Claude reads.
# ---------------------------------------------------------------------------------------------
Write-Host ''
if ($failed) {
    Write-Host "FAILED at: $failed" -ForegroundColor Red
    Write-Host ''

    $errors = $Buffer | Where-Object { $_ -match '^e: |error:|FAILURE:|Execution failed' } |
        Select-Object -First 25
    if ($errors) {
        Write-Host 'First errors:' -ForegroundColor Yellow
        $errors | ForEach-Object { Write-Host "  $_" }
        Write-Host ''
    }
} else {
    Write-Host "All tasks passed ($Scope)." -ForegroundColor Green
    Write-Host ''
}

# ---------------------------------------------------------------------------------------------
# Coverage summary.
#
# Parsed from the XML report, not scraped out of the HTML. The HTML markup is a JaCoCo
# implementation detail that has changed between releases; the XML schema (report.dtd, stable
# since 1.1) has not. Scraping <tfoot> out of index.html is the fragile version of this.
# ---------------------------------------------------------------------------------------------
if (-not $failed -and $Scope -eq 'coverage') {
    $covXml = Join-Path $RepoRoot 'app\build\reports\jacoco\all\jacocoTestReportAll.xml'
    if (Test-Path $covXml) {
        # The report opens with a DOCTYPE pointing at report.dtd, which is not written next to
        # it. .NET's XmlDocument refuses to load a document whose external DTD cannot be
        # resolved, so strip the declaration first - the counters do not need it.
        $raw = (Get-Content $covXml -Raw) -replace '<!DOCTYPE[^>]*>', ''
        $doc = [xml]$raw

        Write-Host 'Coverage (aggregated, :app):' -ForegroundColor Yellow
        foreach ($c in $doc.report.counter) {
            $missed  = [int]$c.missed
            $covered = [int]$c.covered
            $total   = $missed + $covered
            $pct     = if ($total -gt 0) { [math]::Round(100.0 * $covered / $total, 1) } else { 0 }
            Write-Host ('  {0,-12} {1,6}%  ({2}/{3})' -f $c.type, $pct, $covered, $total)
        }
        Write-Host ''
        Write-Host 'HTML report:' -ForegroundColor Gray
        Write-Host '  app\build\reports\jacoco\all\html\index.html' -ForegroundColor Gray
        Write-Host ''
    } else {
        Write-Host "Coverage XML not found at $covXml" -ForegroundColor Yellow
        Write-Host ''
    }
}

Write-Host 'Log written to:' -ForegroundColor Gray
Write-Host '  scripts\build-log\latest.log' -ForegroundColor Gray
Write-Host ''
Write-Host 'Tell Claude: "read scripts/build-log/latest.log" - it can open this file directly.' -ForegroundColor Gray

if ($failed) { exit 1 } else { exit 0 }

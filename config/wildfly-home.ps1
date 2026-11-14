<#
  Shared helper (dot-sourced by the other scripts): finds the WildFly folder.

  If -WildflyHome is given it is used as is. Otherwise the most recently modified folder named
  "wildfly*" under C:\tools that contains bin\standalone.bat is used, so the scripts do not depend
  on the exact name of the folder created by the WildFly zip (it differs between distributions).
#>
function Resolve-WildflyHome([string]$Given) {
    if ($Given) {
        if (-not (Test-Path (Join-Path $Given "bin\standalone.bat"))) {
            throw "No WildFly in '$Given' (bin\standalone.bat not found)."
        }
        return $Given
    }
    $found = Get-ChildItem -Path "C:\tools" -Directory -Filter "wildfly*" -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName "bin\standalone.bat") } |
        Sort-Object LastWriteTime -Descending
    if (-not $found) {
        throw "WildFly not found under C:\tools. Extract the zip there, or pass -WildflyHome <folder>."
    }
    if (@($found).Count -gt 1) {
        Write-Host "Several WildFly folders found; using the most recent: $($found[0].FullName)" -ForegroundColor Yellow
    }
    return $found[0].FullName
}

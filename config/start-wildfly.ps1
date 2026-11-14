<#
  Starts WildFly with the secrets of the project defined for THIS process only.

  Nothing secret is written in this file, in standalone.xml or in Git: the passwords are typed at the
  prompt and live only in the environment of the WildFly process that this script starts.

  Usage (from the project root):
    PS C:\dev\jee-webservices-carburants> .\config\start-wildfly.ps1

  Port offset 1: HTTP 8081 (8080 is used by a svchost on this machine), management 9991.
  Stop Tomcat first if it is running: it also listens on 8081.
#>
param(
    [string]$WildflyHome = "",
    [int]$PortOffset = 1
)

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\wildfly-home.ps1"
$WildflyHome = Resolve-WildflyHome $WildflyHome

function Read-PlainSecret([string]$Prompt) {
    $secure = Read-Host -Prompt $Prompt -AsSecureString
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr) }
}

if (-not (Test-Path "$WildflyHome\bin\standalone.bat")) {
    throw "WildFly not found in $WildflyHome. Pass -WildflyHome <folder> or install it first (see the guide)."
}

$env:CARBURANTS_DB_PASSWORD  = Read-PlainSecret "MySQL root password"
$env:CARBURANTS_API_USER     = "admin"
$env:CARBURANTS_API_PASSWORD = Read-PlainSecret "Password of the API user 'admin' (choose one, it is used by Postman and the clients)"

# A fresh random JWT key at each start: tokens issued before a restart simply stop being valid.
$bytes = New-Object byte[] 48
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:CARBURANTS_JWT_SECRET = [Convert]::ToBase64String($bytes)

Write-Host "Starting WildFly (HTTP port $(8080 + $PortOffset))..." -ForegroundColor Cyan
& "$WildflyHome\bin\standalone.bat" "-Djboss.socket.binding.port-offset=$PortOffset"

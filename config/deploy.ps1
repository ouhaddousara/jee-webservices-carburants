<#
  Copies the four WAR files built by Maven into WildFly's auto-deploy folder.
  WildFly deploys (or redeploys) each file by itself; the result is visible in its console.

  Usage (from the project root, after "mvn -q clean package"):
    PS C:\dev\jee-webservices-carburants> .\config\deploy.ps1
#>
param(
    [string]$WildflyHome = ""
)

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\wildfly-home.ps1"
$WildflyHome = Resolve-WildflyHome $WildflyHome
$target = Join-Path $WildflyHome "standalone\deployments"
if (-not (Test-Path $target)) { throw "Folder not found: $target" }

$wars = @(
    "soap\target\carburants-soap.war",
    "rest\target\carburants-rest.war",
    "jersey\target\carburants-jersey.war",
    "client-web\target\client-web.war"
)

foreach ($war in $wars) {
    if (-not (Test-Path $war)) { throw "Missing $war. Run: mvn -q clean package" }
    Copy-Item $war $target -Force
    Write-Host "Copied $war" -ForegroundColor Green
}
Write-Host "Check the WildFly console: each WAR must end with a 'Deployed' line." -ForegroundColor Cyan

<#
  Registers the MySQL driver and the CarburantsDS datasource in the RUNNING WildFly.

  Prerequisites:
    - WildFly is running (config\start-wildfly.ps1, in another terminal);
    - the database exists (mysql -u root -p -e "source db/create-database.sql");
    - mysql-connector-j-8.0.33.jar is on this machine (see the guide to fetch it with Maven).

  Usage (from the project root):
    PS C:\dev\jee-webservices-carburants> .\config\setup-datasource.ps1
#>
param(
    [string]$WildflyHome = "",
    [int]$PortOffset = 1,
    [string]$ConnectorJar = "$env:USERPROFILE\.m2\repository\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar"
)

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\wildfly-home.ps1"
$WildflyHome = Resolve-WildflyHome $WildflyHome

if (-not (Test-Path $ConnectorJar)) {
    throw "Driver jar not found: $ConnectorJar. Fetch it with: mvn dependency:get -Dartifact=com.mysql:mysql-connector-j:8.0.33"
}
$cli = Join-Path $WildflyHome "bin\jboss-cli.bat"
if (-not (Test-Path $cli)) { throw "jboss-cli.bat not found in $WildflyHome\bin" }

$secure = Read-Host -Prompt "MySQL root password" -AsSecureString
$bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try { $env:CARBURANTS_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr) }
$env:MYSQL_CONNECTOR_JAR = $ConnectorJar

$managementPort = 9990 + $PortOffset
& $cli --connect "--controller=localhost:$managementPort" "--file=$PSScriptRoot\wildfly-datasource.cli"

Remove-Item Env:\CARBURANTS_DB_PASSWORD -ErrorAction SilentlyContinue

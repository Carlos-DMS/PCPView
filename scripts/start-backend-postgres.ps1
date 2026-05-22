param(
    [string]$DbUrl = "jdbc:postgresql://localhost:5432/pcpview",
    [string]$DbUser = "pcpview",
    [string]$DbPassword = "pcpview",
    [string]$JwtSecret = "my-secret-key"
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot

$env:DB_URL = $DbUrl
$env:DB_USER = $DbUser
$env:DB_PASSWORD = $DbPassword
$env:JWT_SECRET = $JwtSecret

Set-Location $projectRoot
.\mvnw.cmd spring-boot:run

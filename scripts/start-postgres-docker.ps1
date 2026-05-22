$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker nao encontrado. Instale o Docker Desktop ou use scripts/init-postgres-local.ps1 com PostgreSQL instalado no Windows."
}

docker compose up -d postgres
docker compose ps postgres

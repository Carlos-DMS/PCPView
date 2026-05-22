$ErrorActionPreference = "Stop"

$base = "C:\Users\alexj\pgsql17"
$bin = Join-Path $base "pgsql\bin"
$data = Join-Path $base "data"

if (-not (Test-Path (Join-Path $bin "pg_ctl.exe"))) {
    throw "PostgreSQL portatil nao encontrado em $base."
}

& (Join-Path $bin "pg_ctl.exe") -D $data stop

$ErrorActionPreference = "Stop"

$base = "C:\Users\alexj\pgsql17"
$bin = Join-Path $base "pgsql\bin"
$data = Join-Path $base "data"
$log = Join-Path $base "postgres.log"

if (-not (Test-Path (Join-Path $bin "pg_ctl.exe"))) {
    throw "PostgreSQL portatil nao encontrado em $base."
}

if (-not (Test-Path $data)) {
    throw "Cluster de dados nao encontrado em $data. Inicialize o banco antes de iniciar."
}

& (Join-Path $bin "pg_ctl.exe") -D $data -l $log -o "-p 5432" start

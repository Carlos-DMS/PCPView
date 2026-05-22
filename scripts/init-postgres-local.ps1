param(
    [string]$HostName = "localhost",
    [string]$Port = "5432",
    [string]$AdminUser = "postgres",
    [string]$DbName = "pcpview",
    [string]$DbUser = "pcpview",
    [string]$DbPassword = "pcpview"
)

$ErrorActionPreference = "Stop"

function Resolve-Psql {
    $psqlCommand = Get-Command psql -ErrorAction SilentlyContinue
    if ($psqlCommand) {
        return $psqlCommand.Source
    }

    $portableCandidate = "C:\Users\alexj\pgsql17\pgsql\bin\psql.exe"
    if (Test-Path $portableCandidate) {
        return $portableCandidate
    }

    $postgresRoot = "C:\Program Files\PostgreSQL"
    if (Test-Path $postgresRoot) {
        $candidate = Get-ChildItem -Path $postgresRoot -Directory |
            Sort-Object Name -Descending |
            ForEach-Object { Join-Path $_.FullName "bin\psql.exe" } |
            Where-Object { Test-Path $_ } |
            Select-Object -First 1

        if ($candidate) {
            return $candidate
        }
    }

    throw "psql nao encontrado. Instale o PostgreSQL ou adicione a pasta bin do PostgreSQL ao PATH."
}

$psql = Resolve-Psql

$roleSql = @"
DO `$`$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$DbUser') THEN
        CREATE ROLE $DbUser LOGIN PASSWORD '$DbPassword';
    ELSE
        ALTER ROLE $DbUser WITH LOGIN PASSWORD '$DbPassword';
    END IF;
END
`$`$;
"@

& $psql -h $HostName -p $Port -U $AdminUser -d postgres -v ON_ERROR_STOP=1 -c $roleSql

$dbExists = & $psql -h $HostName -p $Port -U $AdminUser -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '$DbName'"

if ($dbExists.Trim() -ne "1") {
    & $psql -h $HostName -p $Port -U $AdminUser -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE $DbName OWNER $DbUser;"
}

& $psql -h $HostName -p $Port -U $AdminUser -d postgres -v ON_ERROR_STOP=1 -c "ALTER DATABASE $DbName OWNER TO $DbUser;"

Write-Host "PostgreSQL preparado: database=$DbName user=$DbUser host=$HostName port=$Port"

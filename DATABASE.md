# Banco de dados

O backend esta configurado para usar PostgreSQL por padrao.

## Opcao recomendada local

Use o `compose.yaml` do projeto para subir um PostgreSQL local com volume persistente.

```powershell
docker compose up -d postgres
```

Ou use o script do projeto:

```powershell
.\scripts\start-postgres-docker.ps1
```

Esse comando cria um container chamado `pcpview-postgres` e guarda os dados no volume `pcpview_postgres_data`. Ao parar e iniciar o backend, os dados continuam salvos.

Para parar o banco sem apagar os dados:

```powershell
docker compose stop postgres
```

Para apagar o banco e todos os dados locais, use apenas quando quiser zerar tudo:

```powershell
docker compose down -v
```

## Opcao Windows sem Docker

Se o PostgreSQL estiver instalado diretamente no Windows, rode este script para criar o usuario e o banco:

```powershell
.\scripts\init-postgres-local.ps1
```

Por padrao ele usa:

```properties
database=pcpview
user=pcpview
password=pcpview
host=localhost
port=5432
```

Se o usuario administrador do PostgreSQL tiver outro nome, informe no parametro:

```powershell
.\scripts\init-postgres-local.ps1 -AdminUser postgres
```

## Instalacao portatil atual

Nesta maquina, o PostgreSQL 17.10 foi instalado de forma portatil em:

```text
C:\Users\alexj\pgsql17
```

O cluster de dados fica em:

```text
C:\Users\alexj\pgsql17\data
```

Para iniciar esse PostgreSQL portatil:

```powershell
.\scripts\start-postgres-portable.ps1
```

Para parar:

```powershell
.\scripts\stop-postgres-portable.ps1
```

## Variaveis de ambiente

Configuracao padrao local do backend:

```properties
DB_URL=jdbc:postgresql://localhost:5432/pcpview
DB_USER=pcpview
DB_PASSWORD=pcpview
```

Existe um arquivo `.env.example` com essas chaves. Para uso local, crie um arquivo `.env` baseado nele se quiser personalizar usuario, senha ou nome do banco.

Para rodar em outro PostgreSQL, como servidor local ou banco em nuvem, defina essas variaveis antes de iniciar o Spring Boot.

Exemplo PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/pcpview"
$env:DB_USER="pcpview"
$env:DB_PASSWORD="pcpview"
.\mvnw.cmd spring-boot:run
```

Ou use o script:

```powershell
.\scripts\start-backend-postgres.ps1
```

## Estrutura das tabelas

No PostgreSQL, a estrutura do banco e controlada pelo Flyway em:

```text
src/main/resources/db/migration
```

A primeira migracao e:

```text
V1__create_pcpview_schema.sql
```

O Hibernate esta configurado com:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Assim, em banco real, o Hibernate valida se as tabelas existem e batem com os modelos, mas nao altera o schema automaticamente.
Quando precisar mudar tabela no futuro, crie uma nova migracao `V2__nome_da_mudanca.sql`, depois `V3__...`, e assim por diante.

## Testes

Os testes automatizados usam o perfil `test`, com H2 em memoria e Flyway desativado, para nao depender de um banco PostgreSQL instalado na maquina.

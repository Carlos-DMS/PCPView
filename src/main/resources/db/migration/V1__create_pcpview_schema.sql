CREATE TABLE IF NOT EXISTS tb_users (
    id UUID PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_maquinas (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    operacional BOOLEAN
);

CREATE TABLE IF NOT EXISTS tb_produtos (
    id VARCHAR(255) PRIMARY KEY,
    sku VARCHAR(255) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_ordens (
    numero_ordem VARCHAR(255) PRIMARY KEY,
    quantidade_total INTEGER NOT NULL,
    quantidade_produzida INTEGER,
    status VARCHAR(50),
    data_criacao TIMESTAMP(6),
    product_id VARCHAR(255),
    CONSTRAINT fk_ordens_produtos
        FOREIGN KEY (product_id)
        REFERENCES tb_produtos (id)
);

CREATE TABLE IF NOT EXISTS tb_sub_ordens (
    id VARCHAR(255) PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL,
    quantidade_total INTEGER NOT NULL,
    quantidade_produzida INTEGER NOT NULL,
    status VARCHAR(50),
    machine_ideal_id VARCHAR(255),
    posicao_fila INTEGER,
    inicio_do_processo TIMESTAMP(6),
    CONSTRAINT fk_sub_ordens_ordens
        FOREIGN KEY (order_id)
        REFERENCES tb_ordens (numero_ordem),
    CONSTRAINT fk_sub_ordens_maquinas
        FOREIGN KEY (machine_ideal_id)
        REFERENCES tb_maquinas (id)
);

CREATE TABLE IF NOT EXISTS tb_execucoes (
    id UUID PRIMARY KEY,
    machine_id VARCHAR(255) NOT NULL,
    sub_order_id VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    quantidade_feita_nesta_sessao INTEGER NOT NULL,
    data_inicio TIMESTAMP(6),
    data_fim TIMESTAMP(6),
    pausa_inicio TIMESTAMP(6),
    tempo_pausado_segundos BIGINT,
    setup_inicio TIMESTAMP(6),
    setup_fim TIMESTAMP(6),
    tempo_setup_pausado_segundos BIGINT,
    tempo_setup_segundos BIGINT,
    CONSTRAINT fk_execucoes_maquinas
        FOREIGN KEY (machine_id)
        REFERENCES tb_maquinas (id),
    CONSTRAINT fk_execucoes_sub_ordens
        FOREIGN KEY (sub_order_id)
        REFERENCES tb_sub_ordens (id),
    CONSTRAINT fk_execucoes_users
        FOREIGN KEY (user_id)
        REFERENCES tb_users (id)
);

CREATE INDEX IF NOT EXISTS idx_sub_ordens_maquina_fila
    ON tb_sub_ordens (machine_ideal_id, posicao_fila);

CREATE INDEX IF NOT EXISTS idx_sub_ordens_status
    ON tb_sub_ordens (status);

CREATE INDEX IF NOT EXISTS idx_execucoes_sub_ordem
    ON tb_execucoes (sub_order_id);

CREATE INDEX IF NOT EXISTS idx_execucoes_status
    ON tb_execucoes (status);

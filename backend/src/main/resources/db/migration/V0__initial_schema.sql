CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    telefone VARCHAR(30) NOT NULL,
    senha VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS material (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    descricao VARCHAR(2000) NOT NULL,
    quantidade INTEGER NOT NULL,
    unidade VARCHAR(30) NOT NULL,
    endereco_retirada VARCHAR(240) NOT NULL,
    cidade VARCHAR(120) NOT NULL,
    proprietario_id BIGINT NOT NULL REFERENCES usuario(id),
    categoria VARCHAR(80)
);

CREATE TABLE IF NOT EXISTS reserva (
    id BIGSERIAL PRIMARY KEY,
    quantidade INTEGER
);

-- V1 le estas colunas legadas ao preencher os novos campos de quantidade.
-- Elas aceitam NULL porque as entidades atuais escrevem apenas nos novos campos.
CREATE TABLE IF NOT EXISTS doacao (
    id BIGSERIAL PRIMARY KEY,
    quantidade INTEGER
);
-- Cria a estrutura anterior a V1 para a primeira execucao em um banco vazio.
-- Bancos legados reconhecidos com baseline-version=0 ja possuem estas tabelas;
-- neles o Flyway considera esta versao coberta pelo baseline.

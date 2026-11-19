-- Callback do Flyway executado antes de cada "migrate" (não é versionado nem tem checksum).
--
-- As migrations V1/V2 foram escritas sobre um banco que já possuía as tabelas
-- usuario, material, reserva e doacao (criadas antes do Flyway) e apenas
-- acrescentam colunas a elas. Em um banco novo e vazio essas tabelas não existem
-- e o V1 falharia. Este script cria a estrutura base somente quando ela não existe:
--   * banco novo   -> as tabelas são criadas e o V1/V2 completam o restante;
--   * banco atual  -> nada acontece (IF NOT EXISTS), os dados são preservados.
--
-- As colunas seguem o diagrama ReConstroi-Diagrama-Banco e as entidades JPA.
-- Colunas legadas que o V1 ainda lê (material.categoria, reserva.quantidade,
-- doacao.quantidade) são criadas como opcionais apenas para manter a compatibilidade.

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

CREATE TABLE IF NOT EXISTS doacao (
    id BIGSERIAL PRIMARY KEY,
    quantidade INTEGER
);

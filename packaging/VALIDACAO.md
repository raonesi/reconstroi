# Validação do executável desktop

Arquivo: dist/ReConstroi.exe. Build Windows x64, 18/09/2026.

Validação feita em pasta e banco temporários separados dos dados principais:

- Extração de PostgreSQL, Java, API e interface a partir do único EXE.
- Inicialização automática de cluster novo e criação de reconstroi.
- Aplicação das migrations V0, V1 e V2 e API com status UP.
- Janela nativa ReConstrói — Login aberta e inspecionada visualmente.
- Cadastro e login testados pelas classes DAO do JAR desktop empacotado.
- Cadastro de material com foto, consulta, pesquisa e edição aprovados.
- Encerramento pelo launcher: PostgreSQL parou de aceitar conexões.
- Reabertura: nome, quantidade e foto permaneceram salvos.
- Exclusão aprovada: material ausente da listagem e consulta retornando 404.
- Teste temporário encerrado; executável reaberto com o banco principal preservado.

A automação validou as operações pelas classes usadas pela interface; não percorreu manualmente todos os formulários.
O frontend web e o Node.js não fazem parte deste pacote. O launcher não abre URLs.

Os arquivos do teste estão na pasta ignorada packaging/work-v3. O executável anterior foi guardado em dist/backups.

Pasta definitiva dos dados: %USERPROFILE%\.reconstroi. Banco principal preservado nessa pasta.
SHA-256 do executavel final: 88B6F8EC1906A7C768FE07665F8A12F768A2A2826EF760338DB0227896C0F5AF

## Atualização: arrastar fotos e WebP

- Novo painel na tela de cadastro com TransferHandler para arquivos do Explorador.
- Seleção múltipla, filtro JPG/JPEG/PNG/WebP, cinco fotos de até 5 MB, sem duplicados.
- Remoção afeta somente a seleção; arquivos originais são preservados.
- MIME WebP enviado explicitamente, sem depender do registro do Windows.
- VIEW.FotosMaterialCheck aprovado: transferência, limites, duplicados, remoção e cabeçalho multipart image/webp.
- Executável reconstruído; versão anterior preservada em dist/backups.
- A reabertura encontrou bloqueio do Controle de Aplicativo do Windows. A abertura visual desta atualização não foi confirmada.
SHA-256 desta atualizacao: 748EDC9C1595F6AF09BC7B8629C75549EDF6ED55E4C08D9D35837008DA82F112

## Atualização: remoção da recuperação de senha

- Removidos botão desktop, página web, métodos DAO/API, endpoints, DTOs, serviço de recuperação e configuração de exposição de tokens.
- Migração V3 remove somente a tabela de tokens; usuários e senhas não são alterados. Será aplicada no banco principal ao iniciar a nova versão.
- Backend compilado em packaging/work-v3/backend-removal-check porque a limpeza de backend/target encontrou diretórios bloqueados. O JAR novo foi copiado para backend/target e empacotado.
- Maven verify: 13 testes aprovados em banco exclusivo reconstroi_removal_test_20260918, incluindo autenticação e regras do CRUD.
- Migração V3 aplicada no banco de testes; ausência da tabela de tokens confirmada por SQL.
- ESLint e Next build aprovados, sem rota de recuperação.
- Desktop recompilado e FotosMaterialCheck aprovado.
- EXE reconstruído. Abertura desta versão não confirmada; a tentativa anterior encontrou bloqueio do Controle de Aplicativo do Windows.
- SHA-256: C110FB8FBF37E7B632DEAD603ECD1C234613F86DDE79B791E9BB35E84E4B3DB5

## Atualização: diagrama Astah

Banco principal migrado até V4; 11 tabelas e 104 colunas. Exceção autorizada: recuperação de senha removida. 15 testes passaram em banco isolado; teste de estrutura passou também no banco principal. Usuário e senha preservados por comparação antes/depois. Detalhes em CONFERENCIA-DIAGRAMA.md.
SHA-256: 002B9D9CB72CD4E7F502B5506526D20882B15E6F180A980671D4D0CF6AE82BA3


## Revisão de legibilidade — 19/09/2026

Login e formulário de material separados em etapas; nomes internos mais claros, laços explícitos nos pontos principais e guia LEIA-ME-PROFESSOR.md. Contratos da API e esquema do banco preservados. Backend: 15 testes aprovados em PostgreSQL temporário isolado. Desktop compilado; FotosMaterialCheck aprovado. Executável reconstruído, sem nova verificação visual.
SHA-256: 894B3E7800C773BB18C0065BDA1590350DF4252856FF5D1D906BC055B2005666

## Correção de inicialização do PostgreSQL — 22/09/2026

O PostgreSQL não abria pelo `Iniciar-Banco.cmd` porque `start-database.ps1`
enviava o valor de `-o` com aspas duplicadas. O PostgreSQL interpretava isso
como um `listen_addresses` inválido. O script foi corrigido para passar a linha
de argumentos do Windows corretamente. `Iniciar-Banco.cmd` foi testado com
sucesso, `pg_isready` confirmou `localhost:5432 - accepting connections` e uma
consulta ao banco `reconstroi` retornou normalmente. Nenhum dado foi apagado.


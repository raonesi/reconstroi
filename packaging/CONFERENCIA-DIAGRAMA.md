# Conferência do banco com o diagrama Astah

Referência: `ReConstroi-Diagrama-Banco.asta` (arquivo de modelagem, mantido fora do repositório).
Conferência realizada em 18/09/2026. O arquivo original não foi alterado.

O banco principal `reconstroi` foi atualizado até a migração V4. Agora possui
11 tabelas e 104 colunas, correspondentes aos atributos do diagrama com uma
exceção expressamente solicitada: `token_recuperacao_senha` permanece removida,
assim como a funcionalidade de recuperação.

| Tabela | Ajuste |
| --- | --- |
| material | Adicionados `estado` e `usuario_id`, com FK para usuario |
| reserva | Adicionados `material`, `solicitante` e `status` |
| doacao | Adicionados `data`, `doador` e `material` |
| sessao | Criada com `id`, `token_hash` único, `usuario_id` com FK e `expira_em` |
| Demais tabelas | Campos já correspondiam ao diagrama |

O diagrama contém campos equivalentes antigos e novos. Gatilhos do banco
preenchem os equivalentes durante cada inclusão/alteração: `usuario_id` recebe
`proprietario_id`, `estado` recebe `estado_conservacao` e as quantidades antigas
recebem as quantidades atuais. Os nomes textuais de materiais, pessoas e categorias
são obtidos das relações na gravação. Esses nomes são retratos daquele momento;
alterar uma pessoa posteriormente não reescreve os nomes em reservas históricas.
A data textual da doação usa ISO 8601 em UTC.

Cadastro e login registram sessões com SHA-256 do token, sem armazenar o token
em texto. A autenticação continua usando a assinatura e validade do JWT.

O Astah especifica tipos UML (`String`, `Long`, `int`, `DateTime`, `Decimal`,
`boolean`), não comprimentos SQL, precisão ou nulabilidade de todos os campos.
Esses detalhes mantêm as regras existentes. Tipos equivalentes PostgreSQL, nomes,
ausência de colunas extras, FKs e unicidades indicadas foram verificados por teste.
O manifesto extraído está em `backend/src/test/resources/astah-schema.json`.

Validação: 15 testes aprovados em banco separado, incluindo registro de sessão,
reserva, retirada, finalização de doação e sincronização dos campos. No banco
principal foi executado apenas o teste de estrutura com as migrações; não foram
inseridos usuários ou materiais de teste. A impressão digital de todos os dados
da tabela usuario permaneceu idêntica antes/depois.

Backup anterior: `packaging/work-v3/reconstroi-before-diagram.dump`.
Executável atualizado: `dist/ReConstroi.exe`. A abertura visual desta versão não
foi confirmada; a tentativa anterior encontrou bloqueio do Controle de Aplicativo
do Windows.

## Reconferência de 24/10/2026

O arquivo `ReConstroi-Diagrama-Banco.asta` (18/09/2026, sem alterações) foi lido de novo por um leitor de serialização Java e comparado, coluna a coluna, com o banco `reconstroi` (Flyway V0–V5): o diagrama tem 12 tabelas e 109 atributos, o banco 11 tabelas e 104 colunas; as 104 colunas das 11 tabelas comuns coincidem em nome e tipo, e PKs, 15 de 16 FKs e a tabela do Flyway também. Os 5 atributos que faltam são os de `token_recuperacao_senha`.

Ajuste feito: `doacao.reserva_id` está marcado `UNIQUE` no diagrama e era garantido por índice único parcial. A migração `V5__doacao_reserva_unique_constraint.sql` o substitui por uma restrição `UNIQUE`; `DiagramSchemaTests` agora rejeita índices parciais nas colunas marcadas PK/UNIQUE.

Depois, a pedido, a migração `V6__token_recuperacao_senha.sql` recriou a tabela `token_recuperacao_senha` (só a tabela: nenhuma entidade, serviço ou endpoint a usa, e a recuperação de senha continua desativada). O teste `DiagramSchemaTests` deixou de abrir exceção para ela.

Resultado final (Flyway V0–V6): 12 tabelas, 109 colunas e 16 FKs no banco, iguais às 12 tabelas, 109 atributos e 16 associações do diagrama, sem nenhuma divergência. As afirmações acima de que a tabela ficava fora do banco valem só até a V5.

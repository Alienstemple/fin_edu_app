# Agent Setup

Репозиторий поддерживает только Claude Code и Codex. Cursor-specific правила и конфиги не поддерживаются.

## Общие правила

1. Запускайте агента из корня репозитория `fin_edu_app`.
2. Скопируйте `.env.example` в `.env` и заполните нужные токены.
3. Не коммитьте `.env`, токены, ключи подписи и user-specific пути.
4. Общие инструкции для агентов лежат в `AGENTS.md`.
5. Canonical skills лежат в `.agents/skill-definitions`.

## Claude Code

- Точка входа инструкций: `CLAUDE.md`, который импортирует `AGENTS.md`.
- Project skills: `.claude/skills/*/SKILL.md`.
- Эти файлы являются wrappers и ссылаются на `.agents/skill-definitions`.
- MCP config: `.mcp.json`.

Если MCP-серверу нужны переменные из `.env`, запускайте Claude из shell, где они уже экспортированы:

```bash
set -a
source .env
set +a
claude
```

## Codex

- Точка входа инструкций: `AGENTS.md`.
- Project skills: `.codex/skills/*/SKILL.md`.
- Эти файлы являются wrappers и ссылаются на `.agents/skill-definitions`.
- MCP config: `.codex/config.toml`.

Проверка подключенных MCP-серверов:

```bash
codex mcp list
```

Если меняете `name` или `description` у canonical skill, синхронизируйте metadata в соответствующем wrapper в `.codex/skills` и `.claude/skills`.

## Skills

Правило владения: полный текст skill должен быть только в `.agents/skill-definitions/<skill-name>.md`.

Agent-specific wrappers нужны только для discoverability:

- `.claude/skills/<skill-name>/SKILL.md`
- `.codex/skills/<skill-name>/SKILL.md`

В wrappers не добавляйте проектные правила, примеры и workflow. Добавляйте их в canonical skill.

Добавление нового skill:

1. Создайте `.agents/skill-definitions/<skill-name>.md` с frontmatter `name` и `description`.
2. Создайте wrappers `.claude/skills/<skill-name>/SKILL.md` и `.codex/skills/<skill-name>/SKILL.md` с тем же
   frontmatter и ссылкой на canonical файл.
3. Добавьте строку в таблицу skills в `AGENTS.md`.

## MCP And Secrets

Базовые MCP-серверы:

- `context7`: документация SDK/API. Опционально `CONTEXT7_API_KEY`.
- `figma`: спецификации Figma. Авторизация через OAuth при первом обращении.

Если добавляете или удаляете MCP-сервер, обновите оба repo-конфига:

- `.mcp.json` для Claude Code.
- `.codex/config.toml` для Codex.

Также обновите `.env.example`, если меняется список переменных.

## Product Core

Для изменений внутри `app-modules/product-core` дополнительно действует `app-modules/product-core/AGENTS.md`.

Агент обязан проверить README API-модулей, если изменение затрагивает публичные контракты `*/api` модулей.

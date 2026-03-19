# Agent Guidelines

This repo keeps “fresh memory” notes under `.$state/`. These docs are the primary mental model for the script editor + interpreter stack.

Default todo list: `.$state/TODO.md`

## Compilation and Building

To compile the project, run the gradlew.bat compileJava task on ONLY the root project (CustomNPC-DBC-Addon). This applies for CustomNPC-Plus as well.
And WAIT!!! Waiting is essential, as the task can usually take up to 3 minutes. As long as the compileJava task is running, DO NOT close the terminal or interrupt the process.

## How to use state docs

Before exploring source code or making edits, scan the user prompt for subsystem keywords and open the relevant `.$state/*.md` doc(s) first. If multiple subsystems are involved, read all relevant docs (usually 1–3).

Recommended read order (when multiple apply):
1. `.$state/SYNTAX_HIGHLIGHTER.md`
2. `.$state/AUTOCOMPLETE.md`
3. `.$state/DTS_ECOSYSTEM.md`
4. `.$state/SCRIPT_EDITOR.md`
5. `.$state/SCRIPT_HANDLING.md`

The goal is not to treat the docs as perfectly authoritative; it’s to refresh terminology, invariants, and the intended architecture before verifying details in source.

## State docs (what to read when)

- `.$state/SYNTAX_HIGHLIGHTER.md`
  - Read when the prompt involves: `ScriptDocument`, parsing/marking/token building, `TokenType` priority, `FieldChainMarker`, validation errors, hover/underlines, synthetic built-ins, JSDoc, or type-resolution questions involving `TypeResolver`, `resolveType` or `resolveExpressionType`.

- `.$state/AUTOCOMPLETE.md`
  - Read when the prompt involves: autocomplete UI/behavior, prefix/receiver detection, insertion behavior, providers (`JavaAutocompleteProvider`, `JSAutocompleteProvider`), suggestion ranking, usage tracking, import insertion, or missing/wrong suggestions.

- `.$state/DTS_ECOSYSTEM.md`
  - Read when the prompt involves: `.d.ts` generation (Gradle plugin in `gradle-plugins/`), `hooks.d.ts` / `index.d.ts`, runtime parsing (`TypeScriptDefinitionParser`), registry behavior (`JSTypeRegistry`), `JSTypeInfo`/`JSMethodInfo`/`JSFieldInfo`, or TypeScript generic bound handling.

- `.$state/SCRIPT_EDITOR.md`
  - Read when the prompt involves: `GuiScriptTextArea`, editor UX (scroll/selection/search/go-to-line/rename), hover UI wiring, or the client editor model (`ScriptTextContainer`).

- `.$state/SCRIPT_HANDLING.md`
  - Read when the prompt involves: (`JaninoScript`, `janino`, `GuiScriptInterface` ), runtime script storage/execution (`ScriptContainer`), script-related NBT serialization, hook dispatch, script engine selection, or script controller/handler wiring.

## Cross-subsystem quick picks

- Autocomplete issues that smell like “wrong receiver type / missing members / cannot resolve symbol”:
  - Read `.$state/AUTOCOMPLETE.md` + `.$state/SYNTAX_HIGHLIGHTER.md` (+ `.$state/DTS_ECOSYSTEM.md` if JS/.d.ts types involved).
- JS autocomplete issues specifically:
  - Read `.$state/AUTOCOMPLETE.md` + `.$state/DTS_ECOSYSTEM.md` + the relevant `.$state/SYNTAX_HIGHLIGHTER.md` sections.
- Hover tooltip / red underline / error text mismatches:
  - Read `.$state/SYNTAX_HIGHLIGHTER.md`.

## Workflow expectation

1. Identify subsystem keywords in the user request.
2. Read the relevant `.$state/*.md` doc(s) first (before open-ended repo exploration).
3. Use the docs to decide which source files to inspect next.
4. Verify critical details against source before making edits (the docs can drift).
5. If edits change behavior/architecture described in a state doc, ask the user whether they want the doc updated as part of the change.


## Planning

When I prompt you to plan something, I want you to always follow these steps:
1. Analyze the user request carefully to understand their needs and objectives.
2. Break down the request into smaller, manageable tasks or components.
3. Prioritize the tasks based on their importance and dependencies.
4. Create a detailed plan or outline that addresses each task systematically.
5. Review the plan to ensure it aligns with the user's goals and is feasible to implement.
6. Present the plan to the user for feedback and approval before proceeding with implementation.
7. Once approved, execute the plan step by step, keeping the user informed of progress and any adjustments needed along the way.
8. After completing the tasks, review the outcomes with the user to ensure satisfaction and address any further needs.
9. Document the process and results for future reference and learning.

This plan must always be created and stored at .sisyphus/plans as a .md file with a unique name related to the task.
That document must be your main source of truth for the plan, you must always refer to it before and after taking any action.

## Expert-Scripter Conventions

For CustomNPC+ scripting conventions and quality standards, see `.github/agents/AGENTS.md`.

That file documents:
- Mandatory review gates and triggers
- API verification requirements (zero-tolerance policy)
- Storage decision tree (getNbt vs getStoredData vs getTempData)
- Subagent usage rules
- Production quality checklist
- Code-Review conventions
- Training scripts policy
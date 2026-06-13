# Advanced Configuration

## Preference Page Split

AI Peon configuration is split into two preference pages:

| Page | Purpose |
|------|--------|
| **AI Peon Configuration** | Basic provider, model, URL settings for everyday use |
| **AI Peon Advanced** | Per-agent models, temperatures, debug mode, query/header parameters |

This separation keeps the default configuration simple while providing power users access to fine-grained controls.

## Per-Agent Model Fallback Chain

Three optional preference keys allow different models per agent:

- `PREF_SEARCH_MODEL` — model for search agent
- `PREF_PLAN_MODEL` — model for plan agent  
- `PREF_DEV_MODEL` — model for dev agent

**Fallback behavior**: If an agent-specific model is empty or null, the default model (`PREF_MODEL`) is used.

```
getSearchModel() → searchModel != null && !blank ? searchModel : model
getPlanModel()   → planModel   != null && !blank ? planModel   : model
getDevModel()    → devModel    != null && !blank ? devModel    : model
```

This ensures backward compatibility — existing configurations with only a default model continue working.

## First-Launch Directory Resolution

On first launch, AI Peon resolves skills and commands directories:

1. Check if `~/.claude/skills` exists → use it (Claude Desktop compatibility)
2. Otherwise create and use `~/.llmpeon/skills`

Same logic applies to commands directory (`~/.claude/commands` → `~/.llmpeon/commands`).

This one-time resolution ensures deterministic behavior without filesystem I/O on every config load.

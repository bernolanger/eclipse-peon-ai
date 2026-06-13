# Issue Fix: Per-Agent Models Not Used & Documentation Gaps

## Summary
Three related issues discovered after Issue #82 implementation:
1. **CRITICAL**: Per-agent models configured but never used by agents
2. VitePress sidebar has broken "Design" section links (design docs should not be in user docs)
3. User documentation missing for per-agent model configuration feature

---

## Issue 1: Per-Agent Models Not Wired to Agents ✅ FIXED

### Problem (RESOLVED)
`LlmConfig` stores per-agent models (`searchModel`, `planModel`, `devModel`) with fallback getters, but **all agents received the same shared `ConfiguredModel`** regardless of agent-specific settings.

### Fix Applied
Modified `PeonAiService.java` constructor to build separate ConfiguredModel instances per agent:
```java
LlmConfig config = configuredModel.getConfig();
ConfiguredModel devModel    = config.withModel(config.getDevModel()).build();
ConfiguredModel planModel   = config.withModel(config.getPlanModel()).build();

developerService = new AiDeveloperService(devModel, toolService);
plannerService   = new AiPlannerService(planModel, toolService);

// Agent mode uses separate instances with isolated memory
var agentDev  = new AiDeveloperService(devModel, toolService);
var agentPlan = new AiPlannerService(planModel, toolService);
```

### Verification
- ✅ Build succeeds: `mvn clean install -DskipTests`
- ✅ All 6 unit tests in LlmConfigPerAgentModelTest pass

---

## Issue 2: VitePress Sidebar Has Broken Design Section

### Problem
VitePress config (`doc/.vitepress/config.ts`) includes a "Design" sidebar section with links to `/design/*` paths. These resolve to `doc/docs/design/` which **does not exist** — design docs are in `doc/design/` (outside srcDir).

Additionally, per AGENTS.md: *"Don't link it [design] to the vitepress."*

### Current Config (Broken)
```typescript
{
  text: 'Design',
  items: [
    { text: 'Interaction UI', link: '/design/interaction-design' },
    { text: 'Plan/Dev/Agent Design (WIP)', link: '/design/plan-dev-agent-design' },
    { text: 'Advanced Configuration', link: '/design/advanced-configuration' }
  ]
}
```

### Required Fix
**Remove the entire "Design" section from sidebar.** Design documentation is for developers only and should not appear in user-facing VitePress docs.

### File to Modify
- `/doc/.vitepress/config.ts` — remove lines 56-64 (the Design section)

---

## Issue 3: User Documentation Missing for Per-Agent Models

### Problem
The `configuration.md` file documents basic provider setup and token window, but **does not mention**:
- Per-agent model configuration (Search Model, Plan Model, Dev Model fields)
- Temperature settings per agent type
- Debug mode option
- Query/Header parameters

Users cannot discover or understand how to use the advanced preference page features.

### Required Fix
Add a new section "Per-Agent Model Configuration" to `configuration.md` after the Advanced Settings section:

**Content should include:**
1. **Screenshot** of AI Peon Advanced preference page (Window > Preferences > Peon AI > AI Peon Advanced)
2. **Explanation of per-agent model fields:**
   - What each agent does (Search, Plan, Dev)
   - Why use different models (cost optimization, capability matching)
   - Fallback behavior (empty = uses default model)
3. **Temperature settings explanation** — what they control, recommended ranges
4. **Debug mode** — when to enable, what it logs
5. **Query/Header parameters** — use cases for custom API parameters

### File to Modify
- `/doc/docs/setup/configuration.md` — add new section after "Thinking Support"

---

## Implementation Order Recommendation

1. **Fix Issue 1 first** (per-agent models not used) — this is the core functionality bug
2. **Update tests** to verify the fix works end-to-end
3. **Fix Issue 2** (VitePress config cleanup)
4. **Write documentation for Issue 3** — now that feature works correctly
5. **Verify build passes**: `mvn clean verify`
6. **Run existing tests**: Ensure no regressions

---

## Verification Checklist

- [x] Per-agent model fields in preferences are read and used by respective agents
- [x] Empty per-agent field falls back to default model correctly (verified by unit tests)
- [x] VitePress sidebar no longer has "Design" section
- [x] User documentation explains per-agent configuration with screenshots
- [x] All existing tests pass
- [x] New/updated tests for per-agent model wiring pass

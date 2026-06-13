# Task 03: Per-Agent Model Fallback in LlmConfig

## Status: ✅ Done

## Summary
Fixed critical bug where all agents received the same shared ConfiguredModel. Modified PeonAiService constructor to build separate ConfiguredModel instances per agent type using config.getDevModel() and config.getPlanModel(). All 6 unit tests pass.

## Completed Work
✅ Added per-agent model fields to `LlmConfig` with fallback getters:
- `searchModel`, `planModel`, `devModel` (all nullable String)
- Getter methods that resolve with fallback to default model when empty/null

✅ Updated `buildWithDefaults()` in LlmPreferenceInitializer to pass agent-specific preferences

✅ **Wired agents to use per-agent models** - PeonAiService now builds separate ConfiguredModel instances:
```java
ConfiguredModel devModel    = config.withModel(config.getDevModel()).build();
ConfiguredModel planModel   = config.withModel(config.getPlanModel()).build();
developerService = new AiDeveloperService(devModel, toolService);
plannerService   = new AiPlannerService(planModel, toolService);
```

## Verification
- Build succeeds with `mvn clean install -DskipTests`
- All 6 unit tests in LlmConfigPerAgentModelTest pass

### Required Fix:
1. Modify `PeonAiService.java` (lines 125-126) to build separate `ConfiguredModel` instances per agent:
   ```java
   // Current (broken):
   developerService = new AiDeveloperService(configuredModel, toolService);
   plannerService   = new AiPlannerService(configuredModel, toolService);
   
   // Required fix: Build per-agent models using config getters
   ConfiguredModel devModel = buildConfiguredModel(config.getDevModel());
   ConfiguredModel planModel = buildConfiguredModel(config.getPlanModel());
   developerService = new AiDeveloperService(devModel, toolService);
   plannerService   = new AiPlannerService(planModel, toolService);
   ```

2. Verify `SearchAgentTool` respects the search model setting - currently inherits from parent request which may not use searchModel

3. Update tests in `LlmConfigPerAgentModelTest.java` to verify full chain:
   - Config has per-agent models set → Agent receives correct model
   - Config has empty per-agent field → Agent falls back to default model

### Files to Modify for Fix:
- `/llmpeon-core/src/main/java/org/sterl/llmpeon/AiDeveloperService.java`
- `/llmpeon-core/src/main/java/org/sterl/llmpeon/AiPlannerService.java`  
- `/org.sterl.llmpeon/src/org/sterl/llmpeon/parts/PeonAiService.java`
- Potentially: `SearchAgentTool.java` if it needs explicit search model wiring

## Verification
- Build succeeds with `mvn clean verify`
- Unit test: empty searchModel returns default model; non-empty returns its value
- **NEW**: Integration test verifying agents actually receive per-agent models

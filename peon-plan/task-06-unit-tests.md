# Task 06: Unit Tests for Resolution and Fallback

## Description
Add unit tests verifying directory resolution logic and per-agent model fallback behavior.

## Changes
- **New File**: `/org.sterl.llmpeon.test/src/org/sterl/llmpeon/parts/config/LlmPreferenceInitializerTest.java`
  - Test: `testFirstLaunchDirectoryResolutionWithClaudeExists()` — mock `.claude/skills` exists → returns that path
  - Test: `testFirstLaunchDirectoryResolutionFallbackToLlmpeon()` — no `.claude` → returns `~/.llmpeon/skills`
- **New File**: `/llmpeon-core/src/test/java/org/sterl/llmpeon/ai/LlmConfigPerAgentModelTest.java`
  - Test: `testSearchModelFallbackToDefault()` — empty searchModel returns default model
  - Test: `testSearchModelOverridesDefault()` — non-empty searchModel used directly
  - Same for planModel and devModel

## Verification
- All tests pass with `mvn clean verify`

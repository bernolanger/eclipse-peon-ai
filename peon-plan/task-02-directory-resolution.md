# Task 02: First-Launch Directory Resolution

## Description
Implement one-time directory resolution logic in `LlmPreferenceInitializer.initializeDefaultPreferences()` that checks `.claude/skills` first, then falls back to `~/.llmpeon/skills` (same for commands).

## Changes
- **File**: `/org.sterl.llmpeon/src/org/sterl/llmpeon/parts/config/LlmPreferenceInitializer.java`
- In `initializeDefaultPreferences()`, replace hardcoded `.claude` paths with resolution logic:
  ```java
  private static String resolveFirstLaunchDir(String claudePath, String fallbackName) {
      Path claude = Path.of(claudePath);
      if (Files.isDirectory(claude)) return claude.toString();
      Path llmpeon = Path.of(System.getProperty("user.home"), PeonConstants.LLMPEON_DIR_NAME, fallbackName);
      Files.createDirectories(llmpeon); // ensure it exists
      return llmpeon.toString();
  }
  ```
- Use this for both `PREF_SKILL_DIRECTORY` and `PREF_COMMAND_DIRECTORY`

## Verification
- Build succeeds
- Manual test: fresh Eclipse instance should get `.claude/skills` if it exists, otherwise `~/.llmpeon/skills`

# Task 01: Add Preference Constants

## Description
Add three new preference keys for per-agent model selection and one constant for the llmpeon directory name to `PeonConstants.java`.

## Changes
- **File**: `/org.sterl.llmpeon/src/org/sterl/llmpeon/parts/PeonConstants.java`
- Add:
  ```java
  String PREF_SEARCH_MODEL = "llm.searchModel";
  String PREF_PLAN_MODEL   = "llm.planModel";
  String PREF_DEV_MODEL    = "llm.devModel";
  String LLMPEON_DIR_NAME  = "llmpeon";
  ```

## Verification
- ✅ Build succeeds - no new errors or warnings introduced
- ✅ No existing code references these keys yet (expected)

## Status: ✅ Done

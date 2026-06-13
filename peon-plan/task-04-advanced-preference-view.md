# Task 04: Create AiAdvancedPreferenceView

## Description
Create a new preference page "AI Peon Advanced" that hosts per-agent model fields, temperatures, debug mode, and query/header parameters.

## Changes
- **New File**: `/org.sterl.llmpeon/src/org/sterl/llmpeon/parts/config/AiAdvancedPreferenceView.java`
  - Extend `FieldEditorPreferencePage` like existing pages
  - Fields:
    - Search Model, Plan Model, Dev Model (StringFieldEditor with tooltip explaining fallback)
    - Plan Temperature, Dev Temperature (DoubleSliderFieldEditor — move from simple page)
    - Debug Mode (BooleanFieldEditor)
    - Query Params, Header Params (StringFieldEditor — move from simple page)
  - Set category to `org.sterl.llmpeon.prefs.AiPeon`
- **File**: `/org.sterl.llmpeon/src/org/sterl/llmpeon/parts/config/AiConfigPreferenceView.java`
  - Remove temperature, debug mode, query/header param fields (moved to advanced page)

## Verification
- Build succeeds
- UI test: open Window > Preferences > AI Peon — "AI Peon Advanced" appears as sub-page with all fields

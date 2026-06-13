# Task 05: Register Advanced Preference Page in plugin.xml

## Description
Register the new `AiAdvancedPreferenceView` preference page in the Eclipse extension point.

## Changes
- **File**: `/org.sterl.llmpeon/plugin.xml`
  - Add new `<page>` entry under `org.eclipse.ui.preferencePages`:
    ```xml
    <page
          id="org.sterl.llmpeon.prefs.AiAdvanced"
          name="AI Peon Advanced"
          category="org.sterl.llmpeon.prefs.AiPeon"
          class="org.sterl.llmpeon.parts.config.AiAdvancedPreferenceView">
    </page>
    ```

## Verification
- Build succeeds
- Eclipse restart: new preference page appears under AI Peon category

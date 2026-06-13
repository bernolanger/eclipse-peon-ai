# Task 07: Update Design Documentation

## Description
Document the advanced configuration design and model fallback behavior in the project documentation.

## Changes
- **New File**: `/doc/docs/design/advanced-configuration.md`
  - Document preference page split rationale
  - Document per-agent model fallback chain
  - Document first-launch directory resolution logic
- **File**: `/doc/.vitepress/config.ts`
  - Add sidebar entry for `design/advanced-configuration` under the Design section

## Verification
- Docs build succeeds with `npm run build` in doc/

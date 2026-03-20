# Recommendation Page Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the static recommendation page with a data-driven experience that supports personalized and cold-start states without duplicating the homepage trending section.

**Architecture:** Keep backend changes minimal by composing the page from existing APIs: personalized recommendations, saved recipes, preferences, cuisines, flavours, and filtered recipe searches. Move page-state decisions into a small frontend utility module so we can test the state matrix and tag filtering logic independently before wiring the Vue view.

**Tech Stack:** Vue 3, Vue Router, Element Plus, existing axios API modules, node:assert-based utility tests, Vite build verification

---

## Chunk 1: Recommendation State Logic

### Task 1: Define and test recommendation state helpers

**Files:**
- Create: `Recipe_FrontEnd/src/utils/recommendationPageState.js`
- Create: `Recipe_FrontEnd/src/utils/recommendationPageState.test.js`

- [ ] **Step 1: Write the failing test**

Cover:
- cold-start sections (`right-now`, `cta`, `explore`)
- personalized sections when saved recipe exists
- partial personalization when preferences exist but no saved recipe
- explore tag filtering from cuisine/flavour API results

- [ ] **Step 2: Run test to verify it fails**

Run: `node src/utils/recommendationPageState.test.js`
Expected: FAIL because helper module does not exist yet

- [ ] **Step 3: Write minimal implementation**

Add helpers that:
- detect meaningful preferences
- build the three section descriptors
- filter configured explore-tag candidates against available cuisines/flavours

- [ ] **Step 4: Run test to verify it passes**

Run: `node src/utils/recommendationPageState.test.js`
Expected: PASS

## Chunk 2: Recommendation Page UI

### Task 2: Replace static page content with async data wiring

**Files:**
- Modify: `Recipe_FrontEnd/src/views/recommend/index.vue`
- Modify: `Recipe_FrontEnd/src/api/user.js`
- Modify: `Recipe_FrontEnd/src/api/recipeCard.js`
- Reuse: `Recipe_FrontEnd/src/component/recipeSwitch.vue`

- [ ] **Step 1: Wire the page loader**

Load:
- preferences
- first saved recipe
- cuisines/flavours for cold-start tags
- right-now recommendations

- [ ] **Step 2: Render section modes**

Support:
- recipe-card section
- CTA panel
- explore tags with preview cards

- [ ] **Step 3: Keep existing visual language**

Preserve the current recommendation page styling where it still fits and add only the styles needed for the CTA/explore blocks.

- [ ] **Step 4: Verify manually in code**

Check that the section titles and subtitles match the agreed copy for:
- full personalization
- partial personalization
- cold-start

## Chunk 3: Verification

### Task 3: Run project verification

**Files:**
- Verify only

- [ ] **Step 1: Run utility tests**

Run: `node src/utils/recommendationPageState.test.js`
Expected: PASS

- [ ] **Step 2: Run frontend build**

Run: `npm run build`
Expected: PASS

- [ ] **Step 3: Review results**

Confirm:
- no static mock recommendation arrays remain in the view
- cold-start mode does not reference trending
- recommendation page still builds successfully

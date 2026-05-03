# Unified Recipe Search Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert recipe discovery to a single keyword search over title, description, and ingredients while removing ingredient-tag filtering from the recipes page.

**Architecture:** Keep the current `/api/recipes` endpoint and `RecipeCardDto` request model mostly intact, but reinterpret the existing keyword field as a unified free-text query. Implement the search change in `RecipeCardServiceImpl` with a multi-field Elasticsearch query, then simplify the recipes page UI to use one search input plus existing structured filters.

**Tech Stack:** Spring Boot, Spring Data Elasticsearch, JUnit 5, Mockito, Vue 3, Element Plus

---

## Chunk 1: Backend Unified Keyword Search

### Task 1: Add a failing backend search test

**Files:**
- Modify: `Recipe_BackEnd/src/test/java/org/zhan/recipe_backend/service/impl/RecipeCardServiceImplTest.java`
- Modify: `Recipe_BackEnd/src/main/java/org/zhan/recipe_backend/service/impl/RecipeCardServiceImpl.java`

- [ ] **Step 1: Write the failing test**

Add a test that calls `getRecipeCards` with a keyword and asserts the generated Elasticsearch query targets `title`, `description`, and `ingredients`.

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=RecipeCardServiceImplTest test`

Expected: FAIL because the current query only matches `title`.

- [ ] **Step 3: Write minimal implementation**

Update `RecipeCardServiceImpl#getRecipeCards` so the keyword produces a multi-field search across `title`, `description`, and `ingredients`.

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=RecipeCardServiceImplTest test`

Expected: PASS

## Chunk 2: Frontend Search UI Simplification

### Task 2: Remove ingredient-tag search UI and keep one keyword box

**Files:**
- Modify: `Recipe_FrontEnd/src/views/recipes/index.vue`

- [ ] **Step 1: Update search state shape**

Remove `ingredientTags` from the recipes page state and session restore logic.

- [ ] **Step 2: Remove ingredient tag UI**

Delete the ingredient-tag input row and related handlers so the page exposes one keyword input plus existing filters.

- [ ] **Step 3: Clarify copy**

Adjust the keyword placeholder text so it communicates searching dishes, descriptions, and ingredients.

- [ ] **Step 4: Build-check mentally against current data flow**

Confirm the page still forwards the same search object shape expected by `getRecipeCardApi`, minus the ingredient tag field.

## Chunk 3: Verification

### Task 3: Run focused verification

**Files:**
- Modify: `Recipe_BackEnd/src/test/java/org/zhan/recipe_backend/service/impl/RecipeCardServiceImplTest.java`
- Modify: `Recipe_BackEnd/src/main/java/org/zhan/recipe_backend/service/impl/RecipeCardServiceImpl.java`
- Modify: `Recipe_FrontEnd/src/views/recipes/index.vue`

- [ ] **Step 1: Run focused backend tests**

Run: `./mvnw -q -Dtest=RecipeCardServiceImplTest test`

Expected: PASS

- [ ] **Step 2: Summarize any remaining frontend-only risks**

Document that the frontend change is UI-only unless a local build/test run is also performed.

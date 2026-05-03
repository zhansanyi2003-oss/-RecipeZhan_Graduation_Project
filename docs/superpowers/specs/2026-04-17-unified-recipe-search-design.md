# Unified Recipe Search Design

**Date:** 2026-04-17

**Goal:** Turn recipe search into one free-text keyword search that matches recipe title, description, and ingredient names, while keeping `flavour` and `cuisine` as the only standardized cached tag lists.

## Decisions

### Search model

- The recipe list page uses one keyword input for general search.
- A keyword should match:
  - `title`
  - `description`
  - `ingredients`
- Ingredient search is text-based through Elasticsearch.
- Ingredient data is not normalized, not cached in Redis, and not exposed as a standard selectable tag list for recipe search.

### Tag model

- `flavour` and `cuisine` remain standardized labels.
- `flavour` and `cuisine` should be served from Redis-backed cached lists.
- `dietType` and `course` remain fixed values and are outside this change.

### UI model

- The recipes page removes the dedicated ingredient tag input area.
- The existing keyword search box becomes the single free-text entry point.
- The home page can continue passing `keyword` in the route query.

## Backend impact

- Update recipe search query construction in `RecipeCardServiceImpl`.
- Replace title-only keyword search with a multi-field Elasticsearch query over `title`, `description`, and `ingredients`.
- Keep existing filters for `flavours`, `cuisines`, `dietTypes`, `courses`, and time.
- Keep ingredient detail storage in recipe data unchanged.

## Frontend impact

- Simplify recipe search page state so it no longer carries `ingredientTags`.
- Remove ingredient-tag input UI from the recipes page.
- Keep keyword persistence through route query and session state.

## Verification

- Add a backend test proving keyword search targets `title`, `description`, and `ingredients`.
- Verify recipe list page still loads, searches by keyword, and keeps flavour/cuisine filters working.

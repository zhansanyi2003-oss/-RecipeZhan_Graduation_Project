package org.zhan.recipe_backend.service;

public interface RecipeSearchSyncService {

    void enqueueUpsert(Long recipeId);

    void enqueueDelete(Long recipeId);

    void processDueEvents();
}

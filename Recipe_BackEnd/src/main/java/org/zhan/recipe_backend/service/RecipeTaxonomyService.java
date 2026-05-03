package org.zhan.recipe_backend.service;

import org.zhan.recipe_backend.entity.Cuisine;
import org.zhan.recipe_backend.entity.Flavour;

import java.util.List;

public interface RecipeTaxonomyService {

    List<String> getFlavours();

    List<String> getCuisines();

    Flavour getOrCreateFlavour(String rawName);

    Cuisine getOrCreateCuisine(String rawName);
}

package org.zhan.recipe_backend.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import org.zhan.recipe_backend.document.RecipeDoc;

@Repository
public interface RecipeEsRepository extends ElasticsearchRepository<RecipeDoc, Long> {

}
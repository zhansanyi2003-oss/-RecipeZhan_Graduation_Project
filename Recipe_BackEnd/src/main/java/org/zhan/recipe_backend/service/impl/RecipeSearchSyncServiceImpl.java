package org.zhan.recipe_backend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhan.recipe_backend.common.RecipeSearchSyncOperation;
import org.zhan.recipe_backend.common.RecipeSearchSyncStatus;
import org.zhan.recipe_backend.document.RecipeDoc;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.RecipeSearchSyncEvent;
import org.zhan.recipe_backend.mapper.RecipeMapper;
import org.zhan.recipe_backend.repository.RecipeEsRepository;
import org.zhan.recipe_backend.repository.RecipeRepository;
import org.zhan.recipe_backend.repository.RecipeSearchSyncEventRepository;
import org.zhan.recipe_backend.service.RecipeSearchSyncService;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class RecipeSearchSyncServiceImpl implements RecipeSearchSyncService {

    private static final Logger log = LoggerFactory.getLogger(RecipeSearchSyncServiceImpl.class);
    private static final int BATCH_SIZE = 20;
    private static final int MAX_ATTEMPTS = 5;

    @Autowired
    private RecipeSearchSyncEventRepository eventRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private RecipeEsRepository recipeEsRepository;

    @Override
    @Transactional
    public void enqueueUpsert(Long recipeId) {
        enqueue(recipeId, RecipeSearchSyncOperation.UPSERT);
    }

    @Override
    @Transactional
    public void enqueueDelete(Long recipeId) {
        enqueue(recipeId, RecipeSearchSyncOperation.DELETE);
    }

    @Override
    @Scheduled(fixedDelayString = "${recipe.search-sync.fixed-delay-ms:30000}")
    @Transactional
    public void processDueEvents() {
        eventRepository.findDueEvents(LocalDateTime.now(), PageRequest.of(0, BATCH_SIZE))
                .forEach(this::processEvent);
    }

    private void enqueue(Long recipeId, RecipeSearchSyncOperation operation) {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId is required for search sync event");
        }

        eventRepository.save(RecipeSearchSyncEvent.builder()
                .recipeId(recipeId)
                .operation(operation)
                .status(RecipeSearchSyncStatus.PENDING)
                .attempts(0)
                .nextRetryAt(LocalDateTime.now())
                .build());
    }

    private void processEvent(RecipeSearchSyncEvent event) {
        try {
            event.setAttempts(event.getAttempts() + 1);
            if (event.getOperation() == RecipeSearchSyncOperation.DELETE) {
                recipeEsRepository.deleteById(event.getRecipeId());
            } else {
                Recipe recipe = recipeRepository.findById(event.getRecipeId())
                        .orElseThrow(() -> new IllegalStateException("Recipe not found: " + event.getRecipeId()));
                RecipeDoc recipeDoc = recipeMapper.toRecipeDoc(recipe);
                recipeEsRepository.save(recipeDoc);
            }

            event.setStatus(RecipeSearchSyncStatus.DONE);
            event.setLastError(null);
        } catch (Exception exception) {
            scheduleRetry(event, exception);
        }
    }

    private void scheduleRetry(RecipeSearchSyncEvent event, Exception exception) {
        String message = exception.getMessage() == null
                ? exception.getClass().getSimpleName()
                : exception.getMessage();
        event.setLastError(message);

        if (event.getAttempts() >= MAX_ATTEMPTS) {
            event.setStatus(RecipeSearchSyncStatus.FAILED);
            log.error("Recipe search sync event {} failed permanently for recipe {}",
                    event.getId(), event.getRecipeId(), exception);
            return;
        }

        event.setStatus(RecipeSearchSyncStatus.PENDING);
        event.setNextRetryAt(LocalDateTime.now().plus(retryDelay(event.getAttempts())));
        log.warn("Recipe search sync event {} failed for recipe {}, retry attempt {} scheduled",
                event.getId(), event.getRecipeId(), event.getAttempts(), exception);
    }

    private Duration retryDelay(int attempts) {
        return Duration.ofMinutes(Math.min(attempts * attempts, 30));
    }
}

package org.zhan.recipe_backend.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.zhan.recipe_backend.common.RecipeSearchSyncOperation;
import org.zhan.recipe_backend.common.RecipeSearchSyncStatus;
import org.zhan.recipe_backend.document.RecipeDoc;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.RecipeSearchSyncEvent;
import org.zhan.recipe_backend.mapper.RecipeMapper;
import org.zhan.recipe_backend.repository.RecipeEsRepository;
import org.zhan.recipe_backend.repository.RecipeRepository;
import org.zhan.recipe_backend.repository.RecipeSearchSyncEventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecipeSearchSyncServiceImplTest {

    @Test
    void enqueueUpsert_createsPendingOutboxEvent() {
        RecipeSearchSyncServiceImpl service = new RecipeSearchSyncServiceImpl();
        RecipeSearchSyncEventRepository eventRepository = mock(RecipeSearchSyncEventRepository.class);
        ReflectionTestUtils.setField(service, "eventRepository", eventRepository);

        service.enqueueUpsert(42L);

        ArgumentCaptor<RecipeSearchSyncEvent> eventCaptor = ArgumentCaptor.forClass(RecipeSearchSyncEvent.class);
        verify(eventRepository).save(eventCaptor.capture());

        RecipeSearchSyncEvent event = eventCaptor.getValue();
        assertEquals(42L, event.getRecipeId());
        assertEquals(RecipeSearchSyncOperation.UPSERT, event.getOperation());
        assertEquals(RecipeSearchSyncStatus.PENDING, event.getStatus());
        assertEquals(0, event.getAttempts());
        assertNotNull(event.getNextRetryAt());
    }

    @Test
    void processDueEvents_marksEventDoneAfterSuccessfulUpsert() {
        RecipeSearchSyncServiceImpl service = new RecipeSearchSyncServiceImpl();
        RecipeSearchSyncEventRepository eventRepository = mock(RecipeSearchSyncEventRepository.class);
        RecipeRepository recipeRepository = mock(RecipeRepository.class);
        RecipeMapper recipeMapper = mock(RecipeMapper.class);
        RecipeEsRepository recipeEsRepository = mock(RecipeEsRepository.class);

        RecipeSearchSyncEvent event = RecipeSearchSyncEvent.builder()
                .id(1L)
                .recipeId(42L)
                .operation(RecipeSearchSyncOperation.UPSERT)
                .status(RecipeSearchSyncStatus.PENDING)
                .attempts(0)
                .nextRetryAt(LocalDateTime.now().minusSeconds(1))
                .build();
        Recipe recipe = Recipe.builder().id(42L).title("Indexed recipe").build();
        RecipeDoc recipeDoc = RecipeDoc.builder().id(42L).title("Indexed recipe").build();

        when(eventRepository.findDueEvents(any(), any())).thenReturn(List.of(event));
        when(recipeRepository.findById(42L)).thenReturn(Optional.of(recipe));
        when(recipeMapper.toRecipeDoc(recipe)).thenReturn(recipeDoc);

        ReflectionTestUtils.setField(service, "eventRepository", eventRepository);
        ReflectionTestUtils.setField(service, "recipeRepository", recipeRepository);
        ReflectionTestUtils.setField(service, "recipeMapper", recipeMapper);
        ReflectionTestUtils.setField(service, "recipeEsRepository", recipeEsRepository);

        service.processDueEvents();

        verify(recipeEsRepository).save(recipeDoc);
        assertEquals(RecipeSearchSyncStatus.DONE, event.getStatus());
        assertEquals(1, event.getAttempts());
    }

    @Test
    void processDueEvents_keepsEventPendingWithRetryTimeAfterFailure() {
        RecipeSearchSyncServiceImpl service = new RecipeSearchSyncServiceImpl();
        RecipeSearchSyncEventRepository eventRepository = mock(RecipeSearchSyncEventRepository.class);
        RecipeRepository recipeRepository = mock(RecipeRepository.class);
        RecipeMapper recipeMapper = mock(RecipeMapper.class);
        RecipeEsRepository recipeEsRepository = mock(RecipeEsRepository.class);

        LocalDateTime beforeRun = LocalDateTime.now();
        RecipeSearchSyncEvent event = RecipeSearchSyncEvent.builder()
                .id(1L)
                .recipeId(42L)
                .operation(RecipeSearchSyncOperation.UPSERT)
                .status(RecipeSearchSyncStatus.PENDING)
                .attempts(0)
                .nextRetryAt(beforeRun.minusSeconds(1))
                .build();
        Recipe recipe = Recipe.builder().id(42L).title("Indexed recipe").build();
        RecipeDoc recipeDoc = RecipeDoc.builder().id(42L).title("Indexed recipe").build();

        when(eventRepository.findDueEvents(any(), any())).thenReturn(List.of(event));
        when(recipeRepository.findById(42L)).thenReturn(Optional.of(recipe));
        when(recipeMapper.toRecipeDoc(recipe)).thenReturn(recipeDoc);
        doThrow(new RuntimeException("es unavailable")).when(recipeEsRepository).save(recipeDoc);

        ReflectionTestUtils.setField(service, "eventRepository", eventRepository);
        ReflectionTestUtils.setField(service, "recipeRepository", recipeRepository);
        ReflectionTestUtils.setField(service, "recipeMapper", recipeMapper);
        ReflectionTestUtils.setField(service, "recipeEsRepository", recipeEsRepository);

        Logger logger = (Logger) LoggerFactory.getLogger(RecipeSearchSyncServiceImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        boolean previousAdditive = logger.isAdditive();
        appender.start();
        logger.setAdditive(false);
        logger.addAppender(appender);

        try {
            service.processDueEvents();
        } finally {
            logger.detachAppender(appender);
            logger.setAdditive(previousAdditive);
        }

        assertEquals(RecipeSearchSyncStatus.PENDING, event.getStatus());
        assertEquals(1, event.getAttempts());
        assertEquals("es unavailable", event.getLastError());
        assertTrue(event.getNextRetryAt().isAfter(beforeRun));
    }
}

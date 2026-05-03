package org.zhan.recipe_backend.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.zhan.recipe_backend.repository.UserSavedRepository;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserServiceImplTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void toggleSaveRecipe_savesWithCurrentUserIdBeforeRecipeId() {
        UserServiceImpl service = new UserServiceImpl();
        UserSavedRepository userSavedRepository = mock(UserSavedRepository.class);
        ReflectionTestUtils.setField(service, "userSavedRepository", userSavedRepository);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(7L, null, List.of())
        );

        service.toggleSaveRecipe(99L, true);

        verify(userSavedRepository).saveRecipe(7L, 99L);
    }
}

package org.zhan.recipe_backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zhan.recipe_backend.service.UploadFileService;
import org.zhan.recipe_backend.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private UploadFileService uploadFileService;
    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController();
        uploadFileService = mock(UploadFileService.class);
        userService = mock(UserService.class);
        ReflectionTestUtils.setField(controller, "uploadFileService", uploadFileService);
        ReflectionTestUtils.setField(controller, "userService", userService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void updateAvatar_uploadsThroughAvatarStoragePathAndStoresReturnedUrl() throws Exception {
        when(uploadFileService.saveAvatar(any())).thenReturn("https://example.blob.core.windows.net/recipe-images/avatars/avatar.png");

        mockMvc.perform(multipart("/api/users/avatar")
                        .file("file", "avatar".getBytes()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("https://example.blob.core.windows.net/recipe-images/avatars/avatar.png"));

        verify(uploadFileService).saveAvatar(any());
        verify(userService).updateAvatar("https://example.blob.core.windows.net/recipe-images/avatars/avatar.png");
    }
}

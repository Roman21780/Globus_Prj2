package com.example.prj2.controller;

import com.example.prj2.entity.User;
import com.example.prj2.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .age(25)
                .build();
    }

    @Test
    @DisplayName("Should create user and return 201")
    void testCreateUser_Success() throws Exception {
        User newUser = User.builder()
                .email("newuser@example.com")
                .name("New User")
                .age(28)
                .build();

        User createdUser = User.builder()
                .id(1L)
                .email("newuser@example.com")
                .name("New User")
                .age(28)
                .build();

        when(userService.create(ArgumentMatchers.any(User.class))).thenReturn(createdUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.name").value("New User"))
                .andExpect(jsonPath("$.age").value(28));

        verify(userService, times(1)).create(ArgumentMatchers.any(User.class));
    }

    @Test
    @DisplayName("Should return 400 when email is invalid")
    void testCreateUser_InvalidEmail() throws Exception {
        User invalidUser = User.builder()
                .email("invalid-email")
                .name("User")
                .age(25)
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(ArgumentMatchers.any(User.class));
    }

    @Test
    @DisplayName("Should return 400 when name is empty")
    void testCreateUser_EmptyName() throws Exception {
        User invalidUser = User.builder()
                .email("test@example.com")
                .name("")
                .age(25)
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(ArgumentMatchers.any(User.class));
    }

    @Test
    @DisplayName("Should get all users and return 200")
    void testGetAllUsers_Success() throws Exception {
        User user2 = User.builder()
                .id(2L)
                .email("user2@example.com")
                .name("User 2")
                .age(30)
                .build();

        when(userService.getAll()).thenReturn(List.of(testUser, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].email").value("user2@example.com"));

        verify(userService, times(1)).getAll();
    }

    @Test
    @DisplayName("Should return empty list when no users found")
    void testGetAllUsers_Empty() throws Exception {
        when(userService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService, times(1)).getAll();
    }

    @Test
    @DisplayName("Should get user by id and return 200")
    void testGetUserById_Success() throws Exception {
        when(userService.getById(1L)).thenReturn(testUser);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.age").value(25));

        verify(userService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void testGetUserById_NotFound() throws Exception {
        when(userService.getById(999L))
                .thenThrow(new RuntimeException("User not found: 999"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).getById(999L);
    }

    @Test
    @DisplayName("Should update user and return 200")
    void testUpdateUser_Success() throws Exception {
        User updatedUser = User.builder()
                .id(1L)
                .email("updated@example.com")
                .name("Updated User")
                .age(26)
                .build();

        User updateRequest = User.builder()
                .email("updated@example.com")
                .name("Updated User")
                .age(26)
                .build();

        when(userService.update(eq(1L), ArgumentMatchers.any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.age").value(26));

        verify(userService, times(1)).update(eq(1L), ArgumentMatchers.any(User.class));
    }

    @Test
    @DisplayName("Should return 400 when update with invalid data")
    void testUpdateUser_InvalidData() throws Exception {
        User invalidUpdate = User.builder()
                .email("invalid")
                .name("")
                .age(20)
                .build();

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).update(anyLong(), ArgumentMatchers.any(User.class));
    }

    @Test
    @DisplayName("Should delete user and return 204")
    void testDeleteUser_Success() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Should handle delete of non-existent user")
    void testDeleteUser_NotFound() throws Exception {
        doThrow(new RuntimeException("User not found: 999"))
                .when(userService).delete(999L);

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).delete(999L);
    }

    @Test
    @DisplayName("Should validate path variable type")
    void testGetUserById_InvalidPathVariable() throws Exception {
        mockMvc.perform(get("/users/invalid"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).getById(anyLong());
    }

    @Test
    @DisplayName("Should return correct content type")
    void testGetAllUsers_ContentType() throws Exception {
        when(userService.getAll()).thenReturn(List.of(testUser));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(userService, times(1)).getAll();
    }

    @Test
    @DisplayName("Should accept POST with correct content type")
    void testCreateUser_ContentType() throws Exception {
        User newUser = User.builder()
                .email("test@example.com")
                .name("Test")
                .age(25)
                .build();

        when(userService.create(ArgumentMatchers.any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(userService, times(1)).create(ArgumentMatchers.any(User.class));
    }

    @Test
    @DisplayName("PUT /users/{id} возвращает 404 при обновлении несуществующего пользователя")
    void testUpdateUser_NotFound() throws Exception {
        User updateRequest = User.builder()
                .email("nonexistent@example.com")
                .name("Nonexistent")
                .age(30)
                .build();

        when(userService.update(eq(999L), ArgumentMatchers.any(User.class)))
                .thenThrow(new com.example.prj2.exception.ResourceNotFoundException("User not found: 999"));

        mockMvc.perform(put("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).update(eq(999L), ArgumentMatchers.any(User.class));
    }

}

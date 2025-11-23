package com.example.prj2.service;

import com.example.prj2.entity.User;
import com.example.prj2.exception.ResourceNotFoundException;
import com.example.prj2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
public class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@mail.com")
                .name("Test User")
                .age(25)
                .build();
    }

    // ===== CREATE Tests =====

    @Test
    @DisplayName("Should create user successfully")
    void testCreate_Success() {
        User newUser = User.builder()
                .email("newuser@mail.com")
                .name("New User")
                .age(30)
                .build();

        when(repository.findByEmail(newUser.getEmail())).thenReturn(Optional.empty());
        when(repository.save(newUser)).thenReturn(testUser);

        User result = service.create(newUser);

        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        verify(repository, times(1)).findByEmail(newUser.getEmail());
        verify(repository, times(1)).save(newUser);
    }

    @Test
    @DisplayName("Should throw RuntimeException when email already exists")
    void testCreate_EmailAlreadyExists() {
        User existingUser = User.builder()
                .email("test@mail.com")
                .name("Test")
                .age(25)
                .build();

        when(repository.findByEmail(existingUser.getEmail())).thenReturn(Optional.of(testUser));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> service.create(existingUser));

        assertThat(thrown.getMessage()).isEqualTo("Email already exists");
        verify(repository, times(1)).findByEmail(existingUser.getEmail());
        verify(repository, never()).save(any());
    }

    // ===== GET BY ID Tests =====

    @Test
    @DisplayName("Should get user by id successfully")
    void testGetById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = service.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@mail.com");
        assertThat(result.getName()).isEqualTo("Test User");
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testGetById_NotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class,
                () -> service.getById(999L));

        assertThat(thrown.getMessage()).contains("User not found: 999");
        verify(repository, times(1)).findById(999L);
    }

    // ===== GET ALL Tests =====

    @Test
    @DisplayName("Should get all users")
    void testGetAll_Success() {
        User user2 = User.builder()
                .id(2L)
                .email("user2@mail.com")
                .name("User Two")
                .age(30)
                .build();

        when(repository.findAll()).thenReturn(List.of(testUser, user2));

        List<User> result = service.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no users found")
    void testGetAll_Empty() {
        when(repository.findAll()).thenReturn(List.of());

        List<User> result = service.getAll();

        assertThat(result).isEmpty();
        verify(repository, times(1)).findAll();
    }

    // ===== UPDATE Tests =====

    @Test
    @DisplayName("Should update user successfully")
    void testUpdate_Success() {
        User updateRequest = User.builder()
                .email("updated@mail.com")
                .name("Updated User")
                .age(35)
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .email("updated@mail.com")
                .name("Updated User")
                .age(35)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        when(repository.save(any(User.class))).thenReturn(updatedUser);

        User result = service.update(1L, updateRequest);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("updated@mail.com");
        assertThat(result.getName()).isEqualTo("Updated User");
        assertThat(result.getAge()).isEqualTo(35);
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent user")
    void testUpdate_NotFound() {
        User updateRequest = User.builder()
                .email("updated@mail.com")
                .name("Updated User")
                .age(35)
                .build();

        when(repository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class,
                () -> service.update(999L, updateRequest));

        assertThat(thrown.getMessage()).contains("User not found: 999");
        verify(repository, times(1)).findById(999L);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should update only specific fields")
    void testUpdate_PartialUpdate() {
        User updateRequest = User.builder()
                .email("newemail@mail.com")
                .name("New Name")
                .age(40)
                .build();

        User expectedResult = User.builder()
                .id(1L)
                .email("newemail@mail.com")
                .name("New Name")
                .age(40)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        when(repository.save(any(User.class))).thenReturn(expectedResult);

        User result = service.update(1L, updateRequest);

        // Проверяем, что все поля обновлены
        assertThat(result.getEmail()).isEqualTo("newemail@mail.com");
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getAge()).isEqualTo(40);
        verify(repository, times(1)).save(any(User.class));
    }

    // ===== DELETE Tests =====

    @Test
    @DisplayName("Should delete user successfully")
    void testDelete_Success() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository, times(1)).existsById(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent user")
    void testDelete_NotFound() {
        when(repository.existsById(999L)).thenReturn(false);

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class,
                () -> service.delete(999L));

        assertThat(thrown.getMessage()).contains("User not found: 999");
        verify(repository, times(1)).existsById(999L);
        verify(repository, never()).deleteById(any());
    }

    // ===== Edge Cases =====

    @Test
    @DisplayName("Should handle create with minimum valid age")
    void testCreate_MinimumAge() {
        User minAgeUser = User.builder()
                .email("minAge@mail.com")
                .name("Min Age User")
                .age(18)  // минимальный возраст из валидации
                .build();

        when(repository.findByEmail(minAgeUser.getEmail())).thenReturn(Optional.empty());
        when(repository.save(minAgeUser)).thenReturn(minAgeUser);

        User result = service.create(minAgeUser);

        assertThat(result.getAge()).isEqualTo(18);
        verify(repository, times(1)).save(minAgeUser);
    }

    @Test
    @DisplayName("Should handle create with maximum valid age")
    void testCreate_MaximumAge() {
        User maxAgeUser = User.builder()
                .email("maxAge@mail.com")
                .name("Max Age User")
                .age(150)  // максимальный возраст из валидации
                .build();

        when(repository.findByEmail(maxAgeUser.getEmail())).thenReturn(Optional.empty());
        when(repository.save(maxAgeUser)).thenReturn(maxAgeUser);

        User result = service.create(maxAgeUser);

        assertThat(result.getAge()).isEqualTo(150);
        verify(repository, times(1)).save(maxAgeUser);
    }

    @Test
    @DisplayName("Should verify repository interactions in correct order")
    void testCreate_VerifyInteractionOrder() {
        User newUser = User.builder()
                .email("order@mail.com")
                .name("Order Test")
                .age(25)
                .build();

        when(repository.findByEmail(newUser.getEmail())).thenReturn(Optional.empty());
        when(repository.save(newUser)).thenReturn(newUser);

        service.create(newUser);

        // Проверяем, что сначала проверяется email, потом сохраняется
        InOrder inOrder = inOrder(repository);
        inOrder.verify(repository).findByEmail(newUser.getEmail());
        inOrder.verify(repository).save(newUser);
    }

    @Test
    @DisplayName("Should not modify user fields unexpectedly")
    void testUpdate_FieldModification() {
        User updateRequest = User.builder()
                .email("test2@mail.com")
                .name("Test Updated")
                .age(26)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.update(1L, updateRequest);

        // ID не должен измениться
        assertThat(result.getId()).isEqualTo(1L);
        // Другие поля должны обновиться
        assertThat(result.getEmail()).isEqualTo("test2@mail.com");
        assertThat(result.getName()).isEqualTo("Test Updated");
        assertThat(result.getAge()).isEqualTo(26);
    }
}

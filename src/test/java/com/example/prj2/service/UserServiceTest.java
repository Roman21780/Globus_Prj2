package com.example.prj2.service;

import com.example.prj2.entity.User;
import com.example.prj2.exception.ResourceNotFoundException;
import com.example.prj2.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;

    @Test
    void testCreate() {
        User user = User.builder().email("test@mail.com").name("Test").age(25).build();
        when(repository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(repository.save(user)).thenReturn(user);

        User result = service.create(user);

        assertEquals("test@mail.com", result.getEmail());
        verify(repository).save(user);
    }

    @Test
    void testGetById_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void testGetById_success() {
        User user = User.builder().id(1L).email("test@mail.com").build();
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        User result = service.getById(1L);

        assertEquals(1L, result.getId());
    }
}

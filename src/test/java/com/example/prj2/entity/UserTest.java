package com.example.prj2.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    void getId() {
        User user = User.builder().id(1L).build();
        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    void getEmail() {
        User user = User.builder().email("test@example.com").build();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getName() {
        User user = User.builder().name("John Doe").build();
        assertThat(user.getName()).isEqualTo("John Doe");
    }

    @Test
    void getAge() {
        User user = User.builder().age(30).build();
        assertThat(user.getAge()).isEqualTo(30);
    }

    @Test
    void setId() {
        User user = new User();
        user.setId(10L);
        assertThat(user.getId()).isEqualTo(10L);
    }

    @Test
    void setEmail() {
        User user = new User();
        user.setEmail("new@example.com");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void setName() {
        User user = new User();
        user.setName("Alice");
        assertThat(user.getName()).isEqualTo("Alice");
    }

    @Test
    void setAge() {
        User user = new User();
        user.setAge(25);
        assertThat(user.getAge()).isEqualTo(25);
    }

    @Test
    void testEquals() {
        User user1 = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("John")
                .age(20)
                .build();

        User user2 = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("John")
                .age(20)
                .build();

        User user3 = User.builder()
                .id(2L)
                .email("other@example.com")
                .name("Alice")
                .age(30)
                .build();

        assertThat(user1).isEqualTo(user2);
        assertThat(user1).isNotEqualTo(user3);
    }

    @Test
    void canEqual() {
        User user = User.builder().id(1L).build();
        assertThat(user.canEqual(new User())).isTrue();
        assertThat(user.canEqual("some string")).isFalse();
    }

    @Test
    void testHashCode() {
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(1L).build();
        User user3 = User.builder().id(3L).build();

        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        assertThat(user1.hashCode()).isNotEqualTo(user3.hashCode());
    }

    @Test
    void testToString() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("John Doe")
                .age(40)
                .build();

        String expectedStart = "User.UserBuilder(id=1, email=test@example.com";
        assertThat(user.toString()).startsWith("User(")
                .contains("email=test@example.com")
                .contains("name=John Doe")
                .contains("age=40");
    }

    @Test
    void builder() {
        User user = User.builder()
                .id(5L)
                .email("builder@example.com")
                .name("Builder Name")
                .age(35)
                .build();

        assertThat(user.getId()).isEqualTo(5L);
        assertThat(user.getEmail()).isEqualTo("builder@example.com");
        assertThat(user.getName()).isEqualTo("Builder Name");
        assertThat(user.getAge()).isEqualTo(35);
    }
}

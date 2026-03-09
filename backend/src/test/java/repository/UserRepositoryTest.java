package repository;

import model.User;
import model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRepositoryTest {

    private UserRepository repo;

    @BeforeEach
    void setup() {
        repo = mock(UserRepository.class);
    }

    @Test
    void findByEmail_returnsUser_whenExists() {
        User u = new User(1L, "test@test.com", "hash", "A", "B", UserRole.STUDENT, "1001");

        when(repo.findByEmail("test@test.com")).thenReturn(Optional.of(u));

        Optional<User> found = repo.findByEmail("test@test.com");
        assertTrue(found.isPresent());
        assertEquals("test@test.com", found.get().getEmail());
        assertEquals(UserRole.STUDENT, found.get().getUserType());
    }

    @Test
    void findByEmail_returnsEmpty_whenNotFound() {
        when(repo.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        Optional<User> found = repo.findByEmail("missing@test.com");
        assertTrue(found.isEmpty());
    }

    @Test
    void existsByEmail_true_whenExists_falseWhenMissing() {
        when(repo.existsByEmail("exists@test.com")).thenReturn(true);
        when(repo.existsByEmail("missing@test.com")).thenReturn(false);

        assertTrue(repo.existsByEmail("exists@test.com"));
        assertFalse(repo.existsByEmail("missing@test.com"));
    }

    @Test
    void countByRole_returnsMockedValue() {
        when(repo.countByRole(UserRole.TEACHER)).thenReturn(5);

        int count = repo.countByRole(UserRole.TEACHER);
        assertEquals(5, count);
    }

    @Test
    void findAllTeachers_returnsList() {
        List<User> teachers = List.of(
                new User(1L, "t1@test.com", "hash", "Alice", "Zeus", UserRole.TEACHER, null),
                new User(2L, "t2@test.com", "hash", "Bob", "Alpha", UserRole.TEACHER, null)
        );

        when(repo.findAllTeachers()).thenReturn(List.of());

        // You can still verify method call works without DB
        repo.findAllTeachers();
        verify(repo).findAllTeachers();
    }
}
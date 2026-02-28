package service;

import model.User;
import model.UserRole;
import repository.UserRepository;
import security.SecurityContext;
import security.Session;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // LOGIN (email + password)
    public Session login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        Session session = new Session(
                user.getId(),
                user.getEmail(),
                user.getUserType()
        );

        SecurityContext.set(session);
        return session;
    }

    // SIGNUP
    public void signup(String email,
                       String password,
                       String firstName,
                       String lastName,
                       UserRole userType,
                       String studentCode) {

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }


        if (userType == UserRole.ADMIN) {
            throw new RuntimeException("Admin cannot self-register");
        }


        if (userType == UserRole.STUDENT) {
            if (studentCode == null || studentCode.isBlank()) {
                throw new RuntimeException("Student code is required for students");
            }
        } else {

            studentCode = null;
        }

        String hash = passwordEncoder.encode(password);

        User user = new User(
                null,
                email,
                hash,
                firstName,
                lastName,
                userType,
                studentCode
        );

        userRepository.save(user);
    }

    public void logout() {
        SecurityContext.clear();
    }
}
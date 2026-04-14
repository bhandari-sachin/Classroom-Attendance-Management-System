package service;

import dto.AuthResponse;
import backend.exception.ApiException;
import model.User;
import model.UserRole;
import repository.UserRepository;
import security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    // LOGIN (email + password) -> returns JWT
    public AuthResponse login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(401, "Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(401, "Invalid credentials");
        }

        String token = jwtService.issueToken(
                user.getId(),
                user.getEmail(),
                user.getUserType().name()
        );

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getUserType().name()
        );
    }

    // SIGNUP
    public void signup(String email,
                       String password,
                       String firstName,
                       String lastName,
                       UserRole userType,
                       String studentCode) {

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(400, "Email already exists");
        }

        if (userType == UserRole.ADMIN) {
            throw new ApiException(403, "Admin cannot self-register");
        }

        if (userType == UserRole.STUDENT) {
            if (studentCode == null || studentCode.isBlank()) {
                throw new ApiException(400, "Student code is required for students");
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
        // JWT is stateless; logout handled client-side by discarding token
    }
}
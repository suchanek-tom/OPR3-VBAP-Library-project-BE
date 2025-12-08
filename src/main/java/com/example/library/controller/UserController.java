package com.example.library.controller;

import com.example.library.model.User;
import com.example.library.repository.UserRepository;
import com.example.library.dto.LoginRequest;
import com.example.library.dto.LoginResponse;
import com.example.library.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserController(UserRepository repo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<List<User>> all() {
        logger.info("GET request: Fetching all users");
        try {
            List<User> users = repo.findAll();
            logger.info("Successfully retrieved {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error retrieving all users", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve users");
        }
    }

    // Create user
    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        logger.info("POST request: Creating new user with email: {}", user != null ? user.getEmail() : "null");
        try {
            if (repo.findByEmail(user.getEmail()).isPresent()) {
                logger.warn("User creation failed: Email already exists: {}", user.getEmail());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User saved = repo.save(user);
            logger.info("Successfully created user with ID: {} and email: {}", saved.getId(), saved.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid user data provided", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating user", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user");
        }
    }

    /**
     * Register endpoint - creates user and returns JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        logger.info("POST request: Registering new user with email: {}", user != null ? user.getEmail() : "null");
        try {
            if (repo.findByEmail(user.getEmail()).isPresent()) {
                logger.warn("User registration failed: Email already exists: {}", user.getEmail());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User saved = repo.save(user);
            logger.info("User registered successfully with ID: {} and email: {}", saved.getId(), saved.getEmail());

            String token = jwtUtil.generateToken(String.valueOf(saved.getId()), saved.getEmail(),
                    saved.getRole().name());
            logger.debug("JWT token generated for user: {}", saved.getEmail());

            LoginResponse response = new LoginResponse(
                    saved.getId(),
                    saved.getName(),
                    saved.getSurname(),
                    saved.getEmail(),
                    saved.getAddress(),
                    saved.getCity(),
                    saved.getRole(),
                    token);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid user data during registration", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error registering user", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to register user");
        }
    }

    /**
     * Login endpoint - validates credentials and returns user information with JWT
     * token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("POST request: User login attempt with email: {}",
                loginRequest != null ? loginRequest.getEmail() : "null");
        try {
            if (loginRequest.getEmail() == null || loginRequest.getEmail().isBlank()) {
                logger.warn("Login attempt with missing email");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
            }
            if (loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
                logger.warn("Login attempt with missing password");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
            }

            Optional<User> userOpt = repo.findByEmail(loginRequest.getEmail());

            if (userOpt.isEmpty()) {
                logger.warn("Login failed: User not found with email: {}", loginRequest.getEmail());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
            }

            User user = userOpt.get();

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                logger.warn("Login failed: Invalid password for user: {}", loginRequest.getEmail());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
            }

            String token = jwtUtil.generateToken(String.valueOf(user.getId()), user.getEmail(), user.getRole().name());
            logger.info("User login successful: {} (ID: {})", loginRequest.getEmail(), user.getId());
            logger.debug("JWT token generated for login: {}", loginRequest.getEmail());

            LoginResponse response = new LoginResponse(
                    user.getId(),
                    user.getName(),
                    user.getSurname(),
                    user.getEmail(),
                    user.getAddress(),
                    user.getCity(),
                    user.getRole(),
                    token);

            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error during login process", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed");
        }
    }

    // GET user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getOne(@PathVariable Integer id) {
        logger.info("GET request: Fetching user with id: {}", id);
        try {
            if (id == null || id <= 0) {
                logger.warn("Invalid user ID: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID");
            }

            User user = repo.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("User not found with id: {}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                    });
            logger.info("Successfully retrieved user: {} (ID: {})", user.getEmail(), user.getId());
            return ResponseEntity.ok(user);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving user with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve user");
        }
    }

    // update user
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Integer id, @Valid @RequestBody User updated) {
        logger.info("PUT request: Updating user with id: {}", id);
        try {
            if (id == null || id <= 0) {
                logger.warn("Invalid user ID for update: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID");
            }

            User user = repo.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("User not found for update with id: {}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                    });

            if (updated.getEmail() != null && !updated.getEmail().equals(user.getEmail())) {
                if (repo.findByEmail(updated.getEmail()).isPresent()) {
                    logger.warn("Email update failed: Email already exists: {}", updated.getEmail());
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
                }
            }

            if (updated.getName() != null && !updated.getName().isBlank()) {
                user.setName(updated.getName());
            }
            if (updated.getSurname() != null && !updated.getSurname().isBlank()) {
                user.setSurname(updated.getSurname());
            }
            if (updated.getEmail() != null && !updated.getEmail().isBlank()) {
                user.setEmail(updated.getEmail());
            }
            if (updated.getAddress() != null && !updated.getAddress().isBlank()) {
                user.setAddress(updated.getAddress());
            }
            if (updated.getCity() != null && !updated.getCity().isBlank()) {
                user.setCity(updated.getCity());
            }

            if (updated.getPassword() != null && !updated.getPassword().isEmpty()
                    && updated.getPassword().length() >= 6) {
                logger.info("Password updated for user: {}", id);
                user.setPassword(passwordEncoder.encode(updated.getPassword()));
            }

            User saved = repo.save(user);
            logger.info("User updated successfully: {} (ID: {})", saved.getEmail(), saved.getId());
            return ResponseEntity.ok(saved);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid user data for update", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating user with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user");
        }
    }

    /**
     * Delete user by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        logger.info("DELETE request: Deleting user with id: {}", id);
        try {
            if (id == null || id <= 0) {
                logger.warn("Invalid user ID for deletion: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID");
            }

            if (!repo.existsById(id)) {
                logger.warn("User not found for deletion with id: {}", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }

            repo.deleteById(id);
            logger.info("User deleted successfully with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting user with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete user");
        }
    }
}

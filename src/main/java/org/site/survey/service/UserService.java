package org.site.survey.service;

import lombok.RequiredArgsConstructor;
import org.site.survey.model.User;
import org.site.survey.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public Mono<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Mono<User> createUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public Mono<User> updateUser(Integer id, User user) {
        return userRepository.findById(id)
                .flatMap(existingUser -> {
                    existingUser.setUsername(user.getUsername());
                    existingUser.setEmail(user.getEmail());
                    if (user.getPasswordHash() != null) {
                        existingUser.setPasswordHash(user.getPasswordHash());
                    }
                    existingUser.setRoleId(user.getRoleId());
                    return userRepository.save(existingUser);
                });
    }
}
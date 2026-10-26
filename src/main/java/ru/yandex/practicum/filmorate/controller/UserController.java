package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserMapper;
import ru.yandex.practicum.filmorate.dto.UserResponse;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> body = userService.getAll().stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(UserMapper.toResponse(userService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody User user) {
        User created = userService.add(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserMapper.toResponse(created));
    }

    @PutMapping
    public ResponseEntity<UserResponse> update(@RequestBody User user) {
        return ResponseEntity.ok(UserMapper.toResponse(userService.update(user)));
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.removeFriend(id, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<UserResponse>> getFriends(@PathVariable Long id) {
        List<UserResponse> body = userService.getFriends(id).stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<UserResponse>> getCommonFriends(@PathVariable Long id,
                                                               @PathVariable Long otherId) {
        List<UserResponse> body = userService.getCommonFriends(id, otherId).stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }
}
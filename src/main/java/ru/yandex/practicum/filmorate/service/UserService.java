package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User create(NewUserRequest request) {
        User user = UserMapper.toEntity(request);
        setNameIfBlank(user);
        validateUser(user);
        log.info("Создание пользователя: {}", user.getLogin());
        return userStorage.addUser(user);
    }

    public User update(UpdateUserRequest request) {
        User existing = userStorage.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + request.getId() + " не найден"));
        UserMapper.applyUpdate(existing, request);
        setNameIfBlank(existing);
        validateUser(existing);
        log.info("Обновление пользователя: id={}", existing.getId());
        return userStorage.updateUser(existing);
    }

    public Collection<User> getAll() {
        return userStorage.findAll();
    }

    public User getById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить в друзья самого себя");
        }
        ensureExists(userId);
        ensureExists(friendId);
        userStorage.addFriend(userId, friendId);
        log.info("Пользователь {} отправил заявку в друзья {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        ensureExists(userId);
        ensureExists(friendId);
        userStorage.deleteFriend(userId, friendId);
        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        ensureExists(userId);
        return userStorage.findAllFriends(userId).stream().collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        ensureExists(userId);
        ensureExists(otherId);
        return userStorage.findCommonFriend(userId, otherId).stream().collect(Collectors.toList());
    }

    private void ensureExists(Long id) {
        userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    private void setNameIfBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
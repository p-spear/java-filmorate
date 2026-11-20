package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.debug("Пользователь сохранён: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.debug("Пользователь обновлён: id={}", user.getId());
        return user;
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = requireUser(userId);
        requireUser(friendId);
        user.getFriends().add(friendId);
        log.debug("Заявка в друзья: userId={} -> friendId={}", userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        User user = requireUser(userId);
        requireUser(friendId);
        user.getFriends().remove(friendId);
        User friend = users.get(friendId);
        friend.getFriends().remove(userId);
        log.debug("Удалены из друзей: userId={}, friendId={}", userId, friendId);
    }

    @Override
    public Collection<User> findAllFriends(Long userId) {
        User user = requireUser(userId);
        return user.getFriends().stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> findCommonFriend(Long userId, Long otherId) {
        User user = requireUser(userId);
        User other = requireUser(otherId);
        Set<Long> common = user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .collect(Collectors.toSet());
        return common.stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    private User requireUser(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
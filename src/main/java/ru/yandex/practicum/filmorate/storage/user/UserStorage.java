package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    User addUser(User user);

    Optional<User> findById(Long id);

    User updateUser(User user);

    Collection<User> findAll();

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    Collection<User> findAllFriends(Long userId);

    Collection<User> findCommonFriend(Long userId, Long otherId);
}
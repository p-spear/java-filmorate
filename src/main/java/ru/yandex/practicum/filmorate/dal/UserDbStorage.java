package ru.yandex.practicum.filmorate.dal;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.util.Collection;
import java.util.Optional;

@Repository
@Primary
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    private static final String FIND_ALL = "SELECT * FROM users ORDER BY id";
    private static final String FIND_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String INSERT =
            "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE id=?";

    private static final String ADD_FRIEND =
            "MERGE INTO friendship(user_id, friend_id, status) KEY(user_id, friend_id) " +
                    "VALUES (?, ?, 'PENDING')";
    private static final String DELETE_FRIEND =
            "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIENDS =
            "SELECT u.* FROM users u JOIN friendship f ON f.friend_id = u.id " +
                    "WHERE f.user_id = ? ORDER BY u.id";
    private static final String FIND_COMMON =
            "SELECT u.* FROM users u " +
                    "JOIN friendship f1 ON f1.friend_id = u.id AND f1.user_id = ? " +
                    "JOIN friendship f2 ON f2.friend_id = u.id AND f2.user_id = ? " +
                    "ORDER BY u.id";

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper, User.class);
    }

    @Override
    public User addUser(User user) {
        long id = insert(INSERT,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() == null ? null : Date.valueOf(user.getBirthday()));
        user.setId(id);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return findOne(FIND_BY_ID, id);
    }

    @Override
    public User updateUser(User user) {
        update(UPDATE,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() == null ? null : Date.valueOf(user.getBirthday()),
                user.getId());
        return user;
    }

    @Override
    public Collection<User> findAll() {
        return findMany(FIND_ALL);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        update(ADD_FRIEND, userId, friendId);          // MERGE → 1 строка
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        delete(DELETE_FRIEND, userId, friendId);
    }

    @Override
    public Collection<User> findAllFriends(Long userId) {
        return findMany(FIND_FRIENDS, userId);
    }

    @Override
    public Collection<User> findCommonFriend(Long userId, Long otherId) {
        return findMany(FIND_COMMON, userId, otherId);
    }
}
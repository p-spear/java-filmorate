package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Repository
public class FriendshipRepository extends BaseRepository<User> {

    private static final String FIND_FRIENDS =
            "SELECT u.* FROM users u JOIN friendship f ON f.friend_id = u.id " +
                    "WHERE f.user_id = ? ORDER BY u.id";
    private static final String FIND_COMMON =
            "SELECT u.* FROM users u " +
                    "JOIN friendship f1 ON f1.friend_id = u.id AND f1.user_id = ? " +
                    "JOIN friendship f2 ON f2.friend_id = u.id AND f2.user_id = ? " +
                    "ORDER BY u.id";

    public FriendshipRepository(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper, User.class);
    }

    public void addRequest(long userId, long friendId) {
        jdbc.update("MERGE INTO friendship(user_id, friend_id, status) " +
                "KEY(user_id, friend_id) VALUES (?, ?, 'PENDING')", userId, friendId);
    }

    public void confirm(long userId, long friendId) {
        jdbc.update("UPDATE friendship SET status='CONFIRMED' WHERE user_id=? AND friend_id=?",
                friendId, userId);
        jdbc.update("MERGE INTO friendship(user_id, friend_id, status) " +
                "KEY(user_id, friend_id) VALUES (?, ?, 'CONFIRMED')", userId, friendId);
    }

    public void remove(long userId, long friendId) {
        jdbc.update("DELETE FROM friendship WHERE (user_id=? AND friend_id=?) OR " +
                        "(user_id=? AND friend_id=?)",
                userId, friendId, friendId, userId);
    }

    public List<User> getFriends(long userId) {
        return findMany(FIND_FRIENDS, userId);
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        return findMany(FIND_COMMON, userId, otherId);
    }
}
package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDTO;
import ru.yandex.practicum.filmorate.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    public static User toEntity(NewUserRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setLogin(request.getLogin());
        user.setName(request.getName());
        user.setBirthday(request.getBirthday());
        return user;
    }

    public static UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setBirthday(user.getBirthday());
        return dto;
    }

    public static void applyUpdate(User target, UpdateUserRequest request) {
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            target.setEmail(request.getEmail());
        }
        if (request.getLogin() != null && !request.getLogin().isBlank()) {
            target.setLogin(request.getLogin());
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            target.setName(request.getName());
        }
        if (request.getBirthday() != null) {
            target.setBirthday(request.getBirthday());
        }
    }
}
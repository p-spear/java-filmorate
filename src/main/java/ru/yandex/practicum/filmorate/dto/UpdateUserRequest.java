package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    private Long id;

    @Email(message = "email должен содержать @")
    @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String login;

    @Size(max = 255)
    private String name;

    @Past(message = "birthday не может быть в будущем")
    private LocalDate birthday;
}
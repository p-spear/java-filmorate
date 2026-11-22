package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NewUserRequest {
    @NotBlank(message = "email не может быть пустым")
    @Email(message = "email должен содержать @")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "login не может быть пустым")
    @Pattern(regexp = "\\S+", message = "login не может содержать пробелы")
    @Size(max = 50)
    private String login;

    @Size(max = 255)
    private String name;

    @Past(message = "birthday не может быть в будущем")
    private LocalDate birthday;
}
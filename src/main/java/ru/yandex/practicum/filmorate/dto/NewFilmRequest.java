package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.Set;

@Data
public class NewFilmRequest {
    @NotBlank(message = "name не может быть пустым")
    @Size(max = 255)
    private String name;

    @Size(max = 200, message = "description — не более 200 символов")
    private String description;

    @NotNull(message = "releaseDate должен быть указан")
    private LocalDate releaseDate;

    @NotNull(message = "duration должен быть указан")
    @Positive(message = "duration должен быть положительным")
    private Integer duration;

    private MpaRating mpa;
    private Set<Genre> genres;
}
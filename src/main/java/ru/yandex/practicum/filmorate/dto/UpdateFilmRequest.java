package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateFilmRequest {
    private Long id;

    @Size(max = 255)
    private String name;

    @Size(max = 200)
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "duration должен быть положительным")
    private Integer duration;

    private MpaRating mpa;
    private Set<Genre> genres;
}
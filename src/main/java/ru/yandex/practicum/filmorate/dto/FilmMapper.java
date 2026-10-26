package ru.yandex.practicum.filmorate.dto;

import ru.yandex.practicum.filmorate.model.Film;

public final class FilmMapper {

    public static FilmResponse toResponse(Film film) {
        FilmResponse dto = new FilmResponse();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        return dto;
    }
}
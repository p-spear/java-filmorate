package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmDTO;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {

    public static Film toEntity(NewFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (request.getMpa() != null) {
            MpaRating mpa = new MpaRating();
            mpa.setId(request.getMpa().getId());
            film.setMpa(mpa);
        }
        if (request.getGenres() != null) {
            film.setGenres(request.getGenres().stream().map(g -> {
                Genre genre = new Genre();
                genre.setId(g.getId());
                return genre;
            }).collect(Collectors.toSet()));
        }
        return film;
    }

    public static FilmDTO toDTO(Film film) {
        FilmDTO dto = new FilmDTO();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setMpa(film.getMpa());
        dto.setGenres(film.getGenres());
        return dto;
    }

    public static void applyUpdate(Film target, UpdateFilmRequest request) {
        if (request.getName() != null && !request.getName().isBlank()) {
            target.setName(request.getName());
        }
        if (request.getDescription() != null) {
            target.setDescription(request.getDescription());
        }
        if (request.getReleaseDate() != null) {
            target.setReleaseDate(request.getReleaseDate());
        }
        if (request.getDuration() != null) {
            target.setDuration(request.getDuration());
        }
        if (request.getMpa() != null) {
            MpaRating mpa = new MpaRating();
            mpa.setId(request.getMpa().getId());
            target.setMpa(mpa);
        }
        if (request.getGenres() != null) {
            target.setGenres(request.getGenres().stream().map(g -> {
                Genre genre = new Genre();
                genre.setId(g.getId());
                return genre;
            }).collect(Collectors.toSet()));
        }
    }
}
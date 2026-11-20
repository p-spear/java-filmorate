package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;

    public Film create(NewFilmRequest request) {
        Film film = FilmMapper.toEntity(request);
        validateFilm(film);

        // Валидация существования MPA и жанров (чтобы отдавать 404)
        if (film.getMpa() != null) {
            mpaRepository.findById(film.getMpa().getId()); // бросит NotFoundException, если нет
        }
        if (film.getGenres() != null) {
            for (var genre : film.getGenres()) {
                genreRepository.findById(genre.getId()); // бросит NotFoundException, если нет
            }
        }

        log.info("Добавление фильма: {}", film.getName());
        return filmStorage.addFilm(film);
    }

    public Film update(UpdateFilmRequest request) {
        Film existing = filmStorage.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + request.getId() + " не найден"));

        FilmMapper.applyUpdate(existing, request);
        validateFilm(existing);

        if (existing.getMpa() != null) {
            mpaRepository.findById(existing.getMpa().getId());
        }
        if (existing.getGenres() != null) {
            for (var genre : existing.getGenres()) {
                genreRepository.findById(genre.getId());
            }
        }

        log.info("Обновление фильма: id={}", existing.getId());
        return filmStorage.updateFilm(existing);
    }

    public Collection<Film> getAll() {
        return filmStorage.findAll();
    }

    public Film getById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getPopular(count).stream().collect(Collectors.toList());
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private void resolveMpaAndGenres(Film film, Long mpaId, Set<Long> genreIds) {
        if (mpaId != null) {
            film.setMpa(mpaRepository.findById(mpaId));
        }
        if (genreIds != null) {
            film.setGenres(genreIds.stream()
                    .map(genreRepository::findById)
                    .collect(Collectors.toSet()));
        }
    }
}
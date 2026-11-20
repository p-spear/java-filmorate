package ru.yandex.practicum.filmorate.dal;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Primary
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL =
            "SELECT f.*, m.name AS mpa_name FROM film f " +
                    "LEFT JOIN mpa_rating m ON m.id = f.mpa_id ORDER BY f.id";
    private static final String FIND_BY_ID =
            "SELECT f.*, m.name AS mpa_name FROM film f " +
                    "LEFT JOIN mpa_rating m ON m.id = f.mpa_id WHERE f.id = ?";
    private static final String INSERT =
            "INSERT INTO film(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE film SET name=?, description=?, release_date=?, duration=?, mpa_id=? WHERE id=?";
    private static final String GENRES_BY_FILM =
            "SELECT g.* FROM genre g JOIN film_genre fg ON fg.genre_id = g.id " +
                    "WHERE fg.film_id = ? ORDER BY g.id";
    private static final String INSERT_GENRE =
            "MERGE INTO film_genre(film_id, genre_id) KEY(film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_GENRES = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String ADD_LIKE =
            "MERGE INTO film_like(film_id, user_id) KEY(film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE =
            "DELETE FROM film_like WHERE film_id = ? AND user_id = ?";
    private static final String POPULAR =
            "SELECT f.*, m.name AS mpa_name, COUNT(fl.user_id) AS likes " +
                    "FROM film f LEFT JOIN mpa_rating m ON m.id = f.mpa_id " +
                    "LEFT JOIN film_like fl ON fl.film_id = f.id " +
                    "GROUP BY f.id, m.name ORDER BY likes DESC, f.id ASC LIMIT ?";

    private final GenreRowMapper genreMapper;

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper, GenreRowMapper genreMapper) {
        super(jdbc, mapper, Film.class);
        this.genreMapper = genreMapper;
    }

    @Override
    public Film addFilm(Film film) {
        long id = insert(INSERT,
                film.getName(), film.getDescription(),
                Date.valueOf(film.getReleaseDate()), film.getDuration(),
                film.getMpa() == null ? null : film.getMpa().getId());
        film.setId(id);
        saveGenres(film);
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = findMany(FIND_ALL);
        films.forEach(f -> f.setGenres(loadGenres(f.getId())));
        return films;
    }

    @Override
    public Film updateFilm(Film film) {
        update(UPDATE,
                film.getName(), film.getDescription(),
                Date.valueOf(film.getReleaseDate()), film.getDuration(),
                film.getMpa() == null ? null : film.getMpa().getId(), film.getId());
        jdbc.update(DELETE_GENRES, film.getId());
        saveGenres(film);
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        Optional<Film> filmOpt = findOne(FIND_BY_ID, id);
        filmOpt.ifPresent(f -> f.setGenres(loadGenres(f.getId())));
        return filmOpt;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        update(ADD_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        delete(REMOVE_LIKE, filmId, userId);
    }

    @Override
    public Collection<Film> getPopular(int count) {
        List<Film> films = findMany(POPULAR, count);   // findMany = jdbc.query(query, mapper, params)
        films.forEach(f -> f.setGenres(loadGenres(f.getId())));
        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null) return;
        for (Genre g : film.getGenres()) {
            update(INSERT_GENRE, film.getId(), g.getId()); // MERGE → 1 строка
        }
    }

    private Set<Genre> loadGenres(Long filmId) {
        return new HashSet<>(jdbc.query(GENRES_BY_FILM, genreMapper, filmId));
    }
}
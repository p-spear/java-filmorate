package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
public class GenreRepository extends BaseRepository<Genre> {

    public GenreRepository(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper, Genre.class);
    }

    public List<Genre> findAll() {
        return findMany("SELECT * FROM genre ORDER BY id");
    }

    public Genre findById(long id) {
        return findOne("SELECT * FROM genre WHERE id = ?", id)
                .orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден"));
    }
}
package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Repository
public class MpaRepository extends BaseRepository<MpaRating> {

    public MpaRepository(JdbcTemplate jdbc, MpaRowMapper mapper) {
        super(jdbc, mapper, MpaRating.class);
    }

    public List<MpaRating> findAll() {
        return findMany("SELECT * FROM mpa_rating ORDER BY id");
    }

    public MpaRating findById(long id) {
        return findOne("SELECT * FROM mpa_rating WHERE id = ?", id)
                .orElseThrow(() -> new NotFoundException("MPA с id = " + id + " не найден"));
    }
}
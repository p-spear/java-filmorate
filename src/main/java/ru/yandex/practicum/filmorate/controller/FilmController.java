package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmMapper;
import ru.yandex.practicum.filmorate.dto.FilmResponse;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public ResponseEntity<List<FilmResponse>> findAll() {
        List<FilmResponse> body = filmService.getAll().stream()
                .map(FilmMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(FilmMapper.toResponse(filmService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<FilmResponse> create(@RequestBody Film film) {
        Film created = filmService.add(film);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(FilmMapper.toResponse(created));
    }

    @PutMapping
    public ResponseEntity<FilmResponse> update(@RequestBody Film film) {
        return ResponseEntity.ok(FilmMapper.toResponse(filmService.update(film)));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FilmResponse>> getPopular(
            @RequestParam(defaultValue = "10") int count) {

        if (count <= 0) {
            throw new ru.yandex.practicum.filmorate.exception.ParameterNotValidException(
                    "count", "значение должно быть положительным числом");
        }

        List<FilmResponse> body = filmService.getPopular(count).stream()
                .map(FilmMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }
}
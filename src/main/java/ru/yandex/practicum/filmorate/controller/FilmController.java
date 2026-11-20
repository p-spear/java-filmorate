package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDTO;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public List<FilmDTO> findAll() {
        return filmService.getAll().stream()
                .map(FilmMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public FilmDTO findById(@PathVariable Long id) {
        return FilmMapper.toDTO(filmService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDTO create(@RequestBody @Valid NewFilmRequest request) {
        return FilmMapper.toDTO(filmService.create(request));
    }

    @PutMapping
    public FilmDTO update(@RequestBody @Valid UpdateFilmRequest request) {
        return FilmMapper.toDTO(filmService.update(request));
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDTO> getPopular(@RequestParam(defaultValue = "10") int count) {
        if (count <= 0) {
            throw new ru.yandex.practicum.filmorate.exception.ParameterNotValidException(
                    "count", "значение должно быть положительным числом");
        }
        return filmService.getPopular(count).stream()
                .map(FilmMapper::toDTO)
                .collect(Collectors.toList());
    }
}
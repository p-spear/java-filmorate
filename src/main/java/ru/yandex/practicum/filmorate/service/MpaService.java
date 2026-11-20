package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaRepository mpaRepository;

    public List<MpaRating> getAll() { return mpaRepository.findAll(); }
    public MpaRating getById(long id) { return mpaRepository.findById(id); }
}

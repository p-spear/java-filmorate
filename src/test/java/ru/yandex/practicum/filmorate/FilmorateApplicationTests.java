package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmorateApplicationTests {

	@Nested
	@DisplayName("Тесты валидации фильмов")
	class FilmValidationTests {

		private FilmController filmController;
		private Film film;

		@BeforeEach
		void setUp() {
			filmController = new FilmController();
			film = new Film();
			film.setName("Test Film");
			film.setDescription("Description");
			film.setReleaseDate(LocalDate.of(2000, 1, 1));
			film.setDuration(120);
		}

		@Test
		@DisplayName("Добавление корректного фильма")
		void shouldAddValidFilm() {
			Film added = filmController.create(film);
			assertNotNull(added.getId());
			assertEquals(1, filmController.findAll().size());
		}

		@Test
		@DisplayName("Ошибка при пустом названии фильма")
		void shouldThrowExceptionWhenNameIsEmpty() {
			film.setName("");
			assertThrows(ValidationException.class, () -> filmController.create(film));
		}

		@Test
		@DisplayName("Ошибка при названии из пробелов")
		void shouldThrowExceptionWhenNameIsBlank() {
			film.setName("   ");
			assertThrows(ValidationException.class, () -> filmController.create(film));
		}

		@Test
		@DisplayName("Ошибка при описании длиннее 200 символов")
		void shouldThrowExceptionWhenDescriptionTooLong() {
			film.setDescription("a".repeat(201));
			assertThrows(ValidationException.class, () -> filmController.create(film));
		}

		@Test
		@DisplayName("Описание ровно 200 символов — валидно (граничное условие)")
		void shouldAcceptDescriptionOfExactly200Chars() {
			film.setDescription("a".repeat(200));
			assertDoesNotThrow(() -> filmController.create(film));
		}

		@Test
		@DisplayName("Ошибка при дате релиза раньше 28.12.1895")
		void shouldThrowExceptionWhenReleaseDateTooEarly() {
			film.setReleaseDate(LocalDate.of(1895, 12, 27));
			assertThrows(ValidationException.class, () -> filmController.create(film));
		}

		@Test
		@DisplayName("Дата релиза 28.12.1895 — валидна (граничное условие)")
		void shouldAcceptMinReleaseDate() {
			film.setReleaseDate(LocalDate.of(1895, 12, 28));
			assertDoesNotThrow(() -> filmController.create(film));
		}

		@Test
		@DisplayName("Ошибка при отрицательной продолжительности фильма")
		void shouldThrowExceptionWhenDurationIsNegative() {
			film.setDuration(-1);
			assertThrows(ValidationException.class, () -> filmController.create(film));
		}

		@Test
		@DisplayName("Ошибка при нулевой продолжительности фильма")
		void shouldThrowExceptionWhenDurationIsZero() {
			film.setDuration(0);
			assertThrows(ValidationException.class, () -> filmController.create(film));
		}

		@Test
		@DisplayName("Ошибка обновления фильма без указания id")
		void shouldThrowExceptionWhenUpdateWithoutId() {
			film.setId(null);
			assertThrows(ValidationException.class, () -> filmController.update(film));
		}

		@Test
		@DisplayName("Ошибка обновления несуществующего фильма")
		void shouldThrowExceptionWhenUpdateUnknownFilm() {
			film.setId(999L);
			assertThrows(ValidationException.class, () -> filmController.update(film));
		}
	}

	@Nested
	@DisplayName("Тесты валидации пользователей")
	class UserValidationTests {

		private UserController userController;
		private User user;

		@BeforeEach
		void setUp() {
			userController = new UserController();
			user = new User();
			user.setEmail("test@mail.ru");
			user.setLogin("login");
			user.setName("Name");
			user.setBirthday(LocalDate.of(1990, 1, 1));
		}

		@Test
		@DisplayName("Создание корректного пользователя")
		void shouldCreateValidUser() {
			User created = userController.create(user);
			assertNotNull(created.getId());
			assertEquals(1, userController.findAll().size());
		}

		@Test
		@DisplayName("Если имя пустое — используется логин")
		void shouldUseLoginWhenNameIsEmpty() {
			user.setName("");
			User created = userController.create(user);
			assertEquals(user.getLogin(), created.getName());
		}

		@Test
		@DisplayName("Если имя null — используется логин")
		void shouldUseLoginWhenNameIsNull() {
			user.setName(null);
			User created = userController.create(user);
			assertEquals(user.getLogin(), created.getName());
		}

		@Test
		@DisplayName("Ошибка при email без символа @")
		void shouldThrowExceptionWhenEmailIsInvalid() {
			user.setEmail("invalid-email");
			assertThrows(ValidationException.class, () -> userController.create(user));
		}

		@Test
		@DisplayName("Ошибка при пустом email")
		void shouldThrowExceptionWhenEmailIsEmpty() {
			user.setEmail("");
			assertThrows(ValidationException.class, () -> userController.create(user));
		}

		@Test
		@DisplayName("Ошибка при логине с пробелами")
		void shouldThrowExceptionWhenLoginHasSpaces() {
			user.setLogin("log in");
			assertThrows(ValidationException.class, () -> userController.create(user));
		}

		@Test
		@DisplayName("Ошибка при пустом логине")
		void shouldThrowExceptionWhenLoginIsEmpty() {
			user.setLogin("");
			assertThrows(ValidationException.class, () -> userController.create(user));
		}

		@Test
		@DisplayName("Ошибка при дате рождения в будущем")
		void shouldThrowExceptionWhenBirthdayIsInFuture() {
			user.setBirthday(LocalDate.now().plusDays(1));
			assertThrows(ValidationException.class, () -> userController.create(user));
		}

		@Test
		@DisplayName("Дата рождения сегодня — валидна (граничное условие)")
		void shouldAcceptTodayAsBirthday() {
			user.setBirthday(LocalDate.now());
			assertDoesNotThrow(() -> userController.create(user));
		}

		@Test
		@DisplayName("Ошибка обновления пользователя без указания id")
		void shouldThrowExceptionWhenUpdateWithoutId() {
			user.setId(null);
			assertThrows(ValidationException.class, () -> userController.update(user));
		}

		@Test
		@DisplayName("Ошибка обновления несуществующего пользователя")
		void shouldThrowExceptionWhenUpdateUnknownUser() {
			user.setId(999L);
			assertThrows(ValidationException.class, () -> userController.update(user));
		}
	}
}
package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

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
		@DisplayName("Добавление корректного фильма — 201 Created")
		void shouldAddValidFilm() {
			ResponseEntity<Film> response = filmController.create(film);

			assertEquals(HttpStatus.CREATED, response.getStatusCode());
			assertNotNull(response.getBody());
			assertNotNull(response.getBody().getId());
			assertEquals(1, filmController.findAll().getBody().size());
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

		@Test
		@DisplayName("Обновление существующего фильма — 200 OK")
		void shouldUpdateExistingFilm() {
			Film created = filmController.create(film).getBody();

			Film updated = new Film();
			updated.setId(created.getId());
			updated.setName("Updated Name");
			updated.setDescription("Updated Description");
			updated.setReleaseDate(LocalDate.of(2001, 1, 1));
			updated.setDuration(150);

			ResponseEntity<Film> response = filmController.update(updated);

			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals("Updated Name", response.getBody().getName());
			assertEquals(150, response.getBody().getDuration());
		}

		@Test
		@DisplayName("Получение всех фильмов — 200 OK")
		void shouldReturnAllFilms() {
			filmController.create(film);

			ResponseEntity<Collection<Film>> response = filmController.findAll();

			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(1, response.getBody().size());
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
		@DisplayName("Создание корректного пользователя — 201 Created")
		void shouldCreateValidUser() {
			ResponseEntity<User> response = userController.create(user);

			assertEquals(HttpStatus.CREATED, response.getStatusCode());
			assertNotNull(response.getBody());
			assertNotNull(response.getBody().getId());
			assertEquals(1, userController.findAll().getBody().size());
		}

		@Test
		@DisplayName("Если имя пустое — используется логин")
		void shouldUseLoginWhenNameIsEmpty() {
			user.setName("");
			User created = userController.create(user).getBody();
			assertEquals(user.getLogin(), created.getName());
		}

		@Test
		@DisplayName("Если имя null — используется логин")
		void shouldUseLoginWhenNameIsNull() {
			user.setName(null);
			User created = userController.create(user).getBody();
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

		@Test
		@DisplayName("Обновление существующего пользователя — 200 OK")
		void shouldUpdateExistingUser() {
			User created = userController.create(user).getBody();

			User updated = new User();
			updated.setId(created.getId());
			updated.setEmail("new@mail.ru");
			updated.setLogin("newlogin");
			updated.setName("New Name");
			updated.setBirthday(LocalDate.of(1995, 5, 5));

			ResponseEntity<User> response = userController.update(updated);

			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals("new@mail.ru", response.getBody().getEmail());
			assertEquals("newlogin", response.getBody().getLogin());
		}

		@Test
		@DisplayName("Получение всех пользователей — 200 OK")
		void shouldReturnAllUsers() {
			userController.create(user);

			ResponseEntity<Collection<User>> response = userController.findAll();

			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(1, response.getBody().size());
		}
	}
}
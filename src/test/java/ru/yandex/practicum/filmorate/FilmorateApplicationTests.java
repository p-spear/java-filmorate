package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ParameterNotValidException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmorateApplicationTests {

	private UserService userService;
	private FilmService filmService;
	private UserController userController;
	private FilmController filmController;

	@BeforeEach
	void setUp() {
		InMemoryUserStorage userStorage = new InMemoryUserStorage();
		InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();

		userService = new UserService(userStorage);
		filmService = new FilmService(filmStorage, userStorage);

		userController = new UserController(userService);
		filmController = new FilmController(filmService);
	}

	// ---------------- Вспомогательные фабрики ----------------

	private User makeUser(String email, String login) {
		User user = new User();
		user.setEmail(email);
		user.setLogin(login);
		user.setName(login);
		user.setBirthday(LocalDate.of(1990, 1, 1));
		return user;
	}

	private Film makeFilm(String name) {
		Film film = new Film();
		film.setName(name);
		film.setDescription("description");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);
		return film;
	}

	// ---------------- ВАЛИДАЦИЯ ФИЛЬМОВ ----------------

	@Nested
	@DisplayName("Валидация фильмов")
	class FilmValidationTests {

		@Test
		@DisplayName("Добавление корректного фильма")
		void shouldAddValidFilm() {
			Film added = filmService.add(makeFilm("Film"));
			assertNotNull(added.getId());
			assertEquals(1, filmService.getAll().size());
		}

		@Test
		@DisplayName("Ошибка при пустом названии фильма")
		void shouldThrowWhenNameIsEmpty() {
			Film film = makeFilm("");
			assertThrows(ValidationException.class, () -> filmService.add(film));
		}

		@Test
		@DisplayName("Ошибка при названии из пробелов")
		void shouldThrowWhenNameIsBlank() {
			Film film = makeFilm("   ");
			assertThrows(ValidationException.class, () -> filmService.add(film));
		}

		@Test
		@DisplayName("Ошибка при описании длиннее 200 символов")
		void shouldThrowWhenDescriptionTooLong() {
			Film film = makeFilm("Film");
			film.setDescription("a".repeat(201));
			assertThrows(ValidationException.class, () -> filmService.add(film));
		}

		@Test
		@DisplayName("Описание ровно 200 символов — валидно")
		void shouldAcceptDescriptionOfExactly200() {
			Film film = makeFilm("Film");
			film.setDescription("a".repeat(200));
			assertDoesNotThrow(() -> filmService.add(film));
		}

		@Test
		@DisplayName("Ошибка при дате релиза раньше 28.12.1895")
		void shouldThrowWhenReleaseDateTooEarly() {
			Film film = makeFilm("Film");
			film.setReleaseDate(LocalDate.of(1895, 12, 27));
			assertThrows(ValidationException.class, () -> filmService.add(film));
		}

		@Test
		@DisplayName("Дата релиза 28.12.1895 — валидна")
		void shouldAcceptMinReleaseDate() {
			Film film = makeFilm("Film");
			film.setReleaseDate(LocalDate.of(1895, 12, 28));
			assertDoesNotThrow(() -> filmService.add(film));
		}

		@Test
		@DisplayName("Ошибка при нулевой/отрицательной продолжительности")
		void shouldThrowWhenDurationNotPositive() {
			Film film = makeFilm("Film");
			film.setDuration(0);
			assertThrows(ValidationException.class, () -> filmService.add(film));

			Film film2 = makeFilm("Film2");
			film2.setDuration(-5);
			assertThrows(ValidationException.class, () -> filmService.add(film2));
		}

		@Test
		@DisplayName("Обновление несуществующего фильма → NotFoundException")
		void shouldThrowWhenUpdateUnknownFilm() {
			Film film = makeFilm("Film");
			film.setId(999L);
			assertThrows(NotFoundException.class, () -> filmService.update(film));
		}

		@Test
		@DisplayName("Обновление без id → ValidationException")
		void shouldThrowWhenUpdateWithoutId() {
			Film film = makeFilm("Film");
			assertThrows(ValidationException.class, () -> filmService.update(film));
		}
	}

	// ---------------- ВАЛИДАЦИЯ ПОЛЬЗОВАТЕЛЕЙ ----------------

	@Nested
	@DisplayName("Валидация пользователей")
	class UserValidationTests {

		@Test
		@DisplayName("Создание корректного пользователя")
		void shouldCreateValidUser() {
			User created = userService.add(makeUser("test@mail.ru", "login"));
			assertNotNull(created.getId());
			assertEquals(1, userService.getAll().size());
		}

		@Test
		@DisplayName("Пустое имя заменяется логином")
		void shouldUseLoginWhenNameIsEmpty() {
			User user = makeUser("test@mail.ru", "login");
			user.setName("");
			User created = userService.add(user);
			assertEquals("login", created.getName());
		}

		@Test
		@DisplayName("null-имя заменяется логином")
		void shouldUseLoginWhenNameIsNull() {
			User user = makeUser("test@mail.ru", "login");
			user.setName(null);
			User created = userService.add(user);
			assertEquals("login", created.getName());
		}

		@Test
		@DisplayName("Ошибка при email без @")
		void shouldThrowWhenEmailInvalid() {
			User user = makeUser("invalid-email", "login");
			assertThrows(ValidationException.class, () -> userService.add(user));
		}

		@Test
		@DisplayName("Ошибка при пустом email")
		void shouldThrowWhenEmailEmpty() {
			User user = makeUser("", "login");
			assertThrows(ValidationException.class, () -> userService.add(user));
		}

		@Test
		@DisplayName("Ошибка при логине с пробелами")
		void shouldThrowWhenLoginHasSpaces() {
			User user = makeUser("test@mail.ru", "log in");
			assertThrows(ValidationException.class, () -> userService.add(user));
		}

		@Test
		@DisplayName("Ошибка при пустом логине")
		void shouldThrowWhenLoginEmpty() {
			User user = makeUser("test@mail.ru", "");
			assertThrows(ValidationException.class, () -> userService.add(user));
		}

		@Test
		@DisplayName("Ошибка при дате рождения в будущем")
		void shouldThrowWhenBirthdayInFuture() {
			User user = makeUser("test@mail.ru", "login");
			user.setBirthday(LocalDate.now().plusDays(1));
			assertThrows(ValidationException.class, () -> userService.add(user));
		}

		@Test
		@DisplayName("Дата рождения сегодня — валидна")
		void shouldAcceptTodayAsBirthday() {
			User user = makeUser("test@mail.ru", "login");
			user.setBirthday(LocalDate.now());
			assertDoesNotThrow(() -> userService.add(user));
		}

		@Test
		@DisplayName("Обновление несуществующего пользователя → NotFoundException")
		void shouldThrowWhenUpdateUnknownUser() {
			User user = makeUser("test@mail.ru", "login");
			user.setId(999L);
			assertThrows(NotFoundException.class, () -> userService.update(user));
		}

		@Test
		@DisplayName("Обновление без id → ValidationException")
		void shouldThrowWhenUpdateWithoutId() {
			User user = makeUser("test@mail.ru", "login");
			assertThrows(ValidationException.class, () -> userService.update(user));
		}
	}

	// ---------------- ДРУЗЬЯ ----------------

	@Nested
	@DisplayName("Друзья")
	class FriendsTests {

		private User a;
		private User b;
		private User c;

		@BeforeEach
		void setUp() {
			a = userService.add(makeUser("a@mail.ru", "a"));
			b = userService.add(makeUser("b@mail.ru", "b"));
			c = userService.add(makeUser("c@mail.ru", "c"));
		}

		@Test
		@DisplayName("Добавление в друзья взаимно")
		void addFriendIsMutual() {
			userService.addFriend(a.getId(), b.getId());

			assertEquals(1, userService.getFriends(a.getId()).size());
			assertEquals(1, userService.getFriends(b.getId()).size());
			assertEquals(b.getId(), userService.getFriends(a.getId()).get(0).getId());
			assertEquals(a.getId(), userService.getFriends(b.getId()).get(0).getId());
		}

		@Test
		@DisplayName("Нельзя добавить в друзья самого себя")
		void cannotAddSelf() {
			assertThrows(ValidationException.class,
					() -> userService.addFriend(a.getId(), a.getId()));
		}

		@Test
		@DisplayName("Удаление из друзей взаимно")
		void removeFriendIsMutual() {
			userService.addFriend(a.getId(), b.getId());
			userService.removeFriend(a.getId(), b.getId());

			assertTrue(userService.getFriends(a.getId()).isEmpty());
			assertTrue(userService.getFriends(b.getId()).isEmpty());
		}

		@Test
		@DisplayName("Повторное добавление в друзья не создаёт дубль")
		void addFriendTwiceKeepsSingleEntry() {
			userService.addFriend(a.getId(), b.getId());
			userService.addFriend(a.getId(), b.getId());

			assertEquals(1, userService.getFriends(a.getId()).size());
		}

		@Test
		@DisplayName("Общие друзья")
		void commonFriends() {
			userService.addFriend(a.getId(), c.getId());
			userService.addFriend(b.getId(), c.getId());

			List<User> common = userService.getCommonFriends(a.getId(), b.getId());
			assertEquals(1, common.size());
			assertEquals(c.getId(), common.get(0).getId());
		}

		@Test
		@DisplayName("Общих друзей нет — пустой список")
		void noCommonFriends() {
			userService.addFriend(a.getId(), c.getId());
			assertTrue(userService.getCommonFriends(a.getId(), b.getId()).isEmpty());
		}

		@Test
		@DisplayName("Добавление в друзья несуществующего пользователя → 404")
		void addUnknownFriend() {
			assertThrows(NotFoundException.class,
					() -> userService.addFriend(a.getId(), 999L));
		}

		@Test
		@DisplayName("Получение друзей несуществующего пользователя → 404")
		void getFriendsOfUnknown() {
			assertThrows(NotFoundException.class,
					() -> userService.getFriends(999L));
		}
	}

	// ---------------- ЛАЙКИ И ПОПУЛЯРНЫЕ ФИЛЬМЫ ----------------

	@Nested
	@DisplayName("Лайки и популярные фильмы")
	class LikesTests {

		private User u1;
		private User u2;
		private Film f1;
		private Film f2;

		@BeforeEach
		void setUp() {
			u1 = userService.add(makeUser("u1@mail.ru", "u1"));
			u2 = userService.add(makeUser("u2@mail.ru", "u2"));
			f1 = filmService.add(makeFilm("F1"));
			f2 = filmService.add(makeFilm("F2"));
		}

		@Test
		@DisplayName("Лайк уникален для пользователя")
		void likeIsUnique() {
			filmService.addLike(f1.getId(), u1.getId());
			filmService.addLike(f1.getId(), u1.getId());

			assertEquals(1, filmService.getById(f1.getId()).getLikes().size());
		}

		@Test
		@DisplayName("Удаление лайка")
		void removeLike() {
			filmService.addLike(f1.getId(), u1.getId());
			filmService.removeLike(f1.getId(), u1.getId());

			assertTrue(filmService.getById(f1.getId()).getLikes().isEmpty());
		}

		@Test
		@DisplayName("Удаление несуществующего лайка — идемпотентно")
		void removeNonExistingLikeIsIdempotent() {
			assertDoesNotThrow(() -> filmService.removeLike(f1.getId(), u1.getId()));
		}

		@Test
		@DisplayName("Лайк несуществующего фильма → 404")
		void likeUnknownFilm() {
			assertThrows(NotFoundException.class,
					() -> filmService.addLike(999L, u1.getId()));
		}

		@Test
		@DisplayName("Лайк несуществующим пользователем → 404")
		void likeByUnknownUser() {
			assertThrows(NotFoundException.class,
					() -> filmService.addLike(f1.getId(), 999L));
		}

		@Test
		@DisplayName("Популярные фильмы сортируются по количеству лайков")
		void popularOrder() {
			filmService.addLike(f2.getId(), u1.getId());
			filmService.addLike(f2.getId(), u2.getId());
			filmService.addLike(f1.getId(), u1.getId());

			List<Film> popular = filmService.getPopular(10);
			assertEquals(2, popular.size());
			assertEquals(f2.getId(), popular.get(0).getId());
			assertEquals(f1.getId(), popular.get(1).getId());
		}

		@Test
		@DisplayName("Популярные фильмы ограничиваются count")
		void popularLimit() {
			// f2 — 2 лайка, f1 — 1 лайк → порядок однозначен
			filmService.addLike(f2.getId(), u1.getId());
			filmService.addLike(f2.getId(), u2.getId());
			filmService.addLike(f1.getId(), u1.getId());

			List<Film> popular = filmService.getPopular(1);
			assertEquals(1, popular.size());
			assertEquals(f2.getId(), popular.get(0).getId());
		}

		@Test
		@DisplayName("count <= 0 → ParameterNotValidException через контроллер")
		void popularInvalidCountViaController() {
			assertThrows(ParameterNotValidException.class,
					() -> filmController.getPopular(0));
			assertThrows(ParameterNotValidException.class,
					() -> filmController.getPopular(-1));
		}
	}
}
package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты DAO-объектов Filmorate.
 * Шапка аннотаций — как в ТЗ.
 * Покрыты все публичные методы UserStorage и FilmStorage.
 */
@JdbcTest
@AutoConfigureTestDatabase
@Import({
		UserDbStorage.class, FilmDbStorage.class,
		UserRowMapper.class, FilmRowMapper.class, GenreRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("Интеграционные тесты хранилищ Filmorate")
class FilmorateApplicationTests {

	private final UserDbStorage userStorage;
	private final FilmDbStorage filmStorage;
	private final JdbcTemplate jdbc;

	@BeforeEach
	void cleanUp() {
		jdbc.update("DELETE FROM friendship");
		jdbc.update("DELETE FROM film_like");
		jdbc.update("DELETE FROM film_genre");
		jdbc.update("DELETE FROM film");
		jdbc.update("DELETE FROM users");
	}

	// =====================================================================
	//                               USER STORAGE
	// =====================================================================

	@Nested
	@DisplayName("UserDbStorage: CRUD-операции")
	class UserCrudTests {

		@Test
		@DisplayName("findById возвращает пользователя по id")
		public void testFindUserById() {
			Long id = userStorage.addUser(newUser("a@b.c", "alice")).getId();

			Optional<User> userOptional = userStorage.findById(id);

			assertThat(userOptional)
					.isPresent()
					.hasValueSatisfying(user ->
							assertThat(user).hasFieldOrPropertyWithValue("id", id)
					);
		}

		@Test
		@DisplayName("addUser сохраняет пользователя и присваивает id")
		public void testAddUser() {
			User saved = userStorage.addUser(newUser("a@b.c", "alice"));
			assertThat(saved.getId()).isPositive();
		}

		@Test
		@DisplayName("findById возвращает пустой Optional для несуществующего id")
		public void testFindUserByIdNotFound() {
			assertThat(userStorage.findById(9999L)).isEmpty();
		}

		@Test
		@DisplayName("findAll возвращает всех пользователей")
		public void testFindAllUsers() {
			userStorage.addUser(newUser("a@b.c", "alice"));
			userStorage.addUser(newUser("d@e.f", "bob"));
			assertThat(userStorage.findAll()).hasSize(2);
		}

		@Test
		@DisplayName("updateUser обновляет существующего пользователя")
		public void testUpdateUser() {
			User saved = userStorage.addUser(newUser("a@b.c", "alice"));
			saved.setEmail("new@b.c");
			userStorage.updateUser(saved);

			assertThat(userStorage.findById(saved.getId()))
					.get()
					.hasFieldOrPropertyWithValue("email", "new@b.c");
		}
	}

	@Nested
	@DisplayName("UserDbStorage: дружба")
	class UserFriendshipTests {

		@Test
		@DisplayName("addFriend создаёт одностороннюю дружбу")
		public void testAddFriendIsOneWay() {
			User u1 = userStorage.addUser(newUser("a@b.c", "alice"));
			User u2 = userStorage.addUser(newUser("d@e.f", "bob"));

			userStorage.addFriend(u1.getId(), u2.getId());

			assertThat(userStorage.findAllFriends(u1.getId()))
					.extracting(User::getLogin)
					.containsExactly("bob");
			assertThat(userStorage.findAllFriends(u2.getId())).isEmpty();
		}

		@Test
		@DisplayName("deleteFriend удаляет только запись отправителя, встречная остаётся")
		public void testDeleteFriendDoesNotAffectOtherDirection() {
			User u1 = userStorage.addUser(newUser("a@b.c", "alice"));
			User u2 = userStorage.addUser(newUser("d@e.f", "bob"));

			userStorage.addFriend(u1.getId(), u2.getId());
			userStorage.addFriend(u2.getId(), u1.getId());

			userStorage.deleteFriend(u1.getId(), u2.getId());

			assertThat(userStorage.findAllFriends(u1.getId())).isEmpty();
			assertThat(userStorage.findAllFriends(u2.getId()))
					.extracting(User::getLogin)
					.containsExactly("alice");
		}

		@Test
		@DisplayName("findCommonFriend находит общих друзей двух пользователей")
		public void testFindCommonFriend() {
			User u1 = userStorage.addUser(newUser("a@b.c", "alice"));
			User u2 = userStorage.addUser(newUser("d@e.f", "bob"));
			User common = userStorage.addUser(newUser("g@h.i", "charlie"));

			userStorage.addFriend(u1.getId(), common.getId());
			userStorage.addFriend(u2.getId(), common.getId());

			assertThat(userStorage.findCommonFriend(u1.getId(), u2.getId()))
					.extracting(User::getLogin)
					.containsExactly("charlie");
		}
	}

	// =====================================================================
	//                               FILM STORAGE
	// =====================================================================

	@Nested
	@DisplayName("FilmDbStorage: CRUD-операции")
	class FilmCrudTests {

		@Test
		@DisplayName("addFilm сохраняет фильм, findById его возвращает")
		public void testAddFilmAndFindById() {
			Film saved = filmStorage.addFilm(newFilm("Film A"));

			Optional<Film> found = filmStorage.findById(saved.getId());

			assertThat(found)
					.isPresent()
					.hasValueSatisfying(f -> assertThat(f)
							.hasFieldOrPropertyWithValue("id", saved.getId())
							.hasFieldOrPropertyWithValue("name", "Film A"));
		}

		@Test
		@DisplayName("findAll возвращает все фильмы")
		public void testFindAllFilms() {
			filmStorage.addFilm(newFilm("A"));
			filmStorage.addFilm(newFilm("B"));
			assertThat(filmStorage.findAll()).hasSize(2);
		}

		@Test
		@DisplayName("updateFilm обновляет существующий фильм")
		public void testUpdateFilm() {
			Film saved = filmStorage.addFilm(newFilm("A"));
			saved.setName("A updated");
			filmStorage.updateFilm(saved);

			assertThat(filmStorage.findById(saved.getId()))
					.get()
					.hasFieldOrPropertyWithValue("name", "A updated");
		}
	}

	@Nested
	@DisplayName("FilmDbStorage: лайки и популярность")
	class FilmLikesTests {

		@Test
		@DisplayName("addLike и removeLike меняют количество лайков в БД")
		public void testAddAndRemoveLike() {
			Film film = filmStorage.addFilm(newFilm("A"));
			User user = userStorage.addUser(newUser("a@b.c", "alice"));

			filmStorage.addLike(film.getId(), user.getId());
			Integer countAfterAdd = jdbc.queryForObject(
					"SELECT COUNT(*) FROM film_like WHERE film_id=? AND user_id=?",
					Integer.class, film.getId(), user.getId());
			assertThat(countAfterAdd).isEqualTo(1);

			filmStorage.removeLike(film.getId(), user.getId());
			Integer countAfterRemove = jdbc.queryForObject(
					"SELECT COUNT(*) FROM film_like WHERE film_id=? AND user_id=?",
					Integer.class, film.getId(), user.getId());
			assertThat(countAfterRemove).isZero();
		}

		@Test
		@DisplayName("getPopular сортирует фильмы по убыванию лайков")
		public void testGetPopular() {
			Film f1 = filmStorage.addFilm(newFilm("A"));
			Film f2 = filmStorage.addFilm(newFilm("B"));
			User u1 = userStorage.addUser(newUser("a@b.c", "alice"));
			User u2 = userStorage.addUser(newUser("d@e.f", "bob"));

			filmStorage.addLike(f1.getId(), u1.getId());
			filmStorage.addLike(f2.getId(), u1.getId());
			filmStorage.addLike(f2.getId(), u2.getId());

			List<Film> popular = List.copyOf(filmStorage.getPopular(10));

			assertThat(popular)
					.extracting(Film::getName)
					.containsExactly("B", "A");
		}
	}

	// =====================================================================
	//                               ФАБРИКИ
	// =====================================================================

	private User newUser(String email, String login) {
		User u = new User();
		u.setEmail(email);
		u.setLogin(login);
		u.setName(login);
		u.setBirthday(LocalDate.of(1990, 1, 1));
		return u;
	}

	private Film newFilm(String name) {
		Film f = new Film();
		f.setName(name);
		f.setDescription("desc");
		f.setReleaseDate(LocalDate.of(2000, 1, 1));
		f.setDuration(90);
		MpaRating mpa = new MpaRating();
		mpa.setId(1L);
		f.setMpa(mpa);
		return f;
	}
}
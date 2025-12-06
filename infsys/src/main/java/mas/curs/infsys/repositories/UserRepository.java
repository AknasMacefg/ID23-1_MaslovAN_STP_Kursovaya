package mas.curs.infsys.repositories;

import mas.curs.infsys.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Репозиторий доступа к данным сущности {@link User}.
 * <p>
 * Наследуется от {@link JpaRepository}, предоставляя стандартные CRUD-операции,
 * а также содержит дополнительные методы поиска по email.
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Ищет пользователя по адресу электронной почты.
     *
     * @param email адрес электронной почты
     * @return обёртка {@link Optional} с {@link User}, если найден; иначе пустая обёртка
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверяет, существует ли пользователь с указанным email.
     *
     * @param email адрес электронной почты
     * @return {@code true}, если пользователь существует; {@code false} — если нет
     */
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.UserWishlist uw " +
           "LEFT JOIN FETCH uw.book " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithWishlist(@Param("id") Long id);
}
package mas.curs.infsys.repositories;

import mas.curs.infsys.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.UserWishlist uw " +
           "LEFT JOIN FETCH uw.book " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithWishlist(@Param("id") Long id);
}
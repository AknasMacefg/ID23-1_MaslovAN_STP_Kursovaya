package mas.curs.infsys.repositories;

import mas.curs.infsys.models.UserWishlist;
import mas.curs.infsys.models.UserWishlistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserWishlistRepository extends JpaRepository<UserWishlist, UserWishlistId> {
}


package mas.curs.infsys.repositories;

import mas.curs.infsys.models.BookGenre;
import mas.curs.infsys.models.BookGenreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookGenreRepository extends JpaRepository<BookGenre, BookGenreId> {
}



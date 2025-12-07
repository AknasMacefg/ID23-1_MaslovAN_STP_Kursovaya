package mas.curs.infsys.repositories;

import mas.curs.infsys.models.BookAuthor;
import mas.curs.infsys.models.BookAuthorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookAuthorRepository extends JpaRepository<BookAuthor, BookAuthorId> {
}



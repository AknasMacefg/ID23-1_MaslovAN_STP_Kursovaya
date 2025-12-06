package mas.curs.infsys.repositories;

import mas.curs.infsys.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    @Query("SELECT DISTINCT b FROM Book b " +
           "LEFT JOIN FETCH b.BookAuthor ba " +
           "LEFT JOIN FETCH ba.author " +
           "LEFT JOIN FETCH b.BookGenre bg " +
           "LEFT JOIN FETCH bg.genre " +
           "LEFT JOIN FETCH b.BookSeries bs " +
           "LEFT JOIN FETCH bs.series " +
           "WHERE b.id = :id")
    Optional<Book> findById(@Param("id") Long id);
}


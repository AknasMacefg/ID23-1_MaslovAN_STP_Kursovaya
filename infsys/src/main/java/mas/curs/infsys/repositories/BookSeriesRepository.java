package mas.curs.infsys.repositories;

import mas.curs.infsys.models.BookSeries;
import mas.curs.infsys.models.BookSeriesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookSeriesRepository extends JpaRepository<BookSeries, BookSeriesId> {
}



package mas.curs.infsys.models;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table (name = "book_series")
public class BookSeries {
    @EmbeddedId
    private BookSeriesId id;

    @ManyToOne
    @MapsId("seriesId")
    @JoinColumn(name = "series_id")
    private Series series;

    @ManyToOne
    @MapsId("bookId")
    @JoinColumn(name = "book_id")
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Book book;

    public BookSeries() {}

    public BookSeries(Series series, Book book) {
        this.series = series;
        this.book = book;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public BookSeriesId getId() {
        return id;
    }

    public void setId(BookSeriesId id) {
        this.id = id;
    }
}

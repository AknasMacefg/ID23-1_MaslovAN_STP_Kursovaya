package mas.curs.infsys.models;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class BookSeriesId implements Serializable {
    private Long seriesId;
    private Long bookId;

    public BookSeriesId() {}

    public BookSeriesId(Long seriesId, Long bookId) {
        this.seriesId = seriesId;
        this.bookId = bookId;
    }

    public Long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Long seriesId) {
        this.seriesId = seriesId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookSeriesId that = (BookSeriesId) o;
        return seriesId != null && seriesId.equals(that.seriesId) &&
               bookId != null && bookId.equals(that.bookId);
    }

    @Override
    public int hashCode() {
        return (seriesId != null ? seriesId.hashCode() : 0) + 
               (bookId != null ? bookId.hashCode() : 0);
    }
}










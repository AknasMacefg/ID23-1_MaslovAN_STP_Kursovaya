package mas.curs.infsys.models;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class BookGenreId implements Serializable {
    private Long genreId;
    private Long bookId;

    public BookGenreId() {}

    public BookGenreId(Long genreId, Long bookId) {
        this.genreId = genreId;
        this.bookId = bookId;
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
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
        BookGenreId that = (BookGenreId) o;
        return genreId != null && genreId.equals(that.genreId) &&
               bookId != null && bookId.equals(that.bookId);
    }

    @Override
    public int hashCode() {
        return (genreId != null ? genreId.hashCode() : 0) + 
               (bookId != null ? bookId.hashCode() : 0);
    }
}










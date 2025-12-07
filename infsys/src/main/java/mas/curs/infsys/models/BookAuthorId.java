package mas.curs.infsys.models;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class BookAuthorId implements Serializable {
    private Long authorId;
    private Long bookId;

    public BookAuthorId() {}

    public BookAuthorId(Long authorId, Long bookId) {
        this.authorId = authorId;
        this.bookId = bookId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
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
        BookAuthorId that = (BookAuthorId) o;
        return authorId != null && authorId.equals(that.authorId) &&
               bookId != null && bookId.equals(that.bookId);
    }

    @Override
    public int hashCode() {
        return (authorId != null ? authorId.hashCode() : 0) + 
               (bookId != null ? bookId.hashCode() : 0);
    }
}










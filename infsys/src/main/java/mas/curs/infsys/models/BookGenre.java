package mas.curs.infsys.models;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table (name = "book_genre")
public class BookGenre {
    @EmbeddedId
    private BookGenreId id;

    @ManyToOne
    @MapsId("genreId")
    @JoinColumn(name = "genre_id")
    private Genre genre;

    @ManyToOne
    @MapsId("bookId")
    @JoinColumn(name = "book_id")
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Book book;

    public BookGenre() {}

    public BookGenre(Genre genre, Book book) {
        this.genre = genre;
        this.book = book;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public BookGenreId getId() {
        return id;
    }

    public void setId(BookGenreId id) {
        this.id = id;
    }
}

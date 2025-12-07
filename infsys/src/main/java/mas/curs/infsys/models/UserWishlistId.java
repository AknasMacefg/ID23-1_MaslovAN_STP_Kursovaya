package mas.curs.infsys.models;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class UserWishlistId implements Serializable {
    private Long userId;
    private Long bookId;

    public UserWishlistId() {}

    public UserWishlistId(Long userId, Long bookId) {
        this.userId = userId;
        this.bookId = bookId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
        UserWishlistId that = (UserWishlistId) o;
        return userId != null && userId.equals(that.userId) &&
               bookId != null && bookId.equals(that.bookId);
    }

    @Override
    public int hashCode() {
        return (userId != null ? userId.hashCode() : 0) + 
               (bookId != null ? bookId.hashCode() : 0);
    }
}



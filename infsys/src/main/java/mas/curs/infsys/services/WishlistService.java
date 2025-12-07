package mas.curs.infsys.services;

import mas.curs.infsys.models.*;
import mas.curs.infsys.repositories.BookRepository;
import mas.curs.infsys.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Transactional
    public boolean addToWishlist(Long userId, Long bookId) {
        User user = userRepository.findById(userId).orElse(null);
        Book book = bookRepository.findById(bookId).orElse(null);

        if (user == null || book == null) {
            return false;
        }

        // Check if already in wishlist
        boolean alreadyExists = user.getUserWishlist().stream()
            .anyMatch(wl -> wl.getBook().getId().equals(bookId));

        if (alreadyExists) {
            return false; // Already in wishlist
        }

        UserWishlist wishlistItem = new UserWishlist(user, book, LocalDate.now());
        UserWishlistId id = new UserWishlistId(userId, bookId);
        wishlistItem.setId(id);
        user.getUserWishlist().add(wishlistItem);
        userRepository.save(user);
        return true;
    }

    @Transactional
    public boolean removeFromWishlist(Long userId, Long bookId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        boolean removed = user.getUserWishlist().removeIf(
            wl -> wl.getBook().getId().equals(bookId)
        );

        if (removed) {
            userRepository.save(user);
        }
        return removed;
    }

    public boolean isInWishlist(Long userId, Long bookId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        return user.getUserWishlist().stream()
            .anyMatch(wl -> wl.getBook().getId().equals(bookId));
    }

    public List<UserWishlist> getAllWishlistItems() {
        List<User> users = userRepository.findAll();
        // Filter to only include users with role USER or ADMIN (for testing)
        return users.stream()
            .filter(user -> user.getRole() == Role.USER || 
                           user.getRole() == Role.ADMIN)
            .flatMap(user -> user.getUserWishlist().stream())
            .collect(Collectors.toList());
    }

    public Map<String, Long> getAuthorWishlistStats() {
        List<UserWishlist> wishlistItems = getAllWishlistItems();
        Map<String, Long> authorCounts = new LinkedHashMap<>();
        
        for (UserWishlist item : wishlistItems) {
            Book book = item.getBook();
            if (book != null && book.getBookAuthor() != null) {
                for (BookAuthor bookAuthor : book.getBookAuthor()) {
                    if (bookAuthor.getAuthor() != null) {
                        String authorName = buildAuthorName(bookAuthor.getAuthor());
                        authorCounts.put(authorName, authorCounts.getOrDefault(authorName, 0L) + 1);
                    }
                }
            }
        }
        
        // Sort by count descending and limit to top 10
        return authorCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    public Map<String, Long> getGenreWishlistStats() {
        List<UserWishlist> wishlistItems = getAllWishlistItems();
        Map<String, Long> genreCounts = new LinkedHashMap<>();
        
        for (UserWishlist item : wishlistItems) {
            Book book = item.getBook();
            if (book != null && book.getBookGenre() != null) {
                for (BookGenre bookGenre : book.getBookGenre()) {
                    if (bookGenre.getGenre() != null) {
                        String genreName = bookGenre.getGenre().getName();
                        genreCounts.put(genreName, genreCounts.getOrDefault(genreName, 0L) + 1);
                    }
                }
            }
        }
        
        // Sort by count descending and limit to top 10
        return genreCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    public Map<String, Long> getWishlistTimelineStats() {
        List<UserWishlist> wishlistItems = getAllWishlistItems();
        Map<String, Long> timelineCounts = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        
        // Group by date
        for (UserWishlist item : wishlistItems) {
            if (item.getAdded_at() != null) {
                String dateStr = item.getAdded_at().format(formatter);
                timelineCounts.put(dateStr, timelineCounts.getOrDefault(dateStr, 0L) + 1);
            }
        }
        
        // Sort by date ascending
        return timelineCounts.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    private String buildAuthorName(Author author) {
        StringBuilder name = new StringBuilder();
        if (author.getFirstname() != null) {
            name.append(author.getFirstname());
        }
        if (author.getMiddlename() != null && !author.getMiddlename().isEmpty()) {
            if (name.length() > 0) name.append(" ");
            name.append(author.getMiddlename());
        }
        if (author.getLastname() != null && !author.getLastname().isEmpty()) {
            if (name.length() > 0) name.append(" ");
            name.append(author.getLastname());
        }
        return name.length() > 0 ? name.toString() : "Неизвестный автор";
    }
}


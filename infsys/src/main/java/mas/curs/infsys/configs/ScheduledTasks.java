package mas.curs.infsys.configs;

import mas.curs.infsys.models.Book;
import mas.curs.infsys.models.BookStatus;
import mas.curs.infsys.models.User;
import mas.curs.infsys.services.BookService;
import mas.curs.infsys.services.EmailService;
import mas.curs.infsys.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ScheduledTasks {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private EmailService emailService;

    /**
     * Runs every 24 hours at 2 AM
     * - Sends deletion warnings to users who will be deleted in one month
     * - Deletes users whose last logout was more than a year ago
     * - Updates book statuses from SOON to RELEASED if release_date has passed
     * - Sends email notifications to users who have released books in their wishlist
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyMaintenance() {
        sendDeletionWarnings();
        deleteOldUsers();
        updateBookStatusesAndNotify();
    }

    /**
     * Sends email warnings to users whose accounts will be deleted in one month
     * (users whose last logout was 11 months ago)
     */
    private void sendDeletionWarnings() {
        LocalDateTime elevenMonthsAgo = LocalDateTime.now().minusMonths(11);
        LocalDateTime elevenMonthsAndOneDayAgo = LocalDateTime.now().minusMonths(11).minusDays(1);
        List<User> users = userService.getAllUsers();

        for (User user : users) {
            if (user.getLogout_at() != null 
                    && user.isEmail_notification()
                    && user.getLogout_at().isBefore(elevenMonthsAgo)
                    && user.getLogout_at().isAfter(elevenMonthsAndOneDayAgo)) {
                try {
                    emailService.sendDeletionWarning(user.getEmail(), user.getUsername());
                    System.out.println("Sent deletion warning to user: " + user.getUsername() + " (last logout: " + user.getLogout_at() + ")");
                } catch (Exception e) {
                    System.err.println("Failed to send deletion warning to user " + user.getUsername() + ": " + e.getMessage());
                }
            }
        }
    }

    private void deleteOldUsers() {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        List<User> users = userService.getAllUsers();

        for (User user : users) {
            if (user.getLogout_at() != null && user.getLogout_at().isBefore(oneYearAgo)) {
                try {
                    userService.deleteUser(user.getId());
                    System.out.println("Deleted user: " + user.getUsername() + " (last logout: " + user.getLogout_at() + ")");
                } catch (Exception e) {
                    System.err.println("Failed to delete user " + user.getUsername() + ": " + e.getMessage());
                }
            }
        }
    }

    private void updateBookStatusesAndNotify() {
        List<Book> updatedBooks = bookService.updateBookStatuses();

        for (Book book : updatedBooks) {
            if (book.getStatus() == BookStatus.RELEASED) {
                // Find all users who have this book in their wishlist
                List<User> users = userService.getAllUsers();
                for (User user : users) {
                    if (user.getUserWishlist() != null && user.isEmail_notification()) {
                        boolean hasBookInWishlist = user.getUserWishlist().stream()
                            .anyMatch(wl -> wl.getBook() != null && wl.getBook().getId().equals(book.getId()));

                        if (hasBookInWishlist) {
                            String bookUrl = "http://localhost:8080/books/view/" + book.getId();
                            emailService.sendBookReleaseNotification(user.getEmail(), book.getTitle(), bookUrl);
                        }
                    }
                }
            }
        }
    }
}


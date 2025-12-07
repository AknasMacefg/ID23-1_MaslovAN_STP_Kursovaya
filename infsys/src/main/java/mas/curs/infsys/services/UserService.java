package mas.curs.infsys.services;

import mas.curs.infsys.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import mas.curs.infsys.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private mas.curs.infsys.repositories.UserWishlistRepository userWishlistRepository;

    public List<User> getAllUsers()
    {
        return userRepository.findAll();
    }

    public List<User> getUsersFilteredAndSorted(String role, String sortBy, String search) {
        List<User> users = userRepository.findAll();
        
        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            users = users.stream()
                .filter(user -> {
                    if (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchLower)) return true;
                    if (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchLower)) return true;
                    return false;
                })
                .collect(Collectors.toList());
        }
        
        // Apply filter
        if (role != null && !role.isEmpty()) {
            users = users.stream()
                .filter(user -> user.getRole() != null && user.getRole().toString().equalsIgnoreCase(role))
                .collect(Collectors.toList());
        }
        
        // Apply sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy) {
                case "username":
                    users.sort(Comparator.comparing(User::getUsername, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
                    break;
                case "username_desc":
                    users.sort(Comparator.comparing(User::getUsername, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)).reversed());
                    break;
                case "email":
                    users.sort(Comparator.comparing(User::getEmail, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
                    break;
                case "email_desc":
                    users.sort(Comparator.comparing(User::getEmail, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)).reversed());
                    break;
                case "role":
                    users.sort(Comparator.comparing(User::getRole, Comparator.nullsLast(Comparator.naturalOrder())));
                    break;
                case "role_desc":
                    users.sort(Comparator.comparing(User::getRole, Comparator.nullsFirst(Comparator.reverseOrder())));
                    break;
                case "created_at":
                    users.sort(Comparator.comparing(User::getCreated_at, Comparator.nullsLast(Comparator.naturalOrder())));
                    break;
                case "created_at_desc":
                    users.sort(Comparator.comparing(User::getCreated_at, Comparator.nullsFirst(Comparator.reverseOrder())));
                    break;
                case "updated_at":
                    users.sort(Comparator.comparing(User::getUpdated_at, Comparator.nullsLast(Comparator.naturalOrder())));
                    break;
                case "updated_at_desc":
                    users.sort(Comparator.comparing(User::getUpdated_at, Comparator.nullsFirst(Comparator.reverseOrder())));
                    break;
                case "log_in_at":
                    users.sort(Comparator.comparing(User::getLog_in_at, Comparator.nullsLast(Comparator.naturalOrder())));
                    break;
                case "log_in_at_desc":
                    users.sort(Comparator.comparing(User::getLog_in_at, Comparator.nullsFirst(Comparator.reverseOrder())));
                    break;
                case "logout_at":
                    users.sort(Comparator.comparing(User::getLogout_at, Comparator.nullsLast(Comparator.naturalOrder())));
                    break;
                case "logout_at_desc":
                    users.sort(Comparator.comparing(User::getLogout_at, Comparator.nullsFirst(Comparator.reverseOrder())));
                    break;
            }
        }
        
        return users;
    }

    public User getUserById(long id) {
        return userRepository.findById(id).get();
    }

    public User getUserByIdWithWishlist(long id) {
        return userRepository.findByIdWithWishlist(id).orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public boolean existsUserByEmail (String email) { return userRepository.existsByEmail(email); }
    public boolean isEmpty(){
        return userRepository.count() == 0;
    }
    public boolean register(User user) {
        if (user == null || user.getEmail() == null || user.getUsername() == null || user.getPassword() == null) {
            return false;
        }
        if (userRepository.existsByEmail(user.getEmail()) || userRepository.existsByUsername(user.getUsername())) {
            return false;
        }
        // Шифруем сырой пароль перед сохранением
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setCreated_at(LocalDateTime.now());

        userRepository.save(user);
        return true;
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // Delete all UserWishlist entries for this user using repository
            List<mas.curs.infsys.models.UserWishlist> wishlistItems = userWishlistRepository.findAll().stream()
                .filter(wl -> wl.getUser() != null && wl.getUser().getId().equals(userId))
                .collect(java.util.stream.Collectors.toList());
            userWishlistRepository.deleteAll(wishlistItems);
            
            userRepository.delete(user);
        }
    }

    public void addUser(User user) {
        user.setUpdated_at(LocalDateTime.now());
        userRepository.save(user);
    }

    @org.springframework.transaction.annotation.Transactional
    public void updateEmailNotification(Long userId, boolean emailNotification) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setEmail_notification(emailNotification);
            user.setUpdated_at(LocalDateTime.now());
            userRepository.saveAndFlush(user);
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        // Encode and set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdated_at(LocalDateTime.now());
        userRepository.saveAndFlush(user);
        return true;
    }

    public void updateLoginTime(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setLog_in_at(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public void updateLogoutTime(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setLogout_at(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}

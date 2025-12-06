package mas.curs.infsys.controllers;
import mas.curs.infsys.models.Book;
import mas.curs.infsys.models.User;
import mas.curs.infsys.services.BookService;
import mas.curs.infsys.services.AuthorService;
import mas.curs.infsys.services.GenreService;
import mas.curs.infsys.services.SeriesService;
import mas.curs.infsys.services.WishlistService;
import mas.curs.infsys.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;



@Controller
@RequestMapping("/books")
public class BookController {

    /** Репозиторий пользователей, обеспечивающий доступ к данным. */
    private final BookService bookService;
    private final AuthorService authorService;
    private final GenreService genreService;
    private final SeriesService seriesService;
    private final WishlistService wishlistService;
    private final UserService userService;

    /**
     * Конструктор контроллера пользователей.
     *
     * @param bookService сервис книг
     * @param authorService сервис авторов
     * @param genreService сервис жанров
     * @param seriesService сервис серий
     * @param wishlistService сервис списка желаний
     * @param userService сервис пользователей
     */
    public BookController(BookService bookService, AuthorService authorService, 
                         GenreService genreService, SeriesService seriesService,
                         WishlistService wishlistService, UserService userService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.genreService = genreService;
        this.seriesService = seriesService;
        this.wishlistService = wishlistService;
        this.userService = userService;
    }

    /**
     * Отображает панель управления пользователями.
     *
     * @param model объект {@link Model} для передачи данных в шаблон (список пользователей и сообщения)
     * @param msg необязательное сообщение (используется для отображения статуса операции)
     * @return имя Thymeleaf-шаблона страницы пользователей ({@code users})
     */
    @GetMapping
    public String bookPage(Model model, 
                          @RequestParam(required = false) String msg,
                          @RequestParam(required = false) List<Long> genreIds,
                          @RequestParam(required = false) List<String> languages,
                          @RequestParam(required = false) Boolean adultCheck,
                          @RequestParam(required = false) String sortBy,
                          @RequestParam(required = false) String search,
                          @RequestParam(defaultValue = "0") int page) {
        int pageSize = 50;
        int totalPages = bookService.getTotalPages(genreIds, languages, adultCheck, sortBy, search, pageSize);
        List<Book> allBooks = bookService.getBooksFilteredAndSorted(genreIds, languages, adultCheck, sortBy, search, 0, Integer.MAX_VALUE);
        
        model.addAttribute("books", bookService.getBooksFilteredAndSorted(genreIds, languages, adultCheck, sortBy, search, page, pageSize));
        model.addAttribute("allGenres", genreService.getAllGenres());
        model.addAttribute("allLanguages", mas.curs.infsys.models.Language.values());
        model.addAttribute("selectedGenreIds", genreIds != null ? genreIds : java.util.Collections.emptyList());
        model.addAttribute("selectedLanguages", languages != null ? languages : java.util.Collections.emptyList());
        model.addAttribute("selectedAdultCheck", adultCheck);
        model.addAttribute("selectedSortBy", sortBy);
        model.addAttribute("searchQuery", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", allBooks.size());
        model.addAttribute("showPagination", allBooks.size() > pageSize);
        model.addAttribute("message", msg);
        return "books";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("books", book);
        model.addAttribute("allAuthors", authorService.getAllAuthors());
        model.addAttribute("allGenres", genreService.getAllGenres());
        model.addAttribute("allSeries", seriesService.getAllSeriess());
        
        // Get currently selected IDs
        List<Long> selectedAuthorIds = book.getBookAuthor().stream()
            .map(ba -> ba.getAuthor().getId())
            .collect(Collectors.toList());
        List<Long> selectedGenreIds = book.getBookGenre().stream()
            .map(bg -> bg.getGenre().getId())
            .collect(Collectors.toList());
        List<Long> selectedSeriesIds = book.getBookSeries().stream()
            .map(bs -> bs.getSeries().getId())
            .collect(Collectors.toList());
        
        // Get main author ID
        Long mainAuthorId = book.getBookAuthor().stream()
            .filter(ba -> ba.getMain_author())
            .map(ba -> ba.getAuthor().getId())
            .findFirst()
            .orElse(null);
        
        model.addAttribute("selectedAuthorIds", selectedAuthorIds);
        model.addAttribute("selectedGenreIds", selectedGenreIds);
        model.addAttribute("selectedSeriesIds", selectedSeriesIds);
        model.addAttribute("mainAuthorId", mainAuthorId);
        model.addAttribute("allLanguages", mas.curs.infsys.models.Language.values());
        model.addAttribute("allStatuses", mas.curs.infsys.models.BookStatus.values());
        
        return "edit-book";
    }

    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable("id") Long id,
                               @ModelAttribute("books") Book book,
                               @RequestParam("image") MultipartFile file,
                               @RequestParam(value = "authorIds", required = false) List<Long> authorIds,
                               @RequestParam(value = "mainAuthorId", required = false) Long mainAuthorId,
                               @RequestParam(value = "genreIds", required = false) List<Long> genreIds,
                               @RequestParam(value = "seriesIds", required = false) List<Long> seriesIds,
                               @RequestParam(value = "language", required = false) String languageStr,
                               @RequestParam(value = "status", required = false) String statusStr,
                               RedirectAttributes redirectAttributes) {

        try {
            // Handle file upload
            if (file != null && !file.isEmpty()) {
                String fileName = saveImage(file);
                book.setImage_url("/images/books/" + fileName);
            } else if (book.getImage_url() == null || book.getImage_url().isEmpty()) {
                // Keep existing photo if no new file uploaded
                Book existingBook = bookService.getBookById(id);
                if (existingBook != null) {
                    book.setImage_url(existingBook.getImage_url());
                }
            }

            deleteOldImage(bookService.getBookById(id).getImage_url());

            // Convert language string to enum - keep existing if invalid
            if (languageStr != null && !languageStr.isEmpty()) {
                try {
                    book.setLanguage(mas.curs.infsys.models.Language.valueOf(languageStr));
                } catch (IllegalArgumentException e) {
                    // Keep existing language if invalid value provided
                    Book existingBook = bookService.getBookById(id);
                    if (existingBook != null && existingBook.getLanguage() != null) {
                        book.setLanguage(existingBook.getLanguage());
                    }
                }
            } else {
                // Keep existing language if empty
                Book existingBook = bookService.getBookById(id);
                if (existingBook != null && existingBook.getLanguage() != null) {
                    book.setLanguage(existingBook.getLanguage());
                }
            }

            // Convert status string to enum
            if (statusStr != null && !statusStr.isEmpty()) {
                try {
                    book.setStatus(mas.curs.infsys.models.BookStatus.valueOf(statusStr));
                } catch (IllegalArgumentException e) {
                    // Keep existing status if invalid value provided
                    Book existingBook = bookService.getBookById(id);
                    if (existingBook != null && existingBook.getStatus() != null) {
                        book.setStatus(existingBook.getStatus());
                    } else {
                        book.setStatus(mas.curs.infsys.models.BookStatus.SOON);
                    }
                }
            } else {
                // Keep existing status if empty
                Book existingBook = bookService.getBookById(id);
                if (existingBook != null && existingBook.getStatus() != null) {
                    book.setStatus(existingBook.getStatus());
                } else {
                    book.setStatus(mas.curs.infsys.models.BookStatus.SOON);
                }
            }

            book.setId(id);
            boolean success = bookService.updateBook(book);

            // Update relationships
            if (success) {
                bookService.setBookAuthors(id, authorIds, mainAuthorId);
                bookService.setBookGenres(id, genreIds);
                bookService.setBookSeries(id, seriesIds);
                
                redirectAttributes.addFlashAttribute("success", "Книга успешно обновлена");
                return "redirect:/books/view/" + id;
            } else {
                redirectAttributes.addFlashAttribute("error", "Произошла ошибка при обновлении");
                return "redirect:/books/edit/" + id;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка загрузки изображения: " + e.getMessage());
            return "redirect:/books/edit/" + id;
        }
    }

    @GetMapping("/view/{id}")
    public String showViewForm(@PathVariable("id") Long id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("books", book);
        
        // Check if book is in current user's wishlist
        User currentUser = getCurrentUser();
        boolean inWishlist = false;
        if (currentUser != null) {
            inWishlist = wishlistService.isInWishlist(currentUser.getId(), id);
        }
        model.addAttribute("inWishlist", inWishlist);
        model.addAttribute("currentUser", currentUser);
        
        return "view-book";
    }

    @PostMapping("/view/{id}/wishlist/add")
    public String addToWishlist(@PathVariable("id") Long bookId, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Необходимо войти в систему");
            return "redirect:/login";
        }

        boolean success = wishlistService.addToWishlist(currentUser.getId(), bookId);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Книга добавлена в список желаний");
        } else {
            redirectAttributes.addFlashAttribute("error", "Книга уже в списке желаний или произошла ошибка");
        }
        return "redirect:/books/view/" + bookId;
    }

    @PostMapping("/view/{id}/wishlist/remove")
    public String removeFromWishlist(@PathVariable("id") Long bookId, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Необходимо войти в систему");
            return "redirect:/login";
        }

        boolean success = wishlistService.removeFromWishlist(currentUser.getId(), bookId);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Книга удалена из списка желаний");
        } else {
            redirectAttributes.addFlashAttribute("error", "Произошла ошибка при удалении");
        }
        return "redirect:/books/view/" + bookId;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            try {
                String email = authentication.getName();
                return userService.getUserByEmail(email);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @GetMapping("/edit/new")
    public String showNewBookForm(Model model) {
        model.addAttribute("books", new Book());
        model.addAttribute("allAuthors", authorService.getAllAuthors());
        model.addAttribute("allGenres", genreService.getAllGenres());
        model.addAttribute("allSeries", seriesService.getAllSeriess());
        model.addAttribute("allLanguages", mas.curs.infsys.models.Language.values());
        model.addAttribute("allStatuses", mas.curs.infsys.models.BookStatus.values());
        model.addAttribute("selectedAuthorIds", java.util.Collections.emptyList());
        model.addAttribute("selectedGenreIds", java.util.Collections.emptyList());
        model.addAttribute("selectedSeriesIds", java.util.Collections.emptyList());
        model.addAttribute("mainAuthorId", null);
        return "edit-book";
    }

    @PostMapping("/edit/new")
    public String addBook(@ModelAttribute("books") Book book,
                            @RequestParam("image") MultipartFile file,
                            @RequestParam(value = "authorIds", required = false) List<Long> authorIds,
                            @RequestParam(value = "mainAuthorId", required = false) Long mainAuthorId,
                            @RequestParam(value = "genreIds", required = false) List<Long> genreIds,
                            @RequestParam(value = "seriesIds", required = false) List<Long> seriesIds,
                            @RequestParam(value = "language", required = false) String languageStr,
                            @RequestParam(value = "status", required = false) String statusStr,
                            RedirectAttributes redirectAttributes) {
        try {
            String fileName;
            // Handle file upload for new book
            if (file != null && !file.isEmpty()) {
                fileName = saveImage(file);

            }
            else {
                fileName = "default.jpg";
            }
            book.setImage_url("/images/books/" + fileName);
            
            // Convert language string to enum - set default if invalid
            if (languageStr != null && !languageStr.isEmpty()) {
                try {
                    book.setLanguage(mas.curs.infsys.models.Language.valueOf(languageStr));
                } catch (IllegalArgumentException e) {
                    // Set default language if invalid value provided
                    book.setLanguage(mas.curs.infsys.models.Language.RUSSIAN);
                }
            } else {
                // Set default language if empty
                book.setLanguage(mas.curs.infsys.models.Language.RUSSIAN);
            }
            
            // Convert status string to enum - set default if invalid
            if (statusStr != null && !statusStr.isEmpty()) {
                try {
                    book.setStatus(mas.curs.infsys.models.BookStatus.valueOf(statusStr));
                } catch (IllegalArgumentException e) {
                    book.setStatus(mas.curs.infsys.models.BookStatus.SOON);
                }
            } else {
                book.setStatus(mas.curs.infsys.models.BookStatus.SOON);
            }
            
            Book savedBook = bookService.addBookAndReturn(book);

            if (savedBook != null && savedBook.getId() != null) {
                // Update relationships after book is saved (so it has an ID)
                Long bookId = savedBook.getId();
                bookService.setBookAuthors(bookId, authorIds, mainAuthorId);
                bookService.setBookGenres(bookId, genreIds);
                bookService.setBookSeries(bookId, seriesIds);
                
                redirectAttributes.addFlashAttribute("success", "Книга успешно создана");
                return "redirect:/books";
            } else {
                redirectAttributes.addFlashAttribute("error", "Произошла ошибка при создании");
                return "redirect:/books/edit/new";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка загрузки изображения: " + e.getMessage());
            return "redirect:/books/edit/new";
        }
    }


    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id) {
        bookService.deleteBook(id);
        return "redirect:/books";
    }


    private String saveImage(MultipartFile file) throws IOException {
        // Generate unique filename
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";

        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + fileExtension;

        // Create upload directory if it doesn't exist
        String UPLOAD_DIR = "src/main/resources/static/images/books/";
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    private void deleteOldImage(String oldImagePath) {
        if (oldImagePath != null && !oldImagePath.isEmpty() && !oldImagePath.equals("/images/books/default.jpg")) {
            try {
                Path oldFilePath = Paths.get("src/main/resources/static" + oldImagePath);
                Files.deleteIfExists(oldFilePath);
            } catch (IOException e) {
                // Log the error but don't fail the operation
                e.printStackTrace();
            }
        }
    }

}


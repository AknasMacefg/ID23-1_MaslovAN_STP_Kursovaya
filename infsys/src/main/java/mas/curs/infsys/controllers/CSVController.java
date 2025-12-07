package mas.curs.infsys.controllers;

import mas.curs.infsys.models.*;
import mas.curs.infsys.services.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/csv")
@PreAuthorize("hasRole('ADMIN')")
public class CSVController {

    @Autowired
    private BookService bookService;
    
    @Autowired
    private AuthorService authorService;
    
    @Autowired
    private GenreService genreService;
    
    @Autowired
    private SeriesService seriesService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    
    @Autowired
    private mas.curs.infsys.repositories.BookAuthorRepository bookAuthorRepository;
    
    @Autowired
    private mas.curs.infsys.repositories.BookGenreRepository bookGenreRepository;
    
    @Autowired
    private mas.curs.infsys.repositories.BookSeriesRepository bookSeriesRepository;
    
    @Autowired
    private mas.curs.infsys.repositories.UserWishlistRepository userWishlistRepository;

    @GetMapping
    public String csvPage(Model model) {
        return "csv-management";
    }

    // Export methods
    @GetMapping("/export/books")
    public ResponseEntity<byte[]> exportBooks() throws IOException {
        List<Book> books = bookService.getAllBooks();
        return createCSVResponse(convertBooksToCSV(books), "books.csv");
    }

    @GetMapping("/export/authors")
    public ResponseEntity<byte[]> exportAuthors() throws IOException {
        List<Author> authors = authorService.getAllAuthors();
        return createCSVResponse(convertAuthorsToCSV(authors), "authors.csv");
    }

    @GetMapping("/export/genres")
    public ResponseEntity<byte[]> exportGenres() throws IOException {
        List<Genre> genres = genreService.getAllGenres();
        return createCSVResponse(convertGenresToCSV(genres), "genres.csv");
    }

    @GetMapping("/export/series")
    public ResponseEntity<byte[]> exportSeries() throws IOException {
        List<Series> series = seriesService.getAllSeriess();
        return createCSVResponse(convertSeriesToCSV(series), "series.csv");
    }

    @GetMapping("/export/users")
    public ResponseEntity<byte[]> exportUsers() throws IOException {
        List<User> users = userService.getAllUsers();
        return createCSVResponse(convertUsersToCSV(users), "users.csv");
    }

    @GetMapping("/export/book-authors")
    public ResponseEntity<byte[]> exportBookAuthors() throws IOException {
        List<BookAuthor> bookAuthors = bookAuthorRepository.findAll();
        return createCSVResponse(convertBookAuthorsToCSV(bookAuthors), "book_authors.csv");
    }

    @GetMapping("/export/book-genres")
    public ResponseEntity<byte[]> exportBookGenres() throws IOException {
        List<BookGenre> bookGenres = bookGenreRepository.findAll();
        return createCSVResponse(convertBookGenresToCSV(bookGenres), "book_genres.csv");
    }

    @GetMapping("/export/book-series")
    public ResponseEntity<byte[]> exportBookSeries() throws IOException {
        List<BookSeries> bookSeries = bookSeriesRepository.findAll();
        return createCSVResponse(convertBookSeriesToCSV(bookSeries), "book_series.csv");
    }

    @GetMapping("/export/user-wishlist")
    public ResponseEntity<byte[]> exportUserWishlist() throws IOException {
        List<UserWishlist> userWishlist = userWishlistRepository.findAll();
        return createCSVResponse(convertUserWishlistToCSV(userWishlist), "user_wishlist.csv");
    }

    // Import methods
    @PostMapping("/import/books")
    public String importBooks(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            List<Book> books = parseBooksFromCSV(file);
            for (Book book : books) {
                bookService.addBook(book);
            }
            redirectAttributes.addFlashAttribute("success", "Успешно импортировано " + books.size() + " книг");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка импорта: " + e.getMessage());
        }
        return "redirect:/admin/csv";
    }

    @PostMapping("/import/authors")
    public String importAuthors(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            List<Author> authors = parseAuthorsFromCSV(file);
            for (Author author : authors) {
                authorService.addAuthor(author);
            }
            redirectAttributes.addFlashAttribute("success", "Успешно импортировано " + authors.size() + " авторов");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка импорта: " + e.getMessage());
        }
        return "redirect:/admin/csv";
    }

    @PostMapping("/import/genres")
    public String importGenres(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            List<Genre> genres = parseGenresFromCSV(file);
            for (Genre genre : genres) {
                genreService.addGenre(genre);
            }
            redirectAttributes.addFlashAttribute("success", "Успешно импортировано " + genres.size() + " жанров");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка импорта: " + e.getMessage());
        }
        return "redirect:/admin/csv";
    }

    @PostMapping("/import/series")
    public String importSeries(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            List<Series> series = parseSeriesFromCSV(file);
            for (Series s : series) {
                seriesService.addSeries(s);
            }
            redirectAttributes.addFlashAttribute("success", "Успешно импортировано " + series.size() + " серий");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка импорта: " + e.getMessage());
        }
        return "redirect:/admin/csv";
    }

    @PostMapping("/import/users")
    public String importUsers(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            List<User> users = parseUsersFromCSV(file);
            for (User user : users) {
                userService.addUser(user);
            }
            redirectAttributes.addFlashAttribute("success", "Успешно импортировано " + users.size() + " пользователей");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка импорта: " + e.getMessage());
        }
        return "redirect:/admin/csv";
    }

    @PostMapping("/import/book-authors")
    public String importBookAuthors(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            List<BookAuthor> bookAuthors = parseBookAuthorsFromCSV(file);
            for (BookAuthor bookAuthor : bookAuthors) {
                bookAuthorRepository.save(bookAuthor);
            }
            redirectAttributes.addFlashAttribute("success", "Успешно импортировано " + bookAuthors.size() + " связей книга-автор");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка импорта: " + e.getMessage());
        }
        return "redirect:/admin/csv";
    }

    @PostMapping("/import/book-genres")
    public String importBookGenres(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            List<BookGenre> bookGenres = parseBookGenresFromCSV(file);
            for (BookGenre bookGenre : bookGenres) {
                bookGenreRepository.save(bookGenre);
            }
            redirectAttributes.addFlashAttribute("success", "Успешно импортировано " + bookGenres.size() + " связей книга-жанр");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка импорта: " + e.getMessage());
        }
        return "redirect:/admin/csv";
    }

    @PostMapping("/import/book-series")
    public String importBookSeries(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            List<BookSeries> bookSeries = parseBookSeriesFromCSV(file);
            for (BookSeries bs : bookSeries) {
                bookSeriesRepository.save(bs);
            }
            redirectAttributes.addFlashAttribute("success", "Успешно импортировано " + bookSeries.size() + " связей книга-серия");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка импорта: " + e.getMessage());
        }
        return "redirect:/admin/csv";
    }

    @PostMapping("/import/user-wishlist")
    public String importUserWishlist(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            List<UserWishlist> userWishlist = parseUserWishlistFromCSV(file);
            for (UserWishlist wishlist : userWishlist) {
                userWishlistRepository.save(wishlist);
            }
            redirectAttributes.addFlashAttribute("success", "Успешно импортировано " + userWishlist.size() + " записей списка желаний");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка импорта: " + e.getMessage());
        }
        return "redirect:/admin/csv";
    }

    // CSV Conversion methods using Apache Commons CSV
    private byte[] convertBooksToCSV(List<Book> books) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
            "id", "title", "description", "release_date", "isbn", "price", "language", "pages", "status", "image_url", "adult_check"
        ));
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Book book : books) {
            csvPrinter.printRecord(
                book.getId(),
                book.getTitle(),
                book.getDescription(),
                book.getRelease_date() != null ? book.getRelease_date().format(dateFormatter) : "",
                book.getIsbn(),
                book.getPrice(),
                book.getLanguage() != null ? book.getLanguage().name() : "",
                book.getPages(),
                book.getStatus() != null ? book.getStatus().name() : "",
                book.getImage_url(),
                book.isAdult_check()
            );
        }
        csvPrinter.flush();
        csvPrinter.close();
        return outputStream.toByteArray();
    }

    private byte[] convertAuthorsToCSV(List<Author> authors) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
            "id", "firstname", "middlename", "lastname", "biography", "photo_url"
        ));
        
        for (Author author : authors) {
            csvPrinter.printRecord(
                author.getId(),
                author.getFirstname(),
                author.getMiddlename(),
                author.getLastname(),
                author.getBiography(),
                author.getPhoto_url()
            );
        }
        csvPrinter.flush();
        csvPrinter.close();
        return outputStream.toByteArray();
    }

    private byte[] convertGenresToCSV(List<Genre> genres) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
            "id", "name", "description"
        ));
        
        for (Genre genre : genres) {
            csvPrinter.printRecord(
                genre.getId(),
                genre.getName(),
                genre.getDescription()
            );
        }
        csvPrinter.flush();
        csvPrinter.close();
        return outputStream.toByteArray();
    }

    private byte[] convertSeriesToCSV(List<Series> series) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
            "id", "name", "description"
        ));
        
        for (Series s : series) {
            csvPrinter.printRecord(
                s.getId(),
                s.getName(),
                s.getDescription()
            );
        }
        csvPrinter.flush();
        csvPrinter.close();
        return outputStream.toByteArray();
    }

    private byte[] convertUsersToCSV(List<User> users) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
            "id", "username", "email", "password", "role", "email_notification", "created_at", "updated_at", "log_in_at", "logout_at"
        ));
        
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (User user : users) {
            csvPrinter.printRecord(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole() != null ? user.getRole().name() : "",
                user.isEmail_notification(),
                user.getCreated_at() != null ? user.getCreated_at().format(dateTimeFormatter) : "",
                user.getUpdated_at() != null ? user.getUpdated_at().format(dateTimeFormatter) : "",
                user.getLog_in_at() != null ? user.getLog_in_at().format(dateTimeFormatter) : "",
                user.getLogout_at() != null ? user.getLogout_at().format(dateTimeFormatter) : ""
            );
        }
        csvPrinter.flush();
        csvPrinter.close();
        return outputStream.toByteArray();
    }

    private byte[] convertBookAuthorsToCSV(List<BookAuthor> bookAuthors) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
            "book_id", "author_id", "main_author"
        ));
        
        for (BookAuthor ba : bookAuthors) {
            csvPrinter.printRecord(
                ba.getBook() != null ? ba.getBook().getId() : "",
                ba.getAuthor() != null ? ba.getAuthor().getId() : "",
                ba.getMain_author()
            );
        }
        csvPrinter.flush();
        csvPrinter.close();
        return outputStream.toByteArray();
    }

    private byte[] convertBookGenresToCSV(List<BookGenre> bookGenres) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
            "book_id", "genre_id"
        ));
        
        for (BookGenre bg : bookGenres) {
            csvPrinter.printRecord(
                bg.getBook() != null ? bg.getBook().getId() : "",
                bg.getGenre() != null ? bg.getGenre().getId() : ""
            );
        }
        csvPrinter.flush();
        csvPrinter.close();
        return outputStream.toByteArray();
    }

    private byte[] convertBookSeriesToCSV(List<BookSeries> bookSeries) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
            "book_id", "series_id"
        ));
        
        for (BookSeries bs : bookSeries) {
            csvPrinter.printRecord(
                bs.getBook() != null ? bs.getBook().getId() : "",
                bs.getSeries() != null ? bs.getSeries().getId() : ""
            );
        }
        csvPrinter.flush();
        csvPrinter.close();
        return outputStream.toByteArray();
    }

    private byte[] convertUserWishlistToCSV(List<UserWishlist> userWishlist) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
            "user_id", "book_id", "added_at"
        ));
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (UserWishlist wl : userWishlist) {
            csvPrinter.printRecord(
                wl.getUser() != null ? wl.getUser().getId() : "",
                wl.getBook() != null ? wl.getBook().getId() : "",
                wl.getAdded_at() != null ? wl.getAdded_at().format(dateFormatter) : ""
            );
        }
        csvPrinter.flush();
        csvPrinter.close();
        return outputStream.toByteArray();
    }

    // CSV Parsing methods using Apache Commons CSV
    private List<Book> parseBooksFromCSV(MultipartFile file) throws IOException {
        List<Book> books = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : parser) {
                try {
                    Book book = new Book();
                    if (record.isSet("id") && !record.get("id").isEmpty()) {
                        book.setId(Long.parseLong(record.get("id")));
                    }
                    book.setTitle(record.isSet("title") ? record.get("title") : "");
                    book.setDescription(record.isSet("description") ? record.get("description") : "");
                    if (record.isSet("release_date") && !record.get("release_date").isEmpty()) {
                        book.setRelease_date(LocalDate.parse(record.get("release_date")));
                    }
                    book.setIsbn(record.isSet("isbn") ? record.get("isbn") : "");
                    if (record.isSet("price") && !record.get("price").isEmpty()) {
                        book.setPrice(Double.parseDouble(record.get("price")));
                    }
                    if (record.isSet("language") && !record.get("language").isEmpty()) {
                        book.setLanguage(Language.valueOf(record.get("language")));
                    }
                    if (record.isSet("pages") && !record.get("pages").isEmpty()) {
                        book.setPages(Integer.parseInt(record.get("pages")));
                    }
                    if (record.isSet("status") && !record.get("status").isEmpty()) {
                        try {
                            book.setStatus(BookStatus.valueOf(record.get("status")));
                        } catch (IllegalArgumentException e) {
                            book.setStatus(BookStatus.SOON);
                        }
                    } else {
                        book.setStatus(BookStatus.SOON);
                    }
                    book.setImage_url(record.isSet("image_url") ? record.get("image_url") : "");
                    if (record.isSet("adult_check") && !record.get("adult_check").isEmpty()) {
                        book.setAdult_check(Boolean.parseBoolean(record.get("adult_check")));
                    }
                    books.add(book);
                } catch (Exception e) {
                    // Skip invalid rows
                    continue;
                }
            }
        }
        return books;
    }

    private List<Author> parseAuthorsFromCSV(MultipartFile file) throws IOException {
        List<Author> authors = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : parser) {
                try {
                    Author author = new Author();
                    if (record.isSet("id") && !record.get("id").isEmpty()) {
                        author.setId(Long.parseLong(record.get("id")));
                    }
                    author.setFirstname(record.isSet("firstname") ? record.get("firstname") : "");
                    author.setMiddlename(record.isSet("middlename") ? record.get("middlename") : "");
                    author.setLastname(record.isSet("lastname") ? record.get("lastname") : "");
                    author.setBiography(record.isSet("biography") ? record.get("biography") : "");
                    author.setPhoto_url(record.isSet("photo_url") ? record.get("photo_url") : "");
                    authors.add(author);
                } catch (Exception e) {
                    // Skip invalid rows
                    continue;
                }
            }
        }
        return authors;
    }

    private List<Genre> parseGenresFromCSV(MultipartFile file) throws IOException {
        List<Genre> genres = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : parser) {
                try {
                    Genre genre = new Genre();
                    if (record.isSet("id") && !record.get("id").isEmpty()) {
                        genre.setId(Long.parseLong(record.get("id")));
                    }
                    genre.setName(record.isSet("name") ? record.get("name") : "");
                    genre.setDescription(record.isSet("description") ? record.get("description") : "");
                    genres.add(genre);
                } catch (Exception e) {
                    // Skip invalid rows
                    continue;
                }
            }
        }
        return genres;
    }

    private List<Series> parseSeriesFromCSV(MultipartFile file) throws IOException {
        List<Series> series = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : parser) {
                try {
                    Series s = new Series();
                    if (record.isSet("id") && !record.get("id").isEmpty()) {
                        s.setId(Long.parseLong(record.get("id")));
                    }
                    s.setName(record.isSet("name") ? record.get("name") : "");
                    s.setDescription(record.isSet("description") ? record.get("description") : "");
                    series.add(s);
                } catch (Exception e) {
                    // Skip invalid rows
                    continue;
                }
            }
        }
        return series;
    }

    private List<User> parseUsersFromCSV(MultipartFile file) throws IOException {
        List<User> users = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : parser) {
                try {
                    User user = new User();
                    if (record.isSet("id") && !record.get("id").isEmpty()) {
                        user.setId(Long.parseLong(record.get("id")));
                    }
                    user.setUsername(record.isSet("username") ? record.get("username") : "");
                    user.setEmail(record.isSet("email") ? record.get("email") : "");
                    String password = record.isSet("password") ? record.get("password") : "";
                    if (!password.isEmpty() && !password.startsWith("$2a$")) {
                        user.setPassword(passwordEncoder.encode(password));
                    } else {
                        user.setPassword(password);
                    }
                    if (record.isSet("role") && !record.get("role").isEmpty()) {
                        user.setRole(Role.valueOf(record.get("role")));
                    }
                    if (record.isSet("email_notification") && !record.get("email_notification").isEmpty()) {
                        user.setEmail_notification(Boolean.parseBoolean(record.get("email_notification")));
                    }
                    if (record.isSet("created_at") && !record.get("created_at").isEmpty()) {
                        user.setCreated_at(LocalDateTime.parse(record.get("created_at"), dateTimeFormatter));
                    }
                    if (record.isSet("updated_at") && !record.get("updated_at").isEmpty()) {
                        user.setUpdated_at(LocalDateTime.parse(record.get("updated_at"), dateTimeFormatter));
                    }
                    if (record.isSet("log_in_at") && !record.get("log_in_at").isEmpty()) {
                        user.setLog_in_at(LocalDateTime.parse(record.get("log_in_at"), dateTimeFormatter));
                    }
                    if (record.isSet("logout_at") && !record.get("logout_at").isEmpty()) {
                        user.setLogout_at(LocalDateTime.parse(record.get("logout_at"), dateTimeFormatter));
                    }
                    users.add(user);
                } catch (Exception e) {
                    // Skip invalid rows
                    continue;
                }
            }
        }
        return users;
    }

    private List<BookAuthor> parseBookAuthorsFromCSV(MultipartFile file) throws IOException {
        List<BookAuthor> bookAuthors = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : parser) {
                try {
                    Long bookId = Long.parseLong(record.get("book_id"));
                    Long authorId = Long.parseLong(record.get("author_id"));
                    boolean mainAuthor = record.isSet("main_author") && !record.get("main_author").isEmpty() 
                        && Boolean.parseBoolean(record.get("main_author"));
                    
                    Book book = bookService.getBookById(bookId);
                    Author author = authorService.getAuthorById(authorId);
                    
                    if (book != null && author != null) {
                        BookAuthor bookAuthor = new BookAuthor(author, book, mainAuthor);
                        BookAuthorId id = new BookAuthorId(authorId, bookId);
                        bookAuthor.setId(id);
                        bookAuthors.add(bookAuthor);
                    }
                } catch (Exception e) {
                    // Skip invalid rows
                    continue;
                }
            }
        }
        return bookAuthors;
    }

    private List<BookGenre> parseBookGenresFromCSV(MultipartFile file) throws IOException {
        List<BookGenre> bookGenres = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : parser) {
                try {
                    Long bookId = Long.parseLong(record.get("book_id"));
                    Long genreId = Long.parseLong(record.get("genre_id"));
                    
                    Book book = bookService.getBookById(bookId);
                    Genre genre = genreService.getGenreById(genreId);
                    
                    if (book != null && genre != null) {
                        BookGenre bookGenre = new BookGenre(genre, book);
                        BookGenreId id = new BookGenreId(genreId, bookId);
                        bookGenre.setId(id);
                        bookGenres.add(bookGenre);
                    }
                } catch (Exception e) {
                    // Skip invalid rows
                    continue;
                }
            }
        }
        return bookGenres;
    }

    private List<BookSeries> parseBookSeriesFromCSV(MultipartFile file) throws IOException {
        List<BookSeries> bookSeries = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : parser) {
                try {
                    Long bookId = Long.parseLong(record.get("book_id"));
                    Long seriesId = Long.parseLong(record.get("series_id"));
                    
                    Book book = bookService.getBookById(bookId);
                    Series series = seriesService.getSeriesById(seriesId);
                    
                    if (book != null && series != null) {
                        BookSeries bs = new BookSeries(series, book);
                        BookSeriesId id = new BookSeriesId(seriesId, bookId);
                        bs.setId(id);
                        bookSeries.add(bs);
                    }
                } catch (Exception e) {
                    // Skip invalid rows
                    continue;
                }
            }
        }
        return bookSeries;
    }

    private List<UserWishlist> parseUserWishlistFromCSV(MultipartFile file) throws IOException {
        List<UserWishlist> userWishlist = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : parser) {
                try {
                    Long userId = Long.parseLong(record.get("user_id"));
                    Long bookId = Long.parseLong(record.get("book_id"));
                    LocalDate addedAt = record.isSet("added_at") && !record.get("added_at").isEmpty()
                        ? LocalDate.parse(record.get("added_at"), dateFormatter)
                        : LocalDate.now();
                    
                    User user = userService.getUserById(userId);
                    Book book = bookService.getBookById(bookId);
                    
                    if (user != null && book != null) {
                        UserWishlist wishlist = new UserWishlist(user, book, addedAt);
                        UserWishlistId id = new UserWishlistId(userId, bookId);
                        wishlist.setId(id);
                        userWishlist.add(wishlist);
                    }
                } catch (Exception e) {
                    // Skip invalid rows
                    continue;
                }
            }
        }
        return userWishlist;
    }

    private ResponseEntity<byte[]> createCSVResponse(byte[] csvBytes, String filename) {
        // Add UTF-8 BOM for proper Russian character display in Excel and other programs
        byte[] bom = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] bytes = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, bytes, 0, bom.length);
        System.arraycopy(csvBytes, 0, bytes, bom.length, csvBytes.length);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        // Use RFC 5987 encoding for filename with non-ASCII characters
        String encodedFilename = java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8)
            .replace("+", "%20");
        headers.add("Content-Disposition", 
            String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s", 
                filename, encodedFilename));
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}

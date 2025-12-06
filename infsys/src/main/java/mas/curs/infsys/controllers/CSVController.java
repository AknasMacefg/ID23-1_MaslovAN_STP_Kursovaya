package mas.curs.infsys.controllers;

import mas.curs.infsys.models.*;
import mas.curs.infsys.services.*;
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

    @GetMapping
    public String csvPage(Model model) {
        return "csv-management";
    }

    // Export methods
    @GetMapping("/export/books")
    public ResponseEntity<byte[]> exportBooks() {
        List<Book> books = bookService.getAllBooks();
        String csv = convertBooksToCSV(books);
        return createCSVResponse(csv, "books.csv");
    }

    @GetMapping("/export/authors")
    public ResponseEntity<byte[]> exportAuthors() {
        List<Author> authors = authorService.getAllAuthors();
        String csv = convertAuthorsToCSV(authors);
        return createCSVResponse(csv, "authors.csv");
    }

    @GetMapping("/export/genres")
    public ResponseEntity<byte[]> exportGenres() {
        List<Genre> genres = genreService.getAllGenres();
        String csv = convertGenresToCSV(genres);
        return createCSVResponse(csv, "genres.csv");
    }

    @GetMapping("/export/series")
    public ResponseEntity<byte[]> exportSeries() {
        List<Series> series = seriesService.getAllSeriess();
        String csv = convertSeriesToCSV(series);
        return createCSVResponse(csv, "series.csv");
    }

    @GetMapping("/export/users")
    public ResponseEntity<byte[]> exportUsers() {
        List<User> users = userService.getAllUsers();
        String csv = convertUsersToCSV(users);
        return createCSVResponse(csv, "users.csv");
    }

    // Import methods
    @PostMapping("/import/books")
    public String importBooks(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<Book> books = parseBooksFromCSV(content);
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
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<Author> authors = parseAuthorsFromCSV(content);
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
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<Genre> genres = parseGenresFromCSV(content);
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
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<Series> series = parseSeriesFromCSV(content);
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
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<User> users = parseUsersFromCSV(content);
            for (User user : users) {
                userService.addUser(user);
            }
            redirectAttributes.addFlashAttribute("success", "Успешно импортировано " + users.size() + " пользователей");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка импорта: " + e.getMessage());
        }
        return "redirect:/admin/csv";
    }

    // CSV Conversion methods
    private String convertBooksToCSV(List<Book> books) {
        StringBuilder csv = new StringBuilder();
        csv.append("id,title,description,release_date,isbn,price,language,pages,status,image_url,adult_check\n");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Book book : books) {
            csv.append(escapeCSV(book.getId())).append(",");
            csv.append(escapeCSV(book.getTitle())).append(",");
            csv.append(escapeCSV(book.getDescription())).append(",");
            csv.append(book.getRelease_date() != null ? book.getRelease_date().format(dateFormatter) : "").append(",");
            csv.append(escapeCSV(book.getIsbn())).append(",");
            csv.append(book.getPrice()).append(",");
            csv.append(book.getLanguage() != null ? book.getLanguage().name() : "").append(",");
            csv.append(book.getPages()).append(",");
            csv.append(book.getStatus() != null ? book.getStatus().name() : "").append(",");
            csv.append(escapeCSV(book.getImage_url())).append(",");
            csv.append(book.isAdult_check()).append("\n");
        }
        return csv.toString();
    }

    private String convertAuthorsToCSV(List<Author> authors) {
        StringBuilder csv = new StringBuilder();
        csv.append("id,firstname,middlename,lastname,biography,photo_url\n");
        for (Author author : authors) {
            csv.append(escapeCSV(author.getId())).append(",");
            csv.append(escapeCSV(author.getFirstname())).append(",");
            csv.append(escapeCSV(author.getMiddlename())).append(",");
            csv.append(escapeCSV(author.getLastname())).append(",");
            csv.append(escapeCSV(author.getBiography())).append(",");
            csv.append(escapeCSV(author.getPhoto_url())).append("\n");
        }
        return csv.toString();
    }

    private String convertGenresToCSV(List<Genre> genres) {
        StringBuilder csv = new StringBuilder();
        csv.append("id,name,description\n");
        for (Genre genre : genres) {
            csv.append(escapeCSV(genre.getId())).append(",");
            csv.append(escapeCSV(genre.getName())).append(",");
            csv.append(escapeCSV(genre.getDescription())).append("\n");
        }
        return csv.toString();
    }

    private String convertSeriesToCSV(List<Series> series) {
        StringBuilder csv = new StringBuilder();
        csv.append("id,name,description\n");
        for (Series s : series) {
            csv.append(escapeCSV(s.getId())).append(",");
            csv.append(escapeCSV(s.getName())).append(",");
            csv.append(escapeCSV(s.getDescription())).append("\n");
        }
        return csv.toString();
    }

    private String convertUsersToCSV(List<User> users) {
        StringBuilder csv = new StringBuilder();
        csv.append("id,username,email,password,role,email_notification,created_at,updated_at,log_in_at,logout_at\n");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (User user : users) {
            csv.append(escapeCSV(user.getId())).append(",");
            csv.append(escapeCSV(user.getUsername())).append(",");
            csv.append(escapeCSV(user.getEmail())).append(",");
            csv.append(escapeCSV(user.getPassword())).append(",");
            csv.append(user.getRole() != null ? user.getRole().name() : "").append(",");
            csv.append(user.isEmail_notification()).append(",");
            csv.append(user.getCreated_at() != null ? user.getCreated_at().format(dateTimeFormatter) : "").append(",");
            csv.append(user.getUpdated_at() != null ? user.getUpdated_at().format(dateTimeFormatter) : "").append(",");
            csv.append(user.getLog_in_at() != null ? user.getLog_in_at().format(dateTimeFormatter) : "").append(",");
            csv.append(user.getLogout_at() != null ? user.getLogout_at().format(dateTimeFormatter) : "").append("\n");
        }
        return csv.toString();
    }

    // CSV Parsing methods
    private List<Book> parseBooksFromCSV(String csv) throws Exception {
        List<Book> books = new ArrayList<>();
        String[] lines = csv.split("\n");
        if (lines.length < 2) return books;
        
        for (int i = 1; i < lines.length; i++) {
            String[] values = parseCSVLine(lines[i]);
            if (values.length < 10) continue;
            
            Book book = new Book();
            if (!values[0].isEmpty()) book.setId(Long.parseLong(values[0]));
            book.setTitle(values[1]);
            book.setDescription(values[2]);
            if (!values[3].isEmpty()) {
                book.setRelease_date(LocalDate.parse(values[3]));
            }
            book.setIsbn(values[4]);
            if (!values[5].isEmpty()) {
                book.setPrice(Double.parseDouble(values[5]));
            }
            if (!values[6].isEmpty()) {
                book.setLanguage(Language.valueOf(values[6]));
            }
            if (!values[7].isEmpty()) {
                book.setPages(Integer.parseInt(values[7]));
            }
            if (!values[8].isEmpty()) {
                try {
                    book.setStatus(BookStatus.valueOf(values[8]));
                } catch (IllegalArgumentException e) {
                    book.setStatus(BookStatus.SOON);
                }
            } else {
                book.setStatus(BookStatus.SOON);
            }
            book.setImage_url(values[9]);
            if (values.length > 10 && !values[10].isEmpty()) {
                book.setAdult_check(Boolean.parseBoolean(values[10]));
            }
            books.add(book);
        }
        return books;
    }

    private List<Author> parseAuthorsFromCSV(String csv) throws Exception {
        List<Author> authors = new ArrayList<>();
        String[] lines = csv.split("\n");
        if (lines.length < 2) return authors;
        
        for (int i = 1; i < lines.length; i++) {
            String[] values = parseCSVLine(lines[i]);
            if (values.length < 6) continue;
            
            Author author = new Author();
            if (!values[0].isEmpty()) author.setId(Long.parseLong(values[0]));
            author.setFirstname(values[1]);
            author.setMiddlename(values[2]);
            author.setLastname(values[3]);
            author.setBiography(values[4]);
            author.setPhoto_url(values[5]);
            authors.add(author);
        }
        return authors;
    }

    private List<Genre> parseGenresFromCSV(String csv) throws Exception {
        List<Genre> genres = new ArrayList<>();
        String[] lines = csv.split("\n");
        if (lines.length < 2) return genres;
        
        for (int i = 1; i < lines.length; i++) {
            String[] values = parseCSVLine(lines[i]);
            if (values.length < 3) continue;
            
            Genre genre = new Genre();
            if (!values[0].isEmpty()) genre.setId(Long.parseLong(values[0]));
            genre.setName(values[1]);
            genre.setDescription(values[2]);
            genres.add(genre);
        }
        return genres;
    }

    private List<Series> parseSeriesFromCSV(String csv) throws Exception {
        List<Series> series = new ArrayList<>();
        String[] lines = csv.split("\n");
        if (lines.length < 2) return series;
        
        for (int i = 1; i < lines.length; i++) {
            String[] values = parseCSVLine(lines[i]);
            if (values.length < 3) continue;
            
            Series s = new Series();
            if (!values[0].isEmpty()) s.setId(Long.parseLong(values[0]));
            s.setName(values[1]);
            s.setDescription(values[2]);
            series.add(s);
        }
        return series;
    }

    private List<User> parseUsersFromCSV(String csv) throws Exception {
        List<User> users = new ArrayList<>();
        String[] lines = csv.split("\n");
        if (lines.length < 2) return users;
        
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (int i = 1; i < lines.length; i++) {
            String[] values = parseCSVLine(lines[i]);
            if (values.length < 6) continue;
            
            User user = new User();
            if (!values[0].isEmpty()) user.setId(Long.parseLong(values[0]));
            user.setUsername(values[1]);
            user.setEmail(values[2]);
            // Encode password if it's not already encoded (check if it starts with $2a$ which is BCrypt)
            String password = values[3];
            if (!password.isEmpty() && !password.startsWith("$2a$")) {
                user.setPassword(passwordEncoder.encode(password));
            } else {
                user.setPassword(password);
            }
            if (!values[4].isEmpty()) {
                user.setRole(Role.valueOf(values[4]));
            }
            if (values.length > 5 && !values[5].isEmpty()) {
                user.setEmail_notification(Boolean.parseBoolean(values[5]));
            }
            if (values.length > 6 && !values[6].isEmpty()) {
                user.setCreated_at(LocalDateTime.parse(values[6], dateTimeFormatter));
            }
            if (values.length > 7 && !values[7].isEmpty()) {
                user.setUpdated_at(LocalDateTime.parse(values[7], dateTimeFormatter));
            }
            if (values.length > 8 && !values[8].isEmpty()) {
                user.setLog_in_at(LocalDateTime.parse(values[8], dateTimeFormatter));
            }
            if (values.length > 9 && !values[9].isEmpty()) {
                user.setLogout_at(LocalDateTime.parse(values[9], dateTimeFormatter));
            }
            users.add(user);
        }
        return users;
    }

    private String[] parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values.toArray(new String[0]);
    }

    private String escapeCSV(Object value) {
        if (value == null) return "";
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    private ResponseEntity<byte[]> createCSVResponse(String csv, String filename) {
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}


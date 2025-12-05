package mas.curs.infsys.controllers;
import mas.curs.infsys.models.Book;
import mas.curs.infsys.services.BookService;
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
import java.util.UUID;



@Controller
@RequestMapping("/books")
public class BookController {

    /** Репозиторий пользователей, обеспечивающий доступ к данным. */
    private final BookService bookService;

    /**
     * Конструктор контроллера пользователей.
     *
     * @param userRepository репозиторий пользователей
     */
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Отображает панель управления пользователями.
     *
     * @param model объект {@link Model} для передачи данных в шаблон (список пользователей и сообщения)
     * @param msg необязательное сообщение (используется для отображения статуса операции)
     * @return имя Thymeleaf-шаблона страницы пользователей ({@code users})
     */
    @GetMapping
    public String bookPage(Model model, @RequestParam(required = false) String msg) {
        model.addAttribute("books", bookService.getAllBooks());
        model.addAttribute("message", msg);
        return "books";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("books", book);
        return "edit-book";
    }

    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable("id") Long id,
                               @ModelAttribute("books") Book book,
                               @RequestParam("image") MultipartFile file,
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

            book.setId(id);
            boolean success = bookService.updateBook(book);


            if (success) {
                redirectAttributes.addFlashAttribute("success", "Автор успешно обновлен");
                return "redirect:/books/" + id;
            } else {
                redirectAttributes.addFlashAttribute("error", "Произошла ошибка при обновлении");
                return "redirect:/books/edit/" + id;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка загрузки изображения: " + e.getMessage());
            return "redirect:/books/edit/" + id;
        }
    }

    @GetMapping("/{id}")
    public String showViewForm(@PathVariable("id") Long id, Model model) {
        Book book = bookService.getBookById(id); // Retrieve the existing item
        model.addAttribute("books", book);
        return "view-book"; // Name of your edit Thymeleaf template
    }

    @GetMapping("/edit/new")
    public String showNewBookForm(Model model) {
        model.addAttribute("books", new Book());
        return "edit-book";
    }

    @PostMapping("/edit/new")
    public String addBook(@ModelAttribute("books") Book book,
                            @RequestParam("image") MultipartFile file,
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
            boolean success = bookService.addBook(book);

            if (success) {
                redirectAttributes.addFlashAttribute("success", "Автор успешно создан");
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


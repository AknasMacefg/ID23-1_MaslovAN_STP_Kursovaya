package mas.curs.infsys.controllers;
import mas.curs.infsys.exceptions.ResourceNotFoundException;
import mas.curs.infsys.models.Author;
import mas.curs.infsys.services.AuthorService;
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
import java.util.List;
import java.nio.file.StandardCopyOption;
import java.util.UUID;



@Controller
@RequestMapping("/authors")
public class AuthorController {

    private final AuthorService authorService;
    private final BookService bookService;

    public AuthorController(AuthorService authorService, BookService bookService) {
        this.authorService = authorService;
        this.bookService = bookService;
    }

    @GetMapping
    public String authorPage(Model model, 
                            @RequestParam(required = false) String msg,
                            @RequestParam(required = false) String sortBy,
                            @RequestParam(required = false) String search,
                            @RequestParam(defaultValue = "0") int page) {
        int pageSize = 50;
        int totalPages = authorService.getTotalPages(sortBy, search, pageSize);
        List<Author> allAuthors = authorService.getAuthorsSorted(sortBy, search, 0, Integer.MAX_VALUE);
        
        model.addAttribute("authors", authorService.getAuthorsSorted(sortBy, search, page, pageSize));
        model.addAttribute("selectedSortBy", sortBy);
        model.addAttribute("searchQuery", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", allAuthors.size());
        model.addAttribute("showPagination", allAuthors.size() > pageSize);
        model.addAttribute("message", msg);
        return "authors";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Author author = authorService.getAuthorById(id);
        if (author == null) {
            throw new ResourceNotFoundException("Автор с ID " + id + " не найден");
        }
        model.addAttribute("authors", author);
        return "edit-author";
    }

    @PostMapping("/edit/{id}")
    public String updateAuthor(@PathVariable("id") Long id,
                               @ModelAttribute("authors") Author author,
                               @RequestParam("photo") MultipartFile file,
                               RedirectAttributes redirectAttributes) {

        Author existingAuthor = authorService.getAuthorById(id);
        if (existingAuthor == null) {
            throw new ResourceNotFoundException("Автор с ID " + id + " не найден");
        }

        try {
            // Handle file upload
            if (file != null && !file.isEmpty()) {
                String fileName = saveImage(file);
                author.setPhoto_url("/images/authors/" + fileName);
            } else if (author.getPhoto_url() == null || author.getPhoto_url().isEmpty()) {
                if (existingAuthor != null) {
                    author.setPhoto_url(existingAuthor.getPhoto_url());
                }
            }

            deleteOldImage(existingAuthor.getPhoto_url());

            author.setId(id);
            boolean success = authorService.updateAuthor(author);


            if (success) {
                redirectAttributes.addFlashAttribute("success", "Автор успешно обновлен");
                return "redirect:/authors/view/" + id;
            } else {
                redirectAttributes.addFlashAttribute("error", "Произошла ошибка при обновлении");
                return "redirect:/authors/edit/" + id;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка загрузки изображения: " + e.getMessage());
            return "redirect:/authors/edit/" + id;
        }
    }

    @GetMapping("/view/{id}")
    public String showViewForm(@PathVariable("id") Long id, Model model) {
        Author author = authorService.getAuthorById(id);
        if (author == null) {
            throw new ResourceNotFoundException("Автор с ID " + id + " не найден");
        }
        model.addAttribute("authors", author);
        model.addAttribute("books", bookService.getBooksByAuthor(id));
        return "view-author";
    }

    @GetMapping("/edit/new")
    public String showNewAuthorForm(Model model) {
        model.addAttribute("authors", new Author());
        return "edit-author";
    }

    @PostMapping("/edit/new")
    public String addAuthor(@ModelAttribute("authors") Author author,
                            @RequestParam("photo") MultipartFile file,
                            RedirectAttributes redirectAttributes) {
        try {
            String fileName;
            // Handle file upload for new author
            if (file != null && !file.isEmpty()) {
                fileName = saveImage(file);

            }
            else {
                fileName = "default.jpg";
            }
            author.setPhoto_url("/images/authors/" + fileName);
            boolean success = authorService.addAuthor(author);

            if (success) {
                redirectAttributes.addFlashAttribute("success", "Автор успешно создан");
                return "redirect:/authors";
            } else {
                redirectAttributes.addFlashAttribute("error", "Произошла ошибка при создании");
                return "redirect:/authors/edit/new";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка загрузки изображения: " + e.getMessage());
            return "redirect:/authors/edit/new";
        }
    }


    @GetMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable("id") Long id) {
        Author author = authorService.getAuthorById(id);
        if (author == null) {
            throw new ResourceNotFoundException("Автор с ID " + id + " не найден");
        }
        authorService.deleteAuthor(id);
        return "redirect:/authors";
    }


    private String saveImage(MultipartFile file) throws IOException {

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";

        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + fileExtension;

        String UPLOAD_DIR = "src/main/resources/static/images/authors/";
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    private void deleteOldImage(String oldImagePath) {
        if (oldImagePath != null && !oldImagePath.isEmpty() && !oldImagePath.equals("/images/authors/default.jpg")) {
            try {
                Path oldFilePath = Paths.get("src/main/resources/static" + oldImagePath);
                Files.deleteIfExists(oldFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}


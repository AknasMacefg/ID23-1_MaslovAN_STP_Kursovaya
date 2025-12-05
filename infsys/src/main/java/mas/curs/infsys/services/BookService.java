package mas.curs.infsys.services;

import mas.curs.infsys.models.Book;
import mas.curs.infsys.repositories.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class BookService {
    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    @Autowired
    private UserService userService;

    @Autowired
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private BookRepository bookRepository;

    public List<Book> getAllBooks()
    {
        return bookRepository.findAll();
    }

    public Book getBookById(long id) {
        return bookRepository.findById(id).get();
    }

    public void deleteBook(Long bookId) {
        bookRepository.delete(bookRepository.findById(bookId).get());
    }

    public boolean addBook(Book book) {
        bookRepository.save(book);
        return true;
    }

    public boolean updateBook(Book book) {
        bookRepository.save(book);
        return true;

    }


}

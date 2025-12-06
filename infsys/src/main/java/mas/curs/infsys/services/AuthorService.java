package mas.curs.infsys.services;

import mas.curs.infsys.models.Author;
import mas.curs.infsys.models.Book;
import mas.curs.infsys.repositories.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthorService {
    @Autowired
    private AuthorRepository authorRepository;
    
    @Autowired
    @Lazy
    private BookService bookService;

    public List<Author> getAllAuthors()
    {
        return authorRepository.findAll();
    }

    public List<Author> getAuthorsSorted(String sortBy, String search, int page, int pageSize) {
        List<Author> authors = authorRepository.findAll();
        
        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            authors = authors.stream()
                .filter(author -> {
                    if (author.getFirstname() != null && author.getFirstname().toLowerCase().contains(searchLower)) return true;
                    if (author.getMiddlename() != null && author.getMiddlename().toLowerCase().contains(searchLower)) return true;
                    if (author.getLastname() != null && author.getLastname().toLowerCase().contains(searchLower)) return true;
                    if (author.getBiography() != null && author.getBiography().toLowerCase().contains(searchLower)) return true;
                    return false;
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy) {
                case "fullname":
                    authors.sort(Comparator.comparing((Author a) -> {
                        String fullname = a.getFirstname() != null ? a.getFirstname() : "";
                        if (a.getMiddlename() != null) fullname += " " + a.getMiddlename();
                        if (a.getLastname() != null) fullname += " " + a.getLastname();
                        return fullname;
                    }, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
                    break;
                case "fullname_desc":
                    authors.sort(Comparator.comparing((Author a) -> {
                        String fullname = a.getFirstname() != null ? a.getFirstname() : "";
                        if (a.getMiddlename() != null) fullname += " " + a.getMiddlename();
                        if (a.getLastname() != null) fullname += " " + a.getLastname();
                        return fullname;
                    }, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)).reversed());
                    break;
            }
        }
        
        // Apply pagination if more than pageSize items
        if (authors.size() > pageSize) {
            int start = page * pageSize;
            int end = Math.min(start + pageSize, authors.size());
            if (start < authors.size()) {
                return authors.subList(start, end);
            } else {
                return new java.util.ArrayList<>();
            }
        }
        
        return authors;
    }
    
    public int getTotalPages(String sortBy, String search, int pageSize) {
        List<Author> authors = getAuthorsSorted(sortBy, search, 0, Integer.MAX_VALUE);
        return (int) Math.ceil((double) authors.size() / pageSize);
    }

    public Author getAuthorById(long id) {
        return authorRepository.findById(id).get();
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteAuthor(Long authorId) {
        // Remove all BookAuthor relationships before deleting the author
        List<Book> books = bookService.getBooksByAuthor(authorId);
        for (Book book : books) {
            book.getBookAuthor().removeIf(ba -> ba.getAuthor().getId().equals(authorId));
            bookService.updateBook(book);
        }
        authorRepository.delete(authorRepository.findById(authorId).get());
    }

    public boolean addAuthor(Author author) {
        authorRepository.save(author);
        return true;
    }

    public boolean updateAuthor(Author author) {
        authorRepository.save(author);
        return true;

    }


}

package mas.curs.infsys.services;

import mas.curs.infsys.models.*;
import mas.curs.infsys.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private SeriesService seriesService;
    
    @Autowired
    private mas.curs.infsys.repositories.BookAuthorRepository bookAuthorRepository;
    
    @Autowired
    private mas.curs.infsys.repositories.BookGenreRepository bookGenreRepository;
    
    @Autowired
    private mas.curs.infsys.repositories.BookSeriesRepository bookSeriesRepository;
    
    @Autowired
    private mas.curs.infsys.repositories.UserWishlistRepository userWishlistRepository;

    public List<Book> getAllBooks()
    {
        return bookRepository.findAll();
    }

    public List<Book> getBooksFilteredAndSorted(List<Long> genreIds, List<String> languages, Boolean adultCheck, String sortBy, String search, int page, int pageSize) {
        List<Book> books = bookRepository.findAll();
        
        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            books = books.stream()
                .filter(book -> {
                    // Search in title
                    if (book.getTitle() != null && book.getTitle().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                    // Search in description
                    if (book.getDescription() != null && book.getDescription().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                    // Search in authors
                    if (book.getBookAuthor() != null) {
                        boolean authorMatch = book.getBookAuthor().stream()
                            .anyMatch(ba -> ba.getAuthor() != null && (
                                (ba.getAuthor().getFirstname() != null && ba.getAuthor().getFirstname().toLowerCase().contains(searchLower)) ||
                                (ba.getAuthor().getMiddlename() != null && ba.getAuthor().getMiddlename().toLowerCase().contains(searchLower)) ||
                                (ba.getAuthor().getLastname() != null && ba.getAuthor().getLastname().toLowerCase().contains(searchLower))
                            ));
                        if (authorMatch) return true;
                    }
                    // Search in ISBN
                    if (book.getIsbn() != null && book.getIsbn().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        }
        
        // Apply filters
        if (genreIds != null && !genreIds.isEmpty()) {
            books = books.stream()
                .filter(book -> book.getBookGenre().stream()
                    .anyMatch(bg -> genreIds.contains(bg.getGenre().getId())))
                .collect(Collectors.toList());
        }
        
        if (languages != null && !languages.isEmpty()) {
            books = books.stream()
                .filter(book -> {
                    if (book.getLanguage() == null) return false;
                    return languages.contains(book.getLanguage().name());
                })
                .collect(Collectors.toList());
        }
        
        if (adultCheck != null) {
            books = books.stream()
                .filter(book -> book.isAdult_check() == adultCheck)
                .collect(Collectors.toList());
        }
        
        // Apply sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy) {
                case "title":
                    books.sort(Comparator.comparing(Book::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
                    break;
                case "title_desc":
                    books.sort(Comparator.comparing(Book::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)).reversed());
                    break;
                case "release_date":
                    books.sort(Comparator.comparing(Book::getRelease_date, Comparator.nullsLast(Comparator.naturalOrder())));
                    break;
                case "release_date_desc":
                    books.sort(Comparator.comparing(Book::getRelease_date, Comparator.nullsFirst(Comparator.reverseOrder())));
                    break;
            }
        }
        
        // Apply pagination if more than pageSize items
        if (books.size() > pageSize) {
            int start = page * pageSize;
            int end = Math.min(start + pageSize, books.size());
            if (start < books.size()) {
                return books.subList(start, end);
            } else {
                return new ArrayList<>();
            }
        }
        
        return books;
    }
    
    public int getTotalPages(List<Long> genreIds, List<String> languages, Boolean adultCheck, String sortBy, String search, int pageSize) {
        List<Book> books = getBooksFilteredAndSorted(genreIds, languages, adultCheck, sortBy, search, 0, Integer.MAX_VALUE);
        return (int) Math.ceil((double) books.size() / pageSize);
    }

    public Book getBookById(long id) {
        return bookRepository.findById(id).get();
    }

    @Transactional
    public void deleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null) {
            // Delete all BookAuthor relationships using repository
            List<BookAuthor> bookAuthors = bookAuthorRepository.findAll().stream()
                .filter(ba -> ba.getBook() != null && ba.getBook().getId().equals(bookId))
                .collect(Collectors.toList());
            bookAuthorRepository.deleteAll(bookAuthors);
            
            // Delete all BookGenre relationships using repository
            List<BookGenre> bookGenres = bookGenreRepository.findAll().stream()
                .filter(bg -> bg.getBook() != null && bg.getBook().getId().equals(bookId))
                .collect(Collectors.toList());
            bookGenreRepository.deleteAll(bookGenres);
            
            // Delete all BookSeries relationships using repository
            List<BookSeries> bookSeries = bookSeriesRepository.findAll().stream()
                .filter(bs -> bs.getBook() != null && bs.getBook().getId().equals(bookId))
                .collect(Collectors.toList());
            bookSeriesRepository.deleteAll(bookSeries);
            
            // Delete all UserWishlist relationships using repository
            List<mas.curs.infsys.models.UserWishlist> wishlistItems = userWishlistRepository.findAll().stream()
                .filter(wl -> wl.getBook() != null && wl.getBook().getId().equals(bookId))
                .collect(Collectors.toList());
            userWishlistRepository.deleteAll(wishlistItems);
            
            bookRepository.delete(book);
        }
    }

    public boolean addBook(Book book) {
        bookRepository.save(book);
        return true;
    }

    public Book addBookAndReturn(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public boolean updateBook(Book book) {
        // Load existing book to preserve relationships (especially UserWishlist)
        Book existingBook = bookRepository.findById(book.getId()).orElse(null);
        if (existingBook == null) {
            return false;
        }
        
        // Preserve UserWishlist relationships
        Set<UserWishlist> existingWishlist = existingBook.getUserWishlist();
        
        // Update all fields from the new book
        existingBook.setTitle(book.getTitle());
        existingBook.setDescription(book.getDescription());
        existingBook.setRelease_date(book.getRelease_date());
        existingBook.setIsbn(book.getIsbn());
        existingBook.setPrice(book.getPrice());
        existingBook.setLanguage(book.getLanguage());
        existingBook.setPages(book.getPages());
        existingBook.setStatus(book.getStatus());
        existingBook.setImage_url(book.getImage_url());
        existingBook.setAdult_check(book.isAdult_check());
        
        // Restore UserWishlist relationships
        existingBook.setUserWishlist(existingWishlist);
        
        bookRepository.save(existingBook);
        return true;
    }

    @Transactional
    public void updateBookAuthors(Long bookId, List<Long> authorIds, Long mainAuthorId) {
        Book book = bookRepository.findById(bookId).orElseThrow();
        book.getBookAuthor().clear();
        
        if (authorIds != null && !authorIds.isEmpty()) {
            for (Long authorId : authorIds) {
                Author author = authorService.getAuthorById(authorId);
                boolean isMain = (mainAuthorId != null && mainAuthorId.equals(authorId));
                BookAuthor bookAuthor = new BookAuthor(author, book, isMain);
                // Initialize the embedded ID
                BookAuthorId id = new BookAuthorId(authorId, bookId);
                bookAuthor.setId(id);
                book.getBookAuthor().add(bookAuthor);
            }
        }
        
        bookRepository.save(book);
    }

    @Transactional
    public void updateBookGenres(Long bookId, List<Long> genreIds) {
        Book book = bookRepository.findById(bookId).orElseThrow();
        book.getBookGenre().clear();
        
        if (genreIds != null && !genreIds.isEmpty()) {
            for (Long genreId : genreIds) {
                Genre genre = genreService.getGenreById(genreId);
                BookGenre bookGenre = new BookGenre(genre, book);
                // Initialize the embedded ID
                BookGenreId id = new BookGenreId(genreId, bookId);
                bookGenre.setId(id);
                book.getBookGenre().add(bookGenre);
            }
        }
        
        bookRepository.save(book);
    }

    @Transactional
    public void updateBookSeries(Long bookId, List<Long> seriesIds) {
        Book book = bookRepository.findById(bookId).orElseThrow();
        book.getBookSeries().clear();
        
        if (seriesIds != null && !seriesIds.isEmpty()) {
            for (Long seriesId : seriesIds) {
                Series series = seriesService.getSeriesById(seriesId);
                BookSeries bookSeries = new BookSeries(series, book);
                // Initialize the embedded ID
                BookSeriesId id = new BookSeriesId(seriesId, bookId);
                bookSeries.setId(id);
                book.getBookSeries().add(bookSeries);
            }
        }
        
        bookRepository.save(book);
    }

    @Transactional
    public void setBookAuthors(Long bookId, List<Long> authorIds, Long mainAuthorId) {
        updateBookAuthors(bookId, authorIds, mainAuthorId);
    }

    @Transactional
    public void setBookGenres(Long bookId, List<Long> genreIds) {
        updateBookGenres(bookId, genreIds);
    }

    @Transactional
    public void setBookSeries(Long bookId, List<Long> seriesIds) {
        updateBookSeries(bookId, seriesIds);
    }

    public List<Book> getBooksByGenre(Long genreId) {
        return getAllBooks().stream()
                .filter(book -> book.getBookGenre().stream()
                        .anyMatch(bg -> bg.getGenre().getId().equals(genreId)))
                .collect(Collectors.toList());
    }

    public List<Book> getBooksByAuthor(Long authorId) {
        return getAllBooks().stream()
                .filter(book -> book.getBookAuthor().stream()
                        .anyMatch(ba -> ba.getAuthor().getId().equals(authorId)))
                .collect(Collectors.toList());
    }

    public List<Book> getBooksBySeries(Long seriesId) {
        return getAllBooks().stream()
                .filter(book -> book.getBookSeries().stream()
                        .anyMatch(bs -> bs.getSeries().getId().equals(seriesId)))
                .collect(Collectors.toList());
    }

    public List<Book> getBooksByReleaseDate(java.time.LocalDate date) {
        return getAllBooks().stream()
                .filter(book -> book.getRelease_date() != null && book.getRelease_date().equals(date))
                .filter(book -> book.getStatus() == BookStatus.SOON)
                .sorted(Comparator.comparing(Book::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());
    }

    public List<java.time.LocalDate> getUpcomingReleaseDates() {
        java.time.LocalDate today = java.time.LocalDate.now();
        return getAllBooks().stream()
                .filter(book -> book.getRelease_date() != null)
                .filter(book -> book.getRelease_date().isAfter(today.minusDays(1))) // Include today and future dates
                .filter(book -> book.getStatus() == BookStatus.SOON)
                .map(Book::getRelease_date)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Book> updateBookStatuses() {
        java.time.LocalDate today = java.time.LocalDate.now();
        List<Book> updatedBooks = new ArrayList<>();
        
        List<Book> books = getAllBooks();
        for (Book book : books) {
            if (book.getStatus() == BookStatus.SOON 
                && book.getRelease_date() != null 
                && !book.getRelease_date().isAfter(today)) {
                book.setStatus(BookStatus.RELEASED);
                bookRepository.save(book);
                updatedBooks.add(book);
            }
        }
        
        return updatedBooks;
    }

}

package ua.edu.chnu.security;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class BookService {
    private final BookRepository books;
    private final AuditRepository audit;
    public BookService(BookRepository books, AuditRepository audit) { this.books = books; this.audit = audit; }
    public Book find(long id) { return books.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)); }
    @Transactional
    public Book create(String title, String author, String user) {
        Book b = books.save(new Book(title, author));
        audit.save(new AuditEntry(user, "CREATE", b.id)); return b;
    }
    @Transactional
    public Book update(long id, String title, String author, String user) {
        Book b = find(id); b.title = title; b.author = author;
        audit.save(new AuditEntry(user, "UPDATE", id)); return books.save(b);
    }
    @Transactional
    public void delete(long id, String user) {
        books.delete(find(id)); audit.save(new AuditEntry(user, "DELETE", id));
    }
}

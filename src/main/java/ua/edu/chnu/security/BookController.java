package ua.edu.chnu.security;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/books", "/basic/books"})
public class BookController {
    public record BookInput(@NotBlank @Size(max=200) String title, @NotBlank @Size(max=200) String author) {}
    private final BookRepository books;
    private final BookService service;
    public BookController(BookRepository books, BookService service) { this.books = books; this.service = service; }
    @GetMapping @PreAuthorize("hasAnyRole('READER','EDITOR','ADMIN')")
    public List<Book> all() { return books.findAll(); }
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('READER','EDITOR','ADMIN')")
    public Book one(@PathVariable long id) { return service.find(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('EDITOR','ADMIN')")
    public Book create(@Valid @RequestBody BookInput body, Principal user) { return service.create(body.title(), body.author(), user.getName()); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('EDITOR','ADMIN')")
    public Book update(@PathVariable long id, @Valid @RequestBody BookInput body, Principal user) { return service.update(id, body.title(), body.author(), user.getName()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable long id, Principal user) { service.delete(id, user.getName()); }
}

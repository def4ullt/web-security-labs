package ua.edu.chnu.security;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping({"/api/audit", "/basic/audit"})
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {
    private final AuditRepository audit;
    public AuditController(AuditRepository audit) { this.audit = audit; }
    @GetMapping public List<AuditEntry> all() { return audit.findTop100ByOrderByIdDesc(); }
    @GetMapping("/{id}") public AuditEntry one(@PathVariable long id) { return audit.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)); }
    @GetMapping("/user/{name}") public List<AuditEntry> user(@PathVariable String name) { return audit.findTop100ByUsernameOrderByIdDesc(name); }
    @GetMapping("/action/{action}") public List<AuditEntry> action(@PathVariable String action) { return audit.findTop100ByActionOrderByIdDesc(action); }
    @GetMapping("/book/{id}") public List<AuditEntry> book(@PathVariable long id) { return audit.findTop100ByBookIdOrderByIdDesc(id); }
}

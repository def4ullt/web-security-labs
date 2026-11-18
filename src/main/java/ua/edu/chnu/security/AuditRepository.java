package ua.edu.chnu.security;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AuditRepository extends JpaRepository<AuditEntry, Long> {
    List<AuditEntry> findTop100ByOrderByIdDesc();
    List<AuditEntry> findTop100ByUsernameOrderByIdDesc(String username);
    List<AuditEntry> findTop100ByActionOrderByIdDesc(String action);
    List<AuditEntry> findTop100ByBookIdOrderByIdDesc(Long bookId);
}

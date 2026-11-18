package ua.edu.chnu.security;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class AuditEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public Instant occurredAt;
    public String username;
    public String action;
    public Long bookId;
    protected AuditEntry() {}
    public AuditEntry(String username, String action, Long bookId) {
        this.occurredAt = Instant.now(); this.username = username; this.action = action; this.bookId = bookId;
    }
}

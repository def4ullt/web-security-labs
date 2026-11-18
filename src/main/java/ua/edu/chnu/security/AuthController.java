package ua.edu.chnu.security;
import java.time.Instant;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthController {
    public record Login(@NotBlank String username, @NotBlank String password) {}
    private final UserDetailsService users;
    private final PasswordEncoder passwords;
    private final JwtEncoder encoder;
    public AuthController(UserDetailsService users, PasswordEncoder passwords, JwtEncoder encoder) {
        this.users = users; this.passwords = passwords; this.encoder = encoder;
    }
    @PostMapping("/auth/token")
    public Map<String,Object> token(@Valid @RequestBody Login login) {
        UserDetails user;
        try { user = users.loadUserByUsername(login.username()); }
        catch (UsernameNotFoundException e) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED); }
        if (!passwords.matches(login.password(), user.getPassword())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("web-security-labs").subject(user.getUsername())
            .issuedAt(now).expiresAt(now.plusSeconds(900))
            .claim("roles", user.getAuthorities().stream().map(a -> a.getAuthority().substring(5)).toList()).build();
        String token = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return Map.of("access_token", token, "token_type", "Bearer", "expires_in", 900);
    }
    @GetMapping("/basic/csrf")
    public Map<String,String> csrf(CsrfToken token) { return Map.of("token", token.getToken(), "headerName", token.getHeaderName()); }
}

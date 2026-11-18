package ua.edu.chnu.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:tests;DB_CLOSE_DELAY=-1", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class AccessTests {
    @Autowired MockMvc mvc;
    @Autowired BookRepository books;
    @Autowired AuditRepository audit;
    @Autowired ObjectMapper json;
    long id;
    static final String BODY = "{\"title\":\"Clean Code\",\"author\":\"Robert Martin\"}";
    @BeforeEach void seed() { audit.deleteAll(); books.deleteAll(); id = books.save(new Book("Test", "Author")).id; }
    @Test void anonymousCannotRead() throws Exception { mvc.perform(get("/basic/books")).andExpect(status().isUnauthorized()); }
    @Test void wrongPasswordRejected() throws Exception { mvc.perform(get("/basic/books").with(httpBasic("reader","wrong"))).andExpect(status().isUnauthorized()); }
    @Test void readerCanList() throws Exception { mvc.perform(get("/basic/books").with(httpBasic("reader","Reader123!"))).andExpect(status().isOk()); }
    @Test void readerCanReadOne() throws Exception { mvc.perform(get("/basic/books/"+id).with(httpBasic("reader","Reader123!"))).andExpect(status().isOk()); }
    @Test void readerCannotCreate() throws Exception { mvc.perform(post("/basic/books").with(httpBasic("reader","Reader123!")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isForbidden()); }
    @Test void editorCanCreate() throws Exception { mvc.perform(post("/basic/books").with(httpBasic("editor","Editor123!")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isCreated()); }
    @Test void editorCanUpdate() throws Exception { mvc.perform(put("/basic/books/"+id).with(httpBasic("editor","Editor123!")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isOk()); }
    @Test void editorCannotDelete() throws Exception { mvc.perform(delete("/basic/books/"+id).with(httpBasic("editor","Editor123!")).with(csrf())).andExpect(status().isForbidden()); }
    @Test void adminCanDelete() throws Exception { mvc.perform(delete("/basic/books/"+id).with(httpBasic("admin","Admin123!")).with(csrf())).andExpect(status().isNoContent()); }
    @Test void adminNeedsCsrf() throws Exception { mvc.perform(delete("/basic/books/"+id).with(httpBasic("admin","Admin123!"))).andExpect(status().isForbidden()); }
    private String token(String name, String password) throws Exception {
        String response = mvc.perform(post("/auth/token").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(java.util.Map.of("username",name,"password",password))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("access_token").asText();
    }
    @Test void signedJwtAllowsRead() throws Exception { mvc.perform(get("/api/books").header("Authorization","Bearer "+token("reader","Reader123!"))).andExpect(status().isOk()); }
    @Test void jwtReaderCannotWrite() throws Exception { mvc.perform(post("/api/books").header("Authorization","Bearer "+token("reader","Reader123!")).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isForbidden()); }
    @Test void invalidJwtRejected() throws Exception { mvc.perform(get("/api/books").header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized()); }
    @Test void basicCannotAuthenticateJwtRoute() throws Exception { mvc.perform(get("/api/books").with(httpBasic("admin","Admin123!"))).andExpect(status().isUnauthorized()); }
    @Test void editorCannotReadAudit() throws Exception { mvc.perform(get("/api/audit").header("Authorization","Bearer "+token("editor","Editor123!"))).andExpect(status().isForbidden()); }
    @Test void jwtWriteCreatesAudit() throws Exception {
        mvc.perform(post("/api/books").header("Authorization","Bearer "+token("editor","Editor123!")).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isCreated());
        mvc.perform(get("/api/audit").header("Authorization","Bearer "+token("admin","Admin123!")))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].username").value("editor")).andExpect(jsonPath("$[0].action").value("CREATE"));
    }
    @Test void emptyTitleRejected() throws Exception { mvc.perform(post("/basic/books").with(httpBasic("admin","Admin123!")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\",\"author\":\"X\"}")).andExpect(status().isBadRequest()); }
    @Test void missingBookIs404() throws Exception { mvc.perform(get("/basic/books/999999").with(httpBasic("reader","Reader123!"))).andExpect(status().isNotFound()); }
    @Test void entirePostmanSequenceWithRealCsrfAndJwt() throws Exception {
        var collection = json.readTree(java.nio.file.Path.of("requests/BookShelf.postman_collection.json").toFile());
        var vars = new java.util.HashMap<String,String>();
        for (var variable : collection.get("variable")) vars.put(variable.get("key").asText(), variable.get("value").asText());
        org.springframework.mock.web.MockHttpSession session = new org.springframework.mock.web.MockHttpSession();
        int count = 0;
        for (var folder : collection.get("item")) for (var item : folder.get("item")) {
            var r = item.get("request");
            String method = r.get("method").asText();
            String url = expand(r.get("url").asText(), vars).replace(vars.get("base"), "");
            var request = request(org.springframework.http.HttpMethod.valueOf(method), url).session(session);
            for (var header : r.get("header")) request.header(header.get("key").asText(), expand(header.get("value").asText(), vars));
            if (r.has("auth")) {
                var auth = r.get("auth");
                if (auth.get("type").asText().equals("basic")) request.with(httpBasic(expand(auth.get("basic").get(0).get("value").asText(),vars), expand(auth.get("basic").get(1).get("value").asText(),vars)));
                else request.header("Authorization", "Bearer " + expand(auth.get("bearer").get(0).get("value").asText(), vars));
            }
            if (r.has("body")) request.content(expand(r.get("body").get("raw").asText(),vars));
            var match = java.util.regex.Pattern.compile("\\[(\\d+)\\]").matcher(item.get("name").asText());
            Assertions.assertTrue(match.find());
            var result = mvc.perform(request).andReturn();
            Assertions.assertEquals(Integer.parseInt(match.group(1)), result.getResponse().getStatus(), item.get("name").asText());
            session = (org.springframework.mock.web.MockHttpSession) result.getRequest().getSession(false);
            if (session == null) session = new org.springframework.mock.web.MockHttpSession();
            String response = result.getResponse().getContentAsString();
            if (!response.isBlank()) {
                var data = json.readTree(response);
                if (url.equals("/basic/csrf")) vars.put("csrf",data.get("token").asText());
                if (method.equals("POST") && url.endsWith("/books") && result.getResponse().getStatus()==201) vars.put("bookId",data.get("id").asText());
                if (url.equals("/auth/token")) vars.put(json.readTree(r.get("body").get("raw").asText()).get("username").asText()+"Token",data.get("access_token").asText());
                if (url.equals("/api/audit") && data.isArray() && !data.isEmpty()) vars.put("auditId",data.get(0).get("id").asText());
            }
            count++;
        }
        Assertions.assertEquals(39,count);
    }
    private static String expand(String value, java.util.Map<String,String> vars) {
        for (var entry : vars.entrySet()) value = value.replace("{{"+entry.getKey()+"}}",entry.getValue());
        return value;
    }
}

# BookShelf — Web-application security

Навчальний проєкт з нуля для практичних 1–10. Java 21, Maven, Spring Boot 3.5.16, Spring Security, JPA, H2. Це самостійна реалізація за скопійованими умовами Moodle; 
## Запуск

Відкрий папку як Maven-проєкт в IntelliJ IDEA або VS Code.

```powershell
mvn test
mvn spring-boot:run
```

Сторінка: http://localhost:8080. База зберігається в `data/`, після перезапуску книги й аудит залишаються. Зупинка: Ctrl+C. Порт доступний лише з цього комп'ютера.

## Навчальні облікові записи

| Користувач | Пароль | Роль | Доступ |
|---|---|---|---|
| reader | Reader123! | READER | Читання |
| editor | Editor123! | EDITOR | Читання, створення, зміна |
| admin | Admin123! | ADMIN | Усе, видалення, аудит |

Паролі в пам'яті зберігаються у BCrypt. Усі маршрути книг захищені з першого запуску.

## Два способи доступу

- `/basic/books` і `/basic/audit`: Basic Auth. Для POST/PUT/DELETE спочатку GET `/basic/csrf` з авторизацією; зберегти cookie JSESSIONID та передати отриманий token заголовком X-CSRF-TOKEN у тій самій сесії.
- POST `/auth/token`: JSON з username/password; повертає access_token на 15 хвилин.
- `/api/books` і `/api/audit`: тільки Authorization: Bearer TOKEN. Без cookies і серверної сесії; CSRF вимкнений лише для цієї групи маршрутів.
- JWT підписується RS256. Ключ генерується при старті; старі токени після перезапуску не працюють. Перевіряються підпис, issuer та строк дії.
- CORS і CSRF — різні механізми. Міждоменний доступ для окремого frontend у цьому проєкті не відкритий.

## Практичні

Дивись `docs/SUBMISSION.md`, `requests/basic.http` та `requests/jwt.http`. Postman-колекція містить автоматичне збереження JWT, CSRF і ID створеної книги. Запускай папки послідовно. Очікувані 401/403 — це успішна демонстрація обмеження доступу, а не помилка лабораторної.

19 JUnit-тестів: 10 Basic/ролі/CSRF, 8 перевірок JWT, аудиту, валідації та один наскрізний прогін усіх 39 запитів Postman-колекції через MockMvc. Звіт Maven: `target/surefire-reports`.

`Verify-Requests.ps1` для PowerShell 7 виконує ті самі 39 запитів через HTTP до вже запущеного сервера й записує таблицю результатів без токенів у `docs/request-verification.csv`.

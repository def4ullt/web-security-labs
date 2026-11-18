# Перевірка 18.11.2026

- `mvn -o package`: BUILD SUCCESS.
- JUnit: 19 tests, 0 failures, 0 errors, 0 skipped.
- Наскрізний тест виконав усі 39 запитів з Postman-колекції через MockMvc, справжні Basic credentials, CSRF та підписані RS256 JWT; усі статуси відповідають очікуваним.
- Створено виконуваний `target/web-security-labs-1.0.0.jar`.
- HTTP-запуск у середовищі агента заблокований: Tomcat отримує `Permission denied: connect` при створенні loopback-з'єднання. Сервер не залишений працювати. Потрібен запуск у звичайному терміналі користувача.
- Спроба створити `C:\Users\hedge\OneDrive\Desktop\WebSecurityLabs` завершилася Access denied. Проєкт збережений у робочій папці; Copy-ToDesktop.ps1 призначений для запуску користувачем.
- GitHub і лекційні приклади недоступні з цього середовища. Віддалений репозиторій не створений. Реальні скриншоти Postman/Talend та jwt.io ще потрібні для здачі.

Текстовий підсумок JUnit: `junit-summary.txt`. Це звіт тестів, не заміна скриншотів із завдань.

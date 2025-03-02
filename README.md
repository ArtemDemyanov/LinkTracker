![Build](https://github.com/central-university-dev/backend-academy-2025-spring-template/actions/workflows/build.yaml/badge.svg)

# Link Tracker

Этот проект состоит из двух приложений:
1. **Scrapper** — сервис для отслеживания изменений на GitHub и Stack Overflow.
2. **Bot** — Telegram бот, который взаимодействует с пользователями и отправляет уведомления об обновлениях.

## Требования

- Java 17 или выше
- Maven 3.8.6 или выше
- Telegram бот (для получения уведомлений)
- Токен доступа к API GitHub
- Токен и ключ доступа к API Stack Overflow

---

## Настройка

### 1. **Создайте Telegram бота**:

- Откройте Telegram и найдите бота `@BotFather`.
- Создайте нового бота с помощью команды `/newbot`.
- Сохраните токен, который вы получите от `@BotFather`.

### 2. **Получите токен доступа к API GitHub**:

- Перейдите в [GitHub Developer Settings](https://github.com/settings/tokens).
- Создайте новый токен с необходимыми разрешениями (например, `repo` для доступа к репозиториям).

### 3. **Получите токен и ключ доступа к API Stack Overflow**:

- Перейдите на [Stack Apps](https://stackapps.com/) и зарегистрируйте новое приложение.
- Получите ключ и токен доступа.

### 4. **Настройка конфигурации**:

#### Для **Scrapper**:

Создайте файл `application.yml` в директории `src/main/resources` с следующим содержимым:

```yaml
app:
  github-token: YOUR_GITHUB_TOKEN
  github-base-url: https://api.github.com
  stack-overflow:
    key: YOUR_STACKOVERFLOW_KEY
    access-token: YOUR_STACKOVERFLOW_ACCESS_TOKEN
  stack-overflow-base-url: https://api.stackexchange.com
```

#### Для **Bot**:

Создайте файл `application.yml` в директории `src/main/resources` с следующим содержимым:

```yaml
app:
  telegram-token: YOUR_TELEGRAM_BOT_TOKEN
```

Замените `YOUR_GITHUB_TOKEN`, `YOUR_STACKOVERFLOW_KEY`, `YOUR_STACKOVERFLOW_ACCESS_TOKEN` и `YOUR_TELEGRAM_BOT_TOKEN` на соответствующие значения.

---

## Сборка и запуск

### 1. **Сборка проектов**:

- Откройте терминал в корневой директории каждого проекта (`scrapper` и `bot`).
- Выполните команду для сборки проекта:

  ```bash
  mvn clean install
  ```

### 2. **Запуск Scrapper**:

- Перейдите в директорию `scrapper`.
- Запустите приложение:

  ```bash
  mvn spring-boot:run
  ```

Scrapper запустится на порту `8081` (по умолчанию).

### 3. **Запуск Bot**:

- Перейдите в директорию `bot`.
- Запустите приложение:

  ```bash
  mvn spring-boot:run
  ```

Bot запустится на порту `8080` (по умолчанию).

---

## Использование

### 1. **Регистрация чата**:

- Отправьте команду `/start` вашему Telegram боту. Это зарегистрирует ваш чат в системе.

### 2. **Добавление ссылок**:

- Отправьте команду `/track` и следуйте инструкциям для добавления ссылки на GitHub репозиторий или вопрос Stack Overflow.

### 3. **Получение уведомлений**:

- Scrapper будет периодически проверять обновления на добавленных ссылках и отправлять уведомления в Telegram через Bot.

### 4. **Удаление ссылок**:

- Отправьте команду `/untrack` и следуйте инструкциям для удаления ссылки из отслеживания.

### 5. **Просмотр списка ссылок**:

- Отправьте команду `/list`, чтобы получить список всех отслеживаемых ссылок.

---

## API Документация

После запуска приложений, документация API будет доступна по следующим адресам:

### Scrapper:

- **Swagger UI**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

### Bot:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Логирование

Логи приложений записываются в консоль.

---

## Лицензия

Этот проект распространяется под лицензией MIT. Подробности см. в файле [LICENSE](LICENSE).

---

## Структура проекта

- **scrapper**: Сервис для отслеживания изменений на GitHub и Stack Overflow.
- **bot**: Telegram бот для взаимодействия с пользователями и отправки уведомлений.

---


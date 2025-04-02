# 🚀 Инструкция по запуску Link Tracker

## 📋 Предварительные требования

- Java 17+ ([скачать](https://adoptium.net/))
- Maven 3.6+ ([установка](https://maven.apache.org/install.html))
- Docker ([установка](https://docs.docker.com/engine/install/))
- PostgreSQL 12+ (или Docker)
- Telegram бот (создать через [@BotFather](https://t.me/BotFather))

## 🛠 Настройка окружения

### 1. Конфигурация приложения

Создайте файл application.yml в src/main/resources:

```
app:
telegram:
token: "ВАШ_TELEGRAM_BOT_TOKEN"
username: "ВашBotName"

github:
token: "ваш_github_token"

stackoverflow:
key: "ваш_stackoverflow_key"
accessToken: "ваш_stackoverflow_token"

scheduler:
batch-size: 100      # Размер пакета для обработки
check-interval: 300  # Интервал проверки в секундах

spring:
datasource:
url: jdbc:postgresql://localhost:5432/linktracker
username: postgres
password: postgres
jpa:
hibernate:
ddl-auto: validate
```

### 2. Запуск PostgreSQL через Docker

Создайте docker-compose.yml:

```postgresql:
image: postgres:17
ports:
- "5433:5432"
environment:
POSTGRES_DB: scrapper
POSTGRES_USER: postgres
POSTGRES_PASSWORD: postgres
networks:
- backend
```

### 3. Запуск миграций через Docker

Миграции Liquibase выполняются автоматически при запуске приложения.
Файлы миграций должны находиться в корне проекта в директории migrations

В файле docker-compose.yml:

```
liquibase-migrations:
image: liquibase/liquibase:4.25
depends_on:
- postgresql
command:
- --changelog-file=master.xml
- --driver=org.postgresql.Driver
- --url=jdbc:postgresql://postgresql:5432/scrapper
- --username=postgres
- --password=postgres
- update
volumes:
- ./migrations:/liquibase/changelog
networks:
- backend
volumes:
postgresql_data:
pgadmin-data:
```

Далее запустите сам Docker:

```
docker-compose up -d --build
```

Затем запустите модули bot и scrapper прямо через редактор кода, либо же с помощью

```
mvn exec:exec@run-scrapper exec:exec@run-bot
```

Для дополнительной справки: [HELP.md](./HELP.md)

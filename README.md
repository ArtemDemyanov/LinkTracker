# 🚀 Инструкция по запуску Link Tracker

## 📋 Предварительные требования

- Java 17+ ([скачать](https://adoptium.net/))
- Maven 3.6+ ([установка](https://maven.apache.org/install.html))
- Docker ([установка](https://docs.docker.com/engine/install/))
- PostgreSQL 12+ (или Docker)
- Telegram бот (создать через [@BotFather](https://t.me/BotFather))

## 🛠 Настройка окружения

### 1. Конфигурация приложения

Создайте файл application.yml в scrapper/src/main/resources:

```
app:
github:
token: "${GITHUB_TOKEN}"
base-url: "https://api.github.com"
connection-timeout: 10s
read-timeout: 30s
max-retries: 3
retry-delay: 1s
stackoverflow:
base-url: "https://api.stackexchange.com/2.3"
connection-timeout: 10s
read-timeout: 30s
max-retries: 3
retry-delay: 1s
api:
key: "${SO_TOKEN_KEY}"
access-token: "${SO_ACCESS_TOKEN}"
scheduling:
batch-size: 100
app:
access-type: "ORM"
bot-url: "http://localhost:8080"
message-transport: KAFKA

spring:
datasource:
url: jdbc:postgresql://localhost:5433/your_database_name
username: postgres
password: postgres
driver-class-name: org.postgresql.Driver
application:
name: Scrapper
liquibase:
enabled: false
jpa:
hibernate:
ddl-auto: validate
open-in-view: false

server:
port: 8081

springdoc:
swagger-ui:
enabled: true
path: /swagger-ui

kafka:
bootstrap-servers: localhost:9092
topics:
updates: link-updates
dlq: link-updates-dlq
```

Создайте файл application.yml в bot/src/main/resources:

```
app:
telegram-token: ${TELEGRAM_TOKEN} # env variable
scrapper-url: http://localhost:8081
message-transport: KAFKA

spring:
application:
name: Bot
liquibase:
enabled: false
jpa:
hibernate:
ddl-auto: validate
open-in-view: false
cache:
type: redis
data:
redis:
host: localhost
port: 6379

kafka:
bootstrap-servers: localhost:9092
topics:
updates: link-updates
dlq: link-updates-dlq

server:
port: 8080

springdoc:
swagger-ui:
enabled: true
path: /swagger-ui
### 2. Запуск PostgreSQL, Kafka через Docker
```

Создайте docker-compose.yml:

```
services:

postgresql:
image: postgres:17
ports:
- "5433:5432"
environment:
POSTGRES_DB: database_name
POSTGRES_USER: postgres
POSTGRES_PASSWORD: postgres
#    volumes:
#      - postgresql:/var/lib/postgresql/data
networks:
- backend

zookeeper:
image: confluentinc/cp-zookeeper:7.3.0
environment:
ZOOKEEPER_CLIENT_PORT: 2181
ZOOKEEPER_TICK_TIME: 2000
ports:
- "2181:2181"

kafka:
image: confluentinc/cp-kafka:7.3.0
depends_on:
- zookeeper
ports:
- "9092:9092"
environment:
KAFKA_BROKER_ID: 1
KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT
KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
volumes:
postgresql_data:
pgadmin-data:

networks:
backend:
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
- --url=jdbc:postgresql://postgresql:5432/your_database_name
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

## Написать CRUD приложение

с использованием Spring Data JPA, реализовать контроллер и сервис для выполнения CRUD операций.
Реализовать кастомные исключения и их обработку на уровне контроллера, с присвоением соответствующих HTTP статусов и читаемых, понятных сообщений.
Также необходимо подключить к проекту БД PostgreSQL и обеспечить миграцию данных при помощи liquibase.
Основной функционал должен быть покрыт unit-тестами.

## UPD: Сделайте шедулер, который будет обновлять курсы валют раз в сутки.

И сделайте их 2, один будет фейковый (не будет реально обновлять, а просто скажет типа обновил), а второй реально сходит в цб и обновит.
Курсы валют тут брать: https://cbr.ru/scripts/XML_daily.asp?date_req=02/03/2002
Можно прям ее дернуть через фейн или рестклиент

## Прикрутить Kafka к проекту

Реализовать Producer, Consumer, топики


# Запуск проекта

psql -U roman - вход в БД
mvnd clean test - запуск тестов
mvnd clean install -DskipTests - сборка без тестов
mvnd spring-boot:run -e  - запуск проекта

найти процесс
netstat -ano | findstr :8080
завершить процесс
taskkill /PID 30628 /F 

# Очистить Kafka топик
Вариант 1: Удалить и пересоздать топик через Docker
bash
# Зайти в контейнер Kafka
docker exec -it kafka bash

# Удалить топик
kafka-topics --bootstrap-server localhost:9092 --delete --topic exchange-rates

# Удалить топик user-events (если нужно)
kafka-topics --bootstrap-server localhost:9092 --delete --topic user-events

# Выйти из контейнера
exit

Вариант 2: Через docker-compose
bash
# Остановить контейнеры
docker-compose down

# Удалить все томы (это удалит все данные Kafka)
docker-compose down -v

# Запустить заново
docker-compose up -d

После очистки Kafka:
Шаг 1: Перезагрузите приложение
bash
# Остановите текущее приложение (Ctrl + C)
# Затем запустите его снова
mvnd spring-boot:run -e

Шаг 2: Тестируйте снова
bash
curl -X POST http://localhost:8080/api/rates/update

Теперь в логах должны быть сообщения:

text
[INFO] Successfully parsed 185 exchange rates
[INFO] Saved and sent to Kafka: USD = 100.5000
[INFO] Received event from Kafka: code=USD, rate=100.5000
[INFO] Exchange rate saved from Kafka: code=USD, rate=100.5000
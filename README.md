CarRentApp

Opis projektu
CarRentApp to aplikacja do zarządzania wypożyczalnią samochodów napisana w oparciu o Spring Boot. 
Umożliwia dwóm typom użytkowników (USER i ADMIN) zarządzanie flotą pojazdów oraz rezerwacjami. 
W projekcie wykorzystano wzorce projektowe, zasady SOLID oraz konteneryzację za pomocą Dockera.


Technologie

Język: Java 21
Framework: Spring Boot (Web, Data JPA, Security, Actuator)
ORM: Hibernate
Baza danych: PostgreSQL
Migracje: Flyway
Konteneryzacja: Docker, Docker Compose
Build & Dependency Management: Maven
Testy: JUnit 5, Testcontainers, Mockito
Dokumentacja API: Springdoc OpenAPI (Swagger UI)
Pokrycie testów: JaCoCo


Uruchomienie (Docker)

Uruchom usługi:
docker-compose up --build
Aplikacja: http://localhost:8080
Baza PostgreSQL: port 5433, nazwa bazy car_rent, użytkownik myuser, hasło mypassword.


Struktura projektu

============================================================================
src/
├─ main/
│  ├─ java/org/example/carrentapp/
│  │  ├─ controller/      # REST API (Cars, Users, Roles, Reservations)
│  │  ├─ service/         # Logika biznesowa
│  │  ├─ repository/      # Spring Data JPA
│  │  ├─ entity/          # Encje JPA (Car, User, Role, Reservation)
│  │  ├─ dto/             # Obiekty DTO (LoginDto, ReservationDto, UserDto)
│  │  └─ security/        # Konfiguracja Spring Security, OpenAPI
│  └─ resources/
│     ├─ application.yml  # Konfiguracja aplikacji
│     └─ db/migration/    # Skrypty Flyway (V1__init.sql)
└─ test/                  # Testy jednostkowe i integracyjne
Dockerfile
docker-compose.yml
pom.xml
============================================================================

Architektura i wzorce projektowe

Controllers – obsługa żądań HTTP
Services – logika biznesowa
Repositories – dostęp do danych (Spring Data JPA)
Entities & DTOs – model domenowy i transfer danych

Zastosowane wzorce

Repository Pattern – abstrakcja dostępu do bazy danych
Dependency Injection – zarządzanie zależnościami przez Spring
Strategy – PasswordEncoder jako wymienna strategia szyfrowania
Builder – tworzenie instancji UserDetails w konfiguracji bezpieczeństwa
Polimorfizm – interfejs IfAvailable i klasy EconomyCar, LuxuryCar


Zastosowane zasady SOLID:

Single Responsibility: Każda klasa ma jedno zadanie (np. CarService zarządza logiką samochodów)
Open/Closed: Rozszerzalność przez dziedziczenie (LuxuryCar, EconomyCar)
Liskov Substitution: Klasy pochodne mogą zastąpić klasę bazową Car
Interface Segregation: Wąskie interfejsy (IfAvailable)
Dependency Inversion: Zależności od abstrakcji a nie implementacji

Baza danych i diagram ERD

---- Diagram ERD bazy danych umieszczony w plikach. ----- 

W dokumentacji warto zaznaczyć, że tabela user_roles pełni rolę tabeli pomocniczej (join table) służącej do odwzorowania relacji wiele-do-wielu między użytkownikami a rolami. Dzięki niej:

users ⇄ roles
Jeden użytkownik może mieć wiele ról, a jedna rola może być przypisana wielu użytkownikom.

Tabela user_roles przechowuje jedynie klucze obce user_id i role_id, bez dodatkowych atrybutów, 
służy wyłącznie do pośredniczenia w tej relacji.
Dzięki takiemu podejściu model bazy pozostaje przejrzysty, 
a Hibernate/JPA bez problemu może mapować tę relację jako @ManyToMany.


Migracje bazy danych

Skrypty Flyway w src/main/resources/db/migration.
V1__init.sql – utworzenie tabel: roles, users, user_roles, cars, reservations.


Opis API (Swagger UI)

Dokumentacja dostępna pod /swagger-ui/index.html.

Obsługiwane endpointy:

Cars: GET /api/cars, GET /api/cars/available, POST, PUT, DELETE.
Users: GET /api/users, POST, PUT, DELETE.
Roles: analogicznie.
Reservations: GET /api/reservations, POST, PUT, DELETE.

Zabezpieczenia

Spring Security z HTTP Basic Auth.
Role ADMIN i USER z precyzyjną kontrolą dostępu w SecurityConfig.
Hasła hashowane BCryptPasswordEncoder.
Wyłączone CSRF dla API.


Testy i pokrycie

Unit tests: 94% pokrycia kodu.
Integration tests: 86% pokrycia.
Narzędzia: JUnit 5, Testcontainers, JaCoCo.

Liczba testów:
Unit tests: 58.
Integration tests 32. 
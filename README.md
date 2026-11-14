# Carburants web services (JEE workshop 4)

SOAP and REST web services that manage fuel stations and fuel prices, with four clients. Built with Jakarta EE 10 on WildFly 41 (EE10 distribution).

| Component | Technology | URL (HTTP port 8081) |
|---|---|---|
| `soap` | JAX-WS (SOAP), Basic authentication | `/carburants-soap/CarburantsService?wsdl` |
| `rest` | JAX-RS on RESTEasy (WildFly's own), JWT | `/carburants-rest/api` |
| `jersey` | JAX-RS on Jersey 3.1 embedded in the WAR, JWT | `/carburants-jersey/api` |
| `client-web` | Servlets + JSP calling the SOAP service, plus a jQuery page calling Jersey | `/client-web/` |
| `client-java` | `main` class calling the REST service | `mvn exec:java` |

The shared method of the subject, available on the three services: *prices of the fuels of a station (`nom_station`) at a date*.

## Modules

```
api          contract (DTOs, SOAP interface), JWT, password hashing, rules tested without a server
core         JPA entities and CDI services (shared by the three services)
rest-common  JAX-RS resources, filters and mappers (same code on RESTEasy and Jersey)
soap | rest | jersey     the three service WARs
client-web | client-java the clients
db           MySQL schema and sample data
config       WildFly datasource script and PowerShell helpers (no secret inside)
postman      Postman collection
docs         scoping document, step-by-step guide
```

## Requirements

JDK 17, Maven 3.9, MySQL 8, WildFly 41.0.1.Final, **EE10 Distribution** (the standard distribution is Jakarta EE 11 and is not supported by this project). Full installation and test walkthrough (in French): [`docs/GUIDE-ATELIER-4.md`](docs/GUIDE-ATELIER-4.md). Design decisions: [`docs/CADRAGE.md`](docs/CADRAGE.md).

## Quick start

```powershell
mysql -u root -p -e "source db/create-database.sql"          # schema + sample data
mvn -q clean install                                # builds the four WARs
.\config\start-wildfly.ps1                          # terminal 1: prompts for the secrets
.\config\setup-datasource.ps1                       # terminal 2: driver + datasource (once)
.\config\deploy.ps1                                 # copies the WARs to WildFly
```

Secrets are never stored in the repository: the database password, the API password and the JWT key are typed at the prompt of `start-wildfly.ps1` and live only in the environment of the server process.

## Tests

```powershell
mvn -q -pl api test-compile exec:java               # pure-logic checks, no server needed
```

The Postman collection (`postman/`) exercises the three services end to end.

## Limits

- Authentication uses a single API user; there is no user management.
- OAuth 2.0 is described in the report but not implemented (it needs an authorization server).
- The login throttle is in memory, per web application and per JVM.
- The price in force is resolved in Java from the rows up to the requested date; a very long history would call for a dedicated query.

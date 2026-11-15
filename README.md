# Web services carburants (JEE, atelier 4)

![Java 17](https://img.shields.io/badge/Java-17-0F6E66)
![Jakarta EE 10](https://img.shields.io/badge/Jakarta%20EE-10-0F6E66)
![WildFly 41](https://img.shields.io/badge/WildFly-41%20(EE10)-E9A820)
![MySQL 8](https://img.shields.io/badge/MySQL-8-0F6E66)

Services web **SOAP** (JAX-WS) et **REST** (JAX-RS sur RESTEasy, puis Jersey) pour gérer des stations de carburant et l'historique de leurs prix, avec **quatre clients** et une sécurité par **JWT**. Projet réalisé pour l'atelier 4 du module JEE (FST Tanger, LSI, Pr. Lotfi Elaachak), dans la continuité des ateliers [2](https://github.com/ouhaddousara/jee-ecommerce-jpa-mvc2) et [3](https://github.com/ouhaddousara/jee-ecommerce-jsf-jpa).

La méthode commune aux trois services est celle du sujet : **les prix des carburants d'une station (`nom_station`) à une date**.

## Sommaire

1. [Ce que fait le projet](#ce-que-fait-le-projet)
2. [Architecture](#architecture)
3. [Décisions de conception](#décisions-de-conception)
4. [Prérequis](#prérequis)
5. [Installation et démarrage](#installation-et-démarrage)
6. [Utiliser les services](#utiliser-les-services)
7. [Sécurité](#sécurité)
8. [Tests](#tests)
9. [Structure du dépôt](#structure-du-dépôt)
10. [Limites connues](#limites-connues)
11. [Rapport](#rapport)

## Ce que fait le projet

| Composant | Technologie | Adresse (HTTP, port 8081) |
|---|---|---|
| `soap` | JAX-WS (SOAP), authentification Basic | `/carburants-soap/CarburantsService?wsdl` |
| `rest` | JAX-RS sur **RESTEasy** (celui de WildFly), JWT | `/carburants-rest/api` |
| `jersey` | JAX-RS sur **Jersey 3.1** embarqué dans le WAR, JWT | `/carburants-jersey/api` |
| `client-web` | Servlets + JSP appelant le service SOAP, et une page jQuery appelant Jersey | `/client-web/` et `/client-web/jersey.html` |
| `client-java` | Classe avec `main` appelant le service REST | `mvn exec:java` |

Le service SOAP offre 15 opérations : le CRUD des stations et des carburants, l'enregistrement, la modification, la suppression et l'historique des prix, et la méthode partagée `prixCarburants`.

## Architecture

Un seul dépôt Maven multi-modules : les trois services partagent le même code métier, sans copie.

```
api          contrat (DTO, interface SOAP), JWT, hachage, règles testées sans serveur
core         entités JPA et services CDI (partagés par les trois services)
rest-common  ressources JAX-RS, filtres et gestionnaires d'erreurs (écrits une seule fois)
soap | rest | jersey       les trois WAR de services
client-web | client-java   les clients
db           schéma MySQL et données de départ
config       scripts PowerShell et script CLI de WildFly (aucun secret dedans)
postman      collection de tests
docs         rapport LaTeX et captures
```

```
client-web (JSP) ──SOAP──▶ soap ─┐
client-java ─────REST───▶ rest ──┼─▶ core (CDI + JPA) ─▶ MySQL
page jQuery ─────REST───▶ jersey ┘
```

## Décisions de conception

| Sujet | Décision | Pourquoi |
|---|---|---|
| Serveur | WildFly 41.0.1.Final, **distribution « EE10 »**, JDK 17 | WildFly fournit JAX-WS, JAX-RS, CDI et JPA. Depuis WildFly 40, la distribution standard est en Jakarta EE 11 ; la variante EE10 évite de migrer le code (elle disparaît à partir de WildFly 42) |
| « JAX-RS » et « Jersey » | Deux WAR sur deux **moteurs différents** | Jersey *est* une implémentation de JAX-RS. Le WAR Jersey exclut le sous-système JAX-RS de WildFly (`jboss-deployment-structure.xml`) pour que les deux moteurs ne se concurrencent pas |
| Classe d'association `HistoCarb` | Entité à part entière (`histo_carb`) | Elle a deux clés étrangères et deux attributs, une simple table de jointure ne suffit pas. `UNIQUE (station, carburant, date)` traduit la règle « un prix par jour » |
| Suppressions | `ON DELETE RESTRICT` | Une station ou un carburant qui a un historique ne se supprime pas en silence : l'opération est refusée avec un message |
| Prix « à une date » | Prix **en vigueur** : la ligne la plus récente dont la date est ≤ à la date demandée | Un prix reste valable jusqu'au suivant. Sans prix avant la date : liste vide et message explicite (pas une erreur). Station inconnue : 404 |
| Prix | `BigDecimal`, `DECIMAL(8,2)` | Pas d'erreur d'arrondi binaire sur de l'argent |
| Injection | CDI via un `ServiceLocator` | Se comporte de la même façon sous JBossWS, RESTEasy et Jersey (qui a sa propre injection) |
| Secrets | Variables d'environnement, saisies à l'invite | Jamais dans le code, dans Git ni dans `standalone.xml` versionné |

## Prérequis

- JDK 17
- Maven 3.9
- MySQL 8
- WildFly **41.0.1.Final, distribution « EE10 »** (la distribution standard, en Jakarta EE 11, n'est pas supportée par ce projet)
- PowerShell (les scripts de `config/` sont écrits pour Windows)
- Postman, pour la collection de tests
- Tomcat arrêté s'il tourne (il écoute aussi sur le port 8081)

## Installation et démarrage

Toutes les commandes se lancent depuis la racine du projet, dans PowerShell.

**1. Créer la base** (schéma et données de départ) :

```powershell
mysql -u root -p -e "source db/create-database.sql"
```

> PowerShell n'accepte pas la redirection `<` : on fait lire le fichier par MySQL avec `source`. Le script **supprime et recrée** les trois tables : à réserver au développement.

**2. Construire les modules :**

```powershell
mvn -q clean install
```

**3. Démarrer WildFly** (terminal 1, à laisser ouvert). Le script demande le mot de passe MySQL et le mot de passe de l'utilisateur d'API, et génère une clé JWT aléatoire à chaque démarrage :

```powershell
.\config\start-wildfly.ps1
```

**4. Enregistrer le pilote et la source de données** (terminal 2, une seule fois) :

```powershell
mvn dependency:get "-Dartifact=com.mysql:mysql-connector-j:8.0.33"
.\config\setup-datasource.ps1
```

**5. Déployer les quatre WAR :**

```powershell
.\config\deploy.ps1
```

WildFly déploie chaque fichier de lui-même ; le résultat est visible dans sa console (`Deployed "carburants-soap.war"`, etc.).

> Fermer le terminal 1 arrête WildFly : gardez-le ouvert pendant les tests.

## Utiliser les services

**REST (RESTEasy ou Jersey) :** on obtient un jeton, puis on l'envoie dans `Authorization: Bearer`.

```powershell
$apiPwd = Read-Host "Mot de passe API" -AsSecureString   # puis conversion en clair uniquement pour l'appel
$body = @{ username = "admin"; password = [Net.NetworkCredential]::new("", $apiPwd).Password } | ConvertTo-Json
$t = Invoke-RestMethod -Method Post -Uri http://localhost:8081/carburants-rest/api/auth/login -ContentType application/json -Body $body
Invoke-RestMethod -Uri "http://localhost:8081/carburants-rest/api/prix?station=Station%20Malabata&date=2026-11-08" -Headers @{ Authorization = "Bearer $($t.token)" }
```

Remplacer `carburants-rest` par `carburants-jersey` pour interroger le service Jersey.

**Client Java :**

```powershell
$env:CARBURANTS_API_PASSWORD = "<votre mot de passe>"
mvn -q -pl client-java exec:java "-Dexec.args=Station Malabata 2026-11-08"
```

Le dernier argument est la date, tout ce qui précède forme le nom de la station (pas de guillemets à gérer). Le mot de passe est lu dans l'environnement, jamais sur la ligne de commande.

**Clients web :** `http://localhost:8081/client-web/` (Servlets + JSP, connexion puis gestion des stations, carburants, prix et consultation) et `http://localhost:8081/client-web/jersey.html` (page jQuery).

**SOAP :** le WSDL est public ; les appels exigent une authentification Basic. Les requêtes S1 à S8 de la collection Postman montrent les enveloppes à envoyer.

### Résultats attendus avec les données de départ

| Station | Date | Réponse |
|---|---|---|
| Station Malabata | 2026-11-08 | Gasoil 10,62 ; Sans plomb 95 11,75 |
| Station Malabata | 2026-11-05 | Gasoil 10,45 ; Sans plomb 95 11,60 (le prix du 1er novembre reste en vigueur) |
| Station Malabata | 2026-10-01 | liste vide et message « aucun prix » |

### Codes de réponse REST

| Code | Cas |
|---|---|
| 200 | prix renvoyés (liste vide et message si aucun prix en vigueur) |
| 400 | date invalide, station absente de la requête |
| 401 | jeton manquant, invalide ou expiré ; identifiants incorrects |
| 404 | station inconnue |
| 409 | doublon, suppression refusée |
| 429 | trop de tentatives de connexion |
| 500 | erreur interne : message générique, la cause reste dans le journal |

## Sécurité

| Service | Technique |
|---|---|
| SOAP | HTTP Basic, vérifié par un filtre servlet devant le point d'accès ; le WSDL reste public |
| REST et Jersey | JWT HS256 : `POST /auth/login` donne un jeton, un filtre le vérifie sur `/prix` |
| OAuth 2.0 | expliqué dans le rapport, **non implémenté** (il demande un serveur d'autorisation séparé, par exemple Keycloak) |

Mesures en place :

- JWT écrit avec le seul JDK : algorithme **imposé** (pas de `alg=none`), signature comparée en temps constant, expiration vérifiée (900 s par défaut), clé d'au moins 32 octets ;
- mot de passe de l'API haché avec **PBKDF2** (120 000 itérations, sel aléatoire), jamais conservé en clair ;
- blocage après **5 échecs** de connexion en 300 secondes (429) ;
- erreurs sans requête SQL ni trace de pile ;
- en-têtes de sécurité côté REST (`nosniff`, `no-store`) ;
- client web : protection **CSRF**, politique de sécurité de contenu (CSP), changement d'identifiant de session à la connexion ;
- page jQuery : jeton gardé en mémoire (pas de `localStorage`), valeurs du serveur insérées avec `.text()`.

> **Attention aux secrets.** Les mots de passe ne figurent dans aucun fichier du dépôt. Le mot de passe MySQL de la source de données peut en revanche être écrit en clair dans le `standalone.xml` **local** de WildFly : ne le partagez jamais, ne le commitez pas, ne le montrez pas sur une capture.

## Tests

**Règles métier, sans serveur** (prix en vigueur, validations, JWT, hachage, en-tête Basic, limitation des tentatives) :

```powershell
mvn -q -pl api test-compile exec:java
```

Attendu : `TOTAL CHECKS: 81   FAILURES: 0`.

**Bout en bout, avec Postman :** importer `postman/carburants.postman_collection.json`, renseigner les variables de la collection (`soapUrl`, `restUrl`, `jerseyUrl`, `apiUser`, `apiPassword`), puis lancer le *Runner* sur chaque dossier. Les dossiers RESTEasy et Jersey ont chacun 12 assertions (connexion, prix un jour de changement, prix entre deux changements, aucun prix, station inconnue, date invalide, appel sans jeton). Les requêtes SOAP n'ont pas d'assertion automatique.

## Structure du dépôt

```
jee-webservices-carburants/
├── pom.xml                 POM parent (versions, modules)
├── api/                    contrat et logique pure
├── core/                   JPA + CDI
├── rest-common/            ressources REST partagées
├── soap/  rest/  jersey/   les trois services (WAR)
├── client-web/             Servlets + JSP, page jQuery
├── client-java/            client en ligne de commande
├── db/create-database.sql  schéma et données de départ
├── config/                 start-wildfly, setup-datasource, deploy (PowerShell, CLI)
├── postman/                collection de tests
└── docs/rapport/           rapport LaTeX, captures et PDF
```

## Limites connues

- Un seul utilisateur d'API : pas de gestion d'utilisateurs ni de rôles.
- OAuth 2.0 est décrit dans le rapport mais pas implémenté.
- Tout tourne en HTTP local ; en production, TLS est indispensable (surtout pour Basic).
- Le compteur d'échecs de connexion est en mémoire, par application et par JVM : un déploiement en grappe demanderait un stockage partagé.
- Le prix en vigueur est résolu en Java à partir des lignes jusqu'à la date demandée ; pour un historique très long, une requête SQL dédiée serait préférable.
- La variante EE10 de WildFly est temporaire : un projet à long terme devrait migrer vers Jakarta EE 11 (Jersey 4).

## Rapport

Le rapport (conception, services, clients, sécurité, tests, incidents rencontrés) est dans [`docs/rapport/`](docs/rapport/) : sources LaTeX dans `main.tex`, PDF dans `rapport-atelier-4.pdf`. Pour le recompiler (sous WSL) :

```bash
cd docs/rapport && latexmk -pdf -interaction=nonstopmode main.tex
```

## Auteur

**Sara Ouhaddou**, élève-ingénieure LSI (FST Tanger) : [github.com/ouhaddousara](https://github.com/ouhaddousara)

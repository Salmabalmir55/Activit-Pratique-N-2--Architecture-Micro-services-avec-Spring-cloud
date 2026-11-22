# Activité Pratique N°2 – Architecture Micro-services avec Spring Cloud

Application e-commerce découpée en micro-services : gestion des clients, gestion des produits et facturation, reliés par un annuaire Eureka, une passerelle Spring Cloud Gateway et une configuration centralisée avec Spring Cloud Config.

## Environnement et versions

| Outil | Version |
|---|---|
| JDK | 17 |
| Maven | 3.9 (Maven Wrapper inclus : `mvnw` / `mvnw.cmd`) |
| Spring Boot | 3.4.4 |
| Spring Cloud | 2024.0.3 |
| Base de données | H2 (en mémoire) |
| IDE | IntelliJ IDEA |

Toutes les versions sont centralisées dans le `pom.xml` parent `net.salma.ecom:ecom-app:1.0.0` : il hérite de `spring-boot-starter-parent` 3.4.4 et importe le BOM `spring-cloud-dependencies` 2024.0.3. Chaque micro-service déclare ce pom comme parent et liste ses dépendances sans aucune version.

## Dépendances par service

| Service | Dépendances |
|---|---|
| `customer-service` | Spring Web, Data JPA, Data REST, H2, Lombok, Eureka Client, Config Client, Actuator |
| `inventory-service` | Spring Web, Data JPA, Data REST, H2, Lombok, Eureka Client, Config Client, Actuator |
| `billing-service` | Spring Web, Data JPA, Data REST, HATEOAS, OpenFeign, H2, Lombok, Eureka Client, Config Client |
| `discovery-service` | Eureka Server |
| `gateway-service` | Spring Cloud Gateway, Eureka Client, Config Client, Actuator |
| `config-service` | Config Server, Eureka Client, Actuator |

## Architecture

| Service | Port | Package | Rôle |
|---|---|---|---|
| `discovery-service` | 8761 | `net.salma.ecom.discoveryservice` | Annuaire Eureka |
| `config-service` | 9999 | `net.salma.ecom.configservice` | Configuration centralisée (dossier `config-repo`) |
| `gateway-service` | 8888 | `net.salma.ecom.gatewayservice` | Passerelle Spring Cloud Gateway |
| `customer-service` | 8081 | `net.salma.ecom.customerservice` | Gestion des clients |
| `inventory-service` | 8082 | `net.salma.ecom.inventoryservice` | Gestion des produits |
| `billing-service` | 8083 | `net.salma.ecom.billingservice` | Facturation (OpenFeign vers clients et produits) |

## Construire

```bash
./mvnw clean package -DskipTests
```

Sous Windows : `mvnw.cmd clean package -DskipTests`. Ouvrir le dossier racine dans IntelliJ IDEA : le `pom.xml` parent importe tous les modules.

## 1. customer-service

Micro-service de gestion des clients : l'entité JPA `Customer` est stockée dans une base H2 et exposée automatiquement en REST par Spring Data REST sous `/api`, avec deux projections (`all` et `email`). Trois clients de test sont créés au démarrage.

**Liste des clients** : `GET http://localhost:8081/api/customers`

![Liste des clients](captures/01-customer-service/clients.png)

**Projection `email`** : `GET http://localhost:8081/api/customers?projection=email`

![Projection email](captures/01-customer-service/projection-email.png)

**Un client avec la projection `all`** : `GET http://localhost:8081/api/customers/1?projection=all`

![Projection all](captures/01-customer-service/projection-all.png)

## 2. inventory-service

Micro-service de gestion des produits : l'entité `Product`, identifiée par un UUID, est exposée par Spring Data REST. Trois produits de test sont créés au démarrage.

**Liste des produits** : `GET http://localhost:8082/api/products`

![Liste des produits](captures/02-inventory-service/produits.png)

## 3. discovery-service (Eureka)

Annuaire des services sur le port 8761. `customer-service` et `inventory-service` s'y enregistrent au démarrage.

![Eureka](captures/03-discovery-service/eureka.png)

## 4. gateway-service

### Routes statiques

Première version : deux routes déclarées dans `application.yml`, vers `lb://CUSTOMER-SERVICE` et `lb://INVENTORY-SERVICE`. Le préfixe `lb://` fait passer chaque requête par Eureka et l'équilibrage de charge.

**Clients via la passerelle** : `GET http://localhost:8888/api/customers`

![Route statique clients](captures/04-gateway-service/route-statique-clients.png)

**Produits via la passerelle** : `GET http://localhost:8888/api/products`

![Route statique produits](captures/04-gateway-service/route-statique-produits.png)

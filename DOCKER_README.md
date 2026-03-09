# Docker Setup pour Noblesse

## Architecture

Cette configuration utilise Docker Compose pour lancer le backend et le frontend en conteneurs séparés :

- **Backend** : Spring Boot 3.5.3 (Java 21) sur le port 8080
- **Frontend** : Angular 20.2.0 sur le port 4200
- **Base de données** : SQLite (volume partagé)

## Prérequis

- Docker et Docker Compose installés
- Aucun service ne doit être lancé localement sur les ports 4200 et 8080

## Démarrage

### Lancer l'application complète

```bash
docker-compose --profile full up -d
```

### Lancer frontend uniquement

```bash
docker-compose --profile frontend up -d
```

### Lancer backend uniquement

```bash
docker-compose --profile backend up -d
```

### Arrêter l'application

```bash
docker-compose down # arrête tous les profils actifs
```

### Voir les logs

```bash
# Tous les services
docker-compose logs -f

# Backend seulement
docker-compose logs -f backend

# Frontend seulement
docker-compose logs -f frontend
```

## Accès

- **Frontend** : http://localhost:4200
- **Backend API** : http://localhost:8080/noblesseApi
- **Healthcheck Backend** : http://localhost:8080/noblesseApi/actuator/health

## Profiles

Chaque service utilise des profiles Docker Compose pour flexibiliser le déploiement :

| Profile    | Services lancés     | Commande                                  |
| ---------- | ------------------- | ----------------------------------------- |
| `full`     | Backend + Frontend  | `docker-compose --profile full up -d`     |
| `backend`  | Backend uniquement  | `docker-compose --profile backend up -d`  |
| `frontend` | Frontend uniquement | `docker-compose --profile frontend up -d` |

Le backend utilise le profile `production` par défaut. Pour utiliser un autre profile, modifiez la variable `SPRING_PROFILES_ACTIVE` dans le `docker-compose.yml` :

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=development
```

## Variables d'environnement

### Backend

- `SPRING_PROFILES_ACTIVE` : Profile Spring (production, development, etc.)
- `SPRING_DATASOURCE_URL` : URL de la base de données SQLite
- `SPRING_JPA_HIBERNATE_DDL_AUTO` : Config Hibernate (update, create-drop, etc.)
- `SERVER_PORT` : Port du serveur (8080)

### Frontend

- `NODE_ENV` : Mode Node (production, development)

## Volumes

- `./data/` : Dossier contenant la base de données SQLite `inventaire.db`

## Rebuild

Pour reconstruire les images après des changements de code :

```bash
docker-compose up --build
```

## Développement

Pour développer en local (sans Docker) :

### Backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=development"
```

### Frontend

```bash
cd frontend
npm install
npm start
```

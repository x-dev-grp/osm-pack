# Inventory Service (osm-pack)

Microservice responsible for packaging, container management, and inventory tracking.

## 📖 Functional Overview
Manages the physical storage and containerization of oil products. It bridges the gap between bulk production and retail-ready packaging.

### Key Features
- **Storage Unit Management**: Real-time tracking of tank and silo levels, including capacity and current volume.
- **Packaging Workflow**: Manages the conversion of bulk oil into various retail sizes (bottles, containers).
- **Inventory Transfers**: Handles the internal movement of oil between different storage units while maintaining volume integrity.
- **Container History**: Tracks the history and maintenance status of storage vessels.


## 🛠 Tech Stack
- **Language:** Java 21
- **Framework:** Spring Boot 3.4.4
- **Database:** PostgreSQL (`abiooc_inventory`)
- **Discovery:** Netflix Eureka

## 🚀 Getting Started
### Local Development
```bash
./mvnw spring-boot:run
```

### Docker Build
```bash
docker build --secret id=maven_settings,src=$HOME/.m2/settings.xml -t inventory-service .
```

## ⚙️ Configuration
| Variable | Default | Description |
| :--- | :--- | :--- |
| `SERVER_PORT` | `1234` | Service port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/abiooc_inventory` | Database URL |
| `LOG_LEVEL_EUREKA` | `WARN` | Eureka discovery logs |
| `LOG_LEVEL_REST` | `WARN` | RestTemplate debug logs |

## 🔗 CI/CD
Fully automated pipeline integrated with GHCR.
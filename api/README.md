# api

Capa HTTP delgada (Javalin) entre el frontend React y `SistemaTransporte`. No tiene logica de negocio.

```bash
# desde la raiz del repositorio
mvn -B package -DskipTests
RUTASV_ADMIN_PASSWORD=una-clave-larga java -jar api/target/rutasv-api.jar
# API en http://localhost:7070/api
```

Variables: `RUTASV_PORT` (7070), `RUTASV_DATA_DIR` (`data`), `RUTASV_ADMIN_USER` (`admin`), `RUTASV_ADMIN_PASSWORD`.

Endpoints, errores y formas JSON: [docs/api.md](../docs/api.md).

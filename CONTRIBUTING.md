# Como contribuir

## Flujo de ramas

- `main` esta protegida: nadie hace push directo.
- Cada tarea va en una rama `feat/<rol>-<tarea>`, por ejemplo `feat/ruta-grafo` o `feat/paradas-tabla-hash`.
- Abre un Pull Request hacia `main`. Necesita 1 revision aprobada y el CI en verde (`mvn -B verify`).

## Commits

Mensajes en ingles, con prefijo convencional: `feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `chore:`.
Ejemplo: `feat: add linked stack implementation`.

## Contratos

Las interfaces de `structures`, `persistence` y `service` son contratos del equipo.
Solo cambian por PR, y se avisa antes al equipo. Mira `docs/contratos.md`.

## Seguridad y datos

- Nunca subas contrasenas, claves, correos reales ni archivos de entorno (`.env*`).
- Nunca subas `data/usuarios*` ni `data/historial*` (estan en `.gitignore`).
- Los datos de prueba y los CSV deben ser sinteticos.

## Pruebas

Todo cambio lleva pruebas. Corre `mvn -B verify` antes de abrir el PR.

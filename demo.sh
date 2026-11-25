#!/usr/bin/env bash
# Levanta RutaSV con los datos de demostracion (todo sintetico) para la defensa.
#
#   ./demo.sh            API + frontend en http://localhost:5173
#   ./demo.sh --limpio   igual, pero borra usuarios e historial de la demo anterior
#
# Variables opcionales:
#   RUTASV_PORT            puerto de la API (7070)
#   RUTASV_ADMIN_PASSWORD  contrasena del admin
#   RUTASV_DEMO_PASSWORD   contrasena de ana_01, luis.m y sofia-r
# Si no se dan, se generan al azar y se muestran en pantalla (el repo no guarda contrasenas).
# Ojo: usuarios.txt guarda la contrasena con que se creo cada usuario; con --limpio se empieza de cero.
#
# Los datos de la demo viven en .demo-data/ (ignorada por git): la red se copia de data/ y ahi se
# guardan usuarios.txt e historial.txt, asi data/ nunca se ensucia.
set -euo pipefail
cd "$(dirname "$0")"

PUERTO="${RUTASV_PORT:-7070}"
DIR=".demo-data"
if [[ "${1:-}" == "--limpio" ]]; then
  rm -rf "$DIR"
fi
azar() { head -c 9 /dev/urandom | base64 | tr -d '/+='; }
CLAVES="$DIR/.claves"
mkdir -p "$DIR"
# Se recuerdan las claves generadas para que coincidan con usuarios.txt en el siguiente arranque.
[[ -f "$CLAVES" ]] && source "$CLAVES"
export RUTASV_ADMIN_PASSWORD="${RUTASV_ADMIN_PASSWORD:-$(azar)}"
export RUTASV_DEMO_PASSWORD="${RUTASV_DEMO_PASSWORD:-$(azar)}"
export RUTASV_DEMO=1

printf 'RUTASV_ADMIN_PASSWORD=%q\nRUTASV_DEMO_PASSWORD=%q\n' "$RUTASV_ADMIN_PASSWORD" "$RUTASV_DEMO_PASSWORD" > "$CLAVES"
cp data/estaciones.csv data/tramos.csv "$DIR/"

echo "Compilando la API..."
mvn -B -q package -DskipTests

echo "API en http://localhost:$PUERTO/api (datos: $DIR)"
# Se corre una copia del jar: si alguien recompila durante la demo, el servidor no se cae.
cp api/target/rutasv-api.jar "$DIR/rutasv-api.jar"
RUTASV_PORT="$PUERTO" RUTASV_DATA_DIR="$DIR" java -jar "$DIR/rutasv-api.jar" &
API=$!
trap 'kill $API 2>/dev/null || true' EXIT

cd frontend
[[ -d node_modules ]] || npm ci
echo
echo "Usuarios de la demo:"
echo "  admin    / $RUTASV_ADMIN_PASSWORD"
echo "  ana_01, luis.m, sofia-r / $RUTASV_DEMO_PASSWORD"
echo
RUTASV_API="http://localhost:$PUERTO" npm run dev

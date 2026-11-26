# Ejecutar RutaSV en Windows (PowerShell)

Esta guia usa PowerShell y dos terminales: una para la API y otra para React.
Ejecuta los comandos desde la raiz del repositorio, salvo donde se indique.

## 1. Requisitos

Instala JDK 21, Maven 3.9 o superior y Node.js 20 o superior. Agrega sus
carpetas `bin` al PATH y abre una terminal nueva. Comprueba:

```powershell
java -version
javac -version
mvn.cmd -version
node --version
npm.cmd --version
```

`mvn.cmd -version` debe mostrar que utiliza Java 21. Si no lo hace, revisa
`JAVA_HOME`. Usar `npm.cmd` evita depender de la politica de ejecucion de
scripts `.ps1` de PowerShell.

## 2. Compilar y probar

```powershell
mvn.cmd -B verify
if ($LASTEXITCODE -ne 0) { throw 'Revisar los errores de Maven antes de continuar.' }
Push-Location frontend
try {
    npm.cmd ci
    if ($LASTEXITCODE -ne 0) { throw 'Fallo la instalacion del frontend.' }
    npm.cmd run build
    if ($LASTEXITCODE -ne 0) { throw 'Fallo la compilacion del frontend.' }
} finally {
    Pop-Location
}
```

`npm ci` utiliza las versiones de `package-lock.json`. Los informes Java quedan
en `core/target/surefire-reports` y `api/target/surefire-reports`.

## 3. API: primera terminal

Usa una carpeta de datos fuera del repositorio para conservar los CSV originales.
Este bloque copia los ejemplos solo si no existen; no reinicia tus datos al
volver a ejecutarlo.

```powershell
$datosRutaSv = Join-Path $env:LOCALAPPDATA 'RutaSV/datos-demo'
New-Item -ItemType Directory -Force $datosRutaSv | Out-Null
foreach ($csv in Get-ChildItem ./data -Filter '*.csv') {
    $destino = Join-Path $datosRutaSv $csv.Name
    if (!(Test-Path $destino)) { Copy-Item $csv.FullName $destino }
}
$env:RUTASV_DATA_DIR = $datosRutaSv
$env:RUTASV_ADMIN_USER = 'admin'
$claveRutaSv = Read-Host 'Clave del admin local (minimo 8 caracteres)' -AsSecureString
$env:RUTASV_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new('', $claveRutaSv).Password
try {
    java -jar api/target/rutasv-api.jar
} finally {
    Remove-Item Env:RUTASV_ADMIN_PASSWORD -ErrorAction SilentlyContinue
    Remove-Variable claveRutaSv -ErrorAction SilentlyContinue
}
```

La API escucha en `http://localhost:7070/api`. El admin se crea al iniciar
cuando no existe; cambiar la variable no cambia la clave de un usuario guardado.
Usa datos y claves de demostracion, y no los incluyas en commits.

## 4. React: segunda terminal

Desde la raiz del repositorio:

```powershell
cd frontend
npm.cmd run dev -- --host 127.0.0.1 --strictPort
```

Abre `http://127.0.0.1:5173` e inicia sesion con `admin` y la clave elegida.
Vite reenvia las peticiones `/api` a la API Java en el puerto 7070.

## 5. Comprobacion manual

1. Inicia sesion: el admin debe llegar a Red vial.
2. Con los CSV originales, comprueba que se muestran 8 estaciones.
3. Busca Terminal Occidente (E01) y Metrocentro (E04).
4. Calcula la ruta: debe dar 18 minutos y 9.5 km.
5. Revisa Mi historial: la consulta debe aparecer.

Si una pantalla responde que esta pendiente, revisa el estado del modulo en
[entrega-fase2.md](entrega-fase2.md). Arrancar la aplicacion no implica que
las funciones pendientes ya esten implementadas.

## 6. Cambios y apagado

- React recarga automaticamente los cambios mientras Vite esta ejecutandose.
- Para Java, detiene la API con `Ctrl+C`, ejecuta `mvn.cmd -B verify` y vuelve
  a ejecutar el bloque de inicio de la API.
- Detiene cada terminal con `Ctrl+C` cuando termines.

## Problemas comunes

- **Comando no reconocido:** revisa la instalacion y el PATH; abre otra terminal.
- **Puerto ocupado:** detiene la instancia anterior. `--strictPort` evita que
  Vite cambie silenciosamente a otro puerto.
- **Error de conexion a `/api`:** comprueba que Java sigue ejecutandose en 7070.
- **AccessDeniedException en temporales de pruebas:** en entornos restringidos,
  usa una carpeta temporal donde tengas permiso. Por ejemplo:

  ```powershell
  $tmpRutaSv = Join-Path (Get-Location) 'core/target/tmp-pruebas'
  New-Item -ItemType Directory -Force $tmpRutaSv | Out-Null
  mvn.cmd -B "-DargLine=-Djava.io.tmpdir=`"$tmpRutaSv`"" verify
  ```

  Este ajuste es para las pruebas; no cambia la persistencia de la aplicacion.

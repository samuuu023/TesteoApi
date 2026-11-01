# Cool Barber Shop API

API REST de administración de una barbería. Java 21, Spring Boot 3.5.7, Spring Data JPA, MySQL 8, Spring Security, JWT, ModelMapper, JUnit 5, Mockito y Swagger.

## Abrir en IntelliJ

1. Extraer el ZIP y abrir la carpeta `BarberiaAPI`, donde está `pom.xml`.
2. Seleccionar JDK 21 en Project SDK y en Maven > Runner > JRE.
3. Cargar el proyecto Maven y esperar la descarga de dependencias. El JDK 21 que ya tienes instalado sirve.

## Base de datos y ejecución

Workbench es el cliente; el servicio MySQL Server debe estar iniciado. Ejecutar el script `Barberia_Base_Datos.sql`, entregado por separado, en Workbench. Crea la base `barberia_api` y sus seis tablas. El diagrama también se entrega fuera del proyecto.

En la terminal PowerShell de IntelliJ, ubicada en esta carpeta:

```powershell
$env:JAVA_HOME="C:\Users\Samuu\.jdks\ms-21.0.12.1"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:DB_USER="root"
$env:DB_PASSWORD="TU_CONTRASENA_DE_MYSQL"
.\mvnw.cmd spring-boot:run
```

Sustituir `TU_CONTRASENA_DE_MYSQL` por la contraseña de tu conexión local de MySQL. Si no tienes contraseña, usar `$env:DB_PASSWORD=""`. Estas variables solo afectan esta terminal. No escribir la contraseña en el repositorio. Si tu MySQL usa otro puerto, definir `DB_URL` con la URL completa antes de ejecutar.

Abrir `http://localhost:8080/swagger-ui/index.html`. La raíz `/` no tiene una página web: este proyecto es el backend API REST.

Administrador inicial: `admin@barberia.com` / `Admin123!`. Se crea automáticamente cuando la tabla usuarios está vacía. Las claves se almacenan con BCrypt. Para cambiar los valores antes del primer inicio, definir `ADMIN_LOGIN` y `ADMIN_PASSWORD`. Antes de publicar, definir una clave propia `JWT_SECRET` en Base64 (mínimo 32 bytes decodificados); la clave predeterminada es únicamente para desarrollo local.

## Probar en Swagger

1. Ejecutar `POST /api/auth/login` con `{"login":"admin@barberia.com","clave":"Admin123!"}`.
2. Copiar el valor `token`, pulsar **Authorize** y pegar solo el token.
3. Crear un servicio, un cliente y un barbero mediante sus endpoints POST. Consultar sus IDs.
4. Crear una cita con esos IDs y una fecha futura. Usar lunes a sábado entre 08:00 y 18:00 (hora de El Salvador), en intervalos de 15 minutos.
5. Confirmarla con `PATCH /api/citas/{id}/estado` y `{"estado":"CONFIRMADA"}`. También puede cancelarse. Solo puede completarse una cita confirmada cuyo horario haya terminado.

Hay peticiones listas para el cliente HTTP de IntelliJ en `src/test/resources/barberia-api.http`. Swagger está disponible también en IntelliJ Community desde el navegador; no requiere un plugin HTTP de pago.

## Módulos y permisos

- Autenticación: login, consulta de sesión y creación de usuarios por un administrador.
- Clientes: CRUD, búsqueda por nombre y paginación; correo único.
- Barberos: CRUD, especialidad, estado activo y teléfono único.
- Servicios: CRUD, precio, duración y estado activo; nombre único.
- Citas: CRUD, reprogramación, estados, agenda diaria, filtros y resumen de ingresos por citas completadas.

`ROLE_ADMIN` puede consultar y modificar. `ROLE_USER` puede consultar. Solo ADMIN crea usuarios y selecciona su rol; no hay registro público que permita asignarse permisos. Los tokens duran una hora y verifican firma, expiración y emisor.

Listados: `GET /api/clientes`, `/api/barberos`, `/api/servicios` con `buscar`, `page` y `size`; también `/lista`. Citas acepta `fecha`, `estado`, `barberoId`, `page` y `size`. Las páginas incluyen `content`, `number`, `size`, `totalElements`, `totalPages`.

La cita conserva el precio y horario contratados. Se impide reservar un barbero o cliente ocupado, reservar recursos inactivos y borrar clientes/barberos/servicios con citas. Se bloquean las filas del cliente y del barbero durante la reserva para serializar solicitudes concurrentes sobre esos recursos. Las entidades usan versión para detectar actualizaciones simultáneas. Solo se eliminan citas canceladas; las completadas conservan el historial.

## Pruebas

En otra terminal de IntelliJ, con JDK 21:

```powershell
.\mvnw.cmd clean verify
Start-Process ".\target\site\jacoco\index.html"
```

Todas las pruebas se ejecutan con este comando. También se puede hacer clic derecho en `src/test/java` > Run All Tests. Las pruebas de integración activan el perfil `test` con H2 en memoria; no necesitan MySQL ni modifican tus datos locales. La API en ejecución normal utiliza MySQL y valida el esquema existente.

- `servicios/implementaciones/*ServiceTest`: pruebas unitarias con Mockito, sin base de datos.
- `servicios/implementaciones/*ServiceIntegracionTest`: servicios, DTO, repositorios y relaciones reales con Spring Boot y rollback por prueba.
- `controladores/*Test`: peticiones MockMvc, validaciones, JWT, permisos y flujo de reservas.
- `seguridad/JwtServiceTest`: firma, expiración, emisor, formato y longitud de clave.

Los resultados XML/TXT quedan en `target/surefire-reports`, y la cobertura en `target/site/jacoco`. El ZIP no incluye dependencias, archivos compilados ni bases locales: Maven los genera al compilar. La primera ejecución necesita internet.

## Estructura

```text
src/main/java/org/esfe/
  config/
  controladores/
  dtos/cliente, barbero, servicio, cita/
  excepciones/
  modelos/
  repositorios/
  seguridad/configuracion, dtos, modelos, repositorios, servicios/
  servicios/interfaces, implementaciones/
src/main/resources/application.properties
src/test/java/org/esfe/
  controladores/
  seguridad/
  servicios/implementaciones/
src/test/resources/
```

La organización sigue las capas del ejemplo de clase: controlador -> interfaz de servicio -> implementación -> repositorio -> entidad. Los DTO separan los cuerpos de entrada de las respuestas. Las modificaciones toman el ID de la ruta; se rechazan campos desconocidos para evitar sustituirlo con el JSON.

## Respuestas habituales

- 200: consulta o modificación correcta; 201: registro creado; 204: eliminado.
- 400: campos o formato inválidos, fecha pasada u horario incorrecto.
- 401: credenciales o token incorrectos; 403: rol sin permiso.
- 404: ID inexistente; 409: duplicados, citas cruzadas, referencias o transición inválida.

Si aparece `Connection refused`, revisar MySQL Server, el puerto y las credenciales. Si aparece `Schema-validation`, ejecutar el script de esta entrega en la base `barberia_api`. Un error `JAVA_HOME` se corrige apuntando a la carpeta real del JDK 21, no a `bin`.

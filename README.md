Trabajo Práctico Obligatorio de Programación Orientada a Objetos.
Propuesta elegida: Propuesta B - Agencia de Turismo, adaptada a una agencia de turismo receptivo en Alaska ("Rumbo Norte Alaska").

Descripción
Es un sistema por consola para la agencia. Permite armar el catálogo de tours (itinerario, tarifas por temporada y servicios adicionales), programar salidas, registrar clientes y reservas con sus pasajeros, cobrar la seña y el saldo, cancelar o modificar reservas aplicando la politica de cancelación, asignar guía y vehículo a cada salida, emitir la lista de pasajeros, liquidar los honorarios de los guías y sacar los cuatro reportes que pide el enunciado.
Lo que se carga queda guardado en archivos dentro de la carpeta data, asi que la información se mantiene entre una ejecución y otra.

Integrantes
- Juan Pedro Sama (@juampe-sudo)
- Joaquin Castellano (@joaquingit1)
- Juan Zeljkovich (@zelcito)

Requisitos
- Java 17 o superior. Hace falta el JDK (javac), con el JRE solo no alcanza
- No usamos Maven ni Gradle. La única librería externa es JUnit 5 para las pruebas y ya está en /lib, no hay que descargar nada

Cómo compilar y ejecutar
Los comandos se corren parados en la carpeta raíz del repositorio, porque el programa busca los archivos de data con rutas relativas.

    javac -encoding UTF-8 -d out -sourcepath src src/app/Main.java
    java -cp out app.Main

Funciona igual en Linux, Mac y Windows (cmd o PowerShell). Si en Windows los acentos se ven raros en la consola, correr antes chcp 65001.
Con IntelliJ: marcar src como Sources Root y test como Test Sources Root, agregar el jar de /lib como librería y ejecutar app.Main con la raíz del repo como working directory.

Al iniciar pide elegir el rol (vendedor, coordinador de operaciones o administrador) y muestra el menú de ese rol. El vendedor tiene que ingresar su legajo. Las fechas se ingresan como dd/mm/aaaa y las horas como hh:mm.
La primera vez que se ejecuta carga el personal y los vehículos desde data/datos_iniciales.txt. Para arrancar de cero alcanza con borrar los archivos .dat de la carpeta data.

Pruebas unitarias
Están en /test y usan JUnit 5. Son 72 pruebas sobre el modelo, las estrategias de promoción y cancelación, la persistencia, los controladores y los reportes. Para compilarlas y correrlas (también desde la raíz):

    javac -encoding UTF-8 -d out -cp lib/junit-platform-console-standalone-1.13.4.jar -sourcepath src test/controladores/*.java test/estrategias/*.java test/modelo/*.java test/persistencia/*.java test/reportes/*.java
    java -jar lib/junit-platform-console-standalone-1.13.4.jar execute -cp out --scan-classpath

El plan de pruebas funcionales, con el dato de entrada, el resultado esperado y el obtenido de cada caso, está en docs/plan_de_pruebas.md.

Datos de prueba
En data/datos_iniciales.txt viene cargado el personal y los vehículos:

| Rol | Legajo | Nombre | Datos |
|---|---|---|---|
| Vendedor | 101 | Emily Carter | comisión 5% |
| Vendedor | 102 | Martín Sosa | comisión 4% |
| Guía | 201 | Nathan Kowalski | aventura y naturaleza, USD 280 por día |
| Guía | 202 | Aiyana Nanouk | cultural y naturaleza, USD 250 por día |
| Guía | 203 | Sofía Lindqvist | las tres categorías, USD 320 por día |
| Coordinador | 301 | Daniel Brooks | |
| Administrador | 401 | Rachel Kim | |

Vehículos: KDF482 (camioneta, 8 lugares), JTR915 (combi, 15 lugares) y HMB307 (micro, 40 lugares).

Los tours, salidas, clientes y reservas se cargan desde el menú. Un recorrido corto para probarlo:
1. Administrador > Dar de alta un tour, por ejemplo KEN01 "Kayak en Kenai Fjords", aventura, 2 días, cupo 4 a 12. Después agregarle una tarifa que cubra la fecha de la salida (ej. BAJA del 01/10/2026 al 30/04/2027, USD 1200) y algún servicio adicional
2. Coordinador > Programar salida para ese tour con una fecha futura, y Asignar guía y vehículo (guía 201 y vehículo KDF482)
3. Vendedor (legajo 101) > Registrar cliente, Crear reserva para la salida S001 con uno o mas pasajeros, y Registrar pago con la seña (30% del total)
4. Coordinador > Emitir lista de pasajeros. La lista queda en data/listas
5. Administrador > Reportes. Cada reporte se muestra en pantalla y también se guarda en data/reportes

Estructura del proyecto
- /docs: análisis de requisitos, supuestos y documentación del diseño
- /docs/uml: diagramas UML (imagen .png y fuente .drawio)
- /src: código fuente en Java
- /test: pruebas unitarias con JUnit 5
- /lib: jar de JUnit para compilar y correr las pruebas
- /data: datos iniciales. Ahí mismo el programa guarda los .dat, las listas de pasajeros y los reportes

Entregas
- Etapa 1 (modelado y patrones): docs/Etapa1_Documento_de_Diseno.pdf, tag etapa-1
- Etapa 2 (código y pruebas): código en /src y /test, docs/plan_de_pruebas.md, docs/informe_final.md y el diagrama de clases actualizado, tag etapa-2 (la entrega se pasó al 12/11/26)

Historial del repositorio
- El trabajo empezó en otro repositorio (github.com/jpsam4/TrabajoPOO). Perdimos el acceso a la cuenta administradora de ese repositorio y migramos el trabajo a este, conservando el historial de commits.
- Al migrar, volvimos a abrir en este repositorio como pull requests el trabajo que ya estaba en el anterior (de los branches que ya migramos).

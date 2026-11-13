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

Estructura del proyecto
- /docs: análisis de requisitos, supuestos y documentación del diseño
- /docs/uml: diagramas UML (imagen .png y fuente .drawio)
- /src: código fuente en Java

Entregas
- Etapa 1 (modelado y patrones): docs/Etapa1_Documento_de_Diseno.pdf, tag etapa-1
- Etapa 2 (código y pruebas): pendiente, entrega 04/11/26

Historial del repositorio
- El trabajo empezó en otro repositorio (github.com/jpsam4/TrabajoPOO). Perdimos el acceso a la cuenta administradora de ese repositorio y migramos el trabajo a este, conservando el historial de commits.
- Al migrar, volvimos a abrir en este repositorio como pull requests el trabajo que ya estaba en el anterior (de los branches que ya migramos).

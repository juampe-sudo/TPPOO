# Informe final - Etapa 2

Rumbo Norte Alaska (Propuesta B, agencia de turismo receptivo)
Integrantes: Juan Pedro Sama, Joaquín Castellano y Juan Zeljkovich

## 1. Qué entregamos

- El programa en Java en /src, organizado en paquetes: enums, excepciones, modelo, estrategias, persistencia, reportes, controladores, vista y app.
- Un menú por consola con tres roles (vendedor, coordinador de operaciones y administrador) que cubre los 18 casos de uso del documento de diseño, incluidos Crear reserva y Cancelar reserva, que son los de los diagramas de secuencia.
- Persistencia en archivos: los datos se serializan en data/*.dat y se mantienen entre ejecuciones. El personal y los vehículos se cargan la primera vez desde data/datos_iniciales.txt. Las listas de pasajeros y los reportes también se guardan como .txt.
- 72 pruebas unitarias con JUnit 5 en /test y el plan de pruebas funcionales en docs/plan_de_pruebas.md (66 casos).
- El diagrama de clases final actualizado con los cambios que aparecieron al programar (docs/uml/clases_final.drawio y .png).

Cómo compilarlo, ejecutarlo y correr las pruebas está en el README.

## 2. Cambios respecto del diseño original

Lo principal del diagrama final de la etapa 1 se mantuvo igual: los cuatro controladores, Repositorio<T>, las estrategias de promoción y de cancelación, la interfaz Reporte y la jerarquía de Tour y de Persona. Al programar aparecieron cosas que no habíamos previsto, las agregamos al diagrama y estas son las razones:

| Cambio | Por qué |
|---|---|
| Clase Main (nueva) | En el diagrama no estaba la clase que arranca el programa. Crea los repositorios, se los pasa a los controladores por el constructor y abre el menú, así que es donde se ve la inversión de dependencias |
| Clase CargadorDatosIniciales (nueva) | El enunciado no tiene casos de uso para dar de alta guías, vendedores ni vehículos. Los leemos de un .txt la primera vez que se ejecuta el programa |
| RepositorioArchivo.vincular(...) y revincular() en Salida, Reserva y Guia | Cada archivo .dat se serializa por separado. Al leerlos, la salida que está adentro de una reserva es otro objeto distinto de la que está en salidas.dat, y un pago registrado en uno no se veía en el otro. vincular vuelve a apuntar todas las referencias a los objetos de cada repositorio usando las claves. Los revincular() no los dibujamos porque son solo para esto |
| Menu (abstracta) con MenuVendedor, MenuCoordinador y MenuAdministrador | En el diagrama había un solo MenuConsola con todas las opciones y terminó con más de 600 líneas. Lo separamos en un menú por rol que hereda de Menu la lectura de datos y los formatos. MenuConsola arma la lista de menús y llama a mostrar() del que se elige, sin un switch por rol |
| MenuVendedor guarda el vendedor | El vendedor entra con su legajo y el menú lo recuerda para asignarlo a las reservas que crea |
| ControladorReservas: atributo empleados, buscarVendedor(legajo), buscarCliente(doc), buscarReserva(nro) | Para validar el legajo del vendedor y para que el menú pueda avisar si el cliente no existe antes de pedir todos los datos (flujo 1a de Crear reserva) |
| ControladorReservas.contratarServicio(nro, doc, servicio) | Es otra versión del método para contratar un adicional en una reserva que ya está confirmada (Modificar reserva). La que estaba en el diagrama solo servía para la reserva en curso |
| agregarPasajero recibe también las restricciones | El enunciado pide restricciones alimentarias o de salud y en la firma del diagrama nos habíamos olvidado |
| ControladorCatalogo: registrarProveedor, buscarTour y buscarProveedor | registrarProveedor era parte de CU01 pero no figuraba como operación. Los buscar los usa el menú para no pedir datos de más |
| ControladorSalidas: listarSalidas, guiasDisponibles y vehiculosDisponibles, y los atributos reservas y carpetaListas | El coordinador elige el guía y el vehículo de una lista que ya viene filtrada (habilitado, libre, con capacidad) en vez de ir probando. reservas hace falta para controlar los saldos en el control de plazos |
| cancelarSalida devuelve List<Cancelacion> y controlarPlazos devuelve List<String> | Antes devolvían void y el coordinador no se enteraba cuánto había que devolver ni qué había anulado el control |
| ControladorReportes con una carpeta | Además de mostrar el reporte en pantalla lo guarda en data/reportes |
| Guia guarda sus liquidaciones | Si se pedía dos veces la liquidación de la misma salida se pagaba dos veces. Ahora devuelve la que ya existe |

También hay algunas decisiones que tomamos al programar y que no estaban en los supuestos de la etapa 1:
- una salida se puede iniciar desde su fecha de inicio en adelante. En el CU13 habíamos puesto que tenía que ser ese mismo día, pero así una salida que nadie iniciaba a tiempo quedaba confirmada para siempre y después no se podía cerrar ni liquidar al guía.
- el control de plazos (CU16) se corre solo cada vez que se abre el programa, con la fecha del día. El coordinador también lo puede correr desde su menú.
- el recargo del 7 % por tarjeta en cuotas se cobra aparte: no cuenta para la seña ni para el saldo y no se devuelve si se cancela.
- los códigos de salida (S001, S002, ...) y los números de reserva los genera el sistema.
- una reserva finalizada sigue ocupando su lugar en la salida, para que los reportes y la lista de salidas muestren cuántos viajaron.

## 3. Dónde se ven los patrones en el código

Los principios y patrones que justificamos en la etapa 1 quedaron así en el código:

| Patrón | Dónde se ve |
|---|---|
| SRP | Cada grupo de casos de uso tiene su controlador (ControladorCatalogo, ControladorReservas, ControladorSalidas, ControladorReportes). Leer y escribir archivos está solo en RepositorioArchivo y cada reporte es una clase aparte. Los menús (uno por rol) solo piden datos y muestran resultados |
| OCP | Reserva.calcularTotal(List<Promocion>) y Reserva.cancelar(motivo, hoy, PoliticaCancelacion) no saben qué promoción o política reciben. ControladorReportes recibe una List<Reporte>: para sumar un reporte nuevo alcanza con una clase más y una línea en Main |
| DIP | Los controladores y los reportes reciben Repositorio<T> por constructor. En el programa Main les pasa RepositorioArchivo y en las pruebas usamos RepositorioEnMemoria (está en test/persistencia), así que las pruebas no tocan archivos |
| Experto | Salida calcula sus lugares, su estado y los adicionales vendidos; Reserva su total, lo pagado y el saldo; Pasajero su importe según edad y tipo; Tour su tarifa vigente; Guia su liquidación; Vendedor su comisión |
| Creador | Reserva crea sus Pago, Cancelacion y NotaAjuste; Tour crea las Actividad, Tarifa, ServicioAdicional y Salida; Guia crea la Liquidacion |
| Controlador | Los menús no tienen reglas de negocio, todo pasa por los controladores, que buscan en los repositorios y delegan en las entidades |
| Polimorfismo | TourAventura sobrescribe esApto() y getServiciosObligatorios(), y Salida los usa sin saber de qué categoría es el tour. Promocion, PoliticaCancelacion y Reporte se usan siempre por la interfaz. Cada Empleado sobrescribe getRol(). MenuConsola recorre una List<Menu> y llama a mostrar() sin saber de qué rol es cada uno |

Los requisitos técnicos de la sección 7 los cumplimos así:
- Herencia y polimorfismo: Persona (abstracta) con Cliente, Pasajero y Empleado (abstracta), que tiene a Vendedor, Guia, Coordinador y Administrador. Tour (abstracta) con TourAventura, TourCultural y TourNaturaleza. Menu (abstracta) con MenuConsola, MenuVendedor, MenuCoordinador y MenuAdministrador.
- Encapsulamiento: atributos privados, y protected en Persona y Empleado para que los usen las subclases. Las listas se devuelven con Collections.unmodifiableList para que nadie las modifique desde afuera.
- Colecciones: List en casi todo el modelo, un Set para las categorías del guía, LinkedHashMap en RepositorioArchivo y TreeMap en el reporte de ingresos para que salga ordenado por código. El orden natural está en Actividad, que implementa Comparable<Actividad> (por día y orden), y usamos Comparator en la lista de pasajeros (por apellido y nombre), en el reporte de ventas (por total) y en el de cancelaciones (por fecha y número).
- Excepciones: las propias son ReglaNegocioException y CupoInsuficienteException, con throw/throws. Hay try, catch y finally en RepositorioArchivo, CargadorDatosIniciales y Main.
- Entrada y salida: los objetos se serializan en data/*.dat, se lee datos_iniciales.txt y las listas de pasajeros y los reportes se escriben en .txt.
- La interfaz es un menú por consola.

## 4. Pruebas

Las 72 pruebas unitarias están en /test, separadas por paquete igual que el código:
- modelo: precios por tipo de pasajero y edad, edad mínima y seguro en aventura, tarifas vigentes y superpuestas, cupos y estados de la salida, asignación de guía y vehículo, pagos, seña y recargo, que se aplique solo la mejor promoción, reemplazos, cancelaciones y liquidación del guía.
- estrategias: las dos promociones y las dos políticas de cancelación con sus límites de días.
- persistencia: que lo que se guarda se vuelva a leer igual, que al recargar las referencias queden compartidas y que los datos iniciales no se carguen dos veces.
- controladores y reportes: los casos de uso completos usando RepositorioEnMemoria.

El plan de pruebas funcionales tiene 66 casos que se corrieron sobre el programa por consola. Con esas corridas encontramos errores que las pruebas unitarias no mostraban, por ejemplo que en el reporte de ocupación una salida finalizada aparecía con 0 lugares, y los corregimos.

## 5. Dificultades que encontramos

Lo que más nos costó fueron las referencias después de leer los archivos. Con la serialización cada archivo trae su propia copia de los objetos, entonces al volver a abrir el programa una reserva y su salida dejaban de ser "la misma" y los cambios en una no se veían en la otra. Lo resolvimos con RepositorioArchivo.vincular, que reconecta todo después de cargar (ver punto 2).

Varias reglas dependen de la fecha: la seña, los plazos de 7 y 10 días y la escala de devolución se calculan con los días que faltan para la salida. Para poder probarlas, los métodos del modelo reciben la fecha como parámetro (registrarPago, cancelar, controlarPlazos(hoy)) y el LocalDate.now() quedó en los controladores.

Las excepciones chequeadas nos dieron bastante trabajo repetido. Cuando agregamos validaciones nuevas en Tour (el día de la actividad, los servicios repetidos) hubo que sumar throws ReglaNegocioException en todos los métodos que los llaman y en varias pruebas.

Al principio nos repartimos por capas y algunas partes se superpusieron. La lógica del catálogo la terminó Juan Pedro mientras Joaquín hacía los esqueletos de esas clases, asi que lo reacomodamos y Joaquín pasó a los reportes, el menú y la documentación.

Algunos errores recién aparecieron cuando usamos el programa entero. Tour.getServiciosObligatorios() devolvía una lista vacía y el seguro de viaje no se agregaba solo, se podía cargar dos veces el mismo pasajero en una reserva y el guía se podía liquidar dos veces por la misma salida. Los fuimos corrigiendo con la revisión del código y el plan de pruebas.

Y aparte de lo técnico, perdimos el acceso a la cuenta del repositorio original y tuvimos que migrar el trabajo a este conservando el historial (está explicado en el README).

## 6. Distribución de tareas

Etapa 1:
- Joaquín Castellano: enunciado adaptado a Alaska, actores, requerimientos, reglas de negocio y supuestos, diagrama y descripción de los casos de uso, armado del documento de diseño y el PDF.
- Juan Zeljkovich: diagrama de clases inicial y los dos diagramas de secuencia (Crear reserva y Cancelar reserva).
- Juan Pedro Sama: diagrama de clases final, justificación de los principios SOLID y los patrones GRASP y la nota de cambios. README inicial.

Etapa 2:
- Juan Zeljkovich: enumeraciones, clases de personas (Persona, Cliente, Pasajero, Empleado y sus subclases), Reserva, Pago, Cancelacion y NotaAjuste, las estrategias de promoción y de cancelación, la persistencia (Repositorio, RepositorioArchivo, CargadorDatosIniciales y datos_iniciales.txt) y la interfaz Reporte.
- Juan Pedro Sama: excepciones, la lógica del catálogo (Tour, actividades, tarifas, servicios y programar salidas), Salida con sus estados y asignaciones, Guia y Liquidacion, ControladorCatalogo, ControladorReservas y ControladorSalidas, y gran parte de las pruebas del modelo y de los controladores.
- Joaquín Castellano: esqueletos de las clases del catálogo, revisión del código y correcciones (servicios obligatorios, validaciones de actividades, servicios y clientes, pasajeros repetidos, liquidación repetida), los cuatro reportes y ControladorReportes, los menús por consola y Main, actualización del diagrama de clases, README, plan de pruebas y este informe.

Cada uno trabajó en sus ramas y los cambios entraron a main por pull request.

## 7. Reflexión del grupo

Lo que más nos sirvió fue haber hecho bien el diseño en la etapa 1. Como ya teníamos separados los controladores, los repositorios y las estrategias, al programar casi no hubo que mover responsabilidades de lugar: los cuatro reportes fueron cuatro clases nuevas sin tocar el resto, y como los controladores dependen de Repositorio pudimos probarlos con un repositorio en memoria sin archivos de por medio.

Lo que haríamos distinto: el menú quedó para el final y fue justamente donde aparecieron más errores, porque recién ahí se usaba todo junto. Si lo empezábamos más temprano (aunque fuera con pocas opciones) los hubiéramos visto antes. Encima lo hicimos todo en una sola clase y al final hubo que separarlo en un menú por rol porque ya no se podía leer. También nos hubiera convenido repartir el trabajo por caso de uso y no por capas, para no pisarnos, y acordar desde el principio qué métodos necesitaba el menú de cada controlador, que es de donde salieron la mayoría de los cambios al diagrama.

Con lo que quedó estamos conformes, aunque hay cosas que con más tiempo cambiaríamos. Cuando hay muchas salidas cargadas la consola se vuelve incómoda porque no se pueden filtrar. Y el control de plazos depende de que alguien abra el programa: si un día nadie lo abre, las salidas vencidas se procesan recién la vez siguiente.

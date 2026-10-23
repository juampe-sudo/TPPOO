# Documento de diseño - Etapa 1

**Programación Orientada a Objetos - Trabajo Práctico Obligatorio**

Sistema de gestión para la agencia "Rumbo Norte Alaska"

- Propuesta elegida: B - Agencia de Turismo (adaptada a una agencia receptiva en Alaska, cambio de contexto aprobado por el docente).
- Integrantes: Joaquín Castellano Acuña, Juan Zeljkovich, Juan Pedro Sama.
- Repositorio: https://github.com/jpsam4/TrabajoPOO

## 1. Propuesta elegida y enunciado

Elegimos la Propuesta B (Agencia de Turismo). Con la aprobación del docente cambiamos el contexto: en vez de "Rumbo Sur" trabajamos con una agencia de turismo receptivo en Alaska. Los casos de uso mínimos y las reglas de negocio son los mismos de la propuesta.

La agencia "Rumbo Norte Alaska", con base en Anchorage, es una agencia de turismo receptivo que organiza tours y excursiones con salidas programadas por Alaska. Los tours pueden ser de aventura (kayak entre glaciares en Kenai Fjords, trekking en Denali, escalada en hielo en el glaciar Matanuska), culturales (ruta de la fiebre del oro en Skagway, herencia nativa de Alaska en Anchorage, historia del Iditarod) o de naturaleza (avistaje de osos en Katmai, ballenas en Juneau, auroras boreales en Fairbanks).

De cada tour se conoce el código, el nombre, la descripción, la duración en días, la categoría y el cupo mínimo y máximo. Los tours de aventura registran además un nivel de dificultad y una edad mínima. Cada tour tiene un itinerario formado por actividades (día, orden, horario, descripción y lugar). Algunas actividades incluyen servicios de terceros, como una comida, una noche de alojamiento en un lodge o un vuelo en hidroavión, que se contratan a proveedores, de quienes se registra el nombre, el EIN (número de identificación fiscal de EE.UU., equivalente al CUIT), el contacto y el servicio que prestan.

Cada tour tiene tarifas por temporada con una vigencia y un precio base por pasajero. La agencia programa salidas: cada una corresponde a un tour y tiene fecha de inicio, de fin, estado, un guía y un vehículo (patente, capacidad y tipo: micro, combi o camioneta).

El personal se compone de vendedores (con comisión), guías (idiomas, categorías habilitadas y honorario por día), un coordinador de operaciones y un administrador. Los clientes se registran con nombre, apellido, documento, teléfono, correo y país de residencia. Un cliente arma una reserva para una salida con un vendedor; la reserva incluye pasajeros con documento, fecha de nacimiento, tipo, contacto de emergencia y restricciones alimentarias o de salud. Se pueden contratar servicios adicionales por pasajero (excursiones opcionales, traslados al aeropuerto, seguro de viaje). El cliente paga con una seña y pagos posteriores; si cancela se registra la fecha, el motivo y el importe devuelto.

Al finalizar cada salida el coordinador la cierra y la agencia liquida los honorarios del guía. El administrador emite reportes de ocupación por salida, ventas por vendedor, ingresos por tour y cancelaciones. Se mantienen todas las reglas de negocio de la Propuesta B; solamente cambia el contexto (aprobado por el docente).

## 2. Análisis del enunciado

### 2.1 Actores

| Actor | Qué hace en el sistema |
|-------|------------------------|
| Vendedor | Registra clientes, arma reservas con sus pasajeros, contrata adicionales, registra pagos, modifica y cancela reservas. |
| Coordinador de operaciones | Programa salidas, asigna guía y vehículo, emite la lista de pasajeros, inicia, cierra y cancela salidas y ejecuta el control diario de plazos. |
| Administrador | Mantiene el catálogo de tours (itinerario, tarifas, adicionales y proveedores), liquida los honorarios de los guías y emite los reportes. |

### 2.2 Requerimientos funcionales

- RF01. Dar de alta y modificar tours con su itinerario de actividades, tarifas por temporada, servicios adicionales y proveedores.
- RF02. Programar salidas de un tour con fecha de inicio y cupo.
- RF03. Registrar clientes.
- RF04. Crear reservas para una salida con uno o más pasajeros.
- RF05. Calcular el precio de la reserva según tipo de pasajero, temporada, promociones y adicionales.
- RF06. Contratar servicios adicionales por pasajero.
- RF07. Registrar la seña y los pagos del saldo, en uno o varios pagos y con distintos medios.
- RF08. Modificar reservas (reemplazo de pasajeros y agregado de adicionales).
- RF09. Cancelar reservas aplicando la política de cancelación y registrar la devolución.
- RF10. Asignar guía y vehículo a una salida.
- RF11. Emitir la lista de pasajeros de una salida.
- RF12. Iniciar, cerrar y cancelar salidas.
- RF13. Controlar los plazos: cancelar salidas sin cupo mínimo a 7 días y anular reservas con saldo impago a 10 días.
- RF14. Liquidar los honorarios del guía por salida finalizada.
- RF15. Emitir reportes de ocupación por salida, ventas por vendedor, ingresos por tour y cancelaciones.
- RF16. Guardar y recuperar toda la información en archivos para que se conserve entre ejecuciones.

### 2.3 Reglas de negocio

**Tours y salidas**

- RN01. Un tour tiene un itinerario de actividades ordenadas por día y orden (visita, traslado, comida, alojamiento). Puede ser de un día o de varios.
- RN02. Hay tres categorías: aventura, cultural y naturaleza. La de aventura exige una edad mínima y tiene un nivel de dificultad.
- RN03. Una salida es una realización de un tour en una fecha, con cupo máximo, guía y vehículo. Estados: programada, confirmada, completa, en curso, finalizada y cancelada.
- RN04. La salida se confirma al alcanzar el cupo mínimo. Si 7 días antes de la fecha no lo alcanza, se cancela y se devuelve el 100 % de lo pagado.
- RN05. Al llegar al cupo máximo la salida pasa a "completa" y no admite nuevas reservas.
- RN06. Un guía no puede tener dos salidas superpuestas y solo guía tours de categorías para las que está habilitado.
- RN07. La capacidad del vehículo no puede ser menor que el cupo de la salida.

**Reservas y precios**

- RN08. Una reserva pertenece a un cliente titular y a una salida, e incluye uno o más pasajeros. Ocupa tantos lugares como pasajeros tenga.
- RN09. La tarifa base depende de la temporada (baja, media o alta). Adulto paga 100 %, menor de 12 años 60 %, menor de 3 años no paga pero se registra. Jubilados: 10 % de descuento.
- RN10. Promociones: reserva anticipada (15 % si se reserva con más de 60 días) y grupo (10 % para 6 o más pasajeros). No se acumulan: se aplica la más favorable.
- RN11. Los servicios adicionales se suman al precio por pasajero. El seguro de viaje es obligatorio en los tours de aventura.
- RN12. La reserva se confirma al pagar una seña del 30 %. El saldo se cancela hasta 10 días antes de la salida; si no, la reserva se anula y los lugares vuelven al cupo.
- RN13. Los pagos pueden hacerse en cuotas y con distintos medios (efectivo, transferencia, tarjeta). Tarjeta en cuotas tiene un recargo del 7 %.

**Cancelaciones**

- RN14. Devolución según anticipación: 30 días o más, 90 %; entre 29 y 15 días, 50 %; entre 14 y 7 días, 20 %; menos de 7 días, sin devolución.
- RN15. Si la cancelación la provoca la agencia, se devuelve siempre el 100 %.
- RN16. Un pasajero puede reemplazarse por otro hasta 3 días antes sin cargo, si cumple las condiciones del tour.

**Operación y liquidaciones**

- RN17. Antes de la salida el coordinador emite la lista de pasajeros con documento y contacto de emergencia. No se puede iniciar una salida sin esa lista ni sin guía.
- RN18. El guía cobra por salida finalizada un monto fijo por día más el 5 % de las ventas de adicionales de esa salida.
- RN19. Una reserva pagada no se elimina: las correcciones se registran como notas de ajuste.

### 2.4 Supuestos

Lo que el enunciado no aclara lo resolvimos así:

- S01. Contexto: agencia receptiva en Alaska. Los importes se manejan en dólares estadounidenses (USD). Del proveedor se registra el EIN en lugar del CUIT.
- S02. Los clientes no usan el sistema: todo lo carga el vendedor. Tampoco el guía; recibe la lista de pasajeros que emite el coordinador.
- S03. Es una aplicación de un solo usuario a la vez. No hay login: al iniciar, el menú pide elegir el rol (vendedor, coordinador o administrador) y, si es vendedor, su legajo.
- S04. Personal y vehículos se cargan una vez desde un archivo de datos iniciales; no se pidió un caso de uso para administrarlos.
- S05. Temporadas en Alaska: alta del 15/06 al 31/08, media de mayo al 14/06 y septiembre, baja de octubre a abril (temporada de auroras). La tarifa que se usa es la que está vigente en la fecha de inicio de la salida.
- S06. La edad del pasajero se calcula a la fecha de inicio de la salida. "Menor" es menor de 12 años; si además tiene menos de 3, paga 0 %. De 12 a 17 años paga como adulto. El jubilado paga el 90 % de la tarifa de adulto.
- S07. Las promociones se aplican sobre el subtotal del tour (sin adicionales). La anticipación se mide entre la fecha de la reserva y la fecha de inicio. Para "grupo" cuentan todos los pasajeros, incluidos los menores de 3.
- S08. En los tours de aventura el seguro de viaje se agrega automáticamente a cada pasajero al cargarlo.
- S09. Ocupan cupo las reservas pendientes y confirmadas. La salida pasa a "confirmada" cuando los pasajeros de reservas confirmadas (con seña) llegan al cupo mínimo. Si se libera lugar en una salida completa, vuelve a confirmada (o a programada si queda debajo del mínimo).
- S10. El recargo del 7 % se cobra sobre el importe del pago con tarjeta en más de una cuota y no descuenta saldo: el saldo baja por el importe neto. En una devolución no se devuelven recargos.
- S11. La devolución se hace por el medio con el que se pagó la seña.
- S12. Los días de anticipación para cancelar se cuentan entre la fecha de cancelación y la fecha de inicio de la salida.
- S13. Una reserva anulada por falta de pago del saldo se trata como cancelación del cliente (se aplica la escala de RN14; a 10 días corresponde 20 %).
- S14. El control de plazos (cupo mínimo a 7 días y saldo a 10 días) lo dispara el coordinador una vez por día desde su menú (CU16); el programa no corre en segundo plano.
- S15. Reemplazar un pasajero no modifica el total de la reserva; se deja una nota de ajuste con el cambio. No se agregan pasajeros a una reserva ya creada: se hace otra reserva.
- S16. Dos salidas se superponen si sus rangos de fechas (inicio a fin, inclusive) se pisan.
- S17. Los días trabajados del guía son los días de la salida (fin - inicio + 1). Los adicionales vendidos son los de reservas no anuladas de esa salida.
- S18. La salida toma el cupo mínimo del tour; el cupo máximo lo fija el coordinador al programarla y no puede superar el máximo del tour.
- S19. Al cerrar la salida, sus reservas activas pasan a "finalizada".
- S20. La agencia puede cancelar una salida por su cuenta (en Alaska pasa por clima o por cierre de un parque). El motivo se carga como texto libre.

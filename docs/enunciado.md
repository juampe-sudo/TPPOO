# Enunciado elegido

Propuesta B: Agencia de Turismo, adaptada a una agencia receptiva en Alaska (cambio de contexto aprobado por el docente).

La agencia "Rumbo Norte Alaska", con base en Anchorage, es una agencia de turismo receptivo que organiza tours y excursiones con salidas programadas por Alaska. Los tours pueden ser de aventura (kayak entre glaciares en Kenai Fjords, trekking en Denali, escalada en hielo en el glaciar Matanuska), culturales (ruta de la fiebre del oro en Skagway, herencia nativa de Alaska en Anchorage, historia del Iditarod) o de naturaleza (avistaje de osos en Katmai, ballenas en Juneau, auroras boreales en Fairbanks).

De cada tour se conoce el código, el nombre, la descripción, la duración en días, la categoría y el cupo mínimo y máximo. Los tours de aventura registran además un nivel de dificultad y una edad mínima. Cada tour tiene un itinerario formado por actividades (día, orden, horario, descripción y lugar). Algunas actividades incluyen servicios de terceros, como una comida, una noche de alojamiento en un lodge o un vuelo en hidroavión, que se contratan a proveedores, de quienes se registra el nombre, el EIN (número de identificación fiscal de EE.UU., equivalente al CUIT), el contacto y el servicio que prestan.

Cada tour tiene tarifas por temporada con una vigencia y un precio base por pasajero. La agencia programa salidas: cada una corresponde a un tour y tiene fecha de inicio, de fin, estado, un guía y un vehículo (patente, capacidad y tipo: micro, combi o camioneta).

El personal se compone de vendedores (con comisión), guías (idiomas, categorías habilitadas y honorario por día), un coordinador de operaciones y un administrador. Los clientes se registran con nombre, apellido, documento, teléfono, correo y país de residencia. Un cliente arma una reserva para una salida con un vendedor; la reserva incluye pasajeros con documento, fecha de nacimiento, tipo, contacto de emergencia y restricciones alimentarias o de salud. Se pueden contratar servicios adicionales por pasajero (excursiones opcionales, traslados al aeropuerto, seguro de viaje). El cliente paga con una seña y pagos posteriores; si cancela se registra la fecha, el motivo y el importe devuelto.

Al finalizar cada salida el coordinador la cierra y la agencia liquida los honorarios del guía. El administrador emite reportes de ocupación por salida, ventas por vendedor, ingresos por tour y cancelaciones. Se mantienen todas las reglas de negocio de la Propuesta B; solamente cambia el contexto (aprobado por el docente).

## Casos de uso mínimos

- Administrar el catálogo de tours (itinerario, duración, tarifas) y programar salidas con fecha y cupo.
- Registrar clientes y crear reservas para una salida, con uno o más pasajeros.
- Calcular el precio de la reserva según el tipo de pasajero, la temporada y las promociones, y registrar la seña y los pagos del saldo.
- Contratar servicios adicionales (excursiones opcionales, seguro de viaje, traslados).
- Cancelar o modificar reservas aplicando la política de cancelación.
- Asignar guía y transporte a cada salida, y emitir la lista de pasajeros.
- Liquidar los honorarios de los guías y emitir reportes (ocupación por salida, ventas por vendedor, ingresos por tour, cancelaciones).

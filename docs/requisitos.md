# Requisitos

## Actores

- **Vendedor**: Registra clientes, arma reservas con sus pasajeros, contrata adicionales, registra pagos, modifica y cancela reservas.
- **Coordinador de operaciones**: Programa salidas, asigna guía y vehículo, emite la lista de pasajeros, inicia, cierra y cancela salidas y ejecuta el control diario de plazos.
- **Administrador**: Mantiene el catálogo de tours (itinerario, tarifas, adicionales y proveedores), liquida los honorarios de los guías y emite los reportes.

## Requerimientos funcionales

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

## Reglas de negocio

### Tours y salidas

- RN01. Un tour tiene un itinerario de actividades ordenadas por día y orden (visita, traslado, comida, alojamiento). Puede ser de un día o de varios.
- RN02. Hay tres categorías: aventura, cultural y naturaleza. La de aventura exige una edad mínima y tiene un nivel de dificultad.
- RN03. Una salida es una realización de un tour en una fecha, con cupo máximo, guía y vehículo. Estados: programada, confirmada, completa, en curso, finalizada y cancelada.
- RN04. La salida se confirma al alcanzar el cupo mínimo. Si 7 días antes de la fecha no lo alcanza, se cancela y se devuelve el 100 % de lo pagado.
- RN05. Al llegar al cupo máximo la salida pasa a "completa" y no admite nuevas reservas.
- RN06. Un guía no puede tener dos salidas superpuestas y solo guía tours de categorías para las que está habilitado.
- RN07. La capacidad del vehículo no puede ser menor que el cupo de la salida.

### Reservas y precios

- RN08. Una reserva pertenece a un cliente titular y a una salida, e incluye uno o más pasajeros. Ocupa tantos lugares como pasajeros tenga.
- RN09. La tarifa base depende de la temporada (baja, media o alta). Adulto paga 100 %, menor de 12 años 60 %, menor de 3 años no paga pero se registra. Jubilados: 10 % de descuento.
- RN10. Promociones: reserva anticipada (15 % si se reserva con más de 60 días) y grupo (10 % para 6 o más pasajeros). No se acumulan: se aplica la más favorable.
- RN11. Los servicios adicionales se suman al precio por pasajero. El seguro de viaje es obligatorio en los tours de aventura.
- RN12. La reserva se confirma al pagar una seña del 30 %. El saldo se cancela hasta 10 días antes de la salida; si no, la reserva se anula y los lugares vuelven al cupo.
- RN13. Los pagos pueden hacerse en cuotas y con distintos medios (efectivo, transferencia, tarjeta). Tarjeta en cuotas tiene un recargo del 7 %.

### Cancelaciones

- RN14. Devolución según anticipación: 30 días o más, 90 %; entre 29 y 15 días, 50 %; entre 14 y 7 días, 20 %; menos de 7 días, sin devolución.
- RN15. Si la cancelación la provoca la agencia, se devuelve siempre el 100 %.
- RN16. Un pasajero puede reemplazarse por otro hasta 3 días antes sin cargo, si cumple las condiciones del tour.

### Operación y liquidaciones

- RN17. Antes de la salida el coordinador emite la lista de pasajeros con documento y contacto de emergencia. No se puede iniciar una salida sin esa lista ni sin guía.
- RN18. El guía cobra por salida finalizada un monto fijo por día más el 5 % de las ventas de adicionales de esa salida.
- RN19. Una reserva pagada no se elimina: las correcciones se registran como notas de ajuste.

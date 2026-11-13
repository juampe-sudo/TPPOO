# Informe final - Etapa 2

Rumbo Norte Alaska (Propuesta B, agencia de turismo receptivo)
Integrantes: Juan Pedro Sama, Joaquín Castellano y Juan Zeljkovich

## 1. Qué entregamos

- El programa en Java en /src, organizado en paquetes: enums, excepciones, modelo, estrategias, persistencia, reportes, controladores, vista y app.
- Un menú por consola con tres roles (vendedor, coordinador de operaciones y administrador) que cubre los 18 casos de uso del documento de diseño, incluidos Crear reserva y Cancelar reserva, que son los de los diagramas de secuencia.
- Persistencia en archivos: los datos se serializan en data/*.dat y se mantienen entre ejecuciones. El personal y los vehículos se cargan la primera vez desde data/datos_iniciales.txt. Las listas de pasajeros y los reportes también se guardan como .txt.
- 72 pruebas unitarias con JUnit 5 en /test y el plan de pruebas funcionales en docs/plan_de_pruebas.md (65 casos).
- El diagrama de clases final actualizado con los cambios que aparecieron al programar (docs/uml/clases_final.drawio y .png).

Cómo compilarlo, ejecutarlo y correr las pruebas está en el README.

## 2. Cambios respecto del diseño original

Lo principal del diagrama final de la etapa 1 se mantuvo igual: los cuatro controladores, Repositorio<T>, las estrategias de promoción y de cancelación, la interfaz Reporte y la jerarquía de Tour y de Persona. Al programar aparecieron cosas que no habíamos previsto, las agregamos al diagrama y estas son las razones:

| Cambio | Por qué |
|---|---|
| Clase Main (nueva) | En el diagrama no estaba la clase que arranca el programa. Crea los repositorios, se los pasa a los controladores por el constructor y abre el menú, así que es donde se ve la inversión de dependencias |
| Clase CargadorDatosIniciales (nueva) | El enunciado no tiene casos de uso para dar de alta guías, vendedores ni vehículos. Los leemos de un .txt la primera vez que se ejecuta el programa |
| RepositorioArchivo.vincular(...) y revincular() en Salida, Reserva y Guia | Cada archivo .dat se serializa por separado. Al leerlos, la salida que está adentro de una reserva es otro objeto distinto de la que está en salidas.dat, y un pago registrado en uno no se veía en el otro. vincular vuelve a apuntar todas las referencias a los objetos de cada repositorio usando las claves. Los revincular() no los dibujamos porque son solo para esto |
| MenuConsola guarda el vendedor | El vendedor entra con su legajo y el menú lo recuerda para asignarlo a las reservas que crea |
| ControladorReservas: atributo empleados, buscarVendedor(legajo), buscarCliente(doc), buscarReserva(nro) | Para validar el legajo del vendedor y para que el menú pueda avisar si el cliente no existe antes de pedir todos los datos (flujo 1a de Crear reserva) |
| ControladorReservas.contratarServicio(nro, doc, servicio) | Es otra versión del método para contratar un adicional en una reserva que ya está confirmada (Modificar reserva). La que estaba en el diagrama solo servía para la reserva en curso |
| agregarPasajero recibe también las restricciones | El enunciado pide restricciones alimentarias o de salud y en la firma del diagrama nos habíamos olvidado |
| ControladorCatalogo: registrarProveedor, buscarTour y buscarProveedor | registrarProveedor era parte de CU01 pero no figuraba como operación. Los buscar los usa el menú para no pedir datos de más |
| ControladorSalidas: listarSalidas, guiasDisponibles y vehiculosDisponibles, y los atributos reservas y carpetaListas | El coordinador elige el guía y el vehículo de una lista que ya viene filtrada (habilitado, libre, con capacidad) en vez de ir probando. reservas hace falta para controlar los saldos en el control de plazos |
| cancelarSalida devuelve List<Cancelacion> y controlarPlazos devuelve List<String> | Antes devolvían void y el coordinador no se enteraba cuánto había que devolver ni qué había anulado el control |
| ControladorReportes con una carpeta | Además de mostrar el reporte en pantalla lo guarda en data/reportes |
| Guia guarda sus liquidaciones | Si se pedía dos veces la liquidación de la misma salida se pagaba dos veces. Ahora devuelve la que ya existe |

También hay algunas decisiones que tomamos al programar y que no estaban en los supuestos de la etapa 1:
- una salida solo se puede iniciar el mismo día de su fecha de inicio.
- el control de plazos (CU16) lo corre el coordinador desde el menú, no hay ningún proceso automático.
- el recargo del 7 % por tarjeta en cuotas se cobra aparte: no cuenta para la seña ni para el saldo y no se devuelve si se cancela.
- los códigos de salida (S001, S002, ...) y los números de reserva los genera el sistema.
- una reserva finalizada sigue ocupando su lugar en la salida, para que los reportes y la lista de salidas muestren cuántos viajaron.

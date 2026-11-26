# Mejora: persistencia en base de datos

Rumbo Norte Alaska (Propuesta B, agencia de turismo receptivo)
Integrantes: Juan Pedro Sama, Joaquín Castellano y Juan Zeljkovich

## 1. Qué cambiamos

Hasta la etapa 2 cada repositorio se serializaba en un archivo data/*.dat. Ahora los datos se guardan en una base de datos MySQL que se llama agencia. Las tablas están en data/esquema.sql, y la base y las tablas se crean solas la primera vez que se abre el programa.

El programa se conecta por JDBC con el driver de MySQL (Connector/J), que está en /lib. La url, el usuario y la clave están en data/bd.properties, así cada uno pone los de su MySQL sin tocar el código.

Lo que dice el informe final sobre la persistencia en archivos (los .dat, RepositorioArchivo y vincular) queda reemplazado por lo que está acá.

## 2. Por qué

Con los .dat teníamos estos problemas:
- Cada archivo se serializaba por separado. Al leerlos, la salida que estaba adentro de una reserva era un objeto distinto de la que estaba en salidas.dat. Para arreglarlo hicimos RepositorioArchivo.vincular(...) y los revincular() del modelo, que era la parte más difícil de seguir del proyecto.
- Cada guardar volvía a escribir el archivo entero aunque cambiara una sola reserva.
- Si cambiábamos un atributo de una clase del modelo, los .dat que ya existían no se podían leer más.
- Para ver los datos había que abrir el programa.

En la base cada cosa se guarda una sola vez y las relaciones son claves: la reserva guarda el código de la salida, no una copia de la salida. Así el problema de los objetos repetidos desaparece. Además las tablas se pueden abrir con MySQL Workbench o con la consola de mysql y consultar con SQL.

## 3. ConexionBD (Singleton)

ConexionBD es un Singleton como el que vimos en clase: el constructor es privado, hay un atributo estático instancia y el método estático getInstancia() crea la conexión la primera vez y después devuelve siempre la misma.

Lo usamos porque los siete repositorios tienen que trabajar sobre la misma conexión. Si cada uno abriera la suya habría siete conexiones abiertas con el servidor, cada una con su propia transacción. Además conectarse cuesta (hay que llegar al servidor, iniciar sesión y crear las tablas que falten), y así se hace una sola vez. Tampoco hay que andar pasándola de un objeto a otro, cada repositorio la pide con ConexionBD.getInstancia().getConexion() cuando la necesita.

También tiene un cerrar() estático. Lo llama Main en el finally cuando se sale del programa, y lo usan las pruebas para que cada una arranque con una base nueva. La url, el usuario y la clave los lee de data/bd.properties. Las pruebas cambian la url con la propiedad del sistema agencia.bd para usar otra base, agencia_prueba, y no tocar los datos de verdad. Si no se puede conectar (MySQL apagado, clave mal puesta), el mensaje de error dice el motivo que da MySQL.

## 4. Cómo quedaron los repositorios

La interfaz Repositorio<T> sigue igual (guardar, buscar y listar). RepositorioArchivo se reemplazó por:

- RepositorioSQL<T>, que es abstracta y tiene lo que se repite en todas las entidades. Cuando se crea lee todo de la base y lo deja en un LinkedHashMap, como antes, así buscar y listar no consultan la base y todos los controladores usan los mismos objetos. guardar(obj) llama a escribir(obj) y hace commit. Si algo falla hace rollback, para que no quede por ejemplo una reserva con los pasajeros guardados y los pagos no. Es un Template Method: guardar y cargar están en la clase abstracta y cada subclase completa leer() y escribir().
- Una subclase por entidad: RepositorioProveedorSQL, RepositorioTourSQL, RepositorioClienteSQL, RepositorioEmpleadoSQL, RepositorioVehiculoSQL, RepositorioSalidaSQL y RepositorioReservaSQL. Cada una tiene el SQL de su tabla y de las tablas que dependen de ella. La de reservas, por ejemplo, también guarda los pasajeros, los pagos, las notas y la cancelación.

Para guardar usan INSERT ... ON DUPLICATE KEY UPDATE: si la fila no está la inserta y si ya está (una reserva a la que se le registró un pago, por ejemplo) la actualiza. Las relaciones entre tablas están como FOREIGN KEY, así MySQL rechaza cosas como una reserva de un cliente que no existe.

Las consultas usan PreparedStatement con parámetros (?) en lugar de pegar en el SQL lo que escribe el usuario, y se cierran con try-with-resources.

Las herencias van en una sola tabla con una columna que indica el tipo. La tabla tour tiene categoria (las columnas de aventura quedan vacías en los otros tours) y la tabla empleado tiene rol. Al leer, según esa columna se crea un TourAventura, un TourCultural, un Guia, un Vendedor, etc.

## 5. Qué no se tocó

Los controladores y los reportes dependen de Repositorio<T> y no de una clase concreta (DIP y variaciones protegidas), así que pasar de archivos a base de datos no los afectó. No cambió ninguna línea de:
- ControladorCatalogo, ControladorReservas, ControladorSalidas y ControladorReportes
- los menús y los cuatro reportes
- CargadorDatosIniciales
- Repositorio<T> y RepositorioEnMemoria, ni las pruebas del modelo, de los controladores y de los reportes

## 6. Qué sí cambió

| Cambio | Por qué |
|---|---|
| Main | Es la única clase que conoce los repositorios concretos. Ahora crea los de SQL en vez de RepositorioArchivo, ya no llama a vincular y cierra la conexión al terminar |
| Métodos restaurar en Salida, Reserva y Guia, en lugar de los revincular | Al leer de la base cada objeto tiene que quedar como estaba. Lo que sale siempre igual al aplicar las reglas se arma con los métodos de siempre (agregarActividad, agregarTarifa, agregarServicio, agregarPasajero). El estado de una reserva, sus pagos o su cancelación dependen de cuándo pasaron: si se volviera a llamar a registrarPago o a cancelar con la fecha de hoy daría otro resultado, o la regla lo rechazaría (una reserva anulada no acepta pagos). restaurar pone los valores guardados sin pasar por las reglas. Los Pago, NotaAjuste, Cancelacion y Liquidacion los siguen creando Reserva y Guia, así que el Creador se mantiene |
| Se sacó Serializable del modelo y de las estrategias | Ya no se serializa nada |
| Se borró RepositorioArchivo | Lo reemplazan RepositorioSQL y sus subclases |

En el diagrama de clases (docs/uml/clases_final) cambiamos RepositorioArchivo por ConexionBD, RepositorioSQL y sus subclases. Los restaurar no los dibujamos, igual que antes no estaban los revincular, porque solo se usan al cargar.

## 7. Orden de carga

Para armar una reserva tienen que estar cargados su cliente, su salida y su vendedor, y para una salida su tour, su guía y su vehículo. Por eso cada repositorio recibe por constructor los repositorios que necesita, y Main los crea en este orden:

1. proveedores
2. tours, que usa proveedores para las actividades
3. clientes, empleados y vehículos
4. CargadorDatosIniciales, que solo carga algo si todavía no hay empleados o vehículos
5. salidas, que usa tours, empleados y vehículos y además trae las liquidaciones de los guías
6. reservas, que usa clientes, salidas y empleados

Las liquidaciones se guardan junto con el guía (el controlador guarda el guía después de liquidar), pero se leen con las salidas porque para armarlas tienen que estar cargados el guía y la salida.

## 8. Lo que dejamos así a propósito

- Los restaurar son public porque los repositorios están en otro paquete y Java no deja que un método se vea solo desde un paquete distinto. Los usan solamente los repositorios.
- Hay una sola conexión y no un pool. Es un programa de consola que usa una persona a la vez, un pool no suma nada.
- Para correr el programa y las pruebas de persistencia hace falta tener MySQL instalado y andando, cosa que con los .dat no pasaba. A cambio los datos quedan en una base que se puede consultar y revisar por fuera del programa.
- INSERT ... ON DUPLICATE KEY UPDATE es de MySQL. Si se cambiara a otra base habría que tocar ese SQL en los repositorios, pero los controladores seguirían igual.
- Todo se sigue cargando en memoria al abrir el programa. Para la cantidad de datos de una agencia alcanza, y así buscar y listar quedan como estaban.

## 9. Pruebas

Las pruebas de persistencia (test/persistencia/PersistenciaTest.java) ahora usan MySQL, en la base agencia_prueba, que se borra antes de cada prueba para que todas arranquen de cero. Por eso necesitan MySQL andando. Son cinco:
- guardarYRecargarDesdeLaBase: un cliente guardado aparece igual cuando se vuelven a abrir los repositorios.
- recargaConReferenciasCompartidas: guarda un tour con actividades, tarifa y servicios, una salida con guía y vehículo y una reserva con pasajeros, un pago y una nota. Al volver a abrir, la reserva apunta a la misma salida que está en el repositorio de salidas (lo que antes resolvía vincular), y el total, el estado y el saldo no cambian.
- recuperaCancelacionesYLiquidaciones: se recuperan una salida finalizada, una reserva anulada y la liquidación del guía, y liquidar otra vez devuelve la misma liquidación en lugar de pagarla dos veces.
- siFallaNoQuedaNadaAMedias: si un guardar falla en el medio, el rollback deja la base como estaba.
- datosInicialesSinDuplicar: ya estaba antes y no cambió.

Con estas son 73 pruebas en total.

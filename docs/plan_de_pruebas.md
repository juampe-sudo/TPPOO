# Plan de pruebas funcionales

Estos casos se corrieron sobre el programa por consola, compilado como dice el README, arrancando con la carpeta data sin archivos .dat (o sea solo con el personal y los vehículos de data/datos_iniciales.txt).

La fecha de ejecución fue el **12/11/2026**. Hay reglas que dependen del día (los plazos de 7 y 10 días, la escala de devolución, que una salida solo se inicia el día que empieza) asi que si se repiten otro día hay que correr las fechas de las salidas en la misma medida.

Los casos siguen el orden de una sesión completa: primero el administrador arma el catálogo, después el coordinador programa las salidas, los vendedores hacen las reservas y al final se opera la salida, se liquida al guía y se sacan los reportes. Por eso varios casos usan datos que se cargaron en uno anterior.
Las pruebas unitarias con JUnit van aparte, en /test.

## 1. Catálogo de tours (CU01) - Administrador

| ID | Prueba | Entrada | Resultado esperado | Resultado obtenido | Ok |
|---|---|---|---|---|---|
| P01 | Alta de tour de aventura | KEN01 "Kayak en Kenai Fjords", 2 días, cupo 4 a 12, AVENTURA, dificultad MEDIO, edad mínima 14, seguro de viaje USD 80 | Se registra el tour y el seguro queda como adicional obligatorio | "Tour KEN01 registrado". En el catálogo figura "Adicional Seguro de viaje: USD 80.00 (obligatorio)" | Sí |
| P02 | Actividad con un proveedor que no existe | Día 1, orden 1, 08:00, navegación en kayak hasta el glaciar Aialik, Seward, VISITA, EIN 92-4471035 | Pide los datos del proveedor, lo registra y agrega la actividad | "Ese proveedor no está registrado, cargá sus datos" -> "Proveedor registrado" -> "Actividad agregada" | Sí |
| P03 | Actividades sin proveedor | Día 1 orden 2 13:00 almuerzo (COMIDA) y día 2 orden 1 09:00 regreso a Seward (TRASLADO) | Se agregan | "Actividad agregada" las dos veces | Sí |
| P04 | Actividad fuera de la duración | Día 3 en un tour de 2 días | Error | "Error: El tour dura 2 días, no se puede cargar una actividad el día 3" | Sí |
| P05 | Día y orden repetidos | Día 1, orden 1 de nuevo | Error, no se duplica | "Error: Ya hay una actividad el día 1 con orden 1" | Sí |
| P06 | Tarifa por temporada | BAJA del 01/10/2026 al 30/04/2027, USD 1200 | Se agrega | "Tarifa agregada" | Sí |
| P07 | Tarifa superpuesta | ALTA del 01/04/2027 al 30/09/2027, USD 1500 | Error, abril se pisa con la temporada baja | "Error: La vigencia se superpone con otra tarifa del tour" | Sí |
| P08 | Servicio adicional | "Traslado al aeropuerto", USD 40, no obligatorio | Se agrega | "Servicio agregado" | Sí |
| P09 | Servicio repetido escrito distinto | "traslado al aeropuerto" | Error | "Error: El tour ya tiene el servicio traslado al aeropuerto" | Sí |
| P10 | Código de tour repetido | KEN01 otra vez | Error | "Error: Ya existe un tour con el código KEN01" | Sí |
| P11 | Cupo mínimo mayor al máximo | BAD01 con cupo 10 a 5 | Error, no se registra | "Error: El cupo mínimo tiene que ser mayor a 0 y no superar al máximo" | Sí |
| P12 | Tour de naturaleza | NAT01 "Auroras boreales en Fairbanks", 1 día, cupo 2 a 10, actividad a las 21:00, tarifa BAJA USD 1000, adicional "Cena típica" USD 50 | Se registra, sin seguro obligatorio | "Tour NAT01 registrado". La cena figura sin "(obligatorio)" | Sí |
| P13 | Ver catálogo | Opción 4 | Lista los tours con el itinerario ordenado, tarifas y adicionales | Muestra KEN01 con sus 3 actividades en orden (la primera con el proveedor Kenai Kayak Co), la tarifa y los 2 adicionales, y despues NAT01 | Sí |

## 2. Programar salidas (CU02) - Coordinador

| ID | Prueba | Entrada | Resultado esperado | Resultado obtenido | Ok |
|---|---|---|---|---|---|
| P14 | Fecha que ya pasó | KEN01, 01/01/2020, cupo 8 | Error | "Error: La fecha de inicio de la salida ya pasó" | Sí |
| P15 | Cupo fuera del rango del tour | KEN01, 20/11/2026, cupo 20 | Error | "Error: El cupo tiene que estar entre 4 y 12" | Sí |
| P16 | Salida de varios días | KEN01, 20/11/2026, cupo 8 | Se crea S001 y se calcula la fecha de fin | "Salida S001 programada del 20/11/2026 al 21/11/2026" | Sí |
| P17 | Otras salidas | NAT01 el 12/11/2026 (hoy) cupo 8, el 17/11/2026 cupo 10 y el 15/11/2026 cupo 8 | Se crean S002, S003 y S004 | "Salida S002 programada del 12/11/2026 al 12/11/2026", lo mismo para S003 y S004 | Sí |

## 3. Asignar guía y vehículo (CU11) - Coordinador

| ID | Prueba | Entrada | Resultado esperado | Resultado obtenido | Ok |
|---|---|---|---|---|---|
| P18 | Guías disponibles | S001 (aventura) | Solo muestra guías habilitados para aventura y libres en esas fechas | Lista a 201 Nathan Kowalski y 203 Sofía Lindqvist, no aparece 202 | Sí |
| P19 | Guía no habilitado | S001, legajo 202 | Error | "Error: El guía Aiyana Nanouk no está habilitado para tours de aventura" | Sí |
| P20 | Asignación correcta | S001, guía 201, vehículo KDF482 (8 lugares) | Se asignan los dos | "Guía asignado" y "Vehículo asignado" | Sí |
| P21 | Vehículo con poca capacidad | S003 (cupo 10), guía 203, vehículo KDF482 | Solo ofrece vehículos de 10 lugares o más, y si igual se escribe esa patente da error | Ofrece JTR915 y HMB307. "Error: El vehículo KDF482 tiene capacidad para 8 y la salida tiene un cupo de 10" | Sí |
| P22 | Patente en minúsculas | S002, guía 202, "kdf482". También S004 con guía 202 y JTR915 | La toma igual | "Vehículo asignado" en las dos | Sí |

## 4. Registrar cliente (CU03) - Vendedor

| ID | Prueba | Entrada | Resultado esperado | Resultado obtenido | Ok |
|---|---|---|---|---|---|
| P23 | Legajo que no es de un vendedor | Rol vendedor, legajo 999 | No deja entrar al menú | "No hay un vendedor con legajo 999" | Sí |
| P24 | Correo inválido | Legajo 101. Cliente 28999888, Laura Fernandez, correo laura.fernandez@mail | Error | "Error: El correo laura.fernandez@mail no tiene un formato válido" | Sí |
| P25 | Alta de cliente | Los mismos datos con laura.fernandez@gmail.com, Argentina | Se registra | "Cliente registrado" | Sí |
| P26 | Documento ya registrado | 28999888 | Avisa y no pide los datos de nuevo | "Ese documento ya esta registrado: Laura Fernandez (28999888)" | Sí |
| P27 | Cliente nuevo desde la reserva | Legajo 102, crear reserva con documento 41555666 (Kenji Tanaka, Japón) | Pide los datos, lo registra y sigue con la reserva (flujo 1a de CU04) | "El cliente no está registrado, cargalo primero" -> "Cliente registrado" -> muestra las salidas | Sí |

## 5. Crear reserva, precio y adicionales (CU04, CU05, CU06) - Vendedor

| ID | Prueba | Entrada | Resultado esperado | Resultado obtenido | Ok |
|---|---|---|---|---|---|
| P28 | Pasajeros de la reserva | S001. Laura Fernandez 15/03/1990 ADULTO y Pedro Gomez 02/07/1988 ADULTO, celíaco | Se agregan, el seguro ya viene incluido por ser aventura | "pasajero agregado" dos veces. En los adicionales figura "Seguro de viaje USD 80.00 (ya incluido, es obligatorio)" | Sí |
| P29 | Edad mínima | Tomas Fernandez, nacido el 10/05/2015 (11 años, el mínimo es 14) | No se agrega | "No se agregó: El pasajero Tomas Fernandez no cumple la edad mínima del tour" | Sí |
| P30 | Pasajero repetido | Pedro Gomez 30111222 de nuevo | No se agrega | "No se agregó: El pasajero con documento 30111222 ya está en la reserva" | Sí |
| P31 | Adicional opcional | Traslado al aeropuerto para 28999888 | Se suma a ese pasajero | "Agregado" | Sí |
| P32 | Adicional que el tour no tiene | "Cena" | Error | "Error: El tour no ofrece el servicio Cena" | Sí |
| P33 | Total y seña | Confirmar la reserva | 2 x 1200 + 2 x 80 (seguro) + 40 (traslado) = 2600, seña 780. Ninguna promo: faltan 8 días y son 2 pasajeros | "Reserva 1 creada. Total USD 2600.00, seña a pagar (30%): USD 780.00" | Sí |
| P34 | Jubilado y adicional por pasajero | S002: Kenji Tanaka ADULTO y Haruko Tanaka JUBILADO (1958, diabética), cena para los dos (la segunda escrita "cena típica") | 1000 + 900 (10 % menos por jubilado) + 2 x 50 = 2000, seña 600 | "Reserva 3 creada. Total USD 2000.00, seña a pagar (30%): USD 600.00" | Sí |
| P35 | Lugares libres | Crear otra reserva despues de la 3 | S002 ofrece 6 lugares libres (8 - 2) | "S002 - Auroras boreales en Fairbanks el 12/11/2026 (6 lugares libres)" | Sí |

## 6. Registrar pago (CU07) - Vendedor

| ID | Prueba | Entrada | Resultado esperado | Resultado obtenido | Ok |
|---|---|---|---|---|---|
| P36 | Pago menor a la seña | Reserva 1, USD 500 en efectivo | La reserva sigue pendiente | "Pago registrado. La reserva esta pendiente y el saldo es USD 2100.00" | Sí |
| P37 | Tarjeta en cuotas | Reserva 1, USD 400 con tarjeta en 3 cuotas | Recargo del 7 % (28). Con 900 pagados pasa la seña de 780 y se confirma | "Recargo por pagar en cuotas (7%): USD 28.00" y "La reserva esta confirmada y el saldo es USD 1700.00" | Sí |
| P38 | Pago mayor al saldo | Reserva 1, USD 5000 | Error | "Error: El importe tiene que ser mayor a 0 y no superar el saldo de USD 1700.00" | Sí |
| P39 | Tarjeta en una cuota | Reserva 2 (Laura sola en S001, total 1280), USD 1280 con tarjeta en 1 cuota | Sin recargo, saldo 0 | "Pago registrado. La reserva esta confirmada y el saldo es USD 0.00" | Sí |

## 7. Modificar reserva (CU08) - Vendedor

| ID | Prueba | Entrada | Resultado esperado | Resultado obtenido | Ok |
|---|---|---|---|---|---|
| P40 | Reemplazo de pasajero | Reserva 1: sale Pedro Gomez (30111222) y entra Ana Lopez, 33444555, 21/09/1992 | Se reemplaza sin cargo y queda una nota de ajuste | "Pasajero reemplazado sin cargo, quedó una nota de ajuste" | Sí |
| P41 | Adicional después de pagar | Reserva 1: traslado para Ana Lopez | Sube el total y queda una nota con el importe | "Adicional contratado, el total quedó en USD 2640.00" | Sí |
| P42 | Ver reserva | Reserva 1 | Muestra pasajeros con adicionales, pagos y notas | Laura y Ana con seguro y traslado, los 2 pagos (el de tarjeta "en 3 cuotas") y las notas "Reemplazo de Pedro Gomez por Ana Lopez" y "Adicional Traslado al aeropuerto para Ana Lopez (USD 40.00)" | Sí |

## 8. Cancelar reserva (CU09, CU10) - Vendedor

| ID | Prueba | Entrada | Resultado esperado | Resultado obtenido | Ok |
|---|---|---|---|---|---|
| P43 | Cancelación del cliente a 8 días | Reserva 1, motivo "Cambio de planes del cliente", la causa no es de la agencia | Se devuelve el 20 % de lo pagado (900), por el medio de la seña. El recargo de la tarjeta no se devuelve | "Reserva anulada. Hay que devolver USD 180.00 (Efectivo)" | Sí |
| P44 | Cancelar una reserva anulada | Reserva 1 otra vez | Error | "Error: La reserva 1 no existe, está anulada o finalizada" | Sí |

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

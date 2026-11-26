CREATE TABLE IF NOT EXISTS proveedor (
    ein VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    contacto VARCHAR(200),
    servicio VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS tour (
    codigo VARCHAR(20) PRIMARY KEY,
    categoria VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(1000),
    duracion_dias INT NOT NULL,
    cupo_minimo INT NOT NULL,
    cupo_maximo INT NOT NULL,
    nivel_dificultad VARCHAR(20),
    edad_minima INT
);

CREATE TABLE IF NOT EXISTS actividad (
    tour VARCHAR(20) NOT NULL,
    dia INT NOT NULL,
    orden INT NOT NULL,
    horario TIME NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    lugar VARCHAR(200),
    tipo VARCHAR(20) NOT NULL,
    proveedor VARCHAR(20),
    PRIMARY KEY (tour, dia, orden),
    FOREIGN KEY (tour) REFERENCES tour (codigo),
    FOREIGN KEY (proveedor) REFERENCES proveedor (ein)
);

CREATE TABLE IF NOT EXISTS tarifa (
    tour VARCHAR(20) NOT NULL,
    vigencia_desde DATE NOT NULL,
    vigencia_hasta DATE NOT NULL,
    temporada VARCHAR(20) NOT NULL,
    precio_base DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (tour, vigencia_desde),
    FOREIGN KEY (tour) REFERENCES tour (codigo)
);

CREATE TABLE IF NOT EXISTS servicio_adicional (
    tour VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    precio DOUBLE PRECISION NOT NULL,
    obligatorio BOOLEAN NOT NULL,
    PRIMARY KEY (tour, nombre),
    FOREIGN KEY (tour) REFERENCES tour (codigo)
);

CREATE TABLE IF NOT EXISTS cliente (
    documento VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    telefono VARCHAR(50),
    email VARCHAR(100),
    pais_residencia VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS vehiculo (
    patente VARCHAR(20) PRIMARY KEY,
    capacidad INT NOT NULL,
    tipo VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS empleado (
    legajo INT PRIMARY KEY,
    rol VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    documento VARCHAR(20) NOT NULL,
    telefono VARCHAR(50),
    comision DOUBLE PRECISION,
    honorario_por_dia DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS guia_idioma (
    legajo INT NOT NULL,
    idioma VARCHAR(50) NOT NULL,
    PRIMARY KEY (legajo, idioma),
    FOREIGN KEY (legajo) REFERENCES empleado (legajo)
);

CREATE TABLE IF NOT EXISTS guia_categoria (
    legajo INT NOT NULL,
    categoria VARCHAR(20) NOT NULL,
    PRIMARY KEY (legajo, categoria),
    FOREIGN KEY (legajo) REFERENCES empleado (legajo)
);

CREATE TABLE IF NOT EXISTS salida (
    codigo VARCHAR(20) PRIMARY KEY,
    tour VARCHAR(20) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    cupo_minimo INT NOT NULL,
    cupo_maximo INT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    guia INT,
    vehiculo VARCHAR(20),
    lista_emitida BOOLEAN NOT NULL,
    FOREIGN KEY (tour) REFERENCES tour (codigo),
    FOREIGN KEY (guia) REFERENCES empleado (legajo),
    FOREIGN KEY (vehiculo) REFERENCES vehiculo (patente)
);

CREATE TABLE IF NOT EXISTS liquidacion (
    salida VARCHAR(20) PRIMARY KEY,
    dias INT NOT NULL,
    monto_dias DOUBLE PRECISION NOT NULL,
    monto_adicionales DOUBLE PRECISION NOT NULL,
    FOREIGN KEY (salida) REFERENCES salida (codigo)
);

CREATE TABLE IF NOT EXISTS reserva (
    numero INT PRIMARY KEY,
    fecha DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    total DOUBLE PRECISION NOT NULL,
    cliente VARCHAR(20) NOT NULL,
    salida VARCHAR(20) NOT NULL,
    vendedor INT NOT NULL,
    FOREIGN KEY (cliente) REFERENCES cliente (documento),
    FOREIGN KEY (salida) REFERENCES salida (codigo),
    FOREIGN KEY (vendedor) REFERENCES empleado (legajo)
);

CREATE TABLE IF NOT EXISTS pasajero (
    reserva INT NOT NULL,
    documento VARCHAR(20) NOT NULL,
    nro INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    contacto_emergencia VARCHAR(200),
    restricciones VARCHAR(500),
    PRIMARY KEY (reserva, documento),
    FOREIGN KEY (reserva) REFERENCES reserva (numero)
);

CREATE TABLE IF NOT EXISTS pasajero_servicio (
    reserva INT NOT NULL,
    documento VARCHAR(20) NOT NULL,
    servicio VARCHAR(100) NOT NULL,
    PRIMARY KEY (reserva, documento, servicio),
    FOREIGN KEY (reserva, documento) REFERENCES pasajero (reserva, documento)
);

CREATE TABLE IF NOT EXISTS pago (
    reserva INT NOT NULL,
    nro INT NOT NULL,
    fecha DATE NOT NULL,
    importe DOUBLE PRECISION NOT NULL,
    medio VARCHAR(20) NOT NULL,
    cuotas INT NOT NULL,
    PRIMARY KEY (reserva, nro),
    FOREIGN KEY (reserva) REFERENCES reserva (numero)
);

CREATE TABLE IF NOT EXISTS nota_ajuste (
    reserva INT NOT NULL,
    nro INT NOT NULL,
    fecha DATE NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    importe DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (reserva, nro),
    FOREIGN KEY (reserva) REFERENCES reserva (numero)
);

CREATE TABLE IF NOT EXISTS cancelacion (
    reserva INT PRIMARY KEY,
    fecha DATE NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    importe_devuelto DOUBLE PRECISION NOT NULL,
    medio VARCHAR(20),
    FOREIGN KEY (reserva) REFERENCES reserva (numero)
);

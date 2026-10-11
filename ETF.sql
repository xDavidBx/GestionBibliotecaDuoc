UPDATE usuarios
SET contrasena = LOWER(SHA2('12345', 256))
WHERE correo = 'bibliotecario@colegio.cl';
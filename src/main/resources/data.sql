-- Insertar usuario administrador inicial
INSERT INTO usuarios (correo, nombre, contrasena, rol, activo, fecha_registro) VALUES 
('admin@admin.com', 'Admin', 'admin12345', 'ADMIN', true, CURRENT_TIMESTAMP);

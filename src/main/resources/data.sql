-- Insertar usuario administrador por defecto
INSERT INTO usuarios (nombre, correo, rol, contrasena, fecha_registro, activo) VALUES 
('Administrador', 'admin@admin.com', 'ADMIN', '$2a$10$ynLxR4zY0P6A2w8BI6bR4OY6bKvBg8VGC8w9KjJyIwJ8Y2QjXqX6K', NOW(), true)
ON DUPLICATE KEY UPDATE 
nombre = VALUES(nombre),
rol = VALUES(rol),
contrasena = VALUES(contrasena),
activo = VALUES(activo);
package com.inventario.UsuariosService.controller;

import com.inventario.UsuariosService.entity.Usuario;
import com.inventario.UsuariosService.service.UsuarioService;
import com.inventario.UsuariosService.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioService usuarioService, 
                           JwtUtil jwtUtil, 
                           PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // Solo usuarios con rol ADMIN pueden ver la lista de usuarios
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> getAllUsuarios() {
        return usuarioService.getAllUsuariosIncluyendoInactivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.getUsuarioById(id);
        return usuario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Solo usuarios con rol ADMIN pueden crear usuarios
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUsuario(@RequestBody Usuario usuario) {
        try {
            // Verificar si el correo ya existe
            if (usuarioService.buscarPorCorreo(usuario.getCorreo()).isPresent()) {
                return ResponseEntity.badRequest().body("El correo ya existe");
            }
            
            // Encriptar la contraseña antes de guardar
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
            usuario.setActivo(true);
            Usuario creado = usuarioService.saveUsuario(usuario);
            
            // No devolver la contraseña en la respuesta
            creado.setContrasena(null);
            return ResponseEntity.ok(creado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear usuario: " + e.getMessage());
        }
    }

    // Endpoint público para crear el primer usuario (usado por frontend legacy)
    @PostMapping("/first")
    public ResponseEntity<?> createFirstUsuario(@RequestBody Usuario usuario) {
        try {
            // Solo permite crear si no hay usuarios en el sistema
            if (usuarioService.hasAnyUsuarios()) {
                return ResponseEntity.status(409).body("Ya existen usuarios en el sistema");
            }
            
            // Verificar si el correo ya existe
            if (usuarioService.buscarPorCorreo(usuario.getCorreo()).isPresent()) {
                return ResponseEntity.badRequest().body("El correo ya existe");
            }
            
            // Encriptar la contraseña antes de guardar
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
            usuario.setActivo(true);
            if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
                usuario.setRol("ADMIN"); // Primer usuario siempre es admin
            }
            Usuario creado = usuarioService.saveUsuario(usuario);
            
            // No devolver la contraseña en la respuesta
            creado.setContrasena(null);
            return ResponseEntity.ok(creado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear usuario: " + e.getMessage());
        }
    }

    // Endpoint de login público (no requiere autenticación)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String correo = credentials.get("correo");
        String contrasena = credentials.get("contrasena");
        
        try {
            logger.info("Intento de login para: {}", correo);
            
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorCorreo(correo);
            
            if (usuarioOpt.isEmpty()) {
                logger.warn("Usuario no encontrado: {}", correo);
                return ResponseEntity.status(401).body("Credenciales incorrectas");
            }
            
            Usuario usuario = usuarioOpt.get();
            logger.debug("Usuario encontrado: {}", usuario.getCorreo());
            
            // Verificar contraseña
            if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
                logger.warn("Contraseña incorrecta para: {}", correo);
                return ResponseEntity.status(401).body("Credenciales incorrectas");
            }
            
            if (!usuario.getActivo()) {
                logger.warn("Usuario inactivo: {}", correo);
                return ResponseEntity.status(401).body("Usuario inactivo");
            }
            
            logger.debug("Generando JWT token para: {}", correo);
            
            // Generar JWT token
            String token = jwtUtil.generateToken(usuario.getCorreo(), usuario.getNombre(), usuario.getRol());
            
            logger.info("JWT token generado exitosamente para: {}", correo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("usuario", Map.of(
                "id", usuario.getId(),
                "nombre", usuario.getNombre(),
                "correo", usuario.getCorreo(),
                "rol", usuario.getRol()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error en login para {}: {}", correo, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }

    // Endpoint público para consultas internas entre microservicios
    @GetMapping("/internal/{id}")
    public ResponseEntity<?> getUsuarioForInternalService(@PathVariable Long id) {
        try {
            Optional<Usuario> usuario = usuarioService.getUsuarioById(id);
            if (usuario.isPresent()) {
                Usuario u = usuario.get();
                // Devolver solo los datos necesarios sin la contraseña
                Map<String, Object> response = new HashMap<>();
                response.put("id", u.getId());
                response.put("nombre", u.getNombre());
                response.put("correo", u.getCorreo());
                response.put("rol", u.getRol());
                response.put("activo", u.getActivo());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al buscar usuario: " + e.getMessage());
        }
    }

    // Endpoint para crear el primer admin (sin autenticación)
    @PostMapping("/init-admin")
    public ResponseEntity<?> createAdminUsuario(@RequestBody Usuario usuario) {
        try {
            // Verificar si ya existe algún usuario
            if (usuarioService.hasAnyUsuarios()) {
                return ResponseEntity.status(409).body("Ya existen usuarios en el sistema");
            }
            
            // Crear el primer usuario admin
            usuario.setRol("ADMIN");
            usuario.setActivo(true);
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
            Usuario creado = usuarioService.saveUsuario(usuario);
            
            // No devolver la contraseña
            creado.setContrasena(null);
            return ResponseEntity.ok(creado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear admin: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        if (!usuarioService.getUsuarioById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Usuario updated = usuarioService.updateUsuario(id, usuario);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        if (!usuarioService.getUsuarioById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/correo/{correo}")
    public ResponseEntity<Usuario> getUsuarioByCorreo(@PathVariable String correo) {
        Optional<Usuario> usuario = usuarioService.buscarPorCorreo(correo);
        return usuario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Health check - público
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "UsuariosService");
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }
}

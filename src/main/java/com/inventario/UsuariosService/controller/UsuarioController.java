package com.inventario.UsuariosService.controller;

import com.inventario.UsuariosService.entity.Usuario;
import com.inventario.UsuariosService.service.UsuarioService;
import com.inventario.UsuariosService.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Solo usuarios con rol ADMIN pueden ver la lista de usuarios
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
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
            System.out.println("Intento de login para: " + correo);
            
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorCorreo(correo);
            
            if (usuarioOpt.isEmpty()) {
                System.out.println("Usuario no encontrado: " + correo);
                return ResponseEntity.status(401).body("Credenciales incorrectas");
            }
            
            Usuario usuario = usuarioOpt.get();
            System.out.println("Usuario encontrado: " + usuario.getCorreo());
            
            // Verificar contraseña
            if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
                System.out.println("Contraseña incorrecta para: " + correo);
                return ResponseEntity.status(401).body("Credenciales incorrectas");
            }
            
            if (!usuario.getActivo()) {
                System.out.println("Usuario inactivo: " + correo);
                return ResponseEntity.status(401).body("Usuario inactivo");
            }
            
            System.out.println("Generando JWT token para: " + correo);
            
            // Generar JWT token
            String token = jwtUtil.generateToken(usuario.getCorreo(), usuario.getNombre(), usuario.getRol());
            
            System.out.println("JWT token generado exitosamente");
            
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
            System.out.println("Error en login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
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

package com.inventario.UsuariosService.controller;

import com.inventario.UsuariosService.entity.Usuario;
import com.inventario.UsuariosService.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.getUsuarioById(id);
        return usuario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUsuario(@RequestHeader("X-User-Email") String userEmail, @RequestBody Usuario usuario) {
        // Solo el usuario admin puede crear usuarios
        Optional<Usuario> adminOpt = usuarioService.buscarPorCorreo(userEmail);
        if (adminOpt.isEmpty() || !"admin".equalsIgnoreCase(adminOpt.get().getRol())) {
            return ResponseEntity.status(403).body("Solo el usuario admin puede crear usuarios");
        }
        Usuario creado = usuarioService.saveUsuario(usuario);
        return ResponseEntity.ok(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        if (!usuarioService.getUsuarioById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Usuario updated = usuarioService.updateUsuario(id, usuario);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
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

    @PutMapping("/{id}/contrasena")
    public ResponseEntity<Usuario> cambiarContrasena(@PathVariable Long id, @RequestParam String nuevaContrasena) {
        Usuario actualizado = usuarioService.cambiarContrasena(id, nuevaContrasena);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<Usuario> activarUsuario(@PathVariable Long id) {
        Usuario actualizado = usuarioService.activarUsuario(id);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Usuario> desactivarUsuario(@PathVariable Long id) {
        Usuario actualizado = usuarioService.desactivarUsuario(id);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }
}

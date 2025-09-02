package com.inventario.UsuariosService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "correo", unique = true, nullable = false, length = 100)
    private String correo;
    
    @Column(name = "rol", nullable = false, length = 20)
    private String rol;
    
    @Column(name = "contrasena", nullable = false)
    private String contrasena;
    
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
    
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}

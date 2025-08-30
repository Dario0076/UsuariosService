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
    private String nombre;
    @Column(unique = true, nullable = false)
    private String correo;
    private String rol;
    private String contrasena;
    private LocalDateTime fechaRegistro = LocalDateTime.now();
    private Boolean activo = true;
}

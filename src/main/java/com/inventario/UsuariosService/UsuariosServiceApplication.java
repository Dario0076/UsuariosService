
package com.inventario.UsuariosService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UsuariosServiceApplication {

    // Comentado para usar el endpoint /first para crear el primer usuario
    /*
    @Bean
    public CommandLineRunner initAdmin(@Autowired UsuarioRepository usuarioRepository) {
        return args -> {
            if (usuarioRepository.findByCorreoAndActivoTrue("admin@admin.com").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNombre("Admin");
                admin.setCorreo("admin@admin.com");
                admin.setContrasena("admin1234");
                admin.setRol("admin");
                admin.setActivo(true);
                usuarioRepository.save(admin);
            }
        };
    }
    */

    public static void main(String[] args) {
        SpringApplication.run(UsuariosServiceApplication.class, args);
    }
}


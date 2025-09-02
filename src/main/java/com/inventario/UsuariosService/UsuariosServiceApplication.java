
package com.inventario.UsuariosService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;


@SpringBootApplication
public class UsuariosServiceApplication {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:58943")
                    .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }

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


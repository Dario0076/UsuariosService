package com.inventario.UsuariosService.filter;

import com.inventario.UsuariosService.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String correo = null;
        String jwtToken = null;

        // JWT Token está en la forma "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                correo = jwtUtil.extractCorreo(jwtToken);
            } catch (Exception e) {
                logger.warn("No se pudo obtener el correo del JWT Token");
            }
        }

        // Una vez que obtenemos el token, validamos
        if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Validar token
            if (jwtUtil.validateToken(jwtToken, correo)) {
                
                String rol = jwtUtil.extractRol(jwtToken);
                String nombre = jwtUtil.extractNombre(jwtToken);
                
                // Crear autoridades basadas en el rol
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
                
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(correo, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Agregar información adicional del usuario al contexto
                request.setAttribute("usuarioNombre", nombre);
                request.setAttribute("usuarioRol", rol);
                request.setAttribute("usuarioCorreo", correo);
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        chain.doFilter(request, response);
    }
}

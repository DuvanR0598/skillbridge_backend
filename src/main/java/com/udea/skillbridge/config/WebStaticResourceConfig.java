package com.udea.skillbridge.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Expone la carpeta local "uploads" (donde se guardan los avatares) como
 * recursos estáticos accesibles vía HTTP en la ruta /uploads/**.
 *
 * Ejemplo: el archivo uploads/avatars/3_xxx.jpg queda disponible en
 *          http://localhost:8083/uploads/avatars/3_xxx.jpg
 */
@Configuration
public class WebStaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}

package de.renoth.blposition;

import de.renoth.blposition.config.CorsFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootApplication
public class BlPosition {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(BlPosition.class, args);


    }

    @Bean
    CorsFilter corsFilter() {
        return new CorsFilter();
    }
}

package ar.vicria.horario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


/**
 * Application.
 */
@SpringBootApplication
public class Application {
    /**
     * Start application.
     *
     * @param args start.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

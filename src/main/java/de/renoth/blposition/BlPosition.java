package de.renoth.blposition;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.renoth.blposition.service.LeagueService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

@SpringBootApplication
public class BlPosition {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(BlPosition.class, args);


    }
}

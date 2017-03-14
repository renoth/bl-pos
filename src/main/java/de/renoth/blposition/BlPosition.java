package de.renoth.blposition;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class BlPosition {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(BlPosition.class, args);

        URL url = new URL("https://www.openligadb.de/api/getavailableteams/bl1/2016");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        String json = IOUtils.toString(conn.getInputStream());

        System.out.println(json);

        Gson g = new Gson();

        Type listType = new TypeToken<ArrayList<Team>>(){}.getType();

        List<Team> person = g.fromJson(json, listType);

        System.out.println(person);

    }
}

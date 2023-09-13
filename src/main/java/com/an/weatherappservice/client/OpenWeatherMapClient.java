package com.an.weatherappservice.client;

import com.an.weatherappservice.dto.Coordinate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenWeatherMapClient {

    @Autowired
    CredentialRetriever credentialRetriever;

    public OpenWeatherMapClient() {

    }

    public JsonNode getForecast(String units, String zip) {

        Coordinate coordinate = getCoordinates(units, zip);

        JsonNode response = null;
        try {
            String url = String.format("http://api.openweathermap.org/data/2.5/onecall?APPID=%s&lat=%s&lon=%s&units=%s",
                    credentialRetriever.getSecret(), coordinate.getLatitude(), coordinate.getLongitude(), units);
            ResponseEntity<String> responseEntity = new RestTemplate()
                    .getForEntity(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            response = mapper.readTree(responseEntity.getBody());
        } catch(JsonProcessingException e) {
            System.out.println("unable to parse coordinate json");
        }
        return response;
    }

    protected Coordinate getCoordinates(String units, String zip) {
        Coordinate coordinate = new Coordinate();

        try {
            String url = String.format("https://api.openweathermap.org/data/2.5/weather?appid=%s&units=%s&zip=%s,us",
                    credentialRetriever.getSecret(), units, zip);
            ResponseEntity<String> responseEntity = new RestTemplate()
                    .getForEntity(url, String.class);


            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseEntity.getBody());
            JsonNode coord = root.path("coord");
            coordinate.setLatitude(coord.get("lat").toString());
            coordinate.setLongitude(coord.get("lon").toString());

        } catch(JsonProcessingException e) {
            System.out.println("unable to parse coordinate json");
        }
        return coordinate;
    }
}

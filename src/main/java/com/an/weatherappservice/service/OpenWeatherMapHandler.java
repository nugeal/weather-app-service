package com.an.weatherappservice.service;

import com.an.weatherappservice.client.OpenWeatherMapClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpenWeatherMapHandler {

    @Autowired
    OpenWeatherMapClient client;

    public OpenWeatherMapHandler(){}

    public JsonNode getWeatherForecast(String units, String zip) {
        return client.getForecast(units, zip);
    }
}

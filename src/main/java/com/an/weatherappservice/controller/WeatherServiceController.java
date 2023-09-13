package com.an.weatherappservice.controller;

import com.an.weatherappservice.service.OpenWeatherMapHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherServiceController {

    @Autowired
    OpenWeatherMapHandler handler;

    @GetMapping("/api/weather")
    public String getWeatherForecast(@RequestParam String units, @RequestParam String zip) throws JsonProcessingException {

        JsonNode response = handler.getWeatherForecast(units, zip);

        return response.toString();
    }
}


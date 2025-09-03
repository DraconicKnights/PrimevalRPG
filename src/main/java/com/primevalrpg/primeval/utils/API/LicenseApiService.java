package com.primevalrpg.primeval.utils.API;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LicenseApiService {
    private final String TOKEN;

    private static LicenseApiService instance;

    public LicenseApiService(String token) {
        instance = this;
        this.TOKEN = token;
    }

    public boolean validateServerSubscription() {
        try {
            String API_URL = "https://primevalrpg.com/api/";
            String endpoint = String.format("%s/validate?token=%s", API_URL, TOKEN);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .GET()
                    .build();

            HttpResponse<String> resp = HttpClient.newHttpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) return false;

            // parse JSON text into a JSONObject
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(resp.body());

            // pull the "valid" field out
            Object raw = json.get("valid");
            if (raw instanceof Boolean) {
                return (Boolean) raw;
            } else {
                // missing or not a boolean, treat as false
                return false;
            }
        } catch (ParseException pe) {
            pe.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static LicenseApiService getInstance() {
        return instance;
    }
}
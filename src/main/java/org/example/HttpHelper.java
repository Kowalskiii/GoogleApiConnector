package org.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpHelper {

    final static String API_KEY = "AIzaSyAMUZFB-VMuzT-wKoIcx_ncfc6HP_gPbvk";
    final static String SEARCH_ENGINE = "40109996928d344e5";

    public static String GetGoogleResults(String searchString) {
        String url = "https://www.googleapis.com/customsearch/v1?key="
                + API_KEY + "&cx=" + SEARCH_ENGINE + "&q=" + searchString;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        String responseBody = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .join();

        return responseBody;
    }
}

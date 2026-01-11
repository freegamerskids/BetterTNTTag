package com.lolwm.bettertnttag.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lolwm.bettertnttag.client.exception.TNTTagApiException;
import com.lolwm.bettertnttag.client.model.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TNTTagApiClient {
    private static final String BASE_URL = "https://api.tnttag.info";
    private static final String USER_AGENT = "BetterTNTTag/1.0";

    private final HttpClient httpClient;
    private final Gson gson;

    public TNTTagApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public CompletableFuture<UserStatsResponse> getUserStats(String username, String uuid) throws TNTTagApiException {
        UserRequest request = new UserRequest(username, uuid);
        String jsonBody = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/user/"))
                .header("Content-Type", "application/json")
                .header("User-Agent", USER_AGENT)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendRequest(httpRequest, UserStatsResponse.class);
    }

    public CompletableFuture<UserStatsResponse> getUserStatsByUsername(String username) throws TNTTagApiException {
        return getUserStats(username, null);
    }

    public CompletableFuture<UserStatsResponse> getUserStatsByUuid(String uuid) throws TNTTagApiException {
        return getUserStats(null, uuid);
    }

    public CompletableFuture<StatusResponse> getPlayerStatus(String uuid) throws TNTTagApiException {
        StatusRequest request = new StatusRequest(uuid);
        String jsonBody = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/user/status"))
                .header("Content-Type", "application/json")
                .header("User-Agent", USER_AGENT)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendRequest(httpRequest, StatusResponse.class);
    }

    public CompletableFuture<LeaderboardResponse> getLeaderboards() throws TNTTagApiException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/user/leaderboard"))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        return sendRequest(httpRequest, LeaderboardResponse.class);
    }

    public CompletableFuture<AutocompleteResponse> getAutocomplete(String name) throws TNTTagApiException {
        String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/user/autocomplete?name=" + encodedName))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        return sendRequest(httpRequest, AutocompleteResponse.class);
    }

    public CompletableFuture<CountResponse> getUserCount() throws TNTTagApiException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/user/count"))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        return sendRequest(httpRequest, CountResponse.class);
    }

    public CompletableFuture<NamesResponse> getNameHistory(String uuid) throws TNTTagApiException {
        NamesRequest request = new NamesRequest(uuid);
        String jsonBody = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/user/names"))
                .header("Content-Type", "application/json")
                .header("User-Agent", USER_AGENT)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendRequest(httpRequest, NamesResponse.class);
    }

    public CompletableFuture<MultipleResponse> getMultipleUsers(List<String> uuids) throws TNTTagApiException {
        MultipleRequest request = new MultipleRequest(uuids);
        String jsonBody = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/user/multiple"))
                .header("Content-Type", "application/json")
                .header("User-Agent", USER_AGENT)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendRequest(httpRequest, MultipleResponse.class);
    }

    private <T> CompletableFuture<T> sendRequest(HttpRequest request, Class<T> responseType) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        return gson.fromJson(response.body(), responseType);
                    } else {
                        try {
                            ApiResponse<?> errorResponse = gson.fromJson(response.body(), ApiResponse.class);
                            throw new RuntimeException(new TNTTagApiException(response.statusCode(),
                                    errorResponse.getError() != null ? errorResponse.getError() : "Unknown error",
                                    errorResponse.getCode()));
                        } catch (Exception e) {
                            throw new RuntimeException(new TNTTagApiException(response.statusCode(),
                                    "HTTP " + response.statusCode() + ": " + response.body()));
                        }
                    }
                });
    }
}

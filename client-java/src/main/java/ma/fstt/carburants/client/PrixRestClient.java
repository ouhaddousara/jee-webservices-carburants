package ma.fstt.carburants.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.fstt.carburants.api.dto.ErrorDto;
import ma.fstt.carburants.api.dto.LoginRequest;
import ma.fstt.carburants.api.dto.PrixCarburantDto;
import ma.fstt.carburants.api.dto.PrixStationDto;
import ma.fstt.carburants.api.dto.TokenResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Part 2, client 3 of the subject: a Java class with a {@code main} that calls the JAX-RS service
 * (the RESTEasy one) to get the prices of the fuels of a station at a date.
 *
 * <pre>
 * Usage:  PrixRestClient &lt;nom_station...&gt; &lt;yyyy-MM-dd&gt;   (the last argument is the date)
 * Secrets (never on the command line): CARBURANTS_API_USER (default admin), CARBURANTS_API_PASSWORD.
 * Base URL: CARBURANTS_REST_URL, default http://localhost:8081/carburants-rest/api
 * </pre>
 *
 * Flow: POST /auth/login to obtain a JWT, then GET /prix with "Authorization: Bearer ...".
 * Only the JDK HTTP client is used for the calls (no JAX-RS client library to install).
 */
public final class PrixRestClient {

    private static final String DEFAULT_BASE_URL = "http://localhost:8081/carburants-rest/api";

    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    PrixRestClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /** Logs in and returns the bearer token. */
    String login(String username, String password) throws IOException, InterruptedException {
        String body = json.writeValueAsString(new LoginRequest(username, password));
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/auth/login"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException("Login refused (HTTP " + response.statusCode() + "): " + errorMessage(response.body()));
        }
        return json.readValue(response.body(), TokenResponse.class).getToken();
    }

    /** Calls the shared method of the subject on the JAX-RS service. */
    PrixStationDto prix(String token, String station, String date) throws IOException, InterruptedException {
        String url = baseUrl + "/prix?station=" + URLEncoder.encode(station, StandardCharsets.UTF_8)
                + "&date=" + URLEncoder.encode(date, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException("Request refused (HTTP " + response.statusCode() + "): " + errorMessage(response.body()));
        }
        return json.readValue(response.body(), PrixStationDto.class);
    }

    private String errorMessage(String body) {
        try {
            ErrorDto error = json.readValue(body, ErrorDto.class);
            return error.getMessage() != null ? error.getMessage() : body;
        } catch (IOException e) {
            return body == null || body.isBlank() ? "(empty answer)" : body;
        }
    }

    /** Human readable rendering of an answer. */
    static String format(PrixStationDto result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Station : ").append(result.getStation()).append('\n');
        sb.append("Date    : ").append(result.getDate()).append('\n');
        if (result.getPrix().isEmpty()) {
            sb.append(result.getMessage() == null ? "Aucun prix." : result.getMessage()).append('\n');
            return sb.toString();
        }
        for (PrixCarburantDto p : result.getPrix()) {
            sb.append(String.format("  %-16s %8s MAD   (prix du %s)%n", p.getCarburant(), p.getPrix().toPlainString(), p.getDatePrix()));
        }
        return sb.toString();
    }

    /** The last argument is the date; everything before it is the station name (so no quoting is needed). */
    static String stationFrom(String[] args) {
        return String.join(" ", java.util.Arrays.copyOfRange(args, 0, args.length - 1));
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: PrixRestClient <nom_station...> <yyyy-MM-dd>");
            System.err.println("Environment: CARBURANTS_API_PASSWORD (required), CARBURANTS_API_USER (default admin),");
            System.err.println("             CARBURANTS_REST_URL (default " + DEFAULT_BASE_URL + ")");
            System.exit(2);
        }
        String password = System.getenv("CARBURANTS_API_PASSWORD");
        if (password == null || password.isEmpty()) {
            System.err.println("Define CARBURANTS_API_PASSWORD in the environment first.");
            System.exit(2);
        }
        String user = System.getenv().getOrDefault("CARBURANTS_API_USER", "admin");
        String baseUrl = System.getenv().getOrDefault("CARBURANTS_REST_URL", DEFAULT_BASE_URL);
        String station = stationFrom(args);
        String date = args[args.length - 1];

        try {
            PrixRestClient client = new PrixRestClient(baseUrl);
            String token = client.login(user, password);
            System.out.print(format(client.prix(token, station, date)));
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted.");
            System.exit(1);
        }
    }
}

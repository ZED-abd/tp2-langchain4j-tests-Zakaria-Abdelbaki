package com.zakariaabdelbaki.tp2.outil.meteo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

public class MeteoTool {
    private static final int MAX_JOURS = 7;
    private static final int SEUIL_PLUIE = 25;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @Tool("""
            Indique si la probabilité maximale de pluie dépasse 25 % au moins un jour
            pendant un séjour défini par les coordonnées et le nombre de jours.
            Le départ est exprimé en jours à compter d’aujourd’hui (0 aujourd’hui, 1 demain).
            Les prévisions couvrent les sept premiers jours.
            """)
    public boolean isPluiePrevue(
            @P(name = "latitude", value = "Latitude en degrés") double latitude,
            @P(name = "longitude", value = "Longitude en degrés") double longitude,
            @P(name = "nbJoursDebut", value = "Décalage du départ en jours à partir d’aujourd’hui") int nbJoursDebut,
            @P(name = "nbJours", value = "Durée du séjour en jours") int nbJours) {
        return depasseSeuil(probabilites(latitude, longitude, nbJoursDebut, nbJours), SEUIL_PLUIE);
    }

    int[] probabilites(double latitude, double longitude, int debut, int duree) {
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                || Math.abs(latitude) > 90 || Math.abs(longitude) > 180
                || debut < 0 || debut >= MAX_JOURS || duree < 1 || duree > MAX_JOURS) {
            throw new IllegalArgumentException("Coordonnées ou période hors limites.");
        }
        int fin = Math.min(debut + duree, MAX_JOURS);
        URI uri = URI.create(String.format(Locale.ROOT,
                "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&daily=precipitation_probability_max&timezone=auto&forecast_days=%d",
                latitude, longitude, fin));
        JsonArray valeurs = lireJson(uri).getAsJsonObject("daily").getAsJsonArray("precipitation_probability_max");
        if (valeurs == null || valeurs.size() < fin) {
            throw new IllegalStateException("Prévisions incomplètes.");
        }
        int[] periode = new int[fin - debut];
        for (int jour = debut; jour < fin; jour++) {
            if (valeurs.get(jour).isJsonNull()) {
                throw new IllegalStateException("Probabilité de précipitation indisponible.");
            }
            periode[jour - debut] = valeurs.get(jour).getAsInt();
        }
        return periode;
    }

    static boolean depasseSeuil(int[] probabilites, int seuil) {
        for (int probabilite : probabilites) {
            if (probabilite > seuil) {
                return true;
            }
        }
        return false;
    }

    @Tool("Cherche une ville par son nom. Renvoie les coordonnées du premier résultat, latitude puis longitude.")
    public double[] getCoordonnees(@P(name = "ville", value = "Nom de la ville à géocoder") String ville) {
        String nom = URLEncoder.encode(ville, StandardCharsets.UTF_8);
        URI uri = URI.create("https://geocoding-api.open-meteo.com/v1/search?count=1&language=fr&name=" + nom);
        JsonArray resultats = lireJson(uri).getAsJsonArray("results");
        if (resultats == null || resultats.isEmpty()) {
            throw new IllegalArgumentException("Aucune coordonnée trouvée pour cette ville.");
        }
        JsonObject premier = resultats.get(0).getAsJsonObject();
        return new double[]{premier.get("latitude").getAsDouble(), premier.get("longitude").getAsDouble()};
    }

    private JsonObject lireJson(URI uri) {
        HttpRequest requete = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(20)).GET().build();
        try {
            HttpResponse<String> reponse = http.send(requete, HttpResponse.BodyHandlers.ofString());
            if (reponse.statusCode() != 200) {
                throw new IllegalStateException("Open-Meteo HTTP " + reponse.statusCode());
            }
            return JsonParser.parseString(reponse.body()).getAsJsonObject();
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de joindre Open-Meteo.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Requête météo interrompue.", e);
        }
    }
}

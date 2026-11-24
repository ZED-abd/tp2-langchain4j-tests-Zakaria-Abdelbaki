package com.zakariaabdelbaki.tp2.outil.meteo;

import java.util.Arrays;

public class TestMeteo {
    public static void main(String[] args) {
        MeteoTool outil = new MeteoTool();
        System.out.println("Rabat, latitude 34.02, longitude -6.84");
        for (int debut : new int[]{0, 1}) {
            int[] probabilites = outil.probabilites(34.02, -6.84, debut, 3);
            System.out.printf("Départ J+%d : %s %% ; pluie au seuil de 25 %% : %b%n",
                    debut, Arrays.toString(probabilites), outil.isPluiePrevue(34.02, -6.84, debut, 3));
            System.out.printf("Vérification des seuils -1 et 100 %% : %b / %b%n",
                    MeteoTool.depasseSeuil(probabilites, -1), MeteoTool.depasseSeuil(probabilites, 100));
        }
    }
}

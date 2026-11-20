package me.ksmc.smartcardplugin.util;

import me.ksmc.smartcardplugin.Main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FareRetriever {

    public static String getFare(String agencyID, String originStation, String destinationStation) throws IOException {
        String fareChartFileName = ItemUtils.getFareChartFileName(agencyID);
        String fareChartFilePath = Main.getPlugin().getDataFolder().getAbsolutePath() + "/fare_charts/" + fareChartFileName;

        BufferedReader reader = new BufferedReader(new FileReader(fareChartFilePath));

        String header = reader.readLine();
        String[] destinationStations = header.split(",");

        Map<Integer, String> destinationStationMap = new HashMap<>();
        for (String station : destinationStations) {

        }


        Map<String, Map<String, Double>>  fareMap = new HashMap<>();





        String fare = "";

        return fare;
    }

}

package me.ksmc.smartcardplugin.util;

import me.ksmc.smartcardplugin.Main;
import org.bukkit.ChatColor;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FareRetriever {

    public static double getFare(String agencyID, String originStation, String destinationStation) throws IOException {
        String fareChartFileName = ItemUtils.getFareChartFileName(agencyID);
        String fareChartFilePath = Main.getPlugin().getDataFolder().getAbsolutePath() + "/fare_charts/" + fareChartFileName;
        File fareChart = new File(fareChartFilePath);

        // gets the index of the destination station on the first row
        Scanner scanner = new Scanner(fareChart);
        String headerRow = scanner.nextLine();
        String[] destinationStations = headerRow.split(",");

        Map<String, Integer> destinationStationsMap = new HashMap<>();
        // int i = 1 since the first value of the top line in the .csv file is an empty space
        for (int i = 1; i < destinationStations.length; i++) {
            destinationStationsMap.put(destinationStations[i], i);
        }
        int destinationStationIndex = destinationStationsMap.get(destinationStation);

        // gets the fare:
        // locate the row on which the origin station is located, then go to destinationStationIndex on that row
        String fareRaw = "";
        while (scanner.hasNextLine()) {
            String row = scanner.nextLine();
            String[] originStationRowLine = row.split(",");
            if (originStationRowLine[0].equals(originStation)) {
                fareRaw = originStationRowLine[destinationStationIndex];
                break;
            }
        }

        double tripFare;
        if (!StringUtils.validMoneyString(fareRaw)) {
            return 0;
            //return ChatColor.RED + "Error: The fare amount input for this trip is invalid.";
        }
        tripFare = Double.parseDouble(fareRaw);

        return tripFare;
    }

}

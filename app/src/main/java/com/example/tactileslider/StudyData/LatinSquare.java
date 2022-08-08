package com.example.tactileslider.StudyData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class LatinSquare {

    private HashMap<String, ArrayList<String>> latinSquareMap;
    private final ArrayList<String> variants = new ArrayList<>(Arrays.asList("audio_horizontal", "audio_vertical", "tactile_horizontal",
            "tactile_vertical", "combined_horizontal", "combined_vertical"));

    public LatinSquare() {
        // Setup LatinSquareValues
        initializeLatinSquareMap();
    }

    private void initializeLatinSquareMap() {
        latinSquareMap = new HashMap<>();
        ArrayList<String> p1 = new ArrayList<>(Arrays.asList(variants.get(0), variants.get(1),
                variants.get(5), variants.get(3), variants.get(4), variants.get(2)));
        ArrayList<String> p2 = new ArrayList<>(Arrays.asList(variants.get(1), variants.get(3),
                variants.get(0), variants.get(2), variants.get(5), variants.get(4)));
        ArrayList<String> p3 = new ArrayList<>(Arrays.asList(variants.get(3), variants.get(2),
                variants.get(1), variants.get(4), variants.get(0), variants.get(5)));
        ArrayList<String> p4 = new ArrayList<>(Arrays.asList(variants.get(2), variants.get(4),
                variants.get(3), variants.get(5), variants.get(1), variants.get(0)));
        ArrayList<String> p5 = new ArrayList<>(Arrays.asList(variants.get(5), variants.get(0),
                variants.get(4), variants.get(1), variants.get(2), variants.get(3)));
        ArrayList<String> p6 = new ArrayList<>(Arrays.asList(variants.get(4), variants.get(5),
                variants.get(2), variants.get(0), variants.get(3), variants.get(1)));

        ArrayList<ArrayList> latinSquareOrders = new ArrayList<>(Arrays.asList(p6, p1, p2, p3, p4, p5));
        for(int i = 1; i <= StudySettings.MAX_PARTICIPANTS; i++){
            int pos = i % latinSquareOrders.size();
            latinSquareMap.put("P_"+i, latinSquareOrders.get(pos));
        }
    }

    public ArrayList<String> getVariantOrder (String id) {
        return latinSquareMap.get(id);
    }

}

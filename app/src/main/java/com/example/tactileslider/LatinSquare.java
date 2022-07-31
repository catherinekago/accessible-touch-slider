package com.example.tactileslider;

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
        latinSquareMap.put("P_1", p1);
        latinSquareMap.put("P_2", p2);
        latinSquareMap.put("P_3", p3);
        latinSquareMap.put("P_4", p4);
        latinSquareMap.put("P_5", p5);
        latinSquareMap.put("P_6", p6);
        latinSquareMap.put("P_7", p1);
        latinSquareMap.put("P_8", p2);
        latinSquareMap.put("P_9", p3);
        latinSquareMap.put("P_10", p4);
        latinSquareMap.put("P_11", p5);
        latinSquareMap.put("P_12", p6);
        latinSquareMap.put("P_13", p1);
        latinSquareMap.put("P_14", p2);
        latinSquareMap.put("P_15", p3);
        latinSquareMap.put("P_16", p4);
        latinSquareMap.put("P_17", p5);
        latinSquareMap.put("P_18", p6);
    }

    public ArrayList<String> getVariantOrder (String id) {
        return latinSquareMap.get(id);
    }

}

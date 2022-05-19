package com.example.tactileslider;

import java.util.ArrayList;
import java.util.Collections;

public class UserData {

    private String userID;
    private ArrayList<Measurement> measurementList = new ArrayList<Measurement>();
    private ArrayList<Double> targetList = new ArrayList<Double>();
    private int currentTargetIndex;

    public UserData(String userID, int times){
        this.userID = userID;
        this.targetList = createRandomizedTargetList(times);
        this.currentTargetIndex = 0;
        this.addMeasurement(this.getCurrentTargetList().get(this.getCurrentTargetIndex()));
    }

    public void incrementCurrentTargetIndex() {
        this.currentTargetIndex++;
    }

    public int getCurrentTargetIndex(){
        return this.currentTargetIndex;
    }

    public ArrayList<Double> getCurrentTargetList(){
        return this.targetList;
    }

    private ArrayList<Double> createRandomizedTargetList(int times) {
        // CAUTION: Hardcoded values for range of sliders
        final double from = 0.00;
        final double until = 10.00;

        // Fill targetlist with values
        ArrayList<Double> list = new ArrayList<Double>();
        for (int t = 1; t <= times; t ++) {
            for(double i = from; i <= until; i++){
                list.add(i);
            }
        }

        // Randomize targetList
        Collections.shuffle(list);
        return list;
    }

    public void addMeasurement(double target){
        Measurement newMeasurement = new Measurement(target);
        measurementList.add(newMeasurement);
    }

    public Measurement getLastMeasurement(){
        return measurementList.get(measurementList.size()-1);
    }

}

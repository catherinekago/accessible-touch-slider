package com.example.tactileslider;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UserData implements Serializable {

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
        final double from = 1.00;
        final double until = 7.00;

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

    public void pushDataToDatabase() {
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firebase.collection(userID);

        ArrayList<Measurement> test = measurementList;
        Map<String, Object> measurement = new HashMap<>();
        Measurement lastMeasurement = this.getLastMeasurement();
        measurement.put("target", lastMeasurement.getTarget());
        measurement.put("input", lastMeasurement.getInput());
        measurement.put("error", lastMeasurement.getError());
        measurement.put("completionTime", lastMeasurement.getCompletionTime());

        ArrayList<Map> measurementPairs = new ArrayList<>();
        for(int i=0; i < getLastMeasurement().getMeasurementPairs().size(); i++) {
            Map<String, Object> measurementPair = new HashMap<>();
            measurementPair.put("value", getLastMeasurement().getMeasurementPairs().get(i).getValue());
            measurementPair.put("target", getLastMeasurement().getMeasurementPairs().get(i).getTimestamp());
            measurementPairs.add(measurementPair);
        }
        measurement.put("measurementPairs", measurementPairs);
        collectionRef.document("pair_" + currentTargetIndex).set(measurement);
    }

    public String getUserId(){
        return this.userID;
    }

}

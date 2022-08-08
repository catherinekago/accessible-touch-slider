package com.example.tactileslider.SliderActivity;



import com.example.tactileslider.SliderActivity.Measurement;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UserData implements Serializable {

    private String userID;
    private ArrayList<Measurement> measurementList = new ArrayList<Measurement>();
    private ArrayList<Double> targetList;

    private int currentTargetIndex = 0;

    public UserData(String userID, int times) {
        this.userID = userID;
        this.targetList = createRandomizedTargetList(times);
        this.currentTargetIndex = 0;
    }

    public void createNewUserDataReference(String id) {
        // add collection to firebase
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        // add userID to firebase collectionList collection
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", id);
        firebase.collection("userDataCollectionNames").document(id).set(userData);
    }


    public void incrementCurrentTargetIndex() {
        this.currentTargetIndex++;
    }

    public int getCurrentTargetIndex() {
        return this.currentTargetIndex;
    }

    public void setTargets(ArrayList<Double> list) {
        this.targetList = list;
    }

    public void resetCurrentTargetIndex() {
        this.currentTargetIndex = 0;
    }

    public ArrayList<Double> getCurrentTargetList() {
        return this.targetList;
    }

    public ArrayList<Double> createRandomizedTargetList(int times) {
        // CAUTION: Hardcoded values for range of sliders
        final double from = 1.00;
        final double until = 7.00;

        // Fill targetlist with values
        ArrayList<Double> list = new ArrayList<Double>();
        for (int t = 1; t <= times; t++) {
            for (double i = from; i <= until; i++) {
                list.add(i);
            }
        }

        // Randomize targetList
        Collections.shuffle(list);
        return list;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void addMeasurement(double target) {
        Measurement newMeasurement = new Measurement(target);
        measurementList.add(newMeasurement);
    }

    public Measurement getLastMeasurement() {
        return measurementList.get(measurementList.size() - 1);
    }

    public void pushDataToDatabase() {
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firebase.collection(userID);

        Map<String, Object> measurement = new HashMap<>();
        Measurement lastMeasurement = this.getLastMeasurement();

        measurement.put("target", lastMeasurement.getTarget());
        measurement.put("error", lastMeasurement.getError());

        measurement.put("input", lastMeasurement.getInput());
        measurement.put("completionTime", lastMeasurement.getCompletionTime());

        ArrayList<Map> measurementPairs = new ArrayList<>();
        for (int i = 0; i < getLastMeasurement().getMeasurementPairs().size(); i++) {
            Map<String, Object> measurementPair = new HashMap<>();
            measurementPair.put("xCoord", getLastMeasurement().getMeasurementPairs().get(i).getxCoord());
            measurementPair.put("value", getLastMeasurement().getMeasurementPairs().get(i).getValue());
            measurementPair.put("timestamp", getLastMeasurement().getMeasurementPairs().get(i).getTimestamp());
            measurementPairs.add(measurementPair);
        }
        measurement.put("measurementPairs", measurementPairs);
        collectionRef.document("task_" + currentTargetIndex).set(measurement);


    }

    public String getUserId() {
        return this.userID;
    }

}
package com.example.tactileslider;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UserData implements Serializable {

    private String userID;
    private ArrayList<Measurement> measurementList = new ArrayList<Measurement>();
    private ArrayList<Double> targetList = new ArrayList<Double>();
    private ArrayList<String> questionList = new ArrayList<String>();
    private int currentQuestionIndex;
    private int currentTargetIndex;

    public UserData(String userID, int times){
        this.userID = userID;
        this.targetList = createRandomizedTargetList(times);
        this.questionList = createRandomizedQuestionList();
        this.currentTargetIndex = 0;
        this.addMeasurement(this.getCurrentTargetList().get(this.getCurrentTargetIndex()));
    }

    private ArrayList<String> createRandomizedQuestionList() {

        ArrayList<String> list = new ArrayList<String>();
        // TODO: add questions for questionnaire part here;

        // Randomize questionList
        Collections.shuffle(list);
        return list;
    }

    public void incrementCurrentTargetIndex() {
        this.currentTargetIndex++;
    }

    public int getCurrentTargetIndex(){
        return this.currentTargetIndex;
    }

    public void incrementCurrentQuestionIndex() {
        this.currentQuestionIndex++;
    }

    public int getCurrentQuestionIndex(){
        return this.currentQuestionIndex;
    }

    public void resetCurrentTargetIndex() {this.currentTargetIndex  = 0;}
    public void resetCurrentQuestionIndex() {this.currentQuestionIndex  = 0;}

    public ArrayList<Double> getCurrentTargetList(){
        return this.targetList;
    }

    private ArrayList<Double> createRandomizedTargetList(int times) {
        // CAUTION: Hardcoded values for range of sliders
        // TODO replace with balanced latin square?
        final double from = 1.00;
        final double until = 3.00; // TODO: 7

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

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void addMeasurement(double target){
        Measurement newMeasurement = new Measurement(target);
        measurementList.add(newMeasurement);
    }

    public void addMeasurement(String question){
        Measurement newMeasurement = new Measurement(question);
        measurementList.add(newMeasurement);
    }

    public Measurement getLastMeasurement(){
        return measurementList.get(measurementList.size()-1);
    }

    public void pushDataToDatabase(String phase) {
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firebase.collection(userID);

        Map<String, Object> measurement = new HashMap<>();
        Measurement lastMeasurement = this.getLastMeasurement();

        if (phase.equals("study")){
            measurement.put("target", lastMeasurement.getTarget());
            measurement.put("error", lastMeasurement.getError());
        } else if (phase.equals("questionnaire")){
            measurement.put("question", lastMeasurement.getQuestion());
        }
        measurement.put("input", lastMeasurement.getInput());
        measurement.put("completionTime", lastMeasurement.getCompletionTime());

        ArrayList<Map> measurementPairs = new ArrayList<>();
        for(int i=0; i < getLastMeasurement().getMeasurementPairs().size(); i++) {
            Map<String, Object> measurementPair = new HashMap<>();
            measurementPair.put("xCoord", getLastMeasurement().getMeasurementPairs().get(i).getxCoord());
            measurementPair.put("value", getLastMeasurement().getMeasurementPairs().get(i).getValue());
            measurementPair.put("timestamp", getLastMeasurement().getMeasurementPairs().get(i).getTimestamp());
            measurementPairs.add(measurementPair);
        }
        measurement.put("measurementPairs", measurementPairs);
        collectionRef.document("task_" + currentTargetIndex).set(measurement);
    }

    public String getUserId(){
        return this.userID;
    }

    public ArrayList<String> getCurrentQuestionList() {
        return this.questionList;
    }
}

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
    private int currentQuestionIndex = 0;
    private int currentTargetIndex = 0;

    public UserData(String userID, int times){
        this.userID = userID;
        this.targetList = createRandomizedTargetList(times);
        this.questionList = createRandomizedQuestionList();
        this.currentTargetIndex = 0;
        this.currentQuestionIndex = 0;
    }

    public void createNewUserDataReference(String id){
        // add collection to firebase
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        // add userID to firebase collectionList collection
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", id);
        firebase.collection("userDataCollectionNames").document(id).set(userData);
    }

    public ArrayList<String> createRandomizedQuestionList() {

        ArrayList<String> list = new ArrayList<String>();
        // TODO: add questions for questionnaire part here;
        list.add("Wie fühlen Sie sich heute?");
        list.add("Wie hungrig sind Sie?");
        list.add("Wie müde sind Sie?");
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

    public void setQuestions(ArrayList<String> list) {this.questionList = list;}
    public void setTargets(ArrayList<Double> list) {this.targetList = list;}

    public int getCurrentQuestionIndex(){
        return this.currentQuestionIndex;
    }

    public void resetCurrentTargetIndex() {this.currentTargetIndex  = 0;}
    public void resetCurrentQuestionIndex() {this.currentQuestionIndex  = 0;}

    public ArrayList<Double> getCurrentTargetList(){
        return this.targetList;
    }

    public ArrayList<Double> createRandomizedTargetList(int times) {
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
        if (phase.equals("study")){
            collectionRef.document("task_" + currentTargetIndex).set(measurement);
        } else if (phase.equals("questionnaire")){
            collectionRef.document("task_" + currentQuestionIndex).set(measurement);
        }

    }

    public String getUserId(){
        return this.userID;
    }

    public ArrayList<String> getCurrentQuestionList() {
        return this.questionList;
    }
}

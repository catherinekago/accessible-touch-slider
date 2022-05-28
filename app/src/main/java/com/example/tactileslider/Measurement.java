package com.example.tactileslider;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

// The Measurement Class holds all relevant data of one measurement
public class Measurement implements Serializable {

    private double target;
    private double input;
    private double error;
    private long completionTime;

    private ArrayList<MeasurementPair> measurementPairs = new ArrayList<MeasurementPair>();

    public Measurement(double target) { this.target = target; }

    public void setInput(double input) {
        this.input = input;
        // Calculate error
        double error = this.target - this.input;
        BigDecimal errorBD = new BigDecimal(error).setScale(2, RoundingMode.HALF_UP);
        setError(errorBD.doubleValue());
    }
    public double getInput() { return input; }

    public double getTarget() { return target; }

    public void setError(double error) { this.error = error; }
    public double getError() { return error; }

    public long getCompletionTime() { return completionTime; }
    public void setCompletionTime(long completionTime) { this.completionTime = completionTime; }

    public void addMeasurementPair(double value, long timestamp) {
        MeasurementPair newPair = new MeasurementPair(value, timestamp);
        this.measurementPairs.add(newPair);
    }

    public void removeLastMeasurementPair() {
        if (this.measurementPairs.size()>0){
            this.measurementPairs.remove(this.measurementPairs.size()-1);
        }
    }

    public ArrayList<MeasurementPair> getMeasurementPairs(){
        return this.measurementPairs;
    }
}

// The MeasurementPair class holds the value-timestamp pairs that occurred during the measurements,
// for more detailed analysis.
class MeasurementPair implements Serializable{
    private double value;
    private long timestamp;

    public MeasurementPair(double value, long timestamp){
        this.value = value;
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

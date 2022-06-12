package com.example.tactileslider;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

// The Measurement Class holds all relevant data of one measurement
public class Measurement implements Serializable {

    private double target;
    private String question;
    private double input;
    private double error;
    private long completionTime;

    private ArrayList<MeasurementPair> measurementPairs = new ArrayList<MeasurementPair>();

    public Measurement(double target) { this.target = target; }

    public Measurement(String question) { this.question = question; }

    public void setInput(double input) {
        this.input = input;
        // Calculate error
            double error = this.target - this.input;
            BigDecimal errorBD = new BigDecimal(error).setScale(2, RoundingMode.HALF_UP);
            setError(errorBD.doubleValue());

    }
    public double getInput() { return input; }

    public double getTarget() { return target; }

    public String getQuestion() { return question; }

    public void setError(double error) { this.error = error; }
    public double getError() { return error; }

    public long getCompletionTime() { return completionTime; }
    public void setCompletionTime(long completionTime) { this.completionTime = completionTime; }

    public void addMeasurementPair(double xCoord, double value, long timestamp) {
        MeasurementPair newPair = new MeasurementPair(xCoord, value, timestamp);
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
    private final double xCoord;
    private double value;
    private long timestamp;

    public MeasurementPair(double xCoord, double value, long timestamp){
        this.xCoord = xCoord;
        this.value = value;
        this.timestamp = timestamp;
    }

    public double getxCoord(){ return xCoord; }

    public double getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

package com.example.tactileslider;

public class LikertItem {
    private Integer yCoord;
    private int alphaValue;
    private float frequencyValue;
    private int amplitudeValue;

    public LikertItem(Integer yCoord, int alphaValue, float freqValue, int amplitudeValue) {
        this.yCoord = yCoord;
        this.alphaValue = alphaValue;
        this.frequencyValue = freqValue;
        this.amplitudeValue = amplitudeValue;
    }

    public int getAlphaValue() {
        return alphaValue;
    }

    public float getFrequencyValue() {
        return frequencyValue;
    }
    public float getAmplitudeValue() {
        return amplitudeValue;
    }
}

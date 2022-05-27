package com.example.tactileslider;

public class LikertItem {
    private Integer yCoord;
    private int alphaValue;
    private float frequencyValue;

    public LikertItem(Integer yCoord, int alphaValue, float freqValue) {
        this.yCoord = yCoord;
        this.alphaValue = alphaValue;
        this.frequencyValue = freqValue;
    }

    public int getAlphaValue() {
        return alphaValue;
    }

    public float getFrequencyValue() {
        return frequencyValue;
    }
}

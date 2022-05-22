package com.example.tactileslider;

public class LikertItem {
    private Integer yCoord;
    private int alphaValue;
    private int frequencyValue;

    public LikertItem(Integer yCoord, int alphaValue) {
        this.yCoord = yCoord;
        this.alphaValue = alphaValue;
    }

    public int getAlphaValue() {
        return alphaValue;
    }
}

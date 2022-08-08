package com.example.tactileslider.SliderActivity;

public class LikertItem {
    private Integer yCoord;
    private int alphaValue;
    private int sound;
    private int amplitudeValue;

    public LikertItem(Integer coord, int alphaValue, int sound, int amplitudeValue) {
        this.yCoord = coord;
        this.alphaValue = alphaValue;
        this.sound = sound;
        this.amplitudeValue = amplitudeValue;
    }

    public int getAlphaValue() {
        return alphaValue;
    }

    public int getSound() {
        return sound;
    }
    public float getAmplitudeValue() {
        return amplitudeValue;
    }
}

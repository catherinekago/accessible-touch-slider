package com.example.tactileslider;

import android.media.SoundPool;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.IntStream;

public class TactileArea {

    View sliderView;
    TextView coorinatesView;
    private final String COORD_PREFIX ="Touch Input:   ";
    private int heightTactileArea;
    private int heightTopBar;
    private ArrayList<Integer> likertYCoords = new ArrayList<Integer>();
    private int likertSpacing;
    private final int LIKERT_PADDING = 5;
    private ArrayList<LikertItem> likertItems = new ArrayList<LikertItem>();
    private double userInputValue = 0.0;
    private int soundId;

    // Audio Feedback
    private AudioFeedback audioFeedback;
    private final float MIN_FREQ = 0.5F;
    private final float MAX_FREQ = 2.0F;
    private long lastPlayTime = 0;
    private final long MIN_PAUSE = 300;

    private SliderAreaActivity mainActivity;


    public TactileArea(SliderAreaActivity mainActivity, UserData userData) {

        this.mainActivity = mainActivity;
        sliderView = mainActivity.findViewById(R.id.sliderView);
        coorinatesView = mainActivity.findViewById(R.id.topBar);
        this.audioFeedback = new AudioFeedback();
        this.soundId = audioFeedback.getSoundPool().load(mainActivity, R.raw.audiofeedback, 1);
        audioFeedback.getSoundPool().setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                setUpLayoutDrawnListener();
            }
        });

    }

    // Set up observer to determine when layout is drawn to retrieve height of SliderArea
    private void setUpLayoutDrawnListener() {
        ViewTreeObserver vto = sliderView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    sliderView.getViewTreeObserver()
                            .removeOnGlobalLayoutListener(this);
                } else {
                    sliderView.getViewTreeObserver()
                            .removeGlobalOnLayoutListener(this);
                }
                setUpLayout();


            }
        });
    }

    // Set up the layout with all relevant heights and likert subareas.
    private void setUpLayout() {
        heightTactileArea = sliderView.getMeasuredHeight();
        coorinatesView.setText("Height of tactile area: " + heightTactileArea);
        heightTopBar = coorinatesView.getMeasuredHeight();
        likertYCoords = calculateLikertYCords(heightTopBar, heightTactileArea);
        int alphaStep = 255 / 6;
        float frequencyStep = (MAX_FREQ - MIN_FREQ) / 3;
        ArrayList<Float> frequencies = new ArrayList<Float>();
        Collections.addAll(frequencies,
                MIN_FREQ,
                MIN_FREQ + frequencyStep,
                MIN_FREQ + 2*frequencyStep,
                MIN_FREQ + 3*frequencyStep,
                MIN_FREQ + 2*frequencyStep,
                MIN_FREQ + frequencyStep,
                MIN_FREQ);
        for (int i = 0; i < likertYCoords.size(); i++) {
            int alphaValue = 255 - alphaStep * i;
            likertItems.add(new LikertItem(likertYCoords.get(i), alphaValue, frequencies.get(i)));
        }
    }

    /**
     * @param heightTopBar The height of the topbar to be added to calculation of yCoord
     * @param heightTactileArea The height of the actual tactile area where the yCoord positions are determined
     * @return an arraylist of all y coordinates of the seven Likert scale item positions
     */
    private ArrayList<Integer> calculateLikertYCords(double heightTopBar, double heightTactileArea) {
        this.likertSpacing = (int) (heightTactileArea / 6);
        ArrayList<Integer> likertYCoords = new ArrayList<Integer>();
        for (int i = 0; i < 7; i++){
            likertYCoords.add((int) heightTopBar + (i*likertSpacing));
        }
        return likertYCoords;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void handleTouchEvent(int xTouch, int yTouch, UserData userData, long taskStart){
        userInputValue = calculateLikertValue(yTouch);
        // todo: add
        if (yTouch < heightTopBar || yTouch > heightTopBar + heightTactileArea){
            coorinatesView.setText(COORD_PREFIX +  "out of bounds");
        } else {
            int[] touchRange = IntStream.rangeClosed(yTouch-LIKERT_PADDING, yTouch+LIKERT_PADDING).toArray();
            for (int value: touchRange){
                if (likertYCoords.contains(value)){
                    int index = likertYCoords.indexOf(value);
                    LikertItem crossedItem = likertItems.get(index);
                    sliderView.getBackground().setAlpha(crossedItem.getAlphaValue());
                    coorinatesView.setText(COORD_PREFIX + userInputValue);
                    if (System.currentTimeMillis() > MIN_PAUSE + lastPlayTime){
                        audioFeedback.getSoundPool().play(soundId, 1F, 1F, 1, 0, crossedItem.getFrequencyValue());
                        lastPlayTime = System.currentTimeMillis();
                    }
                    break;
                }
            }
            coorinatesView.setText(COORD_PREFIX + userInputValue);
            // Write measurementPair to userData
            //userData.getLastMeasurement().addMeasurementPair(System.currentTimeMillis() - taskStart, yTouch);
        }
    }

    private double calculateLikertValue(int yTouch) {
        double likertValue = ((yTouch - heightTopBar) * 1.0 / heightTactileArea) * 6.0 + 1.0;
        return Math.round(likertValue * 100.0) / 100.0;
    }
}



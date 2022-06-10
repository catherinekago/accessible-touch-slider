package com.example.tactileslider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.IntStream;

public class TactileArea {


    private static final String HORIZONTAL = "horizontal" ;
    private static final String VERTICAL = "vertical" ;
    private static final String SHORT = "short" ;
    private static final String LONG = "long" ;
    View sliderView;
    TextView coorinatesView;
    private final String COORD_PREFIX = "Touch Input:   ";
    private int sliderLength;
    private int heightTopBar;
    private ArrayList<Integer> likertCoords = new ArrayList<Integer>();
    private int likertSpacing;
    private final int LIKERT_PADDING = 5; // vibrotactile feedback not working as accurately as audio here
    private ArrayList<LikertItem> likertItems = new ArrayList<LikertItem>();
    private double userInputValue = 0.0;
    private int soundId;
    private String feedbackMode;
    private String length;
    private String orientation;
    private  String phase;

    private final String STUDY = "study";
    private final String QUEST = "questionnaire";

    // Audio Feedback
    private AudioFeedback audioFeedback;
    private final float MIN_FREQ = 0.5F;
    private final float MAX_FREQ = 2.0F;
    private long lastPlayTime = 0;
    private final long MIN_PAUSE = 300;
    private final String AUDIO = "audio";

    //Vibration Feedback
    private Vibrator vibrator;
    private final int MIN_AMP = 25;
    private final float MAX_AMP = 255;
    private final String TACTILE = "tactile";
    private final String COMBINED = "combined";

    private SliderAreaActivity mainActivity;
    private ArrayList<Integer> coordRanges;


    public TactileArea(SliderAreaActivity mainActivity, UserData userData, String feedbackMode, String length, String orientation, String phase) {

        this.mainActivity = mainActivity;
        sliderView = mainActivity.findViewById(R.id.sliderView);
        coorinatesView = mainActivity.findViewById(R.id.topBar);
        this.audioFeedback = new AudioFeedback();
        this.soundId = audioFeedback.getSoundPool().load(mainActivity, R.raw.piep, 1);
        this.feedbackMode = feedbackMode;
        this.length = length;
        this.orientation = orientation;
        this.phase = phase;

        audioFeedback.getSoundPool().setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                // Initialize layout once audioFeedback has loaded
                setUpLayoutDrawnListener();
            }
        });

        // Setup vibration
        vibrator = (Vibrator) this.mainActivity.getSystemService(Context.VIBRATOR_SERVICE);

    }

    // Set up observer to determine when layout is drawn to retrieve height of SliderArea
    private void setUpLayoutDrawnListener() {
        ViewTreeObserver vto = sliderView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
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


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void changeLayout(String mode, String length, String orientation) {
        this.feedbackMode = mode;
        this.length = length;
        this.orientation = orientation;
        setUpLayout();
    }

    // Set up the layout with all relevant heights and likert subareas.
    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setUpLayout() {
        // todo: perhaps adjust length to getMeasuredHeight instead of getMeasuredWidth
        if (length.equals(SHORT)){
            sliderLength = sliderView.getMeasuredWidth() /2;
        } else {
            sliderLength = sliderView.getMeasuredWidth();

        }
        sliderView.setLayoutParams(new LinearLayout.LayoutParams(sliderView.getMeasuredWidth(), sliderLength));
        coorinatesView.setText("Height of tactile area: " + this.sliderLength);
        heightTopBar = coorinatesView.getMeasuredHeight();
        likertCoords = calculateLikertYCords(heightTopBar, this.sliderLength);
        ArrayList<Integer> likertCoordRanges = new ArrayList<Integer>();
        for (int coord : likertCoords) {
            int[] touchRange = IntStream.rangeClosed(coord - LIKERT_PADDING, coord + LIKERT_PADDING).toArray();
            for (int i : touchRange) {
                likertCoordRanges.add(i);
            }
        }
        coordRanges = likertCoordRanges;
        int alphaStep = 255 / 6;
        ArrayList<Float> frequencies = calculateFrequencies();
        ArrayList<Integer> amplitudes = calculateAmplitudes();

        for (int i = 0; i < likertCoords.size(); i++) {
            int alphaValue = 255 - alphaStep * i;
            likertItems.add(new LikertItem(likertCoords.get(i), alphaValue, frequencies.get(i), amplitudes.get(i)));
        }
    }

    // Create amplitude steps for the "V" pattern - light to dark to light
    private ArrayList<Integer> calculateAmplitudes() {
        int amplitudeStep = (int) ((MAX_AMP - MIN_AMP) / 3);
        ArrayList<Integer> amplitudes = new ArrayList<Integer>();
        Collections.addAll(amplitudes,
                MIN_AMP + 3 * amplitudeStep,
                MIN_AMP + 2 * amplitudeStep,
                MIN_AMP + amplitudeStep,
                MIN_AMP,
                MIN_AMP + amplitudeStep,
                MIN_AMP + 2 * amplitudeStep,
                MIN_AMP + 3 * amplitudeStep
        );
        return amplitudes;
    }

    // Create frequency steps for the "V" pattern - light to dark to light
    private ArrayList<Float> calculateFrequencies() {
        float frequencyStep = (MAX_FREQ - MIN_FREQ) / 3;
        ArrayList<Float> frequencies = new ArrayList<Float>();
        Collections.addAll(frequencies,
                MIN_FREQ + 3 * frequencyStep,
                MIN_FREQ + 2 * frequencyStep,
                MIN_FREQ + frequencyStep,
                MIN_FREQ,
                MIN_FREQ + frequencyStep,
                MIN_FREQ + 2 * frequencyStep,
                MIN_FREQ + 3 * frequencyStep
        );
        return frequencies;
    }

    /**
     * @param heightTopBar      The height of the topbar to be added to calculation of yCoord
     * @param heightTactileArea The height of the actual tactile area where the yCoord positions are determined
     * @return an arraylist of all y coordinates of the seven Likert scale item positions
     */
    private ArrayList<Integer> calculateLikertYCords(double heightTopBar, double heightTactileArea) {
        this.likertSpacing = (int) (heightTactileArea / 6);
        ArrayList<Integer> likertYCoords = new ArrayList<Integer>();
        for (int i = 0; i < 7; i++) {
            likertYCoords.add((int) heightTopBar + (i * likertSpacing));
        }
        return likertYCoords;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void handleTouchEvent(int xTouch, int yTouch, UserData userData, long taskStart, String phase) {

        userInputValue = calculateLikertValue(yTouch);
        if (coordRanges.contains(yTouch)) {
            LikertItem crossedItem = likertItems.get(getLikertIndexFromRange(yTouch));
            sliderView.getBackground().setAlpha(crossedItem.getAlphaValue());
            coorinatesView.setText(COORD_PREFIX + userInputValue);

            // Generate audio feedback
            if ((feedbackMode.equals(AUDIO) || (feedbackMode.equals(COMBINED))) && System.currentTimeMillis() > MIN_PAUSE + lastPlayTime) {
                audioFeedback.getSoundPool().play(soundId, 1F, 1F, 1, 0, crossedItem.getFrequencyValue());
                lastPlayTime = System.currentTimeMillis();

                // Generate tactile feedback
            } if ((feedbackMode.equals(TACTILE) || (feedbackMode.equals(COMBINED))) && System.currentTimeMillis() > MIN_PAUSE + lastPlayTime) {
                // Haptic feedback that slider is activated
                VibrationEffect effect = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    effect = VibrationEffect.createOneShot(150, (int) crossedItem.getAmplitudeValue());
                    vibrator.vibrate(effect);
                    lastPlayTime = System.currentTimeMillis();
                }

            }
        }
        coorinatesView.setText(COORD_PREFIX + userInputValue);
        // Write measurementPair to userData
        if (phase.equals(STUDY) || phase.equals(QUEST)){
            userData.getLastMeasurement().addMeasurementPair(xTouch, userInputValue, (long) System.currentTimeMillis() - taskStart);
        }
    }


    private int getLikertIndexFromRange(int yTouch) {
        int distance = Math.abs(likertCoords.get(0) - yTouch);
        int idx = 0;
        for (int c = 1; c < likertCoords.size(); c++) {
            int cdistance = Math.abs(likertCoords.get(c) - yTouch);
            if (cdistance < distance) {
                idx = c;
                distance = cdistance;
            }
        }
        return idx;
    }

    public double calculateLikertValue(int coord) {
        double likertValue = ((coord - heightTopBar) * 1.0 / sliderLength) * 6.0 + 1.0;
        return Math.round(likertValue * 100.0) / 100.0;
    }
}



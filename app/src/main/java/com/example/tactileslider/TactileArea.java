package com.example.tactileslider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.PlaybackParams;
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



    private static final String SHORT = "short" ;

    private final UserData userData;
    View sliderView;
    TextView coorinatesView;

    private int sliderLength;
    private static int MAX_LENGTH = 1680; // TODO find out on smartphone
    private int heightTopBar;
    private ArrayList<Integer> likertCoords = new ArrayList<Integer>();
    private int likertSpacing;
    private final int LIKERT_PADDING = 15; // vibrotactile feedback not working as accurately as audio here // 1dp == 0.16mm
    private ArrayList<LikertItem> likertItems = new ArrayList<LikertItem>();
    private double userInputValue = 0.0;
    private int soundId;
    private String feedbackMode;
    private String orientation;
    private  String phase;


    // Audio Feedback
    private SoundPool soundPool;
    private long lastPlayTime = 0;
    private final long MIN_PAUSE_SAME_STEP = 350;
    private final String AUDIO = "audio";

    //Vibration Feedback
    private Vibrator vibrator;
    private final int MIN_AMP = 25;
    private final float MAX_AMP = 255;
    private final String TACTILE = "tactile";
    private final String COMBINED = "combined";
    private Context context;

    private SliderAreaActivity mainActivity;
    private ArrayList<Integer> coordRanges;
    private LikertItem lastCrossedItem;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public TactileArea(SliderAreaActivity mainActivity, UserData userData, String feedbackMode, String orientation, Context context) {

        this.mainActivity = mainActivity;
        sliderView = mainActivity.findViewById(R.id.sliderView);
        coorinatesView = mainActivity.findViewById(R.id.topBar);
        this.feedbackMode = feedbackMode;
        this.orientation = orientation;
        this.phase = phase;
        this.context = context;
        this.userData = userData; 


        // Create Soundpool object
        int maxStreams = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(maxStreams)
                    .build();
        } else {
            soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
        }

        // Setup vibration
        vibrator = (Vibrator) this.mainActivity.getSystemService(Context.VIBRATOR_SERVICE);

        setUpLayoutDrawnListener();


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
        if (length.equals(SHORT)){
            sliderLength = MAX_LENGTH /2;
        } else {
            sliderLength = MAX_LENGTH;

        }
        sliderView.setLayoutParams(new LinearLayout.LayoutParams(sliderView.getMeasuredWidth(), sliderLength));
        coorinatesView.setText(userData.getUserId());
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
        ArrayList<Integer> sounds = createSoundList();
        ArrayList<Integer> amplitudes = calculateAmplitudes();

        for (int i = 0; i < likertCoords.size(); i++) {
            int alphaValue = 255 - alphaStep * i;
            likertItems.add(new LikertItem(likertCoords.get(i), alphaValue, sounds.get(i), amplitudes.get(i)));
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
    private ArrayList<Integer> createSoundList() {
        ArrayList<Integer> sounds = new ArrayList<Integer>();
        Collections.addAll(sounds,
               soundPool.load(context, R.raw.c5, 1),
               soundPool.load(context, R.raw.g4, 1),
               soundPool.load(context, R.raw.e4, 1),
               soundPool.load(context, R.raw.c4, 1),
               soundPool.load(context, R.raw.e4, 1),
               soundPool.load(context, R.raw.g4, 1),
               soundPool.load(context, R.raw.c5, 1)
        );
        return sounds;
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
    public void handleTouchEvent(int xTouch, int yTouch, UserData userData, long taskStart, String phase, Boolean taskStarted) {

        userInputValue = calculateLikertValue(yTouch);
        boolean valueIsMin = userInputValue >= 0.0;
        boolean valueIsNotMax = userInputValue <=8.0;
        if(valueIsMin && valueIsNotMax) {
            if (coordRanges.contains(yTouch)) {

                LikertItem crossedItem = likertItems.get(getLikertIndexFromRange(yTouch));
                sliderView.getBackground().setAlpha(crossedItem.getAlphaValue());
                //coorinatesView.setText(COORD_PREFIX + userInputValue);

                // Generate audio feedback
                if (feedbackMode.equals(AUDIO) || feedbackMode.equals(COMBINED)) {                    // If it is the same step and the pause has passed, or it is a new step, or the first step
                    if (lastCrossedItem == null || crossedItem.getAlphaValue() == lastCrossedItem.getAlphaValue() && System.currentTimeMillis() > MIN_PAUSE_SAME_STEP + lastPlayTime || crossedItem.getAlphaValue() != lastCrossedItem.getAlphaValue()) {
                        soundPool.play(crossedItem.getSound(), 0.5F, 0.5F, 1, 0, 1f);
                        if (feedbackMode.equals(AUDIO)){
                            lastPlayTime = System.currentTimeMillis();
                            lastCrossedItem = crossedItem;
                        }
                    }
                }

                    // Generate tactile feedback
                if (feedbackMode.equals(TACTILE) || feedbackMode.equals(COMBINED)){
                    // If it is the same step and the pause has passed, or it is a new step, or the first step
                    if(lastCrossedItem == null || crossedItem.getAlphaValue() == lastCrossedItem.getAlphaValue() && System.currentTimeMillis() > MIN_PAUSE_SAME_STEP + lastPlayTime || crossedItem.getAlphaValue() != lastCrossedItem.getAlphaValue()) {
                        lastCrossedItem = crossedItem;
                        // Haptic feedback that slider is activated
                        VibrationEffect effect = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            effect = VibrationEffect.createOneShot(75, (int) crossedItem.getAmplitudeValue());
                            lastPlayTime = System.currentTimeMillis();
                            vibrator.vibrate(effect);
                        }
                    }


                }

            }


            // Write measurementPair to userData
            if ((phase.equals(STUDY) || phase.equals(QUEST)) && taskStarted) {
                userData.getLastMeasurement().addMeasurementPair(xTouch, userInputValue, (long) System.currentTimeMillis() - taskStart);
            }
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



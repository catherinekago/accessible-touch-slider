package com.example.tactileslider;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SliderAreaActivity extends AppCompatActivity {

    // Tactile Area Variant
    TactileArea tactileArea;
    private int xTouch;
    private int yTouch;

    private Context context;

    // Touch Gestures
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; //milliseconds
    long lastClickTime = 0;
    private boolean isLongClick = false;

    private long startTask = 0;
    private Vibrator vibrator;
    private boolean taskCompleted = false;
    private TextToSpeech ttsObject;
    private boolean tasksStarted = false;

    // Intent Extras
    UserData userData;
    ArrayList<String> feedbackModes;
    ArrayList<String> orientations;

    int currentVariant = 0;

    private SoundPool soundPool;
    private int doubleTapSound;
    private int successSound;
    private TextView coorinatesView;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide(); //<< this
        super.onCreate(savedInstanceState);

        getExtras();

        setContentView(R.layout.activity_slider_area);

        this.context = this;
        tactileArea = new TactileArea(this, userData, feedbackModes.get(currentVariant), orientations.get(currentVariant), context);
        coorinatesView = findViewById(R.id.topBar);

        View.OnTouchListener getCoordinates = setUpTapAndMotionListener();
        findViewById(R.id.mainView).setOnTouchListener(getCoordinates);
        View.OnLongClickListener detectLongPress = setUpLongClickListener();
        findViewById(R.id.mainView).setOnLongClickListener(detectLongPress);

        // Setup vibration
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        // Create Soundpool object
        int maxStreams = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(maxStreams)
                    .build();
        } else {
            soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
        }

        // Setup sounds
        doubleTapSound = soundPool.load(context, R.raw.doubletap, 1);
        successSound = soundPool.load(context, R.raw.completion_sound, 1);


        // Setup tts component
        this.ttsObject = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
        Locale german = new Locale("de", "DE");
        ttsObject.setLanguage(german);


        // add first measurement
        userData.addMeasurement(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));


    }

    // Get data from intent
    private void getExtras() {

        userData = (UserData) getIntent().getSerializableExtra("userData");

        String feedbackMode_1 = (String) getIntent().getExtras().get("feedbackMode_1");
        String orientation_1 = (String) getIntent().getExtras().get("orientation_1");
        String feedbackMode_2 = (String) getIntent().getExtras().get("feedbackMode_2");
        String orientation_2 = (String) getIntent().getExtras().get("orientation_2");
        String feedbackMode_3 = (String) getIntent().getExtras().get("feedbackMode_3");
        String orientation_3 = (String) getIntent().getExtras().get("orientation_3");
        String feedbackMode_4 = (String) getIntent().getExtras().get("feedbackMode_4");
        String orientation_4 = (String) getIntent().getExtras().get("orientation_4");
        String feedbackMode_5 = (String) getIntent().getExtras().get("feedbackMode_5");
        String orientation_5 = (String) getIntent().getExtras().get("orientation_5");
        String feedbackMode_6 = (String) getIntent().getExtras().get("feedbackMode_6");
        String orientation_6 = (String) getIntent().getExtras().get("orientation_6");

        feedbackModes = new ArrayList<>(Arrays.asList(feedbackMode_1, feedbackMode_2, feedbackMode_3, feedbackMode_4, feedbackMode_5, feedbackMode_6));
        orientations = new ArrayList<>(Arrays.asList(orientation_1, orientation_2, orientation_3, orientation_4, orientation_5, orientation_6));

        String id = userData.getUserId() + "_" + feedbackModes.get(0) + "_" + orientations.get(0);
        userData.setUserID(id);
        // Add ID to database only if phase is study or quest
        userData.createNewUserDataReference(id);
    }

    // Setup touch listener to determine the coordinates of the touch event to handle it accordingly
    private View.OnTouchListener setUpTapAndMotionListener() {
        View.OnTouchListener handleTouch = new View.OnTouchListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                xTouch = (int) event.getX();
                yTouch = (int) event.getY();
                long clickTime = System.currentTimeMillis();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.i("TOUCHED AT ", String.valueOf(xTouch + ", " + yTouch));
                        //Handle double click
                        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                            // if (tasksStarted && !taskCompleted) {
                            // handleValueSelection(); // no differentiation between select and continue

                            if (tasksStarted && !taskCompleted) {
                                // Only allow to proceed to next task if user has provided input (attempt to avoid skipping of questions)
                                if (userData.getLastMeasurement().getMeasurementPairs().size() > 0) {
                                    handleValueSelection();
                                    continueWithNextTask();
                                }
                            } else if (!tasksStarted) {
                                startFirstTask();
                            }
                        }
                        lastClickTime = clickTime;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isLongClick) {
                            tactileArea.handleTouchEvent(xTouch, yTouch, userData, startTask, tasksStarted);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        isLongClick = false;
                        break;
                }
                return false;

            }
        };
        return handleTouch;
    }

    // Initialize first task
    private void startFirstTask() {
        soundPool.play(doubleTapSound, 0.15F, 0.15F, 1, 0, 1f);
        readAloudTarget();
        tasksStarted = true;
        userData.addMeasurement(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
    }

    // Generate speech output to read aloud task
    private void readAloudTarget() {

        int target = (int) Math.round(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
        coorinatesView.setText(userData.getUserId() + ":      Target " + userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
        final int interval = 250; // quarter second
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {


            public void run() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "0.25");
                ttsObject.setSpeechRate(2.25F);
                ttsObject.speak(String.valueOf(target), TextToSpeech.QUEUE_FLUSH, params);
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis() + interval);
        handler.postDelayed(runnable, interval);

    }

    // Accesses the next target within the userData target list and starts speech output
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void continueWithNextTask() {
            if (userData.getCurrentTargetIndex() + 1 < userData.getCurrentTargetList().size()) {
                userData.incrementCurrentTargetIndex();
                userData.addMeasurement(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
                taskCompleted = false;
                readAloudTarget();
            } else {
                soundPool.play(successSound, 0.25F, 0.25F, 1, 0, 1f);
                if (feedbackModes.size() > currentVariant + 1) {
                    initializeNextVariant();
                } else {
                    final String[] userId = userData.getUserId().split("_" + feedbackModes.get(currentVariant));
                    userData.setUserID(userId[0]);
                    finish();
                }
            }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initializeNextVariant() {
        final String[] userId = userData.getUserId().split("_" + feedbackModes.get(currentVariant));
        currentVariant = currentVariant + 1;
        // set userId
        String id = userId[0] + "_" + feedbackModes.get(currentVariant) + "_" + orientations.get(currentVariant);
        userData.setUserID(id);
        userData.createNewUserDataReference(id);
        // reset indices;
        userData.resetCurrentTargetIndex();
        userData.setTargets(userData.createRandomizedTargetList(6)); // TODO decide
        // setup tactile area according to new variant
        tactileArea.changeLayout(feedbackModes.get(currentVariant), orientations.get(currentVariant));

        // reset variables
        tasksStarted = false;
        taskCompleted = false;


    }

    // Setup long click listener for slider dragging interaction
    private View.OnLongClickListener setUpLongClickListener() {
        View.OnLongClickListener handleLongClick = new View.OnLongClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onLongClick(View view) {
                if (!isLongClick && !taskCompleted) {
                    isLongClick = true;
                    startTask = System.currentTimeMillis();
                    // Check for finger position and provide feedback
                    tactileArea.handleTouchEvent(xTouch, yTouch, userData, startTask, tasksStarted);

                }
                return false;
            }
        };
        return handleLongClick;


    }

    // When value has been successfully selected, provide feedback, reset values, and
    // update userData model
    private void handleValueSelection() {

        // Set input value
        soundPool.play(doubleTapSound, 0.15F, 0.15F, 1, 0, 1f);
        taskCompleted = true;
        startTask = 0;
        if (userData.getLastMeasurement().getMeasurementPairs().size() > 0) {
            userData.getLastMeasurement().setInput(userData.getLastMeasurement().getMeasurementPairs().get(userData.getLastMeasurement().getMeasurementPairs().size() - 1).getValue());
            // Set completion time value
            userData.getLastMeasurement().setCompletionTime((long) userData.getLastMeasurement().getMeasurementPairs().get(userData.getLastMeasurement().getMeasurementPairs().size() - 1).getTimestamp());

        } else {
            userData.getLastMeasurement().setInput(-1);
            // Set completion time value
            userData.getLastMeasurement().setCompletionTime((long) -1);

        }
        userData.pushDataToDatabase();

    }

    @Override
    public void onBackPressed() {

    }

}
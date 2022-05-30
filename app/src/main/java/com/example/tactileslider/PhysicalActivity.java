package com.example.tactileslider;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class PhysicalActivity extends AppCompatActivity {
    UserData userData;
    private String feedbackMode;
    private final String AUDIO = "audio";
    private final String HAPTIC = "haptic";
    private Context context;
    private AudioFeedback audioFeedback;
    private int soundIdCompletion;
    private int soundIdDoubleTap;
    private int soundIdLongClick;
    private int soundIdAudioFeedback;
    private TextToSpeech ttsObject;
    private boolean taskStarted = false; // this should be sent to Microcontroller


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide(); //<< this
        super.onCreate(savedInstanceState);
        userData = (UserData) getIntent().getSerializableExtra("userData");
        feedbackMode = (String) getIntent().getExtras().get("feedbackMode");

        setContentView(R.layout.physical_activity);
        this.context = this;

        // Setup audio sounds
        this.audioFeedback = new AudioFeedback();
        this.soundIdCompletion = audioFeedback.getSoundPool().load(this, R.raw.completion_sound, 1);
        this.soundIdDoubleTap = audioFeedback.getSoundPool().load(this, R.raw.doubletap, 1);
        this.soundIdLongClick = audioFeedback.getSoundPool().load(this, R.raw.longclick, 1);
        this.soundIdAudioFeedback = audioFeedback.getSoundPool().load(this, R.raw.piep, 1);

        // Setup tts component
        this.ttsObject =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
        Locale german = new Locale("de", "DE");
        ttsObject.setLanguage(german);

    }

    // Initialize first task
    private void startFirstTask() {
        audioFeedback.getSoundPool().play(soundIdDoubleTap, 0.5F, 0.5F, 1, 0, 1);
        readAloudTarget();
        taskStarted = true; // TODO communicate to arduino
    }

    // Generate speech output to read aloud task
    private void readAloudTarget(){
        int target = (int) Math.round(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
        String toSpeak = "Bitte wählen Sie die " + target + ".";
        final int interval = 2000; // 1 Second
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "0.05");
                ttsObject.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, params);
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }

    // TODO is triggered when slider has been moved back
    // Accesses the next target within the userData target list and starts speech output
    private void continueWithNextTask() {
        if (userData.getCurrentTargetIndex() + 1 < userData.getCurrentTargetList().size()){
            userData.incrementCurrentTargetIndex();
            userData.addMeasurement(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
            taskStarted = true; // TODO communicate to arduino
            audioFeedback.getSoundPool().play(soundIdDoubleTap, 0.5F, 0.5F, 1, 0, 1);
            readAloudTarget();
        } else {
            audioFeedback.getSoundPool().play(soundIdCompletion, 0.5F, 0.5F, 1, 0, 1);
            final String[] userId = userData.getUserId().split("_" + feedbackMode);
            userData.setUserID(userId[0]);
            // TODO: randomize targets again
            taskStarted = false; // TODO communicate to arduino
            finish();
        }

    }

    // TODO is triggered when slider has not been moving for X seconds (microcontroller)
    // When value has been successfully selected, provide feedback, reset values, and
    // update userData model
    private void handleValueSelection(double input, int target, long completiontime, ArrayList<HashMap<Double, Long>> measurementPairs) {
        audioFeedback.getSoundPool().play(soundIdDoubleTap, 1F, 1F, 1, 0, 1);
        taskStarted = false;
        // Set input value
        userData.getLastMeasurement().setInput(input);
        // Set completion time value
        userData.getLastMeasurement().setCompletionTime(completiontime);

        for (HashMap<Double, Long> pair : measurementPairs){
            userData.getLastMeasurement().addMeasurementPair(pair.get("value"), pair.get("timestamp"));
        }
        userData.pushDataToDatabase();
    }

    // TODO convert received data to required data format for userData
    // aka (double input, int target, long completiontime, ArrayList<HashMap<Double, Long>> measurementPairs)
    private void convertDataFromMicrocontroller(){

    }

}

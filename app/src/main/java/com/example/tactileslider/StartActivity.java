package com.example.tactileslider;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StartActivity extends AppCompatActivity {

    private final String AUDIO = "audio";
    private final String TACTILE = "tactile";
    private final String COMBINED = "combined";
    private final String LONG = "long";
    private final String SHORT = "short";
    private final String HORIZONTAL = "horizontal";
    private final String VERTICAL = "vertical";

    private final String TRIAL = "trial";
    private final String STUDY = "study";
    private final int STUDY_REPETITIONS = 10; // TODO: set to 10
    private final String QUEST = "questionnaire";

    private AppCompatButton downloadButton;
    private EditText idText;
    private Button confirmIdButton;

    private UserData userData;

    private JsonFormatter jsonFormatter;

    private AudioFeedback audioFeedback;
    private int soundIdDoubleTap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Setup activity components
        this.downloadButton = findViewById(R.id.buttonDownload);
        this.idText = findViewById(R.id.idText);
        this.jsonFormatter = new JsonFormatter(this);
        
        // Audio Feedback 
        this.audioFeedback = new AudioFeedback();
        this.soundIdDoubleTap = audioFeedback.getSoundPool().load(this, R.raw.doubletap, 1);

        initializeUserDataObject();

        // Setup event listeners
        findViewById(R.id.trial_audio_long_horizontal).setOnClickListener(view -> switchToSliderTrial(findViewById(R.id.trial_audio_long_horizontal), AUDIO, LONG, HORIZONTAL));
        findViewById(R.id.trial_audio_long_vertical).setOnClickListener(view -> switchToSliderTrial(findViewById(R.id.trial_audio_long_vertical), AUDIO, LONG, VERTICAL));
        findViewById(R.id.trial_audio_short_horizontal).setOnClickListener(view -> switchToSliderTrial(findViewById(R.id.trial_audio_short_horizontal), AUDIO, SHORT, HORIZONTAL));
        findViewById(R.id.trial_audio_short_vertical).setOnClickListener(view -> switchToSliderTrial(findViewById(R.id.trial_audio_short_vertical), AUDIO, SHORT, VERTICAL));

        findViewById(R.id.trial_tactile_long_horizontal).setOnClickListener(view -> switchToSliderTrial(findViewById(R.id.trial_tactile_long_horizontal), TACTILE, LONG, HORIZONTAL));
        findViewById(R.id.trial_tactile_long_vertical).setOnClickListener(view -> switchToSliderTrial(findViewById(R.id.trial_tactile_long_vertical), TACTILE, LONG, VERTICAL));
        findViewById(R.id.trial_tactile_short_horizontal).setOnClickListener(view -> switchToSliderTrial(findViewById(R.id.trial_tactile_short_horizontal), TACTILE, SHORT, HORIZONTAL));
        findViewById(R.id.trial_tactile_short_vertical).setOnClickListener(view -> switchToSliderTrial(findViewById(R.id.trial_tactile_short_vertical), TACTILE, SHORT, VERTICAL));

        findViewById(R.id.trial_combined_long_horizontal).setOnClickListener(view -> switchToSliderTrial(findViewById(R.id.trial_combined_long_horizontal), COMBINED, LONG, HORIZONTAL));
        findViewById(R.id.trial_combined_long_vertical).setOnClickListener(view -> switchToSliderTrial(findViewById(R.id.trial_combined_long_vertical), COMBINED, LONG, VERTICAL));
        findViewById(R.id.trial_combined_short_horizontal).setOnClickListener(view -> switchToSliderTrial(findViewById(R.id.trial_combined_short_horizontal), COMBINED, SHORT, HORIZONTAL));
        findViewById(R.id.trial_combined_short_vertical).setOnClickListener(view -> switchToSliderTrial(findViewById(R.id.trial_combined_short_vertical), COMBINED, SHORT, VERTICAL));

        findViewById(R.id.changeId).setOnClickListener(view -> changeId());
        findViewById(R.id.startQuestionnaires).setOnClickListener(view -> startQuestionnairePhase());
        findViewById(R.id.startStudy).setOnClickListener(view -> startStudyPhase());

        downloadButton.setOnClickListener(view -> jsonFormatter.downloadUserTestingData());

        findViewById(R.id.trialTable).setVisibility(View.VISIBLE);
        findViewById(R.id.changeId).setVisibility(View.VISIBLE);
    }

    private void changeId() {
        String newId = String.valueOf(idText.getText());
        userData.setUserID(newId);
        // add collection to firebase
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firebase.collection("participants");
        collectionRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int P_count = task.getResult().size();
                            String lastId = "P_" + (P_count);
                            // add collection to firebase
                            FirebaseFirestore firebase = FirebaseFirestore.getInstance();
                            // add userID to firebase collectionList collection
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", newId);
                            firebase.collection("participants").document(lastId).delete();
                            firebase.collection("participants").document(newId).set(data);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private Intent createIntent(String phase){
        ArrayList<String> variants = getSelectedVariants();
        Intent intent;
        intent = new Intent(this, SliderAreaActivity.class);

        intent.putExtra("feedbackMode_1", variants.get(0).split(" ")[0]);
        intent.putExtra("length_1", variants.get(0).split(" ")[1]);
        intent.putExtra("orientation_1", variants.get(0).split(" ")[2]);

        intent.putExtra("feedbackMode_2", variants.get(1).split(" ")[0]);
        intent.putExtra("length_2", variants.get(1).split(" ")[1]);
        intent.putExtra("orientation_2", variants.get(1).split(" ")[2]);

        if (variants.size() == 3){
            intent.putExtra("feedbackMode_3", variants.get(2).split(" ")[0]);
            intent.putExtra("length_3", variants.get(2).split(" ")[1]);
            intent.putExtra("orientation_3", variants.get(2).split(" ")[2]);
        }

        intent.putExtra("phase", phase);
        intent.putExtra("userData", userData);
        return intent;
    }

    // Initialize questionnaire phase
    private void startQuestionnairePhase() {
        Intent intent = createIntent(QUEST);
        startActivity(intent);
    }

    // Initialize study phase
    private void startStudyPhase() {
        Intent intent = createIntent(STUDY);
        startActivity(intent);
    }

    // Determine which variants have been selected
    private ArrayList<String> getSelectedVariants(){
        ArrayList<String> variants = new ArrayList<>();
        TableLayout table = (TableLayout) findViewById(R.id.trialTable);
        int count = table.getChildCount();
        for (int i = 0; i < count; i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            CheckBox first = (CheckBox) row.getChildAt(0);
            CheckBox second = (CheckBox) row.getChildAt(3);
            if (first.isChecked()){
                variants.add((String) first.getTag());
            }
            if (second.isChecked()){
                variants.add((String) second.getTag());
            }
        }
        // Randomize variants
        Collections.shuffle(variants);
  
        return variants;
    }

    // Switch to slider variant of the given parameters for trial phase
    private void switchToSliderTrial(Button button, String mode, String length, String orientation){

        // change color of button
        button.setBackgroundColor(getResources().getColor(R.color.grape_pale));
        audioFeedback.getSoundPool().play(soundIdDoubleTap, 0.5F, 0.5F, 1, 0, 1);

        Intent intent;
        intent = new Intent(this, SliderAreaActivity.class);
        intent.putExtra("userData", userData);
        intent.putExtra("feedbackMode_1", mode);
        intent.putExtra("orientation_1", orientation);
        intent.putExtra("length_1", length);
        intent.putExtra("phase", TRIAL);
        startActivity(intent);
    }

    // Determine the ID of the participant according to already existing IDs
    private void initializeUserDataObject() {
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firebase.collection("participants");
        collectionRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int P_count = task.getResult().size();
                            String newId = "P_" + (P_count+1);
                            HashMap<String, String> participant = new HashMap<String, String>();
                            participant.put("id", newId);
                            idText.setText(newId);
                            int times = STUDY_REPETITIONS;
                            userData = new UserData(newId,times);
                            firebase.collection("participants").document(newId).set(participant);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    };

    }



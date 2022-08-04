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

    private AppCompatButton downloadButton;
    private EditText idText;

    private UserData userData;
    private LatinSquare latinSquare;

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
        this.latinSquare = new LatinSquare();
        
        // Audio Feedback 
        this.audioFeedback = new AudioFeedback();
        this.soundIdDoubleTap = audioFeedback.getSoundPool().load(this, R.raw.doubletap, 1);

        getUserIdFromDb();

        findViewById(R.id.changeId).setOnClickListener(view -> changeId());
        findViewById(R.id.startStudy).setOnClickListener(view -> createIntent());

        downloadButton.setOnClickListener(view -> jsonFormatter.downloadUserTestingData());

        findViewById(R.id.changeId).setVisibility(View.VISIBLE);
    }

    private void changeId() {
        String newId = String.valueOf(idText.getText());
    }

    private void createIntent(){
        initializeUserDataObject(this);
    }
    
    // Determine the ID of the participant according to already existing IDs
    private void initializeUserDataObject(StartActivity startActivity) {
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firebase.collection("participants");
        collectionRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int P_count = task.getResult().size();
                            String newId;
                            if (("P_"+ (P_count+1)).equals((String.valueOf(idText.getText())))){
                                Log.d("IDS", "same id");
                                newId =  "P_" + (P_count+1);
                            } else {
                                Log.d("IDS", "different id");
                                firebase.collection("participants").document("P_" + P_count).delete();
                                newId =  "P_" + (P_count);
                            }
                            HashMap<String, String> participant = new HashMap<String, String>();
                            participant.put("id", newId);
                            idText.setText(newId);
                            int times =  StudySettings.STUDY_REPETITIONS;
                            userData = new UserData(newId,times);
                            firebase.collection("participants").document(newId).set(participant);

                            // Start intent
                            ArrayList<String> variants = latinSquare.getVariantOrder(userData.getUserId());
                            Log.d("latinsquare", String.valueOf(variants));
                            Intent intent;
                            intent = new Intent(startActivity, SliderAreaActivity.class);
                            for (int i = 0; i < variants.size(); i ++){
                                int tagNum = i + 1;
                                intent.putExtra("feedbackMode_" + tagNum, variants.get(i).split("_")[0]);
                                intent.putExtra("orientation_" + tagNum, variants.get(i).split("_")[1]);
                            }
                            intent.putExtra("userData", userData);
                            startActivity(intent);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    };

    private void getUserIdFromDb(){
            FirebaseFirestore firebase = FirebaseFirestore.getInstance();
            CollectionReference collectionRef = firebase.collection("participants");
            collectionRef
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                int P_count = task.getResult().size();
                                String newId =  "P_" + (P_count+1);
                                idText.setText(newId);

                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        };

    }


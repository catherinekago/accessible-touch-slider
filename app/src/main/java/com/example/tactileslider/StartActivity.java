package com.example.tactileslider;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StartActivity extends AppCompatActivity {

    private final String AUDIO = "audio";
    private final String HAPTIC = "haptic";
    private final String APP = "app";
    private final String PHYSICAL = "physical";

    private Button audioAppButton;
    private Button hapticAppButton;
    private AppCompatButton downloadButton;
    private EditText idInput;
    private TextView idText;
    private Button confirmIdButton;

    private UserData userData;
    private ArrayList<String> userList;
    private ArrayList<CollectionReference> userDataReferences;
    private ArrayList<JSONObject> userDataJsonList;

    private int userDataSetCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Setup activity components
        this.audioAppButton = findViewById(R.id.buttonAudio);
        this.hapticAppButton = findViewById(R.id.buttonHaptic);
        this.downloadButton = findViewById(R.id.buttonDownload);
        this.idInput = findViewById(R.id.idInput);
        this.idText = findViewById(R.id.idText);
        this.confirmIdButton = findViewById(R.id.confirmId);

        // Setup event listeners
        audioAppButton.setOnClickListener(view -> switchToSliderActivity(AUDIO, APP));
        hapticAppButton.setOnClickListener(view -> switchToSliderActivity(HAPTIC, APP));
        confirmIdButton.setOnClickListener(view -> setId(this.idInput.getText().toString()));
        downloadButton.setOnClickListener(view -> downloadUserTestingData());

        audioAppButton.setVisibility(View.INVISIBLE);
        hapticAppButton.setVisibility(View.INVISIBLE);

        // how many user data sets will be generated?
        // repetitions of tasks * number of tasks * number of tests * number of users
        // 1 * 7 * 4 * userList.size()
        // TODO: set to * 4 instead of * 2
        userDataSetCount =  1 * 7 * 2 * 1;

    }

    // Exchange UI elements for ID selection with Mode selection
    private void enableModeSelection() {
        audioAppButton.setVisibility(View.VISIBLE);
        hapticAppButton.setVisibility(View.VISIBLE);
    }

    // Switch to slider with feedback mode according to selection
    private void switchToSliderActivity(String mode, String type){
        userData.setUserID(userData.getUserId() + "_" + mode + "_" + type);
        Intent intent = new Intent(this, SliderAreaActivity.class);
        intent.putExtra("feedbackMode", mode);
        intent.putExtra("userData", userData);
        startActivity(intent);
    }

    // Set the userId within the interface as well as for the userID
    private void setId(String id){
        idText.setText("ID: " + id);
        idInput.setVisibility(View.INVISIBLE);
        confirmIdButton.setVisibility(View.INVISIBLE);
        enableModeSelection();
        initializeUserData(id);

    }

    private void initializeUserData(String id) {
        int times = 1;
        userData = new UserData(id,times);
        // add collection to firebase
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firebase.collection(id);
        // add userID to firebase collectionList collection
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", id);
        firebase.collection("userDataCollectionNames").document(id).set(userData);

    }

    // Get data from firebase and download each set as json
    private void downloadUserTestingData(){
        getUserList();
    }

    private void getUserList() {
        userList = new ArrayList<>();
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firebase.collection("userDataCollectionNames");
        collectionRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentUser : task.getResult()) {
                                userList.add(documentUser.getId());
                            }
                            // todo nextMethod
                            createCollectionReferences();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        }
                        });
    }

    private void createCollectionReferences() {
        userDataReferences = new ArrayList<>();
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        for (String userId : userList){
            userDataReferences.add(firebase.collection(userId + "_audio_app"));
            userDataReferences.add(firebase.collection(userId + "_haptic_app"));
            // todo enable this
            //userDataReferences.add(firebase.collection(userId + "_audio_physical"));
            //userDataReferences.add(firebase.collection(userId + "_haptic_physical"));
        }
        createJsonsFromUserData();
    }

    private void createJsonsFromUserData() {
        userDataJsonList = new ArrayList<>();
        for (CollectionReference ref : userDataReferences){
            ref.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                int i = 1;
                                for (QueryDocumentSnapshot data : task.getResult()) {
                                    JSONObject json = new JSONObject();
                                    try {
                                        // For every user testing data, add user id, feedback and prototype type to JSON object
                                        String completeUserName = data.getReference().getParent().getId();
                                        String userId = completeUserName.split("_")[0];
                                        String feedback = completeUserName.split("_")[1];
                                        String prototype = completeUserName.split("_")[2];
                                        json.put("userId", userId).put("feedback", feedback).put("prototype", prototype);
                                        json.put("target", data.get("target")).put("input", data.get("input")).put("error", data.get("error")).put("completiontime", data.get("completionTime"));
                                        json.put("measurementPairs", createJsonFromMeasurementPairs((ArrayList<HashMap>) data.get("measurementPairs")));
                                        userDataJsonList.add(json);
                                                i++;

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (userDataJsonList.size() == userDataSetCount * userList.size()){
                                    try {
                                        storeJsonsInLocalStorage(groupJsonsByUserId());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }

    private ArrayList<JSONObject> groupJsonsByUserId() throws JSONException {
        ArrayList<String> identifiedUsers = new ArrayList<>();
        ArrayList<JSONObject> dataByUsers = new ArrayList<>();
        JSONObject groupedJSON = new JSONObject();
        JSONObject userJSON = new JSONObject();
        int i = 1;
        for (JSONObject data : userDataJsonList){
            if (identifiedUsers.contains(data.get("userId"))){
                userJSON.put("task_" + i, data);
                i++;
            } else {
                if (userJSON.length() > 0){
                    groupedJSON.put("user_" + identifiedUsers.size(), userJSON);
                    dataByUsers.add(groupedJSON);
                }
                identifiedUsers.add((String) data.get("userId"));
                i = 1;
                userJSON = new JSONObject();
                userJSON.put("task_" + i, data);
                i ++;
            }
        }
        groupedJSON.put("user_" + identifiedUsers.size(), userJSON);
        dataByUsers.add(groupedJSON);
        return dataByUsers;
    }

    private JSONObject createJsonFromMeasurementPairs(ArrayList<HashMap> measurementPairs) throws JSONException {
        JSONObject pairs = new JSONObject();
        int i = 1;
        for (HashMap measurementPair : measurementPairs){
            JSONObject pair = new JSONObject();
            pair.put("value", measurementPair.get("value"));
            pair.put("timestamp", measurementPair.get("timestamp"));
            pairs.put("pair_" +i, pair);
            i++;
        }
        return pairs;
    }

    private void storeJsonsInLocalStorage(ArrayList<JSONObject> groupedJsonList) {
        File dir = new File(this.getFilesDir(), "TactileSlider_userData");
        if (!dir.exists()) {
            dir.mkdir();
        }
        int i = 0;
        for (JSONObject json : groupedJsonList) {
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(userList.get(i) + ".json", Context.MODE_PRIVATE));
                outputStreamWriter.write(String.valueOf(json));
                outputStreamWriter.close();
                Log.i("SUCCESS", "TactileSlider_userData " + dir + " " + this.getFilesDir());
                i++;
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }

        }
    }
    }



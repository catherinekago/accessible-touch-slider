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
    private Button audioPhysicalButton;
    private Button hapticPhysicalButton;
    private AppCompatButton downloadButton;
    private EditText idInput;
    private TextView idText;
    private Button confirmIdButton;

    private UserData userData;
    private ArrayList<String> userList;
    private ArrayList<CollectionReference> userDataReferences;
    private ArrayList<JSONObject> userDataJsonList;

    private int userDataSetCount;
    private View bluetoothConnectionText;

    private JsonFormatter jsonFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Setup activity components
        this.audioAppButton = findViewById(R.id.buttonAudioApp);
        this.hapticAppButton = findViewById(R.id.buttonHapticApp);
        this.audioPhysicalButton = findViewById(R.id.buttonAudioPhysical);
        this.hapticPhysicalButton = findViewById(R.id.buttonHapticPhysical);
        this.downloadButton = findViewById(R.id.buttonDownload);
        this.idInput = findViewById(R.id.idInput);
        this.idText = findViewById(R.id.idText);
        this.bluetoothConnectionText = findViewById(R.id.bluetoothConnection);
        this.confirmIdButton = findViewById(R.id.confirmId);
        this. jsonFormatter = new JsonFormatter(this);

        // Setup event listeners
        audioAppButton.setOnClickListener(view -> switchToSliderActivity(AUDIO, APP));
        hapticAppButton.setOnClickListener(view -> switchToSliderActivity(HAPTIC, APP));
        confirmIdButton.setOnClickListener(view -> setId(this.idInput.getText().toString()));
        downloadButton.setOnClickListener(view -> jsonFormatter.downloadUserTestingData());
        audioPhysicalButton.setOnClickListener(view -> switchToSliderActivity(AUDIO, PHYSICAL));
        hapticPhysicalButton.setOnClickListener(view -> switchToSliderActivity(HAPTIC, PHYSICAL));

        audioAppButton.setVisibility(View.INVISIBLE);
        hapticAppButton.setVisibility(View.INVISIBLE);
        audioPhysicalButton.setVisibility(View.INVISIBLE);
        hapticPhysicalButton.setVisibility(View.INVISIBLE);

        // how many user data sets will be generated?
        // repetitions of tasks * number of tasks * number of tests * number of users
        // 1 * 7 * 4 * userList.size()
        // TODO: set to * 4 instead of * 2
        userDataSetCount =  1 * 7 * 2 * 1;

        // TODO setup bluetooth connection and set bluetoothConnectionText to "verbunden" in green if successful
        // TODO add listener to bluetooth connection and show toast if lost and sound?

    }

    // Exchange UI elements for ID selection with Mode selection
    private void enableModeSelection() {
        audioAppButton.setVisibility(View.VISIBLE);
        hapticAppButton.setVisibility(View.VISIBLE);
        audioPhysicalButton.setVisibility(View.VISIBLE);
        hapticPhysicalButton.setVisibility(View.VISIBLE);
    }

    // Switch to slider with feedback mode according to selection
    private void switchToSliderActivity(String mode, String type){
        userData.setUserID(userData.getUserId() + "_" + mode + "_" + type);
        Intent intent;
        if (mode.equals(APP)){
            intent = new Intent(this, SliderAreaActivity.class);
        } else {
            intent = new Intent(this, PhysicalActivity.class);
        }
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
    }



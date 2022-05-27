package com.example.tactileslider;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class StartActivity extends AppCompatActivity {

    private final String AUDIO = "audio";
    private final String HAPTIC = "haptic";

    private Button audioModeButton;
    private Button hapticModeButton;
    private AppCompatButton downloadButton;
    private EditText idInput;
    private TextView idText;
    private Button confirmIdButton;

    private UserData userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Setup activity components
        this.audioModeButton = findViewById(R.id.buttonAudio);
        this.hapticModeButton = findViewById(R.id.buttonHaptic);
        this.downloadButton = findViewById(R.id.buttonDownload);
        this.idInput = findViewById(R.id.idInput);
        this.idText = findViewById(R.id.idText);
        this.confirmIdButton = findViewById(R.id.confirmId);

        // Setup event listeners
        audioModeButton.setOnClickListener(view -> switchToSliderActivity(AUDIO));
        hapticModeButton.setOnClickListener(view -> switchToSliderActivity(HAPTIC));
        confirmIdButton.setOnClickListener(view -> setId(this.idInput.getText().toString()));

        audioModeButton.setVisibility(View.INVISIBLE);
        hapticModeButton.setVisibility(View.INVISIBLE);

    }

    // Exchange UI elements for ID selection with Mode selection
    private void enableModeSelection() {
        audioModeButton.setVisibility(View.VISIBLE);
        hapticModeButton.setVisibility(View.VISIBLE);
    }

    // Switch to slider with feedback mode according to selection
    private void switchToSliderActivity(String mode){
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
        userData = new UserData(id,1);
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
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firebase.collection("userDataCollectionNames");
        collectionRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentUser : task.getResult()) {
                                Log.d(TAG, documentUser.getId() + " => " + documentUser.getData());
                                // TODO: get audio and haptic documents of user
                                CollectionReference docRef = firebase.collection(documentUser.getId());
                                // TODO: for each document, get the documents and add to json; https://stackoverflow.com/questions/50633157/get-json-object-from-google-cloud-firestore
                                // TODO: download json per document
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    // Source: https://stackoverflow.com/questions/44587187/android-how-to-write-a-file-to-internal-storage
    public void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody){
        File dir = new File(mcoContext.getFilesDir(), "mydir");
        if(!dir.exists()){
            dir.mkdir();
        }

        try {
            File gpxfile = new File(dir, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody); // TODO: try with json object
            writer.flush();
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
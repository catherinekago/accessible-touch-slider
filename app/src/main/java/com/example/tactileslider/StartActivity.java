package com.example.tactileslider;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.PendingIntent;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
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
    private TextView idText;
    private Button confirmIdButton;

    // TODO: randomize latin square function balance factors (matlab)

    // TODO: vibration: Kopfhörer auf, Ton aus?

    // TODO: NO DUAL TASK - it is an accessibility feature
    // TODO: in 2 Wochen paar seeeingeschränkte fragen, was sie von slidern halten
    // TODO: use case definieren für menschen mit Seeinschränkungen - wo nutzen sie slider? (Fragebogen)
    // TODO: sensitive range error rausrechnen (if error kleiner range, setze error 0)

    // TODO: mit Mitbewohnern Studie testen um fehler aufzudecken
    // TODO: überlegen wie könnte man das Design variieren, und warum? Länge, mit / ohne Schablone für beide Längen (lwo cost touchplate)

    // TODO: wie oft wiederholen? Abhängig von der Anzahl der Varianten

    // --> wie grenzen wir das von Verena ab? Quantitativ + mehr Design Varianten

    // Markus:
    // NASA-TLX oder SUS nach JEDER getesteten VARIANTE, aber nicht mehr certainty nach jedem item
    // such dir einen favorisierte Variante aus (haptisch/auditiv, "länge", ...) und beantworte einen Fragebogen "wie sehr mögen Sie Katzen"

    private UserData userData;

    private JsonFormatter jsonFormatter;

    //UsbDevice device;
    //UsbDeviceConnection usbConnection;
    //UsbSerialDevice serial = UsbSerialDevice.createUsbSerialDevice(device, usbConnection);

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
        this.idText = findViewById(R.id.idText);
        this.confirmIdButton = findViewById(R.id.confirmId);
        this. jsonFormatter = new JsonFormatter(this);
        // Set id to next id in line
        createId();

        // Setup event listeners
        audioAppButton.setOnClickListener(view -> switchToSliderActivity(AUDIO, APP));
        hapticAppButton.setOnClickListener(view -> switchToSliderActivity(HAPTIC, APP));
        confirmIdButton.setOnClickListener(view -> startTrials());
        downloadButton.setOnClickListener(view -> jsonFormatter.downloadUserTestingData());
        audioPhysicalButton.setOnClickListener(view -> switchToSliderActivity(AUDIO, PHYSICAL));
        hapticPhysicalButton.setOnClickListener(view -> switchToSliderActivity(HAPTIC, PHYSICAL));

        audioAppButton.setVisibility(View.INVISIBLE);
        hapticAppButton.setVisibility(View.INVISIBLE);
        audioPhysicalButton.setVisibility(View.INVISIBLE);
        hapticPhysicalButton.setVisibility(View.INVISIBLE);

        // Setup USB device connection
        //serial.open();
        //serial.setBaudRate(115200);
        //serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
        //serial.setParity(UsbSerialInterface.PARITY_ODD);
        //serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
        //serial.read(mCallback);

        // serial.write("DATA".getBytes()); // Async-like operation now! :)
    }

    private void startTrials() {
        initializeUserData(idText.getText().toString());
        // TODO: replace these with automized start of trials
        confirmIdButton.setVisibility(View.INVISIBLE);
        enableModeSelection();
    }

    private void createId() {
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firebase.collection("userDataCollectionNames");
        collectionRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int P_count = task.getResult().size();
                            String newId = "P_" + (P_count+1);
                            idText.setText(newId);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    };



    // Define a simple callback for transactions from connected device
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {

        @Override
        public void onReceivedData(byte[] arg0)
        {
            // Code here :)
        }

    };

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
        intent = new Intent(this, SliderAreaActivity.class);
        intent.putExtra("feedbackMode", mode);
        intent.putExtra("userData", userData);
        startActivity(intent);
    }

    private void initializeUserData(String id) {
        // TODO: based on id, determine latin square variant to be used and generate plan
        int times = 1;
        userData = new UserData(id,times);
        // add collection to firebase
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        // add userID to firebase collectionList collection
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", id);
        firebase.collection("userDataCollectionNames").document(id).set(userData);

    }

    }



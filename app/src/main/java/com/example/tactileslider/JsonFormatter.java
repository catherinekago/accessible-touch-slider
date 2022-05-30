package com.example.tactileslider;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class JsonFormatter {
    private final Context context;
    private UserData userData;
    private ArrayList<String> userList;
    private ArrayList<CollectionReference> userDataReferences;
    private ArrayList<JSONObject> userDataJsonList;
    private int userDataSetCount;
    
    public JsonFormatter(Context context){
        this.context = context;
        
    }
    // Get data from firebase and download each set as json
    public void downloadUserTestingData(){
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
        File dir = new File(context.getFilesDir(), "TactileSlider_userData");
        if (!dir.exists()) {
            dir.mkdir();
        }
        int i = 0;
        for (JSONObject json : groupedJsonList) {
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(userList.get(i) + ".json", Context.MODE_PRIVATE));
                outputStreamWriter.write(String.valueOf(json));
                outputStreamWriter.close();
                Log.i("SUCCESS", "TactileSlider_userData " + dir + " " + context.getFilesDir());
                i++;
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }

        }
    }
}

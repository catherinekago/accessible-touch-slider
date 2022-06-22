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
    private ArrayList<String> dataList;
    private ArrayList<String> retrievedDataList;
    private ArrayList<CollectionReference> userDataReferences;
    private ArrayList<JSONObject> userDataJsonList;
    private int dataSetCount;

    private final int STUDY_VARIANTS = 3; // TODO: could change
    private final int QUEST_VARIANTS = 2;
    private final int STUDY_REPETITIONS = 1; // TODO: 3, == times
    private final int STUDY_TASKS = 3; // TODO: 7
    private final int QUEST_REPETITIONS = 1;
    private final int QUEST_TASKS = 3;  // TODO: define!
    private ArrayList<String> participants;


    public JsonFormatter(Context context){
        this.context = context;
    }
    // Get data from firebase and download each set as json
    public void downloadUserTestingData(){
        // get number of participants
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firebase.collection("participants");
        collectionRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                               if (task.isSuccessful()) {
                                                   participants = new ArrayList<>();
                                                   for (QueryDocumentSnapshot data : task.getResult()) {
                                                        participants.add(data.getId());
                                                   }
                                                   dataSetCount = participants.size() * ((STUDY_VARIANTS * STUDY_TASKS * STUDY_REPETITIONS) + (QUEST_VARIANTS * QUEST_REPETITIONS * QUEST_TASKS));
                                                   getDataList();
                                               }
                                           }
                                       });


    }

    private void getDataList() {
        dataList = new ArrayList<>();
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firebase.collection("userDataCollectionNames");
        collectionRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentUser : task.getResult()) {
                                dataList.add(documentUser.getId());
                            }
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
        for (String user : dataList){
                                userDataReferences.add(firebase.collection(user));
                };
        createJsonsFromUserData();
    }
    

    private void createJsonsFromUserData() {
        userDataJsonList = new ArrayList<>();
        retrievedDataList = new ArrayList<>();
        for (CollectionReference ref : userDataReferences){
            ref.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot data : task.getResult()) {
                                        retrievedDataList.add(data.getReference().getParent().getId());
                                        JSONObject json = new JSONObject();
                                        try {
                                            // For every user testing data, add user id, feedback and prototype type to JSON object
                                            String completeUserName = data.getReference().getParent().getId();
                                            String userId = completeUserName.split("_")[0] + "_" + completeUserName.split("_")[1];
                                            String feedback = completeUserName.split("_")[2];
                                            String prototype = completeUserName.split("_")[3];
                                            String orientation = completeUserName.split("_")[4];
                                            String phase = completeUserName.split("_")[5];
                                            json.put("userId", userId).put("feedback", feedback).put("prototype", prototype).put("orientation", orientation).put("phase", phase);
                                            if (phase.equals("study")){
                                                json.put("target", data.get("target")).put("input", data.get("input")).put("error", data.get("error")).put("completiontime", data.get("completionTime"));
                                            } else if (phase.equals("questionnaire")){
                                                json.put("question", data.get("question")).put("input", data.get("input")).put("completiontime", data.get("completionTime"));
                                            }
                                            json.put("measurementPairs", createJsonFromMeasurementPairs((ArrayList<HashMap>) data.get("measurementPairs")));
                                            userDataJsonList.add(json);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    if (userDataJsonList.size() == dataSetCount){
                                        try {
                                            storeJsonsInLocalStorage(groupJsonsByUserId());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
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
                    //groupedJSON.put("user_" + identifiedUsers.size(), userJSON);
                    //dataByUsers.add(groupedJSON);
                    dataByUsers.add(userJSON);
                }
                identifiedUsers.add((String) data.get("userId"));
                i = 1;
                userJSON = new JSONObject();
                userJSON.put("task_" + i, data);
                i ++;
            }
        }
        //groupedJSON.put("user_" + identifiedUsers.size(), userJSON);
        //dataByUsers.add(groupedJSON);
        dataByUsers.add(userJSON);
        return dataByUsers;
    }

    private JSONObject createJsonFromMeasurementPairs(ArrayList<HashMap> measurementPairs) throws JSONException {
        JSONObject pairs = new JSONObject();
        int i = 1;
        for (HashMap measurementPair : measurementPairs){
            JSONObject pair = new JSONObject();
            pair.put("value", measurementPair.get("value"));
            pair.put("xCoord", measurementPair.get("xCoord"));
            pair.put("timestamp", measurementPair.get("timestamp"));
            pairs.put("pair_" +i, pair);
            i++;
        }
        return pairs;
    }

    // data/data/com.example.tactileslider/files
    private void storeJsonsInLocalStorage(ArrayList<JSONObject> jsonList) {
        File dir = new File(context.getFilesDir(), "TactileSlider_userData");
        if (!dir.exists()) {
            dir.mkdir();
        }
        int i = 0;
        for (JSONObject json : jsonList) {
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(participants.get(i) + ".json", Context.MODE_PRIVATE));
                outputStreamWriter.write(String.valueOf(json));
                outputStreamWriter.close();
                Log.i("SUCCESS", "TactileSlider_userData " + dir + " " + participants.get(i));
                i++;
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }

        }
    }
}

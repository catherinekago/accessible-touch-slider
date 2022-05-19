package com.example.tactileslider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TactileSlider tactileSlider;
    UserData userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setup data mask for userID entry before experiment (move LinearLayout and Slider
        // TODO: to "onUserIDsumbitted"
        userData = new UserData("TestID", 1);
        setContentView(R.layout.activity_main);
        LinearLayout sliderLin = (LinearLayout) findViewById(R.id.lin1);
        tactileSlider = new TactileSlider(this, 100, 10, userData);
        tactileSlider.addTactileSlider(sliderLin);


    }

}
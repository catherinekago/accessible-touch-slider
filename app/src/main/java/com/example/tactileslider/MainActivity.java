package com.example.tactileslider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout sliderLin = (LinearLayout) findViewById(R.id.lin1);
        TactileSlider tactileSlider = new TactileSlider(this, 10, Color.BLUE);
        tactileSlider.addTactileSlider(sliderLin);
    }

}
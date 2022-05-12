package com.example.tactileslider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TactileSlider tactileSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout sliderLin = (LinearLayout) findViewById(R.id.lin1);
        tactileSlider = new TactileSlider(this, 100, 10);
        tactileSlider.addTactileSlider(sliderLin);

    }

}
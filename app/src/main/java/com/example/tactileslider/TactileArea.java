package com.example.tactileslider;

import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class TactileArea {

    View sliderView;
    TextView coorinatesView;
    private final String COORD_PREFIX ="Touch Coordinates:   ";
    private int heightTactileArea;
    private int heightTopBar;
    private ArrayList<Integer> likertYCoords = new ArrayList<Integer>();
    private final int LIKERT_PADDING = 5;
    private ArrayList<LikertItem> likertItems = new ArrayList<LikertItem>();
    private double viewTopBorder;
    private double viewBottomBorder;
    private int xTouch;
    private int yTouch;

    private MainActivity mainActivity;


    public TactileArea(MainActivity mainActivity, UserData userData) {

        this.mainActivity = mainActivity;
        sliderView = mainActivity.findViewById(R.id.sliderView);
        coorinatesView = mainActivity.findViewById(R.id.topBar);
        setUpLayoutDrawnListener();

    }

    // Set up observer to determine when layout is drawn to retrieve height of SliderArea
    private void setUpLayoutDrawnListener() {
        ViewTreeObserver vto = sliderView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    sliderView.getViewTreeObserver()
                            .removeOnGlobalLayoutListener(this);
                } else {
                    sliderView.getViewTreeObserver()
                            .removeGlobalOnLayoutListener(this);
                }
                setUpLayout();


            }
        });
    }

    // Set up the layout with all relevant heights and likert subareas.
    private void setUpLayout() {
        heightTactileArea = sliderView.getMeasuredHeight();
        coorinatesView.setText("Height of tactile area: " + heightTactileArea);
        heightTopBar = coorinatesView.getMeasuredHeight();
        likertYCoords = calculateLikertYCords(heightTopBar, heightTactileArea);
        int alphaStep = 255 / 7;
        for (int i = 0; i < likertYCoords.size(); i++){
            // sliderView.getBackground().setAlpha(between 0 and 255);
            int alphaValue = 255 - alphaStep*i;
            likertItems.add(new LikertItem(likertYCoords.get(i), alphaValue));
        }

    }


    /**
     * @param heightTopBar The height of the topbar to be added to calculation of yCoord
     * @param heightTactileArea The height of the actual tactile area where the yCoord positions are determined
     * @return an arraylist of all y coordinates of the seven Likert scale item positions
     */
    private ArrayList<Integer> calculateLikertYCords(double heightTopBar, double heightTactileArea) {
        int spacingTactileArea = (int) (heightTactileArea / 6);
        ArrayList<Integer> likertYCoords = new ArrayList<Integer>();
        for (int i = 0; i < 7; i++){
            likertYCoords.add((int) heightTopBar + (i*spacingTactileArea));
        }
        return likertYCoords;
    }

    ;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void handleTouchEvent(int xTouch, int yTouch){
        setxTouch(xTouch);
        setyTouch(yTouch);
        if (yTouch < heightTopBar || yTouch > heightTopBar + heightTactileArea){
            coorinatesView.setText(COORD_PREFIX +  "out of bounds");
        } else {
            int[] touchRange = IntStream.rangeClosed(yTouch-LIKERT_PADDING, yTouch+LIKERT_PADDING).toArray();
            for (int value: touchRange){
                if (likertYCoords.contains(value)){
                    int index = likertYCoords.indexOf(value);
                    LikertItem crossedItem = likertItems.get(index);
                    sliderView.getBackground().setAlpha(crossedItem.getAlphaValue());
                }
            }
            coorinatesView.setText(COORD_PREFIX + "(" + xTouch + ", " + yTouch + ")");
        }
    }


    public void setxTouch(int xTouch) {
        this.xTouch = xTouch;
    }

    public void setyTouch(int yTouch) {
        this.yTouch = yTouch;
    }
}

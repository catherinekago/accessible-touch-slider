package com.example.tactileslider;

// Source: https://www.coderzheaven.com/2016/08/09/custom-seekbar-in-android-with-labels-in-bottom/

import static android.content.Context.VIBRATOR_SERVICE;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TactileSlider {

    int steps, maxCountLabel, textColor;
        Double currentCount;
        Context mContext;
        LinearLayout mSeekLin;
        SeekBar tactileSlider;
        TextView currentPairs, currentTargetTextView, resultTextView;

        private final int PADDING_HORIZONTAL = 80;
        private final String CURRENT_PAIRS_PREFIX = "Current Pairs (value, time[ms]): ";
        private final String CURRENT_TARGET_PREFIX = "Current Target: ";
        private final String RESULT_PREFIX = "Last measurement ";
        private int PRIMARY_DARK;
        private int PRIMARY_LIGHT;

        // Tactile Feedback
        //Vibration: requires context to work
        Vibrator vibrator;
        final long[] PATTERN_TICK = {0, 100};
        final long[] PATTERN_ENDPOINT = {0, 300};

        // Slider time measurement
        // TODO: how to setup a timer object;
        private UserData userData;
        private long currentStartTime;

        public TactileSlider(Context context, int steps, int maxCountLabel, UserData userData) {
            this.mContext = context;
            this.steps = steps + 1;
            this.maxCountLabel = maxCountLabel;
            this.currentCount = 0.0;
            PRIMARY_DARK = mContext.getResources().getColor(R.color.grape_dark);
            PRIMARY_LIGHT = mContext.getResources().getColor(R.color.grape_light);
            this.textColor = PRIMARY_LIGHT;
            vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
            this.userData = userData;
            this.currentTargetTextView = new TextView(mContext);
            this.resultTextView = new TextView(mContext);
            this.currentPairs = new TextView(mContext);
        }

        Double getCurrentCount(){
            return this.currentCount;
        }

        public void addTactileSlider(LinearLayout parent) {

            if (parent instanceof LinearLayout) {

                setUpTactileSlider(parent);

                // Add LinearLayout for labels below SeekBar
                mSeekLin = new LinearLayout(mContext);
                mSeekLin.setOrientation(LinearLayout.HORIZONTAL);
                mSeekLin.setPadding(PADDING_HORIZONTAL-40, 0, PADDING_HORIZONTAL-54, 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(35, 10, 35, 0);
                params.width = 1150;
                mSeekLin.setLayoutParams(params);

                addTextView(parent, currentTargetTextView, CURRENT_TARGET_PREFIX + String.valueOf(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex())));
                ArrayList<String> pairsAsStrings = new ArrayList<String>();
                for (MeasurementPair pair : userData.getLastMeasurement().getMeasurementPairs()){
                    pairsAsStrings.add(pair.getValue() + " - " + pair.getTimestamp());
                }

                addLabelsBelowSlider();
                parent.addView(tactileSlider);
                parent.addView(mSeekLin);
                addTextView(parent, resultTextView, "Touch slider to start measurement");
                addTextView(parent, currentPairs, CURRENT_PAIRS_PREFIX + pairsAsStrings);


            } else {

                Log.e("TactileSlider", " Parent is not a LinearLayout");
            }
        }

    private void setUpTactileSlider(LinearLayout parent) {
        parent.setOrientation(LinearLayout.VERTICAL);
        tactileSlider = new SeekBar(mContext);
        tactileSlider.setMax(steps - 1);
        tactileSlider.incrementProgressBy(1);
        tactileSlider.setProgress(0);
        tactileSlider.setPadding(PADDING_HORIZONTAL, 0, PADDING_HORIZONTAL, 0);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.width = 1220;
        tactileSlider.setLayoutParams(params);

        setSeekBarProgress(472, 30);
        setSeekBarThumb(80, 80);
        tactileSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Get measured time
                Long completionTime = (long) userData.getLastMeasurement().getMeasurementPairs().get(userData.getLastMeasurement().getMeasurementPairs().size()-1).getTimestamp();
                userData.getLastMeasurement().setCompletionTime(completionTime);
                userData.getLastMeasurement().setInput(currentCount);
                resultTextView.setText(RESULT_PREFIX + "          Target: " + userData.getLastMeasurement().getTarget()+ "         Input: " + userData.getLastMeasurement().getInput() + "         CT: " + userData.getLastMeasurement().getCompletionTime() + "ms");
                // reset currentCount and increment currentTargetIndex
                currentCount = 0.00;
                //Set progress to 0
                tactileSlider.setProgress(0);
                userData.getLastMeasurement().removeLastMeasurementPair();
                currentPairs.setText(CURRENT_PAIRS_PREFIX);
                userData.pushDataToDatabase();
                if (userData.getCurrentTargetIndex() < userData.getCurrentTargetList().size()-1){
                    userData.incrementCurrentTargetIndex();
                    userData.addMeasurement(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
                    currentTargetTextView.setText(CURRENT_TARGET_PREFIX + String.valueOf(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex())));
                    // TODO: voice output of next target
                } else {
                    //TODO: celebration
                    parent.setVisibility(View.INVISIBLE);
                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Start a new Measurement by adding a new Measurement to the userData MeasurementList
                currentStartTime = System.nanoTime();
                resultTextView.setText("... ongoing ...");
                currentPairs.setText(CURRENT_PAIRS_PREFIX + String.valueOf(userData.getLastMeasurement().getMeasurementPairs()));
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                currentCount = new Double(progress) / maxCountLabel;
                if (currentCount % 1 == 0){
                    switch ((int) Math.round(currentCount)){
                        case 0:
                            // This is hardcoded!!!
                        case 10:
                            vibrator.vibrate(PATTERN_ENDPOINT, -1);
                            break;
                        default:
                            vibrator.vibrate(PATTERN_TICK, -1);
                        }
                    }
                // Get current timestamp and add new measurementPair
                long timestamp = System.nanoTime() - currentStartTime;
                timestamp = TimeUnit.NANOSECONDS.toMillis(timestamp);
                userData.getLastMeasurement().addMeasurementPair(currentCount, timestamp);
                ArrayList<String> pairsAsStrings = new ArrayList<String>();
                for (MeasurementPair pair : userData.getLastMeasurement().getMeasurementPairs()){
                    pairsAsStrings.add(pair.getValue() + " - " + pair.getTimestamp());
                }

                currentPairs.setText(CURRENT_PAIRS_PREFIX + pairsAsStrings);
                }

        });
    }


    private void addTextView(LinearLayout parent, TextView textView, String text) {
        textView.setTextSize(20f);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setPadding(PADDING_HORIZONTAL, 80, PADDING_HORIZONTAL, 80);
        textView.setText(text);
        parent.addView(textView);
    }


    private void addLabelsBelowSlider() {
            for (int count = 0; count < maxCountLabel + 1; count++) {
                TextView textView = new TextView(mContext);
                textView.setText(String.valueOf(count));
                textView.setTextColor(textColor);
                textView.setGravity(Gravity.LEFT);
                mSeekLin.addView(textView);
                textView.setLayoutParams((count == maxCountLabel) ? getLayoutParams(0.0f) : getLayoutParams(1.0f));
            }
        }

        // Sourcce: https://localcoder.org/changing-the-size-of-the-seekbar-programmatically-but-cant-get-the-thumb-to-be-l
    private void setSeekBarProgress(int width, int height){
        GradientDrawable shape2 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{mContext.getResources().getColor(R.color.grape_pale), PRIMARY_LIGHT});
        shape2.setCornerRadius(50);
        ClipDrawable clip = new ClipDrawable(shape2, Gravity.LEFT,ClipDrawable.HORIZONTAL);

        GradientDrawable shape1 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{Color.WHITE, Color.rgb(232, 232, 232)});
        shape1.setSize(width, height);
        shape1.setCornerRadius(50);//change the corners of the rectangle
        InsetDrawable d1=  new InsetDrawable(shape1,5,5,5,5);//the padding u want to use

        LayerDrawable myLayer = new LayerDrawable(new Drawable[]{d1,clip});
        tactileSlider.setProgressDrawable(myLayer);

    }

    private void setSeekBarThumb(int width, int height) {
        ShapeDrawable thumb = new ShapeDrawable(new OvalShape());
        thumb.setIntrinsicWidth(width);
        thumb.setIntrinsicHeight(height);
        thumb.getPaint().setColor(PRIMARY_LIGHT);
        tactileSlider.setThumb(thumb);
    }

        LinearLayout.LayoutParams getLayoutParams(float weight) {
            return new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, weight);
        }

    }

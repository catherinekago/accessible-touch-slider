package com.example.tactileslider;

// Source: https://www.coderzheaven.com/2016/08/09/custom-seekbar-in-android-with-labels-in-bottom/

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
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class TactileSlider {

        int steps, maxCountLabel, textColor;
        Double currentCount;
        Context mContext;
        LinearLayout mSeekLin;
        SeekBar tactileSlider;
        TextView currentCountTextView;

        private final int PADDING_HORIZONTAL = 80;
        private final String CURRENT_COUNT_PREFIX = "Current Count: ";
        private int PRIMARY_DARK;
        private int PRIMARY_LIGHT;

        public TactileSlider(Context context, int steps, int maxCountLabel) {
            this.mContext = context;
            this.steps = steps + 1;
            this.maxCountLabel = maxCountLabel;
            this.currentCount = 0.0;
            PRIMARY_DARK = mContext.getResources().getColor(R.color.grape_dark);
            PRIMARY_LIGHT = mContext.getResources().getColor(R.color.grape_light);
            this.textColor = PRIMARY_LIGHT;
        }

        Double getCurrentCount(){
            return this.currentCount;
        }

        public void addTactileSlider(LinearLayout parent) {

            if (parent instanceof LinearLayout) {

                parent.setOrientation(LinearLayout.VERTICAL);
                tactileSlider = new SeekBar(mContext);
                tactileSlider.setMax(steps - 1);
                tactileSlider.incrementProgressBy(1);
                tactileSlider.setProgress(0);
                tactileSlider.setPadding(PADDING_HORIZONTAL, 0, PADDING_HORIZONTAL, 0);
                setSeekBarProgress(40, 100);
                setSeekBarThumb(80, 80);
                tactileSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) { }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                        currentCount = new Double(progress) / maxCountLabel;
                        currentCountTextView.setText(CURRENT_COUNT_PREFIX + String.valueOf(currentCount));
                    }
                });


                // Add LinearLayout for labels below SeekBar
                mSeekLin = new LinearLayout(mContext);
                mSeekLin.setOrientation(LinearLayout.HORIZONTAL);
                mSeekLin.setPadding(PADDING_HORIZONTAL-40, 0, PADDING_HORIZONTAL-54, 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(35, 10, 35, 0);
                mSeekLin.setLayoutParams(params);

                addCurrentCountTextView(parent);
                addLabelsBelowSlider();
                parent.addView(tactileSlider);
                parent.addView(mSeekLin);


            } else {

                Log.e("TactileSlider", " Parent is not a LinearLayout");

            }

        }



    private void addCurrentCountTextView(LinearLayout parent) {
        currentCountTextView = new TextView(mContext);
        currentCountTextView.setTextSize(20f);
        currentCountTextView.setTypeface(currentCountTextView.getTypeface(), Typeface.BOLD);
        currentCountTextView.setPadding(PADDING_HORIZONTAL, 0, PADDING_HORIZONTAL, 80);
        currentCountTextView.setText(CURRENT_COUNT_PREFIX + String.valueOf(currentCount));
        parent.addView(currentCountTextView);

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

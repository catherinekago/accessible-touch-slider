package com.example.tactileslider;

// Source: https://www.coderzheaven.com/2016/08/09/custom-seekbar-in-android-with-labels-in-bottom/

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class TactileSlider {

        int maxCount, maxCountLabel, textColor;
        Context mContext;
        LinearLayout mSeekLin;
        SeekBar tactileSlider;

        public TactileSlider(Context context, int maxCount, int maxCountLabel, int textColor) {
            this.mContext = context;
            this.maxCount = 100;
            this.textColor = textColor;
            this.maxCountLabel = maxCountLabel;
        }

        public void addTactileSlider(LinearLayout parent) {

            if (parent instanceof LinearLayout) {

                parent.setOrientation(LinearLayout.VERTICAL);
                tactileSlider = new SeekBar(mContext);
                tactileSlider.setMax(maxCount - 1);
                tactileSlider.incrementProgressBy(1);

                // Add LinearLayout for labels below SeekBar
                mSeekLin = new LinearLayout(mContext);
                mSeekLin.setOrientation(LinearLayout.HORIZONTAL);
                mSeekLin.setPadding(10, 0, 10, 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(35, 10, 35, 0);
                mSeekLin.setLayoutParams(params);

                addLabelsBelowSlider();
                parent.addView(tactileSlider);
                parent.addView(mSeekLin);

            } else {

                Log.e("TactileSlider", " Parent is not a LinearLayout");

            }

        }

        private void addLabelsBelowSlider() {
            for (int count = 0; count < maxCountLabel; count++) {
                TextView textView = new TextView(mContext);
                textView.setText(String.valueOf(count + 1));
                textView.setTextColor(textColor);
                textView.setGravity(Gravity.LEFT);
                mSeekLin.addView(textView);
                textView.setLayoutParams((count == maxCountLabel- 1) ? getLayoutParams(0.0f) : getLayoutParams(1.0f));
            }
        }

        LinearLayout.LayoutParams getLayoutParams(float weight) {
            return new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, weight);
        }

    }

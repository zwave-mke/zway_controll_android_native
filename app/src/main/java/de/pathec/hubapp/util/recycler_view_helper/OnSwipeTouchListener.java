package de.pathec.hubapp.util.recycler_view_helper;

import android.content.Context;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener {

    private GestureDetector mGestureDetector;
    private Vibrator mVibrator;

    public OnSwipeTouchListener(Context c) {
        mGestureDetector = new GestureDetector(c, new GestureListener());
        mVibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        return mGestureDetector.onTouchEvent(motionEvent);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public void onLongPress(MotionEvent e) {
            if(mVibrator.hasVibrator()) {
                mVibrator.vibrate(50);
            }
            onLongClick();
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        // Determines the fling velocity and then fires the appropriate swipe event accordingly
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            if(mVibrator.hasVibrator()) {
                                mVibrator.vibrate(50);
                            }
                            onSwipeRight();
                        } else {
                            if(mVibrator.hasVibrator()) {
                                mVibrator.vibrate(50);
                            }
                            onSwipeLeft();
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeDown();
                        } else {
                            onSwipeUp();
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return false;
        }
    }

    public void onSwipeRight() { }

    public void onSwipeLeft() { }

    public void onSwipeUp() { }

    public void onSwipeDown() { }

    public void onLongClick() { }
}
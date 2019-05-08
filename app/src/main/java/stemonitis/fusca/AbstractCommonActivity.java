package stemonitis.fusca;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.os.Handler;


public abstract class AbstractCommonActivity extends AppCompatActivity {
    protected boolean autoHide = true;
    protected int autoHideDelayMillis = 2000;
    protected int uiAnimationDelay = 300;
    protected boolean barsAreVisible = false;
    protected int autoScrollDelay = 5000;

    protected HideBarsRunnable hideBarsRunnable = new HideBarsRunnable();
    protected ShowActionBarRunnable showActionBarRunnable = new ShowActionBarRunnable();

    protected Handler uiHandler = new Handler();

    // These fields must be set in the concrete class
    protected View contentView;
    protected Runnable autoScrollRunnable;

    /**
     * Handle the visibility of bars (action bar, navigation bar, etc).
     * Every process ends up with start of auto scroll.
     */
    protected void hideBars(){
        // Hide UI first
        getSupportActionBar().hide();
        barsAreVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        uiHandler.removeCallbacksAndMessages(null);
        contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        uiHandler.postDelayed(autoScrollRunnable, autoScrollDelay);
        // -> autoScroll
    }

    protected class HideBarsRunnable implements Runnable{
        @Override
        public void run(){
            hideBars();
            // -> setVisibility -> autoScroll
        }
    }

    protected void delayedHide(int delayMillis) {
        uiHandler.removeCallbacksAndMessages(null);
        uiHandler.postDelayed(hideBarsRunnable, delayMillis);
        // -> setVisibility -> autoScroll
    }

    protected class ShowActionBarRunnable implements Runnable{
        @Override
        public void run(){
            getSupportActionBar().show();
            if(autoHide){
                delayedHide(autoHideDelayMillis);
                // -> hideBars -> setVisibility -> autoScroll
            }else{
                uiHandler.postDelayed(autoScrollRunnable, autoScrollDelay);
            }
        }
    }

    protected void showBars(){
        barsAreVisible = true;

        // Schedule a runnable to display UI elements after a delay
        uiHandler.removeCallbacksAndMessages(null);
        uiHandler.postDelayed(showActionBarRunnable, uiAnimationDelay);
        // -> ... -> autoScroll
    }

    protected void toggleUiVisibility(){
        uiHandler.removeCallbacksAndMessages(null);
        if (barsAreVisible) {
            hideBars();
            // -> ... -> autoScroll
        } else {
            showBars();
            // -> ... -> autoScroll
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        hideBars();
        uiHandler.removeCallbacksAndMessages(null);
        uiHandler.postDelayed(autoScrollRunnable, autoScrollDelay);
    }
}

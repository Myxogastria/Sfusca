package stemonitis.fusca;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ArticleActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 2000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    public void onFinishButtonClick(View view){
        Intent data = new Intent();
        data.putExtra("nextHeadline", false);
        setResult(RESULT_OK, data);
        finish();
    }

    private static final int FIRST_SCROLL_DELAY = 20000;
    private static final int AUTO_SCROLL_DELAY = 10000;

    private View lArticle;
    private ScrollView svArticle;
    private ProgressBar pbArticle;
    private Handler scrollHandler = new Handler();
    private int scrollBy;
    private boolean autoChange;
    private Iterator<Article> articleIterator;
    private Article presentArticle;
    private TextView tvArticleTitle;
    private TextView tvArticle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        View decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_article);

        mVisible = true;
        mContentView = findViewById(R.id.lArticle);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        mContentView.setOnTouchListener(mDelayHideTouchListener);

        tvArticleTitle = findViewById(R.id.tvArticleTitle);
        tvArticle = findViewById(R.id.tvArticle);

        Intent intent = getIntent();

        autoChange = intent.getBooleanExtra("autoChange", true);
        if (autoChange){
            ArrayList<Article> articles = new ArrayList<>();
            Iterator<String> iTitle = intent.getStringArrayListExtra("titles").iterator();
            Iterator<String> iContent = intent.getStringArrayListExtra("contents").iterator();
            while(iTitle.hasNext() && iContent.hasNext()){
                Article a = new Article(iTitle.next());
                a.setContent(iContent.next());
                articles.add(a);
            }

            articleIterator = articles.iterator();
            presentArticle = articleIterator.next();
        }else{
            presentArticle = new Article(intent.getStringExtra("title"));
            presentArticle.setContent(intent.getStringExtra("content"));
        }

        tvArticleTitle.setText(presentArticle.getTitle());
        tvArticle.setText(presentArticle.getContent());

        pbArticle = findViewById(R.id.pbArticle);
        svArticle = findViewById(R.id.svArticle);
        svArticle.setVerticalScrollBarEnabled(false);
    }

    private boolean canScroll(){
        return svArticle.getScrollY() <
                (svArticle.getChildAt(0).getMeasuredHeight() -
                        svArticle.getMeasuredHeight() );
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);

        pbArticle.setMax(svArticle.getChildAt(0).getMeasuredHeight() -
                svArticle.getMeasuredHeight());
        svArticle.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                pbArticle.setMax(svArticle.getChildAt(0).getMeasuredHeight() -
                        svArticle.getMeasuredHeight());
                pbArticle.setProgress(svArticle.getScrollY());
            }
        });

        lArticle = findViewById(R.id.lArticle);

        scrollHandler.removeCallbacksAndMessages(null);
        scrollHandler.postDelayed(autoScrollRunnable, FIRST_SCROLL_DELAY);
    }

    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if(canScroll()){
                scrollBy = (svArticle.getChildAt(0).getMeasuredHeight() -
                        svArticle.getMeasuredHeight());
                scrollBy = lArticle.getHeight()/4;
                svArticle.smoothScrollBy(0, scrollBy);
                scrollHandler.removeCallbacksAndMessages(null);
                scrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
            }else if (autoChange){
                if(articleIterator.hasNext()){
                    presentArticle = articleIterator.next();
                    redrawArticle();
                }else{
                    Log.i("finish", "aaa");
                    Intent data = new Intent();
                    data.putExtra("nextHeadline", true);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        }
    };

    private void redrawArticle(){
        scrollHandler.removeCallbacksAndMessages(null);

        tvArticleTitle.setText("");
        tvArticle.setText("");
        pbArticle.setProgress(0);
        svArticle.smoothScrollTo(0, 0);

        tvArticleTitle.setText(presentArticle.getTitle());
        tvArticle.setText(presentArticle.getContent());
        svArticle.smoothScrollTo(0, 0);
        pbArticle.setMax(svArticle.getChildAt(0).getMeasuredHeight() -
                svArticle.getMeasuredHeight());

        scrollHandler.postDelayed(autoScrollRunnable, FIRST_SCROLL_DELAY);
    }

    @Override
    public void onResume(){
        super.onResume();
        hide();

        scrollHandler.removeCallbacksAndMessages(null);
        scrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}

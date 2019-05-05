package stemonitis.fusca;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class HeadlineActivity extends AppCompatActivity {
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
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
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

    private static int AUTO_SCROLL_DELAY = 15000;
    private static int DURATION = 1500;

    private List<Headline> media;
    private ListIterator<Headline> hlIterator;
    private Headline headline;
    private TextView title;
    private ListView lvHeadline;
    private Handler nextHandler = new Handler();
    private Handler headlineHandler = new Handler();
    private boolean headlineIsSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        View decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_headline);

        mVisible = true;
        mContentView = findViewById(R.id.lHeadline);

        /**
         * Touch listener to use for in-layout UI controls to delay hiding the
         * system UI. This is to prevent the jarring behavior of controls going away
         * while interacting with activity UI.
         */
        View.OnTouchListener mDelayHideTouchListener =
                new OnSwipeTouchListener(HeadlineActivity.this) {
                    @Override
                    public void onPresumedClick(){
                        nextHandler.removeCallbacksAndMessages(null);
                        scrollHandler.removeCallbacksAndMessages(null);
                        toggle();
                        if (AUTO_HIDE && mVisible) {
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }

                        scrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
                    }

                    @Override
                    public void onSwipeRight(){
                        nextHandler.removeCallbacksAndMessages(null);
                        previousHeadline();
                    }

                    @Override
                    public void onSwipeLeft(){
                        nextHandler.removeCallbacksAndMessages(null);
                        nextHeadline();
                    }
                };
        mContentView.setOnTouchListener(mDelayHideTouchListener);

        media = new ArrayList<>();
        media.add(new Nikkei(20));
        media.add(new Reuters(10));
        media.add(new SZ(10));
        media.add(new TechCrunch(10));
        headlineReload();

        hlIterator = media.listIterator();
        if(hlIterator.hasNext()) {
            headline = hlIterator.next();
        }

        title = findViewById(R.id.tvHeadlineTitle);
        title.setText(headline.getName());

        lvHeadline = findViewById(R.id.lvHeadline);
        lvHeadline.setVerticalScrollBarEnabled(false);
        lvHeadline.setOnItemClickListener(new ListItemClickListener());

        headlineIsSet = false;
        headlineHandler.postDelayed(headlineSetter, RELOAD_CHECK_INTERVAL);
    }

    private static int RELOAD_CHECK_INTERVAL = 200;

    private Runnable headlineSetter = new Runnable() {
        @Override
        public void run() {
            if(!headline.isReloading()){
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(HeadlineActivity.this,
                        android.R.layout.simple_list_item_1, headline.getList()){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView view = (TextView)super.getView(position, convertView, parent);
                        view.setTextSize( 45 );
                        return view;
                    }
                };
                lvHeadline.setAdapter(adapter);
                headlineIsSet = true;
                scrollHandler.removeCallbacksAndMessages(null);
                scrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
            }else{
                headlineIsSet = false;
                headlineHandler.removeCallbacksAndMessages(null);
                headlineHandler.postDelayed(headlineSetter, RELOAD_CHECK_INTERVAL);
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        nextHandler.removeCallbacksAndMessages(null);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                scrollHandler.removeCallbacksAndMessages(null);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                scrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    private Handler scrollHandler = new Handler();
    private int scrollBy=0;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (canScroll()) {
                lvHeadline.smoothScrollBy(scrollBy, DURATION);
                scrollHandler.removeCallbacks(this);
                scrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
            } else if(headlineIsSet){
                nextHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        goToArticlesFrom(0);
                    }
                }, AUTO_SCROLL_DELAY);
            }
        }
    };

    private boolean canScroll(){
        int lastIndex = lvHeadline.getLastVisiblePosition()
                - lvHeadline.getFirstVisiblePosition();
        View c = lvHeadline.getChildAt(lastIndex);
        if (c!=null){
            scrollBy = c.getHeight() * lastIndex;
            return (lvHeadline.getLastVisiblePosition() < lvHeadline.getAdapter().getCount()-1)
                    || (c.getBottom() > lvHeadline.getHeight());
        }else{
            return false;
        }
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
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Log.i("hide", "not null");
            actionBar.hide();
        }else{
            Log.i("hide", "null");
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        // Show the action bar
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            Log.i("show", "not null");
//            actionBar.show();
//        }else{
//            Log.i("show", "null");
//        }
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

    private void headlineReload(){
        new AsyncReloader(media).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_context_headline, menu);
        return true;
    }
//    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
//        super.onCreateContextMenu(menu, view, menuInfo);
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_context_headline, menu);
//    }

    private void previousHeadline(){
        if(!headline.isReloading()) {
            Headline headline0 = headline;
            new AsyncReloader(headline0).execute();
        }
        if (hlIterator.hasPrevious()) {
            headline = hlIterator.previous();
        } else {
            while (hlIterator.hasNext()) {
                headline = hlIterator.next();
            }
        }
        headlineIsSet = false;
        title.setText(headline.getName());
        scrollHandler.removeCallbacksAndMessages(null);
        headlineHandler.removeCallbacksAndMessages(null);
        headlineHandler.postDelayed(headlineSetter, RELOAD_CHECK_INTERVAL);
    }

    private void nextHeadline(){
        if(!headline.isReloading()) {
            Headline headline0 = headline;
            new AsyncReloader(headline0).execute();
        }
        if (hlIterator.hasNext()) {
            headline = hlIterator.next();
        } else {
            while (hlIterator.hasPrevious()) {
                headline = hlIterator.previous();
            }
        }
        headlineIsSet = false;
        title.setText(headline.getName());
        scrollHandler.removeCallbacksAndMessages(null);
        headlineHandler.removeCallbacksAndMessages(null);
        headlineHandler.postDelayed(headlineSetter, RELOAD_CHECK_INTERVAL);
    }

    private class ListItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            goToArticlesFrom(position);
        }
    }

    private void goToArticlesFrom(int index){
        scrollHandler.removeCallbacksAndMessages(null);

        if(headline.getArticles().size()>0) {
            List<Article> aList = headline.getArticles().subList(index, headline.articles.size());
            ArrayList<String> titles = new ArrayList<>();
            ArrayList<String> contents = new ArrayList<>();

            for (Article a : aList) {
                titles.add(a.getTitle());
                contents.add(a.getContent());
            }

            Intent intent = new Intent(HeadlineActivity.this, ArticleActivity.class);
            intent.putExtra("autoChange", true);
            intent.putStringArrayListExtra("titles", titles);
            intent.putStringArrayListExtra("contents", contents);
            startActivityForResult(intent, 0);
        }else{
            scrollHandler.removeCallbacksAndMessages(null);
            nextHeadline();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("onActivityResult", "aaa");
        super.onActivityResult(requestCode, resultCode, data);
        if(data.getBooleanExtra("nextHeadline", false)) {
            scrollHandler.removeCallbacksAndMessages(null);
            nextHeadline();
        }
    }
}

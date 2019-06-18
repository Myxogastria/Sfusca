package stemonitis.fusca;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public final class HeadlineActivity extends AbstractCommonActivity {
    private int scrollDuration;

    private ListIterator<Medium> mediaIterator;
    private Medium medium;
    private TextView title;
    private ListView headlineListView;
    private boolean headlineIsReady;
    private boolean isActive = false;

    private int scrollBy;
    private static final int SCROLL_BY_DIVIDER = 10;
    private int scrollDistance = 0; // set value in canScroll()

    private int mediaFontSize;
    private int headlineFontSize;

    /**
     * 2 handlers are handled in this activity.
     * headlineHandler handles reload of the news.
     * uiHandler (superclass) handles UIs, which include visibility of action bar,
     * switching the medium of headline, and scrolling of headline.
     */
    private Handler headlineHandler = new Handler();

    private View contentView;
    private Runnable autoScrollRunnable;

    @Override
    protected View getContentView(){
        return contentView;
    }

    @Override
    protected Runnable getAutoScrollRunnable(){
        return autoScrollRunnable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.preferences_main, false);
        loadPreference();

		autoScrollRunnable = new Runnable() {
		        @Override
		        public void run() {
		            if (canScroll()) {
		                headlineListView.smoothScrollBy(scrollDistance, scrollDuration);
		                uiHandler.removeCallbacksAndMessages(null);
		                uiHandler.postDelayed(this, autoScrollDelay);
		            } else if(!isActive) {
                        uiHandler.removeCallbacksAndMessages(null);
                        uiHandler.postDelayed(this, autoScrollDelay);
                    } else if(headlineIsReady){
                        Log.i("HeadlineActivity", "can't scroll");
		                uiHandler.removeCallbacksAndMessages(null);
		                uiHandler.postDelayed(new Runnable() {
		                    @Override
		                    public void run() {
		                        goToArticlesFrom(0);
		                    }
		                }, autoScrollDelay);
		            }
		        }
		    };

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        View decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hideBars the status bar whenever you
// hideBars the navigation bar.
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.activity_headline);

        barsAreVisible = true;
        contentView = findViewById(R.id.lHeadline);
        contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        /**
         * Touch listener to use for in-layout UI controls to delay hiding the
         * system UI. This is to prevent the jarring behavior of controls going away
         * while interacting with activity UI.
         */
        contentView.setOnTouchListener(new OnSwipeTouchListener(HeadlineActivity.this) {
                    @Override
                    public void onPresumedClick(){
                        toggleUiVisibility();
                        Toast.makeText(HeadlineActivity.this, "onPresumedClick()", Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onSwipeRight(){
                        previousMedium();
                    }

                    @Override
                    public void onSwipeLeft(){
                        nextMedium();
                    }
                });

        contentView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener(){
                    @Override
                    public void onSystemUiVisibilityChange(int visibility){
                        if(isActive){
                            Log.i("HeadlineActivity", "VisibilityChange" + String.valueOf(visibility));
                            if (visibility == View.SYSTEM_UI_FLAG_VISIBLE){
                                showBars();
                            }else{
                                hideBars();
                            }
                        }
                    }
                });

    }

    private void loadPreference(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        autoScrollDelay = sharedPreferences.getInt(
                getString(R.string.pref_autoScrollDelay), -1)*100;
        scrollDuration = sharedPreferences.getInt(
                getString(R.string.pref_scrollDuration), -1)*100;
        scrollBy = sharedPreferences.getInt(
                getString(R.string.pref_scrollBy), -1);
        mediaFontSize = sharedPreferences.getInt(
                getString(R.string.pref_mediaFontSize), -1);
        headlineFontSize = sharedPreferences.getInt(
                getString(R.string.pref_headlineFontSize), -1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_headline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menuReload:
                hideBars();
                Log.i(this.getClass().getSimpleName(), "Menu Reload selected");
                Toast.makeText(HeadlineActivity.this, R.string.tst_reload, Toast.LENGTH_SHORT).show();
                if(!medium.isReloading()) {
                    new AsyncReloader(medium).withExceptionToast(this).execute();
                    headlineIsReady = false;
                    title.setText(medium.getNameInHeadline());
                    headlineHandler.removeCallbacksAndMessages(null);
                    headlineHandler.postDelayed(headlineSetter, RELOAD_CHECK_INTERVAL);
                }
                break;
            case R.id.menuSettings:
                isActive = false;
                Log.i(this.getClass().getSimpleName(), "Menu Settings selected");
                Intent intent = new Intent(HeadlineActivity.this, SettingsActivity.class);
                intent.putExtra(getString(R.string.intent_settingsType), SettingsActivity.MAIN);
                uiHandler.removeCallbacksAndMessages(null);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hideBars() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private static int RELOAD_CHECK_INTERVAL = 200;

    private Runnable headlineSetter = new Runnable() {
        @Override
        public void run() {
            if(!medium.isReloading()){
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(HeadlineActivity.this,
                        android.R.layout.simple_list_item_1, medium.getList()){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView view = (TextView)super.getView(position, convertView, parent);
                        view.setTextSize( headlineFontSize );
                        return view;
                    }
                };
                headlineListView.setAdapter(adapter);
                headlineIsReady = true;
                uiHandler.removeCallbacksAndMessages(null);
                uiHandler.postDelayed(autoScrollRunnable, autoScrollDelay);
            }else{
                headlineIsReady = false;
                headlineHandler.removeCallbacksAndMessages(null);
                headlineHandler.postDelayed(headlineSetter, RELOAD_CHECK_INTERVAL);
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        uiHandler.removeCallbacksAndMessages(null);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                uiHandler.removeCallbacksAndMessages(null);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                uiHandler.postDelayed(autoScrollRunnable, autoScrollDelay);
                break;
        }

        return super.dispatchTouchEvent(ev);
    }


    private boolean canScroll(){
        int lastIndex = headlineListView.getLastVisiblePosition()
                - headlineListView.getFirstVisiblePosition();
        View c = headlineListView.getChildAt(lastIndex);
        if (c!=null){
            // scroll distance is roughly the height of display
            scrollDistance = c.getHeight() * lastIndex * scrollBy / SCROLL_BY_DIVIDER;
            return (headlineListView.getLastVisiblePosition() < headlineListView.getAdapter().getCount()-1)
                    || (c.getBottom() > headlineListView.getHeight());
        }else{
            return false;
        }
    }


    private void previousMedium(){
        if(!medium.isReloading()) {
            Medium medium0 = medium;
            new AsyncReloader(medium0).withExceptionToast(this).execute();
        }
        if (mediaIterator.hasPrevious()) {
            medium = mediaIterator.previous();
        } else {
            while (mediaIterator.hasNext()) {
                medium = mediaIterator.next();
            }
        }
        headlineIsReady = false;
        title.setText(medium.getNameInHeadline());
        uiHandler.removeCallbacksAndMessages(null);
        headlineHandler.removeCallbacksAndMessages(null);
        headlineHandler.postDelayed(headlineSetter, RELOAD_CHECK_INTERVAL);
    }

    private void nextMedium(){
        if(!medium.isReloading()) {
            Medium medium0 = medium;
            new AsyncReloader(medium0).withExceptionToast(this).execute();
        }
        if (mediaIterator.hasNext()) {
            medium = mediaIterator.next();
        } else {
            while (mediaIterator.hasPrevious()) {
                medium = mediaIterator.previous();
            }
        }
        headlineIsReady = false;
        title.setText(medium.getNameInHeadline());
        uiHandler.removeCallbacksAndMessages(null);
        headlineHandler.removeCallbacksAndMessages(null);
        headlineHandler.postDelayed(headlineSetter, RELOAD_CHECK_INTERVAL);
    }


    private void goToArticlesFrom(int index){
        Log.i("HeadlineActivity", "goToArticlesFrom" + index);
        if(medium.getArticles().size()>0) {
            List<Article> aList = medium.getArticles().subList(index, medium.articles.size());
            ArrayList<String> titles = new ArrayList<>();
            ArrayList<String> contents = new ArrayList<>();

            for (Article a : aList) {
                titles.add(a.getTitle());
                contents.add(a.getContent());
            }

            Intent intent = new Intent(HeadlineActivity.this, ArticleActivity.class);
            intent.putExtra(getString(R.string.intent_autoChange), true);
            intent.putStringArrayListExtra(getString(R.string.intent_articleTitles), titles);
            intent.putStringArrayListExtra(getString(R.string.intent_articleContents), contents);
            intent.putExtra(getString(R.string.intent_articlePreferences), medium.getArticlePreferences());
            startActivityForResult(intent, 0);
        }else{
            nextMedium();
        }
    }


    @Override
    public void onResume(){
        super.onResume();

        loadPreference();

//        mediaList = new ArrayList<>();
//        mediaList.add(new Nikkei(10));
//        mediaList.add(new Reuters(5));
//        mediaList.add(new SZ(5));
//        mediaList.add(new TechCrunch(5));
//        mediaList = MediaFactory.createDefaultMedia();
        List<Medium> mediaList = MediaFactory.resumeMedia(
                PreferenceManager.getDefaultSharedPreferences(HeadlineActivity.this));
        new AsyncReloader(mediaList).withExceptionToast(this).execute();

        mediaIterator = mediaList.listIterator();
        if(mediaIterator.hasNext()) {
            medium = mediaIterator.next();
        }

        title = findViewById(R.id.tvHeadlineTitle);
        title.setText(medium.getNameInHeadline());
        title.setTextSize(mediaFontSize);

        headlineListView = findViewById(R.id.lvHeadline);
        headlineListView.setVerticalScrollBarEnabled(false);
        headlineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToArticlesFrom(position);
            }
        });

        headlineIsReady = false;
        headlineHandler.postDelayed(headlineSetter, RELOAD_CHECK_INTERVAL);

        isActive = true;
    }

    @Override
    public void onPause(){
        isActive = false;

        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("onActivityResult", "aaa");
        super.onActivityResult(requestCode, resultCode, data);
        if(data.getBooleanExtra("nextMedium", false)) {
            uiHandler.removeCallbacksAndMessages(null);
            nextMedium();
        }
    }

}

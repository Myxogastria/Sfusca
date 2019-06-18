package stemonitis.fusca;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ArticleActivity extends AbstractCommonActivity{
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */

    public void onFinishButtonClick(View view){
        Intent data = new Intent();
        data.putExtra("nextHeadline", false);
        setResult(RESULT_OK, data);
        finish();
    }

    private int firstScrollDelay = 20000;
    private int scrollBy=3;
    private static final int SCROLL_BY_DIVIDER = 10;

    private View lArticle;
    private ScrollView svArticle;
    private ProgressBar pbArticle;
    private int scrollDistance;
    private boolean autoChange;
    private Iterator<Article> articleIterator;
    private Article presentArticle;
    private TextView tvArticleTitle;
    private TextView tvArticle;

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

        contentView = findViewById(R.id.lArticle);
        contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);


        tvArticleTitle = findViewById(R.id.tvArticleTitle);
        tvArticle = findViewById(R.id.tvArticle);

        Intent intent = getIntent();

        autoChange = intent.getBooleanExtra(getString(R.string.intent_autoChange), true);
        if (autoChange){
            ArrayList<Article> articles = new ArrayList<>();
            Iterator<String> iTitle = intent.getStringArrayListExtra(getString(R.string.intent_articleTitles)).iterator();
            Iterator<String> iContent = intent.getStringArrayListExtra(getString(R.string.intent_articleContents)).iterator();
            while(iTitle.hasNext() && iContent.hasNext()){
                Article a = new Article(iTitle.next());
                a.setContent(iContent.next());
                articles.add(a);
            }

            articleIterator = articles.iterator();
            presentArticle = articleIterator.next();
        }else{
            presentArticle = new Article(intent.getStringExtra(getString(R.string.intent_articleTitles)));
            presentArticle.setContent(intent.getStringExtra(getString(R.string.intent_articleContents)));
        }

        HashMap<String, Integer> articlePreferences = (HashMap<String, Integer>) intent.getSerializableExtra(getString(R.string.intent_articlePreferences));
        if (articlePreferences.containsKey(getString(R.string.pref_firstScrollDelay))){
            firstScrollDelay = articlePreferences.get(getString(R.string.pref_firstScrollDelay))*100;
        }
        if (articlePreferences.containsKey(getString(R.string.pref_autoScrollDelay))){
            autoScrollDelay = articlePreferences.get(getString(R.string.pref_autoScrollDelay))*100;
        }
        if (articlePreferences.containsKey(getString(R.string.pref_scrollBy))){
            scrollBy = articlePreferences.get(getString(R.string.pref_scrollBy));
        }
        if (articlePreferences.containsKey(getString(R.string.pref_articleFontSize))){
            tvArticle.setTextSize(articlePreferences.get(getString(R.string.pref_articleFontSize)));
        }
        if (articlePreferences.containsKey(getString(R.string.pref_articleTitleFontSize))){
            tvArticleTitle.setTextSize(articlePreferences.get(getString(R.string.pref_articleTitleFontSize)));
        }

        tvArticleTitle.setText(presentArticle.getTitle());
        tvArticle.setText(presentArticle.getContent());

	    autoScrollRunnable = new Runnable() {
	        @Override
	        public void run() {
	            if(canScroll()){
	                scrollDistance = (svArticle.getChildAt(0).getMeasuredHeight() -
	                        svArticle.getMeasuredHeight());
	                scrollDistance = lArticle.getHeight()*scrollBy/SCROLL_BY_DIVIDER;
//	                this is different from AbsListView#smoothScrollBy
	                svArticle.smoothScrollBy(0, scrollDistance);
	                uiHandler.removeCallbacksAndMessages(null);
	                uiHandler.postDelayed(this, autoScrollDelay);
	            }else if (autoChange){
	                if(articleIterator.hasNext()){
	                    presentArticle = articleIterator.next();
	                    redrawArticle();
	                }else{
	                    Log.i("finish", "aaa");
	                    Intent data = new Intent();
	                    data.putExtra("nextMedium", true);
	                    setResult(RESULT_OK, data);
	                    finish();
	                }
	            }
	        }
	    };

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

        uiHandler.removeCallbacksAndMessages(null);
        uiHandler.postDelayed(autoScrollRunnable, firstScrollDelay);
    }

    private void redrawArticle(){
        uiHandler.removeCallbacksAndMessages(null);

        tvArticleTitle.setText("");
        tvArticle.setText("");
        pbArticle.setProgress(0);
        svArticle.smoothScrollTo(0, 0);

        tvArticleTitle.setText(presentArticle.getTitle());
        tvArticle.setText(presentArticle.getContent());
        svArticle.smoothScrollTo(0, 0);
        pbArticle.setMax(svArticle.getChildAt(0).getMeasuredHeight() -
                svArticle.getMeasuredHeight());

        uiHandler.postDelayed(autoScrollRunnable, firstScrollDelay);
    }

}

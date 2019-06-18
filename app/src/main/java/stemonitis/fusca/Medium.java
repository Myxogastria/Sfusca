package stemonitis.fusca;

import android.content.SharedPreferences;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Medium implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected final int id;

    protected static String DEFAULT_CONTENT = "Oops! No content.";
    protected List<Article> articles = new ArrayList<>();
    protected boolean reloading = true;
    protected HashMap<String, Integer> articlePreferences = null;

    protected Medium(int id){
        this.id = id;
    }

    public abstract String getProfileString();
    public abstract String getName();
    public abstract void reload() throws IOException;

    public abstract PreferenceFragmentCompat getSettingsFragment();
    public abstract void setPreferenceManager(PreferenceManager preferenceManager, SharedPreferences sharedPreferences);

    public int getId(){
        return id;
    }

    public String getNameInSettings(){
        return getName();
    };

    public String getNameInHeadline(){
        return getName();
    };

    public List<String> getList(){
        List<String> headlines = new ArrayList<>();
        for(Article a : articles){
            headlines.add(a.getTitle());
        }
        return headlines;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public boolean isReloading(){
        return reloading;
    };

    public HashMap<String, Integer> getArticlePreferences(){
        return articlePreferences;
    };

    protected String getString(int resId){
        return ContextGetter.getContext().getString(resId);
    }

}

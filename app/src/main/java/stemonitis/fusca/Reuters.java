package stemonitis.fusca;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Reuters extends Medium{
    private static String NAME = "Reuters";
    private static String URL = "https://www.reuters.com/theWire";
    private static String URL_PREFIX = "";
    private int maxSize = 10;

    private PreferenceManager preferenceManager;

    public Reuters(int id, String profileString){
        super(id);
        articlePreferences = new HashMap<>();
        for (String str : profileString.split(",")){
            String[] splitted = str.split("=", 2);
            if(splitted.length == 2){
                if (splitted[0].equals("maxSize")){
                    maxSize = Integer.parseInt(splitted[1]);
                }else{
                    articlePreferences.put(splitted[0], Integer.parseInt(splitted[1]));
                }
            }
        }
    }

    @Override
    public String getProfileString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (String key : articlePreferences.keySet()){
            stringBuffer.append("," + key + "=" + articlePreferences.get(key));
        }
        return "maxSize=" + maxSize + stringBuffer.toString();
    }


    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void reload() throws IOException{
        Log.i(NAME, "reload");
        reloading = true;

        try{
            articles = new ArrayList<>();
            Document document = Jsoup.connect(URL).get();

            Elements elements = document.select("h2.FeedItemHeadline_headline.FeedItemHeadline_full");
            for(Element element : elements){
                Article a = new Article(element.text(),
                        URL_PREFIX + element.select("a").attr("href"));
                a.setContent(formatText(getContentFromURL(a.getUrl())));
                articles.add(a);
                if(articles.size()>=maxSize) break;
            }

            Log.i(NAME, "reloaded");
        }catch (IOException e){
            Log.i(NAME, "exception occurred");
            throw e;
        }finally {
            reloading = false;
        }
    }

    private String formatText(Elements content){
        String text = DEFAULT_CONTENT;
        if(content!=null) {
            StringBuffer sb = new StringBuffer();
            Elements pElements = content.select("p");
            if (pElements.size()>0) {
                for (Element e : pElements) {
                    if (e.text().length() > 0) {
                        sb.append(e.text());
                        sb.append("\n\n");
                    }
                }
            }
            text = sb.toString();
        }
        return text;
    }

    private Elements getContentFromURL(String url){
        Elements content = null;
        try{
            Document document = Jsoup.connect(url).get();
            content = new Elements(document.select("div.StandardArticleBody_body").first());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    @Override
    public PreferenceFragmentCompat getSettingsFragment() {
        return new MaxSizePreferenceFragment(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_maxArticles))){
            maxSize = sharedPreferences.getInt(key, maxSize);
        }else{
            articlePreferences.put(key, sharedPreferences.getInt(key,
                    articlePreferences.get(key)));
        }
    }

    @Override
    public void setPreferenceManager(PreferenceManager preferenceManager, SharedPreferences sharedPreferences) {
        this.preferenceManager = preferenceManager;

        for (String key : articlePreferences.keySet()){
            Preference preference = this.preferenceManager.findPreference(key);
            if(preference != null){
                if(preference instanceof SeekBarPreference){
                    ((SeekBarPreference) preference).setValue(articlePreferences.get(key));
                }
            }
        }
        PreferenceCategory articleCategory = (PreferenceCategory) this.preferenceManager.findPreference(
                getString(R.string.pref_category_article));
        for (int i = 0; i < articleCategory.getPreferenceCount(); i++){
            Preference preference = articleCategory.getPreference(i);
            if (!articlePreferences.keySet().contains(preference.getKey())){
                articlePreferences.put(preference.getKey(),
                        sharedPreferences.getInt(preference.getKey(), -1));
            }
        }

        ((SeekBarPreference) this.preferenceManager.findPreference(getString(R.string.pref_maxArticles)))
                .setValue(maxSize);
    }
}

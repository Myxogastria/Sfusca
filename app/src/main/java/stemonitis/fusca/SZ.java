package stemonitis.fusca;

import android.content.SharedPreferences;
import android.os.Bundle;
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
import java.util.List;

public class SZ extends Medium {
    private static String NAME = "Süddeutche Zeitung";
    private static String URL = "http://www.sueddeutsche.de/news";
    private static String URL_PREFIX = "";
    private int maxSize = 10;

    private PreferenceManager preferenceManager;

    public SZ(int id, String profileString){
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
        Log.i("SZ", "reload");
        reloading = true;
        try{
            Document document = Jsoup.connect(URL).get();
            Elements elements = document.select(
                    "a.entrylist__link");
            articles = new ArrayList<>();
            for(Element element : elements){
                String url = element.attr("href");
                if(url.startsWith("https://www.sueddeutsche.de/")) {
                    Article a = new Article(element.select("em.entrylist__title").text(),
                            URL_PREFIX + element.attr("href"));
                    a.setContent(formatText(getContentFromURL(a.getUrl())));
                    if (!a.getContent().isEmpty()) articles.add(a);
                }
                if(articles.size()>=maxSize) break;
            }
            Log.i("SZ", "reloaded");
        }catch (IOException e){
            Log.i("SZ", "exception occurred");
            throw e;
        }finally {
            reloading = false;
        }
    }

    private String formatText(Elements content){
        String text = DEFAULT_CONTENT;
        if(content!=null){
            StringBuffer sb = new StringBuffer();
            Elements pElements = content.select("section.body>p, section.body>ul");
            for(Element e : pElements){
                switch(e.tagName()){
                    case "p":
                        sb.append(e.text());
                        sb.append("\n\n");
                        break;
                    case "ul":
                        Elements liElements = e.select(">li");
                        for(Element eli : liElements) {
                            sb.append("* ");
                            sb.append(eli.text());
                            sb.append("\n");
                        }
                        break;
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
            content = document.select("section.body");
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

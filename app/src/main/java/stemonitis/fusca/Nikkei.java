package stemonitis.fusca;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Nikkei extends Medium implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static String NAME = "日本経済新聞";
    private static String URL = "http://www.nikkei.com/news/category/";
    private static String URL_PREFIX = "http://www.nikkei.com";
    private int maxSize = 10;
    private boolean onlyFree = true;

    private PreferenceManager preferenceManager;

    public Nikkei(int id, String profileString){
        super(id);
        articlePreferences = new HashMap<>();
        for (String str : profileString.split(",")){
            String[] splitted = str.split("=", 2);
            if(splitted.length == 2){
                if (splitted[0].equals("maxSize")){
                    maxSize = Integer.parseInt(splitted[1]);
                }else if (splitted[0].equals(("onlyFree"))){
                    onlyFree = Boolean.valueOf(splitted[1]);
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
        return "maxSize=" + maxSize + ",onlyFree=" + onlyFree + stringBuffer.toString();
    }

    @Override
    public PreferenceFragmentCompat getSettingsFragment() {
        return new InnerPreferenceFragment(this);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void reload() throws IOException {
        Log.i("Nikkei", "reload");
        reloading = true;

        try{
            Document document = Jsoup.connect(URL).get();
            Elements elements = document.select("h3.m-miM09_title");
            articles = new ArrayList<>();
            for(Element element : elements){
                if(!onlyFree || !element.text().contains("［有料会員限定］")) {
                    Article a = new Article(element.text(),
                            URL_PREFIX + element.select("a").attr("href"));
                    a.setContent(formatText(getContentFromURL(a.getUrl())));
                    articles.add(a);
                    if(articles.size()>=maxSize) break;
                }
            }
            elements = document.select("span.m-miM32_itemTitleText");
            for(Element element : elements){
                if(articles.size()>=maxSize) break;
                if(!onlyFree || !element.text().contains("［有料会員限定］")) {
                    Article a = new Article(element.text(),
                            URL_PREFIX + element.select("a").attr("href"));
                    a.setContent(formatText(getContentFromURL(a.getUrl())));
                    articles.add(a);
                }
            }
            Log.i("Nikkei", "reloaded");
        }catch (IOException e){
            Log.i("Nikkei", "exception occurred");
            throw e;
        }finally {
            reloading = false;
        }
    }

    private String formatText(Elements content){
        String text = DEFAULT_CONTENT;
        if(content!=null) {
            StringBuffer sb = new StringBuffer();
            Elements pElements = content.select(">p");
            if (pElements.size()>0) {
                for (Element e : pElements) {
                    if (e.text().length() > 0) {
                        sb.append(e.text());
                        sb.append("\n\n");
                    }
                }
            }else{
                pElements = content.select(">li");
                sb.append(pElements.text());
                sb.append("\n\n");
            }
            text = sb.toString();
        }
        return text;
    }

    private Elements getContentFromURL(String url){
        Elements content = null;
        try{
            Document document = Jsoup.connect(url).get();
            content = document.select("div.cmn-article_text.a-cf.JSID_key_fonttxt.m-streamer_medium, " +
                    "ul.cmn-announce_personnel.JSID_key_fonttxt.m-streamer_medium");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_maxArticles))){
            maxSize = sharedPreferences.getInt(key, maxSize);
        }else if(key.equals(getString(R.string.pref_onlyFree))){
            onlyFree = sharedPreferences.getBoolean(key, onlyFree);
        }else{
            articlePreferences.put(key, sharedPreferences.getInt(key,
                    articlePreferences.get(key)));
        }
    }

    public static class InnerPreferenceFragment extends AbstractMediumPreferenceFragment{
        private Nikkei medium;

        public InnerPreferenceFragment(final Nikkei medium){
            InnerPreferenceFragment.this.medium = medium;
        }

        @Override
        protected SharedPreferences.OnSharedPreferenceChangeListener getPreferenceChangeListener(){
            return InnerPreferenceFragment.this.medium;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            Log.i(this.getClass().getCanonicalName(), "onCreatePreferences");
            setPreferencesFromResource(R.xml.nikkei_preferences, rootKey);

            medium.setPreferenceManager(getPreferenceManager(),
                    PreferenceManager.getDefaultSharedPreferences(getContext()));
        }
    }

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

        Log.i(this.getClass().getSimpleName(), "maxsize" + maxSize + ", onlyFree" + onlyFree);
        ((SeekBarPreference) this.preferenceManager.findPreference(getString(R.string.pref_maxArticles)))
                .setValue(maxSize);
        ((SwitchPreferenceCompat) this.preferenceManager.findPreference(getString(R.string.pref_onlyFree)))
                .setChecked(onlyFree);
    }
}

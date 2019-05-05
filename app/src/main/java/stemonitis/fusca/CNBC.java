package stemonitis.fusca;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class CNBC extends Medium {
    private static String NAME = "CNBC";
    private static String URL = "http://www.cnbc.com/";
    private static String URL_PREFIX = "http://www.cnbc.com";
    private int maxSize = 5;

    public CNBC(){
        super();
    }

    public CNBC(int maxSize){
        super();
        this.maxSize = maxSize;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void reload() {
        Log.i("CNBC", "reload");
        reloading = true;

        try{
            articles = new ArrayList<>();
            Document document = Jsoup.connect(URL).get();

            Elements elements = document.select("#featured_top > div > div.headline");
            for(Element element : elements){
                Article a = new Article(element.text(),
                        URL_PREFIX + element.select("a").attr("href"));
                a.setContent(formatText(getContentFromURL(a.getUrl())));
                articles.add(a);
                if(articles.size()>=maxSize) break;
            }

            elements = document.select("#pipeline_assetlist_0 > li");
            for(Element element : elements){
                if(articles.size()>=maxSize) break;
                Article a = new Article(element.text(),
                        URL_PREFIX + element.select("a").attr("href"));
                a.setContent(formatText(getContentFromURL(a.getUrl())));
                articles.add(a);
            }

            elements = document.select("#pipeline_assetlist_1 > li.card");
            for(Element element : elements){
                if(articles.size()>=maxSize) break;
                Article a = new Article(element.text(),
                        URL_PREFIX + element.select("a").attr("href"));
                a.setContent(formatText(getContentFromURL(a.getUrl())));
                articles.add(a);
            }

            Log.i("CNBC", "reloaded");
        }catch (IOException e){
            e.printStackTrace();
            Log.i("CNBC", "exception occurred");
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
            content = document.select("div.group-container > div.group");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}

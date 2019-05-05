package stemonitis.fusca;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class TechCrunch extends Headline {
    private static String NAME = "TechCrunch";
    private static String URL = "https://techcrunch.com";
    private static String URL_PREFIX = "";
    private int maxSize = 10;

    public TechCrunch() {
        super();
    }

    public TechCrunch(int maxSize){
        super();
        this.maxSize = maxSize;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void reload() {
        Log.i(NAME, "reload");
        reloading = true;

        try {
            Document document = Jsoup.connect(URL).get();
            Elements elements = document.select("h3.mini-view__item__title");
            articles = new ArrayList<>();
            for(Element element : elements){
                Log.i(NAME, element.text());
                Article a = new Article(element.text(),
                        URL_PREFIX + element.select("a").attr("href"));
                a.setContent(formatText(getContentFromURL(a.getUrl())));
                articles.add(a);
                if(articles.size()>=maxSize) break;
            }
            elements = document.select("h2.post-block__title");
            for(Element element : elements){
                if(articles.size()>=maxSize) break;
                Article a = new Article(element.text(),
                        URL_PREFIX + element.select("a").attr("href"));
                a.setContent(formatText(getContentFromURL(a.getUrl())));
                articles.add(a);
            }
            Log.i(NAME, "reloaded");
        }catch (IOException e){
            e.printStackTrace();
            Log.i(NAME, "exception occurred");
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
            content = document.select("div.article-content");
        }catch (IOException e){
            e.printStackTrace();
        }
        return content;
    }
}

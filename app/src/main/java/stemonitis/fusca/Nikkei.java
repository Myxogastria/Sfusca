package stemonitis.fusca;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class Nikkei extends Headline {
    private static String NAME = "日本経済新聞";
    private static String URL = "http://www.nikkei.com/news/category/";
    private static String URL_PREFIX = "http://www.nikkei.com";
    private int maxSize = 10;

    public Nikkei(){
        super();
    }

    public Nikkei(int maxSize){
        super();
        this.maxSize = maxSize;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void reload(){
        Log.i("Nikkei", "reload");
        reloading = true;

        try{
            Document document = Jsoup.connect(URL).get();
            Elements elements = document.select("h3.m-miM09_title");
            articles = new ArrayList<>();
            for(Element element : elements){
                if(!element.text().contains("［有料会員限定］")) {
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
                if(!element.text().contains("［有料会員限定］")) {
                    Article a = new Article(element.text(),
                            URL_PREFIX + element.select("a").attr("href"));
                    a.setContent(formatText(getContentFromURL(a.getUrl())));
                    articles.add(a);
                }
            }
            Log.i("Nikkei", "reloaded");
        }catch (IOException e){
            e.printStackTrace();
            Log.i("Nikkei", "exception occurred");
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

}

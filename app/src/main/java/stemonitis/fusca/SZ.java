package stemonitis.fusca;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class SZ extends Headline {
    private static String NAME = "Süddeutche Zeitung";
    private static String URL = "http://www.sueddeutsche.de/news";
    private static String URL_PREFIX = "";
    private int maxSize = 5;

    public SZ(){
        super();
    }

    public SZ(int maxSize){
        super();
        this.maxSize = maxSize;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void reload() {
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
            e.printStackTrace();
            Log.i("SZ", "exception occurred");
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
}

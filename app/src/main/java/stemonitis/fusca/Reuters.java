package stemonitis.fusca;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class Reuters extends Medium {
    private static String NAME = "Reuters";
    private static String URL = "https://www.reuters.com/theWire";
    private static String URL_PREFIX = "";
    private int maxSize = 5;

    public Reuters(){
        super();
    }

    public Reuters(int maxSize){
        super();
        this.maxSize = maxSize;
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
}

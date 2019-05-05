package stemonitis.fusca;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class BloombergMarkets extends Headline {
    private static String NAME = "Bloomberg Markets";
    private static String URL = "https://www.bloomberg.com/markets";
    private static String URL_PREFIX = "https://www.bloomberg.com";
    private static int MAX_SIZE = 15;

    public BloombergMarkets(){
        super();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void reload() {
        Log.i("BBGMkt", "reload");
        reloading = true;
        try{
            Document document = Jsoup.connect(URL).get();
            Elements elements = document.select(
                    "a.story-list-story__info__headline-link");
            articles = new ArrayList<>();
            for(Element element : elements){
                Article a = new Article(element.text(),
                        URL_PREFIX + element.attr("href"));
                a.setContent(formatText(getContentFromURL(a.getUrl())));
                articles.add(a);
                Log.i("BBGMkt", "add");
                if(articles.size()>=MAX_SIZE) break;
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            reloading = false;
        }
    }

    private String formatText(Elements content){
        String text = DEFAULT_CONTENT;
        if(content!=null){
            StringBuffer sb = new StringBuffer();
            Elements pElements = content.select(">p");
            for(Element e : pElements){
                Log.i("e", "e");
                if((e.text().length()>0) &&
                        (!(e.text().equals(e.getElementsByTag("a").text())))){
                    Log.i("e", "add");
                    sb.append(e.text());
                    sb.append("\n\n");
                }
            }
            Elements trashline = content.select("div.trashline");
            if(trashline!=null){
                sb.append(trashline.text());
            }
            text = sb.toString();
        }
        return text;
    }

    private Elements getContentFromURL(String url){
        Elements content = null;
        try{
            Document document = Jsoup.connect(url).get();
            content = document.select("body > main > div.transporter-item.current > article > div.content-well-v2 > section > div.body-columns > div > div, " +
                    "body > main > div.transporter-item.current > article > div.content-well > section > div.body-copy.fence-body");
            if(content==null){
                Log.i("content", "null");
                if(document.select("body > main > div.transporter-item.current > article > div.content-well-v2 > section > div.body-columns > div > div")!=null){
                    Log.i("content", "1");
                }else if(document.select("body > main > div.transporter-item.current > article > div.content-well > section > div.body-copy.fence-body")!=null){
                    Log.i("content", "2");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}

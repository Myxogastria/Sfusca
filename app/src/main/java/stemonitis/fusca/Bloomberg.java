package stemonitis.fusca;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class Bloomberg extends Medium {
    private static String NAME = "Bloomberg";
    private static String URL = "https://www.bloomberg.com/asia";
    private static String URL_PREFIX = "https://www.bloomberg.com";
    private static int MAX_SIZE = 15;

    public Bloomberg(){
        super();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void reload() {
        reloading = true;
        try{
            Document document = Jsoup.connect(URL).get();
            Elements elements = document.select("a.single-story-module__headline-link");
            articles = new ArrayList<>();
            for(Element element : elements){
                Article a = new Article(element.text(),
                        URL_PREFIX + element.attr("href"));
                a.setContent(formatText(getContentFromURL(a.getUrl())));
                articles.add(a);
                if(articles.size()>=MAX_SIZE) break;
            }
            elements = document.select("a.story-package-module__story__headline-link");
            for(Element element : elements){
                if(articles.size()>=MAX_SIZE) break;
                Article a = new Article(element.text(),
                        URL_PREFIX + element.attr("href"));
                a.setContent(formatText(getContentFromURL(a.getUrl())));
                articles.add(a);
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
                if((e.text().length()>0) &&
                        (!(e.text().equals(e.getElementsByTag("a").text())))){
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
            content = document.select("body > main > div.transporter-item.current > article > div.content-well-v2 > section > div.body-columns > div > div");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}

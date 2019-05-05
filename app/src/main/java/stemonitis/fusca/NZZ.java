package stemonitis.fusca;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class NZZ extends Medium {
    private static String NAME = "Neue Zürcher Zeitung";
    private static String URL = "https://www.nzz.ch/";
    private static String URL_PREFIX = "https://www.nzz.ch";
    private static int MAX_SIZE = 10;

    public NZZ() {
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
            Elements elements = document.select("a.zzteaser__link");
            articles = new ArrayList<>();
            for(Element element : elements){
                if(!element.select("span.zzteaser__title-name").isEmpty()) {
                    Article a = new Article(element.text(),
                            URL_PREFIX + element.attr("href"));
                    a.setContent(formatText(getContentFromURL(a.getUrl())));
                    articles.add(a);
                }
                if(articles.size()>MAX_SIZE) break;
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
            Elements elements = content.select("main.content__main>p, " +
                    "main.content__main>h1, main.content__main>h2, " +
                    "main.content__main>h3, main.content__main>h4, " +
                    "main.content__main>h5, main.content__main>h6, .infobox__wrapper");
            for(Element e : elements){
                switch (e.tagName()){
                    case "h1":
                    case "h2":
                    case "h3":
                    case "h4":
                    case "h5":
                    case "h6":
                        for(int i = 0; i < Integer.valueOf(e.tagName().substring(1)); i++){
                            sb.append("#");
                        }
                        sb.append(" ");
                    case "p":
                        sb.append(e.text());
                        sb.append("\n\n");
                        break;
                    case "div":
                        sb.append("----------------\n");
                        Element h = e.selectFirst("h1, h2, h3, h4, h5, h6");
                        for(int i = 0; i < Integer.valueOf(h.tagName().substring(1)); i++){
                            sb.append("#");
                        }
                        sb.append(" ");
                        sb.append(h.text());
                        for (Element pElements : e.select("p")) {
                            sb.append("\n\n");
                            sb.append(pElements.text());
                        }
                        sb.append("\n");
                        sb.append("----------------\n");
                        sb.append("\n");
                        break;
                    default:
                        sb.append(e.text());
                        sb.append("\n\n");
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
            content = document.select("body > div.l--main > div > div > article > main");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}

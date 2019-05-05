package stemonitis.fusca;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Article {
    private String title;
    private String url;
    private String content;

    public Article(String title){
        this.title = title;
    }

    public Article(String title, String url){
        this.title = title;
        this.url = url;
    }

    public String getTitle(){
        return title;
    }

    public String getUrl(){
        return url;
    }

    @Override
    public String toString(){
        return title;
    }

    public void setContent(String content){
        this.content = content;
    }

    public String getContent(){
        return content;
    }
}

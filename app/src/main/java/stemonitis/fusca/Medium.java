package stemonitis.fusca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Medium {
    protected static String DEFAULT_CONTENT = "Oops! No content.";
    protected List<Article> articles = new ArrayList<>();
    protected boolean reloading = true;

    public abstract String getName();
    public abstract void reload() throws IOException;

    public List<String> getList(){
        List<String> headlines = new ArrayList<>();
        for(Article a : articles){
            headlines.add(a.getTitle());
        }
        return headlines;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public boolean isReloading(){
        return reloading;
    };
}

package stemonitis.fusca;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AsyncReloader extends AsyncTask<Void, Void, Void>{
    private List<Headline> headlines;

    public AsyncReloader(List<Headline> headlines){
        Log.i("AsyncReloader", "constructor");
        this.headlines = headlines;
    }

    public AsyncReloader(Headline headline){
        headlines = new ArrayList<>();
        headlines.add(headline);
    }

    @Override
    public Void doInBackground(Void... params){
        Log.i("AsyncReloader", "doInBackground");
        for(Headline headline : headlines){
            headline.reload();
        }
        return null;
    }

    @Override
    public void onPostExecute(Void param){
    }
}

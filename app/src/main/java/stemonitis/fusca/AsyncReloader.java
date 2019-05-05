package stemonitis.fusca;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AsyncReloader extends AsyncTask<Void, Void, Void>{
    private List<Medium> media;

    public AsyncReloader(List<Medium> media){
        Log.i("AsyncReloader", "constructor");
        this.media = media;
    }

    public AsyncReloader(Medium medium){
        media = new ArrayList<>();
        media.add(medium);
    }

    @Override
    public Void doInBackground(Void... params){
        Log.i("AsyncReloader", "doInBackground");
        for(Medium medium : media){
            medium.reload();
        }
        return null;
    }

    @Override
    public void onPostExecute(Void param){
    }
}

package stemonitis.fusca;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AsyncReloader extends AsyncTask<Void, String, Void>{
    private List<Medium> media;
    private Context toastContext;

    public AsyncReloader(List<Medium> media){
        Log.i("AsyncReloader", "constructor");
        this.media = media;
    }

    public AsyncReloader(Medium medium){
        media = new ArrayList<>();
        media.add(medium);
    }

    public AsyncReloader withExceptionToast(Context context){
        toastContext = context;
        return this;
    }

    @Override
    protected Void doInBackground(Void... params){
        Log.i("AsyncReloader", "doInBackground");
        for(final Medium medium : media){
            try{
                medium.reload();
            }catch (IOException e){
                e.printStackTrace();
                publishProgress(toastContext.getString(
                        R.string.tst_reload_err, medium.getName(), e));
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... msg){
        Log.i(this.getClass().getSimpleName(), "onProgressUpdate");
        if (toastContext != null){
            Log.i(this.getClass().getSimpleName(), "showToast");
            Toast.makeText(toastContext, msg[0], Toast.LENGTH_SHORT).show();
        }
    }

}

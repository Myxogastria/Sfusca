package stemonitis.fusca;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class MediaSortActivity extends AppCompatActivity {
    private static final int MAX_MEDIA = 5;
    private static final int START_SETTINGS = 123;

    private List<Medium> media;
    private boolean mediaChanged;

    private RecyclerView.Adapter<DragSortViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_sort);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MediaSortActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(MediaSortActivity.this, layoutManager.getOrientation()));

        media = MediaFactory.resumeMedia(
                PreferenceManager.getDefaultSharedPreferences(MediaSortActivity.this));
        mediaChanged = false;

        adapter = new DragSortAdapter(media, this);
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = viewHolder1.getAdapterPosition();
                Log.i("onMove", "from " + fromPos + " to " + toPos);
                Collections.rotate(media.subList(Math.min(fromPos, toPos), Math.max(fromPos, toPos)+1), Integer.signum(fromPos - toPos));
                adapter.notifyItemMoved(fromPos, toPos);
                mediaChanged = true;
                for (Medium medium : media){
                    Log.i("onActivityResult", "profile:" + medium.getProfileString() + ", id:" + medium.getId());
                }
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int fromPos = viewHolder.getAdapterPosition();
                media.remove(fromPos);
                adapter.notifyItemRemoved(fromPos);
                mediaChanged = true;
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mediasort, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                if (mediaChanged){
                    new SaveConfirmDialogFragment().show(getSupportFragmentManager(), "SaveConfirmDialogFragment");
                }else{
                    finish();
                }
                break;
            case R.id.menu_add:
                if (media.size() < MAX_MEDIA){
                    new AddDialogFragment().show(getSupportFragmentManager(), "AddDialogFragment");
                }else{
                    Toast.makeText(MediaSortActivity.this, getString(R.string.tst_media_full), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SaveConfirmDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceStates){
            return new AlertDialog.Builder(getActivity())
                    .setMessage(getContext().getString(R.string.dialog_save_confirm_msg))
                    .setPositiveButton(getContext().getString(R.string.dialog_save_confirm_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((MediaSortActivity) getActivity()).applyChangeAndFinish();
                        }
                    })
                    .setNegativeButton(getContext().getString(R.string.dialog_save_confirm_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finish();
                        }
                    })
                    .setNeutralButton(getContext().getString(R.string.dialog_save_confirm_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create();
        }
    }

    public void applyChangeAndFinish(){
        if (media.isEmpty()){
            Toast.makeText(MediaSortActivity.this, getString(R.string.tst_media_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        MediaFactory.saveMedia(media,
                PreferenceManager.getDefaultSharedPreferences(MediaSortActivity.this));

        finish();
    }

    public void addMedium(String mediumName){
        try{
            Medium medium = MediaFactory.createMedium(mediumName, getNextId());
            Log.i("addMedium", medium.getNameInSettings());
            media.add(medium);
            adapter.notifyItemInserted(media.size()-1);
            mediaChanged = true;
        }catch (InvalidIdException e){
            Log.i("addMedium", "InvalidIdException");
        }
    }

    private int getNextId() throws InvalidIdException {
        for(int i=0; i<MAX_MEDIA+1; i++){
            boolean isUsed = false;
            for (Medium medium : media){
                isUsed |= (medium.getId() == i);
            }
            if (!isUsed){
                return i;
            }
        }
        throw new InvalidIdException();
    }

    public void startSettings(int position){
        Intent intent = new Intent(MediaSortActivity.this, SettingsActivity.class);
        intent.putExtra(getString(R.string.intent_settingsType), SettingsActivity.MEDIUM);
        intent.putExtra(getString(R.string.intent_mediumOrder), MediaFactory.makeOrderString(media.get(position)));
        intent.putExtra(getString(R.string.intent_mediumId), media.get(position).getId());
        Log.i("startSettings", "profile:" + media.get(position).getProfileString() + ", id:" + media.get(position).getId());
        startActivityForResult(intent, START_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == START_SETTINGS){
            int position = -1;
            if(data != null){
                String order = data.getStringExtra(getString(R.string.intent_mediumOrder));
                int id = data.getIntExtra(getString(R.string.intent_mediumId), -1);
                Log.i("onActivityResult", "profile:" + order + ", id:" + id);
                for(int i = 0; i < media.size(); i++){
                    if (media.get(i).getId() == id){
                        media.set(i, MediaFactory.createMedium(order, id));
                        position = i;
                        break;
                    }
                }
            }
            for (Medium medium : media){
                Log.i("onActivityResult", "profile:" + medium.getProfileString() + ", id:" + medium.getId());
            }
            if (position >= 0){
                adapter.notifyItemChanged(position);
                mediaChanged = true;
            }
        }
    }

    public class InvalidIdException extends Exception{

    }
}

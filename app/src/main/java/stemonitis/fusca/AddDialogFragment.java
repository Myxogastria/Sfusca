package stemonitis.fusca;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class AddDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        int defaultItem = 0; // default checked item
        final List<Integer> checkedItems = new ArrayList<>();
        checkedItems.add(defaultItem);
        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.dialog_title_add))
                .setSingleChoiceItems(MediaFactory.MENU_IN_DIALOG, defaultItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkedItems.clear();
                        checkedItems.add(which);
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!checkedItems.isEmpty()) {
                            Log.d("checkedItem:", "" + checkedItems.get(0));
                            if(getActivity() instanceof MediaSortActivity){
                                ((MediaSortActivity) getActivity()).addMedium(MediaFactory.MENU[checkedItems.get(0)]);
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", null).create();
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }
}

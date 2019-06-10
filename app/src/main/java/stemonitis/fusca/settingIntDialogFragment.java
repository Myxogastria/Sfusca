package stemonitis.fusca;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.View;

public class settingIntDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstaceState){
        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_setting_int, null);
        return new AlertDialog.Builder(getActivity()).setView(view).create();
    }

    @Override
    public void onPause(){
        super.onPause();
        dismiss();
    }
}

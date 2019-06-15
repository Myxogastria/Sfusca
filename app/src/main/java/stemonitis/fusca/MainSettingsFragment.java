package stemonitis.fusca;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class MainSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey);
//        findPreference("dragsort").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                switch (preference.getKey()){
//                    case "dragsort":
//                        Intent intent = new Intent(getActivity(), DragSortActivity.class);
//                        startActivity(intent);
//                }
//                return false;
//            }
//        });
    }
}

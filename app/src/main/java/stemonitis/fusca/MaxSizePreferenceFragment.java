package stemonitis.fusca;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class MaxSizePreferenceFragment extends AbstractMediumPreferenceFragment {
    private Medium medium;

    public MaxSizePreferenceFragment(Medium medium){
        this.medium = medium;
    }

    @Override
    protected SharedPreferences.OnSharedPreferenceChangeListener getPreferenceChangeListener() {
        return medium;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_medium_maxsize, rootKey);
        addPreferencesFromResource(R.xml.preferences_article);

        medium.setPreferenceManager(getPreferenceManager(),
                PreferenceManager.getDefaultSharedPreferences(getContext()));
    }
}

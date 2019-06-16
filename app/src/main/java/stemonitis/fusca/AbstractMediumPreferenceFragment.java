package stemonitis.fusca;

import android.content.SharedPreferences;

import androidx.preference.PreferenceFragmentCompat;

public abstract class AbstractMediumPreferenceFragment extends PreferenceFragmentCompat {
    abstract protected SharedPreferences.OnSharedPreferenceChangeListener getPreferenceChangeListener();

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(
                getPreferenceChangeListener()
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                getPreferenceChangeListener()
        );
    }
}

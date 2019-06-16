package stemonitis.fusca;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {
    public static final int MAIN = 0;
    public static final int MEDIUM = 1;

    private int settingsType;

    private Medium medium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        settingsType = intent.getIntExtra(getString(R.string.intent_settingsType), -1);

        setContentView(R.layout.activity_settings);
        PreferenceFragmentCompat preferenceFragment = null;
        switch (settingsType){
            case MAIN:
                preferenceFragment = new MainSettingsFragment();
                break;
            case MEDIUM:
                Log.i("SettingsActivity", "onCreate, case medium");
                String order = intent.getStringExtra(getString(R.string.intent_mediumOrder));
                int id = intent.getIntExtra(getString(R.string.intent_mediumId), -1);
                if((order!=null)&&(id>=0)){
                    // media must be given
                    medium = MediaFactory.createMedium(order, id);
                    preferenceFragment = medium.getSettingsFragment();
                }else{
                    finish();
                }
        }
        if (preferenceFragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, preferenceFragment)
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home){
            Log.i("SettingsActivity_Home", "settingsType:" + settingsType);
            if(settingsType == MEDIUM){
                Log.i("SettingsActivity_Home", "profile:" + medium.getProfileString() + ", id:" + medium.getId());
                Intent intent = new Intent();
                intent.putExtra(getString(R.string.intent_mediumOrder),
                        MediaFactory.makeOrderString(medium));
                intent.putExtra(getString(R.string.intent_mediumId), medium.getId());
                setResult(RESULT_OK, intent);
            }
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
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
//            case "medium":
//                Log.i("SettingsActivity", "onCreate, case medium");
//                String profile = intent.getStringExtra("medium_profile");
//                int id = intent.getIntExtra("medium_id", -1);
//                if((profile!=null)&&(id>=0)){
//                    // media must be given
//                    medium = MediumFactory.createMedium(profile, id);
//                    preferenceFragment = medium.getSettingsFragment();
//                }else{
//                    finish();
//                }
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
//            if(settingsType.equals("medium")){
//                Log.i("SettingsActivity_Home", "profile:" + medium.getProfileString() + ", id:" + medium.getId());
//                Intent intent = new Intent();
//                intent.putExtra("medium_profile", medium.getProfileString());
//                intent.putExtra("medium_id", medium.getId());
//                setResult(RESULT_OK, intent);
//            }
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
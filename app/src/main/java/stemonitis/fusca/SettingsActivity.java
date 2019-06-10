package stemonitis.fusca;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {
    private String settingsType;

    private Medium medium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsType = "";
        Intent intent = getIntent();
        if(intent != null){
            settingsType = intent.getStringExtra("settingsType");
        }

//        setContentView(R.layout.settings_activity);
//        PreferenceFragmentCompat preferenceFragment = null;
//        switch (settingsType){
//            case "root":
//                preferenceFragment = new SettingsFragment();
//                break;
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
//        }
//        if (preferenceFragment != null){
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.settings, preferenceFragment)
//                    .commit();
//        }
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
package stemonitis.fusca;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsActivityX extends AppCompatActivity {
    private List<Map<String, String>> settingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle(getString(R.string.menu_settings));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView settingsListView = findViewById(R.id.lvSettings);
        settingsList = new ArrayList<>();

        settingsList.add(new Setting(getString(R.string.settings_media), "").getMap());
        settingsList.add(new Setting(getString(R.string.settings_autoScrollDelay),
                15000, R.string.value_autoScrollDelay).getMap());
        settingsList.add(new Setting(getString(R.string.settings_SCROLL_DURATION),
                1500, R.string.value_SCROLL_DURATION).getMap());
        settingsList.add(new Setting(getString(R.string.settings_scrollBy),
                1, R.string.value_scrollBy).getMap());
        settingsList.add(new Setting(getString(R.string.settings_media_font_size),
                60, R.string.value_media_font_size).getMap());
        settingsList.add(new Setting(getString(R.string.settings_headline_font_size),
                45, R.string.value_headline_font_size).getMap());

        String[] from = {"item", "value"};
        int[] to = {android.R.id.text1, android.R.id.text2};
        settingsListView.setAdapter(new SimpleAdapter(SettingsActivityX.this,
                settingsList, android.R.layout.simple_list_item_2, from, to));

        settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new settingIntDialogFragment().show(getSupportFragmentManager(), "settingInt");
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    protected class Setting{
        static final int TYPE_STRING = 0;
        static final int TYPE_INT = 1;
        private int type;

        private String name;
        private String stringValue;
        private int intValue;
        private int formatStringId;

        Setting(String name, String value){
            type = TYPE_STRING;
            Setting.this.name = name;
            Setting.this.stringValue = value;
        }

        Setting(String name, int value, int formatStringId){
            type = TYPE_INT;
            Setting.this.name = name;
            Setting.this.intValue = value;
            Setting.this.formatStringId = formatStringId;
        }

        Map<String, String> getMap(){
            Map<String, String> map = new HashMap<>();
            map.put("item", name);
            switch (type){
                case TYPE_STRING:
                    map.put("value", stringValue);
                    break;
                case TYPE_INT:
                    map.put("value", getString(formatStringId, intValue));
                    break;
            }
            return map;
        }

    }
}

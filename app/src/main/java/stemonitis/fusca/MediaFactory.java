package stemonitis.fusca;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MediaFactory {
    public static final String[] MENU_IN_DIALOG = {
            "日本経済新聞",
            "Reuters",
            "Süddeutche Zeitung",
            "TechCrunch"
    };
    public static final String[] MENU = {
            Nikkei.class.getSimpleName(),
            Reuters.class.getSimpleName(),
            SZ.class.getSimpleName(),
            TechCrunch.class.getSimpleName()};

    private MediaFactory(){
        throw new AssertionError();
    };

    public static Medium createMedium(String order, int id){
        String mediumName = "";
        String profileString = "";

        if(order != null){
            if (order.contains(":")){
                int firstColon = order.indexOf(":");
                mediumName = order.substring(0, firstColon);
                profileString = order.substring(firstColon+1);
            }else{
                mediumName = order;
            }
        }

        if(mediumName.equals(Nikkei.class.getSimpleName())) {
            return new Nikkei(id, profileString);
        }else if(mediumName.equals(Reuters.class.getSimpleName())) {
            return new Reuters(id, profileString);
        }else if(mediumName.equals(SZ.class.getSimpleName())) {
            return new SZ(id, profileString);
        }else if(mediumName.equals(TechCrunch.class.getSimpleName())) {
            return new TechCrunch(id, profileString);
        }else{
                return null;
        }
    }

    public static List<Medium> createDefaultMedia(){
        List<Medium> media = new ArrayList<>();
        media.add(new Nikkei(0, ""));
        media.add(new Reuters(1, ""));
        media.add(new SZ(2, ""));
        media.add(new TechCrunch(3, ""));
        return media;
    }

    public static String makeOrderString(Medium medium){
        return medium.getClass().getSimpleName() + ":" + medium.getProfileString();
    }

    public static void saveMedia(List<Medium> media, SharedPreferences sharedPreferences){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Map<String, ?> allEntries = sharedPreferences.getAll();

        StringBuffer idStringBuffer = new StringBuffer();
        for (Medium medium : media){
            idStringBuffer.append(medium.getId()+",");
            editor.putString("profile" + medium.getId(),
                    medium.getClass().getSimpleName() + ":" + medium.getProfileString());
        }
        idStringBuffer.deleteCharAt(idStringBuffer.length()-1);
        editor.putString("idArray", idStringBuffer.toString());

        for (Map.Entry<String, ?> entry : allEntries.entrySet()){
            if (entry.getKey().matches( "profile^[" +
                    idStringBuffer.toString().replace(",", "|") + "]")){
                editor.remove(entry.getKey());
            }
        }

        editor.apply();
    }

    public static List<Medium> resumeMedia(SharedPreferences sharedPreferences){
        List<Medium> media;

        String idArrayString = sharedPreferences.getString("idArray", null);
        if(idArrayString!=null){
            Log.i("idArrayString", idArrayString);
        }else{
            Log.i("idArrayString", "null");
        }
        if((idArrayString != null)&&(idArrayString.matches("[0-9][0-9|,]*[0-9]"))){
            media = new ArrayList<>();
            for (String idString : idArrayString.split(",")) {
                int id = Integer.parseInt(idString);
                media.add(MediaFactory.createMedium(
                        sharedPreferences.getString("profile" + id, ""), id));
            }
        }else{
            media = MediaFactory.createDefaultMedia();
        }

        return media;
    }

}

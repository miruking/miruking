package com.example.miruking.utils;

import android.content.Context;
import org.json.JSONObject;

import java.io.*;

public class ProfileManager {
    private static final String FILE_NAME = "profile.json";

    public static void saveProfile(Context context, String nickname, int exp, int level) {
        try {
            JSONObject json = new JSONObject();
            json.put("nickname", nickname);
            json.put("exp", exp);
            json.put("level", level);

            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(json.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONObject loadProfile(Context context) {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (!file.exists()) return null;

            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();

            return new JSONObject(builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
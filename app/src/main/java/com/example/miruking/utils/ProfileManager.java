package com.example.miruking.utils;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import org.json.JSONObject;

import java.io.*;

public class ProfileManager {
    private static final String FILE_NAME = "profile.json";

    public static void saveProfile(Context context, int xp) {
        File file = new File(context.getFilesDir(), "profile.txt");

        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(String.valueOf(xp));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int loadProfile(Context context) {
        File file = new File(context.getFilesDir(), "profile.txt");

        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("0"); // 초기 XP 0
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int currentXp = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            currentXp = Integer.parseInt(line.trim());
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace(); // 예외는 무시하고 기본값 유지
            return 0;
        }

        return currentXp;
    }
}
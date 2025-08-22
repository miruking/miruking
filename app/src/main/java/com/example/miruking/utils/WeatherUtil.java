package com.example.miruking.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// 날씨 유틸리티 클래스
public class WeatherUtil {

    public interface WeatherCallback {
        void onResult(String weatherMessage);
    }

    // ✅ 비동기 날씨 가져오기 (기본값 서울시청 좌표 포함)
    public static void getWeatherMessageAsync(Context context, WeatherCallback callback) {
        new Thread(() -> {
            try {
                double[] location = getUserLocation(context);
                if (location == null) {
                    location = new double[]{37.5665, 126.9780}; // 기본값: 서울시청
                }

                double lat = location[0];
                double lon = location[1];

                String apiUrl = "https://wttr.in/" + lat + "," + lon + "?format=j1";
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    Log.e("WeatherUtil", "날씨 API 응답 오류 코드: " + responseCode);
                    callback.onResult("날씨 정보를 가져올 수 없습니다.");
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(result.toString());
                JSONObject currentCondition = json.getJSONArray("current_condition").getJSONObject(0);
                String weatherDesc = currentCondition.getJSONArray("weatherDesc")
                        .getJSONObject(0).getString("value");
                int tempC = Integer.parseInt(currentCondition.getString("temp_C"));

                String message = createWeatherMessage(weatherDesc, tempC);
                callback.onResult(message);

            } catch (Exception e) {
                Log.e("WeatherUtil", "날씨 가져오기 실패", e);
                callback.onResult("날씨 정보를 가져올 수 없습니다.");
            }
        }).start();
    }

    // 위치 가져오기
    private static double[] getUserLocation(Context context) {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) return null;

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (location != null) {
                return new double[]{location.getLatitude(), location.getLongitude()};
            }
        } catch (Exception e) {
            Log.e("WeatherUtil", "위치 가져오기 실패", e);
        }
        return null;
    }

    // 날씨 멘트 생성
    private static String createWeatherMessage(String desc, int tempC) {
        StringBuilder msg = new StringBuilder(desc + " / " + tempC + "°C");
        String lowerDesc = desc.toLowerCase();

        // 비 관련
        if (lowerDesc.contains("rain") || lowerDesc.contains("shower") || lowerDesc.contains("drizzle")) {
            msg.append(" ☔ 우산을 챙겨주세요.");
        }
        // 눈 관련
        else if (lowerDesc.contains("snow") || lowerDesc.contains("sleet")) {
            msg.append(" ❄ 장갑을 챙겨주세요.");
        }
        // 천둥/번개
        else if (lowerDesc.contains("thunder") || lowerDesc.contains("storm")) {
            msg.append(" ⚡ 외출 시 주의하세요.");
        }
        // 흐림/구름
        else if (lowerDesc.contains("cloud") || lowerDesc.contains("overcast")) {
            msg.append(" ☁️ 흐린 날씨입니다.");
        }
        // 안개/박무
        else if (lowerDesc.contains("fog") || lowerDesc.contains("mist") || lowerDesc.contains("haze")) {
            msg.append(" 🌫️ 시야가 좋지 않아요. 운전 시 주의하세요.");
        }
        // 바람
        else if (lowerDesc.contains("wind") || lowerDesc.contains("breeze")) {
            msg.append(" 🌬️ 바람이 강할 수 있어요.");
        }
        // 온도 기준 안내
        if (tempC >= 28) {
            msg.append(" 🌞 양산을 챙겨주세요.");
        } else if (tempC <= 5) {
            msg.append(" 🧥 따뜻하게 입어주세요.");
        }

        return msg.toString();
    }

}

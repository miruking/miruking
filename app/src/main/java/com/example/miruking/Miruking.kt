package com.example.miruking
import android.app.Application
import com.kakao.sdk.common.KakaoSdk


class Miruking : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "55da22c7862c64d5a67c41d12b26edd7")
    }
}
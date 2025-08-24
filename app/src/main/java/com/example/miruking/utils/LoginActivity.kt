package com.example.miruking.utils

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient

import com.example.miruking.R
import com.example.miruking.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        result = findViewById(R.id.tvResult)
        val loginBtn: Button = findViewById(R.id.btnKakaoLogin)

        // 이미 토큰이 있으면 곧바로 메인으로
        UserApiClient.instance.accessTokenInfo { _, error ->
            if (error == null) goMain()
        }

        loginBtn.setOnClickListener { startKakaoLogin() }
    }

    private fun startKakaoLogin() {
        // 카카오톡 앱 가능 시 우선 시도
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token: OAuthToken?, error: Throwable? ->
                if (error != null) {
                    // 실패 시 카카오계정(웹)으로 폴백
                    loginWithKakaoAccount()
                } else {
                    goMain()
                }
            }
        } else {
            loginWithKakaoAccount()
        }
    }

    private fun loginWithKakaoAccount() {
        UserApiClient.instance.loginWithKakaoAccount(this) { token: OAuthToken?, error: Throwable? ->
            if (error != null) {
                result.text = "로그인 실패: ${error.message}"
            } else {
                goMain()
            }
        }
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
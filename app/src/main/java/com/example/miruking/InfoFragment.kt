package com.example.miruking

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.miruking.utils.LoginActivity
import com.kakao.sdk.user.UserApiClient

class InfoFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var ivProfile: ImageView
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_info, container, false)

        tvName = view.findViewById(R.id.tvName)
        ivProfile = view.findViewById(R.id.ivProfile)
        btnLogout = view.findViewById(R.id.btnLogout)

        UserApiClient.instance.accessTokenInfo { _, error ->
            if (error != null) {
                goLogin()
            } else {
                fetchName()
            }
        }

        btnLogout.setOnClickListener {
            UserApiClient.instance.logout { e ->
                if (e != null) {
                    Toast.makeText(context, "로그아웃 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "로그아웃 완료", Toast.LENGTH_SHORT).show()
                    goLogin(clearTask = true)
                }
            }
        }

        return view
    }

    private fun fetchName() {
        UserApiClient.instance.me { user, err ->
            if (err != null) {
                tvName.text = "(이름 조회 실패)"
                return@me
            }

            val nickname = user?.kakaoAccount?.profile?.nickname ?: "(이름 없음)"
            tvName.text = nickname

            val profileUrl = user?.kakaoAccount?.profile?.profileImageUrl
            if (!profileUrl.isNullOrBlank()) {
                Glide.with(requireContext())
                    .load(profileUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_default_profile)
                    .into(ivProfile)
            }
        }
    }

    private fun goLogin(clearTask: Boolean = false) {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        if (clearTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            requireActivity().finish()
        }
        startActivity(intent)
        if (!clearTask) requireActivity().finish()
    }
}
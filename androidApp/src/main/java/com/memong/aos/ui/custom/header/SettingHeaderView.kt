package com.memong.aos.ui.custom.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.memong.aos.R
import com.memong.aos.databinding.ViewSetHeaderBinding

internal class SettingHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding: ViewSetHeaderBinding =
        ViewSetHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    enum class SetHeaderMode(@StringRes val titleResId: Int) {
        DEFAULT(R.string.haru_set_default),
        MEMO(R.string.haru_set_memo),
        BACKUP(R.string.haru_set_backup),
        PASSWORD(R.string.haru_set_password),
        PASSWORD_REGI(R.string.haru_password_regi),
        PASSWORD_CHANGE(R.string.haru_password_change),
        PASSWORD_RESET(R.string.haru_password_reset),
        PASSWORD_CHECK(R.string.haru_password_check),
        PASSWORD_CHECK_SEARCH(R.string.haru_password_check_search),
        PASSWORD_LOCK(R.string.haru_password_locked),
        PASSWORD_UNLOCK(R.string.haru_password_unlocked),
        PASSWORD_DELETE(R.string.haru_password_delete),
        TAG(R.string.haru_set_tag),
        SERVICE_INFO(R.string.haru_service_info),
        NOTICE(R.string.haru_notice),
        HELP(R.string.haru_help),
        GUIDE_W(R.string.haru_guide_w),
        GUIDE_E(R.string.haru_guide_e),
        GUIDE_B(R.string.haru_guide_b),
        GUIDE_F(R.string.haru_guide_f),
        TERMS(R.string.haru_terms),
        TRASH(R.string.haru_trash)
    }

    fun setMode(mode: SetHeaderMode) {
        binding.txSet.text = context.getString(mode.titleResId)
    }

    fun setOnBackClickListener(listener: () -> Unit) {
        binding.btnSetBack.setOnClickListener { listener() }
    }

    fun onDestroy() {
        binding.btnSetBack.setOnClickListener(null)
        this.removeAllViews()
    }

}

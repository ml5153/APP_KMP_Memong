package com.memong.aos.ui.custom.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.memong.aos.R
import com.memong.aos.data.extension.isUserInKorea
import com.memong.aos.data.utils.Util
import com.memong.aos.databinding.DialogMemoAiCustomBinding
import com.memong.aos.ui.adapter.ChipAdapter
import com.memong.aos.ui.adapter.itemDecoration.ChipSpacingDecoration

internal class MemoAICustomDialog(context: Context) : Dialog(context) {

    private val binding: DialogMemoAiCustomBinding =
        DialogMemoAiCustomBinding.inflate(LayoutInflater.from(context))

    private var onChipClick: ((String) -> Unit)? = null

    private val allChipItems by lazy {
        listOf(
            context.getString(R.string.haru_chip_today_weather_title),
            context.getString(R.string.haru_chip_today_news_title),
            context.getString(R.string.haru_chip_today_lunch_title),
            context.getString(R.string.haru_chip_today_dinner_title),
            context.getString(R.string.haru_chip_today_stock_title),
            context.getString(R.string.haru_chip_today_exchange_rate_title),
            context.getString(R.string.haru_chip_today_sports_title),
            context.getString(R.string.haru_chip_today_issue_title),
            context.getString(R.string.haru_chip_today_schedule_title),
            context.getString(R.string.haru_chip_today_movie_title),
            context.getString(R.string.haru_chip_today_drama_title),
            context.getString(R.string.haru_chip_today_book_title),
            context.getString(R.string.haru_chip_today_music_title),
            context.getString(R.string.haru_chip_today_health_tip_title),
            context.getString(R.string.haru_chip_today_diet_tip_title),
            context.getString(R.string.haru_chip_today_workout_title),
            context.getString(R.string.haru_chip_today_trip_title),
            context.getString(R.string.haru_chip_today_recipe_title),
            context.getString(R.string.haru_chip_today_tip_title),
            context.getString(R.string.haru_chip_today_english_title),
            context.getString(R.string.haru_chip_today_japanese_title),
            context.getString(R.string.haru_chip_today_chinese_title),
            context.getString(R.string.haru_chip_today_history_title),
            context.getString(R.string.haru_chip_today_quote_title),
            context.getString(R.string.haru_chip_today_proverb_title),
            context.getString(R.string.haru_chip_today_politics_title),
            context.getString(R.string.haru_chip_today_economy_title),
            context.getString(R.string.haru_chip_today_society_title),
            context.getString(R.string.haru_chip_today_international_title),
            context.getString(R.string.haru_chip_today_science_title),
            context.getString(R.string.haru_chip_today_it_title),
            context.getString(R.string.haru_chip_today_environment_title),
            context.getString(R.string.haru_chip_today_indicator_title),
            context.getString(R.string.haru_chip_today_real_estate_title),
            context.getString(R.string.haru_chip_today_oil_title),
            context.getString(R.string.haru_chip_today_gold_title),
            context.getString(R.string.haru_chip_today_coin_title),
            context.getString(R.string.haru_chip_today_fashion_title),
            context.getString(R.string.haru_chip_today_shopping_title),
            context.getString(R.string.haru_chip_today_traffic_title),
            context.getString(R.string.haru_chip_today_fine_dust_title)
        )
    }


    companion object {
        const val NAME = "MemoAICustomDialog"
    }

    private var onCancelAction: (() -> Unit)? = null
    private var onConfirmAction: ((String) -> Unit)? = null

    init {
        setContentView(binding.root)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Chip RecyclerView 세팅
        val chipItems = getTodayChipItems()

        binding.rvChips.isVisible = context.isUserInKorea()
        binding.rvChips.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ChipAdapter(chipItems) { selected ->
                onChipClick?.invoke(selected)
            }
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER

            // 간격 주기
            val spacing = Util.dpToPx(context, 8)
            addItemDecoration(ChipSpacingDecoration(spacing))
        }

        // 버튼 리스너
        binding.btnCancel.setOnClickListener {
            binding.btnCancel.isEnabled = false
            onCancelAction?.invoke()
        }
        binding.btnConfirm.setOnClickListener {
            binding.btnConfirm.isEnabled = false
            val q = binding.etQuestion.text?.toString().orEmpty().trim()
            onConfirmAction?.invoke(q)
        }

        // 입력 변화에 따라 Confirm 버튼 활성/비활성
        binding.etQuestion.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnConfirm.isEnabled = !s.isNullOrBlank()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.btnConfirm.isEnabled = false
    }

    // =================== Public API ===================

    fun setTitle(title: String) {
        binding.tvTitle.text = title
        binding.tvTitle.visibility = if (title.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setMessage(msg: String) {
        binding.tvMessage.text = msg
        binding.tvMessage.visibility = if (msg.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setBottomSubMessage(sub: String) {
        binding.tvBottomSub.text = sub
        binding.tvBottomSub.visibility = if (sub.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setButtons(
        cancelText: String,
        confirmText: String,
        onCancel: (() -> Unit)? = null,
        onConfirm: ((String) -> Unit)? = null
    ) {
        binding.btnCancel.text = cancelText
        binding.btnConfirm.text = confirmText
        this.onCancelAction = onCancel
        this.onConfirmAction = onConfirm
        binding.btnCancel.isEnabled = true
        binding.btnConfirm.isEnabled = binding.etQuestion.text?.isNotBlank() == true
    }

    fun setOnChipClick(listener: (String) -> Unit) {
        this.onChipClick = listener
    }

    /** 버튼 1개만 쓰고 싶을 때 (Cancel 숨김) */
    fun showSingleConfirmOnly(confirmText: String, onConfirm: ((String) -> Unit)?) {
        binding.btnCancel.visibility = View.GONE
        (binding.buttonContainer as ViewGroup).apply {
            // 간격 뷰도 숨김
            if (childCount >= 3) getChildAt(1).visibility = View.GONE
        }
        setButtons(
            cancelText = "",
            confirmText = confirmText,
            onCancel = null,
            onConfirm = onConfirm
        )
    }

    private fun getTodayChipItems(): List<String> {
        val cal = java.util.Calendar.getInstance()
        val year = cal.get(java.util.Calendar.YEAR)
        val month = cal.get(java.util.Calendar.MONTH)   // 0-based
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)

        // 날짜를 시드값으로 변환 (YYYYMMDD 같은 방식)
        val seed = year * 10_000 + (month + 1) * 100 + day
        val random = java.util.Random(seed.toLong())

        return allChipItems.shuffled(random).take(5)
    }

    fun onDestroy() {
        binding.btnCancel.setOnClickListener(null)
        binding.btnConfirm.setOnClickListener(null)
        dismiss()
    }
}

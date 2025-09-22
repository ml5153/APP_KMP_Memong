package com.avatye.haru.memo.ui.custom.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avatye.haru.memo.data.utils.Util
import com.avatye.haru.memo.databinding.DialogMemoAiCustomBinding
import com.avatye.haru.memo.ui.adapter.ChipAdapter
import com.avatye.haru.memo.ui.adapter.itemDecoration.ChipSpacingDecoration

internal class MemoAICustomDialog(context: Context) : Dialog(context) {

    private val binding: DialogMemoAiCustomBinding =
        DialogMemoAiCustomBinding.inflate(LayoutInflater.from(context))

    private var onChipClick: ((String) -> Unit)? = null

    private val allChipItems = listOf(
        "오늘의 날씨", "오늘의 뉴스", "오늘 점심 뭐 먹을까?",
        "오늘 저녁 뭐 먹을까?", "오늘의 주식 시장은?", "오늘의 환율은?",
        "오늘의 스포츠 경기 결과는?", "오늘의 주요 이슈는?", "오늘의 일정 추천은?",
        "오늘의 영화 추천", "오늘의 드라마 추천", "오늘의 책 추천",
        "오늘의 음악 추천", "오늘의 건강 팁", "오늘의 다이어트 팁",
        "오늘의 운동 루틴", "오늘의 여행지 추천", "오늘의 레시피 추천", "오늘의 꿀팁",
        "오늘의 영어 회화", "오늘의 일본어 표현", "오늘의 중국어 표현",
        "오늘의 역사 사건", "오늘의 명언", "오늘의 속담",
        "오늘의 정치 뉴스", "오늘의 경제 뉴스", "오늘의 사회 뉴스",
        "오늘의 국제 뉴스", "오늘의 과학 뉴스", "오늘의 IT 뉴스",
        "오늘의 환경 뉴스", "오늘의 경제 지표", "오늘의 부동산 뉴스",
        "오늘의 유가", "오늘의 금값", "오늘의 코인 시세",
        "요즘 패션 트렌드", "오늘의 쇼핑 추천",
        "오늘의 교통 상황", "오늘의 미세먼지 정보"
    )

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

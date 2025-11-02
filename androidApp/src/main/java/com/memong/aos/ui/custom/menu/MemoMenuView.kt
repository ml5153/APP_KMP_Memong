package com.memong.aos.ui.custom.menu

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import com.memong.aos.R
import com.memong.aos.data.entity.MemoEntity
import com.memong.aos.data.utils.RemoteConfigUtil
import com.memong.aos.data.utils.Util.Companion.dpToPx
import com.memong.aos.databinding.ViewMemoMenuBinding
import com.memong.aos.ui.adapter.MemoMenuAdapter

internal class MemoMenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var popupWindow: PopupWindow? = null
    private var itemClickListener: ((Int, String) -> Unit)? = null

    private var currentMemoItem: MemoEntity? = null

    enum class Mode {
        OPTIONS, MEMO_FONT_SIZE, DETAIL
    }

    private var mode: Mode = Mode.OPTIONS

    fun show(anchor: View, mode: Mode, selectedLabel: String? = null) {
        this.mode = mode

        val inflater = LayoutInflater.from(context)
        val contentView = inflater.inflate(
            R.layout.view_memo_menu,
            null,
            false
        ) as ViewGroup

        val binding = ViewMemoMenuBinding.bind(contentView)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        val items = getItemsForMode(mode)

        val adapter = MemoMenuAdapter(
            context = context,
            items = items,
            selectedLabel = selectedLabel,
            mode = mode
        ) { position, label ->
            itemClickListener?.invoke(position, label)
            popupWindow?.dismiss()
        }

        binding.recyclerView.adapter = adapter

        popupWindow = PopupWindow(
            contentView,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            elevation = 6f
        }

        anchor.post {
            val location = IntArray(2)
            anchor.getLocationOnScreen(location)
            val anchorRect = Rect(
                location[0],
                location[1],
                location[0] + anchor.width,
                location[1] + anchor.height
            )

            contentView.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            val popupWidth = contentView.measuredWidth
            val screenWidth = context.resources.displayMetrics.widthPixels
            val maxX = screenWidth - popupWidth - dpToPx(context, 12)
            val finalX = location[0].coerceAtMost(maxX)
            val finalY = anchorRect.top

            popupWindow?.showAtLocation(anchor, Gravity.NO_GRAVITY, finalX, finalY)
        }
    }

    private fun getItemsForMode(mode: Mode): List<String> {
        return when (mode) {
            Mode.OPTIONS -> buildList {
                add(context.getString(R.string.haru_menu_options_sort))
//                add(context.getString(R.string.haru_menu_options_edit))
                if (RemoteConfigUtil.getUseAi()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        add(context.getString(R.string.haru_menu_ai))
                    }
                }
                add(context.getString(R.string.haru_menu_options_trash))
                add(context.getString(R.string.haru_menu_options_settings))
            }

            Mode.MEMO_FONT_SIZE -> listOf(
                context.getString(R.string.haru_menu_font_size_xs),
                context.getString(R.string.haru_menu_font_size_s),
                context.getString(R.string.haru_menu_font_size_m),
                context.getString(R.string.haru_menu_font_size_l),
                context.getString(R.string.haru_menu_font_size_xl),
            )

            Mode.DETAIL -> buildList {
                add(context.getString(R.string.haru_menu_share))

                if (currentMemoItem?.isLocked == true) {
                    add(context.getString(R.string.haru_menu_secret_unlock))
                } else {
                    add(context.getString(R.string.haru_menu_secret))
                }
                if (RemoteConfigUtil.getUseAi()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        add(context.getString(R.string.haru_menu_ai_summation))
                    }
                }
                add(context.getString(R.string.haru_menu_delete))
                add(context.getString(R.string.haru_menu_information))
            }
        }
    }

    fun setOnItemClickListener(listener: (position: Int, label: String) -> Unit) {
        itemClickListener = listener
    }

    fun setMemoItem(memoItem: MemoEntity) {
        this@MemoMenuView.currentMemoItem = memoItem
    }
}


package com.memong.aos.ui.custom.bottomsheet

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.memong.aos.R
import com.memong.aos.data.entity.MemoEntity
import com.memong.aos.data.utils.Util
import com.memong.aos.databinding.DialogBottomSheetMemoIfnoCustomBinding


internal class MemoInfoBottomSheetCustomDialog : BottomSheetDialogFragment() {

    private var _binding: DialogBottomSheetMemoIfnoCustomBinding? = null
    private val binding get() = _binding!!

    companion object {
        val NAME: String = MemoInfoBottomSheetCustomDialog::class.java.simpleName

        private const val ARG_MEMO = "arg_memo"

        fun newInstance(memo: MemoEntity): MemoInfoBottomSheetCustomDialog {
            return MemoInfoBottomSheetCustomDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MEMO, memo)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBottomSheetMemoIfnoCustomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonClose.setOnClickListener { dismiss() }

        val memo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_MEMO, MemoEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_MEMO)
        } ?: return

        val createdAt = Util.formatDate(memo.created) // 날짜 포맷 함수
        val modifiedAt = Util.formatDate(memo.modified)

        binding.textCreatedAt.text = createdAt
        binding.textUpdatedAt.text = modifiedAt
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.background = null // 배경 제거
            bottomSheet?.setBackgroundResource(R.drawable.shape_bg_rounded_top_white) // 둥근 배경 적용
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet =
            dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?: return

        val heightPx = Util.dpToPx(requireContext(), 200)

        bottomSheet.layoutParams?.let {
            it.height = heightPx
            bottomSheet.layoutParams = it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
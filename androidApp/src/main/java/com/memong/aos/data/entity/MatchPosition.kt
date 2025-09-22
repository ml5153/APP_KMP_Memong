package com.memong.aos.data.entity

data class MatchPosition(
    val position: Int,   // 어댑터의 Block 위치
    val rowIndex: Int,   // BodyBlock 내부의 행 인덱스
    val indexInRow: Int  // 해당 행에서 몇 번째 매치인지(0-based)
)
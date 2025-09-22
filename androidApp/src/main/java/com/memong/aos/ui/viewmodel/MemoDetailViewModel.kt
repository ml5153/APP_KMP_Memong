package com.memong.aos.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.avatye.haru.log.LogTrack
import com.memong.aos.data.dao.MemoDao
import com.memong.aos.data.dao.TagDao
import com.memong.aos.data.dao.TaggingDao
import com.memong.aos.data.entity.BodyItem
import com.memong.aos.data.entity.BodyRow
import com.memong.aos.data.entity.BodyType
import com.memong.aos.data.entity.MemoBlock
import com.memong.aos.data.entity.MemoEntity
import com.memong.aos.data.entity.TagEntity
import com.memong.aos.data.entity.TaggingEntity
import com.memong.aos.data.enum.MemoMode
import com.memong.aos.data.utils.RowCodecUtil
import com.memong.aos.ui.MemoDetailActivity.Companion.IMAGE_END_POS
import com.memong.aos.ui.MemoDetailActivity.Companion.TITLE_END_POS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MemoDetailViewModel : ViewModel() {
    companion object {
        const val NAME = "MemoDetailViewModel"
    }

    private val _memoItem = MutableStateFlow<MemoEntity?>(null)
    val memoItem: StateFlow<MemoEntity?> = _memoItem

    private val _memoMode = MutableStateFlow(MemoMode.NONE)
    val memoMode: StateFlow<MemoMode> = _memoMode

    private val _memoBlocks = MutableStateFlow<List<MemoBlock>>(emptyList())
    val memoBlocks: StateFlow<List<MemoBlock>> = _memoBlocks

    init {
        LogTrack.i(NAME) { "init" }
        _memoBlocks.value = listOf(
            MemoBlock.DateBlock(date = ""),
            MemoBlock.BodyBlock(
                bodyRows = mutableListOf(
                    BodyRow(text = "", type = BodyType.TEXT, isChecked = false)
                )
            )
        )
    }

    // region { Memo }
    fun setMemoItem(memo: MemoEntity) {
        _memoItem.value = memo
    }

    fun insertNewMemo(
        memoDao: MemoDao,
        tagDao: TagDao,
        taggingDao: TaggingDao,
        userId: String,
        uuid: String,
        title: String,
        body: List<BodyItem>,
        imagePath: Map<Int, List<String>>,
        bgColor: String,
        now: Long,
        onInserted: () -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val hasText = title.isNotBlank() || body.any { it.text.isNotBlank() }
            val hasImage = imagePath.isNotEmpty()

            val category = when {
                hasImage -> 2
                hasText -> 1
                else -> -1
            }

            val newMemo = MemoEntity(
                userid = userId,
                uuid = uuid,
                title = title,
                body = body,
                imagePath = imagePath,
                created = now,
                modified = now,
                read = now,
                custom = now,
                isLocked = false,
                isImportant = false,
                bgColor = bgColor,
                category = category,
                isDeleted = false,
                deleted = 0
            )

            val insertedId = memoDao.insertMemo(newMemo)
            val updatedMemo = newMemo.copy(_id = insertedId.toInt())
            _memoItem.value = updatedMemo

            // 태그
            val tags = getTagsFromBodyBlocks(_memoBlocks.value)
            for (tagName in tags) {
                val tagId: Int = tagDao.getTagByName(tagName)?._id ?: run {
                    val newTag = TagEntity(tagName = tagName, created = System.currentTimeMillis())
                    val tagInsertedId = tagDao.insertTag(newTag)
                    tagInsertedId.toInt()
                }

                val tagging = TaggingEntity(
                    tagid = tagId,
                    memoid = insertedId.toInt()
                )
                taggingDao.insertTagging(tagging)
            }

            withContext(Dispatchers.Main) {
                onInserted()
            }
        }
    }

    fun updateMemo(
        memoDao: MemoDao,
        tagDao: TagDao,
        taggingDao: TaggingDao,
        title: String,
        body: List<BodyItem>,
        imagePath: Map<Int, List<String>>,
        now: Long,
        onUpdated: () -> Unit
    ) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val original = _memoItem.value ?: return@launch

                    val hasText = title.isNotBlank() || body.any { it.text.isNotBlank() }
                    val hasImage = imagePath.values.any { it.isNotEmpty() }
                    val category = when {
                        hasImage -> 2
                        hasText -> 1
                        else -> -1
                    }

                    val updated = original.copy(
                        title = title,
                        body = body,
                        imagePath = imagePath,
                        modified = now,
                        category = category
                    )

                    memoDao.updateMemo(updated)
                    _memoItem.value = updated

                    val previousTagEntities = tagDao.getTagsForMemo(updated._id)
                    val previousTagNames = previousTagEntities.map { it.tagName }.toSet()

                    taggingDao.deleteAllTagsForMemo(updated._id)

                    val currentTagNames = getTagsFromBodyBlocks(_memoBlocks.value).toSet()

                    for (tagName in currentTagNames) {
                        val tagId = tagDao.getTagByName(tagName)?._id ?: run {
                            val newTag = TagEntity(tagName = tagName, created = System.currentTimeMillis())
                            tagDao.insertTag(newTag).toInt()
                        }

                        taggingDao.insertTagging(
                            TaggingEntity(tagid = tagId, memoid = updated._id)
                        )
                    }

                    val removedTagNames = previousTagNames - currentTagNames
                    for (removedName in removedTagNames) {
                        val tag = tagDao.getTagByName(removedName) ?: continue
                        val isStillTagged = taggingDao.countTaggingsForTag(tag._id) > 0
                        if (!isStillTagged) {
                            tagDao.deleteQueryTag(tag._id)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        onUpdated()
                    }

                    LogTrack.i(NAME) {
                        """
        [updateMemo]
        blocks.size = ${_memoBlocks.value.size}

        ▷ title: ${original.title} -> ${updated.title}
        ▷ body : ${original.body} -> ${updated.body}
        ▷ imagePath: ${original.imagePath.values} -> ${updated.imagePath.values}
        """.trimIndent()
                    }

                } catch (e: Exception) {
                    LogTrack.e(NAME) { "updateMemo failed: ${e.localizedMessage}" }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateImportant() {
        val current = _memoItem.value ?: return
        val updated = current.copy(isImportant = !current.isImportant)
        _memoItem.value = updated
    }

    fun updateDelete() {
        val current = _memoItem.value ?: return
        val deleted = current.copy(
            isDeleted = true,
            deleted = System.currentTimeMillis()
        )
        _memoItem.value = deleted
    }

    fun updateMemoColor(hexColor: String) {
        val current = _memoItem.value ?: return
        _memoItem.value = current.copy(bgColor = hexColor)
    }

    fun updateMemoBackgroundColor(
        colorHex: String,
        memoDao: MemoDao,
        onUpdated: (() -> Unit)? = null
    ) {
        val current = _memoItem.value ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val updated = current.copy(bgColor = colorHex)
            memoDao.updateMemo(updated)
            _memoItem.value = updated

            withContext(Dispatchers.Main) {
                onUpdated?.invoke()
            }
        }
    }

    fun updateMemoLockState(
        memoDao: MemoDao,
        memoId: Int,
        isLocked: Boolean,
        onUpdated: (() -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val memo = memoDao.getMemoById(memoId) ?: return@launch
            val updated = memo.copy(isLocked = isLocked)
            memoDao.updateMemo(updated)
            _memoItem.value = updated

            withContext(Dispatchers.Main) {
                onUpdated?.invoke()
            }
        }
    }
    // endregion

    // Mode
    fun setMemoMode(mode: MemoMode) {
        _memoMode.value = mode
    }

    // region { Block }
    fun setMemoBlocks(blocks: List<MemoBlock>) {
        _memoBlocks.value = blocks
    }

    fun getTitleAndBodyItems(): Pair<String, List<BodyItem>> {
        val stream = _memoBlocks.value
        var title = ""
        var firstRowTaken = false
        var contentIndex = 0
        val out = mutableListOf<BodyItem>()

        for (block in stream) {
            when (block) {
                is MemoBlock.BodyBlock -> {
                    block.bodyRows.forEach { row ->
                        val s = RowCodecUtil.serialize(row)

                        if (!firstRowTaken) {
                            val nl = s.indexOf('\n')
                            if (nl >= 0) {
                                title = s.substring(0, nl).trimEnd()
                                val tail = s.substring(nl + 1)
                                if (tail.isNotBlank()) {
                                    out += BodyItem(index = contentIndex, text = tail)
                                }
                            } else {
                                title = s.trim()
                            }
                            firstRowTaken = true
                        } else {
                            out += BodyItem(index = contentIndex, text = s)
                        }
                    }
                    contentIndex++
                }
                is MemoBlock.ImageUriBlock -> {
                    contentIndex++ // 이미지 블록 하나 끝
                }
                else -> Unit
            }
        }
        return title to out
    }

    fun setImageBlock(position: Int, uris: List<String>) {
        val currentList = _memoBlocks.value.toMutableList()
        val safePosition = position.coerceIn(0, currentList.size)

        val existingBlock = currentList.getOrNull(safePosition)

        if (existingBlock is MemoBlock.ImageUriBlock) {
            val updatedBlock = existingBlock.copy(
                uris = (existingBlock.uris + uris).toMutableList()
            )
            currentList[safePosition] = updatedBlock
        } else {
            val newImageBlock = MemoBlock.ImageUriBlock(
                id = UUID.randomUUID().toString(),
                uris = uris.toMutableList()
            )
            currentList.add(safePosition, newImageBlock)
        }

        val next = currentList.getOrNull(safePosition + 1)
        if (next !is MemoBlock.BodyBlock) {
            currentList.add(safePosition + 1, MemoBlock.BodyBlock())
        }

        _memoBlocks.value = currentList
    }

    fun setImageListBlock(position: Int, uris: List<String>) {
        if (uris.isEmpty()) return

        val currentList = _memoBlocks.value.toMutableList()
        val safePosition = maxOf(position.coerceIn(0, currentList.size), TITLE_END_POS + IMAGE_END_POS)

        val updatedList = currentList.toMutableList()
        val targetBlock = updatedList.getOrNull(safePosition)

        if (targetBlock is MemoBlock.ImageUriBlock) {
            val newBlock = targetBlock.copy(
                id = UUID.randomUUID().toString(),
                uris = (targetBlock.uris + uris).toMutableList() // 이미 String
            )
            updatedList[safePosition] = newBlock
        } else {
            val newImageBlock = MemoBlock.ImageUriBlock(
                id = UUID.randomUUID().toString(),
                uris = uris.toMutableList()
            )
            updatedList.add(safePosition, newImageBlock)

            val nextBlock = updatedList.getOrNull(safePosition + 1)
            if (nextBlock !is MemoBlock.BodyBlock) {
                updatedList.add(safePosition + 1, MemoBlock.BodyBlock())
            }
        }

        _memoBlocks.value = updatedList.toList()
    }


    fun getTagsFromBodyBlocks(blocks: List<MemoBlock>): List<String> {
        val tagRegex = Regex("#[\\w가-힣]+")
        return blocks
            .filterIsInstance<MemoBlock.BodyBlock>()
            .flatMap { block ->
                block.bodyRows.asSequence()
                    .map { it.text }
                    .flatMap { t -> tagRegex.findAll(t).map { m -> m.value.trim('#') } }
            }
            .distinct()
    }
}

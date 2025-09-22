package com.memong.aos.ui

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.avatye.adcash.BannerAdSize
import com.bumptech.glide.Glide
import com.memong.aos.BuildConfig
import com.memong.aos.R
import com.memong.aos.data.enum.MemoMode
import com.memong.aos.data.extension.start
import com.memong.aos.data.utils.PermissionUtil
import com.memong.aos.data.utils.Util
import com.memong.aos.databinding.ActivityImageDetailBinding
import com.memong.aos.databinding.ItemImageDetailPagerBinding

internal class ImageDetailActivity : BaseActivity() {

    override val NAME: String
        get() = ImageDetailActivity::class.java.simpleName

    private lateinit var binding: ActivityImageDetailBinding

    companion object {
        private var imageDeleteCallback: (() -> Unit)? = null

        private const val EXTRA_MEMO_MOD = "EXTRA:MEMO-MODE"
        private const val EXTRA_IMAGE_URI = "EXTRA:IMAGE-URI"
        private const val EXTRA_START_INDEX = "EXTRA:START-INDEX"
        private const val REQUEST_CODE_WRITE = 1001

        fun start(
            activity: Activity,
            mode: MemoMode,
            startIndex: Int,
            uris: List<String>,              // String 기반
            onImageDelete: () -> Unit,
            close: Boolean = false
        ) {
            imageDeleteCallback = onImageDelete

            activity.start(
                intent = Intent(activity, ImageDetailActivity::class.java).apply {
                    putExtra(EXTRA_MEMO_MOD, mode.value)
                    putExtra(EXTRA_START_INDEX, startIndex)
                    putStringArrayListExtra(EXTRA_IMAGE_URI, ArrayList(uris)) // String 리스트 전달
                },
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }
    }

    private var uriToDownload: String? = null   // Uri → String
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureWindowInsets(binding.imageDetailRootView, paddingDp = 0)

        currentIndex = intent.getIntExtra(EXTRA_START_INDEX, 0)
        val memoMode: Int = intent.getIntExtra(EXTRA_MEMO_MOD, MemoMode.NONE.value)

        val uriList: ArrayList<String> =
            intent.getStringArrayListExtra(EXTRA_IMAGE_URI) ?: arrayListOf()

        if (uriList.isNotEmpty()) {
            setViewPager(urls = uriList)
            setHeader(memoMode = memoMode, urls = uriList)
        }

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = binding.bottomBannerView
        )
    }

    override fun onPause() {
        super.onPause()
        binding.bottomBannerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomBannerView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.headerImageDetail.onDestroy()
        binding.bottomBannerView.onDestroy()
        imageDeleteCallback = null
    }

    private fun setViewPager(urls: List<String>) {
        binding.imageViewPager.adapter = ImagePagerAdapter(images = urls)
        binding.imageViewPager.setCurrentItem(currentIndex, false)
        binding.headerImageDetail.setIndicatorText("${currentIndex + 1} / ${urls.size}")
        binding.imageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentIndex = position
                binding.headerImageDetail.setIndicatorText("${position + 1} / ${urls.size}")

                // view 줌인 리셋
                val viewHolder = (binding.imageViewPager.getChildAt(0) as? RecyclerView)
                    ?.findViewHolderForAdapterPosition(position) as? ImagePagerAdapter.ImageViewHolder
                viewHolder?.binding?.itemImageDetail?.setScale(1.0f, false)
            }
        })
    }

    private fun setHeader(memoMode: Int, urls: List<String>) {
        val currentUri: String? = urls.getOrNull(binding.imageViewPager.currentItem)
        currentUri?.let { uri ->
            // 삭제하기
            binding.headerImageDetail.onDeleteClick = {
                when (memoMode) {
                    MemoMode.CREATE_MEMO.value, MemoMode.MODIFY_MEMO.value -> {
                        imageDeleteCallback?.invoke()
                        Util.toastShort(this@ImageDetailActivity, getString(R.string.haru_image_delete_success))
                        finish()
                    }

                    else -> {
                        Util.toastShort(this@ImageDetailActivity, getString(R.string.haru_image_delete_failed))
                        finish()
                    }
                }
            }

            // 공유하기
            binding.headerImageDetail.onShareClick = {
                shareImage(uri = uri)
            }

            // 다운로드
            binding.headerImageDetail.onDownLoadClick = {
                if (PermissionUtil.hasImageWritePermission(this)) {
                    downloadImage(uri = uri)
                } else {
                    uriToDownload = uri
                    PermissionUtil.requestImageWritePermission(this, REQUEST_CODE_WRITE)
                }
            }

            // x버튼
            binding.headerImageDetail.onBackClick = {
                finish()
            }
        }
    }

    private fun shareImage(uri: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, Uri.parse(uri))   // String → Uri
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.haru_image_share)))
    }

    private fun downloadImage(uri: String) {
        val inputStream = contentResolver.openInputStream(Uri.parse(uri))
        val filename = "memo_${System.currentTimeMillis()}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyMemoApp")
        }

        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        imageUri?.let { outUri ->
            contentResolver.openOutputStream(outUri).use { outputStream ->
                inputStream?.copyTo(outputStream!!)
            }
            Util.toastShort(this, getString(R.string.haru_image_download))
        } ?: run {
            Util.toastShort(this, getString(R.string.haru_image_download_failed))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_WRITE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                uriToDownload?.let { downloadImage(it) }
            } else {
                Util.toastShort(this, getString(R.string.haru_image_download_failed_permission))
            }
        }
    }

    class ImagePagerAdapter(private val images: List<String>) :
        ListAdapter<String, ImagePagerAdapter.ImageViewHolder>(UriDiffCallback()) {

        inner class ImageViewHolder(val binding: ItemImageDetailPagerBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(uri: String) {
                Glide.with(binding.root.context)
                    .load(uri)   // String 기반
                    .into(binding.itemImageDetail)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val binding = ItemImageDetailPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ImageViewHolder(binding)
        }

        override fun getItemCount() = images.size

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            holder.bind(images[position])
        }

        class UriDiffCallback : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        }
    }
}
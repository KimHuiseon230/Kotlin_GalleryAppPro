package com.example.galleryapppro

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.galleryapppro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //1. 인텐트를 통해서 갤러리 앱에서 클릭을 하면 클릭된 이미지의 Uri를 가져와서, 컨텐트 리졸버를 이용해
        // inputstream을 이용해서 BitmapFactory 이용해서 이미지 뷰를 가져온다.
        val requestLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val uri: Uri = it.data!!.data!!
            // 1.1이미지를 가져오면 oom이 발생하할 수 으므로 사이즈를 우리가 원하는 사이즈로 비율을 설정
            val inSampleSize = calculateInSampleSize(
                uri,
                resources.getDimensionPixelOffset(R.dimen.imgSize),
                resources.getDimensionPixelOffset(R.dimen.imgSize)
            )
            //1.2 비트맵 옵션 및 비율 설정 ()
            val option = BitmapFactory.Options()
            option.inSampleSize = inSampleSize
            try {
                //1.3 contentResolver 을 사용해 uri의 내가 원하는 정보를 가져온다. (uri-> inputStream)
                val inputStream = contentResolver.openInputStream(uri)
                // 진짜 비트맵으로 가져옴
                //1.4  inputStream 으로 BitmapFactory를 이용해서 이미지를 가져온다.(oom을 방지하기 위해서 option 사이즈 비율을 정함)
                var bitmap = BitmapFactory.decodeStream(inputStream, null, option)
                //1.5 이미지뷰에 bitmap을 저장시키면 된다.
                bitmap?.let {
                    binding.ivPicture.setImageBitmap(bitmap)
                }
                    ?: let {
                        Log.e("MainActivity", "BitmapFactory를 통해서 가져온 Bitmap => Null 발생")
                    }
                inputStream?.close()
            } catch (e: java.lang.Exception) {
                Log.e("MainActivity", "${e.printStackTrace()}")
            }
        }

        //2. 갤러리 앱에서 암시적 인텐트 방법으로 요청
        binding.btnCallimg.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            requestLauncher.launch(intent)
        }

    }

    // 4.이미지 비율을 계산하는 함수를 만든다.
    fun calculateInSampleSize(uri: Uri, reqWidth: Int, reqHight: Int): Int {
        val option = BitmapFactory.Options()
        // 이미지를 가져오지 말고, 이미지의 정보만 줄 것을 요청.
        option.inJustDecodeBounds = true
        try {
            //  contentResolver 의 이미지 정보를 다시 가져온다.
            var inputStream = contentResolver.openInputStream(uri)
            /// 진짜 Bitmap을 inputStream을 통해서 이미지를 가져오는 것이 아니라, Bitmap의 정보만 옵션에 저장해서 가져온다.
            BitmapFactory.decodeStream(inputStream, null, option)
            inputStream?.close()
            inputStream = null
        } catch (e: java.lang.Exception) {
            Log.e("MainActivity", "calculateInSampleSize inputStream : ${e.printStackTrace()}")
        }
        // 갤러리 앱에 가져올 실제 이미지 사이즈
        val height = option.outHeight
        val width = option.outWidth
        // 이미지를 줄일 필요 없이 가져와라
        var inSampleSize = 1;
        if (height > reqHight || width > reqWidth) {
            //2..4..8
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHight && halfWidth / inSampleSize >= halfWidth)
                inSampleSize *= 2
        }
        return 0
    }
}
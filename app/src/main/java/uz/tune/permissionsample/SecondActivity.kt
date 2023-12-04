package uz.tune.permissionsample

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import uz.tune.permissionsample.databinding.ActivitySecondBinding
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files


class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding

    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOpenCamera.setOnClickListener {
            PermissionUtil.checkSinglePermission(
                this,
                object : PermissionUtil.MyPermissionListener {
                    override fun onAllow() {
                        val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        try {
                            directory?.let { file ->
                                photoFile = File.createTempFile(
                                    "PROFILE_IMG_${System.currentTimeMillis()}",
                                    ".jpg",
                                    file
                                )
                                val uri = FileProvider.getUriForFile(
                                    this@SecondActivity,
                                    "${packageName}.provider",
                                    photoFile!!
                                )
                                takePicture.launch(uri)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }

                    override fun onDeny() {
                    }
                },
                Manifest.permission.RECORD_AUDIO
            )
        }

        binding.btnOpenGallery.setOnClickListener {
            PermissionUtil.checkPermission(
                this,
                object : PermissionUtil.MyPermissionListener {
                    override fun onAllow() {
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/*"
                        takePhotoFromGalleryResult.launch(intent)
                    }

                    override fun onDeny() {
                        Toast.makeText(
                            this@SecondActivity,
                            "ruxsat berilmadi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }
    }


    private val takePhotoFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let {
                    binding.ivPhoto.setImageURI(it)

//                    val folder =
//                        Environment
//                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

                    val folder =
                        Environment.getExternalStorageDirectory().absolutePath + "/Documents"

                    val openInputStream = contentResolver?.openInputStream(it)
                    val file =
                        File(folder, "a${System.currentTimeMillis()}.jpg")
                    val fileOutputStream = FileOutputStream(file)
                    openInputStream?.copyTo(fileOutputStream)
                    openInputStream?.close()
                    Log.d("PATH", file.absolutePath)
                }
            }
        }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                binding.ivPhoto.setImageURI(photoFile?.toUri())
            }
        }
}
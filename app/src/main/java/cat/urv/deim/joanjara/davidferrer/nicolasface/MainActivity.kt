package cat.urv.deim.joanjara.davidferrer.nicolasface

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Build.*
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.view.View
import android.net.Uri
import android.provider.MediaStore
import android.os.StrictMode
import java.io.File
import java.text.SimpleDateFormat
import java.util.*




class MainActivity : AppCompatActivity() {
    private var REQUEST_IMAGE_CAPTURE = 0
    private var URI_NICOLAS = Uri.EMPTY
    private var image : Uri? = null
    private var mCameraFileName : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pm = this.getPackageManager()
        if(!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            photo_take_btn.isClickable=false
            photo_take_btn.visibility= View.INVISIBLE
        }
        img_save_btn.isClickable=false
        img_save_btn.visibility= View.INVISIBLE

        img_share_btn.isClickable=false
        img_share_btn.visibility= View.INVISIBLE

        //BUTTON CLICK PICK
        img_pick_btn.setOnClickListener {
            //check runtime permission
            if (VERSION.SDK_INT >= VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE);
                }
                else{
                    //permission already granted
                    pickImageFromGallery();
                }
            }
            else{
                //system OS is < Marshmallow
                pickImageFromGallery();
            }
        }
        //BUTTON CLICK SAVE
        img_save_btn.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED){
                //permission denied
                val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE);
            }
            else{
                //permission already granted
                saveImageFromGallery();
                img_share_btn.isClickable=true
                img_share_btn.visibility= View.VISIBLE
            }
        }
        //BUTTON CLICK TAKE PHOTO
        photo_take_btn.setOnClickListener {
            REQUEST_IMAGE_CAPTURE = 1
            dispatchTakePictureIntent()
        }
        //BUTTON TO SHARE AN IMAGE
        img_share_btn.setOnClickListener {
            shareImage()
        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun saveImageFromGallery() {
        // Save image to gallery
        val draw = image_view.drawable
        val bitmap = (draw as BitmapDrawable).bitmap
        val message = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "",
            "Nicolas Cage"
        )
        URI_NICOLAS = Uri.parse(message)
        Toast.makeText(this, "Image saved: "+URI_NICOLAS , Toast.LENGTH_SHORT).show()
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            image_view.setImageURI(data?.data)
            img_save_btn.isClickable=true
            img_save_btn.visibility= View.VISIBLE
            //This goes our code to make nicolas happens

        }
        else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            img_save_btn.isClickable=true
            img_save_btn.visibility= View.VISIBLE
            if (data != null) {
                image = data.data
                image_view.setImageURI(image)
                image_view.setVisibility(View.VISIBLE)
            }
            if (image == null && mCameraFileName != null) {
                image = Uri.fromFile(File(mCameraFileName))
                image_view.setImageURI(image)
                image_view.setVisibility(View.VISIBLE)
            }
            val file = File(mCameraFileName)
            if (!file.exists()) {
                file.mkdir()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val intent = Intent()
        intent.action = MediaStore.ACTION_IMAGE_CAPTURE

        val date = Date()
        val df = SimpleDateFormat("-mm-ss")

        val newPicFile = df.format(date) + ".jpg"
        val outPath = "/sdcard/$newPicFile"
        val outFile = File(outPath)

        mCameraFileName = outFile.toString()
        val outuri = Uri.fromFile(outFile)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    fun shareImage(){
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type ="image/*"
        sendIntent.putExtra(Intent.EXTRA_STREAM, URI_NICOLAS)
        startActivity(sendIntent)
    }
}

package cat.urv.deim.joanjara.davidferrer.nicolasface

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build.*
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.view.View
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore

class MainActivity : AppCompatActivity() {
    private var REQUEST_IMAGE_CAPTURE = 0
    private var URI_NICOLAS = Uri.EMPTY
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
        val draw = image_view.drawable
        val bitmap = (draw as BitmapDrawable).bitmap
        // Save image to gallery
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
            val imageBitmap = data!!.extras!!.get("data") as Bitmap
            image_view.setImageBitmap(imageBitmap)
        }

    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    fun shareImage(){
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type ="image/*"
        sendIntent.putExtra(Intent.EXTRA_STREAM, URI_NICOLAS)
        startActivity(sendIntent)
    }
}

package cat.urv.deim.joanjara.davidferrer.nicolasface

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Build.*
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.activity_main.*
import android.view.View
import android.net.Uri
import android.provider.MediaStore
import android.os.StrictMode
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.*
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import android.graphics.BitmapFactory
import android.graphics.Bitmap

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private var REQUEST_IMAGE_CAPTURE = 0
    private var URI_NICOLAS = Uri.EMPTY
    private var image : Uri? = null
    private var mCameraFileName : String? = null
    private var dataFace : Intent? = null
    private var auxBitMap : Bitmap? = null
    // High-accuracy landmark detection and face classification
    val highAccuracyOpts = FirebaseVisionFaceDetectorOptions.Builder()
        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
        .build()
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pm = this.getPackageManager()
        if(!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            photo_take_btn.isClickable=false
            photo_take_btn.visibility= View.INVISIBLE
        }

        nicolas_btn.isClickable=false
        nicolas_btn.visibility= View.INVISIBLE

        img_share_btn.isClickable=false
        img_share_btn.visibility= View.INVISIBLE

        //BUTTON CLICK PICK
        img_pick_btn.setOnClickListener {
            //check runtime permission
            auxBitMap=null
            if (VERSION.SDK_INT >= VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE);
                } else {
                    //permission already granted
                    pickImageFromGallery();
                }
            } else {
                //system OS is < Marshmallow
                img_share_btn.isClickable=false
                img_share_btn.visibility= View.INVISIBLE
                pickImageFromGallery();
            }
        }

        //BUTTON CLICK TAKE PHOTO
        photo_take_btn.setOnClickListener {
            dataFace=null
            REQUEST_IMAGE_CAPTURE = 1
            dispatchTakePictureIntent()
        }
        //BUTTON TO SHARE AN IMAGE
        img_share_btn.setOnClickListener {
            shareImage()
        }
        //BUTTON TRANSFORM INTO NICOLAS CAGE
        nicolas_btn.setOnClickListener {
            Toast.makeText(this, "Transforming image to Nicolas Cage Image", Toast.LENGTH_SHORT).show()
            detectFaces(dataFace, auxBitMap)
            saveImageFromGallery();
            img_share_btn.isClickable=true
            img_share_btn.visibility= View.VISIBLE
            nicolas_btn.isClickable=false
            nicolas_btn.visibility= View.INVISIBLE
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
        auxBitMap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), URI_NICOLAS);
        Toast.makeText(this, "Image saved: "+URI_NICOLAS , Toast.LENGTH_SHORT).show()
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    //permission from popup granted
                    pickImageFromGallery()
                } else {
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
            //This goes our code to make nicolas happens
            nicolas_btn.isClickable=true
            nicolas_btn.visibility= View.VISIBLE
            dataFace = data
        }
        else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            image = Uri.fromFile(File(mCameraFileName))
            image_view.setImageURI(image)
            image_view.setVisibility(View.VISIBLE)
            val file = File(mCameraFileName)
            if (!file.exists()) {
                file.mkdir()
            }
            saveImageFromGallery()
            nicolas_btn.isClickable=true
            nicolas_btn.visibility= View.VISIBLE
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

    fun detectFaces(data: Intent?, map: Bitmap?) {
        val image: FirebaseVisionImage

        try {
            if (data?.data != null || map!=null) {
                // Agafar el bitmap del storage, en principi no fa falta
                /*val workingBitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, data.data!!)*/

                val workingBitmap = (image_view.drawable as BitmapDrawable).bitmap
                if(data!=null) image = FirebaseVisionImage.fromFilePath(this, data!!.data!!)
                else image = FirebaseVisionImage.fromBitmap(map!!)
                // Obtenemos una instancia de FirebaseVisionFaceDetector
                val detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(highAccuracyOpts)

                // Pasamos la imagen al metodo detectInImage
                val result = detector.detectInImage(image)
                    .addOnSuccessListener { faces ->
                        // Task completed successfully
                        //canvas.rotate((-Math.PI / 2).toFloat());
                        modifyFaces(faces,workingBitmap)


                        //Toast.makeText(this, faces.size.toString(), Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener(
                        object : OnFailureListener {
                            override fun onFailure(e: Exception) {
                                // Task failed with an exception
                                // ...
                            }
                        })

            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    fun modifyFaces(list : List<FirebaseVisionFace>, bitmap: Bitmap){
        var mutableBitmap = bitmap.copy(bitmap.config, true)


        var nicolas = BitmapFactory.decodeResource(this.getResources(),R.drawable.nicolas_cage1)


        // Rotacio de bitmap, en principi no fa falta
        /*if(bitmap.width>bitmap.height){
            val matrix = Matrix()
            matrix.postRotate(-90F)

            //val scaledBitmap = Bitmap.createScaledBitmap(bitmapOrg, width, height, true)

            mutableBitmap = Bitmap.createBitmap(
                mutableBitmap, 0,
                0,
                mutableBitmap.width,
                mutableBitmap.height,
                matrix,
                true
            )
        }*/

        val canvas = Canvas(mutableBitmap)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, mutableBitmap.getWidth(), mutableBitmap.getHeight())
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        for (face in list) {
            var bounds = face.boundingBox


            /*canvas.drawCircle(
                bounds.exactCenterX(),
                bounds.exactCenterY(),
                bounds.height().toFloat() / 2,
                paint
            )*/
            var scaledNicolas = Bitmap.createScaledBitmap(
                nicolas, bounds.width(),
                bounds.height()+bounds.height()/2, false
            )
            canvas.drawBitmap(scaledNicolas, bounds.exactCenterX()-bounds.width()/2,
                bounds.exactCenterY()-(bounds.height()), paint)
        }
        canvas.drawBitmap(mutableBitmap, rect, rect, paint);
        image_view.setImageBitmap(mutableBitmap)
    }

}

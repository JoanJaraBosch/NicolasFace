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
import android.graphics.*
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private var REQUEST_IMAGE_CAPTURE = 0
    private var URI_NICOLAS = Uri.EMPTY
    private var dataFace : Intent? = null

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

        nicolas_btn.isClickable=false
        nicolas_btn.visibility= View.INVISIBLE

        img_share_btn.isClickable=false
        img_share_btn.visibility= View.INVISIBLE

        //BUTTON CLICK PICK
        img_pick_btn.setOnClickListener {
            //check runtime permission
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
            REQUEST_IMAGE_CAPTURE = 1
            dispatchTakePictureIntent()
        }
        //BUTTON TO SAVE AN IMAGE
        img_save_btn.setOnClickListener {
            saveImage()
        }
        //BUTTON TO SHARE AN IMAGE
        img_share_btn.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED){
                //permission denied
                val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE);
            }
            else {
                saveImage()
                shareImage()
            }
        }
        //BUTTON TRANSFORM INTO NICOLAS CAGE
        nicolas_btn.setOnClickListener {
            Toast.makeText(this, "Transforming image to Nicolas Cage Image", Toast.LENGTH_SHORT).show()
            detectFaces()
            img_share_btn.isClickable=true
            img_share_btn.visibility= View.VISIBLE
            nicolas_btn.isClickable=false
            nicolas_btn.visibility= View.INVISIBLE
        }
    }

    private fun saveImage(){
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_DENIED){
            //permission denied
            val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            //show popup to request runtime permission
            requestPermissions(permissions, PERMISSION_CODE);
        }
        else{
            try {
                val mess =MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    (image_view.drawable as BitmapDrawable).bitmap,
                    "NicolasCage",
                    "A nicolas cage modified image"
                );
                URI_NICOLAS = Uri.parse(mess)
                Toast.makeText(this, "Image saved: "+URI_NICOLAS.toString(), Toast.LENGTH_SHORT).show()
            }catch(e: ClassCastException){
                Toast.makeText(this, "Can't save the image", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
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
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()

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
            nicolas_btn.isClickable=true
            nicolas_btn.visibility= View.VISIBLE
            val file =  File(mCurrentPhotoPath);
            val bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(file));

            image_view.setImageBitmap(bitmap)
        }
    }

    private fun dispatchTakePictureIntent() {
        /*Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }*(
         */
        val takePictureIntent =  Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null;
            try {
                photoFile = createImageFile();
            } catch ( ex: IOException) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(this,
                "cat.urv.deim.joanjara.davidferrer.nicolasface.fileprovider",
                photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
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

    fun detectFaces() {
        val image: FirebaseVisionImage

        try {
                // Agafar el bitmap del storage, en principi no fa falta
                /*val workingBitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, data.data!!)*/
            val workingBitmap = (image_view.drawable as BitmapDrawable).bitmap
            image = FirebaseVisionImage.fromBitmap(workingBitmap)
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

    var mCurrentPhotoPath: String? =null

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath()
        return image
    }
}

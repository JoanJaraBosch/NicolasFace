package cat.urv.deim.joanjara.davidferrer.nicolasface

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build.*
import android.os.Bundle
import android.widget.Toast
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.activity_main.*
import android.provider.MediaStore
import android.graphics.Bitmap
import android.graphics.Paint
import android.R.attr.bitmap
import android.graphics.Rect
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T








@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    // High-accuracy landmark detection and face classification
    val highAccuracyOpts = FirebaseVisionFaceDetectorOptions.Builder()
        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
        .build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //BUTTON CLICK
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
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            image_view.setImageURI(data?.data)
            detectFaces(data)

        }
        super.onActivityResult(requestCode,resultCode,data)
    }

    fun detectFaces(data: Intent?) {
        val image: FirebaseVisionImage

        try {
            if(data?.data != null){
                val workingBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data.data!!)
                val mutableBitmap = workingBitmap.copy(workingBitmap.config, true)

                val canvas = Canvas(mutableBitmap)

                val color = -0xbdbdbe
                val paint = Paint()
                val rect = Rect(0, 0, mutableBitmap.getWidth(), mutableBitmap.getHeight())
                paint.setAntiAlias(true);
                canvas.drawARGB(0, 0, 0, 0);
                paint.setColor(color);

                image = FirebaseVisionImage.fromFilePath(this, data.data!!)

                // Obtenemos una instancia de FirebaseVisionFaceDetector
                val detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(highAccuracyOpts)

                // Pasamos la imagen al metodo detectInImage
                val result = detector.detectInImage(image)
                    .addOnSuccessListener { faces ->
                        // Task completed successfully
                        canvas.rotate((-Math.PI/2).toFloat());
                        for (face in faces) {
                            var bounds = face.boundingBox
                            canvas.drawCircle(bounds.exactCenterX(),bounds.exactCenterY(),bounds.height().toFloat()/2,paint)

                            ///Toast.makeText(this,"X= "+face.boundingBox.centerX().toString()+" Y="+face.boundingBox.centerY(),Toast.LENGTH_SHORT).show()
                        }

                        canvas.drawBitmap(mutableBitmap, rect, rect, paint);
                        image_view.setImageBitmap(mutableBitmap)
                        Toast.makeText(this, faces.size.toString(), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show()
        }
    }




}

package abhishek.tripstory

import abhishek.tripstory.databinding.ActivityTripCreationBinding
import abhishek.tripstory.model.tripCreationModel
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class tripCreationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripCreationBinding
    private lateinit var dialog: Dialog
    private var imageUrl : Uri? = null
    private var launchGalleryActivity= registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == Activity.RESULT_OK){
            imageUrl= it.data!!.data
            binding.imageView1.setImageURI(imageUrl)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityTripCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog= Dialog(this)
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.editTextStartDate.setOnClickListener {
            showDatePickerDialog(binding.editTextStartDate)
        }
        binding.editTextEndDate.setOnClickListener {
            showDatePickerDialog(binding.editTextEndDate)
        }
        binding.imageView1.setOnClickListener{

            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type= "image/*"
            launchGalleryActivity.launch(intent)

        }
        binding.proceedButton.setOnClickListener{

            validateFieldsAndUpload(this,binding.editTextTripName.text.toString() ,binding.editTextDestination.text.toString() ,binding.editTextStartDate.text.toString() ,binding.editTextEndDate.text.toString(),binding.editTextDescription.text.toString(), imageUrl )
        }


    }
    fun validateFieldsAndUpload(
        context: Context,
        tripName: String,
        tripDestination: String,
        startDate: String,
        endDate: String,
        description: String,
        imageUri: Uri?
    ) {


        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        dateFormat.isLenient = false


        val parsedStartDate =if(startDate.isNotEmpty()) dateFormat.parse(startDate) else dateFormat.parse("21 Jun 2023")
        val parsedEndDate = if(startDate.isNotEmpty()) dateFormat.parse(endDate) else dateFormat.parse("21 Jun 2023")

        when {
            tripName.isEmpty() || tripDestination.isEmpty() ||
                    startDate.isEmpty() || endDate.isEmpty() ||
                    description.isEmpty() -> {
                showDialog(context, "Invalid Input", "Please fill in all fields.")
            }
            parsedStartDate.after(parsedEndDate) -> {
                showDialog(context, "Invalid Date Range", "Start date must be before end date.")
            }
            imageUri == null -> {
                showDialog(context, "Choose Image", "Please select an image.")
            }
            else -> {
                uploadImage()
            }
        }
    }

    fun showDialog(context: Context, title: String, message: String) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()

        alertDialog.show()
    }
    private fun uploadImage() {
        dialog.show()
        val fileName = UUID.randomUUID().toString() + ".jpg"

        // Get the reference to the Firebase Storage
        val refStorage = FirebaseStorage.getInstance().reference.child("category/$fileName")

        // Load the image from the imageUri using an InputStream
        val inputStream = contentResolver.openInputStream(imageUrl!!)

        // Use BitmapFactory to decode the input stream into a Bitmap
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // Compress the bitmap into a ByteArrayOutputStream
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        // Create a byte array from the ByteArrayOutputStream
        val data = outputStream.toByteArray()

        // Create the upload task with the compressed byte array
        val uploadTask = refStorage.putBytes(data)

        uploadTask.addOnSuccessListener {
            // Get the download URL of the uploaded image
            refStorage.downloadUrl.addOnSuccessListener { imageUrl ->
                fetchData(imageUrl.toString())
            }
        }.addOnFailureListener {
            dialog.dismiss()
            Toast.makeText(this, "Something went wrong with storage", Toast.LENGTH_SHORT).show()
        }
    }



    private fun fetchData(photo: String) {
        var userName="";
        val userEmail= currentEmail.getEmail(this)
        var userImage = "";
        val tripId = Random.nextInt(10000, 99999).toString()
        val tripName= binding.editTextTripName.text.toString()
        val destination= binding.editTextDestination.text.toString()
        val startDate= binding.editTextStartDate.text.toString()
        val endDate: String= binding.editTextEndDate.text.toString()
        val description= binding.editTextDescription.text.toString()


        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userEmail!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    userName = document.getString("userName").toString()
                    userImage= document.getString("image").toString()
                    val data= tripCreationModel(userName,userEmail,userImage,tripId,tripName,destination,startDate,endDate,photo,description)
                    addDataToFirestore(data)
                    Toast.makeText(this, "name image fetched", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "cant fetch name image ", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                dialog.dismiss()
                println("Error getting user document: $exception")
            }
    }

    fun addDataToFirestore(data: tripCreationModel) {
        val db = FirebaseFirestore.getInstance()

        val usersCollection = db.collection("trips")
        usersCollection.document(data.tripId.toString()) // Automatically generates a unique document ID
            .set(data)
            .addOnSuccessListener {
                dialog.dismiss()
                finish()
                Toast.makeText(this, "User added to Firestore successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(this, "error cant set data to store", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val calendar = Calendar.getInstance()
                calendar.set(selectedYear, selectedMonth, selectedDay)

                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val formattedDate = outputFormat.format(calendar.time)

                editText.setText(formattedDate)
            },
            year,
            month,
            day
        )
        datePicker.show()
    }

}
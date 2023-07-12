package abhishek.tripstory

import abhishek.tripstory.Adapters.AddDiaryImageAdapter
import abhishek.tripstory.databinding.ActivityDiaryCreationBinding
import abhishek.tripstory.model.tripCreationModel
import abhishek.tripstory.model.tripDiaryModel
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class DiaryCreationActivity : AppCompatActivity() {
    private lateinit var list: ArrayList<Uri>
    private lateinit var  listImages: ArrayList<String>
    private lateinit var  adapter: AddDiaryImageAdapter

    private lateinit var dialog: Dialog
    private lateinit var categoryList: ArrayList<String>
    private var launchProductActivity= registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == Activity.RESULT_OK){
            val imageUrl= it.data!!.data
            list.add(imageUrl!!)
            adapter.notifyDataSetChanged()
        }
    }

    private lateinit var binding: ActivityDiaryCreationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDiaryCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        list= ArrayList()
        listImages=ArrayList()

        dialog= Dialog(this)
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        setTripsToSpinner()
        binding.tvDate.setOnClickListener {
            showDatePickerDialog(binding.tvDate)
        }
        binding.buttonAddImage.setOnClickListener{
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type= "image/*"
            launchProductActivity.launch(intent)
        }

        adapter = AddDiaryImageAdapter(list)
        binding.productImageRecyclerView.adapter = adapter

        binding.buttonAddToDiary.setOnClickListener{
            validateDataAndShowDialogs(
                this,
                binding.tvDiaryTitle.text.toString(),
                binding.tvLocation.text.toString(),
                binding.tvDate.text.toString(),
                binding.tvDescription.text.toString(),
                categoryList[binding.dropdownTripList.selectedItemPosition],
                list
            )
        }

    }

    fun validateDataAndShowDialogs(
        context: Context,
        diaryName: String,
        location: String,
        eventDate: String,
        experience: String,
        selectedTrip: String,
        imageList: ArrayList<Uri>
    ) {
        when {
            diaryName.isEmpty() -> {
                showDialog(context, "Invalid Input", "Please enter a diary name.")
            }
            location.isEmpty() -> {
                showDialog(context, "Invalid Input", "Please enter a location.")
            }
            eventDate.isEmpty() -> {
                showDialog(context, "Invalid Input", "Please enter an event date.")
            }
            experience.isEmpty() -> {
                showDialog(context, "Invalid Input", "Please enter an experience.")
            }
            selectedTrip==categoryList[0] -> {
                showDialog(context, "Invalid Input", "Please select a trip From Drop down Box.")
            }
            imageList.isEmpty() -> {
                showDialog(context, "Invalid Input", "Please select at least one image.")
            }
            else -> {
                // All fields and image list are valid, proceed with further actions
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










    private var i=0
    private fun uploadImage() {
        dialog.show()
        val fileName = UUID.randomUUID().toString() + ".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("diaries/$fileName")
        Glide.with(this)
            .asBitmap()
            .load(list[i])
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // Compress the Bitmap to a desired quality without changing the dimensions
                    val outputStream = ByteArrayOutputStream()
                    resource.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val data = outputStream.toByteArray()

                    // Upload the compressed image
                    refStorage.putBytes(data)
                        .addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener { image ->
                                listImages.add(image.toString())
                                if (list.size == listImages.size) {
                                    getMoreData()
                                } else {
                                    i += 1
                                    uploadImage()
                                }
                            }
                        }
                        .addOnFailureListener {
                            dialog.dismiss()
                            Toast.makeText(this@DiaryCreationActivity, "Something went wrong with storage", Toast.LENGTH_SHORT).show()
                        }
                }
            })
    }


    private fun getMoreData() {
        var userName="";
        val userEmail= currentEmail.getEmail(this)
        var userImage = "";
        val selectedItem = categoryList[binding.dropdownTripList.selectedItemPosition]
        val tripId = selectedItem.substring(0, 5)
        val tripName = selectedItem.substring(5)
        val diaryName= binding.tvDiaryTitle.text.toString()
        val date=binding.tvDate.text.toString()
        val destination= binding.tvLocation.text.toString()
        val imageList= listImages
        val description= binding.tvDescription.text.toString()
        val location= binding.tvLocation.text.toString()


        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userEmail!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    userName = document.getString("userName").toString()
                    userImage= document.getString("image").toString()
                    val data= tripDiaryModel(userName,userEmail,userImage,tripId,tripName,diaryName,date,imageList,description, location)
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

    private fun addDataToFirestore(data: tripDiaryModel) {
        val db = Firebase.firestore.collection("diaries")
        val key=  SystemClock.elapsedRealtime().toString()
        db.document(key).set(data).addOnSuccessListener {
            Toast.makeText(this,"success upload items", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            finish()
        }
            .addOnFailureListener{
                dialog.dismiss()
                Toast.makeText(this,"Something Went Wrong", Toast.LENGTH_SHORT).show()

            }

    }

    private fun setTripsToSpinner() {
        categoryList= ArrayList()
        Firebase.firestore.collection("trips")
            .whereEqualTo("userEmail", currentEmail.getEmail(this))
            .get().addOnSuccessListener {
            categoryList.clear()
            for(doc in it.documents){
                val name= doc.getString("tripName")
                val id= doc.getString("tripId")
                val data= "$id    $name"
                categoryList.add(data!!)
            }
                if (categoryList.isEmpty()) {
                    showCreateTripDialog()
                }
            categoryList.add(0, "Select Trip")

            val arrayAdapter = ArrayAdapter(this, R.layout.layout_dropdown_item,categoryList)
            binding.dropdownTripList.adapter= arrayAdapter
        }
    }
    private fun showCreateTripDialog() {
        Log.d("ttttttttt","dialog called")
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Create a Trip")
        dialogBuilder.setMessage("Please create atleast one trip before creating a new diary.")
        dialogBuilder.setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            finish()
            // Handle positive button click if required
        }

        val dialog = dialogBuilder.create()
        dialog.setCancelable(false)
        dialog.show()
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
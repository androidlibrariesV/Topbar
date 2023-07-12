package abhishek.tripstory

import abhishek.tripstory.databinding.ActivityMainBinding
import abhishek.tripstory.databinding.ActivityNewUserBinding
import abhishek.tripstory.model.UserModel
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.UserManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserInfo
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

class NewUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog : Dialog

    private var imageUrl : Uri? = null
    private var launchGalleryActivity= registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == Activity.RESULT_OK){
            imageUrl= it.data!!.data
            binding.userImage.setImageURI(imageUrl)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityNewUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        dialog= Dialog(this)
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.userImage.setOnClickListener{
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type= "image/*"
            launchGalleryActivity.launch(intent)
        }

        binding.signupButton.setOnClickListener{


            val email = binding.userEmail.text.toString()
            val password = binding.userPassword.text.toString()
            val name=binding.userName.text.toString()
            checkField(this, email,password,name, imageUrl  )


        }
    }


    fun checkField(context: Context, email: String, password: String, name: String, imageUri: Uri?) {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        val passwordRegex = "^.{6,20}$"

        val isEmailValid = email.matches(emailRegex.toRegex())
        val isPasswordValid = password.matches(passwordRegex.toRegex())

        when {
            email.isEmpty() || password.isEmpty() || name.isEmpty() -> {
                showDialog(context, "Invalid Input", "Please fill in all fields.")
            }
            !isEmailValid -> {
                showDialog(context, "Invalid Email", "Please enter a valid email address.")
            }
            !isPasswordValid -> {
                showDialog(context, "Invalid Password", "Password must be between 6 and 20 characters.")
            }
            imageUri == null -> {
                showDialog(context, "Choose Profile Photo", "Please select a profile photo.")
            }
            else -> {
                createAccountWithEmailAndPassword(email, password)
            }
        }
    }

    fun showDialog(context: Context, title: String, message: String) {
        dialog.dismiss()
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()

        alertDialog.show()
    }


    private fun createAccountWithEmailAndPassword(email: String, password: String) {
        dialog.show()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this, "Authenticated now Saving data",
                        Toast.LENGTH_SHORT
                    ).show()
                    uploadImage()
                } else {
                    // Signup failed, display an error message
                    dialog.dismiss()
                    Toast.makeText(
                        this, "Signup failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }
    private fun uploadImage() {

        val fileName = UUID.randomUUID().toString() + ".jpg"

        // Get the reference to the Firebase Storage
        val refStorage = FirebaseStorage.getInstance().reference.child("category/$fileName")

        // Load the image from the imageUri using an InputStream
        val inputStream = contentResolver.openInputStream(imageUrl!!)

        // Use BitmapFactory to decode the input stream into a Bitmap
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // Compress the bitmap without changing dimensions
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        // Create a byte array from the ByteArrayOutputStream
        val data = outputStream.toByteArray()

        // Create the upload task with the compressed byte array
        val uploadTask = refStorage.putBytes(data)

        uploadTask.addOnSuccessListener {
            // Get the download URL of the uploaded image
            refStorage.downloadUrl.addOnSuccessListener { imageUrl ->
                val userInfo = UserModel(binding.userName.text.toString(), binding.userEmail.text.toString(), imageUrl.toString())
                saveUserInfoToFireBase(userInfo)
            }
        }.addOnFailureListener {
            dialog.dismiss()
            Toast.makeText(this, "Something went wrong with storage", Toast.LENGTH_SHORT).show()
        }
    }



    private fun saveUserInfoToFireBase(userInfo: UserModel) {
        Firebase.firestore.collection("users")
            .document(binding.userEmail.text.toString())
            .set(userInfo)
            .addOnSuccessListener {
                addEmailPreferences()

                dialog.dismiss()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()


                Toast.makeText(this,"user",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                dialog.dismiss()
                Toast.makeText(this,"Auth but not add",Toast.LENGTH_SHORT).show()
            }

    }
    private fun addEmailPreferences() {
        val preferences= this.getSharedPreferences("userr", MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("emailmain", binding.userEmail.text.toString())
        editor.apply()
        val x=preferences.getString("emailmain","")
        Toast.makeText(this, "pref added and $x", Toast.LENGTH_SHORT).show()
    }


}
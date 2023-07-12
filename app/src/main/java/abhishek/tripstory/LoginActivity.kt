package abhishek.tripstory

import abhishek.tripstory.databinding.ActivityLoginBinding
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private lateinit var auth: FirebaseAuth
class LoginActivity : AppCompatActivity() {
    private lateinit var dialog: Dialog
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog= Dialog(this)
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.loginButton.setOnClickListener {
            validateEmailAndPassword(this, binding.userEmail.text.toString(), binding.userPassword.text.toString())

        }
        binding.signupButton.setOnClickListener {
            startActivity(Intent(this, NewUserActivity::class.java))
        }
    }
    fun validateEmailAndPassword(context: Context, email: String, password: String) {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        val passwordRegex = "^.{6,20}$"

        val isEmailValid = email.matches(emailRegex.toRegex())
        val isPasswordValid = password.matches(passwordRegex.toRegex())

        if (email.isEmpty() || password.isEmpty()) {
            showAlertDialog(context, "Invalid Input", "Please fill in all fields.")
        } else if (!isEmailValid || !isPasswordValid) {
            val errorMessage = when {
                !isEmailValid -> "Please enter a valid email address."
                else -> "Password must be between 6 and 20 characters."
            }

            showAlertDialog(context, "Invalid Input", errorMessage)
        } else {
            checkAndProceedLogin()
        }
    }

    fun showAlertDialog(context: Context, title: String, message: String) {
        dialog.dismiss()
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()

        alertDialog.show()
    }

    private fun checkAndProceedLogin() {
        dialog.show()

        if(binding.userEmail.text!!.isEmpty() || binding.userPassword.text!!.isEmpty()){
            Toast.makeText(this,"Please Provide All Details", Toast.LENGTH_SHORT).show()
        }else{

            Firebase.firestore.collection("users").document(binding.userEmail.text.toString())
                .get(Source.SERVER).addOnSuccessListener {
                    if(it.exists())
                        signInWithEmailAndPassword(binding.userEmail.text.toString(),binding.userPassword.text.toString())
                    else{
                        dialog.dismiss()
                        Toast.makeText(this,"signup first", Toast.LENGTH_SHORT).show()

                        AlertDialog.Builder(this)
                            .setTitle("⚠️ User Not Found")
                            .setMessage("Check your Email Id or Signup first")
                            .setPositiveButton("Signup") { dialog: DialogInterface, _: Int ->
                                startActivity(Intent(this, NewUserActivity::class.java))
                                finish()
                            }.setNeutralButton("cancel"){ dialog: DialogInterface, _: Int ->
                                dialog.dismiss()

                            }.show()
                    }

                }.addOnFailureListener {
                    dialog.dismiss()
                    AlertDialog.Builder(this)
                        .setTitle("⚠️ Please connect to the Internet")
                        .setMessage("Please connect and try again")
                        .setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                        }.show()
                }
        }

    }
    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    dialog.dismiss()
                    // Login successful, navigate to the next activity
                    addEmailPreferences()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                   startActivity(Intent(this, MainActivity::class.java))

                } else {
                    dialog.dismiss()
                    // Login failed, display an error message
                    Toast.makeText(
                        this, "Authentication failed Please check Your Password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
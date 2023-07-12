package abhishek.tripstory

import abhishek.tripstory.Adapters.ExploreDiaryAdapter
import abhishek.tripstory.Adapters.ExploreTripAdapter
import abhishek.tripstory.Adapters.FullTripDiaryAdapter
import abhishek.tripstory.databinding.ActivityFullTripBinding
import abhishek.tripstory.databinding.ActivityLoginBinding
import abhishek.tripstory.model.tripCreationModel
import abhishek.tripstory.model.tripDiaryModel
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale

class FullTripActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullTripBinding
    private lateinit var tripId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityFullTripBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tripId = intent.getStringExtra("tripId").toString()
        Toast.makeText(this, "hello $tripId", Toast.LENGTH_SHORT).show()
        loadTripData()
        loadDiaryData()
    }

    private fun loadDiaryData() {
        val list = ArrayList<tripDiaryModel>()
        Firebase.firestore.collection("diaries")
            .whereEqualTo("tripId",tripId)
            .orderBy(FieldPath.documentId() )
            .get().addOnSuccessListener {
                list.clear()
                for(doc in it.documents.reversed()){
                    val data = doc.toObject(tripDiaryModel::class.java)
                    list.add(data!!)
                }
                list.sortWith(Comparator { trip1, trip2 ->
                    val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val startDate1 = format.parse(trip1.date)
                    val startDate2 = format.parse(trip2.date)
                    startDate1?.compareTo(startDate2) ?: 0
                })
                binding.rvDiaries.adapter = FullTripDiaryAdapter(this, list)
            }
    }

    private fun loadTripData() {
        Firebase.firestore.collection("trips")
            .document(tripId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.toObject(tripCreationModel::class.java)
                    // Use the 'data' variable as needed
                    binding.tvTripName.text= data?.tripName.toString()
                    binding.tvUserName.text= data?.userName.toString()
                    binding.tvLocation.text= data?.destination.toString()
                    Glide.with(this).load(data?.photo).into(binding.ivTripImage)
                    Glide.with(this).load(data?.userImage).into(binding.ivDp)
                } else {
                    Toast.makeText(this, "Document does not exist", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to retrieve trip data", Toast.LENGTH_SHORT).show()

            }

    }
}
package abhishek.tripstory.fragments

import abhishek.tripstory.Adapters.ExploreTripAdapter
import abhishek.tripstory.LoginActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import abhishek.tripstory.currentEmail
import abhishek.tripstory.databinding.FragmentMyTripBinding
import abhishek.tripstory.model.UserModel
import abhishek.tripstory.model.tripCreationModel
import android.content.Intent
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class MyTripFragment : Fragment() {
    private lateinit var binding: FragmentMyTripBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentMyTripBinding.inflate(layoutInflater)


//        val userEmail= currentEmail.getEmail(requireContext())
//        Toast.makeText(requireContext(), "frag 2 $userEmail", Toast.LENGTH_SHORT).show()
        loadUserInfo()
        loadTrips()
        binding.tvSignout.setOnClickListener{
            signout()
        }
        return binding.root
    }


    private fun signout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun loadTrips() {


        val list = ArrayList<tripCreationModel>()
        Firebase.firestore.collection("trips").whereEqualTo("userEmail", currentEmail.getEmail(requireContext()))
            .get().addOnSuccessListener {
                list.clear()
                for(doc in it.documents){
                    val data = doc.toObject(tripCreationModel::class.java)
                    list.add(data!!)
                }
                binding.recyclerView.adapter = ExploreTripAdapter(
                    requireContext(),
                    list,
                    false
                )
            }


    }

    private fun loadUserInfo() {
        Firebase.firestore.collection("users")
            .document(currentEmail.getEmail(requireContext()).toString())
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.toObject(UserModel::class.java)
                    // Use the 'data' variable as needed
                    binding.tvUserName.text= data?.userName.toString()
                    binding.tvUserEmail.text= data?.userEmail.toString()
                    Glide.with(this).load(data?.image).into(binding.ivDp)
                } else {
                    Toast.makeText(requireContext(), "Document does not exist", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to retrieve trip data", Toast.LENGTH_SHORT).show()

            }
    }

}
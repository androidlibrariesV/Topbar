package abhishek.tripstory

import abhishek.tripstory.databinding.ActivityMainBinding
import abhishek.tripstory.fragments.ExploreFragment
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isExpanded = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        if(FirebaseAuth.getInstance().currentUser==null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


        ////bottom bar configuration
        val navHostFragment=supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        val navController= navHostFragment!!.findNavController()
        val popupMenu= PopupMenu(this,null)
        popupMenu.inflate(R.menu.bottom_nav)
        binding.bottomBar.setupWithNavController(popupMenu.menu,navController)

        binding.buttonCreateTrip.setOnClickListener{
            binding.buttonCreateTrip.visibility = View.GONE
            binding.buttonCreateDiary.visibility = View.GONE
            binding.buttonPlus.text = "+"
            isExpanded = !isExpanded
            startActivity(Intent(this, tripCreationActivity::class.java))
            // Collapse the buttons


        }
        binding.buttonCreateDiary.setOnClickListener{
            binding.buttonCreateTrip.visibility = View.GONE
            binding.buttonCreateDiary.visibility = View.GONE
            binding.buttonPlus.text = "+"
            isExpanded = !isExpanded
            startActivity(Intent(this, DiaryCreationActivity::class.java))
        }


        binding.buttonPlus.setOnClickListener {
            if (isExpanded) {
                // Collapse the buttons
                binding.buttonCreateTrip.visibility = View.GONE
                binding.buttonCreateDiary.visibility = View.GONE
                binding.buttonPlus.text = "+"
            } else {
                // Expand the buttons
                binding.buttonCreateTrip.visibility = View.VISIBLE
                binding.buttonCreateDiary.visibility = View.VISIBLE
                binding.buttonPlus.text = "✕"
            }
            // Toggle the flag variable
            isExpanded = !isExpanded
        }



    }



    override fun onBackPressed() {
//        super.onBackPressed()
        if(ExploreFragment.backpressedlistener!=null){
            ExploreFragment.backpressedlistener!!.onBackPressed();
        }

    }
}
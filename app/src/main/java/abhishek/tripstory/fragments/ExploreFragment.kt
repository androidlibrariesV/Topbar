package abhishek.tripstory.fragments

import abhishek.tripstory.Adapters.ExploreDiaryAdapter
import abhishek.tripstory.Adapters.ExploreTripAdapter
import abhishek.tripstory.R
import abhishek.tripstory.backPressForFragent.backpressedlistener
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import abhishek.tripstory.currentEmail
import abhishek.tripstory.databinding.FragmentExploreBinding
import abhishek.tripstory.model.tripCreationModel
import abhishek.tripstory.model.tripDiaryModel
import android.app.DatePickerDialog
import android.app.Dialog
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale

var etext=""
class ExploreFragment : Fragment(), backpressedlistener {
    private lateinit var binding : FragmentExploreBinding
    var listTrip = ArrayList<tripCreationModel>()
    var listDiary = ArrayList<tripDiaryModel>()
    private lateinit var dialog: Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentExploreBinding.inflate(layoutInflater)
        dialog= Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

//        val userEmail= currentEmail.getEmail(requireContext())
//        Toast.makeText(requireContext(), "frag 1 $userEmail", Toast.LENGTH_SHORT).show()
        loadExploreTrips(true, false, false,false)
        loadExploreDiaries(false,false)
        binding.bvSortByTrip.setOnClickListener{
            showTripDialog()
        }
        binding.bvSortByDiary.setOnClickListener{
            showDiaryDialog()
        }
        binding.filterIcon.setOnClickListener{
            showFilterDialog()
        }
        binding.lens.setOnClickListener{
            binding.searchView.visibility= View.VISIBLE
            binding.topBanner.visibility= View.GONE
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText!=null){
                    etext=newText;
                }
                searchDiaries()
                searchTrips()
                return false
            }
        })


        return binding.root
    }

    private fun showFilterDialog() {
        var view : View= LayoutInflater.from(context).inflate(R.layout.dialog_filter, null)
        val builder= AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

//            builder.window?.setBackgroundDrawableResource(android.R.color.transparent)
        builder.show()

        val beDate : EditText = view.findViewById(R.id.tvBefore)
        val afDate : EditText = view.findViewById(R.id.tvAfter)
        val onDate : EditText = view.findViewById(R.id.tvOn)
        val location : EditText = view.findViewById(R.id.tvLocation)
        val cancelButton : Button = view.findViewById(R.id.cancelButton)
        val filterButton : Button = view.findViewById(R.id.FilterButton)
        beDate.setOnClickListener {
            loadDate(beDate)
        }
        afDate.setOnClickListener {
            loadDate(afDate)
        }
        onDate.setOnClickListener {
            loadDate(onDate)
        }
        cancelButton.setOnClickListener {

            builder.dismiss()
        }
        filterButton.setOnClickListener {
            var on= onDate.text.toString();
            var after= afDate.text.toString();
            var befour= beDate.text.toString();
            var location= location.text.toString();
            if(onDate.text.toString()!="" && (beDate.text.toString()!="" || afDate.text.toString()!="")){
                val alertDialog = AlertDialog.Builder(requireContext())
                    .setMessage("On Date cannot be used with Before/After Date Filter")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss() // Dismiss the dialog when OK button is clicked
                    }
                    .create()

                alertDialog.show()
            }else{
                filterDiary(befour,after,on,location)
                filterTrip(befour,after,on,location)
                builder.dismiss()

            }

        }
    }

    private fun filterTrip(before: String, after: String, on: String, location: String) {
        Log.d("Filter", "before: $before, after: $after, on: $on, location: $location")
        val filteredList = if (on.isNotEmpty()) {
            // Filter by "on" date
            ArrayList(listTrip.filter { trip ->
                val startDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(trip.startDate)
                val endDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(trip.endDate)
                val onDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(on)

                (startDate?.before(onDate) == true || startDate?.equals(onDate) == true) &&
                        (endDate?.after(onDate) == true || endDate?.equals(onDate) == true) &&
                        trip.destination?.contains(location, ignoreCase = true) ?:false
            })
        } else {
            // Filter by "before" and/or "after" dates
            ArrayList(listTrip.filter { trip ->
                val startDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(trip.startDate)
                val endDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(trip.endDate)
                val beforeDate = if (before.isNotEmpty()) SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(before) else null
                val afterDate = if (after.isNotEmpty()) SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(after) else null

                (beforeDate == null || (startDate?.before(beforeDate) == true && endDate?.before(beforeDate) == true)) &&
                        (afterDate == null || (startDate?.after(afterDate) == true && endDate?.after(afterDate) == true)) &&
                        trip.destination?.contains(location, ignoreCase = true) ?:false
            })
        }
        dialog.dismiss()
        binding.rvTrip.adapter = ExploreTripAdapter(requireContext(), filteredList, true)


    }

    private fun filterDiary(before: String, after: String, on: String, location: String) {



        val filteredList = if (on.isNotEmpty()) {
            // Filter by "on" date
            ArrayList(listDiary.filter { diary ->
                diary.date == on && diary.location?.lowercase()?.contains(location.lowercase()) ?: false
            })
        } else {
            // Filter by "before" and/or "after" dates
            ArrayList(listDiary.filter { diary ->
                val diaryDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(diary.date)
                val beforeDate = if (before.isNotEmpty()) SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(before) else null
                val afterDate = if (after.isNotEmpty()) SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(after) else null

                (beforeDate == null || diaryDate?.before(beforeDate) == true) &&
                        (afterDate == null || diaryDate?.after(afterDate) == true) &&
                        diary.location?.lowercase()?.contains(location.lowercase()) ?: false
            })
        }
        dialog.dismiss()
        binding.rvDiaries.adapter = ExploreDiaryAdapter(requireContext(), filteredList)
    }

    private fun loadDate(editText: EditText) {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            requireContext(),
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

    private fun showTripDialog() {
        var view : View= LayoutInflater.from(context).inflate(R.layout.dialog_trip_sort, null)
        val builder= AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
//            builder.window?.setBackgroundDrawableResource(android.R.color.transparent)
        builder.show()

        val sd : Button = view.findViewById(R.id.sd)
        val sa : Button = view.findViewById(R.id.sa)
        val ed : Button = view.findViewById(R.id.ed)
        val ea : Button = view.findViewById(R.id.ea)
        sd.setOnClickListener {
            loadExploreTrips(true, false, false,false)
            builder.dismiss()
        }
        sa.setOnClickListener {
            loadExploreTrips(false, false, true,false)
            builder.dismiss()
        }
        ed.setOnClickListener {
            loadExploreTrips(false, true, false,false)
            builder.dismiss()
        }
        ea.setOnClickListener {
            loadExploreTrips(false, false, false,true)
            builder.dismiss()
        }
    }

    private fun showDiaryDialog() {

        var view : View= LayoutInflater.from(context).inflate(R.layout.dialog_diary_sort, null)
        val builder= AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
//            builder.window?.setBackgroundDrawableResource(android.R.color.transparent)
        builder.show()

        val def : Button = view.findViewById(R.id.defaul)
        val asc : Button = view.findViewById(R.id.asc)
        val dsc : TextView = view.findViewById(R.id.dec)
        def.setOnClickListener {
            loadExploreDiaries(false,false)
            builder.dismiss()
        }
        asc.setOnClickListener {
            loadExploreDiaries(false,true)
            builder.dismiss()
        }
        dsc.setOnClickListener {
            loadExploreDiaries(true,false)
            builder.dismiss()
        }
    }

    private fun loadExploreDiaries(Datedsc: Boolean, Dateasc: Boolean) {
        listDiary.clear()
//        val list = ArrayList<tripDiaryModel>()
        Firebase.firestore.collection("diaries")
            .orderBy(FieldPath.documentId() )
            .get().addOnSuccessListener {
                listDiary.clear()
                for(doc in it.documents.reversed()){
                    val data = doc.toObject(tripDiaryModel::class.java)
                    listDiary.add(data!!)
                }
                if(Dateasc){
                    listDiary.sortWith(Comparator { trip1, trip2 ->
                        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val startDate1 = format.parse(trip1.date)
                        val startDate2 = format.parse(trip2.date)
                        startDate1?.compareTo(startDate2) ?: 0
                    })
                }else if(Datedsc){
                    listDiary.sortWith(Comparator { trip1, trip2 ->
                        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val startDate1 = format.parse(trip1.date)
                        val startDate2 = format.parse(trip2.date)
                        startDate2?.compareTo(startDate1) ?: 0
                    })
                }
                searchDiaries();



            }

    }

    private fun searchDiaries() {
        val filteredList = ArrayList<tripDiaryModel>()
        filteredList.clear()
        for(item in listDiary){
            if(item.userName!!.lowercase().contains(etext!!.lowercase())
                || item.location!!.lowercase().contains(etext!!.lowercase())
                || item.tripName!!.lowercase().contains(etext!!.lowercase())
                || item.diaryName!!.lowercase().contains(etext!!.lowercase())){
//                 Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
                filteredList.add(item)
            }
        }
        dialog.dismiss()
        binding.rvDiaries.adapter = ExploreDiaryAdapter(requireContext(), filteredList)


    }



    private fun loadExploreTrips(starDatedsc: Boolean, endDatedsc: Boolean, starDateasc: Boolean, endDateasc: Boolean) {
        listTrip.clear()
//        val list = ArrayList<tripCreationModel>()
        Firebase.firestore.collection("trips")
            .get().addOnSuccessListener {
                listTrip.clear()
                for(doc in it.documents){
                    val data = doc.toObject(tripCreationModel::class.java)
                    listTrip.add(data!!)
                }
                if(starDateasc){
                    listTrip.sortWith(Comparator { trip1, trip2 ->
                        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val startDate1 = format.parse(trip1.startDate)
                        val startDate2 = format.parse(trip2.startDate)
                        startDate1?.compareTo(startDate2) ?: 0
                    })
                }else if(endDateasc){
                    listTrip.sortWith(Comparator { trip1, trip2 ->
                        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val startDate1 = format.parse(trip1.endDate)
                        val startDate2 = format.parse(trip2.endDate)
                        startDate1?.compareTo(startDate2) ?: 0
                    })
                }else if(starDatedsc){
                    listTrip.sortWith(Comparator { trip1, trip2 ->
                        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val startDate1 = format.parse(trip1.startDate)
                        val startDate2 = format.parse(trip2.startDate)
                        startDate2?.compareTo(startDate1) ?: 0
                    })
                }else if(endDatedsc){
                    listTrip.sortWith(Comparator { trip1, trip2 ->
                        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val startDate1 = format.parse(trip1.endDate)
                        val startDate2 = format.parse(trip2.endDate)
                        startDate2?.compareTo(startDate1) ?: 0
                    })

                }
                searchTrips();

            }

    }

    private fun searchTrips() {
        val filteredList = ArrayList<tripCreationModel>()
        filteredList.clear()
        for(item in listTrip){
            if(item.userName!!.lowercase().contains(etext!!.lowercase())
                || item.destination!!.lowercase().contains(etext!!.lowercase())
                || item.tripName!!.lowercase().contains(etext!!.lowercase())){
//                 Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
                filteredList.add(item)
            }
        }
        dialog.dismiss()
        binding.rvTrip.adapter = ExploreTripAdapter(requireContext(), filteredList, true)
    }
    fun makeInvisible(){

        etext="";
        binding.searchView.setQuery("",false)
        binding.searchView.visibility= View.GONE
        binding.topBanner.visibility= View.VISIBLE

    }
    override fun onPause() {
        backpressedlistener = null
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        dialog.show()
        loadExploreTrips(true, false, false,false)
        loadExploreDiaries(false,false)


        backpressedlistener = this
    }
    override fun onBackPressed() {
        makeInvisible()
//        Toast.makeText(context, "back button pressed", Toast.LENGTH_LONG).show()
    }
    companion object {
        var backpressedlistener: backpressedlistener? = null
    }




}
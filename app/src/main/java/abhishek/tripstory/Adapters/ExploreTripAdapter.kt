package abhishek.tripstory.Adapters

import abhishek.tripstory.FullTripActivity
import abhishek.tripstory.R
import abhishek.tripstory.databinding.LayoutTripBinding
import abhishek.tripstory.model.tripCreationModel

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ExploreTripAdapter(
    var context: Context,
    val list: ArrayList<tripCreationModel>,
    var callFromProfile: Boolean
)
    : RecyclerView.Adapter<ExploreTripAdapter.CategoryViewHolder>(){
    inner class CategoryViewHolder(view : View) : RecyclerView.ViewHolder(view){
        var binding = LayoutTripBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_trip, parent, false))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        if(!callFromProfile) holder.binding.bgColor.setBackgroundColor(Color.WHITE)
        holder.binding.tvUserName.text = list[position].userName
        holder.binding.tvTripName.text= list[position].tripName
        holder.binding.tvDestination.text= list[position].destination
        var date= list[position].startDate + " to "+  list[position].endDate
        holder.binding.tvDate.text= date
        holder.binding.tvDestination.text= list[position].destination
        Glide.with(context).load(list[position].photo).into(holder.binding.ivTripImage)
        holder.binding.parentView.setOnClickListener{
            val intent1 = Intent(context, FullTripActivity::class.java)
            intent1.putExtra("tripId", list[position].tripId)
            context.startActivity(intent1)
        }

//        holder.itemView.setOnClickListener{
//            val intent= Intent(context, CatagoryActivity::class.java)
//            intent.putExtra("cat", list[position].cat)
//            context.startActivity(intent)
//
//        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
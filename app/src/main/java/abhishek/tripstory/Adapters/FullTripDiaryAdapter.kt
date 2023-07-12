package abhishek.tripstory.Adapters

import abhishek.tripstory.FullTripActivity
import abhishek.tripstory.R
import abhishek.tripstory.databinding.LayoutDiaryBinding
import abhishek.tripstory.databinding.LayoutDiarySingleTripBinding
import abhishek.tripstory.databinding.LayoutTripBinding
import abhishek.tripstory.model.tripCreationModel
import abhishek.tripstory.model.tripDiaryModel

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel


class FullTripDiaryAdapter(var context : Context, val list : ArrayList<tripDiaryModel>)
    : RecyclerView.Adapter<FullTripDiaryAdapter.CategoryViewHolder>(){
    inner class CategoryViewHolder(view : View) : RecyclerView.ViewHolder(view){
        var binding = LayoutDiarySingleTripBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_diary_single_trip, parent, false))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {


        holder.binding.tvLocation.text= list[position].location
        holder.binding.dateButton.text = list[position].date
        holder.binding.tvDiaryName.text = list[position].diaryName
        holder.binding.tvDescription.text= list[position].description
        val slideList = ArrayList<SlideModel>()
        for(data in list[position].imageList!!){
            slideList.add(SlideModel(data, ScaleTypes.CENTER_CROP))
        }
        holder.binding.imageSlider.setImageList(slideList)
        if (list[position].expand){
            holder.binding.tvDescription.visibility = View.VISIBLE
            holder.binding.tvSeeDescription.text="Tap to hide"
        } else {
            holder.binding.tvDescription.visibility = View.GONE
            holder.binding.tvSeeDescription.text="See Description"
        }
        holder.binding.tvSeeDescription.setOnClickListener{
            list[position].expand = !list[position].expand
            notifyItemChanged(position)
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
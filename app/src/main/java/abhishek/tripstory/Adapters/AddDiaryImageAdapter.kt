package abhishek.tripstory.Adapters

import abhishek.tripstory.databinding.LayoutImageBinding

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class AddDiaryImageAdapter(val list : ArrayList<Uri>) : RecyclerView.Adapter<AddDiaryImageAdapter.AddProductImageViewHolder>() {

    inner class  AddProductImageViewHolder(val binding: LayoutImageBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddProductImageViewHolder {
        val binding = LayoutImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddProductImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddProductImageViewHolder, position: Int) {
        holder.binding.itemImage.setImageURI(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
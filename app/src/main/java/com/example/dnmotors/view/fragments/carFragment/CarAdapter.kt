package com.example.dnmotors.view.fragments.carFragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dnmotors.R
import com.example.dnmotors.databinding.ItemCarBinding
class CarAdapter(
    private val cars: List<Car>,
    private val onItemClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    inner class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageCar: ImageView = itemView.findViewById(R.id.imageCar)
        val textPrice: TextView = itemView.findViewById(R.id.textPrice)
        val textBrandModel: TextView = itemView.findViewById(R.id.textBrandModel)
        val textYear: TextView = itemView.findViewById(R.id.textYear)
        private val likeButton: ImageButton = itemView.findViewById(R.id.btnLike)
        private var isLiked = false

        fun bind(car: Car) {
            // Здесь можно ставить данные в текст, картинку и т.д.

            likeButton.setOnClickListener {
                isLiked = !isLiked
                if (isLiked) {
                    likeButton.setImageResource(R.drawable.ic_like_filled)
                    likeButton.setColorFilter(Color.RED)
                } else {
                    likeButton.setImageResource(R.drawable.ic_like)
                    likeButton.setColorFilter(Color.GRAY)
                }
            }

            // обработка клика на всю карточку
//            itemView.setOnClickListener {
//                onCarClick(car)
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.textPrice.text = "${car.price} ₸"
        holder.textBrandModel.text = "${car.brand} ${car.model}"
        holder.textYear.text = "${car.year} y."
        holder.bind(cars[position])


        Glide.with(holder.itemView.context)
            .load(car.imageUrl) // если у тебя URL в базе
            .placeholder(R.drawable.tayota_camry_xv80)
            .into(holder.imageCar)

        holder.itemView.setOnClickListener {
            onItemClick(car)
        }
//        val bundle = Bundle()
//        bundle.putParcelable("car", car)
//        val fragment = CarDetailsFragment()
//        fragment.arguments = bundle
//
//        parentFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container_view, fragment)
//            .addToBackStack(null)
//            .commit()


    }


    override fun getItemCount(): Int = cars.size
}

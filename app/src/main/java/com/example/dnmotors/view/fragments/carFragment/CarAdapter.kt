package com.example.dnmotors.view.fragments.carFragment

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dnmotors.R
import com.example.dnmotors.view.fragments.favouritesFragment.FavouritesManager

class CarAdapter(
    private val cars: MutableList<Car>,
    private val onItemClick: (Car) -> Unit,
    private val isInFavourites: Boolean = false
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    inner class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageCar: ImageView = itemView.findViewById(R.id.imageCar)
        val textPrice: TextView = itemView.findViewById(R.id.textPrice)
        val textBrandModel: TextView = itemView.findViewById(R.id.textBrandModel)
        val textYear: TextView = itemView.findViewById(R.id.textYear)
        private val likeButton: ImageButton = itemView.findViewById(R.id.btnLike)

        fun bind(car: Car) {
            textPrice.text = "${car.price} ₸"
            textBrandModel.text = "${car.brand} ${car.model}"
            textYear.text = "${car.year} y."

            updateLikeButton(car.isLiked)

            likeButton.setOnClickListener {
                car.isLiked = !car.isLiked
                updateLikeButton(car.isLiked)

                if (car.isLiked) {
                    FavouritesManager.likedCars.add(car)
                } else {
                    FavouritesManager.likedCars.removeAll { it.vin == car.vin }

                    if (isInFavourites) {
                        val index = adapterPosition
                        if (index != RecyclerView.NO_POSITION && index < cars.size) {
                            cars.removeAt(index)
                            notifyItemRemoved(index)
                            return@setOnClickListener
                        }
                    }
                }

                notifyItemChanged(adapterPosition)
            }
        }

        private fun updateLikeButton(isLiked: Boolean) {
            if (isLiked) {
                likeButton.setImageResource(R.drawable.ic_like_filled)
                likeButton.setColorFilter(Color.RED)
            } else {
                likeButton.setImageResource(R.drawable.ic_like)
                likeButton.setColorFilter(Color.GRAY)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.bind(car)

        Glide.with(holder.itemView.context)
            .load(car.imageUrl.getOrNull(1) ?: car.imageUrl.firstOrNull() ?: "")
            .placeholder(R.drawable.tayota_camry_xv80)
            .into(holder.imageCar)

        holder.itemView.setOnClickListener {
            onItemClick(car)
        }
    }

    override fun getItemCount(): Int = cars.size

    fun updateList(newCars: List<Car>) {
        cars.clear()
        cars.addAll(newCars)
        notifyDataSetChanged()
    }
}

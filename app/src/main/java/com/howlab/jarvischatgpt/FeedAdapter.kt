package com.howlab.jarvischatgpt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import com.howlab.jarvischatgpt.databinding.ItemProductBinding
import com.howlab.jarvischatgpt.network.Product
import java.text.DecimalFormat
import java.text.NumberFormat

class FeedAdapter(
    private val onClick: (Product) -> Unit
) : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {

    private val products = mutableListOf<Product>()

    fun setProducts(list: List<Product>) {
        products.clear()
        products.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.thumbnailImage.load(product.thumbnailImage) {
                scale(Scale.FILL)
            }

            binding.productTitleText.text = product.title
            binding.locationTextView.text = product.location

            binding.productPriceTextView.text = product.price + " 원"

            binding.root.setOnClickListener {
                onClick.invoke(product)
            }
        }
    }
}
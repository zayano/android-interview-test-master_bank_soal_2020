package com.tokopedia.filter.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tokopedia.filter.R.layout.main_item
import com.tokopedia.filter.view.models.DataProduct
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.main_item.view.*
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.text.DecimalFormat

class AdapterProduct(private val context: Context, private val items: List<DataProduct>)
    : RecyclerView.Adapter<AdapterProduct.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(main_item, parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position])
    }
    override fun getItemCount(): Int = items.size

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
            LayoutContainer {
        private val df = DecimalFormat("###,###,###")

        @SuppressLint("SetTextI18n")
        fun bindItem(items: DataProduct) {
            containerView.title.text = items.name
            containerView.price.text = "Rp. "+df.format(items.priceInt).replace(',','.')
            containerView.city.text = items.shop.city

            try {
                val bitmap = BitmapFactory.decodeStream(URL(items.imageUrl).content as InputStream)
                containerView.image.setImageBitmap(bitmap)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}
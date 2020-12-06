package com.tokopedia.filter.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.StrictMode
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.tokopedia.filter.R
import com.tokopedia.filter.view.adapter.AdapterProduct
import com.tokopedia.filter.view.models.DataProduct
import com.tokopedia.filter.view.models.ShopDetail
import kotlinx.android.synthetic.main.activity_product.*
import kotlinx.android.synthetic.main.product_item.view.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.util.*
import kotlin.concurrent.schedule

class ProductActivity : AppCompatActivity() {

    private var dataProduct = mutableListOf<DataProduct>()
    private var city = mutableListOf<String>()
    private var prices = mutableListOf<Int>()
    private var filterPrice = mutableListOf<Float>()
    private var filterCity = mutableListOf<String>()
    private lateinit var productAdapter: AdapterProduct
    private lateinit var alert: AlertDialog.Builder
    private lateinit var loading: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder()
                .permitAll().build()
        StrictMode.setThreadPolicy(policy)

        alert = AlertDialog.Builder(this)
        alert.setCancelable(false)
        alert.setView(R.layout.product_load_more)
        loading = alert.create()
        loading.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        dataProduct = initData()
        productAdapter = AdapterProduct(this, dataProduct)
        product_list.layoutManager = GridLayoutManager(this, 2)
        product_list.adapter = productAdapter

    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        filter.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.product_item, null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
            val mAlertDialog = mBuilder.setCancelable(false).show()

            val df = DecimalFormat("###,###,###")
            val max = Collections.max(prices)
            val min = Collections.min(prices)

            mDialogView.price_max.text = "max\nRp. "+df.format(max).replace(',','.')
            mDialogView.price_min.text = "min\nRp. "+df.format(min).replace(',','.')

            mDialogView.price_slider.addOnChangeListener { slider, _, _ ->
                mDialogView.price_max.text = "max\nRp. "+df.format(slider.values[1]).replace(',','.')
                mDialogView.price_min.text = "min\nRp. "+df.format(slider.values[0]).replace(',','.')

            }

            mDialogView.btn_cancel.setOnClickListener {
                mAlertDialog.dismiss()
            }

            mDialogView.btn_reset.setOnClickListener {
                filterCity.clear()
                mDialogView.price_slider.values = arrayListOf(min.toFloat(), max.toFloat())

                mDialogView.chipsGroup.clearCheck()
                mDialogView.price_slider.values = arrayListOf(min.toFloat(), max.toFloat())

                val filter = filterData(initData(), filterCity, filterPrice)
                dataProduct.clear()
                dataProduct.addAll(filter)

                Timer("Waiting..", false).schedule(1000) {
                    product_list.post{
                        productAdapter.notifyDataSetChanged()
                    }
                }
            }

            mDialogView.price_slider.valueTo = max.toFloat()
            mDialogView.price_slider.valueFrom = min.toFloat()

            if(filterPrice.isEmpty())
                mDialogView.price_slider.values = arrayListOf(min.toFloat(), max.toFloat())
            else
                mDialogView.price_slider.values = arrayListOf(filterPrice[0], filterPrice[1])


            setCityChips(city, mDialogView.chipsGroup)

            mDialogView.btn_filter.setOnClickListener {
                filterPrice = mDialogView.price_slider.values
                loading.show()

                val filter = filterData(initData(), filterCity, filterPrice)
                dataProduct.clear()
                dataProduct.addAll(filter)

                Timer("Waiting..", false).schedule(1000) {
                    product_list.post{
                        productAdapter.notifyDataSetChanged()
                    }
                    loading.dismiss()
                    mAlertDialog.dismiss()
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun setCityChips(listCity: MutableList<String>, chipsGroup: ChipGroup) {
        for (city in listCity) {

            val mChip = this.layoutInflater.inflate(R.layout.item_chip, null, false) as Chip
            val paddingDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()

            mChip.text = city
            mChip.setPadding(paddingDp, 0, paddingDp, 0)

            if(filterCity.isNotEmpty())
                if(filterCity.contains(city))
                    mChip.isChecked = true

            mChip.setOnCheckedChangeListener { _, b ->
                if(b)
                    filterCity.add(mChip.text.toString())
                else
                    filterCity.remove(mChip.text.toString())

            }

            chipsGroup.addView(mChip)
        }
    }

    private fun filterData(
            product: MutableList<DataProduct>,
            list_city: MutableList<String>,
            price: MutableList<Float>): Collection<DataProduct>
    {

        val filterProduct = mutableListOf<DataProduct>()

        for(data in product){
            if(list_city.isNotEmpty())

                for(city in list_city) {
                    if (data.shop.city == city && data.priceInt >= price[0] && data.priceInt <= price[1]) {
                        filterProduct.add(data)
                    }
                }
            else {
                if (data.priceInt >= price[0] && data.priceInt <= price[1]) {
                    filterProduct.add(data)
                }
            }
        }
        return filterProduct
    }

    private fun initData() : MutableList<DataProduct> {
        val product = mutableListOf<DataProduct>()
        val inputStream: InputStream = resources.openRawResource(R.raw.products)
        val byteArrayOutputStream = ByteArrayOutputStream()

        var ctr: Int
        try {
            ctr = inputStream.read()
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr)
                ctr = inputStream.read()
            }
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            val jObject = JSONObject(byteArrayOutputStream.toString())
            val jObjectResult = jObject.getJSONObject("data")
            val jArray = jObjectResult.getJSONArray("products")

            for (i in 0 until jArray.length()) {
                val jShop = jArray.getJSONObject(i).getJSONObject("shop")
                val countries = jShop.getString("city")
                val price = jArray.getJSONObject(i).getInt("priceInt")

                if(!city.contains(countries))
                    city.add(countries)

                if(!prices.contains(price))
                    prices.add(price)

                product.add(
                        DataProduct(
                                jArray.getJSONObject(i).getInt("id"),
                                jArray.getJSONObject(i).getString("name"),
                                jArray.getJSONObject(i).getString("imageUrl"),
                                jArray.getJSONObject(i).getInt("priceInt"),
                                jArray.getJSONObject(i).getInt("discountPercentage"),
                                jArray.getJSONObject(i).getInt("slashedPriceInt"),
                                ShopDetail(
                                        jShop.getInt("id"),
                                        jShop.getString("name"),
                                        jShop.getString("city")
                                )
                        )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return product
    }
}
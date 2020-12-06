@file:Suppress("DEPRECATION")

package com.tokopedia.maps

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.JsonParser
import com.tokopedia.maps.model.ModelMaps
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
open class MapsActivity : AppCompatActivity() {

    private var mapFragment: SupportMapFragment? = null
    private var googleMap: GoogleMap? = null
    private var googlePlex: CameraPosition? = null

    private lateinit var textCountryName: TextView
    private lateinit var textCountryCapital: TextView
    private lateinit var textCountryPopulation: TextView
    private lateinit var textCountryCallCode: TextView

    private var editText: EditText? = null
    private var buttonSubmit: View? = null

    private var loading: ProgressDialog? = null
    private var alert: ProgressDialog? = null

    private var subscriptions = CompositeDisposable()

    private val dialogClickListener = DialogInterface.OnClickListener {
        dialog, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                dialog.dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        checkPermissions()
        bindViews()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        subscriptions = CompositeDisposable()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions,0)
    }

    private fun bindViews() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        editText = findViewById(R.id.editText)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        textCountryName = findViewById(R.id.txtCountryName)
        textCountryCapital = findViewById(R.id.txtCountryCapital)
        textCountryPopulation = findViewById(R.id.txtCountryPopulation)
        textCountryCallCode = findViewById(R.id.txtCountryCallCode)
        loading = ProgressDialog(this)
        alert = ProgressDialog(this)
    }

    @SuppressLint("SetTextI18n")
    private fun initListeners() {
        buttonSubmit!!.setOnClickListener {
            // TODO
            // search by the given country name, and
            // 1. pin point to the map
            // 2. set the country information to the textViews.
            loading?.setMessage("Searching Data ..")
            loading?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            loading?.show()
            loading?.setCancelable(false)

            val country = editText?.text.toString()
            subscriptions
                    .add(provideService()
                            .getCountry(country)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                resp ->
                                loading?.dismiss()
                                var status = 0

                                for(data in resp) {
                                    if (country.toLowerCase(Locale.getDefault()) == data.name.toLowerCase(Locale.getDefault())) {
                                        textCountryName.text = "Nama negara: " + data.name
                                        textCountryCapital.text = "Ibukota negara: " + data.capital
                                        textCountryCallCode.text = "Kode Panggilan: " + data.callingCodes[0]
                                        textCountryPopulation.text = "Jumlah Penduduk: " + data.population
                                        loadMap(data.latlng[0], data.latlng[1])
                                        status = 0
                                        break
                                    } else {
                                        for(altSpell in data.altSpellings){
                                            if(country.toLowerCase(Locale.getDefault()) == altSpell.toLowerCase(Locale.getDefault())){
                                                textCountryName.text = "Nama negara: " + data.name
                                                textCountryCapital.text = "Ibukota negara: " + data.capital
                                                textCountryCallCode.text = "Kode Panggilan: " + data.callingCodes[0]
                                                textCountryPopulation.text = "Jumlah Penduduk: " + data.population
                                                loadMap(data.latlng[0], data.latlng[1])
                                                status = 0
                                                break
                                            }
                                            else{
                                                status = 1
                                            }
                                        }
                                    }
                                }
                                if(status == 1) {
                                    val builder = AlertDialog.Builder(this)
                                    val message = "Not Found"
                                    builder.setMessage(message)
                                            .setPositiveButton("OK", dialogClickListener)
                                            .setCancelable(false).show()
                                }

                            }, {
                                err ->
                                loading?.dismiss()
                                val builder = AlertDialog.Builder(this)
                                var message = "An Error Occurred"
                                if (err is HttpException) {
                                    val data = err.response()?.errorBody()?.string()
                                    message = JsonParser().parse(data).asJsonObject["message"].asString
                                    builder.setMessage(message)
                                            .setPositiveButton("OK", dialogClickListener)
                                            .setCancelable(false).show()
                                }
                                else{
                                    message = err.message ?: message
                                    builder.setMessage(""+ message)
                                            .setPositiveButton("OK", dialogClickListener)
                                            .setCancelable(false).show()

                                }
                            }))

        }
    }

    private fun loadMap(lat: Double, lng:Double) {
        mapFragment!!.getMapAsync {
            googleMap ->
            this@MapsActivity.googleMap = googleMap
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            googleMap.clear() //clear old markers

            googlePlex = CameraPosition.builder()
                    .target(LatLng(lat, lng))
                    .bearing(0f)
                    .tilt(45f)
                    .build()

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null)
            googleMap.addMarker(
                    MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_position))
                            .position(LatLng(lat, lng)))
        }
    }

    private fun provideService():Service{
        val clientBuilder: OkHttpClient.Builder = buildClient()
        val retrofit = Retrofit.Builder()
                .baseUrl("https://restcountries-v1.p.rapidapi.com/")
                .client(clientBuilder
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .addInterceptor { chain ->
                            val newRequest = chain.request().newBuilder()
                                    .addHeader("x-rapidapi-host", "restcountries-v1.p.rapidapi.com")
                                    .addHeader("x-rapidapi-key", "372cb821c0msh62a692d41b70225p15f260jsn36cc3e7e949b")
                                    .build()
                            chain.proceed(newRequest)
                        }
                        .retryOnConnectionFailure(true)
                        .build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
        return retrofit.create(Service::class.java)
    }

    private fun buildClient(): OkHttpClient.Builder {
        val clientBuilder = OkHttpClient.Builder()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        clientBuilder.addInterceptor(loggingInterceptor)
                .connectTimeout(5 , TimeUnit.MINUTES)
        return clientBuilder
    }

    interface Service {
        @GET("name/{country}")
        fun getCountry(@Path("country")country:String): Observable<MutableList<ModelMaps>>
    }

    override fun onPause() {
        super.onPause()
        subscriptions.clear()
    }
}

package com.example.friendlist

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.friendlist.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    //**********************************************************************************************
    //
    //ALL VARIABLES THAT CONTROL THINGS FROM PERMISSIONS TO API CALLS
    //
    //**********************************************************************************************

    //variable for stupid false positive fire on permissions
    var ignore = true;

    //setup main access
    private lateinit var mainEnv: ActivityMainBinding

    //variables for layouts and adapters
    private var layoutManager: RecyclerView.LayoutManager? = null;

    //variables for location
    private var currentLocation: Location? = null
    lateinit var locationManager: LocationManager
    lateinit var locationByGps: Location

    //permissions vars
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isLocationPermissionGranted = false
    private var isRecordPermissionGrated = false

    //forecast data
    lateinit var UMSLForecast: Forecast
    lateinit var CurrentForecast: Forecast

    //vars related to severe weather notification
    lateinit var notificationChannel: NotificationChannel
    lateinit var notificationManager: NotificationManager
    lateinit var builder: Notification.Builder
    private val channelId = "12345"
    private val description = "Test Notification"

//**********************************************************************************************
    //
    //Function to catch permission requests and reload the app when granted or denied
    //
    //**********************************************************************************************

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //ignore once
        if (!ignore) {

            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) {

                //restart the activity
                var intent = Intent(applicationContext, MainActivity::class.java);
                startActivity(intent);
            }

            else{
                finish();
            }

        }

        ignore = !ignore;
    }

    //**********************************************************************************************
    //
    //Function to request location permissions
    //
    //**********************************************************************************************
    private fun requestPermissions(){

        var reset = false;

        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        isRecordPermissionGrated = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED


        val permissionList: MutableList<String> = ArrayList()

        if(!isLocationPermissionGranted){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if(permissionList.isNotEmpty()){
            permissionLauncher.launch(permissionList.toTypedArray())
        }

    }

    //**********************************************************************************************
    //
    //Function to handle getting current weather
    //
    //**********************************************************************************************
    fun FetchCurrentWeather(Lat: Double, Lon: Double){

        val request = CurrentWeatherServiceBuilder.buildService(CurrentWeatherInterface::class.java)
        val call = request.getMovies("1c7fb8efde83d3f121efc469262363f7", Lat.toString(), Lon.toString(), "metric")

        //make the request
        call.enqueue(object: Callback<CurrentWeather>{
            override fun onResponse(
                call: Call<CurrentWeather>,
                response: Response<CurrentWeather>
            ) {
                if (response.isSuccessful) {

                    //set all text
                    mainEnv.currentTemp.text = "Temperature: " + response.body()!!.main.temp.toInt().toString() + "°c";
                    mainEnv.currentForecast.text = "Forecast: " + response.body()!!.weather[0].main;
                    mainEnv.currentHumidity.text = "Humidity: " + response.body()!!.main.humidity.toInt().toString();

                    //set map location
                    val mapFragment = supportFragmentManager.findFragmentById(
                        R.id.map_fragment
                    ) as? SupportMapFragment
                    mapFragment?.getMapAsync { googleMap ->
                        googleMap.addMarker(MarkerOptions().title("You").position(LatLng(Lat,Lon)))
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(Lat,Lon), 15f))
                    }
                }
                else {
                    Toast.makeText(this@MainActivity, "Failed Retrieval of Weather", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CurrentWeather>, t: Throwable) {
                Toast.makeText(this@MainActivity, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    //**********************************************************************************************
    //
    //Function to handle getting UMSL weather
    //
    //**********************************************************************************************
    fun FetchUMSLWeather(Lat: Double, Lon: Double){

        val request = CurrentWeatherServiceBuilder.buildService(CurrentWeatherInterface::class.java)
        val call = request.getMovies("1c7fb8efde83d3f121efc469262363f7", "38.709011", "-90.310287", "metric")

        //make the request
        call.enqueue(object: Callback<CurrentWeather>{
            override fun onResponse(
                call: Call<CurrentWeather>,
                response: Response<CurrentWeather>
            ) {
                if (response.isSuccessful) {

                    //set all text
                    mainEnv.UMSLTemp.text = "Temperature: " + response.body()!!.main.temp.toInt().toString() + "°c";
                    mainEnv.UMSLForecast.text = "Forecast: " + response.body()!!.weather[0].main;
                    mainEnv.UMSLHumidity.text = "Humidity: " + response.body()!!.main.humidity.toInt().toString();

                    //set the umsl weather icon
                    if(response.body()!!.weather[0].id > 800)
                        mainEnv.weatherico.setImageResource(R.drawable.cloudy);
                    else if(response.body()!!.weather[0].id > 799 && response.body()!!.weather[0].id < 801)
                        mainEnv.weatherico.setImageResource(R.drawable.sunny);
                    else if(response.body()!!.weather[0].id > 700)
                        mainEnv.weatherico.setImageResource(R.drawable.atmosphere);
                    else if(response.body()!!.weather[0].id > 600)
                        mainEnv.weatherico.setImageResource(R.drawable.snow);
                    else if(response.body()!!.weather[0].id > 500)
                        mainEnv.weatherico.setImageResource(R.drawable.rain);
                    else if(response.body()!!.weather[0].id > 300)
                        mainEnv.weatherico.setImageResource(R.drawable.rain);
                    else if(response.body()!!.weather[0].id > 200)
                        mainEnv.weatherico.setImageResource(R.drawable.thunder);
                }
                else {
                    Toast.makeText(this@MainActivity, "Failed Retrieval of Weather", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CurrentWeather>, t: Throwable) {
                Toast.makeText(this@MainActivity, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //**********************************************************************************************
    //
    //Function to making and showing t-storm warning if needed
    //
    //**********************************************************************************************
    fun checkNotification(forecast: Forecast){

        //check to see if there is severe weather
        var showNot = false
        for (x in arrayOf(1, 2, 3, 4, 5)) {
            if (forecast.list[x].weather[0].id.toInt() < 300 && forecast.list[x].weather[0].id.toInt() > 200)
                showNot = true
        }
        if (showNot) {

            //make intent
            val intent = Intent(this, LauncherActivity::class.java)

            //build intent target action and paramters
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            //Create arbitrary channel for sending notification
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)

            //build the notification
            builder = Notification.Builder(this, channelId)
                .setContentTitle("SEVERE WEATHER WARNING AT UMSL")
                .setContentText("Thunderstorms Are Expected in the Next 15 Hours At UMSL")
                .setSmallIcon(R.drawable.rain).setLargeIcon(
                    BitmapFactory.decodeResource(this.resources, R.drawable.ic_launcher_background)
                ).setContentIntent(pendingIntent)

            //send notification
            notificationManager.notify(12345, builder.build())
        }
    }

    //**********************************************************************************************
    //
    //Function to handle getting Forecasts
    //
    //**********************************************************************************************
    fun FetchForecast(Lat: Double, Lon: Double, Run: Int){

        val umslLat = 38.709011
        val umslLon = -90.310287
        var call: Call<Forecast>

        if(Run == 0){
            val request = ForecastServiceBuilder.buildService(ForecastInterface::class.java)
            call = request.getForecast("1c7fb8efde83d3f121efc469262363f7", Lat.toString(), Lon.toString(), "metric")
        }
        else {
            val request = ForecastServiceBuilder.buildService(ForecastInterface::class.java)
            call = request.getForecast("1c7fb8efde83d3f121efc469262363f7", umslLat.toString(), umslLon.toString(), "metric")
        }

        //make the request
        call.enqueue(object: Callback<Forecast>{
            override fun onResponse(
                call: Call<Forecast>,
                response: Response<Forecast>
            ) {
                if (response.isSuccessful) {

                    //set first call's results to current location's forecast
                    // and recurse
                    if(Run == 0) {
                        CurrentForecast = response.body()!!
                        FetchForecast(1.0, 1.0, 1);
                    }

                    //set second call's results to umsl location's forecast
                    //and check for severe weather notification
                    if(Run == 1)
                        UMSLForecast = response.body()!!
                        checkNotification(response.body()!!);
                }
                else {
                    Toast.makeText(this@MainActivity, "Failed Retrieval of Forecast", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Forecast>, t: Throwable) {
                Toast.makeText(this@MainActivity, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //**********************************************************************************************
    //
    //Function to handle setting forecast results
    //
    //**********************************************************************************************
    fun SetForeCast(pos: Int){

        //set umsl forecast
        if (UMSLForecast != null) {

            //set all text
            mainEnv.UMSLTemp.text = "Temperature: " + UMSLForecast.list[pos].main.temp.toInt().toString() + "°c";
            mainEnv.UMSLForecast.text = "Forecast: " + UMSLForecast.list[pos].weather[0].main;
            mainEnv.UMSLHumidity.text = "Humidity: " + UMSLForecast.list[pos].main.humidity.toInt().toString();

            //set the umsl weather icon
            var ident = UMSLForecast.list[pos].weather[0].id
            if(ident > 800)
                mainEnv.weatherico.setImageResource(R.drawable.cloudy);
            else if(ident > 799 && ident < 801)
                mainEnv.weatherico.setImageResource(R.drawable.sunny);
            else if(ident > 700)
                mainEnv.weatherico.setImageResource(R.drawable.atmosphere);
            else if(ident > 600)
                mainEnv.weatherico.setImageResource(R.drawable.snow);
            else if(ident > 500)
                mainEnv.weatherico.setImageResource(R.drawable.rain);
            else if(ident > 300)
                mainEnv.weatherico.setImageResource(R.drawable.rain);
            else if(ident > 200)
                mainEnv.weatherico.setImageResource(R.drawable.thunder);
        }

        //set current forecase
        if(CurrentForecast != null){

            //set all text
            mainEnv.currentTemp.text = "Temperature: " + CurrentForecast.list[pos].main.temp.toInt().toString() + "°c";
            mainEnv.currentForecast.text = "Forecast: " + CurrentForecast.list[pos].weather[0].main;
            mainEnv.currentHumidity.text = "Humidity: " + CurrentForecast.list[pos].main.humidity.toInt().toString();
        }
    }

    //**********************************************************************************************
    //
    // MAIN()
    //
    //**********************************************************************************************
    override fun onCreate(savedInstanceState: Bundle?) {

        //really? Android 12 async change???
        var latitude = 1.0
        var longitude = 1.0

        super.onCreate(savedInstanceState)

        //bind
        mainEnv = ActivityMainBinding.inflate(layoutInflater);

        //set env
        setContentView(mainEnv.root);

        //remove the title bar
        this.supportActionBar!!.hide();

        //setup notification service
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //make tie to layout manager for ease of any future recyclerview setup
        layoutManager = LinearLayoutManager(applicationContext);

        //setup permissions
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                permissions ->
            isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION]?:  isLocationPermissionGranted
        }
        requestPermissions();

        //setup location service
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        //setup listeners for location
        val gpsLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByGps = location
            }

            //other functions that have to be overloaded as well
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        //make sure we have gps before proceeding further
        if (hasGps) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions();
                return
            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                0F,
                gpsLocationListener
            )
        }

        //retreive the lat and long locations from gps
        val lastKnownLocationByGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocationByGps?.let {

            //set the values once we're sure the aync call has returned
            //(Android 12) or set immediately for android 10 and down
            locationByGps = lastKnownLocationByGps
            currentLocation = locationByGps
            latitude = currentLocation!!.latitude
            longitude = currentLocation!!.longitude


            //set up the spinner with time options
            var spinner = mainEnv.timeSpinner
            var times = arrayOf(
                "Current",
                "3 Hours From Now",
                "6 Hours From Now",
                "9 Hours From Now",
                "12 Hours From Now",
                "15 Hours From Now"
            )

            if (spinner != null) {

                //setup simple array adapter for populating spinner
                val adapter = ArrayAdapter(this, R.layout.spinner_list, times)
                spinner.adapter = adapter

                //setup selection listeners
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (position != 0)

                            //set forecast to match hour position
                            SetForeCast(position);
                        else {

                            //refetch 'cause I'm lazy
                            FetchCurrentWeather(latitude, longitude);
                            FetchUMSLWeather(latitude, longitude);
                        }
                    }
                }
            }

            //get the data for future weather for both umsl and current
            FetchForecast(latitude, longitude, 0);
        }
    }
}
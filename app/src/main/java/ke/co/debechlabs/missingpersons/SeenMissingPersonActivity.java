package ke.co.debechlabs.missingpersons;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import ke.co.debechlabs.missingpersons.Config.Config;
import ke.co.debechlabs.missingpersons.app.AppController;
import ke.co.debechlabs.missingpersons.util.CircularNetworkImageView;

public class SeenMissingPersonActivity extends ActionBarActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "Sighting";
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;

    private int year;
    private int month;
    private int day;

    private String id, name, url;

    CircularNetworkImageView personImage;
    CardView formCard;
    TextView txtPersonName, txtChosenCoordinates, txtChosenPlaceName;
    EditText etxDateSeen;
    ImageLoader imageLoader;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seen_missing_person);

        id = getIntent().getStringExtra("id");
        name = getIntent().getStringExtra("name");
        url = getIntent().getStringExtra("photo");

        personImage = (CircularNetworkImageView) findViewById(R.id.personPhoto);
        txtPersonName = (TextView) findViewById(R.id.personName);
        txtChosenCoordinates = (TextView) findViewById(R.id.chosenCoordinates);
        txtChosenPlaceName = (TextView) findViewById(R.id.chosenName);
        etxDateSeen = (EditText) findViewById(R.id.input_date_seen);
        btnSubmit = (Button) findViewById(R.id.submit_sighting);
        formCard = (CardView) findViewById(R.id.formCard);
        imageLoader = AppController.getInstance().getImageLoader();

        personImage.setImageUrl(url, imageLoader);
        personImage.bringToFront();
        personImage.setElevation(10);
        formCard.setElevation(5);
        txtPersonName.setText(name);

        setCurrentDateOnView();
        etxDateSeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(999);
            }
        });

        btnSubmit.setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng placeLatLng = place.getLatLng();

                double lat = placeLatLng.latitude;
                double lng = placeLatLng.longitude;
                goToLocationZoom(lat, lng, 15);

                String coordinatesString = new StringBuilder()
                        .append(lat)
                        .append(",")
                        .append(lng)
                        .toString();

                txtChosenCoordinates.setText(coordinatesString);
                txtChosenPlaceName.setText(place.getName());

                mMap.addMarker(new MarkerOptions().position(placeLatLng).title(place.getName().toString()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng));
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Report Sighting");

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id){
            case 999:
                return new DatePickerDialog(this, datePickerListener,
                        year, month,day);
        }
        return super.onCreateDialog(id);
    }

    private void goToLocationZoom(double lat, double lng, float zoom) {
        LatLng latlng = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, zoom);
        mMap.moveCamera(cameraUpdate);
    }

    public void setCurrentDateOnView() {
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        // set current date into textview
        etxDateSeen.setText(new StringBuilder()
                // Month is 0 based, just add 1
                .append(day).append("-")
                .append(month + 1).append("-")
                .append(year).append(" "));

    }

    private DatePickerDialog.OnDateSetListener datePickerListener
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;

            view.setMaxDate(System.currentTimeMillis());

            // set selected date into textview
            etxDateSeen.setText(new StringBuilder()
                    .append(day).append("-")
                    .append(month + 1).append("-")
                    .append(year).append(" "));

            // set selected date into datepicker also
//            dpResult.init(year, month, day, null);

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submit_sighting:
                submitSighting();
                break;
        }
    }

    private void submitSighting() {
        final String coordinates = txtChosenCoordinates.getText().toString();
        final String placename = txtChosenPlaceName.getText().toString();
        final String dateseen = etxDateSeen.getText().toString();

        if (TextUtils.isEmpty(coordinates)){
            new AlertDialog.Builder(this)
                    .setTitle("Oops..")
                    .setMessage("You have to pick a location in order to submit this sighting. Simply search and pick the actual location")
                    .setPositiveButton("OK", null)
                    .show();

            return;
        }

        final ProgressDialog pdialog = new ProgressDialog(this);
        pdialog.setTitle("Adding Submission");
        pdialog.setMessage("Please Wait...");
        pdialog.show();

        String url = Config.server_url + "API/MissingPersons/addSighting";

       StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
           @Override
           public void onResponse(String response) {
               pdialog.hide();
               try {
                   JSONObject responseObject = new JSONObject(response);
                   Toast.makeText(SeenMissingPersonActivity.this, responseObject.getString("message"), Toast.LENGTH_SHORT).show();
                   JSONObject dataObj = responseObject.getJSONObject("data");
                   int id = dataObj.getInt("id");
                   onBackPressed();
                   finish();
               } catch (JSONException e) {
                   e.printStackTrace();
               }

           }
       }, new Response.ErrorListener() {
           @Override
           public void onErrorResponse(VolleyError error) {
               pdialog.hide();
               new AlertDialog.Builder(SeenMissingPersonActivity.this)
                       .setTitle("Error")
                       .setMessage(error.getMessage())
                       .setPositiveButton("RETRY", null)
                       .show();
           }
       }){
           @Override
           protected Map<String, String> getParams() throws AuthFailureError {
               HashMap<String, String> params = new HashMap<String, String>();

               FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

               params.put("person_id", id);
               params.put("place_coordinates", coordinates);
               params.put("place_name", placename);
               params.put("date_seen", dateseen);
               params.put("sentby", String.valueOf("[" + user.getProviderId() + "]" + user.getEmail()));
               return params;
           }
       };

       stringRequest.setRetryPolicy(new DefaultRetryPolicy(
               0,
               DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
               DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

       AppController.getInstance().addToRequestQueue(stringRequest);
    }
}

package ke.co.debechlabs.missingpersons;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import ke.co.debechlabs.missingpersons.Config.Config;
import ke.co.debechlabs.missingpersons.adapters.SightingListAdapter;
import ke.co.debechlabs.missingpersons.app.AppController;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import ke.co.debechlabs.missingpersons.models.Sighting;
import ke.co.debechlabs.missingpersons.util.CircularNetworkImageView;

public class PersonSightings extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    String id, name, url, missingDate, sightings;

    CircularNetworkImageView personImage;
    ImageLoader imageLoader;

    TextView txt_personName, txt_missingDate, txt_numberofSightings;
    ListView sightingList;

    SightingListAdapter sightingListAdapter;
    List<Sighting> sightingLists = new ArrayList<Sighting>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_sightings);


        id = getIntent().getStringExtra("id");
        name = getIntent().getStringExtra("name");
        url = getIntent().getStringExtra("photo");
        imageLoader = AppController.getInstance().getImageLoader();
        missingDate = getIntent().getStringExtra("missingDate");
        sightings = getIntent().getStringExtra("sightings");

        personImage = (CircularNetworkImageView) findViewById(R.id.personImage);
        txt_personName  = (TextView) findViewById(R.id.personName);
        txt_missingDate = (TextView) findViewById(R.id.missingDate);
        txt_numberofSightings = (TextView) findViewById(R.id.numberofSightings);
        sightingList = (ListView) findViewById(R.id.sightingsList);

        personImage.setImageUrl(url, imageLoader);
        txt_personName.setText(name);
        txt_missingDate.setText("Went Missing on: " + Config.FormatDateString(missingDate, "dd/MM/yyyy", "MMMM dd, yyyy"));
        txt_numberofSightings.setText("Sightings: " + sightings);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        sightingListAdapter = new SightingListAdapter(this, sightingLists);
        sightingList.setAdapter(sightingListAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fetchData();
    }


    private void fetchData(){
        String url = Config.server_url + "API/MissingPersons/getPersonSightings/" + id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Boolean status = response.getBoolean("status");
                    if (status == true){
                        JSONArray data = response.getJSONArray("data");
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject personObject = data.getJSONObject(i);
                            Sighting sighting = new Sighting();

                            sighting.setId(personObject.getInt("id"));
                            sighting.setPerson_id(personObject.getInt("person_id"));
                            sighting.setLocation(personObject.getString("place_name"));
                            sighting.setCoordinates(personObject.getString("place_coordinates"));
                            sighting.setDateseen(personObject.getString("date_seen"));
                            sighting.setDateadded(personObject.getString("date_created"));
                            sighting.setValidated(personObject.getString("validated"));
                            String coordinates[] = personObject.getString("place_coordinates").split(",");
                            double lat = Double.parseDouble(coordinates[0]);
                            double lng = Double.parseDouble(coordinates[1]);

                            builder.include(new LatLng(lat, lng));

                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lng))
                                    .title(personObject.getString("place_name"))
                                    .snippet("(Click to mark this as the location the person was found)")
                            );
                            sightingLists.add(sighting);
                            sightingListAdapter.notifyDataSetChanged();

                        }
                        LatLngBounds bounds = builder.build();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
                    }else{
                        Toast.makeText(PersonSightings.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }
}

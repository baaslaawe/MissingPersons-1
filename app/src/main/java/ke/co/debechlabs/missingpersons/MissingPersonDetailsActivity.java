package ke.co.debechlabs.missingpersons;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.Manifest;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import ke.co.debechlabs.missingpersons.Config.Config;
import ke.co.debechlabs.missingpersons.app.AppController;

public class MissingPersonDetailsActivity extends AppCompatActivity implements View.OnClickListener{
    int id;
    CollapsingToolbarLayout collapsingToolbarLayout;

    ImageLoader imageLoader;
    NetworkImageView header;
    TextView txtPersonID, txtSightings, txtPhotoURL, txtPersonName, txtObNumber, txtDateSeen, txtLastSeenAt, txtAge, txtLastWearing, txtPhysicalDisabilities, txtBirthMarks, txtContactPerson, txtContactPersonNumber, txtAltContactPerson, txtAltContactPersonNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missing_person_details);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.MyToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        id = getIntent().getIntExtra("id", 0);
        imageLoader = AppController.getInstance().getImageLoader();
        header = (NetworkImageView) findViewById(R.id.bgheader);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        txtLastSeenAt = (TextView) findViewById(R.id.last_seen_at);
        txtAge = (TextView) findViewById(R.id.person_age);
        txtLastWearing = (TextView) findViewById(R.id.last_seen_wearing);
        txtPhysicalDisabilities = (TextView) findViewById(R.id.physical_disabilities);
        txtContactPerson = (TextView) findViewById(R.id.contact_person);
        txtContactPersonNumber = (TextView) findViewById(R.id.contact_person_number);
        txtBirthMarks = (TextView) findViewById(R.id.birth_marks);
        txtAltContactPerson = (TextView) findViewById(R.id.alt_contact_person);
        txtAltContactPersonNumber = (TextView) findViewById(R.id.alt_contact_person_number);
        txtObNumber = (TextView) findViewById(R.id.ob_number_txt);
        txtPersonName = (TextView) findViewById(R.id.personName);
        txtDateSeen = (TextView) findViewById(R.id.last_seen_date);
        txtPersonID = (TextView) findViewById(R.id.person_id);
        txtPhotoURL = (TextView) findViewById(R.id.photo_url);
        txtSightings = (TextView) findViewById(R.id.person_sightings);

        Context context = this;
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBarPlus1);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBarPlus1);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.colorIcons));
        collapsingToolbarLayout.setTitle("");
//        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(context, R.color.colorPrimary));
        fetchData();


    }

    private void fetchData(){
        if (this.id != 0){
            String url  = Config.server_url + "API/MissingPersons/getByID/" + this.id;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject personObj = response.getJSONObject("data");

                        header.setImageUrl(Config.server_url + personObj.getString("missing_person_image"), imageLoader);
                        txtPersonID.setText(personObj.getString("id"));
                        txtSightings.setText(personObj.getString("sightings"));
                        txtObNumber.setText(personObj.getString("ob_number"));
                        txtPersonName.setText(personObj.getString("missing_person_name"));
                        collapsingToolbarLayout.setTitle(personObj.getString("missing_person_name"));
                        txtLastSeenAt.setText(personObj.getString("missing_person_location_last_seen"));
                        txtAge.setText(personObj.getString("missing_person_age"));
                        txtLastWearing.setText(personObj.getString("missing_person_last_wearing"));
                        txtPhysicalDisabilities.setText(personObj.getString("missing_person_disabilities"));
                        txtContactPerson.setText(personObj.getString("missing_person_contact_person"));
                        txtContactPersonNumber.setText(personObj.getString("missing_person_contact_person_number"));
                        txtBirthMarks.setText(personObj.getString("missing_person_birth_marks"));
                        txtPhotoURL.setText(Config.server_url + personObj.getString("missing_person_image"));
                        txtDateSeen.setText(Config.FormatDateString(personObj.getString("missing_person_date_last_seen"), "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MissingPersonDetailsActivity.this, "There was an error fetching the person", Toast.LENGTH_SHORT).show();
                }
            });

            AppController.getInstance().addToRequestQueue(jsonObjectRequest);
        }else{
            Intent i = new Intent(MissingPersonDetailsActivity.this, MissingPersonsListActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.missing_person_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_poster:
                Intent intent = new Intent(MissingPersonDetailsActivity.this, PosterActivity.class);
                intent.putExtra("id", txtPersonID.getText().toString());
                startActivity(intent);
                break;
            case R.id.action_share:
                break;

            case R.id.action_report_sighting:
                Intent seenIntent = new Intent(MissingPersonDetailsActivity.this, SeenMissingPersonActivity.class);
                seenIntent.putExtra("id", txtPersonID.getText().toString());
                seenIntent.putExtra("name", txtPersonName.getText().toString());
                seenIntent.putExtra("photo", txtPhotoURL.getText().toString());
                seenIntent.putExtra("missingDate", txtDateSeen.getText().toString());
                startActivity(seenIntent);
                break;

            case R.id.action_view_sightings:
                Intent viewSightingIntent = new Intent(MissingPersonDetailsActivity.this, PersonSightings.class);
                viewSightingIntent.putExtra("id", txtPersonID.getText().toString());
                viewSightingIntent.putExtra("name", txtPersonName.getText().toString());
                viewSightingIntent.putExtra("photo", txtPhotoURL.getText().toString());
                viewSightingIntent.putExtra("missingDate", txtDateSeen.getText().toString());
                viewSightingIntent.putExtra("sightings", txtSightings.getText().toString());
                startActivity(viewSightingIntent);
                break;
        }
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.seenPerson:
                Intent intent = new Intent(MissingPersonDetailsActivity.this, SeenMissingPersonActivity.class);
                startActivity(intent);
                break;
        }
    }
}

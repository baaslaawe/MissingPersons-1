package ke.co.debechlabs.missingpersons;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ke.co.debechlabs.missingpersons.Config.Config;
import ke.co.debechlabs.missingpersons.adapters.SpinnerAdapter;
import ke.co.debechlabs.missingpersons.app.AppController;
import ke.co.debechlabs.missingpersons.models.Relationship;
import ke.co.debechlabs.missingpersons.modules.AppHelper;
import ke.co.debechlabs.missingpersons.modules.VolleyMultipartRequest;

import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

public class NewFamilyMember extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    List<Relationship> relationships = new ArrayList<Relationship>();
    Spinner relationshipSpinner;
    EditText etxFirstName, etxLastName, etxDOB;
    TextView txtphotoPath;
    TextView txtSelectedRelationship;
    Button btnAddPhoto, btnAddMember;
    ImageView imgProfile;
    SpinnerAdapter spinnerAdapter;
    private AwesomeValidation mAwesomeValidation;
    private int year;
    private int month;
    private int day;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_family_member);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("New Member");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mAwesomeValidation = new AwesomeValidation(BASIC);

        mAwesomeValidation.addValidation(this, R.id.input_firstname, "[a-zA-Z\\s]+", R.string.firstname_error);
        mAwesomeValidation.addValidation(this, R.id.input_lastname, "[a-zA-Z\\s]+", R.string.lastname_error);

        relationshipSpinner = (Spinner) findViewById(R.id.relationship);
        btnAddPhoto = (Button) findViewById(R.id.uploadImage);
        btnAddMember = (Button) findViewById(R.id.addMember);
        imgProfile = (ImageView) findViewById(R.id.familyMemberPhoto);
        txtphotoPath = (TextView) findViewById(R.id.photoPath);
        etxFirstName = (EditText) findViewById(R.id.input_firstname);
        etxLastName = (EditText) findViewById(R.id.input_lastname);
        etxDOB = (EditText) findViewById(R.id.input_date_birth);
        txtSelectedRelationship = (TextView) findViewById(R.id.selectedRelationshipID);

        spinnerAdapter = new SpinnerAdapter(this, relationships);
        relationshipSpinner.setAdapter(spinnerAdapter);
        btnAddPhoto.setOnClickListener(this);
        etxDOB.setOnClickListener(this);
        btnAddMember.setOnClickListener(this);
        relationshipSpinner.setOnItemSelectedListener(this);
        setCurrentDateOnView();
        getRelationships();

        relationshipSpinner.setSelection(0);
    }

    private void getRelationships(){
        String url = Config.server_url + "API/MissingPersons/getRelations";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseObj = new JSONObject(response);
                    JSONArray dataArray = responseObj.getJSONArray("data");

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataRow = dataArray.getJSONObject(i);
                        Relationship r = new Relationship();

                        r.set_id(dataRow.getInt("id"));
                        r.set_relationship(dataRow.getString("relationship_name"));

                        relationships.add(r);
                        spinnerAdapter.notifyDataSetChanged();
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

        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.uploadImage:
                startCropActivity();
                break;
            case R.id.input_date_birth:
                openCalendarDialog();
                break;
            case R.id.addMember:
                submitMember();
                break;
        }
    }

    private void submitMember() {
        if (!mAwesomeValidation.validate()){
            return;
        }

        String photoPath = txtphotoPath.getText().toString();
        final String relationship = txtSelectedRelationship.getText().toString();
        if (TextUtils.isEmpty(photoPath)){
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Please upload a photo")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        if (TextUtils.isEmpty(relationship)){
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Please choose a relationship")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        final String firstname = etxFirstName.getText().toString();
        final String lastname = etxLastName.getText().toString();
        final String date_of_birth = etxDOB.getText().toString();

        String url = Config.server_url + "API/MissingPersons/addFamilyMember";
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please Wait...");
        pDialog.setCancelable(false);
        pDialog.setTitle("Adding Family Member");
        pDialog.show();

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                pDialog.hide();
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    Toast.makeText(NewFamilyMember.this, result.getString("message"), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(NewFamilyMember.this, FamilyProfileActivity.class);
                    startActivity(intent);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.hide();
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                }else{
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                new android.app.AlertDialog.Builder(NewFamilyMember.this)
                        .setTitle("Error")
                        .setMessage(errorMessage)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, null).show();
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("firstname", firstname);
                params.put("lastname", lastname);
                params.put("date_of_birth", date_of_birth);
                params.put("relationship", relationship);
                params.put("user_id", Config.getUserID());

                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                Map<String, DataPart> params = new HashMap<>();
                Drawable d = Drawable.createFromPath(txtphotoPath.getText().toString());
                String mimeType = Config.getMimeType(txtphotoPath.getText().toString());
                params.put("memberPhoto", new DataPart(txtphotoPath.getText().toString(),
                        AppHelper.getFileDataFromDrawable(getBaseContext(), d), mimeType));
                return params;
            }
        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(volleyMultipartRequest);
    }

    private void openCalendarDialog() {
        showDialog(999);
    }

    public void setCurrentDateOnView() {
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        // set current date into textview
        etxDOB.setText(new StringBuilder()
                // Month is 0 based, just add 1
                .append(day).append("-")
                .append(month + 1).append("-")
                .append(year).append(" "));

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
            etxDOB.setText(new StringBuilder()
                    .append(day).append("-")
                    .append(month + 1).append("-")
                    .append(year).append(" "));

            // set selected date into datepicker also
//            dpResult.init(year, month, day, null);

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    imgProfile.setImageURI(result.getUri());
                    txtphotoPath.setText(result.getUri().getPath());
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void startCropActivity() {
        CropImage.activity(null)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setFixAspectRatio(true)
                .start(NewFamilyMember.this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        TextView relationshipID = (TextView) view.findViewById(R.id.contentID);
        txtSelectedRelationship.setText(relationshipID.getText().toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}

package ke.co.debechlabs.missingpersons;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import ke.co.debechlabs.missingpersons.Config.Config;
import ke.co.debechlabs.missingpersons.Session.Session;
import ke.co.debechlabs.missingpersons.app.AppController;
import ke.co.debechlabs.missingpersons.modules.AppHelper;
import ke.co.debechlabs.missingpersons.modules.VolleyMultipartRequest;

import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

public class NewMissingPersonActivity extends AppCompatActivity {
    private static final String TAG = "New Missing Person";
    private AwesomeValidation mAwesomeValidation;

    EditText etxMissingPersonName, etxMissingPersonAge, etxNickName, etxResidence, etxLastWearing, etxLocationLastSeen, etxBirthMarks, etxDisabilities, etxContactPerson, etxContactPersonPhone, etxContactPersonAlt, etxContactPersonPhoneAlt;
    TextView txtDateLastSeen, txtphotoPath;
    ImageView imgMissingPersonPhoto;
    Button btnAddPhoto, btnAddReport;

    private int year;
    private int month;
    private int day;
    private final static int RESULT_SELECT_IMAGE = 100;
    private final static int IMAGE_URI = 150;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_missing_person);
        Session session = new Session(getApplicationContext());
        if (session.checkSession() == false){
            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Report A Missing Person");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        txtphotoPath = (TextView) findViewById(R.id.photoPath);
        etxMissingPersonName = (EditText) findViewById(R.id.input_name);
        etxMissingPersonAge = (EditText) findViewById(R.id.input_age);
        etxNickName = (EditText) findViewById(R.id.input_nickname);
        etxResidence = (EditText) findViewById(R.id.input_residence);
        etxLastWearing = (EditText) findViewById(R.id.input_last_wearing);
        etxLocationLastSeen = (EditText) findViewById(R.id.input_location_last_seen);
        etxBirthMarks = (EditText) findViewById(R.id.input_birthmarks);
        etxDisabilities = (EditText) findViewById(R.id.input_disabilities);
        etxContactPerson = (EditText) findViewById(R.id.input_contact_person_name);
        etxContactPersonPhone = (EditText) findViewById(R.id.input_contact_person_number);
        etxContactPersonAlt = (EditText) findViewById(R.id.input_contact_person_name_alt);
        etxContactPersonPhoneAlt = (EditText) findViewById(R.id.input_contact_person_number_alt);
        txtDateLastSeen = (TextView) findViewById(R.id.input_date_last_seen);
        btnAddPhoto = (Button) findViewById(R.id.uploadImage);
        btnAddReport = (Button) findViewById(R.id.addReport);
        imgMissingPersonPhoto = (ImageView) findViewById(R.id.missingPersonPhoto);

        mAwesomeValidation = new AwesomeValidation(BASIC);

        mAwesomeValidation.addValidation(this, R.id.input_name, "[a-zA-Z\\s]+", R.string.err_name);
        mAwesomeValidation.addValidation(this, R.id.input_age, "^([1-9][0-9]{0,2})?(\\\\.[0-9]?)?$", R.string.age_error);
        mAwesomeValidation.addValidation(this, R.id.input_residence, RegexTemplate.NOT_EMPTY, R.string.residence_not_empty);
        mAwesomeValidation.addValidation(this, R.id.input_last_wearing, RegexTemplate.NOT_EMPTY, R.string.cannot_be_empty);
        mAwesomeValidation.addValidation(this, R.id.input_location_last_seen, RegexTemplate.NOT_EMPTY, R.string.residence_not_empty);
        mAwesomeValidation.addValidation(this, R.id.input_contact_person_name, RegexTemplate.NOT_EMPTY, R.string.residence_not_empty);
        mAwesomeValidation.addValidation(this, R.id.input_contact_person_number, RegexTemplate.NOT_EMPTY, R.string.residence_not_empty);

        setCurrentDateOnView();
        txtDateLastSeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(999);
            }
        });
        btnAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    CropImage.activity(null)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setFixAspectRatio(true)
                            .start(NewMissingPersonActivity.this);

                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        });

        btnAddReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAwesomeValidation.validate()){
                    String url = Config.server_url + "API/MissingPersons/add";

                    final String personname = etxMissingPersonName.getText().toString();
                    final String personage = etxMissingPersonAge.getText().toString();
                    final String nickname = etxNickName.getText().toString();
                    final String residence = etxResidence.getText().toString();
                    final String date_last_seen = txtDateLastSeen.getText().toString();
                    final String last_wearing = etxLastWearing.getText().toString();
                    final String location_last_seen_ = etxLocationLastSeen.getText().toString();
                    final String birth_marks = etxBirthMarks.getText().toString();
                    final String disabilities = etxDisabilities.getText().toString();
                    final String contactPerson = etxContactPerson.getText().toString();
                    final String contactPersonPhone = etxContactPersonPhone.getText().toString();
                    final String contactPersonAlt = etxContactPersonAlt.getText().toString();
                    final String contactPersonPhoneAlt = etxContactPersonPhoneAlt.getText().toString();
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    final ProgressDialog pDialog = new ProgressDialog(NewMissingPersonActivity.this);
                    pDialog.setTitle("Adding Missing Person");
                    pDialog.setMessage("Please wait...");
                    pDialog.show();

                    VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
                        @Override
                        public void onResponse(NetworkResponse response) {
                            pDialog.hide();
                            String resultResponse = new String(response.data);
                            try {
                                JSONObject result = new JSONObject(resultResponse);
                                Toast.makeText(NewMissingPersonActivity.this, result.getString("message"), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(NewMissingPersonActivity.this, MissingPersonDetailsActivity.class);
                                intent.putExtra("id", result.getInt("id"));
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
                            new AlertDialog.Builder(NewMissingPersonActivity.this)
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
                            params.put("name", personname);
                            params.put("age", personage);
                            params.put("nickname", nickname);
                            params.put("residence", residence);
                            params.put("date_last_seen", date_last_seen);
                            params.put("last_wearing", last_wearing);
                            params.put("location_last_seen", location_last_seen_);
                            params.put("birth_marks", birth_marks);
                            params.put("disabilities", disabilities);
                            params.put("contact_person", contactPerson);
                            params.put("contact_person_phone", contactPersonPhone);
                            params.put("contact_person_alt", contactPersonAlt);
                            params.put("contact_person_phone_alt", contactPersonPhoneAlt);
                            params.put("user_uid", String.valueOf("[" + user.getProviderId() + "]" + user.getEmail()));
                            return params;
                        }

                        @Override
                        protected Map<String, DataPart> getByteData() throws AuthFailureError {
                            Map<String, DataPart> params = new HashMap<>();

                            Drawable d = Drawable.createFromPath(txtphotoPath.getText().toString());
                            String mimeType = Config.getMimeType(txtphotoPath.getText().toString());
                            params.put("missingPersonPhoto", new DataPart(txtphotoPath.getText().toString(),
                                    AppHelper.getFileDataFromDrawable(getBaseContext(), d), mimeType));

                            return params;
                        }
                    };

                    multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                            0,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    AppController.getInstance().addToRequestQueue(multipartRequest);
                }
            }
        });
    }

    public void setCurrentDateOnView() {
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        // set current date into textview
        txtDateLastSeen.setText(new StringBuilder()
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
        return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;

            // set selected date into textview
            txtDateLastSeen.setText(new StringBuilder()
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
                    imgMissingPersonPhoto.setImageURI(result.getUri());
                    txtphotoPath.setText(result.getUri().getPath());
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}

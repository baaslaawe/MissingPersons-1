package ke.co.debechlabs.missingpersons;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import ke.co.debechlabs.missingpersons.Config.Config;
import ke.co.debechlabs.missingpersons.app.AppController;

public class PosterActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "POSTER";
    Button btnShare, btnDownload, btnBack;
    TextView txtpersonname, txtDescription, txtContactPerson, txtContactPersonPhone, txtContactPersonAlt, txtContactPersonPhoneAlt, txtTodaysDate;
    ImageLoader imageLoader;
    NetworkImageView imgPerson;
    ProgressBar imgProgress;
    FrameLayout loader;
    RelativeLayout posterLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poster);

        btnShare = (Button) findViewById(R.id.sharePoster);
        btnDownload = (Button) findViewById(R.id.downloadPoster);
        btnBack = (Button) findViewById(R.id.backButton);
        txtpersonname = (TextView) findViewById(R.id.person_name);
        txtDescription = (TextView) findViewById(R.id.person_description);
        txtContactPerson = (TextView) findViewById(R.id.contact_person);
        txtContactPersonPhone = (TextView) findViewById(R.id.contact_person_phone);
        txtContactPersonAlt = (TextView) findViewById(R.id.contact_person_alt);
        txtContactPersonPhoneAlt = (TextView) findViewById(R.id.contact_person_number_alt);
        txtTodaysDate = (TextView) findViewById(R.id.todaysDate);
        imageLoader=AppController.getInstance().getImageLoader();
        imgPerson = (NetworkImageView) findViewById(R.id.personImage);
        imgProgress = (ProgressBar) findViewById(R.id.imgProgress);
        loader = (FrameLayout) findViewById(R.id.loader);

        Date date = Calendar.getInstance().getTime();
        //
        // Display a date in day, month, year format
        //
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String today = formatter.format(date);
        txtTodaysDate.setText(today);
        posterLayout = (RelativeLayout) findViewById(R.id.main_poster);

        loader.setVisibility(View.VISIBLE);
        posterLayout.setVisibility(View.GONE);

        btnShare.setOnClickListener(this);
        btnDownload.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        int id = Integer.parseInt(getIntent().getStringExtra("id"));
        getDetails(id);
    }

    private void getDetails(int id){
        String url = Config.server_url + "API/MissingPersons/getByID/" + id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject person = response.getJSONObject("data");

                    txtpersonname.setText(person.getString("missing_person_name"));

                    String missing_date = person.getString("missing_person_date_last_seen");
                    String formatted_missing_date = Config.FormatDateString(missing_date, "yyyy-MM-dd HH:mm:ss", "MMMM dd, yyyy");

                    String personDescription = new StringBuilder()
                                                .append("Last Seen at ")
                                                .append(person.getString("missing_person_location_last_seen"))
                                                .append(" on ")
                                                .append(formatted_missing_date)
                                                .append(" wearing ")
                                                .append(person.getString("missing_person_last_wearing"))
                                                .append(".")
                                                .toString();

                    txtDescription.setText(personDescription);
                    txtContactPerson.setText(person.getString("missing_person_contact_person"));
                    txtContactPersonPhone.setText(person.getString("missing_person_contact_person_number"));
                    txtContactPersonAlt.setText(person.getString("missing_person_contact_person_alt"));
                    txtContactPersonPhoneAlt.setText(person.getString("missing_person_contact_person_number_alt"));

                    imgPerson.setImageUrl(Config.server_url + person.getString("missing_person_image"), imageLoader);
                    imgProgress.setVisibility(View.GONE);
                    loader.setVisibility(View.GONE);
                    posterLayout.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PosterActivity.this, "Could not find this user", Toast.LENGTH_SHORT).show();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.sharePoster:
                sharePoster();
                break;

            case R.id.downloadPoster:
                downloadPoster();
                break;

            case R.id.backButton:
                onBackPressed();
                break;
        }
    }

    public void downloadPoster(){
        Bitmap posterBitmap = generateBitmap();

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/MissingPersons/Posters");
        myDir.mkdirs();

//        Generate a random number
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);

        String fname = "Poster-" + n + ".jpg";
        File file = new File(myDir, fname);
        Log.i(TAG, "" + file);

        if (file.exists())
            file.delete();

        try{
            FileOutputStream out = new FileOutputStream(file);
            posterBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            Toast.makeText(this, "Downloaded Poster", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sharePoster(){
        Bitmap posterBitmap = generateBitmap();

        File cachePath = new File(this.getCacheDir(), "images");
        cachePath.mkdirs(); // don't forget to make the directory

        try {
            FileOutputStream stream = new FileOutputStream(cachePath + "/image.png");
            posterBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            File imagePath = new File(this.getCacheDir(), "images");
            File newFile = new File(imagePath, "image.png");
            Uri contentUri = FileProvider.getUriForFile(this, "co.ke.debechlabs.missingpersons.fileprovider", newFile);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hello. Please help us find the above named person. If you have seen or have seen or have any whereabouts of this person, please contact: 0725160399");
                startActivity(Intent.createChooser(shareIntent, "Choose an app"));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Bitmap generateBitmap(){
        posterLayout.setDrawingCacheEnabled(true);
        posterLayout.buildDrawingCache();

        Bitmap posterBitmap = posterLayout.getDrawingCache();

        return posterBitmap;
    }
}

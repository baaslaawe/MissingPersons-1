package ke.co.debechlabs.missingpersons;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ke.co.debechlabs.missingpersons.Config.Config;
import ke.co.debechlabs.missingpersons.adapters.ContactListAdapter;
import ke.co.debechlabs.missingpersons.app.AppController;
import ke.co.debechlabs.missingpersons.models.Contact;

public class HelpfulInfo extends AppCompatActivity{
    List<Contact> contacts = new ArrayList<Contact>();
    ContactListAdapter contactListAdapter;
    ListView contactsListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpful_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Some Helpful Information");
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        contactsListView = (ListView) findViewById(R.id.contactsListView);

        contactListAdapter = new ContactListAdapter(this, contacts);
        contactsListView.setAdapter(contactListAdapter);

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView txtContact = (TextView) view.findViewById(R.id.contact);
                String contact = txtContact.getText().toString();

                Intent intent = new Intent(Intent.ACTION_CALL);

                intent.setData(Uri.parse("tel:" + contact));
                if ( ContextCompat.checkSelfPermission( HelpfulInfo.this, Manifest.permission.CALL_PHONE ) != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions( HelpfulInfo.this, new String[] {  Manifest.permission.CALL_PHONE  }, 1);
                }else {
                    startActivity(intent);
                }
            }
        });
        getContacts();
    }

    private void getContacts(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        String url = Config.server_url + "API/MissingPersons/getContacts";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.hide();
                try {
                    if (response.getBoolean("status") == true){
                        JSONArray responseArray = response.getJSONArray("data");
                        for (int i = 0; i < responseArray.length(); i++) {
                            JSONObject contactObject = responseArray.getJSONObject(i);
                            Contact contact = new Contact();

                            contact.setId(contactObject.getInt("id"));
                            contact.setContact(contactObject.getString("contact"));
                            contact.setName(contactObject.getString("name"));

                            contacts.add(contact);
                            contactListAdapter.notifyDataSetChanged();
                        }
                    }else{
                        Toast.makeText(HelpfulInfo.this, "There are no contacts yet", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.hide();
                Toast.makeText(HelpfulInfo.this, "Server Error", Toast.LENGTH_SHORT).show();
                Log.e("Volley Error", error.toString());
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

}

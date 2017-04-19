package ke.co.debechlabs.missingpersons;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import java.util.HashMap;
import java.util.List;

import ke.co.debechlabs.missingpersons.Config.Config;
import ke.co.debechlabs.missingpersons.adapters.MissingPersonsAdapter;
import ke.co.debechlabs.missingpersons.adapters.MissingPersonsListAdapter;
import ke.co.debechlabs.missingpersons.app.AppController;
import ke.co.debechlabs.missingpersons.models.Person;

public class MissingPersonsListActivity extends AppCompatActivity implements View.OnClickListener, SearchView.OnQueryTextListener {

    public static final String KEY_NAME = "name";
    public static final String KEY_DATE = "date";
    public static final String KEY_THUMB = "thumb";

    FrameLayout loaderLayout, noDataLayout;
    ListView missingPersonsList;
    List<Person> personsList = new ArrayList<Person>();
    FloatingActionButton addMissingPersonButton;

    MenuItem searchMenuItem;
    SearchView searchView;

    MissingPersonsListAdapter mpAdapater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missing_persons_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Missing Persons");

        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loaderLayout = (FrameLayout) findViewById(R.id.loader);
        noDataLayout = (FrameLayout) findViewById(R.id.no_data);
        missingPersonsList = (ListView) findViewById(R.id.missing_persons_list);
        addMissingPersonButton = (FloatingActionButton) findViewById(R.id.addMissingPerson);

        addMissingPersonButton.setOnClickListener(this);
        mpAdapater = new MissingPersonsListAdapter(this, personsList);
        missingPersonsList.setAdapter(mpAdapater);

        createList();

        missingPersonsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView txtID = (TextView) view.findViewById(R.id.personID);
                int personID = Integer.parseInt(txtID.getText().toString());
                Intent intent = new Intent(MissingPersonsListActivity.this, MissingPersonDetailsActivity.class);
                intent.putExtra("id", personID);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.persons_list, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    private void createList(){
        loaderLayout.setVisibility(View.VISIBLE);
        noDataLayout.setVisibility(View.GONE);
        missingPersonsList.setVisibility(View.GONE);

        String url = Config.server_url + "API/MissingPersons/get";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int count = response.getInt("count");
                    loaderLayout.setVisibility(View.GONE);
                    if (count==0){
                        noDataLayout.setVisibility(View.VISIBLE);
                        missingPersonsList.setVisibility(View.GONE);
                    }else{
                        JSONArray persons = response.getJSONArray("data");
                        for (int i=0; i < persons.length(); i++){
                            Person p = new Person();
                            JSONObject personObject = persons.getJSONObject(i);
                            p.setId(personObject.getInt("id"));
                            p.setReported_date(personObject.getString("missing_person_created_at"));
                            p.setImage_url(Config.server_url + personObject.getString("missing_person_image_thumb"));
                            p.setPersonname(personObject.getString("missing_person_name"));

                            personsList.add(p);
                            mpAdapater.notifyDataSetChanged();
                        }
                        noDataLayout.setVisibility(View.GONE);
                        missingPersonsList.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MissingPersonsListActivity.this, "There was an error getting the list", Toast.LENGTH_SHORT).show();
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void oldCreateList(){
        ArrayList<HashMap<String, String>> personsList = new ArrayList<HashMap<String, String>>();

        HashMap<String, String> map = new HashMap<String, String>();

        map.put(KEY_NAME, "Robert Snodgrass");
        map.put(KEY_DATE, "Jan 20, 2017");
        map.put(KEY_THUMB, "https://pickaface.net/assets/images/slides/slide2.png");

        personsList.add(map);

        map = new HashMap<String, String>();

        map.put(KEY_NAME, "Jane Kasumari");
        map.put(KEY_DATE, "Jan 23, 2017");
        map.put(KEY_THUMB, "https://www.tm-town.com/assets/default_female600x600-3702af30bd630e7b0fa62af75cd2e67c.png");

        personsList.add(map);

        map = new HashMap<String, String>();

        map.put(KEY_NAME, "John Amka Twende");
        map.put(KEY_DATE, "Feb 01, 2017");
        map.put(KEY_THUMB, "https://www.tm-town.com/assets/default_male600x600-79218392a28f78af249216e097aaf683.png");

        personsList.add(map);

        MissingPersonsAdapter adapter = new MissingPersonsAdapter(this, personsList);
        missingPersonsList.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addMissingPerson:
                Intent intent = new Intent(MissingPersonsListActivity.this, NewMissingPersonActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mpAdapater.getFilter().filter(newText);
        return true;
    }
}

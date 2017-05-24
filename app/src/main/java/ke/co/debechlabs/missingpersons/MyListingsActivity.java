package ke.co.debechlabs.missingpersons;

import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ke.co.debechlabs.missingpersons.Config.Config;
import ke.co.debechlabs.missingpersons.adapters.MissingPersonsRecyclerAdapter;
import ke.co.debechlabs.missingpersons.app.AppController;
import ke.co.debechlabs.missingpersons.models.Member;
import ke.co.debechlabs.missingpersons.models.Person;

public class MyListingsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RelativeLayout nolistingLayout, listingLayout;
    FrameLayout loadingLayout;
    private List<Person> personList = new ArrayList<Person>();
    private MissingPersonsRecyclerAdapter missingPersonsRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("My Listings");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.listings_recycler_view);
        loadingLayout = (FrameLayout) findViewById(R.id.loadingLayout);
        listingLayout = (RelativeLayout) findViewById(R.id.content_my_listing);
        nolistingLayout = (RelativeLayout) findViewById(R.id.no_listings);

        missingPersonsRecyclerAdapter = new MissingPersonsRecyclerAdapter(this, personList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new MyListingsActivity.GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(missingPersonsRecyclerAdapter);
        getMyListings();
    }

    public void getMyListings(){
        loadingLayout.setVisibility(View.VISIBLE);
        nolistingLayout.setVisibility(View.GONE);
        listingLayout.setVisibility(View.GONE);
        String url = Config.server_url + "API/MissingPersons/getMyListings/";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadingLayout.setVisibility(View.GONE);
                try {
                    JSONObject responseObj = new JSONObject(response);
                    if (responseObj.getBoolean("status") == true){
                        listingLayout.setVisibility(View.VISIBLE);
                        JSONArray jsonArray = responseObj.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Person person = new Person();
                            JSONObject personObj = jsonArray.getJSONObject(i);
                            person.setId(personObj.getInt("id"));
                            person.setReported_date(Config.FormatDateString(personObj.getString("missing_person_created_at"), "yyyy-MM-dd HH:mm:ss", "MMMM dd, yyyy"));
                            person.setImage_url(personObj.getString("missing_person_image"));
                            person.setPersonname(personObj.getString("missing_person_name"));
                            person.setFound(personObj.getInt("found"));

                            personList.add(person);
                            missingPersonsRecyclerAdapter.notifyDataSetChanged();
                        }
                    }else{
                        nolistingLayout.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MyListingsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("user_id", Config.getUserID());
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}

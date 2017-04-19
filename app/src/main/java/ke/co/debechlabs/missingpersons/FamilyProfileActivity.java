package ke.co.debechlabs.missingpersons;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.design.widget.FloatingActionButton;
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
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
import ke.co.debechlabs.missingpersons.adapters.MembersAdapter;
import ke.co.debechlabs.missingpersons.app.AppController;
import ke.co.debechlabs.missingpersons.models.Member;

public class FamilyProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recyclerView;
    private List<Member> memberList = new ArrayList<Member>();
    private MembersAdapter membersAdapter;

    private RelativeLayout nomembersLayout, familyMembersLayout;
    FrameLayout loadingLayout;
    FloatingActionButton fabAddMember;

    private final static String TAG = "FamilyMembersFragment";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("My Family Profile");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.family_members_recycler_view);
        nomembersLayout = (RelativeLayout) findViewById(R.id.no_members);
        familyMembersLayout = (RelativeLayout) findViewById(R.id.content_family_members);
        loadingLayout = (FrameLayout) findViewById(R.id.loadingLayout);
        fabAddMember = (FloatingActionButton) findViewById(R.id.addFamilyMember);

        fabAddMember.setOnClickListener(this);
        membersAdapter = new MembersAdapter(this, memberList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(membersAdapter);
        getFamilyMembers();
    }

    private void getFamilyMembers(){
        loadingLayout.setVisibility(View.VISIBLE);
        nomembersLayout.setVisibility(View.GONE);
        familyMembersLayout.setVisibility(View.GONE);
        String url = Config.server_url + "API/MissingPersons/findFamilyMembers/";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadingLayout.setVisibility(View.GONE);
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    int count = jsonObject.getInt("count");
                    if (count == 0){
                        Toast.makeText(FamilyProfileActivity.this, "There are no family members yet", Toast.LENGTH_SHORT).show();
                        nomembersLayout.setVisibility(View.VISIBLE);
                    }else{
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject familyMember = jsonArray.getJSONObject(i);

                            Member m = new Member();
                            m.setId(familyMember.getInt("id"));
                            m.setMember_name(familyMember.getString("firstname") + " " + familyMember.getString("lastname"));
                            m.setMember_relationship(familyMember.getString("relationship"));
                            m.setMember_photo(familyMember.getString("profile_image"));

                            memberList.add(m);
                            membersAdapter.notifyDataSetChanged();
                        }
                        familyMembersLayout.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Getting Family Members", error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                params.put("user_id", "[" + user.getProviderId() + "]" + user.getEmail());
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addFamilyMember:
                Intent intent = new Intent(this, NewFamilyMember.class);
                startActivity(intent);
                break;
        }
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

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}

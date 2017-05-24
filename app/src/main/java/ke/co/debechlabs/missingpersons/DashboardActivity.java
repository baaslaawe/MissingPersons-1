package ke.co.debechlabs.missingpersons;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ke.co.debechlabs.missingpersons.Config.Config;
import ke.co.debechlabs.missingpersons.Session.Session;

public class DashboardActivity extends AppCompatActivity {
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Dashboard");

        String[] PERMISSIONS = {android.Manifest.permission.CALL_PHONE, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if(user != null) {
//            Check login in DB
        }
        setSupportActionBar(toolbar);

        LinearLayout menuListMissingPersons = (LinearLayout) findViewById(R.id.menu_missing_list);
        LinearLayout menuAddMissingPersons = (LinearLayout) findViewById(R.id.menu_add_missing);
        LinearLayout menuMyListings = (LinearLayout) findViewById(R.id.menu_my_listings);
        LinearLayout menuFamilyProfile = (LinearLayout) findViewById(R.id.menu_family_profile);

        menuListMissingPersons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, MissingPersonsListActivity.class);
                startActivity(intent);
            }
        });

        menuFamilyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, FamilyProfileActivity.class);
                startActivity(intent);
            }
        });

        menuAddMissingPersons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, NewMissingPersonActivity.class);
                startActivity(intent);
            }
        });

        menuMyListings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, MyListingsActivity.class);
                startActivity(intent);
            }
        });
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dashboardmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout:
                Session sess = new Session(DashboardActivity.this);
                sess.logout();
                break;
            case R.id.helpfulinfo:
                Intent intent = new Intent(DashboardActivity.this, HelpfulInfo.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }
}

package ke.co.debechlabs.missingpersons.Session;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ke.co.debechlabs.missingpersons.SignIn;

/**
 * Created by chriz on 4/13/2017.
 */

public class Session {
    private FirebaseAuth mAuth;
    private Context c;
    public Session(Context ctx){
        this.c = ctx;
    }
    public boolean checkSession(){
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            return true;
        }
        return false;
    }

    public void logout(){
        new AlertDialog.Builder(this.c)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to Log Out?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        FirebaseAuth.getInstance().signOut();
                        LoginManager.getInstance().logOut();
                        Intent intent = new Intent(c, SignIn.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        c.startActivity(intent);
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }
}

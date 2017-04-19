package ke.co.debechlabs.missingpersons.Config;

import android.annotation.TargetApi;
import android.content.Context;
import java.text.SimpleDateFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.Date;

import ke.co.debechlabs.missingpersons.app.AppController;

/**
 * Created by chriz on 4/11/2017.
 */

public class Config {
    public static final String recaptcha_private_key = "6Lc9jxwUAAAAACDHbwCqm2nGwWfgUv76wuMdtSLH";
    public static final String recaptcha_public_key = "6Lc9jxwUAAAAAF3_Ql-Uy0H0Do7tGQWBJqhdaXRb";

    public static final String server_url = "http://00d10c2d.ngrok.io/missingpersons/";

    public String idToken;

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String FormatDateString(String dateString, String inputFormat, String outputformat){
        String formatted_date = "";

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(inputFormat);
            Date d = dateFormat.parse(dateString);

            dateFormat = new SimpleDateFormat(outputformat);
            formatted_date = dateFormat.format(d);
        }catch(Exception e){
        }

        return formatted_date;
    }

    public static String getUserID(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userID = "[" + user.getProviderId() + "]" + user.getEmail();

        return userID;
    }

    public static void verifyLogin(FirebaseUser user){
        final String url = server_url + "API/MissingPersons/addUser/";
        user.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        String id = task.getResult().getToken();
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url + id, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });

                        AppController.getInstance().addToRequestQueue(stringRequest);
                    }
                });
    }
}

package com.storenshare.storenshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 300269668 on 6/4/2018.
 */

public class MyFunction {
    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean isPasswordValid(String pass) {
        boolean isValid = false;
        if (!pass.isEmpty())
            isValid = true;
        return isValid;
    }

    public static boolean checkPasswordLength(String pass) {
        boolean isValid = false;
        if (pass.length() > 5)
            isValid = true;
        return isValid;
    }

    public boolean comparePassword(String password, String cpassword) {
        boolean isValid = false;
        if(password.equals(cpassword))
            isValid=true;
        return isValid;
    }
    public void delSharedMember(final Context context,String url, String sharing_id){

        Map<String, String> params = new HashMap<String, String>();
        params.put("sharing_id", sharing_id);
        CustomRequest user_request = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int success = response.getInt("success");
                    if (success == 1) {

                        Toast.makeText(context,"Member removed",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context,"Failed to remove",Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception e) {
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError response) {
            }
        });
        // Adding request to request queue
        MyApplication.getInstance().addToReqQueue(user_request);

    }
    public void addLog(final Context context, String user_id,String msg){
        String url="http://shareblood.x10host.com/storeshare/tracking.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", user_id);
        params.put("message", msg);
        CustomRequest user_request = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int success = response.getInt("success");

                } catch (Exception e) {
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError response) {
                Toast.makeText(context,response.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        // Adding request to request queue
        MyApplication.getInstance().addToReqQueue(user_request);

    }

    public void navigationActions(final Activity context, MenuItem item, GoogleSignInClient mGoogleSignInClient){
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            context.startActivity(new Intent(context,UserProfileActivity.class));
        } else if (id == R.id.nav_createDoc) {
            context.startActivity(new Intent(context,CreateDocsActivity.class));
        } else if (id == R.id.nav_shared) {
            context.startActivity(new Intent(context,SharedWithMeActivity.class));
        } else if (id == R.id.nav_EditProfile) {
            context.startActivity(new Intent(context, EditProfile.class));
        } else if (id == R.id.nav_share) {

        }
        else if (id == R.id.view_log) {
            context.startActivity(new Intent(context, LogActivity.class));
        }
        else if (id == R.id.nav_signOut) {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener( context, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...

                            SharedPreferences sharedpreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.remove("userID");
                            editor.commit();

                            Toast.makeText(context, "You are log out", Toast.LENGTH_LONG).show();
                            context.startActivity(new Intent(context, HomeActivity.class));
                            context.finish();
                        }
                    });
        }
    }
}

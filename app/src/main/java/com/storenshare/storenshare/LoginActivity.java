package com.storenshare.storenshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    final String url = "http://shareblood.x10host.com/storeshare/login.php";
    EditText email, pass;
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = (EditText) findViewById(R.id.etEmail);
        pass = (EditText) findViewById(R.id.etPassword);
        setupToolbarMenu();
    }
    private void setupToolbarMenu() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Login");
    }
    public void login(View view) {
        String emailID = email.getText().toString().trim();
        String password = pass.getText().toString().trim();
        MyFunction mf = new MyFunction();

        if (!mf.isEmailValid(emailID)) {
            email.setError("Enter valid email address!");
        } else if (!mf.isPasswordValid(password)) {
            pass.setError("Enter password!");
        } else if (!mf.checkPasswordLength(password)) {
            pass.setError("Password must be minimum 6 characters!");
        } else {

            Map<String, String> params = new HashMap<String, String>();
            params.put("email", emailID);
            params.put("pass", password);
            CustomRequest user_request = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        int success = response.getInt("success");
                        if (success == 1) {
                            JSONArray ja = response.getJSONArray("user");
                            SharedPreferences sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject jobj = ja.getJSONObject(i);
                                editor.putString("userID", jobj.getString("id"));
                                editor.commit();
                                MyFunction mf = new MyFunction();
                                mf.addLog(LoginActivity.this, jobj.getString("id"), "You are logged in.");
                                Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                                intent.putExtra("userId", jobj.getString("id"));
                                startActivity(intent);
                            } // for loop ends
                        } else {

                            Toast.makeText(getApplicationContext(),
                                    "Invalid username/password", Toast.LENGTH_LONG)
                                    .show();
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
    }


    public void signUp(View view) {
        startActivity(new Intent(LoginActivity.this,SignupActivity.class));
    }
}

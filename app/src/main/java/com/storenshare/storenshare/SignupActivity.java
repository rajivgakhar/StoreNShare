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
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    final String url = "http://shareblood.x10host.com/storeshare/insert.php";
    EditText email,pass,cpass;
    RadioButton rmale,rfemale;
    String gender="male";
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        email=(EditText)findViewById(R.id.etEmail);
        pass=(EditText)findViewById(R.id.etPassword);
        cpass=(EditText)findViewById(R.id.etCPassword);
        rmale=(RadioButton)findViewById(R.id.rbMale);
        rfemale=(RadioButton)findViewById(R.id.rbFemale);
        setupToolbarMenu();
    }
    private void setupToolbarMenu() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Create an Account!");
    }
    public void signUp(View view) {
        final String emailid=email.getText().toString().trim();
        final String password=pass.getText().toString().trim();
        String cpassword=cpass.getText().toString().trim();

        if(rmale.isChecked())
            gender="male";
        else
            gender="female";
        MyFunction mf = new MyFunction();

        if (!mf.isEmailValid(emailid)) {
            email.setError("Enter valid email address!");
        } else if (!mf.isPasswordValid(password)) {
            pass.setError("Enter password!");
        } else if (!mf.checkPasswordLength(password)) {
            pass.setError("Password must be minimum 6 characters!");
        }  else if (!mf.isPasswordValid(cpassword)) {
            cpass.setError("Enter password!");
        } else if (!mf.checkPasswordLength(cpassword)) {
            cpass.setError("Password must be minimum 6 characters!");
        }
        else if (!mf.comparePassword(password,cpassword)) {
            cpass.setError("Password mismatches!");
        }
        else {
            try {
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response.equals("failed")) {
                                    Toast.makeText(SignupActivity.this, "Email already exist", Toast.LENGTH_LONG).show();
                                } else {
                                    Intent intent = new Intent(SignupActivity.this, UserProfileActivity.class);
                                    intent.putExtra("userId", response);
                                    SharedPreferences sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    editor.putString("userID", response);
                                    editor.commit();
                                    MyFunction mf = new MyFunction();
                                    mf.addLog(SignupActivity.this,response, "You created account.");

                                    startActivity(intent);
                                    // Signed in successfully, show authenticated UI.

                                    finish();
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(getApplicationContext(),
                                "failed to insert", Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("name", "");
                        params.put("email", emailid);
                        params.put("password", password);
                        params.put("photoUrl", "");
                        params.put("loginType", "app");
                        params.put("gender", gender);
                        params.put("phone", "");
                        return params;
                    }
                };
                // Adding request to request queue
                MyApplication.getInstance().addToReqQueue(postRequest);

            } catch (Exception ex) {

            }
        }
    }

    public void openLoginActivity(View view) {
        startActivity(new Intent(SignupActivity.this,LoginActivity.class));
    }
}

package com.storenshare.storenshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    //TextView name,email,password;
    String userId;
    GoogleSignInClient mGoogleSignInClient;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    TextView name, email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        setupToolbarMenu();
        setupNavigationDrawerMenu();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        final TextView emailEt = findViewById(R.id.editTextEmail);
        final EditText nameEt = findViewById(R.id.User_name);
        final EditText pass = findViewById(R.id.password);
        final EditText phone = findViewById(R.id.editPhone);
        Button updatebutton = findViewById(R.id.btnUpdate);

        final String url = "http://shareblood.x10host.com/storeshare/profile.php";
        final String UpdateUrl = "http://shareblood.x10host.com/storeshare/update.php";
        SharedPreferences sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId=sharedpreferences.getString("userID","1");

        Map<String, String> params = new HashMap<String, String>();
        params.put("userId", userId);
        CustomRequest user_request = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {
                    int success = response.getInt("success");

                    if (success == 1) {

                        JSONArray ja = response.getJSONArray("users");
                       // JSONArray ja1 = response.getJSONArray("content");

                        for (int i = 0; i < ja.length(); i++) {

                            JSONObject jobj = ja.getJSONObject(i);
                            //HashMap<String, String> item = new HashMap<String, String>();
                            nameEt.setText(jobj.getString("name"));
                            emailEt.setText(jobj.getString("email"));
                            name.setText(jobj.getString("name"));
                            email.setText(jobj.getString("email"));
                            pass.setText(jobj.getString("password"));
                            phone.setText(jobj.getString("phone"));
                            // storeInfoSharedPref(jobj.getString("id"), jobj.getString("name"), jobj.getString("age"), jobj.getString("city"));
                            Toast.makeText(EditProfile.this, jobj.getString("name")+"bgxx", Toast.LENGTH_SHORT).show();

                        } // for loop ends


                    } else {

                        Toast.makeText(getApplicationContext(),
                                "Invalid id", Toast.LENGTH_SHORT)
                                .show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    Log.e("testttttttt",e.getMessage());
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError response) {

            }
        });


        // Adding request to request queue
        MyApplication.getInstance().addToReqQueue(user_request);

        //button on click
        updatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String UserName= name.getText().toString();
                String Password= pass.getText().toString();
                String Phone= phone.getText().toString();

                Map<String, String> params = new HashMap<String, String>();
                params.put("userId", userId);
                params.put("UserName", UserName);
                params.put("password", Password);
                params.put("Phone", Phone);

                CustomRequest user_request1 = new CustomRequest(Request.Method.POST, UpdateUrl, params, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            int success = response.getInt("success");

                          if (success == 1) {

                               // JSONArray ja = response.getJSONArray("users");
                                // JSONArray ja1 = response.getJSONArray("content");

                               /* for (int i = 0; i < ja.length(); i++) {

                                    JSONObject jobj = ja.getJSONObject(i);
                                    //HashMap<String, String> item = new HashMap<String, String>();
                                    name.setText(jobj.getString("name"));
                                    email.setText(jobj.getString("email"));
                                    pass.setText(jobj.getInt("password"));
                                    phone.setText(jobj.getInt("phone"));
                                    // storeInfoSharedPref(jobj.getString("id"), jobj.getString("name"), jobj.getString("age"), jobj.getString("city"));
                                    Toast.makeText(EditProfile.this, jobj.getString("name")+"bgxx", Toast.LENGTH_SHORT).show();

                                } */

                                Toast.makeText(EditProfile.this,"updated",Toast.LENGTH_SHORT).show();
                            } else {

                                Toast.makeText(getApplicationContext(),
                                        "Invalid id", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    e.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                            Log.e("testttttttt",e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError response) {

                    }
                });


                // Adding request to request queue
                MyApplication.getInstance().addToReqQueue(user_request1);

            }
        });
    }
    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
            closeDrawer();
        else
            super.onBackPressed();
    }
    private void closeDrawer(){
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }
    private void setupToolbarMenu(){
        mToolbar=(Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle("Edit Profile");
    }
    private void setupNavigationDrawerMenu(){
        NavigationView navigationView=(NavigationView)findViewById(R.id.navigationView);
        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle drawerToggle=new ActionBarDrawerToggle(this,
                mDrawerLayout,mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();//mainitain the state of drawer if open make it open
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        name = headerLayout.findViewById(R.id.userName);
        email = headerLayout.findViewById(R.id.userEmail);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        MyFunction mf=new MyFunction();
        mf.navigationActions(getApplicationContext(),item,mGoogleSignInClient);
        closeDrawer();
        return true;
    }
}

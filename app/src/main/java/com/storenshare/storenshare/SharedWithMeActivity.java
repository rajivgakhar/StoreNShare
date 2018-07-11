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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Adapter.MyAdapter;
import Model.ListItem;

public class SharedWithMeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    final String url = "http://shareblood.x10host.com/storeshare/sharedData.php";
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<ListItem> listItems;
    String userId = "";

    GoogleSignInClient mGoogleSignInClient;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    TextView name, email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_with_me);
        SharedPreferences sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedpreferences.getString("userID", "1");
        recyclerView = (RecyclerView) findViewById(R.id.rvSharedFiles);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));


        setupToolbarMenu();
        setupNavigationDrawerMenu();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        listItems = new ArrayList<>();
        getData();
    }
    private void getData() {
        listItems.clear();
        Map<String, String> params = new HashMap<String, String>();
        params.put("userId", userId);
        CustomRequest user_request = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {
                    int success = response.getInt("success");

                    if (success == 1) {

                        JSONArray ja1 = response.getJSONArray("content");

                        for (int i = 0; i < ja1.length(); i++) {

                            JSONObject jobj1 = ja1.getJSONObject(i);

                            ListItem listItem = new ListItem(jobj1.getString("filename"),jobj1.getString("data_id"),jobj1.getString("owner_id"));
                            listItems.add(listItem);
                        } // for loop ends

                        adapter = new MyAdapter(SharedWithMeActivity.this, listItems);
                        recyclerView.setAdapter(adapter);


                    } else {

                        Toast.makeText(getApplicationContext(),
                                "No shared data available", Toast.LENGTH_SHORT)
                                .show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
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
        mToolbar.setTitle("Shared With Me");
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

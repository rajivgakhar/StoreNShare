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
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
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

import Adapter.MyListAdapter;
import Model.SharedUser;

public class ShareFileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    EditText member;
    RadioButton rbView,rbEdit;
    String userId;
    final String url = "http://shareblood.x10host.com/storeshare/shareFile.php";
    String contentId,contentUrl;
    ListView lvSharedUsers;
    List<SharedUser> members;

    GoogleSignInClient mGoogleSignInClient;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    TextView name, email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_file);
        member=(EditText)findViewById(R.id.etEmail);
        rbView=(RadioButton)findViewById(R.id.rbView);
        rbEdit=(RadioButton)findViewById(R.id.rbEdit);
        lvSharedUsers=(ListView) findViewById(R.id.lvSharedUsers);

        SharedPreferences sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedpreferences.getString("userID", "1");
        Intent in=getIntent();
        contentId=in.getStringExtra("contentId");
        contentUrl=in.getStringExtra("contentUrl");
        onLoadViewData(contentId);
        setupToolbarMenu();
        setupNavigationDrawerMenu();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void shareFileWithMember(View view) {
        String emailID = member.getText().toString().trim();
        final MyFunction mf = new MyFunction();
        String permission="view";
        if(rbView.isChecked()){
            permission="view";
        }else
            permission="edit";


        if (!mf.isEmailValid(emailID)) {
            member.setError("Enter valid email address!");
        }  else {

            Map<String, String> params = new HashMap<String, String>();
            params.put("member", emailID);
            params.put("owner", userId);
            params.put("data_id", contentId);
            params.put("permission", permission);
            CustomRequest user_request = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        int success = response.getInt("success");
                        if (success == 1) {
                            Intent in=new Intent(ShareFileActivity.this, ShareFileActivity.class);
                            in.putExtra("contentId",contentId);
                            mf.addLog(ShareFileActivity.this, userId, "You shared a file ");
                            startActivity(in);

                            finish();

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

    public void onLoadViewData(String contentId){
        members=new ArrayList<>();

        Map<String, String> params = new HashMap<String, String>();
        params.put("data_id", contentId+"");
        CustomRequest user_request = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int success = response.getInt("success");
                    if (success == 1) {
                        JSONArray ja = response.getJSONArray("users");

                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject jobj = ja.getJSONObject(i);
                           members.add( new SharedUser(jobj.getString("id"),
                                   jobj.getString("name"),
                                   jobj.getString("permission"),
                                   jobj.getString("data_id")
                           ));
                        } // for loop ends

                        lvSharedUsers.setAdapter(new MyListAdapter(ShareFileActivity.this,members));

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
        mToolbar.setTitle("Share File");
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
        mf.navigationActions(ShareFileActivity.this,item,mGoogleSignInClient);
        closeDrawer();
        return true;
    }
}

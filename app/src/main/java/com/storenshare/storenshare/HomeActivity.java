package com.storenshare.storenshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private final int RC_SIGN_IN = 1;
    final String url = "http://shareblood.x10host.com/storeshare/insert.php";
    SharedPreferences sharedpreferences;
    GoogleSignInClient mGoogleSignInClient;
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.btnGoogle).setOnClickListener(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        setupToolbarMenu();
    }
    private void setupToolbarMenu() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Store N Share");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGoogle:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null)
            startActivity(new Intent(HomeActivity.this, UserProfileActivity.class));
        //updateUI(account);
        if (sharedpreferences.contains("userID"))
            startActivity(new Intent(HomeActivity.this, UserProfileActivity.class));
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);
            final String personName = acct.getDisplayName();
            final String personGivenName = acct.getGivenName();
            final String personFamilyName = acct.getFamilyName();
            final String personEmail = acct.getEmail();
            final String personId = acct.getId();
            final Uri personPhoto = acct.getPhotoUrl();

            try {
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                               Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
                                intent.putExtra("userId", response);

                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("userID", response);
                                editor.commit();
                                startActivity(intent);
                                finish();

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
                        params.put("name", personGivenName);
                        params.put("email", personEmail);
                        params.put("password", "");
                        params.put("photoUrl", String.valueOf(personPhoto));
                        params.put("loginType", "google");
                        params.put("gender", "");
                        params.put("phone", "");
                        return params;
                    }
                };
                // Adding request to request queue
                MyApplication.getInstance().addToReqQueue(postRequest, "test");

            } catch (Exception ex) {

            }


            // updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("test", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(HomeActivity.this, "SignIN failed", Toast.LENGTH_LONG).show();
            //updateUI(null);
        }
    }

    public void signUp(View view) {
        startActivity(new Intent(HomeActivity.this, SignupActivity.class));
    }

    public void openLoginActivity(View view) {
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
    }
}

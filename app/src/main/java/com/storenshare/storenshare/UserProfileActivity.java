package com.storenshare.storenshare;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import Adapter.MyAdapter;
import Model.ListItem;

public class UserProfileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ActivityCompat.OnRequestPermissionsResultCallback {


    TextView name, email;
    GoogleSignInClient mGoogleSignInClient;
    final String url = "http://shareblood.x10host.com/storeshare/profile.php";
    Intent in;
    private static final int PICK_FILE_REQUEST = 1;
    private static final String TAG = UserProfileActivity.class.getSimpleName();
    private String selectedFilePath;
    private String SERVER_URL = "http://shareblood.x10host.com/storeshare/upload.php";
    ProgressDialog dialog;
    ProgressBar pbFileUp;
    TextView txtFileUpload;
    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 0;
    String userId = "";


    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<ListItem> listItems;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data == null) {
                    //no data present
                    return;
                }

                Uri selectedFileUri = data.getData();
                selectedFilePath = FilePath.getPath(this, selectedFileUri);
                if (selectedFilePath != null && !selectedFilePath.equals("")) {
                    Toast.makeText(this, "" + selectedFilePath, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
                }

                //on upload button Click
                if (selectedFilePath != null) {
                    //dialog = ProgressDialog.show(UserProfileActivity.this, "", "Uploading File...", true);
                    txtFileUpload.setText("File is uploading.");
                    txtFileUpload.setVisibility(View.VISIBLE);
                    pbFileUp.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //creating new thread to handle Http Operations
                            uploadFile(selectedFilePath);
                            pbFileUp.post(new Runnable() {
                                public void run() {
                                    txtFileUpload.setVisibility(View.GONE);
                                    pbFileUp.setVisibility(View.GONE);

                                }
                            });
                        }
                    }).start();

                } else {
                    Toast.makeText(UserProfileActivity.this, "Please choose a File First", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        //sets the select file to all types of files
        intent.setType("*/*");
        //allows to select data and return it
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //starts new activity to select file and return data
        startActivityForResult(Intent.createChooser(intent, "Choose File to Upload.."), PICK_FILE_REQUEST);
    }

    //android upload file to server
    public int uploadFile(final String selectedFilePath) {

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);
        final long fileSize = selectedFile.length();

        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length - 1];

        if (!selectedFile.isFile()) {
            //  dialog.dismiss();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(UserProfileActivity.this, "Source File Doesn't Exist: " + selectedFilePath, Toast.LENGTH_SHORT).show();

                }
            });
            return 0;
        } else {
            try {

                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(SERVER_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file", selectedFilePath);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read

                while (bytesRead > 0) {
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UserProfileActivity.this, "File Upload completed", Toast.LENGTH_SHORT).show();

                            /***********save file into database start*/

                            Map<String, String> params = new HashMap<String, String>();
                            params.put("userId", userId);
                            params.put("fileName", fileName);
                            params.put("fileSize", String.valueOf(fileSize));
                            CustomRequest uploadFileData = new CustomRequest(Request.Method.POST, SERVER_URL, params, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {

                                    try {
                                        int success = response.getInt("success");

                                        if (success == 1) {

                                            getData();
                                            JSONArray ja = response.getJSONArray("users");

                                            for (int i = 0; i < ja.length(); i++) {

                                                JSONObject jobj = ja.getJSONObject(i);

                                            } // for loop ends
                                            MyFunction mf1 = new MyFunction();
                                            mf1.addLog(UserProfileActivity.this, userId, "You uploaded " + fileName);

                                        } else {

                                            Toast.makeText(getApplicationContext(),
                                                    "Invalid id", Toast.LENGTH_SHORT)
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
                            MyApplication.getInstance().addToReqQueue(uploadFileData);


                            /***********save file into database end*/

                        }
                    });
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UserProfileActivity.this, "File Not Found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(UserProfileActivity.this, "URL error!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(UserProfileActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
            }

            //dialog.dismiss();
            return serverResponseCode;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        setupToolbarMenu();
        setupNavigationDrawerMenu();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    PERMISSION_REQUEST_EXTERNAL_STORAGE);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showFileChooser();

                // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //    .setAction("Action", null).show();
            }
        });

//-------------------------------------------------------
        txtFileUpload = (TextView) findViewById(R.id.txtFileUploading);
        pbFileUp = (ProgressBar) findViewById(R.id.pbFileUpload);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        in = getIntent();
        userId = in.getStringExtra("userId");
        SharedPreferences sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedpreferences.getString("userID", "1");


        recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

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

                        JSONArray ja = response.getJSONArray("users");
                        JSONArray ja1 = response.getJSONArray("content");

                        for (int i = 0; i < ja.length(); i++) {

                            JSONObject jobj = ja.getJSONObject(i);
                            name.setText(jobj.getString("name"));
                            email.setText(jobj.getString("email"));

                        } // for loop ends
                        for (int i = 0; i < ja1.length(); i++) {

                            JSONObject jobj1 = ja1.getJSONObject(i);

                            ListItem listItem = new ListItem(jobj1.getString("filename"), jobj1.getString("id"), jobj1.getString("userid"));
                            listItems.add(listItem);
                        } // for loop ends

                        adapter = new MyAdapter(UserProfileActivity.this, listItems);
                        recyclerView.setAdapter(adapter);


                    } else {

                        Toast.makeText(getApplicationContext(),
                                "Invalid id", Toast.LENGTH_SHORT)
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
    public void onRequestPermissionsResult(
            int requestCode, String[] permission,
            int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            closeDrawer();
        else
            super.onBackPressed();
    }

    private void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void setupToolbarMenu() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Home");
    }

    private void setupNavigationDrawerMenu() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, mToolbar,
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        MyFunction mf=new MyFunction();
        mf.navigationActions(UserProfileActivity.this,item,mGoogleSignInClient);
        closeDrawer();
        return true;
    }
}

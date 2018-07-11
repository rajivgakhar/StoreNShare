package com.storenshare.storenshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateDocsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
private EditText body,title;
String userId="1";
    private String SERVER_URL = "http://shareblood.x10host.com/storeshare/upload.php";
    final String dataUrlById = "http://shareblood.x10host.com/storeshare/getDataById.php";
    final String dataUrl = "http://shareblood.x10host.com/storeshare/getData.php";
    final String filePath="http://shareblood.x10host.com/storeshare/uploads/";
    String contentId,contentUrl,contentName,loginUserId,permission,ownerID;
    Button btnSave;
    GoogleSignInClient mGoogleSignInClient;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    TextView name, email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_docs);
        body=(EditText)findViewById(R.id.etEditor);
        title=(EditText)findViewById(R.id.etTitle);
        btnSave=(Button)findViewById(R.id.btnSave);
        SharedPreferences sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        loginUserId = sharedpreferences.getString("userID", "1");
        Intent in=getIntent();
        contentId=in.getStringExtra("contentId");
        setupToolbarMenu();
        setupNavigationDrawerMenu();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if(contentId!=null) {

            //get content from data table
            Map<String, String> params = new HashMap<String, String>();
            params.put("id", contentId);
            CustomRequest user_request1 = new CustomRequest(Request.Method.POST, dataUrlById, params, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    try {
                        int success = response.getInt("success");

                        if (success == 1) {

                            JSONArray ja = response.getJSONArray("content");
                            for (int i = 0; i < ja.length(); i++) {

                                JSONObject jobj = ja.getJSONObject(i);
                                ownerID=jobj.getString("userid");
                                contentName=jobj.getString("filename");


                            } // for loop ends
                            title.setText(contentName);
                            contentUrl=filePath+contentName;

                            new Thread(new Runnable() {

                                public void run() {

                                    final ArrayList<String> urls = new ArrayList<String>(); //to read each line

                                    try {
                                        // Create a URL for the desired page
                                        URL url = new URL(contentUrl); //My text file location
                                        //First open the connection
                                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                        conn.setConnectTimeout(60000); // timing out in a minute

                                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                                        String str;
                                        while ((str = in.readLine()) != null) {
                                            urls.add(str);
                                        }
                                        in.close();
                                    } catch (Exception e) {
                                        Log.d("MyTag", e.toString());
                                    }

                                    //since we are in background thread, to post results we have to go back to ui thread. do the following for that

                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            body.setText(urls.get(0)); // My TextFile has 3 lines
                                            btnSave.setText("Update");
                                        }
                                    });

                                }
                            }).start();



                            //if data owner if different from login user
                            if(!loginUserId.equals(ownerID))
                            {
//get content from data table
                                Map<String, String> params1 = new HashMap<String, String>();
                                params1.put("id", contentId);
                                params1.put("member_id", loginUserId);
                                CustomRequest user_request2 = new CustomRequest(Request.Method.POST, dataUrl, params1, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        try {
                                            int success = response.getInt("success");
                                            if (success == 1) {

                                                JSONArray ja = response.getJSONArray("content");
                                                for (int i = 0; i < ja.length(); i++) {

                                                    JSONObject jobj = ja.getJSONObject(i);
                                                    permission=jobj.getString("permission");

                                                } // for loop ends
                                                if(permission.equals("view")) {
                                                    title.setEnabled(false);
                                                    body.setEnabled(false);
                                                    btnSave.setVisibility(View.GONE);
                                                }else{
                                                    title.setEnabled(true);
                                                    body.setEnabled(true);
                                                    btnSave.setVisibility(View.VISIBLE);
                                                }

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
                                        Log.e("testttttttt1",response.getMessage());
                                    }
                                });


                                // Adding request to request queue
                                MyApplication.getInstance().addToReqQueue(user_request2);
                                //get content from data table END
                            }

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
            //get content from data table END
      //title.setText(contentName);

        }

    }

    public void saveFile(View view) {

        String fName = "", bodyF = "";
        fName = title.getText().toString().trim();
        if (fName.equals("")) {
            Toast.makeText(CreateDocsActivity.this, "Enter file name", Toast.LENGTH_SHORT).show();
        } else {
            bodyF = body.getText().toString().trim();
            generateNoteOnSD(CreateDocsActivity.this, fName, bodyF);
        }
    }
    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();

            final String selectedFile=root+"/"+sFileName;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //creating new thread to handle Http Operations
                    uploadFile(selectedFile);

                }
            }).start();


            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CreateDocsActivity.this, "Source File " +
                            "Doesn't Exist: " + selectedFilePath, Toast.LENGTH_SHORT).show();
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
                Log.e("Server Response", "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateDocsActivity.this, "File Upload completed", Toast.LENGTH_SHORT).show();

                            /***********save file into database start*/

                            Map<String, String> params = new HashMap<String, String>();
                            if(contentId==null)
                                contentId="";
                            params.put("id", contentId);
                            params.put("userId", loginUserId);
                            params.put("fileName", fileName);
                            params.put("fileSize", String.valueOf(fileSize));
                            CustomRequest uploadFileData = new CustomRequest(Request.Method.POST, SERVER_URL, params, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {

                                    try {
                                        int success = response.getInt("success");

                                        if (success == 1) {


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
                        Toast.makeText(CreateDocsActivity.this, "File Not Found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(CreateDocsActivity.this, "URL error!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(CreateDocsActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
            }
            return serverResponseCode;
        }

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
        mToolbar.setTitle("Create Document");
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

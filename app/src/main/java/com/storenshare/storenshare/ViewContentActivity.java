package com.storenshare.storenshare;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;


public class ViewContentActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener{
ImageView ivContent;
String contentId,contentUrl,contentName,ownerID, loginUserId,permission;
Button del,inShare,openEditor;

final String filePath="http://shareblood.x10host.com/storeshare/uploads/";
    final String url = "http://shareblood.x10host.com/storeshare/deleteContent.php";
    final String dataUrlById = "http://shareblood.x10host.com/storeshare/getDataById.php";
    final String dataUrl = "http://shareblood.x10host.com/storeshare/getData.php";
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 1;
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;

    GoogleSignInClient mGoogleSignInClient;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    TextView name, email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_content);

        setupToolbarMenu();
        setupNavigationDrawerMenu();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        ivContent=(ImageView)findViewById(R.id.ivContent);
        del=(Button)findViewById(R.id.btnDel);
        inShare=(Button)findViewById(R.id.btnInShare);
        openEditor=(Button)findViewById(R.id.btnView);

        Intent in=getIntent();
        contentId=in.getStringExtra("contentId");
        SharedPreferences sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        loginUserId = sharedpreferences.getString("userID", "1");

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
                            //permission=jobj.getString("permission");

                        } // for loop ends
                        contentUrl=filePath+contentName;
                        String[] parts = contentName.split("\\.");
                        if(!(parts.length>1))
                            openEditor.setVisibility(View.VISIBLE);

                        mToolbar.setTitle(contentName);
                        Picasso.get()
                                .load(contentUrl)
                                .placeholder(R.drawable.file_icon)
                                .error(R.drawable.file)
                                .transform(transformation)
                                .into(ivContent);
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
                                                    del.setVisibility(View.GONE);
                                                    inShare.setVisibility(View.GONE);
                                                }else{
                                                    del.setVisibility(View.VISIBLE);
                                                    inShare.setVisibility(View.GONE);
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


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    PERMISSION_WRITE_EXTERNAL_STORAGE);
        }

    }
    Transformation transformation = new Transformation() {

        @Override
        public Bitmap transform(Bitmap source) {
            int targetWidth = 800;

            double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
            int targetHeight = (int) (targetWidth * aspectRatio);
            Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
            if (result != source) {
                // Same bitmap is returned if sizes are the same
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "transformation" + " desiredWidth";
        }
    };
    public void deleteFile(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Action")
                .setMessage("Do you really want to delete?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {



        Map<String, String> params = new HashMap<String, String>();
        params.put("id",contentId );
        params.put("url","uploads/"+contentName );
        CustomRequest user_request = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int success = response.getInt("success");
                    if (success == 1) {
                        Toast.makeText(getApplicationContext(),
                                response.getString("message")+"", Toast.LENGTH_LONG)
                                .show();
                        startActivity(new Intent(ViewContentActivity.this,UserProfileActivity.class));
                    } else {

                        Toast.makeText(getApplicationContext(),
                                response.getString("message")+"", Toast.LENGTH_LONG)
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
                    }})
                .setNegativeButton(android.R.string.no, null).show();


    }

    public void downloadFile(View view) {
        new DownloadFileFromURL().execute(contentUrl);
    }

    public void shareFile(View view) {
        shareTextUrl(contentUrl);
    }

    public void shareFileWithMember(View view) {
        Intent in=new Intent(ViewContentActivity.this, ShareFileActivity.class);
        in.putExtra("contentId",contentId);
        in.putExtra("contentUrl",contentUrl);
       startActivity(in);
    }

    public void openEditor(View view) {
        Intent in=new Intent(ViewContentActivity.this, CreateDocsActivity.class);
        in.putExtra("contentId",contentId);
        in.putExtra("contentUrl",contentUrl);
        in.putExtra("contentFileName",contentName);
        startActivity(in);
    }

    private class DownLoadImageTask extends AsyncTask<String,Void,Bitmap> {
        ImageView imageView;

        public DownLoadImageTask(ImageView imageView){
            this.imageView = imageView;
        }
        protected Bitmap doInBackground(String...urls){
            String urlOfImage = urls[0];
            Bitmap logo = null;
            try{
                InputStream is = new URL(urlOfImage).openStream();
                logo = BitmapFactory.decodeStream(is);
            }catch(Exception e){
                e.printStackTrace();
            }
            return logo;
        }
        protected void onPostExecute(Bitmap result){

            imageView.setImageBitmap(result);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                 File newDir = new File(Environment.getExternalStorageDirectory().toString()+ "/storeNshare/");
                if(!newDir.isDirectory())
                    newDir.mkdir();

                // Output stream
                OutputStream output = new FileOutputStream(Environment
                        .getExternalStorageDirectory().toString()
                        + "/storeNshare/"+contentName);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);

        }

    }
    // Method to share either text or URL.
    private void shareTextUrl(String url) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, "Image");
        share.putExtra(Intent.EXTRA_TEXT, url+"");

        startActivity(Intent.createChooser(share, "Share link!"));
    }

    // Method to share any image.
    private void shareImage() {
        Intent share = new Intent(Intent.ACTION_SEND);

        // If you want to share a png image only, you can do:
        // setType("image/png"); OR for jpeg: setType("image/jpeg");
        share.setType("image/*");

        // Make sure you put example png image named myImage.png in your
        // directory
        String imagePath = Environment.getExternalStorageDirectory()
                + "/myImage.png";

        File imageFileToShare = new File(imagePath);

        Uri uri = Uri.fromFile(imageFileToShare);
        share.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(Intent.createChooser(share, "Share Image!"));
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
        //mToolbar.setTitle("Edit Profile");
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
        MyFunction mf=new MyFunction();
        mf.navigationActions(getApplicationContext(),item,mGoogleSignInClient);
        closeDrawer();
        return true;
    }
}

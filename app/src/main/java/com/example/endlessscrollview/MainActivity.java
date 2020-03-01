package com.example.endlessscrollview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    GridLayoutManager layoutManager;
    RecyclerView.Adapter myadapter;
    ArrayList<ProductItems> listnewsData = new ArrayList<ProductItems>();
    public int page_number=1;

    private boolean isLoading=true;
    private int current_items,total_items,scrolled_out_items;

    ProgressBar progressBar;
    FusedLocationProviderClient fusedLocationClient;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    TextView currentLocation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        RecyclerView endless_view=findViewById(R.id.endless_view);
        layoutManager=new GridLayoutManager(this,2);
        endless_view.setLayoutManager(layoutManager);
        myadapter=new ListAdapterItems(listnewsData);
        endless_view.setHasFixedSize(true);
        endless_view.setAdapter(myadapter);

        currentLocation=findViewById(R.id.tvCurrentLocation);
        Button getCurrentLocation=findViewById(R.id.getCurrentLocation);
        getCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();


            }
        });
        progressBar=findViewById(R.id.progress_bar);
        String Producturl = "http://idealytik.com/SmartPasalWebServices/ProductLists.php?page_number="+page_number;
        new MyAsyncTaskgetNews1().execute(Producturl);

        endless_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                current_items=layoutManager.getChildCount();
                total_items=layoutManager.getItemCount();
                scrolled_out_items=layoutManager.findFirstVisibleItemPosition();
                if (dy>0){
                    if (isLoading&&(current_items+scrolled_out_items==total_items) ){
                        isLoading=false;

                        //fetch data

                        Toast.makeText(getApplicationContext(),"Fetching Data",Toast.LENGTH_SHORT).show();

                        fetchData();







                    }



                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isLoading=true;
                }


            }
        });


    }



    private void fetchData() {
        progressBar.setVisibility(View.VISIBLE);

                 page_number++;

                String Producturl = "http://idealytik.com/SmartPasalWebServices/ProductLists.php?page_number="+page_number;
                new MyAsyncTaskgetNews1().execute(Producturl);


                progressBar.setVisibility(View.GONE);

            }




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermission();
                    Toast.makeText(this,"Access permitted",Toast.LENGTH_SHORT).show();

                } else {
                    // Permission Denied
                    Toast.makeText( this,"Access denied" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ){//Can add more as per requirement


            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
        else
        {
            getLocation();
        }
    }

    private void getLocation() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Double latitude=location.getLatitude();
                Double longitude=location.getLongitude();
                getCompleteAddressString(latitude,longitude);
            }
        });
    }


    public class MyAsyncTaskgetNews1 extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            //before works

        }
        @Override
        protected String  doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                String NewsData;
                //define the url we have to connect with
                URL url = new URL(params[0]);
                //make connect with url and send request
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("http.keepAlive", "false");
                //waiting for 7000ms for response
                urlConnection.setConnectTimeout(7000);//set timeout to 5 seconds
                urlConnection.setDoOutput(true);



                try {
                    //getting the response data
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    //convert the stream to string
                    NewsData = ConvertInputToStringNoChange(in);
                    //send to display data
                    publishProgress(NewsData);
                } finally {
                    //end connection
                    urlConnection.disconnect();
                }

            }catch (Exception ex){
                Log.d("error",ex.getMessage());
            }
            return null;
        }
        protected void onProgressUpdate(String... progress) {

            try {

      JSONArray userInfo=new JSONArray(progress[0]);
      for (int i = 0; i < userInfo.length(); i++) {
          JSONObject userCredentials = userInfo.getJSONObject(i);

          listnewsData.add(new ProductItems(userCredentials.getString("name"),userCredentials.getString("picture_path"),userCredentials.getString("product_id"),userCredentials.getString("marked_price"),userCredentials.getString("fixed_price"),userCredentials.getString("brand"),userCredentials.getString("desc"),userCredentials.getString("sku")));



      }















                myadapter.notifyDataSetChanged();
                //display response data


            }



            catch (Exception ex) {
                Log.d("error is", ex.getMessage());
                Toast.makeText(getApplicationContext(),"End of items",Toast.LENGTH_LONG).show();
            }

        }


        protected void onPostExecute(String  result2){



        }





    }
    // this method convert any stream to string
    public static String ConvertInputToStringNoChange(InputStream inputStream) {

        BufferedReader bureader=new BufferedReader( new InputStreamReader(inputStream));
        String line ;
        String linereultcal="";

        try{
            while((line=bureader.readLine())!=null) {

                linereultcal+=line;

            }
            inputStream.close();


        }catch (Exception ex){}

        return linereultcal;
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                currentLocation.setText(strAdd);
                Toast.makeText(getApplicationContext(),strAdd,Toast.LENGTH_LONG).show();
                Log.d("My Current location", strReturnedAddress.toString());
            } else {
                Log.d("My Current loction ", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            Log.d("My Current loction ", "Canont get Address!");
        }
        return strAdd;
    }




}

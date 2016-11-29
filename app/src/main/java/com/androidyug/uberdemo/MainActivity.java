package com.androidyug.uberdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidyug.uberdemo.model.Category;
import com.androidyug.uberdemo.model.OlaResponse;
import com.androidyug.uberdemo.model.RideEstimate;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.android.rides.RideRequestButtonCallback;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.ServerTokenSession;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.uber.sdk.rides.client.error.ApiError;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView ivIcon;
    TextView tvPriceEstimate, tvTimeEstimate;
    FrameLayout flOlaButton;

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.fl_button_ola:
                // open the ola app with the given drop and pickup location
                openOlaApp("12.9491416", "77.64298","mini","5be2884248c945718eb749b8902d192e", "12.96","77.67");
                break;
        }
    }


    public interface OlaApiEndpoint {
        // Request method and URL specified in the annotation
        // Callback for the parsed response is the last parameter

//        https://sandbox-t.olacabs.com/v1/products?pickup_lat=12.9491416&pickup_lng=77.64298&drop_lat=12.96&drop_lng=77.67
        @Headers({"X-APP-TOKEN : 5be2884248c945718eb749b8902d192e"})
        @GET("v1/products")
        Call<OlaResponse> getRideEstimate(@Query("pickup_lat") String pickupLat,
                                          @Query("pickup_lng") String pickupLng,
                                          @Query("drop_lat") String dropLat,
                                          @Query("drop_lng") String dropLng);
    }



    // Trailing slash is needed
    public static final String BASE_URL_OLA = "https://sandbox-t.olacabs.com/";


    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL_OLA)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();


    RideRequestButton requestButton;
    SessionConfiguration config = new SessionConfiguration.Builder()
            // mandatory
            .setClientId("1yM6USaydGhlOLxpx4Bq_OAUwp_B0Plr")
            // required for enhanced button features
            .setServerToken("xZlNvUPnx7uVSB3BqKwoDy0ZRBkcNwPaVfbOCtay")
            // required for implicit grant authentication
            .setRedirectUri("androidyug.com")
            // required scope for Ride Request Widget features
            .setScopes(Arrays.asList(Scope.RIDE_WIDGETS))
            // optional: set Sandbox as operating environment
            .setEnvironment(SessionConfiguration.Environment.SANDBOX)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        UberSdk.initialize(config);

        OlaApiEndpoint apiService = retrofit.create(OlaApiEndpoint.class);

        Call<OlaResponse> call = apiService.getRideEstimate("12.9491416", "77.64298", "12.96","77.67");
        call.enqueue(new Callback<OlaResponse>() {
            @Override
            public void onResponse(Call<OlaResponse> call, Response<OlaResponse> response) {
                OlaResponse olaResponse = response.body();

                List<Category>catList = olaResponse.categories;
                List<RideEstimate> reList = olaResponse.ride_estimate;

                // sorting the RideEstimate array in the ascending order of min-price
                Collections.sort(reList, new Comparator<RideEstimate>() {
                    @Override
                    public int compare(RideEstimate t1, RideEstimate t2) {
                        return t1.amount_min.compareTo(t2.amount_min);
                    }
                });

                RideEstimate re = reList.get(0); // getting the first element in the array which is the minimum fare estimate
                String id = re.category;
                Category c = getCategory(id, catList); // getting the category detail based on id
                if (c!=null){
                    String iconUrl = c.image;
                    String currency = c.currency;
                    if (currency.equalsIgnoreCase("inr")){
                        String currencySymbol = getResources().getString(R.string.rupee_symbol);
                        String priceEstimate =  currencySymbol+ " " + re.amount_min + "-" + re.amount_max + " for OLA " + id.toUpperCase() ;
                        tvPriceEstimate.setText(priceEstimate);
                    } else {
                        String currencySymbol = getResources().getString(R.string.rupee_symbol);
                        String priceEstimate =  c.currency+ " " + re.amount_min + "-" + re.amount_max + " for OLA " + id.toUpperCase() ;
                        tvPriceEstimate.setText(priceEstimate);
                    }

                    String timeEstimate = re.travel_time_in_minutes + " min";
                    tvTimeEstimate.setText(timeEstimate);
                    Glide.with(MainActivity.this)
                            .load(iconUrl)
                            .into(ivIcon);
                }

            }

            @Override
            public void onFailure(Call<OlaResponse> call, Throwable t) {
                Log.d("ola_response", "failure");
            }
        });


        // get the context by invoking ``getApplicationContext()``, ``getContext()``, ``getBaseContext()`` or ``this`` when in the activity class
//        requestButton = new RideRequestButton(this);


         requestButton = (RideRequestButton) findViewById(R.id.uber_button);
        getRideParams();
        setRideEastimates();
// get your layout, for instance:
// RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_main);
//        ll.addView(requestButton);
        requestButton.loadRideInformation();

        RideRequestButtonCallback callback = new RideRequestButtonCallback() {

            @Override
            public void onRideInformationLoaded() {
                // react to the displayed estimates
                Log.d("TAG DEBUG",""+this);
            }

            @Override
            public void onError(ApiError apiError) {
                // API error details: /docs/riders/references/api#section-errors
                Log.d("TAG DEBUG ERROR",""+this);
            }

            @Override
            public void onError(Throwable throwable) {
                // Unexpected error, very likely an IOException
                Log.d("TAG DEBUG ERR",""+this);
            }
        };
        requestButton.setCallback(callback);
    }


    void getTheCheapRideEstimate(OlaResponse olaResponse){
        List<Category>catList = olaResponse.categories;
        List<RideEstimate> reList = olaResponse.ride_estimate;

        // sorting the RideEstimate array in the ascending order of min-price
        Collections.sort(reList, new Comparator<RideEstimate>() {
            @Override
            public int compare(RideEstimate t1, RideEstimate t2) {
                return t1.amount_min.compareTo(t2.amount_min);
            }
        });

        RideEstimate re = reList.get(0); // getting the first element in the array which is the minimum fare estimate
        String id = re.category;
        Category c = getCategory(id, catList); // getting the category detail based on id
        if (c!=null){

        }
    }



    Category getCategory(String id, List<Category> catList){
        Category _category = null;
        for (Category c : catList){
            if (c.id.equalsIgnoreCase(id)){
                _category = c;
            }
        }
        return _category;
    }



    void getRideParams(){
        RideParameters rideParams = new RideParameters.Builder()
                // Optional product_id from /v1/products endpoint (e.g. UberX). If not provided, most cost-efficient product will be used
//                .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d")
                // Required for price estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of dropoff location
                .setDropoffLocation(12.9279, 77.6271, "Uber HQ", "1455 Market Street, San Francisco")
                // Required for pickup estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of pickup location
                .setPickupLocation(12.9719, 77.6412, "Uber HQ", "1455 Market Street, San Francisco")
                // Required for price estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of dropoff location.
//                .setDropoffLocation(37.795079, -122.4397805, "Embarcadero", "One Embarcadero Center, San Francisco")
                .setDropoffLocation(12.9279, 77.6271, "Uber HQ", "1455 Market Street, San Francisco")
                .build();
// set parameters for the RideRequestButton instance
        requestButton.setRideParameters(rideParams);
    }

    void setRideEastimates(){
        ServerTokenSession session = new ServerTokenSession(config);
        requestButton.setSession(session);
    }


    void initView(){
       ivIcon  = (ImageView) findViewById(R.id.iv_icon);
       tvPriceEstimate = (TextView) findViewById(R.id.tv_price_estimate);
       tvTimeEstimate = (TextView) findViewById(R.id.tv_time_estimate);
       flOlaButton = (FrameLayout) findViewById(R.id.fl_button_ola);
       flOlaButton.setOnClickListener(this);
    }


    void openOlaApp(String pickup_lat, String pick_lng, String category, String x_app_token, String drop_lat, String drop_lng){

        String url = "olacabs://app/launch?lat="+ pickup_lat + "&lng="+ pick_lng+ "&category=" + category + "&utm_source=" + x_app_token+ "&landing_page=bk&drop_lat=" + drop_lat +"&drop_lng=" + drop_lng;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

    }




}

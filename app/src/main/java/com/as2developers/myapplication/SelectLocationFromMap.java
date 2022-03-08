package com.as2developers.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.as2developers.myapplication.Modals.UserModal;
//import com.as2developers.myapplication.databinding.MenuHeaderBinding;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SelectLocationFromMap extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //Initializing the variable
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    String latS,lonS;
    ImageView searchBtn;
    SearchView searchView;
    LatLng home;
    private List<Place.Field> fields;
    final int place_piker_req_code = 1;
    String LocationName;
    LatLng latLngGlobal;
    GoogleMap googleMapGlobal;
    private Marker markerGlobal;
    private Double homeLat,homeLon;
    String  myLocationName;
    BottomSheetDialog sheetDialog;
    Double latGlobal,lonGlobal;
    MarkerOptions optionsGlobal;
    RadioGroup radioGroup;
    String radioValue;
    Button next;
    String radioS,finalLocation,userLocality,UserAddressLine;
    EditText uLocality,uAddressLine;
    ImageButton ImgBtn;
    //for slide navigation bar
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    TextView WelcomeUser;
    private static final int REQUEST_CALL =1;


    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference ref;

    //for turing on location
    private LocationRequest locationRequest;
    public static  final int REQUEST_CHECK_SETTING = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location_from_map);
    //hooks for navigation bar
        drawerLayout =findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        TurnOnLocation();

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.dummy_content,R.string.dummy_content);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        //end

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user!=null){
            ref = database.getReference("Users").child(user.getPhoneNumber());
        }

        //to be done...

        searchBtn = (ImageView)findViewById(R.id.searchBtn);
        searchView = (SearchView)findViewById(R.id.searchView);
        ImgBtn = findViewById(R.id.Img);

        uAddressLine = findViewById(R.id.yourLocation);

        //if location is turn off the turn it on
        searchView.clearFocus();
        searchView.setFocusable(false);
        //noe assigning the variable
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        //initialize the fused location
        client = LocationServices.getFusedLocationProviderClient(this);

        //checking the permissions
        if (ActivityCompat.checkSelfPermission(SelectLocationFromMap.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //if perm. is granted
            //now calling the method
            getCurrentLocation();

            //searchview code
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    String location = searchView.getQuery().toString();
                    List<Address> addressList = null;
                    if(location!=null || !location.equals("")){
                        Geocoder geocoder = new Geocoder(SelectLocationFromMap.this);
                        try {
                            addressList = geocoder.getFromLocationName(location,1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(addressList.isEmpty()){
                            Toast.makeText(SelectLocationFromMap.this, "Can't find this location,search nearby locations!", Toast.LENGTH_SHORT).show();
                        }else {
                            if(markerGlobal!=null){ //to remove previous location marker
                                markerGlobal.remove();
                            }
                            Address address = addressList.get(0);
                            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                            markerGlobal = googleMapGlobal.addMarker(new MarkerOptions().position(latLng).title(location));
                            optionsGlobal = new MarkerOptions().position(latLng).title(location);
                            //googleMapGlobal.addMarker(optionsGlobal).setIcon(BitmapFromVector(getApplicationContext(), R.drawable.ic_location));
                            googleMapGlobal.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            //also i have to change the value of lat and lon
                            latLngGlobal = latLng;
                            latS = Double.toString(address.getLatitude());
                            lonS = Double.toString(address.getLongitude());
                            latGlobal = address.getLatitude();
                            lonGlobal = address.getLongitude();
                        }

                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });
        }else{
            //when  permission denied
            //again asking for permission
            ActivityCompat.requestPermissions(SelectLocationFromMap.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }



        ImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                WelcomeUser = navigationView.findViewById(R.id.Welcome_Note);
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModal user01 = snapshot.getValue(UserModal.class);
                        String userName = user01.getName();
                        WelcomeUser.setText(String.format("Welcome %s", userName));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                drawerLayout.openDrawer(GravityCompat.START);

            }
        });

    }

    private void TurnOnLocation() {
    }

    //for drawable
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    //for location
    private void getCurrentLocation() {
        //initialize task location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //when success

                if(location!=null){
                    //sync map
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        //added after
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {

                            //All things are ready here to show the maps
                            googleMapGlobal = googleMap; // puting this in global

                            //Initialize lat lng
                            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                            //making home location global
                            home = latLng;
                            latLngGlobal = home;
                            latGlobal = location.getLatitude();
                            lonGlobal = location.getLongitude();

                            homeLat = latGlobal;
                            homeLon = lonGlobal;

                            //creating marker options
                            MarkerOptions options = new MarkerOptions().position(latLng).title("You are here!");
                            //  optionsGlobal = options;
                            //now zoom into the map
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17)); //   <<---here we can change the ZOOM ratio..
                            //adding marker on map
                            googleMap.addMarker(options).setIcon(BitmapFromVector(getApplicationContext(), R.drawable.ic_location));
                            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);


                            //making the data global to share in there activity
                            latS = Double.toString(location.getLatitude());
                            lonS = Double.toString(location.getLongitude());


                            //when some one click on the search place options
                            //places.initialize places

                            fields = Arrays.asList(Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG);

                            Places.initialize(getApplicationContext(),"AIzaSyBCE8DVjURtaJp1rpbigQZD7Io-FZSmQIE"); //we have to put place api key here
                            //create a new place cline instance
                            PlacesClient placesClient = Places.createClient(getApplicationContext());

                        }

                    });
                }
            }
        });
    }

    //this for the search place


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case place_piker_req_code:
                Place place = Autocomplete.getPlaceFromIntent(data);
                LocationName = place.getName();
                latLngGlobal = place.getLatLng();
                MarkerOptions options = new MarkerOptions().position(latLngGlobal).title(LocationName);
                googleMapGlobal.moveCamera(CameraUpdateFactory.newLatLng(latLngGlobal));
                //now zoom into the map
                googleMapGlobal.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngGlobal,17)); //   <<---here we can change the ZOOM ratio..

                //adding marker on map
                googleMapGlobal.addMarker(options).setIcon(BitmapFromVector(getApplicationContext(), R.drawable.ic_location));
                googleMapGlobal.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==44){
            if(grantResults.length > 0  && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //when permission granted call method
                getCurrentLocation();
            }else{
                Toast.makeText(this, "Location Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode==REQUEST_CALL){
            if(grantResults.length > 0  && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //when permission granted call method
                makePhoneCall();
            }else{
                Toast.makeText(this, "Call Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void HomeLocation(View view) {
        //if someone click on the search button
        latLngGlobal = home;
        googleMapGlobal.animateCamera(CameraUpdateFactory.newLatLngZoom(home,17));
        if(markerGlobal!=null) markerGlobal.remove();

        latGlobal = homeLat;
        lonGlobal = homeLon;

        latS = Double.toString(homeLat);
        lonS = Double.toString(homeLon);
        Toast.makeText(this, "Your current location!", Toast.LENGTH_SHORT).show();
    }

    public void Continue(View view) {
        //when someone clicked on the Continue button
//calling bottomSheetLayout
        sheetDialog = new BottomSheetDialog(SelectLocationFromMap.this,R.style.BottomSheetStyle);

        View v = LayoutInflater.from(SelectLocationFromMap.this).inflate(R.layout.location_confirm,(LinearLayout)findViewById(R.id.sheet));
        sheetDialog.setContentView(v);

        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latGlobal,lonGlobal,1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String country = addresses.get(0).getCountryName();
        String locality = addresses.get(0).getLocality();
        String name = addresses.get(0).getAdminArea();
        String pin = addresses.get(0).getPostalCode();
        TextInputEditText editText = sheetDialog.findViewById(R.id.yourLocation);

        //to share data to an another activity
        finalLocation = locality+","+name+","+country+","+pin;
        editText.setText(finalLocation);

        sheetDialog.show();
        addAddressToFirebase(finalLocation);
        Toast.makeText(this,  "lat: "+latS+", lan: "+lonS+" LocationName: "+latLngGlobal, Toast.LENGTH_SHORT).show();

        uLocality = (EditText) sheetDialog.findViewById(R.id.UserLocality);
        uAddressLine = (EditText) sheetDialog.findViewById(R.id.addressLine);

        next = (Button) sheetDialog.findViewById(R.id.nextBtn);
        radioGroup = (RadioGroup) sheetDialog.findViewById(R.id.radio_Group);
        sheetDialog.show();
        radioS ="Home";
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.radio_home:
                        radioS = "Home";
                        Toast.makeText(SelectLocationFromMap.this, radioS, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.ratio_office:
                        radioS = "Office";
                        Toast.makeText(SelectLocationFromMap.this, radioS, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.radio_shop:
                        radioS = "Shop";
                        Toast.makeText(SelectLocationFromMap.this, radioS, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.radio_outlet:
                        radioS = "Outlet/Mall";
                        Toast.makeText(SelectLocationFromMap.this, radioS, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        //for keyboard shifting
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        showKeyboard(uLocality);
        showKeyboard(uAddressLine);
        uLocality.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                //Get Value form edittext
                userLocality = uLocality.getText().toString();
                //check condition
                if(i== EditorInfo.IME_ACTION_DONE){
                    //when action is equal to action done
                    //hide keyboard
                    hideKeyBoard(uLocality);
                    return true;
                }
            return false;
            }
        });
        uAddressLine.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                //Get Value form edittext
                UserAddressLine = uAddressLine.getText().toString();
                //check condition
                if(i== EditorInfo.IME_ACTION_DONE){
                    //when action is equal to action done
                    //hide keyboard
                    hideKeyBoard(uAddressLine);
                }
                return false;
            }
        });
        //after clicking next button
        next = (Button) sheetDialog.findViewById(R.id.nextBtn);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SelectLocationFromMap.this,FormFillupActivity.class);
                //passing the value
                //getting some value
                userLocality = uLocality.getText().toString();
                UserAddressLine = uAddressLine.getText().toString();
                finalLocation = editText.getText().toString();
                i.putExtra("LatLon",latGlobal);
                i.putExtra("locationType",radioS);
                i.putExtra("LocationDetails",finalLocation);
                i.putExtra("pin",pin);
                i.putExtra("locality",userLocality);
                i.putExtra("AddressLine",UserAddressLine);


                //now if location type selected then only go to next activity
                if(radioS.length()==0){
                    Toast.makeText(SelectLocationFromMap.this, "Please Select A location type.eg: home", Toast.LENGTH_SHORT).show();
                }else{
                    startActivity(i);
                    Toast.makeText(SelectLocationFromMap.this, radioS, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void addAddressToFirebase(String finalLocation) {

        ref.child("address").setValue(finalLocation);
    }

    private void hideKeyBoard(EditText editText) {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(editText.getApplicationWindowToken(),0);
    }

    private void showKeyboard(EditText editText) {
        //Initialize input manager
        InputMethodManager manager = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE
        );
        //show soft keyboard
        manager.showSoftInput(editText.getRootView(),InputMethodManager.SHOW_IMPLICIT);
        //Focus on EditText
        editText.requestFocus();
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.profile:
                Intent i = new Intent(getApplicationContext(),ProfilePage.class);
                startActivity(i);
                break;
            case R.id.pickup:
                Toast.makeText(this, "Opening to a new pickup..", Toast.LENGTH_SHORT).show();
                break;
            case R.id.howItWorks:
                startActivity(new Intent(this,HowItWorks.class));
                break;
            case R.id.aboutUs:
                startActivity(new Intent(this,AboutUs.class));
                break;
            case R.id.call_us:
                makePhoneCall();
                break;
            case R.id.home:
                Toast.makeText(this, "You are at Home!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logOut:
                Toast.makeText(this, "Logging out..", Toast.LENGTH_SHORT).show();
                //Toast.makeText(SelectLocationFromMap.this, "Back to Home Page", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SelectLocationFromMap.this,Login_Phone.class));
                finish();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void makePhoneCall(){

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        }
        else{
            String phoneNo = "tel:"+"8867825522";
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse(phoneNo));
            startActivity(intent);
        }
    }
}
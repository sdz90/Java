public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    //Initialize variables
    TextView textView1, textView2, textView3, textView4, textView5, textView6, textView7, textView8; //textView5, textView7;
    FusedLocationProviderClient fusedLocationProviderClient;
    private AdView mAdView;
    private static final int DEFAULT_ZOOM = 15;
    GoogleMap mMap;
    MapView mapView;
    private Marker marker;
    private LatLng latLng;
    ImageButton buttonLocation, copyaddress, copyMarkeraddress, mapsLayers, shareAddress, shareMarkerAddress, saveAddress, saveMarkerAddress, searchButton;
    SearchView searchView;
    Location my_location = new Location("locationB");
    //String addressBuff, markerAddressBuff;
    String AddressLat, AddressLong, MarkerAddressLat, MarkerAddressLong, savedAddress, savedMarkerAddress, savedAdressName;
    private String editText1 = "";
    private String editText2 = "";
    MyDatabaseHelper myDB;
    MenuItem menuItem;
    boolean isSearchViewShown = false;

    //Create menu
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        menuItem = menu.findItem(R.id.menu_item1);

        int profile_counts = myDB.getProfilesCount();
        myDB.close();

        if (profile_counts == 0) {
            menuItem.setActionView(null);
        } else {

            menuItem.setActionView(R.layout.notification_badge);
            View view = menuItem.getActionView();
            textView8 = view.findViewById(R.id.notification_count);
            textView8.setText(String.valueOf(profile_counts));
            menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menu.performIdentifierAction(menuItem.getItemId(), 0);
                }
            });
        }

    return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item1:
                Intent intent = new Intent(this, SavedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                return true;
            case R.id.map_markers:
                Intent intent3 = new Intent(this, MapMarkersActivity.class);
                intent3.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent3);
                return true;
            case R.id.menu_item2:
                Intent intent2 = new Intent(this, AboutActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent2);
                return true;
            case R.id.menu_item3:
                //kill app
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Check if Location Services are enabled

    private boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return LocationManagerCompat.isLocationEnabled(locationManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        invalidateOptionsMenu();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Show First time greeting dialog
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);

        if (firstStart) {
            showStartDialog();
        }

        //Assign variable
        buttonLocation = findViewById(R.id.button_Location);
        copyaddress = findViewById(R.id.copyButton);
        copyMarkeraddress = findViewById(R.id.copyButton2);
        shareAddress = findViewById(R.id.shareButton);
        shareMarkerAddress = findViewById(R.id.shareButton2);
        mapsLayers = findViewById(R.id.layersButton);
        saveAddress = findViewById(R.id.saveAddressButton);
        saveMarkerAddress = findViewById(R.id.saveMarkerAddressButton);
        searchButton = findViewById(R.id.searchButton);

        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);
        textView5 = findViewById(R.id.textView5);
        textView6 = findViewById(R.id.textView6);
        textView7 = findViewById(R.id.textView7);
        searchView = findViewById(R.id.searchLocation);

        copyaddress.setVisibility(View.INVISIBLE);
        saveAddress.setVisibility(View.INVISIBLE);
        shareAddress.setVisibility(View.INVISIBLE);
        copyMarkeraddress.setVisibility(View.INVISIBLE);
        saveMarkerAddress.setVisibility(View.INVISIBLE);
        shareMarkerAddress.setVisibility(View.INVISIBLE);



        //Initialize Ad Banner
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        //Initialize Maps
        mapView = findViewById(R.id.mapsView);

        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }

        myDB = new MyDatabaseHelper(MainActivity.this);

        //Initialize fusedLocationProviderClient;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //when permission granted
            getLocation();
            updateLocationUI();

        } else {
            //when permission denied
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        //searchView

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;
                //if (location != null || !location.equals("")){
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try{
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList == null || addressList.size() == 0) {
                        Toast.makeText(MainActivity.this, "ERROR! No such address/location found!", Toast.LENGTH_SHORT).show();
                    }else {
                        Address address = addressList.get(0);
                        LatLng latlng = new LatLng(address.getLatitude(), address.getLongitude());
                        String addressStr = addressList.get(0).getAddressLine(0);
                        mMap.clear();
                        //mMap.addMarker(new MarkerOptions().position(latlng).title(location));

                        MarkerAddressLat = String.format(java.util.Locale.US, "%.6f", address.getLatitude());
                        MarkerAddressLong = String.format(java.util.Locale.US, "%.6f", address.getLongitude());

                        savedMarkerAddress = addressStr;


                        //textView7.setText(""+ address);
                        textView4.setText(addressStr);
                        //make marker copy, save, share buttons visible
                        copyMarkeraddress.setVisibility(View.VISIBLE);
                        saveMarkerAddress.setVisibility(View.VISIBLE);
                        shareMarkerAddress.setVisibility(View.VISIBLE);


                        //place marker where user just clicked
                        marker = mMap.addMarker(new MarkerOptions().position(latlng).title(location).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

                        CalculateDistance(); //call calculation of distance
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10));
                    }
               // }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //Copy Address to clipboard

        copyaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Copy Address", textView3.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(), "Address copied to clipboard.", Toast.LENGTH_SHORT).show();
            }
        });

        //Copy Marker Address to clipboard

        copyMarkeraddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Copy Address", textView4.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(), "Marker address copied to clipboard.", Toast.LENGTH_SHORT).show();
            }
        });

        //Change Maps Layer type

        mapsLayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeMapLayer();
            }
        });

        //Show&hide searchbar

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSearchViewShown){
                    isSearchViewShown = true;
                    findViewById(R.id.searchLocation).setVisibility(View.VISIBLE);

                }else {
                    isSearchViewShown = false;
                    findViewById(R.id.searchLocation).setVisibility(View.GONE);
                }
            }
        });


        //Share My Address

        shareAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = textView3.getText().toString();
                //String uriStr = textViewUri.getText().toString();

                String shareString = title; //+ ", " + uriStr;

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareString);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share address via..."));

            }
        });

        //share Marker Address

        shareMarkerAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = textView4.getText().toString();
                //String uriStr = textViewUri.getText().toString();

                String shareString = title; //+ ", " + uriStr;

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareString);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share address via..."));
                //startActivity(sendIntent);

            }
        });

        saveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(MainActivity.this);
                final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
                if (textView3.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "ERROR! Check if permissions have been granted or Location Services enabled", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Give name to this location...");

                    // Set up the input
                    final EditText input = new EditText(MainActivity.this);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editText1 = input.getText().toString();
                            savedAdressName = editText1;
                            myDatabaseHelper.addAddress(timeStamp, savedAddress, AddressLat, AddressLong, savedAdressName);
                            invalidateOptionsMenu();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();

                }
            }
        });

        saveMarkerAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(MainActivity.this);
                final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
                if (textView4.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "ERROR! Check if permissions have been granted or Location Services enabled", Toast.LENGTH_SHORT).show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Give name to this location...");

                    // Set up the input
                    final EditText input = new EditText(MainActivity.this);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editText2 = input.getText().toString();
                            //String Address = editText2 + ": \n" + savedMarkerAddress;
                            savedAdressName = editText2;
                            myDatabaseHelper.addAddress(timeStamp, savedMarkerAddress, MarkerAddressLat, MarkerAddressLong, savedAdressName);
                            invalidateOptionsMenu();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                            public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();

                }

            }
        });


        //main button
        buttonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check permission
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //check Location Services
                    if (!isLocationEnabled(MainActivity.this)) {

                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Oops...");
                        alertDialog.setMessage("Please enable Location Services on your phone to locate your address.");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                       //dialog.dismiss();
                                       MainActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    }
                                });
                        alertDialog.show();
                    } else {

                    //when permission granted
                    getLocation();
                    updateLocationUI();
                    }
                    //Toast.makeText(getApplicationContext(),"Address found.",Toast.LENGTH_SHORT).show();

                } else {
                    //when permission denied
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }

        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       /* switch (requestCode) {
            case 1024:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   // saveAddressToTxtFile(addressBuff);
                } else
                    Toast.makeText(this, "ERROR! Check if WRITE permissions have been granted!", Toast.LENGTH_SHORT).show();
        }*/
        switch (requestCode) {
            case 44:
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
                updateLocationUI();
            } else
                Toast.makeText(this, "ERROR! Check if Location permissions have been granted!", Toast.LENGTH_SHORT).show();
        }
    }


    private void showStartDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hello")
                .setMessage("Thanks")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("firstStart", false);
                editor.apply();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //Initialize location
                Location location = task.getResult();
                if (location != null) {
                    //if No address then don't display copy button / save button / share button

                        copyaddress.setVisibility(View.INVISIBLE);
                        saveAddress.setVisibility(View.INVISIBLE);
                        shareAddress.setVisibility(View.INVISIBLE);

                    try {
                        //Initialize geoCoder
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        //Initialize address list
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1);
                        //set variable for distance calculation

                        //Toast.makeText(MainActivity.this, "Address found.", Toast.LENGTH_SHORT).show();


                        my_location.setLatitude(location.getLatitude());
                        my_location.setLongitude(location.getLongitude());

                        savedAddress = addresses.get(0).getAddressLine(0);

                        //Round Lat Long as values too long
                        AddressLat = String.format(java.util.Locale.US, "%.6f", addresses.get(0).getLatitude());
                        AddressLong = String.format(java.util.Locale.US, "%.6f", addresses.get(0).getLongitude());
                        //Set latitude on TextView
                        textView1.setText("Lat: " + AddressLat);
                        //set longitude on TextView
                        textView2.setText("Long: " + AddressLat);
                        //set country name on TextView
                        //textView3.setText(Html.fromHtml(
                        //        "<font color='#6200EE'><b>Country :</b><br></font>"
                        //                + addresses.get(0).getCountryName()
                        //));
                        //set locality on TextView
                        //textView4.setText(Html.fromHtml(
                        //        "<font color='#6200EE'><b>County/District :</b><br></font>"
                        //                + addresses.get(0).getLocality()
                        //));
                        //set address on TextView
                        textView3.setText(addresses.get(0).getAddressLine(0));

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(location.getLatitude(),
                                        location.getLongitude()), DEFAULT_ZOOM));
                        Toast.makeText(getApplicationContext(), "Address found.", Toast.LENGTH_SHORT).show();
                        copyaddress.setVisibility(View.VISIBLE); //set copy button visible
                        saveAddress.setVisibility(View.VISIBLE); //set copy button visible
                        shareAddress.setVisibility(View.VISIBLE); //set share button visible
                        findViewById(R.id.progress_bar).setVisibility(View.GONE);

                        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("location_latitude", AddressLat);
                        editor.putString("location_longitude", AddressLong);
                        editor.apply();

                    } catch (IOException e) {
                        e.printStackTrace();
                        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getBaseContext());
        mMap = googleMap;
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Add Zoom buttons
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Add a marker in Sydney and move the camera
      //  LatLng sydney = new LatLng(-34, 151); LatLng ind = new LatLng(8.524139, 76.936638);
      //  mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
      //  mMap.addMarker(new MarkerOptions().position(ind).title("Marker in Trivandrum"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(ind));


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                latLng = point;
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    if (!isLocationEnabled(MainActivity.this)) {

                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Oops...");
                        alertDialog.setMessage("Please enable Location Services on your phone to locate your address.");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                       // dialog.dismiss();
                                        MainActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    }
                                });
                        alertDialog.show();
                    } else {

                        try {
                            addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
                            //check for 0
                            if (addresses.size() == 0) {
                                Toast.makeText(MainActivity.this, "ERROR! No address found!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                            String address = addresses.get(0).getAddressLine(0);
                            // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            String city = addresses.get(0).getLocality();
                            String state = addresses.get(0).getAdminArea();
                            String country = addresses.get(0).getCountryName();
                            String postalCode = addresses.get(0).getPostalCode();
                            String knownName = addresses.get(0).getFeatureName();
                            String countrycode = addresses.get(0).getCountryCode();


                            MarkerAddressLat = String.format(java.util.Locale.US,"%.6f", addresses.get(0).getLatitude());
                            MarkerAddressLong = String.format(java.util.Locale.US, "%.6f", addresses.get(0).getLongitude());
                            savedMarkerAddress = address;

                            textView6.setText("Lat: " + MarkerAddressLat);
                            textView7.setText("Long: " + MarkerAddressLong);


                            //textView7.setText(""+ address);
                            textView4.setText(address);
                            //make marker copy, save, share buttons visible
                            copyMarkeraddress.setVisibility(View.VISIBLE);
                            saveMarkerAddress.setVisibility(View.VISIBLE);
                            shareMarkerAddress.setVisibility(View.VISIBLE);

                            if (marker != null) {
                                marker.remove();
                            }

                            //place marker where user just clicked
                            marker = mMap.addMarker(new MarkerOptions().position(point).title("Placed Marker").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

                            CalculateDistance(); //call calculation of distance


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });
    }

    //Change Map Layers method

    public void ChangeMapLayer() {

        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //} else if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
        //    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

       // } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_TERRAIN) {
       //     mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }


    //calculate distance between my location and marker

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMap.clear();
        mapView.onDestroy();
    }

    public void CalculateDistance() {

            Location selected_location = new Location("locationA");
            selected_location.setLatitude(marker.getPosition().latitude);
            selected_location.setLongitude(marker.getPosition().longitude);

            double distanceKM = selected_location.distanceTo(my_location)/1000; //in km
            double distanceMI = selected_location.distanceTo(my_location)/1000/1.60934; //in miles
            String distanceRoundedKM = String.format(java.util.Locale.US, "%.3f", distanceKM);
            String distanceRoundedMI = String.format(java.util.Locale.US, "%.3f", distanceMI);

        if (Build.VERSION.SDK_INT >= 24) {
            textView5.setText(Html.fromHtml(
                    "<font color='#6200EE'><b>Distance to marker:</b><br></font>"
                            + distanceRoundedKM + "km / " + distanceRoundedMI + "mi", 0
            ));
        } else {
            textView5.setText(Html.fromHtml(
                    "<font color='#6200EE'><b>Distance to marker:</b><br></font>"
                            + distanceRoundedKM + "km / " + distanceRoundedMI + "mi"
            ));
        }
    }
}



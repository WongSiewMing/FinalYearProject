package com.example.raindown.finalyearproject;

import android.content.*;
import android.net.*;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.*;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.*;

import java.io.UnsupportedEncodingException;
import java.util.*;
import Helper.*;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class Navigation extends AppCompatActivity implements AdapterView.OnItemClickListener {


    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;
    private ListView navigationList;
    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;
    public static Student student = null;
    List<Subscribe> arrSubscribe = new ArrayList<>();

    //new
    private LinearLayout navigationLayout;
    private TextView userName, userID;
    private ImageView userImage;
    private String userStoreID = "";
    private static final String TAG = "testData";
    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation);
        this.setTitle("Navigation");

        Intent intent = getIntent();
        //original
        student = (Student) intent.getSerializableExtra("Login");

        Log.d(TAG, "User Name : " + student.getStudentName());

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("topicstr", student.getStudentID());
        editor.commit();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.openDrawer, R.string.closeDrawer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        //new
        userName = findViewById(R.id.nav_name);
        userID = findViewById(R.id.nav_id);
        userImage = findViewById(R.id.nav_Photo);

        userName.setText(student.getStudentName());
        userID.setText(student.getStudentID());
        Picasso.with(this).load(student.getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(userImage);

        navigationLayout = (LinearLayout) findViewById(R.id.navigationBar);
        navigationList = (ListView) findViewById(R.id.menulist);
        ArrayList<String> navigationItem = new ArrayList<String>();
        navigationItem.add("Profile");
        navigationItem.add("Menu");
        navigationItem.add("My Store");
        navigationItem.add("About Us");


        navigationList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, navigationItem);

        navigationList.setAdapter(adapter);

        navigationList.setOnItemClickListener(this);


        //testing
        Toolbar toolbar = findViewById(R.id.customAppBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        fragmentManager = getSupportFragmentManager();

        // getTopicString();
        loadSelection(1);

        //new update to store user info in sharedPreferences
        UserSharedPreferences.init(getApplicationContext());
        UserSharedPreferences.write(UserSharedPreferences.userID, student.getStudentID());
        UserSharedPreferences.write(UserSharedPreferences.userName, student.getStudentName());
        UserSharedPreferences.write(UserSharedPreferences.userPhoto, student.getPhoto());
        UserSharedPreferences.write(UserSharedPreferences.userProgramme, student.getStudentProgramme());
        startService(new Intent(this, ServiceCenter.class));

    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerLayout.isDrawerOpen(navigationLayout)) {
            drawerLayout.closeDrawer(navigationLayout);
        } else {
            drawerLayout.openDrawer(navigationLayout);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        loadSelection(position);
        drawerLayout.closeDrawer(navigationLayout);
    }


    private void loadSelection(int i) {
        navigationList.setItemChecked(i, true);
        switch (i) {
            case 0:
                MyProfile myProfile = new MyProfile();
                Bundle bundle2 = new Bundle();
                bundle2.putSerializable("MyProfile", student);
                myProfile.setArguments(bundle2);
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentHolder, myProfile);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

            case 1:
                fragmentManager = this.getSupportFragmentManager();
                for (int i2 = 0; i2 < fragmentManager.getBackStackEntryCount(); ++i2){
                    fragmentManager.popBackStack();
                }
                MainMenu mainMenu = new MainMenu();
                Bundle bundle= new Bundle();
                bundle.putSerializable("MainMenu", student);
                mainMenu.setArguments(bundle);
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentHolder, mainMenu);
                //fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

            case 2:
                getUserStoreID();
                break;

            case 3:
                AboutUs aboutUs = new AboutUs();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentHolder, aboutUs);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                break;

        }
    }


    public void getTopicString() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(this.getApplication());

                JsonArrayRequest followChatObjectRequest = new JsonArrayRequest(Constant.serverFile + "followNotification.php?studentID=" + student.getStudentID(),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    arrSubscribe.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject subscribeResponse = (JSONObject) response.get(i);
                                        arrSubscribe.add(new Subscribe(subscribeResponse.getString("subscribeID"), subscribeResponse.getString("subscribeeID"),
                                                subscribeResponse.getString("subscriberID"), subscribeResponse.getString("subscribeDate"),
                                                subscribeResponse.getString("unsubscribeDate"), subscribeResponse.getString("subscribeStatus")));
                                    }
                                    if(arrSubscribe.size() != 0){
                                        Intent intent = new Intent(getApplicationContext(), NotificationService.class);
                                        startService(intent);
                                    }
                                } catch (Exception e) {
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {

                            }
                        });

                queue.add(followChatObjectRequest);
            } else {
                Toast.makeText(this.getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this.getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void getUserStoreID(){
        try {
            ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected){
                RequestQueue queue = Volley.newRequestQueue(this);
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getSpecificStoreID.php?studentID=" + student.getStudentID(),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try{
                                    for(int i = 0; i < response.length(); i++){
                                        JSONObject storeResponse = (JSONObject) response.get(i);
                                        userStoreID = storeResponse.getString("StoreID");
                                        Log.d(TAG, "User Store ID = " + userStoreID);
                                        if (userStoreID.equals("empty")){
                                            Log.d(TAG, "This user had no registed store.");
                                            RegisterStore registerStore = new RegisterStore();

                                            fragmentTransaction = fragmentManager.beginTransaction();
                                            fragmentTransaction.replace(R.id.fragmentHolder, registerStore);
                                            fragmentTransaction.addToBackStack(null);
                                            fragmentTransaction.commit();

                                        }else{
                                            StoreProfile storeProfile = new StoreProfile();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("storeID", userStoreID);
                                            storeProfile.setArguments(bundle);

                                            fragmentTransaction = fragmentManager.beginTransaction();
                                            fragmentTransaction.replace(R.id.fragmentHolder, storeProfile);
                                            fragmentTransaction.addToBackStack(null);
                                            fragmentTransaction.commit();
                                        }
                                    }

                                }catch (Exception e){

                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
                queue.add(jsonObjectRequest);
            }else {
                Toast.makeText(this.getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        }catch (Exception e) {
            Toast.makeText(this.getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, ServiceCenter.class));
    }

    @Override
    public void onBackPressed() {
        fragmentManager = this.getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 0){
            if (backPressedTime + 2000 > System.currentTimeMillis()){
                Log.d("Track Status", "You quit");
                backToast.cancel();
                super.onBackPressed();

                return;
            }else {
                backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
                backToast.show();
            }
            backPressedTime = System.currentTimeMillis();
        }else {
            super.onBackPressed();
        }



//        UserSharedPreferences.remove(UserSharedPreferences.userName);
//        UserSharedPreferences.remove(UserSharedPreferences.userPhoto);
//        UserSharedPreferences.remove(UserSharedPreferences.userProgramme);
//        UserSharedPreferences.remove(UserSharedPreferences.userID);
    }
}

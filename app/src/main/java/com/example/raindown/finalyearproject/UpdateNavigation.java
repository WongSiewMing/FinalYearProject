package com.example.raindown.finalyearproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import Helper.BottomNavigationViewHelper;
import Helper.Constant;
import Helper.PreferenceUnits;
import Helper.Student;

/*Author : Wong Siew Ming
Programme : RSD3
Year : 2019*/

public class UpdateNavigation extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mytoggle;
    private Toolbar toolbar;
    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;
    private BottomNavigationView bottom_navigation;

    public static Student student = null;
    private TextView userName, userID;
    private ImageView userImage;
    private String userStoreID = "";
    private static final String TAG = "testData";
    private long backPressedTime;
    private Toast backToast;

    protected void onCreate(Bundle savedInstanceState) {
        fragmentManager = this.getSupportFragmentManager();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_navigation);
        setNavigationViewListener();

        Intent intent = getIntent();
        student = (Student) intent.getSerializableExtra("Login");

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("topicstr", student.getStudentID());
        editor.commit();

        navigationView = (NavigationView) findViewById(R.id.nav_bar);
        drawerLayout = findViewById(R.id.myDrawer);
        mytoggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        bottom_navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        View view = bottom_navigation.findViewById(R.id.navigation_home);
        view.performClick();

        drawerLayout.addDrawerListener(mytoggle);
        mytoggle.syncState();

        userName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.update_nav_name);
        userID = (TextView) navigationView.getHeaderView(0).findViewById(R.id.update_nav_id);
        userImage = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.update_nav_photo);

        userName.setText(student.getStudentName().toUpperCase());
        userID.setText(student.getStudentID());
        Picasso.with(this).load(student.getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(userImage);

        toolbar = findViewById(R.id.update_customAppBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        UserSharedPreferences.init(getApplicationContext());
        UserSharedPreferences.write(UserSharedPreferences.userID, student.getStudentID());
        UserSharedPreferences.write(UserSharedPreferences.userName, student.getStudentName());
        UserSharedPreferences.write(UserSharedPreferences.userPhoto, student.getPhoto());
        UserSharedPreferences.write(UserSharedPreferences.userProgramme, student.getStudentProgramme());
        startService(new Intent(this, ServiceCenter.class));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle bt_navigation view item clicks here.
        switch (item.getItemId()) {

            case R.id.nav_home:
                Home home = new Home();
                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("Home", student);
                home.setArguments(bundle1);
                loadFragment(home);
                break;

            case R.id.nav_profile:
                MyProfile myProfile = new MyProfile();
                Bundle bundle2 = new Bundle();
                bundle2.putSerializable("MyProfile", student);
                myProfile.setArguments(bundle2);
                loadFragment(myProfile);
                break;

            case R.id.nav_favourite:
                FavouriteList favouriteList = new FavouriteList();
                Bundle bundle3 = new Bundle();
                bundle3.putSerializable("FavouriteList", student);
                favouriteList.setArguments(bundle3);
                loadFragment(favouriteList);
                break;

            case R.id.nav_follow:
                FollowList followingList = new FollowList();
                Bundle bundle4 = new Bundle();
                bundle4.putSerializable("FollowList", student);
                followingList.setArguments(bundle4);
                loadFragment(followingList);
                break;

            case R.id.nav_appointment:
                Appointment_Menu appointment_menu = new Appointment_Menu();
                Bundle bundle8 = new Bundle();
                bundle8.putSerializable("AppointmentMenu", student);
                appointment_menu.setArguments(bundle8);
                loadFragment(appointment_menu);
                break;

            case R.id.nav_store:
                getUserStoreID();
                break;

            case R.id.nav_history:
                break;

            case R.id.nav_about:
                AboutUs aboutUs = new AboutUs();
                loadFragment(aboutUs);
                break;

            case R.id.nav_feedback:
                FeedbackForm feedbackForm = new FeedbackForm();
                loadFragment(feedbackForm);
                break;

            case R.id.nav_logout:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setMessage("Are you sure you want to Logout ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                logOut();
                            }
                        })

                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
        }
        //close bt_navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;

    }

    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_bar);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.update_fragmentHolder, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void logOut(){
        PreferenceUnits.saveID(null,this);
        PreferenceUnits.savePassword(null,this);
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }
    public void getUserStoreID(){
        Fragment fragment;
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
                                            fragmentTransaction.replace(R.id.update_fragmentHolder, registerStore);
                                            fragmentTransaction.addToBackStack(null);
                                            fragmentTransaction.commit();

                                        }else{
                                            StoreProfile storeProfile = new StoreProfile();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("storeID", userStoreID);
                                            storeProfile.setArguments(bundle);

                                            fragmentTransaction = fragmentManager.beginTransaction();
                                            fragmentTransaction.replace(R.id.update_fragmentHolder, storeProfile);
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
        if (fragmentManager.getBackStackEntryCount() != 1) {
            super.onBackPressed();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Home home = new Home();
                    Bundle bundle1 = new Bundle();
                    bundle1.putSerializable("Home", student);
                    home.setArguments(bundle1);
                    loadFragment(home);
                    return true;

                case R.id.navigation_news:
                    UserActivity userActivity = new UserActivity();
                    Bundle bundle2 = new Bundle();
                    bundle2.putSerializable("UserActivity", student);
                    userActivity.setArguments(bundle2);
                    loadFragment(userActivity);
                    return true;

                case R.id.navigation_summary:

                    return true;

                case R.id.navigation_store:
                    StoreCategory storeList = new StoreCategory();
                    loadFragment(storeList);
                    return true;

                case R.id.navigation_profile:
                    MyProfile myProfile = new MyProfile();
                    Bundle bundle5 = new Bundle();
                    bundle5.putSerializable("MyProfile", student);
                    myProfile.setArguments(bundle5);
                    loadFragment(myProfile);
                    return true;
            }

            return false;
        }
    };

    public void clickEvent(View v)
    {
        Fragment visibleFragment = getCurrentFragment();

        Chat_Menu chat_menu = new Chat_Menu();
        Bundle bundle1 = new Bundle();
        bundle1.putSerializable("ChatMenu", student);
        chat_menu.setArguments(bundle1);

        if (visibleFragment instanceof Chat_Menu){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right);
            transaction.replace(R.id.update_fragmentHolder, chat_menu);
            transaction.addToBackStack(null);
            transaction.commit();
        }
            else{
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right);
            transaction.replace(R.id.update_fragmentHolder, chat_menu);
            transaction.addToBackStack(null);
            transaction.commit();
        }

    }

    Fragment getCurrentFragment()
    {
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.update_fragmentHolder);
        return currentFragment;
    }

}

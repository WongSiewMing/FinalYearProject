package com.example.raindown.finalyearproject;

import android.app.*;
import android.content.Context;
import android.net.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.Request;
import com.android.volley.toolbox.*;
import com.squareup.picasso.*;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;
import Helper.*;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class StuffDetails extends Fragment {

    FragmentManager fragmentManager;
    View view;
    ImageView favourite, chat, viewProfile, totalLoveEarned;
    TextView totalFavouritesText, noTotalFavourites;
    Stuff stuff = null;
    Student s = null;
    ProgressDialog pDialog = null;
    String currentFavouriteID = "", favouriteID = "", jsonURL = "", favouriteDate = "", favouriteCount = "", loveCount = "";
    SimpleDateFormat sdf = null;
    Date d = null;
    public static String confirmedTopic = "";
    public final static List<PrivateChat> arrayPrivateChat = new ArrayList<>();
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    JSONObject jsonObj;
    String command = "", message = "", countFollowersCommand = "", countFollowingCommand = "", ratingCommand = "",
            sellerStuffCommand = "", checkSubscribeCommand = "";
    private ImageView btnBack;
    private Button btnAction;
    private final static String TAG = "Stuff Details";


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity) {
            a = (Activity) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        stuff = (Stuff) bundle.getSerializable("ClickedStuff");
        getActivity().setTitle(stuff.getStuffName());

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        view = inflater.inflate(R.layout.stuffdetails, container, false);
        view = inflater.inflate(R.layout.stuffdetails_v2, container, false);
        pDialog = new ProgressDialog(getActivity());

        final Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("StudentClickedStuff"); //me

        ImageView stuffPhoto = (ImageView) view.findViewById(R.id.stuffImage);
        Picasso.with(getActivity()).load(stuff.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(stuffPhoto);

        TextView studentName = (TextView) view.findViewById(R.id.studentName);
        studentName.setText(stuff.getStudentID().getStudentName()); //seller

        TextView stuffName = (TextView) view.findViewById(R.id.stuffName);
        stuffName.setText(stuff.getStuffName());

        TextView stuffDescription = (TextView) view.findViewById(R.id.stuffDescription);
        stuffDescription.setText(stuff.getStuffDescription());

        TextView stuffPrice = (TextView) view.findViewById(R.id.stuffPrice);
        stuffPrice.setText(String.format("%.2f", stuff.getStuffPrice()));

        TextView stuffQuantity = (TextView) view.findViewById(R.id.stuffQuantity);
        stuffQuantity.setText("" + stuff.getStuffQuantity());

        TextView stuffCategory = (TextView) view.findViewById(R.id.stuffCategory);
        stuffCategory.setText(stuff.getStuffCategory());

        TextView stuffCondition = (TextView) view.findViewById(R.id.stuffCondition);
        stuffCondition.setText(stuff.getStuffCondition());

        TextView stuffValidity = (TextView) view.findViewById(R.id.stuffValidity);
        stuffValidity.setText(stuff.getValidStartDate() + " - " + stuff.getValidEndDate());

        favourite = (ImageView) view.findViewById(R.id.unfavourite);
        chat = (ImageView) view.findViewById(R.id.chat);
        viewProfile = (ImageView) view.findViewById(R.id.view);

        btnBack = view.findViewById(R.id.back);
        btnAction = view.findViewById(R.id.makeAppointment);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnAction.getText().toString().trim().equals("MAKE APPOINTMENT")){
                    Log.d(TAG, "Go to make Appointment ui.");
                    Log.d(TAG, "Main user = " + UpdateNavigation.student.getStudentName());
                    MakeAppointment frag = new MakeAppointment();
                    Bundle bundle1 = new Bundle();
                    bundle1.putSerializable("ClickedStuff", stuff);
                    frag.setArguments(bundle1);
                    fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack(null)
                            .commit();
                }else  if (btnAction.getText().toString().trim().equals("EDIT STUFF")){
                    Log.d(TAG, "Go edit Stuff");
                    MaintainStuff frag = new MaintainStuff();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("MaintainStuff", stuff);
                    frag.setArguments(bundle);

                    fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack(null)
                            .commit();

                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        if (stuff.getStudentID().getStudentID().equals(s.getStudentID())) {
            favourite.setEnabled(false);
            chat.setEnabled(false);
            viewProfile.setEnabled(false);
            favourite.setVisibility(view.INVISIBLE);
            chat.setVisibility(view.INVISIBLE);
            viewProfile.setVisibility(view.INVISIBLE);
            totalLoveEarned = (ImageView) view.findViewById(R.id.totalLoveEarned);
            totalLoveEarned.setVisibility(view.VISIBLE);
            totalFavouritesText = (TextView) view.findViewById(R.id.totalFavouritesText);
            totalFavouritesText.setVisibility(view.VISIBLE);
            noTotalFavourites = (TextView) view.findViewById(R.id.noTotalFavourites);
            noTotalFavourites.setVisibility(view.VISIBLE);
            btnAction.setText("EDIT STUFF");

            command = "{\"command\": \"303035303032\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(stuff.getStudentID().getStudentID()) + "\" ," +
                    "\"stuffID\": " + "\"" + Conversion.asciiToHex(stuff.getStuffID()) + "\"}";

            Log.d("shiit",stuff.getStuffID());

            pahoMqttClient = new PahoMqttClient();
            mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,command,"MY/TARUC/SSS/000000001/PUB");
            getTotalLove(command);


        } else {

            command = "{\"command\": \"303035303033\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\" ," +
                    "\"stuffID\": " + "\"" + Conversion.asciiToHex(stuff.getStuffID()) + "\"}";

            pahoMqttClient = new PahoMqttClient();
            mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,command,"MY/TARUC/SSS/000000001/PUB");
            checkFavourite(command);

            favourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (favourite.getDrawable().getConstantState() == ContextCompat.getDrawable(getActivity(), R.mipmap.icon_unfavourite).getConstantState()) {
                        favourite.setImageResource(R.mipmap.icon_favourite);
                        setFavourite("favourite");
                    } else {
                        favourite.setImageResource(R.mipmap.icon_unfavourite);
                        setFavourite("unfavourite");
                    }
                }
            });

            chat.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    command = "{\"command\": \"303035303036\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\" ," +
                            "\"recipient\": " + "\"" + Conversion.asciiToHex(stuff.getStudentID().getStudentID()) + "\"}";

                    pahoMqttClient = new PahoMqttClient();
                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,command,"MY/TARUC/SSS/000000001/PUB");
                    getTopic(command);
                }
            });

            viewProfile.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    countFollowersCommand = "{\"command\": \"303035303038\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"subscribeeID\": " + "\"" + Conversion.asciiToHex(stuff.getStudentID().getStudentID()) + "\"}";

                    pahoMqttClient = new PahoMqttClient();
                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,countFollowersCommand,"MY/TARUC/SSS/000000001/PUB");
                    countFollowingMethod();
                }
            });
        }

        return view;
    }

    public void getTotalLove(String msg){

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                jsonObj = new JSONObject(msg);

                if(jsonObj.getString("command").equals("303035303032")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getTotalLove.php?studentID="
                            + Conversion.hexToAscii(jsonObj.getString("studentID")) + "&stuffID=" + Conversion.hexToAscii(jsonObj.getString("stuffID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject totalLoveResponse = (JSONObject) response.get(i);
                                            loveCount = totalLoveResponse.getString("LoveCount");
                                        }
                                        noTotalFavourites.setText(loveCount);
                                        if (pDialog.isShowing())
                                            pDialog.dismiss();
                                    } catch (Exception e) {
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    if (pDialog.isShowing())
                                        pDialog.dismiss();
                                }
                            });
                    queue.add(jsonObjectRequest);
                }

            } else {
                Toast.makeText(getActivity().getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


    public void checkFavourite(String msg) {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                jsonObj = new JSONObject(msg);

                if(jsonObj.getString("command").equals("303035303033")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "checkFavourite.php?studentID="
                            + Conversion.hexToAscii(jsonObj.getString("studentID")) + "&stuffID=" + Conversion.hexToAscii(jsonObj.getString("stuffID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject favouriteCountResponse = (JSONObject) response.get(i);
                                            favouriteCount = favouriteCountResponse.getString("FavouriteCount");

                                        }
                                        if (favouriteCount.equals("1")) {
                                            favourite.setImageResource(R.mipmap.icon_favourite);
                                        }

                                        if (pDialog.isShowing())
                                            pDialog.dismiss();

                                    } catch (Exception e) {
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    if (pDialog.isShowing())
                                        pDialog.dismiss();
                                }
                            });
                    queue.add(jsonObjectRequest);
                }


            } else {
                Toast.makeText(getActivity().getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


    public void getTopic(String msg) {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                jsonObj = new JSONObject(msg);

                if(jsonObj.getString("command").equals("303035303036")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getTopic.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID"))
                            + "&recipient=" + Conversion.hexToAscii(jsonObj.getString("recipient")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject topicResponse = (JSONObject) response.get(i);
                                            confirmedTopic = topicResponse.getString("ChatGroupID");
                                        }
                                        if (pDialog.isShowing())
                                            pDialog.dismiss();

                                        command = "{\"command\": \"303035303037\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                                "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\" ," +
                                                "\"recipient\": " + "\"" + Conversion.asciiToHex(stuff.getStudentID().getStudentID()) + "\"}";

                                        pahoMqttClient = new PahoMqttClient();
                                        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,command,"MY/TARUC/SSS/000000001/PUB");

                                        getPreviousPrivateChat(command);

                                    } catch (Exception e) {

                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    if (pDialog.isShowing())
                                        pDialog.dismiss();
                                }
                            });
                    queue.add(jsonObjectRequest);
                }


            } else {
                Toast.makeText(getActivity(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


    public void getPreviousPrivateChat(String msg) {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                jsonObj = new JSONObject(msg);

                if (jsonObj.getString("command").equals("303035303037")) {
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getPreviousPrivateChat.php?studentID="
                            + Conversion.hexToAscii(jsonObj.getString("studentID")) + "&recipient=" + Conversion.hexToAscii(jsonObj.getString("recipient")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        arrayPrivateChat.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject privateChatResponse = (JSONObject) response.get(i);
                                            arrayPrivateChat.add(new PrivateChat(
                                                    privateChatResponse.getString("studentID"), privateChatResponse.getString("studentName"),
                                                    privateChatResponse.getString("recipient"), privateChatResponse.getString("message"),
                                                    privateChatResponse.getString("postDate"), privateChatResponse.getString("postTime")));
                                        }
                                        if (pDialog.isShowing())
                                            pDialog.dismiss();

//                                        PrivateChatRoom frag = new PrivateChatRoom();
//                                        Bundle bundles = new Bundle();
//                                        bundles.putSerializable("Recipient", stuff.getStudentID());
//                                        bundles.putSerializable("Me",s);
//                                        frag.setArguments(bundles);
//                                        fragmentManager = getFragmentManager();
//                                        fragmentManager.beginTransaction()
//                                                .replace(R.id.fragmentHolder, frag)
//                                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                                                .commit();

                                        //new code by lee thian xin
                                        if (stuff.getStudentID().equals(s.getStudentID())){
                                            Toast.makeText(getActivity(),"This is your profile", Toast.LENGTH_LONG).show();
                                        }else {
                                            ChatRoom_V2 frag = new ChatRoom_V2();
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("UserData", s);//own data
                                            bundle.putString("ClickedUserID", stuff.getStudentID().getStudentID());
                                            frag.setArguments(bundle);
                                            fragmentManager = getFragmentManager();
                                            fragmentManager.beginTransaction()
                                                    .replace(R.id.update_fragmentHolder, frag)
                                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                                    .addToBackStack(null)
                                                    .commit();
                                        }

                                    } catch (Exception e) {

                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    if (pDialog.isShowing())
                                        pDialog.dismiss();
                                }
                            });
                    queue.add(jsonObjectRequest);
                }

            } else {
                Toast.makeText(getActivity(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


    public void setFavourite(String status) {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                if (status.equals("favourite")) {
                    command = "{\"command\": \"303035303034\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"stuffID\": " + "\"" + Conversion.asciiToHex(stuff.getStuffID()) + "\" ," +
                            "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";
                    getFavouriteID(getActivity(), Constant.serverFile + "getFavouriteID.php", command);
                } else {
                    command = "{\"command\": \"303035303035\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\" ," +
                            "\"stuffID\": " + "\"" + Conversion.asciiToHex(stuff.getStuffID()) + "\"}";
                    removeFavourite(command);
                }

                pahoMqttClient = new PahoMqttClient();
                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,command,"MY/TARUC/SSS/000000001/PUB");

            } else {
                Toast.makeText(getActivity().getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


    public void getFavouriteID(Context context, String url, String msg) {
        message = msg;
        RequestQueue queue = Volley.newRequestQueue(context);
        if (!pDialog.isShowing())
            pDialog.setMessage("Sync with server...");
        pDialog.show();

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject favouriteIDResponse = (JSONObject) response.get(i);
                                currentFavouriteID = favouriteIDResponse.getString("CurrentFavouriteID");
                            }
                            if (currentFavouriteID.equals("0")) {
                                favouriteID = "FAV0001";
                            } else {
                                favouriteID = String.format("FAV%04d", (Integer.parseInt(currentFavouriteID.substring(3, 7)) + 1));
                            }
                            insertFavouriteData(message);
                            if (pDialog.isShowing())
                                pDialog.dismiss();
                        } catch (Exception e) {

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        if (pDialog.isShowing())
                            pDialog.dismiss();
                    }
                });
        queue.add(jsonObjectRequest);
    }


    public void insertFavouriteData(String msg) {

        try {
            jsonObj = new JSONObject(msg);
            d = Calendar.getInstance().getTime();
            sdf = new SimpleDateFormat("dd/MM/yyyy");
            favouriteDate = sdf.format(d);

            if(jsonObj.getString("command").equals("303035303034")){
                jsonURL = Constant.serverFile + "insertFavouriteData.php?favouriteID=" + favouriteID + "&stuffID=" + Conversion.hexToAscii(jsonObj.getString("stuffID"))
                        + "&studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID")) + "&favouriteDate=" + favouriteDate + "&unfavouriteDate=" + ""
                        + "&favouriteStatus=" + "Active";

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                try {
                    StringRequest postRequest = new StringRequest(
                            Request.Method.POST,
                            jsonURL,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if (isAdded()) {
                                    }
                                    JSONObject jsonObject = null;
                                    try {
                                        jsonObject = new JSONObject(response);
                                        String success = jsonObject.getString("success");
                                        if (success.equals("1")) {
                                            Toast.makeText(getActivity().getApplicationContext(), "Added to favourite list.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getActivity().getApplicationContext(), "Error.", Toast.LENGTH_LONG).show();
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (isAdded()) {
                                    }

                                }
                            }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            try {
                                params.put("favouriteID", favouriteID);
                                params.put("stuffID", Conversion.hexToAscii(jsonObj.getString("stuffID")));
                                params.put("studentID", Conversion.hexToAscii(jsonObj.getString("studentID")));
                                params.put("favouriteDate", favouriteDate);
                                params.put("unfavouriteDate", "");
                                params.put("favouriteStatus", "Active");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("Content-Type", "application/x-www-form-urlencoded");
                            return params;
                        }
                    };
                    queue.add(postRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
            if (isAdded()) {

            }
        }
    }


    public void removeFavourite(String msg) {

        try {
            jsonObj = new JSONObject(msg);
            if(jsonObj.getString("command").equals("303035303035")){
                String url = Constant.serverFile + "removeFavourite.php?stuffID=" + Conversion.hexToAscii(jsonObj.getString("stuffID"))
                        + "&studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID")) ;
                RequestQueue queue = Volley.newRequestQueue(getActivity());
                try {
                    StringRequest postRequest = new StringRequest(
                            Request.Method.POST,
                            url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if (isAdded()) {
                                    }
                                    JSONObject jsonObject = null;
                                    try {
                                        jsonObject = new JSONObject(response);
                                        String success = jsonObject.getString("updated");
                                        if (success.equals("1")) {
                                            Toast.makeText(getActivity().getApplicationContext(), "Removed from favourite list.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getActivity().getApplicationContext(), "Error.", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (isAdded()) {
                                    }

                                }
                            }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            try {
                                params.put("stuffID", Conversion.hexToAscii(jsonObj.getString("stuffID")));
                                params.put("studentID", Conversion.hexToAscii(jsonObj.getString("studentID")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("Content-Type", "application/x-www-form-urlencoded");
                            return params;
                        }
                    };
                    queue.add(postRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            if (isAdded()) {
            }

        }
    }


    public void countFollowingMethod(){

        countFollowingCommand = "{\"command\": \"303035303039\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"subscriberID\": " + "\"" + Conversion.asciiToHex(stuff.getStudentID().getStudentID()) + "\"}";

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,countFollowingCommand,"MY/TARUC/SSS/000000001/PUB");
        getRatingMethod();
    }


    public void getRatingMethod(){

        ratingCommand = "{\"command\": \"30303530300A\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"studentID\": " + "\"" + Conversion.asciiToHex(stuff.getStudentID().getStudentID()) + "\"}";

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,ratingCommand,"MY/TARUC/SSS/000000001/PUB");
        getSellerDetailsMethod();
    }


    public void getSellerDetailsMethod(){

        sellerStuffCommand = "{\"command\": \"30303530300B\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"studentID\": " + "\"" + Conversion.asciiToHex(stuff.getStudentID().getStudentID()) + "\"}";

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,sellerStuffCommand,"MY/TARUC/SSS/000000001/PUB");
        checkSubscribeMethod();
    }


    public void checkSubscribeMethod(){

        checkSubscribeCommand = "{\"command\": \"30303530300C\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"subscriberID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\" ," +
                "\"subscribeeID\": " + "\"" + Conversion.asciiToHex(stuff.getStudentID().getStudentID()) + "\"}";

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,checkSubscribeCommand,"MY/TARUC/SSS/000000001/PUB");

        SellerProfile frag = new SellerProfile();
        Bundle bundles = new Bundle();
        bundles.putSerializable("ViewProfile", stuff);
        bundles.putSerializable("Me",s);
        bundles.putSerializable("Followers", countFollowersCommand);
        bundles.putSerializable("Following", countFollowingCommand);
        bundles.putSerializable("Rating", ratingCommand);
        bundles.putSerializable("SellerStuff",sellerStuffCommand);
        bundles.putSerializable("CheckSubscribe",checkSubscribeCommand);
        frag.setArguments(bundles);
        fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.update_fragmentHolder, frag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        // Log.d(TAG,"Welcome back");
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

    }


}

package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.ActivityLikeOB;
import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.Student;
import Helper.Stuff;
import Helper.UserActivityOB;

public class ActivityRecyclerViewAdapter extends RecyclerView.Adapter<ActivityRecyclerViewAdapter.ActivityRecyclerViewHolder> {
    private OnItemClickListener mListener;
    private ArrayList<UserActivityOB> mData;

    private Context mContext;
    private Student s;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;

    private List<ActivityLikeOB> likeStatus;
    private List<Stuff> arrayMyStuff = new ArrayList<>();
    private String likeRespond = "";
    FragmentManager fragmentManager;
    private static final String TAG = "User Activity";
    private ProgressDialog pDialog = null;
    private String UserID, insertNewLikeActivityUrl = "";
    private String command;

    public ActivityRecyclerViewAdapter(ArrayList<UserActivityOB> mData, Context mContext, MqttAndroidClient mqttAndroidClient, PahoMqttClient pahoMqttClient) {
        this.mData = mData;
        this.mContext = mContext;
        this.mqttAndroidClient = mqttAndroidClient;
        this.pahoMqttClient = pahoMqttClient;
    }

    public interface OnItemClickListener {
        void onCommentClick(int position);
        void onBookmarkClick(int position);
        void onShareClick(int position);
        void onViewStuffClick(int position);
        void onViewPosterClick(int position);
    }

    public void setOnItemClickListener(ActivityRecyclerViewAdapter.OnItemClickListener listener){
        this.mListener = listener;
    }

    public static class ActivityRecyclerViewHolder extends RecyclerView.ViewHolder{
        public ImageView posterPhoto;
        public TextView posterName;
        public TextView posterDate;
        public TextView btnviewStuff;
        public TextView posterCaption;
        public ImageView posterThumbnail;
        public ImageView likeButton;
        public ImageView commentButton;
        public TextView likeCondition;
        public ImageView bookmarkButton;
        public TextView likeAmount;
        public TextView commentAmount;
        public ImageView shareButton;
        public ProgressBar userActProgressbar;
        public RelativeLayout activityHolder;

        public ActivityRecyclerViewHolder(View itemView,final OnItemClickListener listener) {
            super(itemView);

            posterPhoto = itemView.findViewById(R.id.posterPhoto);
            posterName = itemView.findViewById(R.id.posterName);
            posterDate = itemView.findViewById(R.id.posterDate);
            btnviewStuff = itemView.findViewById(R.id.btnviewStuff);
            posterCaption = itemView.findViewById(R.id.posterCaption);
            posterThumbnail = itemView.findViewById(R.id.posterThumbnail);
            likeButton = itemView.findViewById(R.id.likeButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            likeCondition = itemView.findViewById(R.id.likeConditon);
            bookmarkButton = itemView.findViewById(R.id.bookmarkButton);
            likeAmount = itemView.findViewById(R.id.likeAmount);
            commentAmount = itemView.findViewById(R.id.commentAmount);
            shareButton = itemView.findViewById(R.id.shareButton);
            userActProgressbar = itemView.findViewById(R.id.userActProgressbar);
            activityHolder = itemView.findViewById(R.id.activityHolder);

            posterPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onViewPosterClick(position);
                        }
                    }
                }
            });

            btnviewStuff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onViewStuffClick(position);
                        }
                    }
                }
            });

            commentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onCommentClick(position);
                        }
                    }
                }
            });

            bookmarkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onBookmarkClick(position);
                        }
                    }
                }
            });

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onShareClick(position);
                        }
                    }
                }
            });
        }
    }

    public void changeLikeButton(){
        notifyDataSetChanged();
    }

    @Override
    public ActivityRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_activity_cardview, parent, false);
        ActivityRecyclerViewHolder mvh = new ActivityRecyclerViewHolder(v, mListener);
        return mvh;
    }

    @Override
    public void onBindViewHolder(final ActivityRecyclerViewHolder holder, final int position) {
        UserActivityOB userActivity = mData.get(position);

        UserID = UserSharedPreferences.read(UserSharedPreferences.userID, null);

//        Picasso.with(mContext).load(mData.get(position).getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(holder.posterPhoto);
        Picasso.with(mContext).load(userActivity.getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(holder.posterPhoto);

        holder.posterName.setText(userActivity.getStudentID().getStudentName());
        String date = userActivity.getUploadDate() + " on " + userActivity.getUploadTime();
        holder.posterDate.setText(date);
        holder.posterCaption.setText(userActivity.getActivityCaption());
        if (userActivity.getActivityThumbnail().equals("")) {
            holder.posterThumbnail.setVisibility(View.GONE);
        } else {
            Picasso.with(mContext).load(userActivity.getActivityThumbnail()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(holder.posterThumbnail);
        }
        holder.likeCondition.setText("didnt like");
        checkLike(holder, userActivity.getActivityID(), UserID);

        if (userActivity.getStuffID().equals("")) {
            holder.btnviewStuff.setVisibility(View.GONE);
        }

        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.likeCondition.getText().toString().equals("didnt like")) {
                    getLikeID(UserID, position, holder.likeCondition.getText().toString());
                    holder.likeButton.setImageResource(R.drawable.ic_liked);
                    holder.likeCondition.setText("Liked");
                } else if (holder.likeCondition.getText().equals("Liked")) {
                    getLikeID(UserID, position, holder.likeCondition.getText().toString());
                    holder.likeButton.setImageResource(R.drawable.ic_like);
                    holder.likeCondition.setText("didnt like");
                }
            }
        });

        holder.userActProgressbar.setVisibility(View.GONE);
        holder.activityHolder.setVisibility(View.VISIBLE);
    }

    private void getLikeID(String UserID, final int position, String likeCondition) {
        likeStatus = new ArrayList<>();
        pDialog = new ProgressDialog(mContext);
        try {
            ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                try {
                    command = "{\"command\": \"30303530304C\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"StudentID\": " + "\"" + Conversion.asciiToHex(UserID) + "\" ," +
                            "\"ActivityID\": " + "\"" + Conversion.asciiToHex(mData.get(position).getActivityID()) + "\" }";
                    pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
                } catch (MqttException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


                RequestQueue queue = Volley.newRequestQueue(mContext);
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();
                //http://192.168.0.108/raindown/getLikeID.php?studentID=17Wmr05969&activityID=act1003

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getLikeID.php?studentID=" + UserID + "&activityID=" +
                        mData.get(position).getActivityID(),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    String status = "";
                                    String likeID = "";
                                    Log.d(TAG, "Respond mOU");
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject likeResponse = (JSONObject) response.get(i);
                                        likeID = likeResponse.getString("likeID");
                                        status = likeResponse.getString("likeStatus");
                                    }
                                    Log.d(TAG, "Like ID =" + likeID);
                                    Log.d(TAG, "Like Status =" + status);

                                    if (response.length() == 0) {
                                        getLastLikeID(mData.get(position).getActivityID());
                                    } else if (response.length() > 0) {
                                        updateLikeID(status, likeID);
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

            } else {
                Toast.makeText(mContext.getApplicationContext(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(mContext.getApplicationContext(),
                    "Error create activity:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void updateLikeID(String status, final String likeID) {
        Log.d(TAG, "like ID status =" + status);
        String ChangeLikeStatus = "";
        if (status.equals("ACTIVE")) {
            ChangeLikeStatus = "DEACTIVE";
        } else if (status.equals("DEACTIVE")) {
            ChangeLikeStatus = "ACTIVE";
        }
        String updateLikeActivityUrl = Constant.serverFile + "updateLikeActivity.php?likeID=" + likeID +
                "&status=" + ChangeLikeStatus;
        //http://192.168.0.108/raindown/updateLikeActivity.php?likeID=like1003&status=ACTIVE
        Log.d(TAG, "Update Like URL = " + updateLikeActivityUrl);

        RequestQueue queue = Volley.newRequestQueue(mContext);
        try {
            StringRequest postRequest = new StringRequest(
                    Request.Method.POST,
                    updateLikeActivityUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObject = null;
                            Log.d(TAG, "insert like Respond is here");
                            try {
                                jsonObject = new JSONObject(response);
                                String message = jsonObject.getString("message");
                                Toast.makeText(mContext.getApplicationContext(), message,
                                        Toast.LENGTH_LONG).show();


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

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

    private void getLastLikeID(final String activityID) {
        RequestQueue queue = Volley.newRequestQueue(mContext);

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getLastLikeID.php",
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            String currentLikeID = "";
                            String newLikeID = "";
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject likeIDResponse = (JSONObject) response.get(i);
                                currentLikeID = likeIDResponse.getString("CurrentLikeID");
                            }
                            Log.d(TAG, "Current Like ID =" + currentLikeID);
                            if (currentLikeID.equals("0")) {
                                newLikeID = "like1001";
                            } else {
                                String first = currentLikeID.substring(0, 4);
                                String last = currentLikeID.substring(4);
                                int number = Integer.parseInt(last) + 1;
                                newLikeID = first + Integer.toString(number);
                            }

                            Log.d(TAG, "New Like ID = " + newLikeID);

                            insertNewLikeActivity(newLikeID, activityID);


                        } catch (Exception e) {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        queue.add(jsonObjectRequest);
    }

    private void insertNewLikeActivity(final String newID, final String activityID) {
        UserID = UserSharedPreferences.read(UserSharedPreferences.userID, null);
        //192.168.0.108/raindown/insertLikeActivity.php?likeID=like1003&studentID=17WMR05969&activityID=act1003
        insertNewLikeActivityUrl = Constant.serverFile + "insertLikeActivity.php?likeID=" + newID
                + "&studentID=" + UserID + "&activityID=" + activityID;

        Log.d(TAG, "Item to be insert are like id =" + newID + ", studentID =" + UserID + ", activity ID =" + activityID);

        RequestQueue queue = Volley.newRequestQueue(mContext);
        try {
            StringRequest postRequest = new StringRequest(
                    Request.Method.POST,
                    insertNewLikeActivityUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObject = null;
                            Log.d(TAG, "insert like Respond is here");
                            try {
                                jsonObject = new JSONObject(response);
                                String success = jsonObject.getString("success");
                                if (success.equals("1")) {
                                    Toast.makeText(mContext.getApplicationContext(), "Activity Liked",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(mContext.getApplicationContext(), "Oops there is a problem occur...",
                                            Toast.LENGTH_LONG).show();
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("likeID", newID);
                    params.put("studentID", UserID);
                    params.put("activityID", activityID);

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


    private void checkLike(final ActivityRecyclerViewHolder holder, String actID, String UserID) {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        //http://192.168.0.108/raindown/getActivityLikeList.php?studentID=17wmr05969&activityID=act1002

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getActivityLikeList.php?studentID=" + UserID + "&activityID=" + actID,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if (response.length() >= 1) {
                            holder.likeButton.setImageResource(R.drawable.ic_liked);
                            holder.likeCondition.setText("Liked");

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d("Testing respond", "Not Responding");
                    }
                });
        queue.add(jsonObjectRequest);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}

package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.app.AlertDialog;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.AvailableTimeOB;
import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;

public class AvailableTimeAdapter extends RecyclerView.Adapter<AvailableTimeAdapter.MyviewHolder> {

    private Context mContext;
    private List<AvailableTimeOB> mData;
    private MqttAndroidClient mqttAndroidClient;

    private SimpleDateFormat df = new SimpleDateFormat("hh:mma");
    private DateFormat dateFormat = new SimpleDateFormat("HHmm");
    private Date d;
    private String command;
    private JSONObject jsonObj;
    private PahoMqttClient pahoMqttClient = new PahoMqttClient();
    private String updateAvailableTimeURL = "", deleteAvailableTimeURL = "";
    private final static String TAG = "avaiableTime adapter";
    private String encodedAvailableStatus;
    private FragmentManager fragmentManager;

    public AvailableTimeAdapter(Context mContext, List<AvailableTimeOB> mData, MqttAndroidClient mqttAndroidClient) {
        this.mContext = mContext;
        this.mData = mData;
        this.mqttAndroidClient = mqttAndroidClient;

    }

    @Override
    public MyviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater mInfrater = LayoutInflater.from(mContext);
        view = mInfrater.inflate(R.layout.availabletime_cardview, parent, false);
        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyviewHolder holder, final int position) {
        holder.availableDate.setText(mData.get(position).getAvailableDate());


        try {
            d = dateFormat.parse(mData.get(position).getStartTime());

            holder.startTime.setText(df.format(d));

            d = dateFormat.parse(mData.get(position).getEndTime());
            holder.endTime.setText(df.format(d));


        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (mData.get(position).getAvailableStatus().equals("ACTIVE")) {
            holder.availableStatus.setText("Active");
            holder.availableStatus.setTextColor(mContext.getResources().getColor(R.color.lightgreen));
        } else if (mData.get(position).getAvailableStatus().equals("DEACTIVE")) {
            holder.availableStatus.setText("Deactive");
            holder.availableStatus.setTextColor(mContext.getResources().getColor(R.color.red));
        }

        holder.availableStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAvailableStatus(position, holder);
            }
        });

        holder.deleteAvailableTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:

                                deleteAvailableTime(position);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Delete Available Time ?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        holder.editAvailableTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                UpdateAvailableTime updateAvailableTime = new UpdateAvailableTime();
                Bundle bundle = new Bundle();
                bundle.putSerializable("EditAvailableTime", mData.get(position));
                updateAvailableTime.setArguments(bundle);
                fragmentManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, updateAvailableTime)
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        });
    }

    private void deleteAvailableTime(final int position) {
        try {

            command = "{\"command\": \"30303530305C\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"availableID\": " + "\"" + Conversion.asciiToHex(mData.get(position).getAvailableID()) + "\" ," +
                    "\"recordStatus\": " + "\"" + Conversion.asciiToHex("DELETED") + "\" }";

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

            deleteAvailableTimeURL = Constant.serverFile + "deleteAvailableTime.php?availableID=" + mData.get(position).getAvailableID()
                    + "&recordStatus=" + "DELETED";


            RequestQueue queue = Volley.newRequestQueue(mContext);
            try {
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST,
                        deleteAvailableTimeURL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(response);
                                    String success = jsonObject.getString("success");
                                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                    if (success.equals("1")) {
                                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        break;
                                                }
                                            }
                                        };

                                        builder.setTitle("Available Time is successfully deleted")
                                                .setMessage("Available Time is deleted.")
                                                .setPositiveButton("OK", dialogClickListener).show();
                                    } else {
                                        builder.setTitle("Available Time is failed to delete");
                                        builder.setMessage("Available Time is not delete.");
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                        builder.show();
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
//                        params.put("availableID", mData.get(position).getAvailableID());
//                        params.put("recordStatus", "DELETED");

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


        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    private void updateAvailableStatus(final int position, final MyviewHolder holder) {
        try {
            if (holder.availableStatus.getText().toString().trim().equals("Active")) {
                command = "{\"command\": \"30303530305B\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"availableID\": " + "\"" + Conversion.asciiToHex(mData.get(position).getAvailableID()) + "\" ," +
                        "\"availableStatus\": " + "\"" + Conversion.asciiToHex("DEACTIVE") + "\" }";
                holder.availableStatus.setText("Deactive");
                holder.availableStatus.setTextColor(mContext.getResources().getColor(R.color.red));


            } else if (holder.availableStatus.getText().toString().trim().equals("Deactive")) {
                command = "{\"command\": \"30303530305A\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"availableID\": " + "\"" + Conversion.asciiToHex(mData.get(position).getAvailableID()) + "\" ," +
                        "\"availableStatus\": " + "\"" + Conversion.asciiToHex("ACTIVE") + "\" }";
                holder.availableStatus.setText("Active");
                holder.availableStatus.setTextColor(mContext.getResources().getColor(R.color.lightgreen));

            }

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

            encodedAvailableStatus = "";
            jsonObj = new JSONObject(command);
            final String[] encodeAvailableStatus = {Conversion.hexToAscii(jsonObj.getString("availableStatus"))};
            for (String s : encodeAvailableStatus) {
                encodedAvailableStatus += URLEncoder.encode(s, "UTF-8");
            }

            updateAvailableTimeURL = Constant.serverFile + "updateAvailableTime.php?availableID=" + mData.get(position).getAvailableID()
                    + "&availableStatus=" + encodedAvailableStatus;


            Log.d(TAG, "Update Store URL = " + updateAvailableTimeURL);

            RequestQueue queue = Volley.newRequestQueue(mContext);
            try {
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST,
                        updateAvailableTimeURL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(response);
                                    String success = jsonObject.getString("success");
                                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                    if (success.equals("1")) {
                                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        break;
                                                }
                                            }
                                        };

                                        builder.setTitle("Available Time is successfully updated")
                                                .setMessage("Available Time is updated.")
                                                .setPositiveButton("OK", dialogClickListener).show();
                                    } else {
                                        builder.setTitle("Available Time is failed to update");
                                        builder.setMessage("Available Time is not updated.");
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                        builder.show();
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
                        params.put("availableID", mData.get(position).getAvailableID());
                        params.put("availableStatus", encodedAvailableStatus);

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


        } catch (Exception e) {
            e.printStackTrace();

        }

    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyviewHolder extends RecyclerView.ViewHolder {

        CardView availableTimeCardView;
        TextView availableDate, startTime, endTime, availableStatus;
        ImageView editAvailableTime, deleteAvailableTime;

        public MyviewHolder(View itemView) {
            super(itemView);
            availableTimeCardView = itemView.findViewById(R.id.availableTimeCardView);
            availableDate = itemView.findViewById(R.id.availableDate);
            startTime = itemView.findViewById(R.id.availableStartTime);
            endTime = itemView.findViewById(R.id.availableEndTime);
            availableStatus = itemView.findViewById(R.id.availableStatus);
            editAvailableTime = itemView.findViewById(R.id.editAvailableTime);
            deleteAvailableTime = itemView.findViewById(R.id.deleteAvailableTime);
        }
    }
}

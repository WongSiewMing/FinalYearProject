package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
   Programme : RSD3
   Year : 2018*/

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.PrivateChat;
import Helper.PrivateChatOB;

public class ChatRoom_V2_Adapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private static final String TAG = "ChatRoomAdapter";
    private String command;
    private PahoMqttClient pahoMqttClient;
    private String jsonURL;

    private Context mContext;
    private List<PrivateChatOB> mData;
    private String photo;
    private MqttAndroidClient mqttAndroidClient;

    public ChatRoom_V2_Adapter(Context mContext, List<PrivateChatOB> mData, String photo, MqttAndroidClient mqttAndroidClient) {
        this.mContext = mContext;
        this.mData = mData;
        this.photo = photo;
        this.mqttAndroidClient = mqttAndroidClient;
    }

    @Override
    public int getItemViewType(int position) {
        String UserID = UserSharedPreferences.read(UserSharedPreferences.userID, null);
        String messageUserID = mData.get(position).getStudentID();
        if (UserID.equals(messageUserID)){
            return VIEW_TYPE_MESSAGE_SENT;
        }else{
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_message_cardview, parent, false);
            return new SentMessageHolder(view);
        }else  if (viewType == VIEW_TYPE_MESSAGE_RECEIVED){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.received_message_cardview, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PrivateChatOB message = mData.get(position);

        switch (holder.getItemViewType()){
            case VIEW_TYPE_MESSAGE_SENT :
                ((SentMessageHolder) holder).bind(message);
                break;
            case  VIEW_TYPE_MESSAGE_RECEIVED :
                ((ReceivedMessageHolder) holder).bind(message);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private class SentMessageHolder extends  RecyclerView.ViewHolder {

        CardView userMsgCardView;
        TextView senderMessage;
        TextView sendTime;

        public SentMessageHolder(View itemView) {
            super(itemView);
            userMsgCardView = itemView.findViewById(R.id.userMessage_Cardview);
            senderMessage = itemView.findViewById(R.id.sender_message_text);
            sendTime = itemView.findViewById(R.id.sender_time);
        }

        void bind(final PrivateChatOB message){
            senderMessage.setText(message.getMessage());
            sendTime.setText(message.getPostTime());
            userMsgCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d(TAG, "LONG pressed detected");
                    if (senderMessage.getText().toString().equals("Message unsended")){

                    }else {
                        final CharSequence[] options = {"Unsend Message", "Cancel"};
                        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Choose your action");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (options[which] == "Unsend Message"){
                                    UnsendMsg(message, senderMessage);

                                }else if (options[which] == "Cancel"){
                                    dialog.dismiss();
                                }

                            }
                        });
                        builder.show();
                    }


                    return true;
                }
            });

        }
    }

    private void UnsendMsg(final PrivateChatOB msg, final TextView senderMsg) {
        try {

            command = "{\"command\": \"303035303056\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"privateID\": " + "\"" + Conversion.asciiToHex(msg.getPriChatID()) + "\" ," +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(msg.getStudentID()) + "\" ," +
                    "\"recipient\": " + "\"" + Conversion.asciiToHex(msg.getRecipient()) + "\" ," +
                    "\"message\": " + "\"" + Conversion.asciiToHex(msg.getMessage()) + "\" ," +
                    "\"image\": " + "\"" + Conversion.asciiToHex(msg.getImage()) + "\" ," +
                    "\"postDate\": " + "\"" + Conversion.asciiToHex(msg.getPostDate()) + "\" ," +
                    "\"postTime\": " + "\"" + Conversion.asciiToHex(msg.getPostTime()) + "\" }";
            Log.d(TAG,"student ID =" + msg.getPriChatID());
            Log.d(TAG,"student ID =" + msg.getStudentID());
            Log.d(TAG,"recipient =" + msg.getRecipient());
            Log.d(TAG,"message =" + msg.getMessage());
            Log.d(TAG,"postDate =" + msg.getPostDate());
            Log.d(TAG,"postTime =" + msg.getPostTime());
            pahoMqttClient = new PahoMqttClient();

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
            //http://192.168.0.108/raindown/removePrivateChatMessage.php?privateID=PRI1001


            jsonURL = Constant.serverFile + "removePrivateChatMessage.php?privateID=" + msg.getPriChatID();

            Log.d(TAG,"Delete msg URL = " + jsonURL);

            RequestQueue queue = Volley.newRequestQueue(mContext);
            try {
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST,
                        jsonURL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(response);
                                    String ID = jsonObject.getString("ID");
                                    if(ID.equals("0")){
                                        senderMsg.setText("Message unsended");
                                        senderMsg.setTextColor(mContext.getResources().getColor(R.color.red));
                                        Toast.makeText(mContext.getApplicationContext(), "Message recalled",
                                                Toast.LENGTH_LONG).show();
                                    }else {
                                        Toast.makeText(mContext.getApplicationContext(), "Failed to recall message",
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
                        params.put("privateID", msg.getPriChatID());

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

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {

        ImageView opponentPhoto;
        TextView opponentName, opponentMessage,opponentMsgReceivedTime;

        public ReceivedMessageHolder(View itemView) {
            super(itemView);
            opponentPhoto = itemView.findViewById(R.id.Opponent_photo);
            opponentName = itemView.findViewById(R.id.Opponent_name);
            opponentMessage = itemView.findViewById(R.id.Opponent_message);
            opponentMsgReceivedTime = itemView.findViewById(R.id.Opponent_msg_received_time);
        }

        void bind(PrivateChatOB message){
            Picasso.with(mContext).load(photo).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(opponentPhoto);
            opponentName.setText(message.getStudentName());
            opponentMessage.setText(message.getMessage());
            opponentMsgReceivedTime.setText(message.getPostTime());
        }
    }
}

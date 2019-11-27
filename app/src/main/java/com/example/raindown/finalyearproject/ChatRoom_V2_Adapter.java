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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.PrivateChatOB;

public class ChatRoom_V2_Adapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_IMAGE_SENT = 3;
    private static final int VIEW_TYPE_IMAGE_RECEIVED = 4;

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
        String messageSent = mData.get(position).getMessage();

        if (UserID.equals(messageUserID) && !messageSent.equals("[Image]") && !messageSent.equals("[Temporary Image]")){
            return VIEW_TYPE_MESSAGE_SENT;
        }else if (!UserID.equals(messageUserID) && !messageSent.equals("[Image]") && !messageSent.equals("[Temporary Image]")){
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }else if (UserID.equals(messageUserID) && messageSent.equals("[Image]")){
            return VIEW_TYPE_IMAGE_SENT;
        } else if (!UserID.equals(messageUserID) && messageSent.equals("[Image]")) {
            return VIEW_TYPE_IMAGE_RECEIVED;
        }

        return 0;
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
        } else if (viewType == VIEW_TYPE_IMAGE_SENT){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_sender_image_cardview, parent, false);
            return  new SentImageHolder(view);
        } else if (viewType == VIEW_TYPE_IMAGE_RECEIVED){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_receive_image_cardview, parent, false);
            return new ReceivedImageHolder(view);
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
            case VIEW_TYPE_IMAGE_SENT :
                ((SentImageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_RECEIVED :
                ((ReceivedImageHolder) holder).bind(message);
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
                    if (senderMessage.getText().toString().equals("Message removed")){

                    }else {
                        final CharSequence[] options = {"Remove Message", "Cancel"};
                        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Choose your action");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (options[which] == "Remove Message"){
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

    private class SentImageHolder extends  RecyclerView.ViewHolder {

        CardView userMsgCardView;
        ImageView senderImage;
        TextView sendTime;

        public SentImageHolder(View itemView) {
            super(itemView);
            userMsgCardView = itemView.findViewById(R.id.senderImage_CardView);
            senderImage = itemView.findViewById(R.id.sender_image_view);
            sendTime = itemView.findViewById(R.id.sender_time_image);
        }

        void bind(final PrivateChatOB message){
            Picasso.with(mContext).load(message.getImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(senderImage);
            sendTime.setText(message.getPostTime());

            userMsgCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (senderImage.getDrawable().getConstantState() != mContext.getResources().getDrawable(R.drawable.ic_image_delete).getConstantState()){
                        final CharSequence[] options = {"Remove Message", "Cancel"};
                        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Choose your action");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (options[which] == "Remove Message"){
                                    removeImageMsg(message, senderImage);

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
            pahoMqttClient = new PahoMqttClient();

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

            jsonURL = Constant.serverFile + "removePrivateChatMessage.php?privateID=" + msg.getPriChatID() + "&hiddenUserID=" + UserSharedPreferences.read(UserSharedPreferences.userID, null);

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
                                        senderMsg.setText("Message removed");
                                        senderMsg.setTextColor(mContext.getResources().getColor(R.color.white));
                                        Toast.makeText(mContext.getApplicationContext(), "Message removed",
                                                Toast.LENGTH_LONG).show();
                                    }else {
                                        Toast.makeText(mContext.getApplicationContext(), "Failed to remove message",
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
                        params.put("hiddenUserID", UserSharedPreferences.read(UserSharedPreferences.userID, null));
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

    private void removeImageMsg(final PrivateChatOB msg, final ImageView imgMsg) {
        try {

            command = "{\"command\": \"303035303083\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"privateID\": " + "\"" + Conversion.asciiToHex(msg.getPriChatID()) + "\" ," +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(msg.getStudentID()) + "\" ," +
                    "\"recipient\": " + "\"" + Conversion.asciiToHex(msg.getRecipient()) + "\" ," +
                    "\"message\": " + "\"" + Conversion.asciiToHex(msg.getMessage()) + "\" ," +
                    "\"image\": " + "\"" + Conversion.asciiToHex(msg.getImage()) + "\" ," +
                    "\"postDate\": " + "\"" + Conversion.asciiToHex(msg.getPostDate()) + "\" ," +
                    "\"postTime\": " + "\"" + Conversion.asciiToHex(msg.getPostTime()) + "\" }";
            pahoMqttClient = new PahoMqttClient();

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

            jsonURL = Constant.serverFile + "removePrivateChatMessage.php?privateID=" + msg.getPriChatID() + "&hiddenUserID=" + UserSharedPreferences.read(UserSharedPreferences.userID, null);

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
                                        imgMsg.setImageResource(R.drawable.ic_image_delete);
                                        Toast.makeText(mContext.getApplicationContext(), "Message removed",
                                                Toast.LENGTH_LONG).show();
                                    }else {
                                        Toast.makeText(mContext.getApplicationContext(), "Failed to remove message",
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
                        params.put("hiddenUserID", UserSharedPreferences.read(UserSharedPreferences.userID, null));
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

        CardView receivedMsgCardView;
        ImageView opponentPhoto;
        TextView opponentName, opponentMessage,opponentMsgReceivedTime;

        public ReceivedMessageHolder(View itemView) {
            super(itemView);
            opponentPhoto = itemView.findViewById(R.id.Opponent_photo);
            opponentName = itemView.findViewById(R.id.Opponent_name);
            opponentMessage = itemView.findViewById(R.id.Opponent_message);
            opponentMsgReceivedTime = itemView.findViewById(R.id.Opponent_msg_received_time);
            receivedMsgCardView = itemView.findViewById(R.id.receivedMessage_CardView);
        }

        void bind(final PrivateChatOB message){
            Picasso.with(mContext).load(photo).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(opponentPhoto);
            opponentName.setText(message.getStudentName());
            opponentMessage.setText(message.getMessage());
            opponentMsgReceivedTime.setText(message.getPostTime());

            receivedMsgCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (opponentMessage.getText().toString().equals("Message removed")){

                    }else {
                        final CharSequence[] options = {"Remove Message", "Cancel"};
                        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Choose your action");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (options[which] == "Remove Message"){
                                    UnsendMsg(message, opponentMessage);

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

    private class ReceivedImageHolder extends RecyclerView.ViewHolder {

        CardView receivedImageCardView;
        ImageView opponentPhoto, opponentImage;
        TextView opponentName, opponentMsgReceivedTime;

        public ReceivedImageHolder(View itemView) {
            super(itemView);
            opponentPhoto = itemView.findViewById(R.id.Opponent_photo_image);
            opponentName = itemView.findViewById(R.id.Opponent_name_image);
            opponentImage = itemView.findViewById(R.id.received_image_view);
            opponentMsgReceivedTime = itemView.findViewById(R.id.Opponent_msg_received_time_image);
            receivedImageCardView = itemView.findViewById(R.id.receivedImage_CardView);
        }

        void bind(final PrivateChatOB message){
            Picasso.with(mContext).load(photo).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(opponentPhoto);
            opponentName.setText(message.getStudentName());
            Picasso.with(mContext).load(message.getImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(opponentImage);
            opponentMsgReceivedTime.setText(message.getPostTime());

            receivedImageCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (opponentImage.getDrawable().getConstantState() != mContext.getResources().getDrawable(R.drawable.ic_image_delete).getConstantState()){
                        final CharSequence[] options = {"Remove Message", "Cancel"};
                        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Choose your action");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (options[which] == "Remove Message"){
                                    removeImageMsg(message, opponentImage);

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

}

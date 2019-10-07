package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.util.List;

import Helper.Message;
import Helper.PahoMqttClient;
import Helper.PrivateChatOB;

public class PublicChatRoom_V2_Adapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private static final String TAG = "PublicChatRoomAdapter";
    private String command;
    private PahoMqttClient pahoMqttClient;
    private String jsonURL;

    private Context mContext;
    private List<Message> mData;
    private MqttAndroidClient mqttAndroidClient;

    public PublicChatRoom_V2_Adapter(Context mContext, List<Message> mData, MqttAndroidClient mqttAndroidClient) {
        this.mContext = mContext;
        this.mData = mData;
        this.mqttAndroidClient = mqttAndroidClient;
    }

    @Override
    public int getItemViewType(int position) {
        String UserID = UserSharedPreferences.read(UserSharedPreferences.userID, null);
        String messageUserID = mData.get(position).getStudentID().getStudentID();
        if (UserID.equals(messageUserID)){
            return VIEW_TYPE_MESSAGE_SENT;
        }else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_message_cardview, parent, false);
            return new SentMessageHolder(view);
        }else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.received_message_cardview, parent, false);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = mData.get(position);

        switch (holder.getItemViewType()){
            case VIEW_TYPE_MESSAGE_SENT :
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED :
                ((ReceivedMessageHolder) holder).bind(message);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder{
        CardView userMsgCardView;
        TextView senderMessage;
        TextView sendTime;

        public SentMessageHolder(View itemView) {
            super(itemView);
            userMsgCardView = itemView.findViewById(R.id.userMessage_Cardview);
            senderMessage = itemView.findViewById(R.id.sender_message_text);
            sendTime = itemView.findViewById(R.id.sender_time);
        }
        void bind(final Message message){
            senderMessage.setText(message.getMessage());
            sendTime.setText(message.getPostTime());

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

        void bind(Message message){



            Picasso.with(mContext).load(message.getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(opponentPhoto);
            opponentName.setText(message.getStudentID().getStudentName());
            opponentMessage.setText(message.getMessage());
            opponentMsgReceivedTime.setText(message.getPostTime());

        }
    }
}

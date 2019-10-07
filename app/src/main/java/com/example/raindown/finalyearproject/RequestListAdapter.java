package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Helper.AppointmentOB;
import Helper.Conversion;
import Helper.PahoMqttClient;

public class RequestListAdapter extends RecyclerView.Adapter<RequestListAdapter.MyviewHolder>{
    private Context mContext;
    private List<AppointmentOB> mData;
    private MqttAndroidClient mqttAndroidClient;
    private FragmentManager fragmentManager;
    private PahoMqttClient pahoMqttClient = new PahoMqttClient();
    private String command="";
    private final static String TAG = "Request list adapter";

    public RequestListAdapter(Context mContext, List<AppointmentOB> mData, MqttAndroidClient mqttAndroidClient) {
        this.mContext = mContext;
        this.mData = mData;
        this.mqttAndroidClient = mqttAndroidClient;
    }

    @Override
    public MyviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater mInfrater = LayoutInflater.from(mContext);
        view = mInfrater.inflate(R.layout.request_cardview, parent,false);

        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyviewHolder holder, final int position) {
        Picasso.with(mContext).load(mData.get(position).getStuffOwnerID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(holder.ownerPhoto);
        holder.ownerName.setText(mData.get(position).getStuffOwnerID().getStudentName());
        holder.ownerProgramme.setText(mData.get(position).getStuffOwnerID().getStudentProgramme());
        Picasso.with(mContext).load(mData.get(position).getStuffID().getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(holder.stuffImage);
        holder.stuffName.setText(mData.get(position).getStuffID().getStuffName());
        holder.stuffDescription.setText(mData.get(position).getStuffID().getStuffDescription());
        holder.dayOfWeek.setText(mData.get(position).getAvailableID().getAvailableDate());
        holder.appointmentDate.setText(mData.get(position).getAppointmentDate());
        holder.startTime.setText(mData.get(position).getAvailableID().getStartTime());
        holder.endTime.setText(mData.get(position).getAvailableID().getEndTime());

        if (mData.get(position).getAppointmentStatus().equals("PENDING")){
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HHmm");
            Date c = Calendar.getInstance().getTime();
            String currentDate = sdf.format(c);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HHmm");
            Date cDate = new Date();
            Date tDate = new Date();
            try {
                cDate = dateFormat.parse(currentDate);
                tDate = dateFormat.parse(mData.get(position).getAppointmentDate() + " " + mData.get(position).getAvailableID().getEndTime());
            }catch (ParseException e){
                e.printStackTrace();
            }

            if (tDate.before(cDate)){
                holder.appointmentStatus.setText("expired");
                holder.appointmentStatus.setTextColor(mContext.getResources().getColor(R.color.red));
            }else {
                holder.appointmentStatus.setText(mData.get(position).getAppointmentStatus().toLowerCase());
                holder.appointmentStatus.setTextColor(mContext.getResources().getColor(R.color.lightgreen));
            }

        }else if (mData.get(position).getAppointmentStatus().equals("ACCEPTED")){
            holder.appointmentStatus.setText("accepted");
            holder.appointmentStatus.setTextColor(mContext.getResources().getColor(R.color.lightgreen));
        }else if (mData.get(position).getAppointmentStatus().equals("COMPLETED")){
            holder.appointmentStatus.setText("completed");
            holder.appointmentStatus.setTextColor(mContext.getResources().getColor(R.color.lightgreen));
        }else if (mData.get(position).getAppointmentStatus().equals("CANCELED")){
            holder.appointmentStatus.setText("canceled");
            holder.appointmentStatus.setTextColor(mContext.getResources().getColor(R.color.red));
        }else if (mData.get(position).getAppointmentStatus().equals("REJECTED")){
            holder.appointmentStatus.setText("rejected");
            holder.appointmentStatus.setTextColor(mContext.getResources().getColor(R.color.red));
        }
        
        holder.requestCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final CharSequence[] options = {"Delete", "Cancel"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Choose your action");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (options[which] == "Delete"){
                            deleteAction(holder, position);

                        }else if (options[which] == "Cancel"){
                            dialog.dismiss();
                        }

                    }
                });
                builder.show();
                return true;
            }
        });
        holder.requestCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestDetail requestDetail = new RequestDetail();
                Bundle bundle = new Bundle();
                bundle.putSerializable("requestOB", mData.get(position));
                requestDetail.setArguments(bundle);
                fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, requestDetail)
                        .addToBackStack(null)
                        .commit();

            }
        });


    }

    private void deleteAction(MyviewHolder holder, int position) {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
        if (holder.appointmentStatus.getText().toString().trim().equals("accepted")){
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case  DialogInterface.BUTTON_POSITIVE:
                            break;
                    }

                }
            };
            builder2.setTitle("Delete appointment denied!")
                    .setMessage("Can not delete appointment that on going.")
                    .setPositiveButton("OK", dialogClickListener).show();

        }else {
            Log.d(TAG,"Hi");
            command = "{\"command\": \"303035303063\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"appointmentID\": " + "\"" + Conversion.asciiToHex(mData.get(position).getAppointmentID()) + "\" ," +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(Navigation.student.getStudentID()) + "\" ," +
                    "\"requesterRecord\": " + "\"" + Conversion.asciiToHex("DELETED") + "\" }";

            try {
                pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
            }catch (Exception e){
                e.printStackTrace();
            }



        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyviewHolder extends RecyclerView.ViewHolder{
        CardView requestCardView;
        ImageView ownerPhoto, stuffImage;
        TextView ownerName, ownerProgramme, stuffName, stuffDescription, dayOfWeek, appointmentDate, startTime, endTime, appointmentStatus;

        public MyviewHolder(View itemView) {
            super(itemView);
            requestCardView = itemView.findViewById(R.id.requestCardView);
            ownerPhoto = itemView.findViewById(R.id.ownerPhoto);
            stuffImage = itemView.findViewById(R.id.stuffImage);
            ownerName = itemView.findViewById(R.id.ownerName);
            ownerProgramme = itemView.findViewById(R.id.ownerProgramme);
            stuffName = itemView.findViewById(R.id.stuffName);
            stuffDescription = itemView.findViewById(R.id.stuffDescription);
            dayOfWeek = itemView.findViewById(R.id.dayOfWeek);
            appointmentDate = itemView.findViewById(R.id.appointmentDate);
            startTime = itemView.findViewById(R.id.startTime);
            endTime = itemView.findViewById(R.id.endTime);
            appointmentStatus = itemView.findViewById(R.id.appointmentStatus);
        }
    }
}

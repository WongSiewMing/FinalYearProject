package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Helper.AvailableTimeOB;
import Helper.Conversion;
import Helper.PahoMqttClient;

public class SelectAvailableTime_Adapter extends RecyclerView.Adapter<SelectAvailableTime_Adapter.MyviewHolder> {

    private Context mContext;
    private List<AvailableTimeOB> mData;
    private MqttAndroidClient mqttAndroidClient;
    private Dialog selectTimeDialog;

    private PahoMqttClient pahoMqttClient = new PahoMqttClient();

    private String command = "", formattedDate = "";
    private Date d;
    private SimpleDateFormat df = new SimpleDateFormat("hh:mma");
    private DateFormat timeFormat = new SimpleDateFormat("HHmm");
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private final static String TAG = "Select Time Adapter";

    public SelectAvailableTime_Adapter(Context mContext, List<AvailableTimeOB> mData, MqttAndroidClient mqttAndroidClient, Dialog selectTimeDialog) {
        this.mContext = mContext;
        this.mData = mData;
        this.mqttAndroidClient = mqttAndroidClient;
        this.selectTimeDialog = selectTimeDialog;
    }

    @Override
    public MyviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.select_time_cardview, parent, false);

        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyviewHolder holder, final int position) {

        final Calendar cal = Calendar.getInstance();

        if (mData.get(position).getAvailableDate().trim().equals("Monday")) {
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                cal.add(Calendar.DATE, 1);
            }
            holder.appointmentDate.setText(dateFormat.format(cal.getTime()));

        }else if (mData.get(position).getAvailableDate().trim().equals("Tuesday")){
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.TUESDAY) {
                cal.add(Calendar.DATE, 1);
            }
            holder.appointmentDate.setText(dateFormat.format(cal.getTime()));
        }else if (mData.get(position).getAvailableDate().trim().equals("Wednesday")){
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
                cal.add(Calendar.DATE, 1);
            }
            holder.appointmentDate.setText(dateFormat.format(cal.getTime()));
        }else if (mData.get(position).getAvailableDate().trim().equals("Thursday")){
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY) {
                cal.add(Calendar.DATE, 1);
            }
            holder.appointmentDate.setText(dateFormat.format(cal.getTime()));
        }else if (mData.get(position).getAvailableDate().trim().equals("Friday")){
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
                cal.add(Calendar.DATE, 1);
            }
            holder.appointmentDate.setText(dateFormat.format(cal.getTime()));
        }else if (mData.get(position).getAvailableDate().trim().equals("Saturday")){
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
                cal.add(Calendar.DATE, 1);
            }
            holder.appointmentDate.setText(dateFormat.format(cal.getTime()));
        }else if (mData.get(position).getAvailableDate().trim().equals("Sunday")){
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                cal.add(Calendar.DATE, 1);
            }
            holder.appointmentDate.setText(dateFormat.format(cal.getTime()));
        }


        if (mData.get(position).getAvailableStatus().equals("ACTIVE")) {
            holder.availableTimeStatus.setText("Active");
            holder.availableTimeStatus.setTextColor(mContext.getResources().getColor(R.color.lightgreen));
        } else if (mData.get(position).getAvailableStatus().equals("DEACTIVE")) {
            holder.availableTimeStatus.setText("Deactive");
            holder.availableTimeStatus.setTextColor(mContext.getResources().getColor(R.color.red));
        }

        try {
            d = timeFormat.parse(mData.get(position).getStartTime().trim());
            holder.startTime.setText(df.format(d));
            d = timeFormat.parse(mData.get(position).getEndTime().trim());
            holder.endTime.setText(df.format(d));

        }catch (ParseException e){
            e.printStackTrace();
        }

        holder.dayOfWeek.setText(mData.get(position).getAvailableDate());


        holder.selectTimeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkTime(holder, position)){
                    Log.d(TAG,"Success time selected!");
                    try {
                        command = "{\"command\": \"30303530305D\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                "\"availableID\": " + "\"" + Conversion.asciiToHex(mData.get(position).getAvailableID()) + "\" ," +
                                "\"studentID\": " + "\"" + Conversion.asciiToHex(Navigation.student.getStudentID()) + "\" ," +
                                "\"targetUserID\": " + "\"" + Conversion.asciiToHex(mData.get(position).getStudentID()) + "\" ," +
                                "\"availableDate\": " + "\"" + Conversion.asciiToHex(holder.appointmentDate.getText().toString().trim()) + "\" ," +
                                "\"availableDayOfWeek\": " + "\"" + Conversion.asciiToHex(mData.get(position).getAvailableDate()) + "\" ," +
                                "\"startTime\": " + "\"" + Conversion.asciiToHex(holder.startTime.getText().toString().trim()) + "\" ," +
                                "\"endTime\": " + "\"" + Conversion.asciiToHex(holder.endTime.getText().toString().trim()) + "\" ," +
                                "\"availableStatus\": " + "\"" + Conversion.asciiToHex(mData.get(position).getAvailableStatus()) + "\" }";

                        pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
                        selectTimeDialog.dismiss();

                    }catch (Exception e){
                        e.printStackTrace();
                    }


                }


            }
        });


    }

    private boolean checkTime(MyviewHolder holder, int position) {
        String error = "";
        boolean indicator = true;
        SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
        Date c = Calendar.getInstance().getTime();
        String currentTime = sdf.format(c);

        Log.d(TAG, "Current Time =" + currentTime);
        DateFormat dateFormat2 = new SimpleDateFormat("HHmm");

        sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(c);
        String targetDate = holder.appointmentDate.getText().toString().trim();

        Date cTime = new Date();
        Date targetTime = new Date();
        try {

            cTime = dateFormat2.parse(currentTime);
            targetTime = dateFormat2.parse(mData.get(position).getStartTime().trim());

        }catch (ParseException e){
            e.printStackTrace();
        }
        if (currentDate.equals(targetDate)){

            if (targetTime.before(cTime)){
                error += "- This time already pass current time.\n";
                indicator = false;
            }
        }


        if (mData.get(position).getAvailableStatus().equals("DEACTIVE")){
            error += "- This time is not available.";
            indicator = false;
        }

        if (indicator == false){
            AlertDialog.Builder sent = new AlertDialog.Builder(mContext);
            sent.setTitle("Invalid selection");
            sent.setMessage(error);
            sent.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            sent.show();
        }
        return indicator;

    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyviewHolder extends RecyclerView.ViewHolder {
        CardView selectTimeCardView;
        TextView appointmentDate, startTime, endTime, dayOfWeek, availableTimeStatus;

        public MyviewHolder(View itemView) {
            super(itemView);
            selectTimeCardView = itemView.findViewById(R.id.selectTimeCardView);
            appointmentDate = itemView.findViewById(R.id.date);
            startTime = itemView.findViewById(R.id.availableStartTime);
            endTime = itemView.findViewById(R.id.availableEndTime);
            dayOfWeek = itemView.findViewById(R.id.availableDate);
            availableTimeStatus = itemView.findViewById(R.id.availableStatus);
        }
    }
}

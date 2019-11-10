package com.example.raindown.finalyearproject;
/*Author : Wong Siew Ming
Programme : RSD3
Year : 2019*/

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;
import java.util.List;

import Helper.Constant;
import Helper.Student;
import Helper.Stuff;
import Helper.Trade;

public class ManageSentTrade extends Fragment {

    private View view;
    private final static List<Trade> arraySentTrade = new ArrayList<>();
    private ProgressDialog pDialog = null;
    private Student s = null;
    private ImageView infoIcon;
    private TextView notice;
    private FragmentManager fragmentManager;

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
        getActivity().setTitle("Sent Trade Request");
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

        view = inflater.inflate(R.layout.manage_sent_trade, container, false);
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("manageSentTrade");
        infoIcon = (ImageView) view.findViewById(R.id.noSentTrade);
        notice = (TextView) view.findViewById(R.id.noSentTradeFound);
        registerClickCallBack();
        getSentTrade();

        return view;
    }

    public void getSentTrade() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getSentTradeList.php?studentID=" + s.getStudentID(),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (response.toString().equals("[]")) {
                                    infoIcon.setVisibility(view.VISIBLE);
                                    notice.setVisibility(view.VISIBLE);
                                }
                                try {
                                    arraySentTrade.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject tradeResponse = (JSONObject) response.get(i);
                                        arraySentTrade.add(new Trade(tradeResponse.getString("TradeID"),
                                                new Stuff(tradeResponse.getString("OfferStuffID"),
                                                        new Student(tradeResponse.getString("OfferStuffStudent")),
                                                        tradeResponse.getString("OfferStuffName"),
                                                        tradeResponse.getString("OfferStuffImage"),
                                                        tradeResponse.getDouble("OfferStuffPrice")),
                                                new Stuff(tradeResponse.getString("RequestStuffID"),
                                                        new Student(tradeResponse.getString("RequestStuffStudent")),
                                                        tradeResponse.getString("RequestStuffName"),
                                                        tradeResponse.getString("RequestStuffImage"),
                                                        tradeResponse.getDouble("RequestStuffPrice")),
                                                new Student(tradeResponse.getString("studentID"),
                                                        tradeResponse.getString("photo"),
                                                        tradeResponse.getString("studentName"),
                                                        tradeResponse.getString("studentProgramme"),
                                                        tradeResponse.getString("studentFaculty"),
                                                        tradeResponse.getInt("yearOfStudy")),
                                                new Student(tradeResponse.getString("SellerID")),
                                                tradeResponse.getString("TradeStatus"),
                                                tradeResponse.getString("TradeDate"),
                                                tradeResponse.getString("TradeTime")));

                                    }


                                    populateListView();
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
                Toast.makeText(getActivity().getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

    }

    public void populateListView() {

        ArrayAdapter<Trade> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.manageSentTradeList);
        list.setAdapter(adapter);
    }

    public class MyListAdapter extends ArrayAdapter<Trade> {

        public MyListAdapter() {
            super(getActivity(), R.layout.tradingcardview, arraySentTrade);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View itemView = convertView;

            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.tradingcardview, parent, false);
            }

            Trade currentTradeRequest = arraySentTrade.get(position);

            ImageView studentAvatar = (ImageView) itemView.findViewById(R.id.tradeStudentAvatar);
            Picasso.with(getActivity()).load(currentTradeRequest.getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(studentAvatar);

            TextView studentName = (TextView) itemView.findViewById(R.id.tradeRequesterName);
            studentName.setText(currentTradeRequest.getStudentID().getStudentName());

            TextView studentFaculty = (TextView) itemView.findViewById(R.id.tradeRequesterFaculty);
            studentFaculty.setText(currentTradeRequest.getStudentID().getStudentFaculty());

            TextView studentProgramme = (TextView) itemView.findViewById(R.id.tradeRequesterProgramme);
            studentProgramme.setText(currentTradeRequest.getStudentID().getStudentProgramme());

            TextView studentYear = (TextView) itemView.findViewById(R.id.tradeRequesterYear);
            studentYear.setText("Year " + currentTradeRequest.getStudentID().getYearOfStudy());

            ImageView offerItem = (ImageView) itemView.findViewById(R.id.tradeOfferItem);
            Picasso.with(getActivity()).load(currentTradeRequest.getUserStuffID().getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(offerItem);

            ImageView requestItem = (ImageView) itemView.findViewById(R.id.tradeRequestItem);
            Picasso.with(getActivity()).load(currentTradeRequest.getRequestStuffID().getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(requestItem);

            TextView tradeDate = (TextView) itemView.findViewById(R.id.tradeDate);
            tradeDate.setText(currentTradeRequest.getTradeDate());

            TextView tradeTime = (TextView) itemView.findViewById(R.id.tradeTime);
            tradeTime.setText(currentTradeRequest.getTradeTime());

            String status = currentTradeRequest.getTradeStatus();

            TextView tradeStatus = (TextView) itemView.findViewById(R.id.tradeStatus);

            if (status.equals("Pending")){
                tradeStatus.setText(currentTradeRequest.getTradeStatus());
                tradeStatus.setTextColor(getResources().getColor(R.color.yellow));
            } else if (status.equals("Accepted")) {
                tradeStatus.setText(currentTradeRequest.getTradeStatus());
                tradeStatus.setTextColor(getResources().getColor(R.color.brightgreeen));
            }

            return itemView;
        }
    }

    public void registerClickCallBack() {

        ListView list = (ListView) view.findViewById(R.id.manageSentTradeList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                Trade clickedSentTrade = arraySentTrade.get(position);
                SentTradeDetail frag = new SentTradeDetail();
                Bundle bundles = new Bundle();
                bundles.putSerializable("ClickedSentTrade", clickedSentTrade);
                bundles.putSerializable("StudentClickedTrade", s); //me
                frag.setArguments(bundles);

                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        });
    }
}

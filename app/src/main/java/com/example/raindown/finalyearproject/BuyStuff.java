package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.*;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.*;

import Helper.*;

/*Author : Adelina Tang Chooi Li
Programme : RSD3
Year : 2017*/

public class BuyStuff extends Fragment {

    public final static List<Category> arrayCategory = new ArrayList<>();
    FragmentManager fragmentManager;
    View view;
    Student s = null;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    String category = "";
    JSONObject jsonObj;
    Category clickedCategory;

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
        getActivity().setTitle("Buy Stuffs");
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
        view = inflater.inflate(R.layout.buystuff, container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("BuyStuff");
        populateArrayCategory();
        registerClickCallBack();

        return view;
    }

    public void populateArrayCategory() {
        arrayCategory.clear();
        arrayCategory.add(0, new Category(R.mipmap.icon_book, "Books"));
        arrayCategory.add(1, new Category(R.mipmap.icon_electronics, "Electronics"));
        arrayCategory.add(2, new Category(R.mipmap.icon_furnitures, "Furnitures"));
        arrayCategory.add(3, new Category(R.mipmap.icon_miscellaneous, "Miscellaneous"));
        arrayCategory.add(4, new Category(R.mipmap.icon_request, "Request"));
        populateListView();
    }


    public void populateListView() {
        ArrayAdapter<Category> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.categoryList);
        list.setAdapter(adapter);
    }


    public class MyListAdapter extends ArrayAdapter<Category> {

        public MyListAdapter() {
            super(getActivity(), R.layout.templatebuystuff, arrayCategory);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatebuystuff, parent, false);
            }
            Category currentCategory = arrayCategory.get(position);

            ImageView categoryIcon = (ImageView) itemView.findViewById(R.id.categoryIcon);
            categoryIcon.setImageResource(currentCategory.getCategoryIcon());

            TextView categoryName = (TextView) itemView.findViewById(R.id.categoryName);
            categoryName.setText(currentCategory.getCategoryName());

            return itemView;

        }
    }


    public void registerClickCallBack() {
        ListView list = (ListView) view.findViewById(R.id.categoryList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                clickedCategory = arrayCategory.get(position);

                if (clickedCategory.getCategoryName().equals("Books")) {
                    category = "303031";
                } else if (clickedCategory.getCategoryName().equals("Electronics")) {
                    category = "303032";
                } else if (clickedCategory.getCategoryName().equals("Furnitures")) {
                    category = "303033";
                } else if (clickedCategory.getCategoryName().equals("Miscellaneous")) {
                    category = "303034";
                }

                String command = "{\"command\": \"303035303031\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"stuffCategory\": " + "\"" + category + "\"}";


                pahoMqttClient = new PahoMqttClient();
                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

                try {
                    jsonObj = new JSONObject(command);
                    if (jsonObj.getString("command").equals("303035303031")) {
                        CategoryType frag = new CategoryType();
                        Bundle bundles = new Bundle();
                        bundles.putSerializable("JSON", command);
                        bundles.putSerializable("ClickedCategory", clickedCategory);
                        bundles.putSerializable("StudentClickedCategory", s);
                        frag.setArguments(bundles);

                        fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.update_fragmentHolder, frag)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .commit();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (clickedCategory.getCategoryName().equals("Request")) {
                    RequestStuff request = new RequestStuff();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("BuyStuff", s);
                    request.setArguments(bundle);
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, request)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack(null)
                            .commit();
                }


            }
        });
    }

}

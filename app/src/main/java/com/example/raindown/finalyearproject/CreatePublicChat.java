package com.example.raindown.finalyearproject;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import Helper.*;
import static android.app.Activity.RESULT_OK;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class CreatePublicChat extends Fragment {

    public static final int CAMERA_REQUEST = 10;
    public static final int IMAGE_GALLERY_REQUEST = 20;
    ImageView attachImage, buttonImage;
    View view;
    Student s = null;
    Button createButton;
    EditText subject, message;
    String error = "", formattedDate = "", time = "", encodedSubject = "", encodedMessage = "", converted = "", command = "";
    ProgressDialog pDialog = null;
    String currentRoomID = "", currentMessageID = "", currentParticipateID = "", roomID = "", messageID = "", participateID = "",jsonURL = "";
    SimpleDateFormat df, sdf;
    Calendar c = null;
    JSONObject jsonObj;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;


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
        getActivity().setTitle("Create Public Chat");
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

        view = inflater.inflate(R.layout.createpublicchat, container, false);
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("Me");

        buttonImage = (ImageView)view.findViewById(R.id.buttonImage);
        attachImage = (ImageView)view.findViewById(R.id.attachImage);
        createButton = (Button) view.findViewById(R.id.createPublicChatButton);
        subject = (EditText) view.findViewById(R.id.subjectName);
        message = (EditText) view.findViewById(R.id.messageInput);

        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = {"Take photo", "Choose from gallery", "Cancel"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Choose your action");
                builder.setItems(options, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int selection) {
                        if (options[selection] == "Take photo") {
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                        } else if (options[selection] == "Choose from gallery") {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            String pictureDirectoryPath = pictureDirectory.getPath();
                            Uri data = Uri.parse(pictureDirectoryPath);
                            photoPickerIntent.setDataAndType(data, "image/*");
                            startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);

                        } else if (options[selection] == "Cancel") {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();

            }

        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateInput()){

                    command = "{\"command\": \"30303530301D\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"creator\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\" ," +
                            "\"subject\": " + "\"" + Conversion.asciiToHex(subject.getText().toString().trim()) + "\" ," +
                            "\"message\": " + "\"" + Conversion.asciiToHex(message.getText().toString().trim()) + "\"}";

                    pahoMqttClient = new PahoMqttClient();
                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,command,"MY/TARUC/SSS/000000001/PUB");
                    generateRoomID();
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                Bitmap cameraImage = (Bitmap) data.getExtras().get("data");
                attachImage.setImageBitmap(cameraImage);
                converted = bitmapToString(cameraImage);
            }

            if (requestCode == IMAGE_GALLERY_REQUEST) {
                Uri imageUri = data.getData();
                InputStream inputStream;

                try {
                    inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                    Bitmap image = BitmapFactory.decodeStream(inputStream);
                    attachImage.setImageBitmap(image);
                    converted = bitmapToString(image);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Unable to open image", Toast.LENGTH_LONG).show();
                }


            }
        }
    }


    public String bitmapToString(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] arr = baos.toByteArray();
        String result = Base64.encodeToString(arr, Base64.DEFAULT);
        return result;
    }


    public boolean validateInput(){

        error = "";
        boolean indicator = true;

        if (attachImage.getDrawable() == null) {
            error += "- No image is attached.\n";
            indicator = false;
        }
        if (subject.getText().toString().trim().equals("")) {
            error += "- Subject is missing.\n";
            indicator = false;
        }
        if(subject.getText().toString().trim().length() > 31){
            error += "- No of characters for subject has exceeded 30.\n";
            indicator = false;
        }
        if (message.getText().toString().trim().equals("")) {
            error += "- Message is missing.\n";
            indicator = false;
        }

        if (indicator == false) {
            AlertDialog.Builder sent = new AlertDialog.Builder(getActivity());
            sent.setTitle("Invalid input");
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


    public void generateRoomID(){

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "generateRoomID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject idResponse = (JSONObject) response.get(i);
                                        currentRoomID = idResponse.getString("CurrentRoomID");
                                        currentMessageID = idResponse.getString("CurrentMessageID");
                                        currentParticipateID = idResponse.getString("CurrentParticipateID");
                                    }
                                    if (currentRoomID.equals("0")) {
                                        roomID = "ROM0001";
                                    } else {
                                        roomID = String.format("ROM%04d", (Integer.parseInt(currentRoomID.substring(3, 7)) + 1));
                                    }
                                    if (currentMessageID.equals("0")) {
                                        messageID = "MSG0001";
                                    } else {
                                        messageID = String.format("MSG%04d", (Integer.parseInt(currentMessageID.substring(3, 7)) + 1));
                                    }
                                    if (currentParticipateID.equals("0")) {
                                        participateID = "PAR0001";
                                    } else {
                                        participateID = String.format("PAR%04d", (Integer.parseInt(currentParticipateID.substring(3, 7)) + 1));
                                    }

                                    insertRoomData();
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


    public void insertRoomData(){

        encodedSubject = "";
        encodedMessage = "";

        c = Calendar.getInstance();
        df = new SimpleDateFormat("dd/MM/yyyy");
        formattedDate = df.format(c.getTime());
        sdf = new SimpleDateFormat("HH:mm");
        time = sdf.format(new Date());

        try {
            jsonObj = new JSONObject(command);
            if(jsonObj.getString("command").equals("30303530301D")){
                String[] encodeSubject = {Conversion.hexToAscii(jsonObj.getString("subject"))};
                for (String s : encodeSubject)
                {
                    encodedSubject += URLEncoder.encode(s, "UTF-8");
                }

                String[] encodeMessage = {Conversion.hexToAscii(jsonObj.getString("message"))};
                for (String s : encodeMessage)
                {
                    encodedMessage += URLEncoder.encode(s, "UTF-8");
                }

                jsonURL = Constant.serverFile + "insertRoomData.php?roomID=" + roomID + "&creator=" + Conversion.hexToAscii(jsonObj.getString("creator"))
                        + "&subject=" + encodedSubject + "&photo=" + "" + "&createDate=" + formattedDate + "&createTime=" + time
                        + "&message=" + encodedMessage + "&messageID=" + messageID + "&participateID=" + participateID;

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
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        if (success.equals("1")) {
                                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                        case DialogInterface.BUTTON_POSITIVE:
                                                            clearData();
                                                            break;
                                                    }
                                                }
                                            };

                                            builder.setTitle("Room is successfully created")
                                                    .setMessage(subject.getText().toString() + " is created")
                                                    .setPositiveButton("OK", dialogClickListener).show();
                                        } else {
                                            builder.setTitle("Failed to create room");
                                            builder.setMessage(subject.getText().toString() + " is failed to be created");
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
                                    if (isAdded()) {
                                    }

                                }
                            }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();

                            try {
                                params.put("roomID", roomID);
                                params.put("creator", Conversion.hexToAscii(jsonObj.getString("creator")));
                                params.put("subject",Conversion.hexToAscii(jsonObj.getString("subject")));
                                params.put("photo", converted);
                                params.put("createDate",formattedDate);
                                params.put("createTime",time);
                                params.put("message", Conversion.hexToAscii(jsonObj.getString("message")));
                                params.put("messageID", messageID);
                                params.put("participateID", participateID);
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


    public void clearData(){
        subject.setText("");
        message.setText("");
        attachImage.setImageResource(0);
    }

}

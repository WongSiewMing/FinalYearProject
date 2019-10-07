package com.example.raindown.finalyearproject;

//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.google.zxing.Result;
//
//import me.dm7.barcodescanner.zxing.ZXingScannerView;
//
//
//public class Scanner extends Fragment implements ZXingScannerView.ResultHandler {
//
//    private ZXingScannerView mScannerView;
//    private static final String TAG = "QRcode";
//
//
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Log.d(TAG,"We in QRcode");
//        mScannerView = new ZXingScannerView(getActivity());
//        getActivity().setTitle("Scan QR Code");
//        return mScannerView;
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        mScannerView.setResultHandler(this);
//        mScannerView.startCamera();
//    }
//
//    @Override
//    public void handleResult(Result result) {
//        String myResult = result.getText();
//        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(myResult));
//        startActivity(browserIntent);
//
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        mScannerView.stopCamera();
//    }
//}

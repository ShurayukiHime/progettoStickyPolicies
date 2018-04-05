package com.example.giada.stickypoliciesapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.giada.stickypoliciesapp.utilities.CryptoUtils;
import com.example.giada.stickypoliciesapp.utilities.NetworkUtils;
import com.example.giada.stickypoliciesapp.utilities.ParsingUtils;
import com.google.gson.Gson;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.crypto.Cipher;

public class AccessDataActivity extends AppCompatActivity {
    private final static String TAG = AccessDataActivity.class.getSimpleName();
    private EncryptedData bobsData;

    final static String DATA_ACCESS_PATH = "access";

    private TextView mUrlDisplayTextView;
    private TextView mSearchResultsTextView;
    private ImageView mImageView;
    private int cipher_block_length = 16;
    byte[] pii;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate!");
        setContentView(R.layout.activity_access_data);

        mSearchResultsTextView = (TextView) findViewById(R.id.tv_search_results);
        mUrlDisplayTextView = (TextView) findViewById(R.id.tv_url_display);
        mImageView = (ImageView) findViewById(R.id.shared_secret_img);
    }

    private void showText(byte[] pii) {
        String stringPii = new String (pii, Charset.forName("UTF-8"));
        mSearchResultsTextView.append("Alice's personal data: " + stringPii + "\n");
    }

    private void showPicture(byte[] pii) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(pii, 0, pii.length);
        mImageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bundle extrasBundle = getIntent().getExtras();
        Gson gson = new Gson();
        String tmp = null;
        if (!extrasBundle.isEmpty() && extrasBundle.containsKey("string_gson_data")) {
            tmp = (String) extrasBundle.get("string_gson_data");
            tmp = ParsingUtils.readInternalStorage(getApplicationContext(), tmp);
            bobsData = gson.fromJson(tmp, EncryptedData.class);
        }
        pii = accessEncryptedData();
        if (pii != null) {
            String policy = bobsData.getStickyPolicy();
            String dataType = ParsingUtils.getTagContent(policy, ParsingUtils.DATA_TYPE_TAG);
            switch (dataType) {
                case ParsingUtils.DATA_TYPE_PICTURE:
                    showPicture(pii);
                    break;
                case ParsingUtils.DATA_TYPE_TEXT:
                    showText(pii);
                    break;
            }
        }
        if (tmp == null) {
            mUrlDisplayTextView.setText("No data received, or data received was corrupted. Please try again.");
            Log.d(TAG, "Received data is null.");
        }
    }

    private byte[] accessEncryptedData() {
        // suppose we received bobsData as Gson from Alice

        // 2) contact TA and send stuff
        EncryptedData taData = new EncryptedData(bobsData.getStickyPolicy(), bobsData.getKeyAndHashEncrypted(), bobsData.getSignedEncrkeyAndHash(), null);
        Gson jsonData = new Gson();
        String postData = jsonData.toJson(taData, EncryptedData.class);
        URL serverURL = NetworkUtils.buildUrl(DATA_ACCESS_PATH, "", "");
        mUrlDisplayTextView.setText(serverURL.toString());
        // 3) obtain key

        //POSSIBLE IMPROVEMENT: BOB REGISTERS@TA AND KEY IS SENT ENCRYPTED WITH BOB'S PUBK
        //MAY NOT BE A GOOD IDEA IF DATA IS LARGER THAN MODULUS?

        byte[] encodedSymmKey = new byte[0];
        try {
            encodedSymmKey = new PolicyClient.SendEncryptedDataTask(postData).execute(serverURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Log.d(TAG, "Error or refusal from Trusted Authority: " + e.getMessage());
        }
        //may not be the wisest solution
        // 4) decryptSymmetric & profit
        try {
            byte[] encrPiiAndIV = bobsData.getEncrPiiAndIV();
            byte[] encryptedPii = Arrays.copyOfRange(encrPiiAndIV, 0, (encrPiiAndIV.length - cipher_block_length));
            byte[] initVec = Arrays.copyOfRange(encrPiiAndIV, (encrPiiAndIV.length - cipher_block_length), encrPiiAndIV.length);
            byte[] decodedPii = CryptoUtils.encrDecrSymmetric(Cipher.DECRYPT_MODE, encodedSymmKey, initVec, encryptedPii);
            return decodedPii;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error in decrypting personal data: " + e.getMessage());
            mSearchResultsTextView.append("We're sorry, something went wrong.");
            return null;
        }
    }
}

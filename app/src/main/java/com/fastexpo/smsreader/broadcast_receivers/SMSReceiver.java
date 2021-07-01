package com.fastexpo.smsreader.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fastexpo.smsreader.R;
import com.fastexpo.smsreader.database.AppDatabase;
import com.fastexpo.smsreader.dataentitty.Keyword;
import com.fastexpo.smsreader.dataentitty.Message;
import com.fastexpo.smsreader.enums.FilterType;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.List;

public class SMSReceiver extends BroadcastReceiver {
    AppDatabase mAppDatabase;
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SMSReceiver";
    String baseURL = "http://demo5951353.mockable.io/";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "SMS Received", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onReceive: SMSRECEIVED");
        mAppDatabase = AppDatabase.getDatabase(context);

        if (intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // get sms objects
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus.length == 0) {
                    return;
                }
                // large message might be broken into many
                SmsMessage[] messages = new SmsMessage[pdus.length];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    sb.append(messages[i].getMessageBody());
                }
                String sender = messages[0].getOriginatingAddress();
                String message = sb.toString();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(messages[0].getTimestampMillis());
                String receivedTime = String.format("%1$tA %1$tb %1$td %1$tY at %1$tI:%1$tM %1$Tp", calendar);

                Message receivedMessage = new Message();
                receivedMessage.isSent = false;
                receivedMessage.messageFrom = sender;
                receivedMessage.messageText = message.trim().toUpperCase();
                receivedMessage.receivedTime = receivedTime;

                if (checkFilters(context, receivedMessage)) {
                    postMessage(context,receivedMessage);
                    mAppDatabase.messageDao().insertAll(receivedMessage);
                }
            }
        }
    }

    public boolean checkFilters(Context context, Message message) {
        switch (getFilter(context)) {
            case SENDER:
                List<Keyword> senderKeywords = mAppDatabase.keywordDao().getKeywords(FilterType.SENDER);
                for (Keyword keyword : senderKeywords) {
                    if (message.messageFrom.contains(keyword.keyword)) {
                        return true;
                    }
                }
                break;
            case MESSAGE:
                List<Keyword> messageKeywords = mAppDatabase.keywordDao().getKeywords(FilterType.MESSAGE);
                for (Keyword keyword : messageKeywords) {
                    if (message.messageText.contains(keyword.keyword)) {
                        return true;
                    }
                }
                break;
            case BOTH:

                boolean matchedSender = false;
                boolean matchesMessage = false;

                List<Keyword> sKeywords = mAppDatabase.keywordDao().getKeywords(FilterType.SENDER);
                for (Keyword keyword : sKeywords) {
                    if (message.messageFrom.contains(keyword.keyword)) {
                        matchedSender = true;
                        break;
                    }
                }

                List<Keyword> mKeywords = mAppDatabase.keywordDao().getKeywords(FilterType.MESSAGE);
                for (Keyword keyword : mKeywords) {
                    if (message.messageText.contains(keyword.keyword)) {
                        matchesMessage = true;
                        break;
                    }
                }
                return matchedSender & matchesMessage;
            case None:
                return true;
        }
        return false;
    }

    public void postMessage(Context context,Message message){
        try {
            JSONObject object = new JSONObject();
            object.put("Sender",message.messageFrom);
            object.put("Content",message.messageText);
            object.put("ReceivedTime",message.receivedTime);
            httpRequest(context, object, Request.Method.POST, new VolleyCallback() {
                @Override
                public void OnSuccess(String object) {
                    Log.d(TAG, "OnSuccess: ");
                    mAppDatabase.messageDao().updateMessage(message.mid,Calendar.getInstance().getTime().toString());
                    Toast.makeText(context, "Posted", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void OnFailure(VolleyError error) {
                    Log.d(TAG, "OnFailure: ");
                }
            },10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FilterType getFilter(Context context) {
        SharedPreferences sp = context.getSharedPreferences(String.valueOf(R.string.SMSReceiverPref), Context.MODE_PRIVATE);
        String filterTypeString = sp.getString(String.valueOf(R.string.FilterType), FilterType.None.name());
        return FilterType.valueOf(filterTypeString);
    }

    public void httpRequest(Context mContext, @Nullable JSONObject message, final int method,
                            final VolleyCallback callBack, int timeOut) throws Exception {
        if (mContext == null) {
            throw new Exception("Null Context");
        }
        if (callBack == null) {
            throw new Exception("Null CallBack");
        }
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        //String URL = Base_url + apiType;
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callBack.OnSuccess(response);
            }
        };

        Response.ErrorListener volleyErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.OnFailure(error);
            }
        };
        StringRequest stringRequest = new StringRequest(method, baseURL, responseListener, volleyErrorListener) {

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return message == null ? null : message.toString().getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", message.toString(), "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = new String(response.data);
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(timeOut, 1, 1.0f));
        requestQueue.add(stringRequest);
    }

    public interface VolleyCallback {
        public void OnSuccess(String object);

        public void OnFailure(VolleyError error);
    }

}
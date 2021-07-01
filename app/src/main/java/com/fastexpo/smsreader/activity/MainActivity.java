package com.fastexpo.smsreader.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fastexpo.smsreader.R;
import com.fastexpo.smsreader.adapters.LogAdapter;
import com.fastexpo.smsreader.database.AppDatabase;
import com.fastexpo.smsreader.databinding.ActivityMainBinding;
import com.fastexpo.smsreader.dataentitty.Keyword;
import com.fastexpo.smsreader.dataentitty.Message;
import com.fastexpo.smsreader.enums.FilterType;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mBinding;
    AppDatabase mAppDatabase;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);
        mAppDatabase = AppDatabase.getDatabase(getApplicationContext());

        mBinding.edtSenderLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinding.edtSenderKeyword.getText().toString().equals("")) {
                    return;
                }
                String senderKeyword = mBinding.edtSenderKeyword.getText().toString().trim().toUpperCase();
                Keyword keyword = new Keyword();
                keyword.filterType = FilterType.SENDER;
                keyword.keyword = senderKeyword;
                mAppDatabase.keywordDao().insertAll(keyword);
                loadSenderKeywords();
                mBinding.edtSenderKeyword.setText("");
            }
        });

        mBinding.edtMessageLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinding.edtMessageKeyword.getText().toString().equals("")) {
                    return;
                }
                String messageKeyWord = mBinding.edtMessageKeyword.getText().toString().trim().toUpperCase();
                Keyword keyword = new Keyword();
                keyword.filterType = FilterType.MESSAGE;
                keyword.keyword = messageKeyWord;
                mAppDatabase.keywordDao().insertAll(keyword);
                loadMessageKeywords();
                mBinding.edtMessageKeyword.setText("");
            }
        });

        mBinding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbSender:
                        setFilterType(getApplicationContext(), FilterType.SENDER);
                        break;
                    case R.id.rbMessage:
                        setFilterType(getApplicationContext(), FilterType.MESSAGE);
                        break;
                    case R.id.rbBoth:
                        setFilterType(getApplicationContext(), FilterType.BOTH);
                        break;
                    case R.id.rbNone:
                    default:
                        setFilterType(getApplicationContext(), FilterType.None);

                }
            }
        });

        mBinding.btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLogs();
            }
        });

        mBinding.btnClearLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this).setTitle(R.string.app_name).setMessage("Do you want to clear logs?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAppDatabase.messageDao().clearLogs();
                        loadLogs();
                    }
                }).setNegativeButton("No", null).show();
            }
        });

        switch (getFilter(getApplicationContext())) {

            case SENDER:
                mBinding.rbSender.setChecked(true);
                break;
            case MESSAGE:
                mBinding.rbMessage.setChecked(true);
                break;
            case BOTH:
                mBinding.rbBoth.setChecked(true);
                break;
            case None:
            default:
                mBinding.rbNone.setChecked(true);
        }

        loadMessageKeywords();
        loadSenderKeywords();
        loadLogs();

        requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, 1145);
    }

    public void setFilterType(Context context, FilterType filterType) {
        SharedPreferences sp = context.getSharedPreferences(String.valueOf(R.string.SMSReceiverPref), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(String.valueOf(R.string.FilterType), filterType.name());
        editor.apply();
    }

    public FilterType getFilter(Context context) {
        SharedPreferences sp = context.getSharedPreferences(String.valueOf(R.string.SMSReceiverPref), Context.MODE_PRIVATE);
        String filterTypeString = sp.getString(String.valueOf(R.string.FilterType), FilterType.None.name());
        return FilterType.valueOf(filterTypeString);
    }

    private void loadLogs() {
        List<Message> messageList = new ArrayList<>();
        messageList = mAppDatabase.messageDao().getAll();
        mBinding.rvLogs.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        List<Message> finalMessageList = messageList;
        LogAdapter adapter = new LogAdapter(getApplicationContext(), new LogAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int pos) {
                Message message = finalMessageList.get(pos);
                String displayMessage = "From: " + message.messageFrom + "\n" +
                        "Received On: " + message.receivedTime + "\n" +
                        "Content: " + message.messageText + "\n" +
                        "Uploaded: " + message.isSent + "\n";
                if (message.isSent) {
                    displayMessage = displayMessage + " Uploaded On: " + message.status;
                }
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(displayMessage)
                        .setTitle(R.string.app_name)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });


        mBinding.rvLogs.setAdapter(adapter);
        adapter.setData(messageList);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        } else {
            finish();
        }

    }

    public void loadSenderKeywords() {
        mBinding.cgSenderKeywords.removeAllViews();
        List<Keyword> senderKeywords = mAppDatabase.keywordDao().getKeywords(FilterType.SENDER);
        for (Keyword keyword : senderKeywords) {
            Chip chip = new Chip(MainActivity.this);
            chip.setTag(keyword.kwid);
            chip.setCloseIconVisible(true);
            chip.setCheckable(false);
            chip.setText(keyword.keyword);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = (int) v.getTag();
                    Keyword toDelete = mAppDatabase.keywordDao().getKeyword(id);
                    mAppDatabase.keywordDao().delete(toDelete);
                    mBinding.cgSenderKeywords.removeView(v);
                }
            });
            mBinding.cgSenderKeywords.addView(chip);
        }
    }

    public void loadMessageKeywords() {
        mBinding.cgMessageKeywords.removeAllViews();
        List<Keyword> messageKeywords = mAppDatabase.keywordDao().getKeywords(FilterType.MESSAGE);
        for (Keyword keyword : messageKeywords) {
            Chip chip = new Chip(MainActivity.this);
            chip.setTag(keyword.kwid);
            chip.setCloseIconVisible(true);
            chip.setCheckable(false);
            chip.setText(keyword.keyword);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = (int) v.getTag();
                    Keyword toDelete = mAppDatabase.keywordDao().getKeyword(id);
                    mAppDatabase.keywordDao().delete(toDelete);
                    mBinding.cgMessageKeywords.removeView(v);
                }
            });
            mBinding.cgMessageKeywords.addView(chip);
        }
    }


}
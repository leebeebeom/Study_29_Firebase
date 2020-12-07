package com.example.a29_firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.example.a29_firebase.MainActivity.TAG;
//서비스기 때문에 매니패스트에 추가해 줘야함
//클래스 네임에서 Alt+Enter
//이게 끝임. 매우 간단
public class MyFcmService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: ID:" + remoteMessage.getMessageId());
        Log.d(TAG, "onMessageReceived: DATA:" + remoteMessage.getData());

    }
}

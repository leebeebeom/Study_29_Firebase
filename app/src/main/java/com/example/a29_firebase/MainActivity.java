package com.example.a29_firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    public static final String MESSAGES = "messages";
    public static final String TAG = "로그";
    private DatabaseReference mDatabaseReference; // 데이터 베이스 객체
    private EditText mMessageEditText; // MainActivity 에 메세지 입력 객체
    private FirebaseAuth mFirebaseAuth; // 인증 객체
    private FirebaseUser mFirebaseUser; // 인증이 되면 유저 정보가 저장될 객체
    private String mUsername; // mFirebaseUser 에서 가져온 이름이 저장될 객체
    private String mPhotoUrl; // mFireBaseUser 에에서 가져온 Uri 가 저장될 객체
    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder> mAdapter;
    private String mUserEmail;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    //리사이클러뷰 뷰홀더
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView text;
        CircleImageView profilePhoto;
        ImageView image;
        CircleImageView profilePhoto_My;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.message_name);
            text = itemView.findViewById(R.id.message_text);
            profilePhoto = itemView.findViewById(R.id.message_profile_photo);
            image = itemView.findViewById(R.id.message_image);
            profilePhoto_My = itemView.findViewById(R.id.message_profile_photo_my);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference(); // 파이어베이스 데이터베이스 객체 초기화

        mFirebaseAuth = FirebaseAuth.getInstance(); // 인증 객체 초기화

        mFirebaseUser = mFirebaseAuth.getCurrentUser(); // 로그인이 안 됐다면 mFirebaseUser 는 null
        //로그인
        if (mFirebaseUser == null) {
            //로그인이 안 됐다면 로그인 페이지로 보냄
            startActivity(new Intent(this, SigninActivity.class));
            finish();
        } else {
            //로그인이 됐다면 이름, 프사 가져옴
            mUsername = mFirebaseUser.getDisplayName();
            mUserEmail = mFirebaseUser.getEmail();
            Log.d(TAG, "onCreate: " + mUsername.toString() + ", Email:" + mUserEmail);
            if (mFirebaseUser.getPhotoUrl() != null) {//프로필 사진이 있다면
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mMessageEditText = findViewById(R.id.message_edit);
        findViewById(R.id.send_btn).setOnClickListener(v -> {//보내기 버튼 클릭시 chatMessage 객체 생성
            Log.d(TAG, "onCreate: " + mUsername.toString());
            ChatMessage chatMessage = new ChatMessage(mMessageEditText.getText().toString(),
                    mUsername, mPhotoUrl, null, mUserEmail);
            //messages 디렉토리가 없다면 생성 후 데이터 넣기
            mDatabaseReference.child(MESSAGES)
                    .push()
                    .setValue(chatMessage);//여기까지 데이터 푸쉬
            mMessageEditText.setText("");
        });

        mRecyclerView = findViewById(R.id.recycler_view);
        //어댑터 초기화
        Query query = mDatabaseReference.child(MESSAGES); // message 데이터 요청
        FirebaseRecyclerOptions<ChatMessage> options = new FirebaseRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class) // ChatMessage 객체로 받겠다.
                .build();
        mAdapter = new FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull ChatMessage model) {

                holder.text.setText(model.getText());
                holder.name.setText(model.getName());
                //내 메세지일 경우
                if (mUserEmail.equals(model.getEmail())) {
                    //오른쪽 정렬
                    holder.name.setGravity(Gravity.END);
                    holder.text.setGravity(Gravity.END);
                    //왼쪽 프사 GONE
                    holder.profilePhoto.setVisibility(View.GONE);
                    //오른쪽 프사 VISIBLE
                    holder.profilePhoto_My.setVisibility(View.VISIBLE);
                    //프사가 없을 경우
                    if (model.getProfilePhotoUrl() == null) {
                        //기본 이미지
                        Drawable drawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_account_circle_24);
                        holder.profilePhoto_My.setImageDrawable(drawable);
                    } else {//프사가 있을 경우
                        Glide.with(MainActivity.this)
                                .load(model.getProfilePhotoUrl())
                                .into(holder.profilePhoto_My);
                    }
                } else {//상대방 메세지일 경우
                    if (model.getProfilePhotoUrl() == null) {
                        //벡터 이미지는 그냥 안들어감
                        Drawable drawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_account_circle_24);
                        holder.profilePhoto.setImageDrawable(drawable);
                    } else {
                        Glide.with(MainActivity.this)
                                .load(model.getProfilePhotoUrl())
                                .into(holder.profilePhoto);
                    }
                }
            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
                return new MessageViewHolder(view);
            }
        };
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        // 새로운 글이 추가되면 제일 하단으로 포지션 이동
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mAdapter.getItemCount();
                LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        // 키보드 올라올 때 RecyclerView의 위치를 마지막 포지션으로 이동
        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                        }
                    }, 100);
                }
            }
        });
        //리모트 컨픽 초기화
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        //설정
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
                new FirebaseRemoteConfigSettings.Builder()
                        //값을 가져오는 최소 시간 설정
                        .setMinimumFetchIntervalInSeconds(0)
                        .build();
        //맵 객체로 설정에 필요한 디폴트 값 넣어주기
        //파이어베이스 홈페이지에서 키값이 필요하기 때문에 맵으로 넣어줌
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("message_length", 10L);
        //컨픽 설정 적용
        mFirebaseRemoteConfig.setConfigSettingsAsync(firebaseRemoteConfigSettings);
        //기본 값 설정
        mFirebaseRemoteConfig.setDefaultsAsync(defaultConfigMap);
        //가져오기
        mFirebaseRemoteConfig.fetchAndActivate()
                //여기서 안 사실
                //CompleteListener 는 Success 와 Failure 를 합쳐놓은 개념
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //패치앤액티베이트 후 FirebaseRemoteConfig에서 값을 가져올 수 있다
                        long messageLength = mFirebaseRemoteConfig.getLong("message_length");
                        Log.d(TAG, "onCreate: " + messageLength);
                        mMessageEditText.setFilters(new InputFilter[]{
                                new InputFilter.LengthFilter((int) messageLength)});
                    } else {
                        Log.d(TAG, "onComplete: 실패");
                    }
                });
    }

    //상태주기에 따라 상태를 컨트롤함
    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //메뉴에 로그아웃 버튼 달아주기
        getMenuInflater().inflate(R.menu.item_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sign_out_menu) {
            //로그아웃 버튼 클릭시 로그아웃
            FirebaseAuth.getInstance().signOut();
            mUsername = ""; // 유저네임 비우기
            mPhotoUrl = ""; // 프사 Uri 비우기
            startActivity(new Intent(this, SigninActivity.class));
            finish();
            return true;
        } else if (item.getItemId() == R.id.crash_mune) {
            throw new RuntimeException("Test Crash");
        }

        return super.onOptionsItemSelected(item);
    }
}
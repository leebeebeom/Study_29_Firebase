package com.example.a29_firebase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
//로그인은 파이어베이스 홈페이지에 가면 잘 설명되어 있음.

//문제(였던 것)
//파이어베이스에서 구글 로그인 활성 안했던 것(ApiException 12500)
//파이어베이스에 SHA-1 지문 추가 안했던 것(ApiException 10)
public class SigninActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 1000;
    public static final String TAG = "로그";
    private FirebaseAuth mFirebaseAuth;
    private GoogleSignInClient mGoogleSignInClient; // GoogleApiClient Deprecated 됨

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mFirebaseAuth = FirebaseAuth.getInstance(); // Auth 객체 초기화
        //gso 객체 초기화(파이어 베이스 홈페이지에 나와있음)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        //구글 클라이언트 초기화
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //SignIn 버튼 클릭시
        findViewById(R.id.btn_sign_in).setOnClickListener(v -> {
            Intent intent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(intent, REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            //로그인 결과
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //task 에서 account 가져오기
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "onActivityResult: " + account.getIdToken());
                //파이어베이스와 구글계정과 연동하는 메소드
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.d(TAG, "onActivityResult: " + e);
            }
        }
    }

    //연동 메소드
    private void firebaseAuthWithGoogle(String idToken) {
        //그레덴셜 객체 생성
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        //Auth 객체랑 연결
        mFirebaseAuth.signInWithCredential(credential)
                //완료시
                //annOnSuccess, Cancel, Fail 등 여러가지 리스너가 있음
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        //문제가 있다면 토스트
                        Toast.makeText(SigninActivity.this, "인증 실패", Toast.LENGTH_SHORT).show();
                    } else {
                        //성공시 메인 액티비티로 보냄
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
                });


    }

}
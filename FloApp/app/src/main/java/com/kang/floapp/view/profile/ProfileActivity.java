package com.kang.floapp.view.profile;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.kang.floapp.R;
import com.kang.floapp.model.User;
import com.kang.floapp.model.dto.ResponseDto;
import com.kang.floapp.model.network.UserAPI;
import com.kang.floapp.utils.SharedPreference;
import com.kang.floapp.view.common.Constants;
import com.kang.floapp.view.main.MainActivity;
import com.kang.floapp.view.user.JoinActivity;
import com.kang.floapp.view.user.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private Context mContext = ProfileActivity.this;
    private static final String TAG = "ProfileActivity";
    private MaterialButton mtBtnLogout;
    private MaterialButton mtBtnProfileUpdate;
    private TextView tvUsername;
    private TextView tvUserEmail;
    private ImageView ivProfileBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();

        ivProfileBack = findViewById(R.id.iv_profile_back);
        mtBtnLogout = findViewById(R.id.mt_btn_logout);
        tvUsername = findViewById(R.id.tv_username);
        tvUserEmail = findViewById(R.id.tv_user_email);
        mtBtnProfileUpdate = findViewById(R.id.mt_btn_profileUpdate);

        mtBtnLogout.setOnClickListener(v -> {
            SharedPreference.removeAttribute(mContext, "principal");
            Constants.JSessionValue = null;

            alert("????????????");
        });

        mtBtnProfileUpdate.setOnClickListener(v -> {

            startActivityForResult(new Intent(mContext, ProfileUpdateActivity.class),300);
        });


        ivProfileBack.setOnClickListener(v -> {
            finish();
        });

        String username = intent.getStringExtra("username");
        String email = intent.getStringExtra("email");

        tvUsername.setText(username);
        tvUserEmail.setText(email);

    }


    public void findUser(){
        Call<ResponseDto<User>> call = UserAPI.retrofit.create(UserAPI.class).find(userValidaionCheck().getId(), Constants.JSessionValue);

        call.enqueue(new Callback<ResponseDto<User>>() {
            @Override
            public void onResponse(Call<ResponseDto<User>> call, Response<ResponseDto<User>> response) {
                Log.d(TAG, "onResponse: ?????? ??????");

                if(response.body() == null || response.body().getStatusCode() != 1){
                    startActivity(new Intent(mContext, LoginActivity.class));
                }else if(response.body().getStatusCode() == 1){

                    User user = response.body().getData();
                    Log.d(TAG, "onResponse: ????????? ?????? ??????: " + user);

                    Gson gson = new Gson();
                    String principal = gson.toJson(user);
                    Log.d(TAG, "onResponse: gson ?????? " + principal);
                    SharedPreference.setAttribute(mContext,"principal",principal);// ?????? ??????

                    tvUsername.setText(user.getUsername());
                    tvUserEmail.setText(user.getEmail());
                }

            }

            @Override
            public void onFailure(Call<ResponseDto<User>> call, Throwable t) {
                Log.d(TAG, "onFailure: ?????? ??????"+t.getMessage());
            }
        });

    }



    private void alert(String value){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(value);
        builder.setPositiveButton("??????",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(value.equals("????????????")){
                            onBackPressed();
                        }
                    }
                });
        builder.show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class); //?????? ?????????????????? ?????? ??????????????? ???????????? ????????? ??????
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //????????? ????????? ??????
        startActivity(intent);  //????????? ??????
        finish();   //?????? ???????????? ??????
    }


    public User userValidaionCheck(){
        Gson gson = new Gson();
        String principal = SharedPreference.getAttribute(mContext, "principal");
        Log.d(TAG, "onCreateView: ??????" + principal);
        User user = gson.fromJson(principal, User.class);
        Log.d(TAG, "onCreateView: ????" + user);

        return user;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "requestCode: " + requestCode);
        Log.d(TAG, "resultcode: "+resultCode);

        if (requestCode == 300){
            if(resultCode == 1){
                findUser();
                Toast.makeText(mContext, "?????? ????????? ?????????????????????", Toast.LENGTH_SHORT).show();
            }else{

            }
        }


    }
}
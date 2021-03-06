package com.kang.floapp.view.user;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.kang.floapp.R;
import com.kang.floapp.model.User;
import com.kang.floapp.model.dto.ResponseDto;
import com.kang.floapp.model.network.AuthAPI;
import com.kang.floapp.utils.SharedPreference;
import com.kang.floapp.view.common.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private Context mContext = LoginActivity.this;

    
    private TextInputEditText inputLoginName;
    private TextInputEditText inputLoginPassword;
    private Button mtBtnLogin;
    private TextView tvJoinBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        tvJoinBtn = findViewById(R.id.tv_join_btn);
        inputLoginName = findViewById(R.id.input_login_name);
        inputLoginPassword = findViewById(R.id.input_login_password);
        mtBtnLogin = findViewById(R.id.mt_btn_login);

        tvJoinBtn.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, JoinActivity.class));
        });
        
        mtBtnLogin.setOnClickListener(v -> {

            String username = inputLoginName.getText().toString().trim();
            String password = inputLoginPassword.getText().toString().trim();


            if (username.equals("") || password.equals("")){
                Toast.makeText(this, "null ?????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();

            }else {

                //Call<Void> call = AuthAPI.retrofit.create(AuthAPI.class).login(username, password);

                Call<ResponseDto<User>> call = AuthAPI.retrofit.create(AuthAPI.class).login(username, password);


                call.enqueue(new Callback<ResponseDto<User>>() {
                    @Override
                    public void onResponse(Call<ResponseDto<User>> call, Response<ResponseDto<User>> response) {
                        Log.d(TAG, "onResponse: " + response.body());


                        if(response.body().getStatusCode() == 1){
                            Log.d(TAG, "onResponse: "+response.headers());
                            Log.d(TAG, "onResponse: "+response.headers().get("Set-Cookie"));

                            String JSessionId = response.headers().get("Set-Cookie");
                            String JSessionValue = JSessionId.split(";")[0];

                            Log.d(TAG, "????????????: " + JSessionValue);

                            Constants.JSessionValue = JSessionValue;


                            User user = response.body().getData(); //????????? ??? ???????????
                            Log.d(TAG, "onResponse: ????????? ?????? ??????: " + user);

                            Gson gson = new Gson();
                            String principal = gson.toJson(user);
                            Log.d(TAG, "onResponse: gson ?????? " + principal);
                            SharedPreference.setAttribute(mContext,"principal",principal);// ?????? ??????

                            //Constants.user = user; ?????? ??? ?????????.. ??? ???????????? ???????????? ?????? null??? ?????? ?????????..
                            //????????? ??????????????? JseesionId??? ????????? ????????? ??????????????????, jsessionId??? ????????? ??????????????? ?????????.. ????????? ????????????.

                            alert(response.body().getMsg());

                        }else{
                            alert(response.body().getMsg());
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseDto<User>> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t.getMessage());
                        alert("????????? ????????? ?????????????????????.");
                    }
                });
            }
        });





    }

    private void alert(String value){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(value);
        builder.setPositiveButton("??????",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(value.equals("login success")){
                            finish();
                        }
                    }
                });
        builder.show();
    }




}
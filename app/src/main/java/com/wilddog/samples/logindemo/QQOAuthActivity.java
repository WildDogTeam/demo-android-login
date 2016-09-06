package com.wilddog.samples.logindemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qq.util.Util;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQAuth;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.credentialandprovider.AuthCredential;
import com.wilddog.wilddogauth.core.credentialandprovider.QQAuthProvider;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogauth.model.WilddogUser;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class QQOAuthActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();
    public  String mAppid;
    private Button mNewLoginButton;
    private TextView mUserInfo;
    private ImageView mUserLogo;
    public static QQAuth mQQAuth;
    private UserInfo mInfo;
    private Tencent mTencent;
    private TextView mWilddogText;
    private WilddogAuth mWilddogAuth;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "-->onCreate");
        /* Create the Wilddog ref that is used for all authentication with Wilddog */
        mWilddogAuth = WilddogAuth.getInstance(getResources().getString(R.string.wilddog_url),this);
        setContentView(R.layout.activity_qq);
        initViews();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "-->onStart");
        final Context context = QQOAuthActivity.this;
        final Context ctxContext = context.getApplicationContext();
        mAppid = getResources().getString(R.string.APP_ID);
        mQQAuth = QQAuth.createInstance(mAppid, ctxContext);
        mTencent = Tencent.createInstance(mAppid, QQOAuthActivity.this);
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "-->onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "-->onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "-->onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "-->onDestroy");
        super.onDestroy();
    }

    private void initViews() {
        mNewLoginButton = (Button) findViewById(R.id.new_login_btn);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.main_container);
        OnClickListener listener = new NewClickListener();
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);
            if (view instanceof Button) {
                view.setOnClickListener(listener);
            }
        }
        mUserInfo = (TextView) findViewById(R.id.user_nickname);
        mUserLogo = (ImageView) findViewById(R.id.user_logo);
        updateLoginButton();
    }

    private void updateLoginButton() {
        if (mQQAuth != null && mQQAuth.isSessionValid()) {
            mNewLoginButton.setTextColor(Color.RED);
            mNewLoginButton.setText(R.string.qq_logout);
        } else {
            mNewLoginButton.setTextColor(Color.BLUE);
            mNewLoginButton.setText(R.string.qq_login);
            mWilddogAuth.signOut();
        }
    }

    private void updateUserInfo() {
        if (mQQAuth != null && mQQAuth.isSessionValid()) {
            IUiListener listener = new IUiListener() {

                @Override
                public void onError(UiError e) {

                }

                @Override
                public void onComplete(final Object response) {
                    Message msg = new Message();
                    msg.obj = response;
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                    new Thread() {

                        @Override
                        public void run() {
                            JSONObject json = (JSONObject) response;
                            if (json.has("figureurl")) {
                                Bitmap bitmap = null;
                                try {
                                    bitmap = Util.getbitmap(json
                                            .getString("figureurl_qq_2"));
                                } catch (JSONException e) {

                                }
                                Message msg = new Message();
                                msg.obj = bitmap;
                                msg.what = 1;
                                mHandler.sendMessage(msg);
                            }
                        }

                    }.start();
                }

                @Override
                public void onCancel() {
                }
            };
            mInfo = new UserInfo(this, mQQAuth.getQQToken());
            mInfo.getUserInfo(listener);

        } else {
            mUserInfo.setText("");
            mUserInfo.setVisibility(View.GONE);
            mUserLogo.setVisibility(View.GONE);
            mWilddogText.setVisibility(View.GONE);
        }
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                JSONObject response = (JSONObject) msg.obj;
                if (response.has("nickname")) {
                    try {
                        mUserInfo.setVisibility(View.VISIBLE);
                        mUserInfo.setText(response.getString("nickname"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (msg.what == 1) {
                Bitmap bitmap = (Bitmap) msg.obj;
                mUserLogo.setImageBitmap(bitmap);
                mUserLogo.setVisibility(View.VISIBLE);
            }
        }

    };

    private void onClickLogin() {
        if (!mQQAuth.isSessionValid()) {
            IUiListener listener = new BaseUiListener() {
                @Override
                protected void doComplete(JSONObject values) {
                    updateUserInfo();
                    updateLoginButton();
                }
            };
            mQQAuth.login(this, "all", listener);
            mTencent.login(this, "all", listener);
        } else {
            mQQAuth.logout(this);
            updateUserInfo();
            updateLoginButton();
        }
    }

    public static boolean ready(Context context) {
        if (mQQAuth == null) {
            return false;
        }
        boolean ready = mQQAuth.isSessionValid()
                && mQQAuth.getQQToken().getOpenId() != null;
        if (!ready)
            Toast.makeText(context, "login and get openId first, please!",
                    Toast.LENGTH_SHORT).show();
        return ready;
    }

    private class BaseUiListener implements IUiListener {



        @Override
        public void onComplete(Object response) {

            Util.showResultDialog(QQOAuthActivity.this, response.toString(),
                    "登录成功");
            doComplete((JSONObject) response);

            //将token集成到用Wilddog中
            mWilddogText = (TextView) findViewById(R.id.wilddog_text);




            String access_token="";
            try {
                access_token= (String)((JSONObject) response).get("access_token");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(TextUtils.isEmpty(access_token)){


            }

            AuthCredential qqAuthCredential= QQAuthProvider.getCredential(access_token);

            mWilddogAuth.signInWithCredential(qqAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        WilddogUser wilddogUser=task.getResult().getWilddogUser();
                        if(wilddogUser==null){
                            mWilddogText.setVisibility(View.GONE);
                        }else {
                            mWilddogText.setText("集成Wilddog登录信息：\n"+"Logged in as " + wilddogUser.getUid() + " (" + wilddogUser.getProviderId() + ")");
                            Log.d("authData--------", wilddogUser.toString());
                        }
                    }else {
                        Log.e("error--------", task.getException().toString());
                    }
                }
            });

           /* mWilddogRef.authWithOAuthToken("qq", options, new Wilddog.AuthResultHandler() {

                @Override
                public void onAuthenticated(AuthData authData) {
                    if (authData==null){
                        mWilddogText.setVisibility(View.GONE);
                    } else {
                        mWilddogText.setText("集成Wilddog登录信息：\n"+"Logged in as " + authData.getUid() + " (" + authData.getProvider() + ")");
                        Log.d("authData--------", authData.toString());
                    }


                }

                @Override
                public void onAuthenticationError(WilddogError error) {
                    Log.e("error--------", error.toString());
                }
            });*/
        }

        protected void doComplete(JSONObject values) {

        }

        @Override
        public void onError(UiError e) {
            Util.toastMessage(QQOAuthActivity.this, "onError: " + e.errorDetail);
            Util.dismissDialog();
        }

        @Override
        public void onCancel() {
            Util.toastMessage(QQOAuthActivity.this, "onCancel: ");
            Util.dismissDialog();
        }
    }


    class NewClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Class<?> cls = null;
            switch (v.getId()) {
                case R.id.new_login_btn:
                    onClickLogin();
                    return;
            }
            if (cls != null) {
                Intent intent = new Intent(context, cls);
                context.startActivity(intent);
            }
        }
    }
}

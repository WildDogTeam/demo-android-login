package com.wilddog.samples.logindemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.WeiboParameters;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;

import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;
import com.sina.weibo.sdk.utils.UIUtils;

import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.credentialandprovider.AuthCredential;
import com.wilddog.wilddogauth.core.credentialandprovider.WeiboAuthCredential;
import com.wilddog.wilddogauth.core.credentialandprovider.WeiboAuthProvider;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogauth.model.WilddogUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jeen on 2015/9/23.
 * 该类主要演示如何使用 Code 手动进行新浪微博授权。
 */
public class WeiboOAuthActivity extends Activity {

    /**
     * 通过 code 获取 Token 的 URL
     */
    private static final String OAUTH2_ACCESS_TOKEN_URL = "https://open.weibo.cn/oauth2/access_token";
    /** 获取 Token 成功或失败的消息 */
    private static final int MSG_FETCH_TOKEN_SUCCESS = 1;
    private static final int MSG_FETCH_TOKEN_FAILED  = 2;

    /**
     * 获取到的 Token
     */
    private Oauth2AccessToken mAccessToken;

    /**
     * WeiboSDKDemo 程序的 APP_SECRET。
     * 请注意：请务必妥善保管好自己的 APP_SECRET，不要直接暴露在程序中，此处仅作为一个DEMO来演示。
     */

    private static final String TAG = "WeiboOAuthActivity";

    /* A reference to the WilddogAuth */
    private WilddogAuth mWilddogAuth;

    /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mSsoHandler;

    /** 微博 Web 授权接口类，提供登陆等功能  */
    private WeiboAuth mWeiboAuth;

    /** 获取到的 Code */
    private String mCode;

    /** 当前 DEMO 应用的 APP_KEY，第三方应用应该使用自己的 APP_KEY 替换该 APP_KEY */
    private  String APP_KEY;

    private String WEIBO_DEMO_APP_SECRET ;

    /**
     * 当前 DEMO 应用的回调页，第三方应用可以使用自己的回调页。
     *
     * <p>
     * 注：关于授权回调页对移动客户端应用来说对用户是不可见的，所以定义为何种形式都将不影响，
     * 但是没有定义将无法使用 SDK 认证登录。
     * 建议使用默认回调页：https://api.weibo.com/oauth2/default.html
     * </p>
     */
    private  String REDIRECT_URL = "https://auth.wilddog.com" ;

    /**
     * Scope 是 OAuth2.0 授权机制中 authorize 接口的一个参数。通过 Scope，平台将开放更多的微博
     * 核心功能给开发者，同时也加强用户隐私保护，提升了用户体验，用户在新 OAuth2.0 授权页中有权利
     * 选择赋予应用的功能。
     *
     * 我们通过新浪微博开放平台-->管理中心-->我的应用-->接口管理处，能看到我们目前已有哪些接口的
     * 使用权限，高级权限需要进行申请。
     *
     * 目前 Scope 支持传入多个 Scope 权限，用逗号分隔。
     *
     * 有关哪些 OpenAPI 需要权限申请，请查看：http://open.weibo.com/wiki/%E5%BE%AE%E5%8D%9AAPI
     * 关于 Scope 概念及注意事项，请查看：http://open.weibo.com/wiki/Scope
     */
    private  String SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";

    /** UI 元素列表 */
    private Button mCodeButton;
    private Button mAuthCodeButton;
    private Button mWilddogButton;
    private TextView mCodeText;
    private TextView mTokenText;
    private TextView mWilddogText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo);

        /* Create the Wilddog ref that is used for all authentication with Wilddog */
        mWilddogAuth = WilddogAuth.getInstance(getResources().getString(R.string.wilddog_url),this);

        APP_KEY = getResources().getString(R.string.APP_KEY);
        WEIBO_DEMO_APP_SECRET = getResources().getString(R.string.WEIBO_DEMO_APP_SECRET);
        // 初始化微博对象
        mWeiboAuth = new WeiboAuth(this, APP_KEY, REDIRECT_URL, SCOPE);

        // 第一步：获取 Code
        mCodeButton = (Button) findViewById(R.id.code);
        mCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWeiboAuth.authorize(new AuthListener(), WeiboAuth.OBTAIN_AUTH_CODE);
            }
        });

        // 第二步：通过 Code 获取 Token
        mAuthCodeButton = (Button) findViewById(R.id.mCodeButton);
        mAuthCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchTokenAsync(mCode, WEIBO_DEMO_APP_SECRET);
            }
        });
        // 第三步：集成到Wilddog中
        mWilddogButton = (Button) findViewById(R.id.mWilddogButton);
        mWilddogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchTokenAsync1();
            }
        });
        mWilddogAuth.signOut();
    }


    /**
     * 微博认证授权回调类。
     */
    class AuthListener implements WeiboAuthListener {


        @Override
        public void onComplete(Bundle values) {
            if (values == null) {
                Toast.makeText(WeiboOAuthActivity.this,
                        "values为空", Toast.LENGTH_SHORT).show();
                return;
            }

            String code = values.getString("code");
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(WeiboOAuthActivity.this,
                        "obtain_code_failed", Toast.LENGTH_SHORT).show();
                return;
            }
            mCodeText = (TextView) findViewById(R.id.code_text);
            mTokenText = (TextView) findViewById(R.id.token_text);
            mWilddogText = (TextView) findViewById(R.id.wilddog_text);
            mCode = code;
            mCodeText.setText(code);
            //使Button失效
            mAuthCodeButton.setEnabled(true);
            mWilddogButton.setEnabled(true);
            mTokenText.setText("");
            mWilddogText.setText("");
            Toast.makeText(WeiboOAuthActivity.this,
                    "obtain_code_success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            UIUtils.showToast(WeiboOAuthActivity.this,
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG);
        }

        @Override
        public void onCancel() {
            Toast.makeText(WeiboOAuthActivity.this,
                    "toast_auth_canceled", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 该 Handler 配合RequestListener对应的回调来更新 UI。
     */
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FETCH_TOKEN_SUCCESS:
                    // 显示 Token
                    String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                            new java.util.Date(mAccessToken.getExpiresTime()));
                    String format = getString(R.string.weibosdk_demo_token_to_string_format_1);
                    mTokenText.setText(String.format(format, mAccessToken.getToken(), date));
                    mAuthCodeButton.setEnabled(false);

                    Toast.makeText(WeiboOAuthActivity.this,
                            "obtain_token_success", Toast.LENGTH_SHORT).show();
                    break;

                case MSG_FETCH_TOKEN_FAILED:
                    Toast.makeText(WeiboOAuthActivity.this,
                            "obtain_token_failed", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        };
    };

    /**
     * 异步获取 Token。
     *
     * @param authCode  授权 Code，该 Code 是一次性的，只能被获取一次 Token
     * @param appSecret 应用程序的 APP_SECRET，请务必妥善保管好自己的 APP_SECRET，
     *                  不要直接暴露在程序中，此处仅作为一个DEMO来演示。
     */
    public void fetchTokenAsync(String authCode, String appSecret) {

        WeiboParameters requestParams = new WeiboParameters();
        requestParams.add(WBConstants.AUTH_PARAMS_CLIENT_ID,     APP_KEY);
        requestParams.add(WBConstants.AUTH_PARAMS_CLIENT_SECRET, appSecret);
        requestParams.add(WBConstants.AUTH_PARAMS_GRANT_TYPE,    "authorization_code");
        requestParams.add(WBConstants.AUTH_PARAMS_CODE, authCode);
        requestParams.add(WBConstants.AUTH_PARAMS_REDIRECT_URL, REDIRECT_URL);

        /**
         * 请注意：
         * {@link RequestListener} 对应的回调是运行在后台线程中的，
         * 因此，需要使用 Handler 来配合更新 UI。
         */
        AsyncWeiboRunner.request(OAUTH2_ACCESS_TOKEN_URL, requestParams, "POST", new RequestListener() {
            @Override
            public void onComplete(String response) {
                LogUtil.d(TAG, "Response: " + response);

                // 获取 Token 成功
                Oauth2AccessToken token = Oauth2AccessToken.parseAccessToken(response);
                if (token != null && token.isSessionValid()) {
                    LogUtil.d(TAG, "Success! " + token.toString());

                    mAccessToken = token;
                    mHandler.obtainMessage(MSG_FETCH_TOKEN_SUCCESS).sendToTarget();
                } else {
                    LogUtil.d(TAG, "Failed to receive access token");
                }
            }

            @Override
            public void onComplete4binary(ByteArrayOutputStream responseOS) {
                LogUtil.e(TAG, "onComplete4binary...");
                mHandler.obtainMessage(MSG_FETCH_TOKEN_FAILED).sendToTarget();
            }

            @Override
            public void onIOException(IOException e) {
                LogUtil.e(TAG, "onIOException： " + e.getMessage());
                mHandler.obtainMessage(MSG_FETCH_TOKEN_FAILED).sendToTarget();
            }

            @Override
            public void onError(WeiboException e) {
                LogUtil.e(TAG, "WeiboException： " + e.getMessage());
                mHandler.obtainMessage(MSG_FETCH_TOKEN_FAILED).sendToTarget();
            }
        });

    }
    public void fetchTokenAsync1() {
        //将token集成到用Wilddog中


       String token=  mAccessToken.getToken().toString();

        String uid= mAccessToken.getUid();

        AuthCredential weiboAuthCredential=WeiboAuthProvider.getCredential(token,uid);

        mWilddogAuth.signInWithCredential(weiboAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if(task.isSuccessful()){
                    WilddogUser wilddogUser=task.getResult().getWilddogUser();
                    mWilddogText.setText("Logged in as " + wilddogUser.getUid() + " (" + wilddogUser.getProviderId() + ")");
                }else {
                    Log.e("error--------", task.getException().toString());
                }
            }
        });


    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_logout) {
//            logout();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//    private void logout() {
//            /* logout of Wilddog */
//        mWilddogRef.unauth();
//    }

}








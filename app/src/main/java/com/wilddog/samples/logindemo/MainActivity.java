package com.wilddog.samples.logindemo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.credentialandprovider.AuthCredential;
import com.wilddog.wilddogauth.core.credentialandprovider.WeiXinAuthCredential;
import com.wilddog.wilddogauth.core.credentialandprovider.WeiXinAuthProvider;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogauth.model.WilddogUser;

import java.util.Map;

/**
 * Created by Jeen on 2015/9/24.
 *
 * This application demos the use of the Wilddog Login feature. It currently supports logging in
 * with weibo, QQ, Email/Password, and Anonymous providers.
 *
 * The methods in this class have been divided into sections based on providers (with a few
 * general methods).
 *
 */

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /* *************************************
     *              GENERAL                *
     ***************************************/
    /* TextView that is used to display information about the logged in user */
    private TextView mLoggedInStatusTextView;

    /* A dialog that is presented until the Wilddog authentication finished. */
    private ProgressDialog mAuthProgressDialog;

    /* A reference to the WilddogAuth */
    private WilddogAuth mWilddogAuth;

    /* Data from the authenticated user */
    private WilddogUser mUser;
    
    /* Listener for Wilddog session changes */
    private WilddogAuth.AuthStateListener mAuthStateListener;

    /* *************************************
     *              PASSWORD               *
     ***************************************/
    private Button mPasswordLoginButton;

    /* *************************************
     *            ANONYMOUSLY              *
     ***************************************/
    private Button mAnonymousLoginButton;

    /* *************************************
     *              WEIBO                  *
     ***************************************/
    private Button mWeiboButton;
    /* *************************************
     *              WEIBO                  *
     ***************************************/
    private Button mQQButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Load the view and display it */
        setContentView(R.layout.activity_main);

        /* Create the Wilddog ref that is used for all authentication with Wilddog */
        mWilddogAuth = WilddogAuth.getInstance();

        /* *************************************
         *               PASSWORD              *
         ***************************************/
        mPasswordLoginButton = (Button) findViewById(R.id.login_with_password);
        mPasswordLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginWithPassword();
            }
        });

        /* *************************************
         *              ANONYMOUSLY            *
         ***************************************/
        /* Load and setup the anonymous login button */
        mAnonymousLoginButton = (Button) findViewById(R.id.login_anonymously);
        mAnonymousLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginAnonymously();
            }
        });

        /* *************************************
         *              weibo                  *
         ***************************************/
        /* Load and setup the anonymous login button */
        mWeiboButton = (Button) findViewById(R.id.login_button_default);
        mWeiboButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, WeiboOAuthActivity.class);
                startActivity(intent);
            }
        });
        /* *************************************
         *              qq                     *
         ***************************************/
        /* Load and setup the anonymous login button */
        mQQButton = (Button) findViewById(R.id.login_with_qq);
        mQQButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, QQOAuthActivity.class);
                startActivity(intent);
            }
        });


        /* *************************************
         *               GENERAL               *
         ***************************************/
        mLoggedInStatusTextView = (TextView) findViewById(R.id.login_status);

        /* Setup the progress dialog that is displayed later when authenticating with Wilddog */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading");
        mAuthProgressDialog.setMessage("Authenticating with Wilddog...");
        mAuthProgressDialog.setCancelable(false);
        mAuthProgressDialog.show();

        mAuthStateListener = new WilddogAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(WilddogAuth wilddogAuth) {
                mAuthProgressDialog.hide();
                WilddogUser wilddogUser=wilddogAuth.getCurrentUser();
                if(wilddogUser!=null) {
                    setAuthenticatedUser(wilddogUser);
                }else {

                }
            }
        };
        /* Check if the user is authenticated with Wilddog already. If this is the case we can set the authenticated
         * user and hide hide any login buttons */
        mWilddogAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // if changing configurations, stop tracking wilddog session.
        mWilddogAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* If a user is currently authenticated, display a logout menu */
        if (this.mUser != null) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Unauthenticate from Wilddog and from providers where necessary.
     */
    private void logout() {
        if (this.mUser != null) {
            /* logout of Wilddog */
            mWilddogAuth.signOut();
            /* Update authenticated user and show login buttons */
            setAuthenticatedUser(null);
        }
    }

    /**
     * Once a user is logged in, take the mAuthData provided from Wilddog and "use" it.
     */
    private void setAuthenticatedUser(WilddogUser wilddogUser) {
        if (wilddogUser != null) {
            /* Hide all the login buttons */
            mPasswordLoginButton.setVisibility(View.GONE);
            mAnonymousLoginButton.setVisibility(View.GONE);
            mWeiboButton.setVisibility(View.GONE);
            mQQButton.setVisibility(View.GONE);
            mLoggedInStatusTextView.setVisibility(View.VISIBLE);
            /* show a provider specific status text */
            String name = null;
           String providerId=  wilddogUser.getProviderId();
            if (providerId.equals("weibo")
                    || providerId.equals("qq")) {
                name =  wilddogUser.getDisplayName();
            } else if (providerId.equals("anonymous")
                    || providerId.equals("password")) {
                name = wilddogUser.getUid();
            } else {
                Log.e(TAG, "Invalid provider: " + wilddogUser.getProviderId());
            }
            if (name != null) {
                mLoggedInStatusTextView.setText("Logged in as " + name + " (" + wilddogUser.getProviderId() + ")");
            }
        } else {
            /* No authenticated user show all the login buttons 登出后再次显示按钮*/
            mPasswordLoginButton.setVisibility(View.VISIBLE);
            mAnonymousLoginButton.setVisibility(View.VISIBLE);
            //add
            mWeiboButton.setVisibility(View.VISIBLE);
            mQQButton.setVisibility(View.VISIBLE);
            mLoggedInStatusTextView.setVisibility(View.GONE);
        }
        this.mUser = wilddogUser;
        /* invalidate options menu to hide/show the logout button */
        supportInvalidateOptionsMenu();
    }

    /**
     * Show errors to users
     */
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }



    /* ************************************
     *              PASSWORD              *
     **************************************
     */
    public void loginWithPassword() {
        mAuthProgressDialog.show();
//        TODO:  Replace into your account password

       mWilddogAuth.signInWithEmailAndPassword("email@test.com", "12345678").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                processTask(task);
            }
        });
    }

    /* ************************************
     *             ANONYMOUSLY            *
     **************************************
     */
    private void loginAnonymously() {
        mAuthProgressDialog.show();
        mWilddogAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
               processTask(task);
            }
        });

    }

    /*
    * 处理授权登录成功后的结果
    * */

    private void processTask(Task<AuthResult> task){
        if(task.isSuccessful()){
            mAuthProgressDialog.hide();
            Log.i(TAG, task.getResult().getWilddogUser().getProviderId() + " auth successful");
            setAuthenticatedUser(task.getResult().getWilddogUser());
        }else {
            mAuthProgressDialog.hide();
            showErrorDialog(task.getException().toString());
        }
    }

}

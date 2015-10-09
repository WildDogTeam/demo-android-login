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

import com.wilddog.client.AuthData;
import com.wilddog.client.Wilddog;
import com.wilddog.client.WilddogError;

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

    /* A reference to the Wilddog */
    private Wilddog mWilddogRef;

    /* Data from the authenticated user */
    private AuthData mAuthData;
    
    /* Listener for Wilddog session changes */
    private Wilddog.AuthStateListener mAuthStateListener;

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
        mWilddogRef = new Wilddog(getResources().getString(R.string.wilddog_url));

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

        mAuthStateListener = new Wilddog.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                mAuthProgressDialog.hide();
                setAuthenticatedUser(authData);
            }
        };
        /* Check if the user is authenticated with Wilddog already. If this is the case we can set the authenticated
         * user and hide hide any login buttons */
        mWilddogRef.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // if changing configurations, stop tracking wilddog session.
        mWilddogRef.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* If a user is currently authenticated, display a logout menu */
        if (this.mAuthData != null) {
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
        if (this.mAuthData != null) {
            /* logout of Wilddog */
            mWilddogRef.unauth();
            /* Update authenticated user and show login buttons */
            setAuthenticatedUser(null);
        }
    }

    /**
     * Once a user is logged in, take the mAuthData provided from Wilddog and "use" it.
     */
    private void setAuthenticatedUser(AuthData authData) {
        if (authData != null) {
            /* Hide all the login buttons */
            mPasswordLoginButton.setVisibility(View.GONE);
            mAnonymousLoginButton.setVisibility(View.GONE);
            mWeiboButton.setVisibility(View.GONE);
            mQQButton.setVisibility(View.GONE);
            mLoggedInStatusTextView.setVisibility(View.VISIBLE);
            /* show a provider specific status text */
            String name = null;
            if (authData.getProvider().equals("weibo")
                    || authData.getProvider().equals("qq")) {
                name = (String) authData.getProviderData().get("displayName");
            } else if (authData.getProvider().equals("anonymous")
                    || authData.getProvider().equals("password")) {
                name = authData.getUid();
            } else {
                Log.e(TAG, "Invalid provider: " + authData.getProvider());
            }
            if (name != null) {
                mLoggedInStatusTextView.setText("Logged in as " + name + " (" + authData.getProvider() + ")");
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
        this.mAuthData = authData;
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

    /**
     * Utility class for authentication results
     */
    private class AuthResultHandler implements Wilddog.AuthResultHandler {

        private final String provider;

        public AuthResultHandler(String provider) {
            this.provider = provider;
        }

        @Override
        public void onAuthenticated(AuthData authData) {
            mAuthProgressDialog.hide();
            Log.i(TAG, provider + " auth successful");
            setAuthenticatedUser(authData);
        }

        @Override
        public void onAuthenticationError(WilddogError wilddogError) {
            mAuthProgressDialog.hide();
            showErrorDialog(wilddogError.toString());
        }
    }

    /* ************************************
     *              PASSWORD              *
     **************************************
     */
    public void loginWithPassword() {
        mAuthProgressDialog.show();
//        TODO:  Replace into your account password
        mWilddogRef.authWithPassword("email@test.com", "12345678", new AuthResultHandler("password"));
    }

    /* ************************************
     *             ANONYMOUSLY            *
     **************************************
     */
    private void loginAnonymously() {
        mAuthProgressDialog.show();
        mWilddogRef.authAnonymously(new AuthResultHandler("anonymous"));

    }
}

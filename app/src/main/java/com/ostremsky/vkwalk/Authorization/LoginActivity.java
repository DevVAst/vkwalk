package com.ostremsky.vkwalk.Authorization;

/**
 * Created by DevAs on 03.07.2016.
 */

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ostremsky.vkwalk.InfoHelper;
import com.ostremsky.vkwalk.R;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.util.VKUtil;

import org.json.JSONException;

import java.io.IOException;


/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends FragmentActivity {

    private boolean isResumed = false;

    /**
     * Scope is set of required permissions for your application
     *
     * @see <a href="https://vk.com/dev/permissions">vk.com api permissions documentation</a>
     */
    private static final String[] sMyScope = new String[]{
            //VKScope.FRIENDS,
            //VKScope.WALL,
            //VKScope.PHOTOS,
            //VKScope.NOHTTPS,
            //VKScope.MESSAGES,
            //VKScope.DOCS,
            VKScope.GROUPS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        VKSdk.wakeUpSession(this, new VKCallback<VKSdk.LoginState>() {
            @Override
            public void onResult(VKSdk.LoginState res) {
                if (isResumed) {
                    switch (res) {
                        case LoggedOut:
                            showLogin();
                            break;
                        case LoggedIn:
                            showLogout();
                            break;
                        case Pending:
                            break;
                        case Unknown:
                            break;
                    }
                }
            }

            @Override
            public void onError(VKError error) {

            }
        });

       //String[] fingerprint = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        //Log.d("Fingerprint", fingerprint[0]);
    }

    private void showLogout() {
        startCheckSubscribe();
        /*
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new LogoutFragment())
                .commitAllowingStateLoss();*/
    }

    private void showLogin() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new LoginFragment())
                .commitAllowingStateLoss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;

        if (!VKSdk.isLoggedIn()) {
            showLogin();
        } else {

        }
    }

    @Override
    protected void onPause() {
        isResumed = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKCallback<VKAccessToken> callback = new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // User passed Authorization

                final VKRequest request =  VKApi.users().get(VKParameters.from(VKApiConst.ACCESS_TOKEN));
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        VKList users = (VKList) response.parsedModel;
                        try {
                            int userId = users.get(0).fields.getInt("id");
                            InfoHelper.setUserId(userId);
                            checkUserExists(String.valueOf(userId));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onError(VKError error) {
                // User didn't pass Authorization
            }
        };

        if (!VKSdk.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startCheckSubscribe() {
        final VKRequest request =  VKApi.users().get(VKParameters.from(VKApiConst.ACCESS_TOKEN));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKList users = (VKList) response.parsedModel;
                try {
                    int userId = users.get(0).fields.getInt("id");
                    InfoHelper.setUserId(userId);

                    startNextActivity(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static class LoginFragment extends android.support.v4.app.Fragment {
        public LoginFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_login, container, false);
            v.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKSdk.login(getActivity(), sMyScope);

                }
            });
            return v;
        }

    }

    public static class LogoutFragment extends android.support.v4.app.Fragment {
        public LogoutFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_logout, container, false);
            v.findViewById(R.id.continue_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((LoginActivity) getActivity()).startCheckSubscribe();
                }
            });

            v.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKSdk.logout();
                    if (!VKSdk.isLoggedIn()) {
                        ((LoginActivity) getActivity()).showLogin();
                    }
                }
            });
            return v;
        }
    }

    public void checkUserExists(String userId){
        String url = Uri.parse(InfoHelper.URL_I).buildUpon()
                .appendQueryParameter(InfoHelper.GET_METHOD, "getUserById")
                .appendQueryParameter("vk_id", userId)
                .build().toString();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    String result = response.body().string();
                    if(result.equals("null")){
                        startNextActivity(false);
                    }else {

                        startNextActivity(true);
                    }
                }
            }
        });
    }

    private void startNextActivity(boolean userExists){

        if(userExists){
            startActivity(new Intent(LoginActivity.this, AuthorizeActivity.class).putExtra(AuthorizeActivity.TAG, AuthorizeActivity.CHECK_SUBSCRIBE));
            finish();
        }else {
            startActivity(new Intent(LoginActivity.this, AuthorizeActivity.class).putExtra(AuthorizeActivity.TAG, AuthorizeActivity.SELECT_CITY));
            finish();
        }
    }

}

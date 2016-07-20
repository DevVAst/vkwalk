package com.ostremsky.vkwalk.Authorization;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.ostremsky.vkwalk.InfoHelper;
import com.ostremsky.vkwalk.R;
import com.ostremsky.vkwalk.WorkActivity;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class AuthorizeActivity extends AppCompatActivity {
    public static String LOG_TAG = "AuthorizeActivityTAG";
    public static String TAG = "int";
    public static String SELECT_CITY = "selectCity";
    public static String CHECK_SUBSCRIBE = "changeToSubscribe";
    private static String groupId;
    //private Drawer drawerResult = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorize);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
/*
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName("Mike Penz").withEmail("mikepenz@gmail.com").withIcon(R.drawable.face)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();*//*
        drawerResult = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.drawer_header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_home).withBadge("99").withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_free_play).withIcon(FontAwesome.Icon.faw_gamepad),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_custom).withIcon(FontAwesome.Icon.faw_eye).withBadge("6").withIdentifier(2),
                        new SectionDrawerItem().withName(R.string.drawer_item_settings),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_help).withIcon(FontAwesome.Icon.faw_cog),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_open_source).withIcon(FontAwesome.Icon.faw_question).withSetSelected(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_contact).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1)
                )
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        InputMethodManager inputMethodManager = (InputMethodManager) AuthorizeActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(AuthorizeActivity.this.getCurrentFocus().getWindowToken(), 0);
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {

                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {
                            Toast.makeText(AuthorizeActivity.this, ((Nameable) drawerItem).getName().toString(), Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }

                })
                .withOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem instanceof SecondaryDrawerItem) {
                            Toast.makeText(AuthorizeActivity.this, ((SecondaryDrawerItem) drawerItem).getName().toString(), Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }

                })
                .build();
*/

        if(savedInstanceState == null && getFragmentManager().findFragmentById(R.id.containerSelect) == null) {
            Intent intent = getIntent();
            String in = intent.getStringExtra(TAG);
            if (in.equals(SELECT_CITY)) {
                getFragmentManager().beginTransaction().add(R.id.containerSelect, new SelectCityFragment()).commit();
            }else if (in.equals(CHECK_SUBSCRIBE)) {
                checkSubscribe();
            }
        }
    }

    /*
    @Override
    public void onBackPressed() {
        // Закрываем Navigation Drawer по нажатию системной кнопки "Назад" если он открыт
        if (drawerResult.isDrawerOpen()) {
            drawerResult.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }*/

    public void checkSubscribe(){
        findGroupId();
    }
    private void findGroupId(){
        HttpUrl.Builder urlBuilder = HttpUrl.parse(InfoHelper.URL_I).newBuilder();
        urlBuilder.addQueryParameter(InfoHelper.GET_METHOD, "getCityGroupByUserId");
        Log.d(LOG_TAG, "InfoHelper.getUserId()" + InfoHelper.getUserId());
        urlBuilder.addQueryParameter("vk_id", String.valueOf(InfoHelper.getUserId()));
        String url = urlBuilder.build().toString();
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
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        JSONObject object = array.getJSONObject(0);
                        groupId = object.getString("group_id");
                        Log.d(LOG_TAG, "groupId=" + groupId);
                        if (groupId.equals("0"))
                            startMainActivity();
                        else
                            checkIsMember();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private void checkIsMember(){
        VKRequest isMember = VKApi.groups().isMember(VKParameters.from(VKApiConst.GROUP_ID, groupId, VKApiConst.USER_ID, InfoHelper.getUserId()));
        isMember.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    int res = response.json.getInt("response");
                    if (res == 1) {
                        startMainActivity();
                    } else {
                        CheckSubscribeFragment fragment = new CheckSubscribeFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("groupId", groupId);
                        fragment.setArguments(bundle);

                        if(getFragmentManager().findFragmentById(R.id.containerSelect) == null)
                            getFragmentManager().beginTransaction().replace(R.id.containerSelect, fragment).commit();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void startMainActivity(){
        startActivity(new Intent(AuthorizeActivity.this, WorkActivity.class));
        finish();
    }
}

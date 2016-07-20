package com.ostremsky.vkwalk;


/**
 * Created by DevAs on 03.07.2016.
 */

public class InfoHelper {
    private static int userId = -1;
    public static void setUserId(int userId){
        InfoHelper.userId = userId;
    }
    public static int getUserId(){
        return userId;
    }

    public static String URL_I = "http://devas.xyz/APIwalk.php";
    public static String GET_METHOD = "method";
}

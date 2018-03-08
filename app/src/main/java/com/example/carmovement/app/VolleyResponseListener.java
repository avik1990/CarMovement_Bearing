package com.example.carmovement.app;

import com.android.volley.VolleyError;

/**
 * Created by Uzibaba on 8/7/2016.
 */
public interface VolleyResponseListener {

    public String onSuccess(String response,String type);
    public String onFailure(VolleyError error);
}

package com.example.carmovement.app;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Uzibaba on 8/7/2016.
 */
public class VolleyHandler {

    String mUrl;
    int method;
    Map<String,String> params;
    VolleyResponseListener mResponse;
    String req_name;

    public VolleyHandler(VolleyResponseListener mResponse,String mUrl, int method,Map<String,String> params,String req_name) {
        this.mUrl = mUrl;
        this.method = method;
        this.params=params;
        this.mResponse=mResponse;
        this.req_name=req_name;
    }

    public void makeStringReq()
    {
        StringRequest strRequest = new StringRequest(method, mUrl,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        mResponse.onSuccess(response,req_name);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        mResponse.onFailure(error);
                    }
                })

        {
            @Override
            protected Map<String, String> getParams()
            {
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest,
                "login");
    }


    private void makeJsonObjReq() {
        Map<String, String> postParam = new HashMap<String, String>();
        postParam.put("lat", "22.236444");
        postParam.put("lon", "88.325647");


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                "Const.URL_LOGIN", new JSONObject(postParam),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response.....", response.toString());
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Response.....", "Error: " + error.getMessage());
//                hideProgressDialog();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }


        };
        AppController.getInstance().addToRequestQueue(jsonObjReq,
                "login");
    }
}

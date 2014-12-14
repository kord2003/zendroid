package com.zendroid;

import org.json.JSONObject;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WebRequestHelper {

	private static final String DEFAULT_ZEN_URL = "https://docs.google.com/uc?id=0B5-hwnDYG3RtVy1fY3Z3cjV0UVE&export=download";

	private RequestQueue requestQueue;

	private Gson gson;

	public WebRequestHelper(Context context) {
		requestQueue = Volley.newRequestQueue(context);
		gson = new Gson();
	}

	public void sendZenRequest(String param,
			final OnResponseListener<ZenResponse> listener) {
		String url = DEFAULT_ZEN_URL;
		JsonObjectRequest request = new JsonObjectRequest(Method.GET, url,
				null, new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						ZenResponse zr = gson.fromJson(response.toString(),
								ZenResponse.class);
						listener.onSuccess(zr);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						listener.onError(error);
					}
				});
		requestQueue.add(request);
	}

}

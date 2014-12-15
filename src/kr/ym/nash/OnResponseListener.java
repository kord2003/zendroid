package kr.ym.nash;

import com.android.volley.VolleyError;

public interface OnResponseListener<T> {
	public void onSuccess(T data);
	public void onError(VolleyError error);
}

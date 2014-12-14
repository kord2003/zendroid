package com.zendroid;

import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.zendroid.util.SystemUiHider;

public class FullscreenActivity extends Activity implements Callback {

	//russia_soul_3s.mp4
	//test_video.mkv
	private static final String DEFAULT_MEDIA_FILE = "russia_soul_3s.mp4";

	private static final boolean AUTO_HIDE = true;

	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	private static final int DEFAULT_UPDATE_ZEN_VALUES_DELAY = 5000;

	private static final boolean TOGGLE_ON_CLICK = true;

	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	private static final String TAG = FullscreenActivity.class.getName();

	private SystemUiHider mSystemUiHider;

	private WebRequestHelper webRequestHelper;

	private SurfaceView surfaceView;

	private MediaPlayer mediaPlayer;

	private SurfaceHolder surfaceHolder;

	private Handler handler = new Handler();

	private Runnable hideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	private Runnable updateZenValuesRunnable = new Runnable() {
		@Override
		public void run() {
			updateZenValues();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);
		
		tvUsd = (TextView) findViewById(R.id.tvUsd);
		TypefaceUtils.setCustomTypeface(tvUsd);
		tvEur = (TextView) findViewById(R.id.tvEur);
		TypefaceUtils.setCustomTypeface(tvEur);
		tvBrent = (TextView) findViewById(R.id.tvBrent);
		TypefaceUtils.setCustomTypeface(tvBrent);
		

		TextView tvUsdLabel = (TextView) findViewById(R.id.tvUsdLabel);
		TypefaceUtils.setCustomTypeface(tvUsdLabel);
		TextView tvEurLabel = (TextView) findViewById(R.id.tvEurLabel);
		TypefaceUtils.setCustomTypeface(tvEurLabel);
		TextView tvBrentLabel = (TextView) findViewById(R.id.tvBrentLabel);
		TypefaceUtils.setCustomTypeface(tvBrentLabel);
		

		webRequestHelper = new WebRequestHelper(getApplicationContext());

		if (surfaceView == null) {
			surfaceView = (SurfaceView) findViewById(R.id.sfView);
		}
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setLooping(true);

			getWindow().setFormat(PixelFormat.UNKNOWN);

			surfaceHolder = surfaceView.getHolder();
			surfaceHolder.addCallback(this);
			surfaceHolder.setFixedSize(176, 144);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		final View containerDummyButton = findViewById(R.id.containerDummyButton);
		final View containerTouchZone = findViewById(R.id.containerTouchZone);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, containerTouchZone,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = containerDummyButton
										.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							containerDummyButton
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							containerDummyButton
									.setVisibility(visible ? View.VISIBLE
											: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		containerTouchZone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.btnDummy).setOnTouchListener(mDelayHideTouchListener);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	private int currentPosition;

	private boolean isPrepared = false;

	private boolean isZenAutoUpdateEnabled = true;

	private TextView tvUsd;

	private TextView tvEur;

	private TextView tvBrent;

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		handler.removeCallbacks(hideRunnable);
		handler.postDelayed(hideRunnable, delayMillis);
	}

	@Override
	protected void onResume() {
		super.onResume();		

		if (isPrepared) {
			startVideo();
		} else {
			initVideo();
		}
		isZenAutoUpdateEnabled = true;
		updateZenValues();
	}

	@Override
	protected void onPause() {
		super.onPause();
		pauseVideo();
		isZenAutoUpdateEnabled = false;
		stopUpdatingZenValues();
	}

	private void initVideo() {
		//Log.d(TAG, "initVideo()");
		surfaceView.post(new Runnable() {
			@Override
			public void run() {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.reset();
				}

				mediaPlayer.setDisplay(surfaceHolder);

				try {
					AssetFileDescriptor afd = getAssets().openFd(DEFAULT_MEDIA_FILE);
					
					mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

					mediaPlayer.prepareAsync();
					mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							//Log.d(TAG, "onPrepared()");
							isPrepared = true;
							startVideo();
						}
					});
					mediaPlayer
							.setOnSeekCompleteListener(new OnSeekCompleteListener() {
								@Override
								public void onSeekComplete(MediaPlayer mp) {
									//Log.d(TAG, "onSeekComplete()");
								}
							});
					mediaPlayer.setOnInfoListener(new OnInfoListener() {
						@Override
						public boolean onInfo(MediaPlayer mp, int what,
								int extra) {
							//Log.d(TAG, "onInfo() what = " + what + ", extra = " + extra);
							return false;
						}
					});
					mediaPlayer
							.setOnCompletionListener(new OnCompletionListener() {
								@Override
								public void onCompletion(MediaPlayer mp) {
									//Log.d(TAG, "onCompletion()");
								}
							});
					mediaPlayer.setOnErrorListener(new OnErrorListener() {
						@Override
						public boolean onError(MediaPlayer mp, int what,
								int extra) {
							//Log.d(TAG, "onError() what = " + what + ", extra = " + extra);
							return false;
						}
					});
				} catch (IllegalArgumentException e) {
					FileLogger.appendLog(getApplicationContext(),
							"Error: " + e.getMessage());
					e.printStackTrace();
				} catch (IllegalStateException e) {
					FileLogger.appendLog(getApplicationContext(),
							"Error: " + e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					FileLogger.appendLog(getApplicationContext(),
							"Error: " + e.getMessage());
					e.printStackTrace();
				}

			}
		});
	}

	private void startVideo() {
		//Log.d(TAG, "startVideo() currentPosition = " + currentPosition);
		surfaceView.post(new Runnable() {
			@Override
			public void run() {
				if (currentPosition == 0) {
					currentPosition = 1;
				}
				mediaPlayer.setDisplay(surfaceHolder);
				mediaPlayer.seekTo(currentPosition);
				mediaPlayer.start();
			}
		});
	}

	private void pauseVideo() {
		currentPosition = mediaPlayer.getCurrentPosition();
		mediaPlayer.pause();
		//Log.d(TAG, "pauseVideo() currentPosition = " + currentPosition);
	}

	private synchronized void startUpdatingZenValues() {
		//Log.d(TAG, "startUpdatingZenValues");
		isZenAutoUpdateEnabled = true;
		handler.removeCallbacks(updateZenValuesRunnable);
		handler.postDelayed(updateZenValuesRunnable,
				DEFAULT_UPDATE_ZEN_VALUES_DELAY);
	}

	private synchronized void stopUpdatingZenValues() {
		//Log.d(TAG, "stopUpdatingZenValues");
		handler.removeCallbacks(updateZenValuesRunnable);
	}

	private void updateZenValues() {
		webRequestHelper.sendZenRequest(null,
				new OnResponseListener<ZenResponse>() {
					@Override
					public void onSuccess(ZenResponse data) {
						FileLogger.appendLog(getApplicationContext(), "u = "
								+ data.getUsd() + ", e = " + data.getEur()
								+ ", b = " + data.getBrent());
						tvUsd.setText(data.getUsd());
						tvEur.setText(data.getEur());
						tvBrent.setText(data.getBrent());
						
						if (isZenAutoUpdateEnabled) {
							startUpdatingZenValues();
						}
					}

					@Override
					public void onError(VolleyError error) {
						FileLogger.appendLog(getApplicationContext(),
								"[Error] " + error.getMessage());
					}
				});
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected void onDestroy() {
		mediaPlayer.release();
		mediaPlayer = null;
		
		super.onDestroy();
	}
}
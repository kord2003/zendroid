package kr.ym.nash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.zendroid.util.SystemUiHider;

public class FullscreenActivity extends Activity implements Callback {

	// russia_soul_3s.mp4
	// test_video.mkv
	// russian_soul_114frames.m4v
	private static final String DEFAULT_VIDEO_FILE = "russian_soul_112frames.m4v";

	// Lindsey__Stirling-Roundtable_Rival.mp3
	private static final String DEFAULT_AUDIO_FILE = "russian_soul_audio.ogg";

	private static final boolean AUTO_HIDE = true;

	private static final int AUTO_HIDE_DELAY_MILLIS = 7000;

	private static final int DEFAULT_UPDATE_ZEN_VALUES_DELAY = 30000;

	private static final boolean TOGGLE_ON_CLICK = true;

	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	private static final String TAG = FullscreenActivity.class.getName();

	private SystemUiHider mSystemUiHider;

	private WebRequestHelper webRequestHelper;

	private SurfaceView surfaceView;

	private MediaPlayer mediaPlayerVideo;

	private MediaPlayer mediaPlayerAudio;

	private SurfaceHolder surfaceHolder;

	private Handler handler = new Handler();

	private View containerZenValues;

	private View containerTouchZone;

	private ShareActionProvider mShareActionProvider;

	private int currentVideoPosition;

	private int currentAudioPosition;

	private boolean isVideoPrepared = false;

	private boolean isAudioPrepared = false;

	private boolean isZenAutoUpdateEnabled = true;

	private TextView tvUsd;

	private TextView tvEur;

	private TextView tvBrent;

	private String shareString;

	private TextView tvErrorMessage;

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

		String sharePostfix = getString(R.string.share_postfix);
		shareString = sharePostfix;

		final View containerDummyButton = findViewById(R.id.containerDummyButton);
		containerZenValues = findViewById(R.id.containerZenValues);
		containerTouchZone = findViewById(R.id.containerTouchZone);

		tvErrorMessage = (TextView) findViewById(R.id.tvErrorMessage);
		TypefaceUtils.setCustomTypeface(tvErrorMessage);

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
		if (mediaPlayerVideo == null) {
			mediaPlayerVideo = new MediaPlayer();
			mediaPlayerVideo.setLooping(true);

			getWindow().setFormat(PixelFormat.UNKNOWN);

			surfaceHolder = surfaceView.getHolder();
			surfaceHolder.addCallback(this);
			surfaceHolder.setFixedSize(176, 144);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		if (mediaPlayerAudio == null) {
			mediaPlayerAudio = new MediaPlayer();
			mediaPlayerAudio.setLooping(true);
		}

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, containerTouchZone, HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
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
						mControlsHeight = containerDummyButton.getHeight();
					}
					if (mShortAnimTime == 0) {
						mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
					}
					containerDummyButton.animate().translationY(visible ? 0 : mControlsHeight)
							.setDuration(mShortAnimTime);
				} else {
					// If the ViewPropertyAnimator APIs aren't
					// available, simply show or hide the in-layout UI
					// controls.
					containerDummyButton.setVisibility(visible ? View.VISIBLE : View.GONE);
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

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate menu resource file.
		getMenuInflater().inflate(R.menu.menu_share, menu);

		// Locate MenuItem with ShareActionProvider
		MenuItem item = menu.findItem(R.id.menu_item_share);

		// Fetch and store ShareActionProvider
		mShareActionProvider = (ShareActionProvider) item.getActionProvider();

		// Return true to display menu
		return true;
	}

	private Intent createShareIntent() {
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);

		/*Bitmap bitmap = loadBitmapFromView(containerTouchZone);
		String localAbsoluteFilePath = saveImageLocally(bitmap);

		Log.d(TAG, "localAbsoluteFilePath = " + localAbsoluteFilePath);
		if (localAbsoluteFilePath != null && localAbsoluteFilePath != "") {
			Uri phototUri = Uri.parse(localAbsoluteFilePath);

			File file = new File(phototUri.getPath());

			Log.d(TAG, "file path: " + file.getPath());
			sharingIntent.setData(phototUri);
			sharingIntent.setType("image/png");
			sharingIntent.putExtra(Intent.EXTRA_STREAM, phototUri);
		}*/

		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareString);
		return sharingIntent;
	}

	private Bitmap loadBitmapFromView(View v) {
		Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
		v.draw(c);
		return b;
	}

	private String saveImageLocally(Bitmap bitmap) {
		//Log.d(TAG, "saveImageLocally = " + bitmap.getWidth());
		File outputDir = getExternalCacheDir();
		File outputFile = null;
		try {
			outputFile = File.createTempFile("tmp", ".png", outputDir);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			FileOutputStream out = new FileOutputStream(outputFile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return outputFile.getAbsolutePath();
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

		if (isVideoPrepared && isAudioPrepared) {
			startVideo();
			startAudio();
		} else {
			initMediaPlayers();
		}
		isZenAutoUpdateEnabled = true;
		updateZenValues();
	}

	@Override
	protected void onPause() {
		super.onPause();
		pauseVideo();
		pauseAudio();
		isZenAutoUpdateEnabled = false;
		stopUpdatingZenValues();
	}

	private void initMediaPlayers() {
		// Log.d(TAG, "initVideo()");
		surfaceView.post(new Runnable() {
			@Override
			public void run() {
				initVideoPlayer();
				initAudioPlayer();
			}
		});
	}

	private void startVideo() {
		// Log.d(TAG, "startVideo() currentPosition = " + currentPosition);
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (currentVideoPosition == 0) {
					currentVideoPosition = 1;
				}
				mediaPlayerVideo.setDisplay(surfaceHolder);
				mediaPlayerVideo.seekTo(currentVideoPosition);
				mediaPlayerVideo.start();
			}
		});
	}

	private void pauseVideo() {
		currentVideoPosition = mediaPlayerVideo.getCurrentPosition();
		mediaPlayerVideo.pause();
		// Log.d(TAG, "pauseVideo() currentPosition = " + currentPosition);
	}

	private void startAudio() {
		// Log.d(TAG, "startVideo() currentPosition = " + currentPosition);
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (currentAudioPosition == 0) {
					currentAudioPosition = 1;
				}
				mediaPlayerAudio.seekTo(currentAudioPosition);
				mediaPlayerAudio.start();
			}
		});
	}

	private void pauseAudio() {
		currentAudioPosition = mediaPlayerAudio.getCurrentPosition();
		mediaPlayerAudio.pause();
		// Log.d(TAG, "pauseVideo() currentPosition = " + currentPosition);
	}

	private synchronized void startUpdatingZenValues() {
		// Log.d(TAG, "startUpdatingZenValues");
		isZenAutoUpdateEnabled = true;
		handler.removeCallbacks(updateZenValuesRunnable);
		handler.postDelayed(updateZenValuesRunnable, DEFAULT_UPDATE_ZEN_VALUES_DELAY);
	}

	private synchronized void stopUpdatingZenValues() {
		// Log.d(TAG, "stopUpdatingZenValues");
		handler.removeCallbacks(updateZenValuesRunnable);
	}

	private void updateZenValues() {
		webRequestHelper.sendZenRequest(null, new OnResponseListener<ZenResponse>() {
			@Override
			public void onSuccess(ZenResponse data) {
				FileLogger.appendLog(getApplicationContext(), "u = " + data.getUsd() + ", e = " + data.getEur()
						+ ", b = " + data.getBrent());

				String sharePrefix = getString(R.string.share_prefix);
				String sharePostfix = getString(R.string.share_postfix);
				String usd = getString(R.string.usd_label);
				String eur = getString(R.string.eur_label);
				String brent = getString(R.string.brent_label);
				shareString = sharePrefix + " " + usd + " = " + data.getUsd() + ", " + eur + " = " + data.getEur()
						+ ", " + brent + " = " + data.getBrent() + ".\r\n" + sharePostfix;
				if (mShareActionProvider != null) {
					mShareActionProvider.setShareIntent(createShareIntent());
				}
				tvUsd.setText(data.getUsd());
				tvEur.setText(data.getEur());
				tvBrent.setText(data.getBrent());

				containerZenValues.setVisibility(View.VISIBLE);
				tvErrorMessage.setVisibility(View.GONE);
				if (isZenAutoUpdateEnabled) {
					startUpdatingZenValues();
				}
			}

			@Override
			public void onError(VolleyError error) {
				FileLogger.appendLog(getApplicationContext(), "[Error] " + error.getMessage());
				containerZenValues.setVisibility(View.GONE);
				tvErrorMessage.setVisibility(View.VISIBLE);
				if (isZenAutoUpdateEnabled) {
					startUpdatingZenValues();
				}
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
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		switchScreenMode();
	}

	private void switchScreenMode() {
		int surfaceViewWidth = surfaceView.getWidth();
		int surfaceViewHeight = surfaceView.getHeight();

		int videoWidth = mediaPlayerVideo.getVideoWidth();
		int videoHeight = mediaPlayerVideo.getVideoHeight();

		float ratioWidth = surfaceViewWidth / videoWidth;
		float ratioHeight = surfaceViewHeight / videoHeight;
		// float aspectRatio = videoWidth/videoHeight;

		LayoutParams layoutParams = (LayoutParams) surfaceView.getLayoutParams();

		// Log.d(TAG, "switchScreenMode videoWidth = " + videoWidth +
		// ", videoHeight = " + videoHeight + ", aspectRatio = " + aspectRatio);
		if (ratioWidth > ratioHeight) {

			layoutParams.width = surfaceViewWidth;
			layoutParams.height = (int) (videoHeight * ratioWidth);
		} else {
			layoutParams.width = (int) (videoWidth * ratioHeight);
			layoutParams.height = surfaceViewHeight;
		}

		// Log.d(TAG, "switchScreenMode layoutParams.width = " +
		// layoutParams.width + ", layoutParams.height = " +
		// layoutParams.height);

		surfaceView.setLayoutParams(layoutParams);
	}

	@Override
	protected void onDestroy() {
		mediaPlayerVideo.release();
		mediaPlayerVideo = null;

		mediaPlayerAudio.release();
		mediaPlayerAudio = null;

		super.onDestroy();
	}

	private void initVideoPlayer() {
		if (mediaPlayerVideo.isPlaying()) {
			mediaPlayerVideo.reset();
		}

		mediaPlayerVideo.setDisplay(surfaceHolder);

		try {
			AssetFileDescriptor videoAssetFileDescriptor = getAssets().openFd(DEFAULT_VIDEO_FILE);

			mediaPlayerVideo.setDataSource(videoAssetFileDescriptor.getFileDescriptor(),
					videoAssetFileDescriptor.getStartOffset(), videoAssetFileDescriptor.getLength());
			mediaPlayerVideo.prepareAsync();
			mediaPlayerVideo.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					// Log.d(TAG, "onPrepared()");
					isVideoPrepared = true;
					mediaPlayerVideo.setVolume(0f, 0f);
					switchScreenMode();
					startVideo();
				}
			});
			mediaPlayerVideo.setOnSeekCompleteListener(new OnSeekCompleteListener() {
				@Override
				public void onSeekComplete(MediaPlayer mp) {
					// Log.d(TAG, "onSeekComplete()");
				}
			});
			mediaPlayerVideo.setOnInfoListener(new OnInfoListener() {
				@Override
				public boolean onInfo(MediaPlayer mp, int what, int extra) {
					// Log.d(TAG, "onInfo() what = " + what + ", extra = " +
					// extra);
					return false;
				}
			});
			mediaPlayerVideo.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					// Log.d(TAG, "onCompletion()");
				}
			});
			mediaPlayerVideo.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					// Log.d(TAG, "onError() what = " + what + ", extra = " +
					// extra);
					return false;
				}
			});
		} catch (IllegalArgumentException e) {
			FileLogger.appendLog(getApplicationContext(), "Error: " + e.getMessage());
			e.printStackTrace();
		} catch (IllegalStateException e) {
			FileLogger.appendLog(getApplicationContext(), "Error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			FileLogger.appendLog(getApplicationContext(), "Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void initAudioPlayer() {
		if (mediaPlayerAudio.isPlaying()) {
			mediaPlayerAudio.reset();
		}

		try {
			AssetFileDescriptor audioAssetFileDescriptor = getAssets().openFd(DEFAULT_AUDIO_FILE);

			mediaPlayerAudio.setDataSource(audioAssetFileDescriptor.getFileDescriptor(),
					audioAssetFileDescriptor.getStartOffset(), audioAssetFileDescriptor.getLength());
			mediaPlayerAudio.prepareAsync();
			mediaPlayerAudio.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					// Log.d(TAG, "onPrepared()");
					isAudioPrepared = true;
					startAudio();
				}
			});
			mediaPlayerAudio.setOnSeekCompleteListener(new OnSeekCompleteListener() {
				@Override
				public void onSeekComplete(MediaPlayer mp) {
					// Log.d(TAG, "onSeekComplete()");
				}
			});
			mediaPlayerAudio.setOnInfoListener(new OnInfoListener() {
				@Override
				public boolean onInfo(MediaPlayer mp, int what, int extra) {
					// Log.d(TAG, "onInfo() what = " + what + ", extra = " +
					// extra);
					return false;
				}
			});
			mediaPlayerAudio.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					// Log.d(TAG, "onCompletion()");
				}
			});
			mediaPlayerAudio.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					// Log.d(TAG, "onError() what = " + what + ", extra = " +
					// extra);
					return false;
				}
			});
		} catch (IllegalArgumentException e) {
			FileLogger.appendLog(getApplicationContext(), "Error: " + e.getMessage());
			e.printStackTrace();
		} catch (IllegalStateException e) {
			FileLogger.appendLog(getApplicationContext(), "Error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			FileLogger.appendLog(getApplicationContext(), "Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}

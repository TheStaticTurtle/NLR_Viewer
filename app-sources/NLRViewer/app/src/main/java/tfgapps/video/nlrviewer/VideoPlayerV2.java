package tfgapps.video.nlrviewer;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.halilibo.bettervideoplayer.BetterVideoCallback;
import com.halilibo.bettervideoplayer.BetterVideoPlayer;
import com.halilibo.bettervideoplayer.BetterVideoProgressCallback;
import com.halilibo.bettervideoplayer.subtitle.CaptionsView;

import java.net.ProtocolException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoPlayerV2 extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private BetterVideoPlayer mBetterVideoPlayer;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mBetterVideoPlayer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mVisible = true;
        Bundle b = getIntent().getExtras();
        String vidurl ="";
        String suburl ="";
        String title ="";
        Integer theme = R.style.FullscreenTheme;
        try { vidurl = b.getString("vidurl");  } catch (Exception e) { Log.e("VideoPlayerV2.oncreate","vidurl",e); }
        try { suburl = b.getString("suburl");  } catch (Exception e) { Log.e("VideoPlayerV2.oncreate","suburl",e); }
        try { title = b.getString("title");  } catch (Exception e) { Log.e("VideoPlayerV2.oncreate","title",e); }
        try { theme = b.getInt("theme");  } catch (Exception e) { Log.e("VideoPlayerV2.oncreate","theme",e); }

        Log.wtf("videosub",suburl);
        setTheme(theme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player_v2);

        mBetterVideoPlayer = (BetterVideoPlayer) findViewById(R.id.bvp);

        setupPlayer(vidurl,suburl,title,0);

        // Set up the user interaction to manually show or hide the system UI.
        /*mBetterVideoPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });*/

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    int IMAt=0;
    int IMAt_old=0;
    int RescaleCouter=0;
    void setupPlayer(final String v,final String u,String t, final int start) {
        RescaleCouter=0;
        String vidurl =v;
        String suburl =u;
        String title =t;
        IMAt=0;
        mBetterVideoPlayer = (BetterVideoPlayer) findViewById(R.id.bvp);
        mBetterVideoPlayer.reset();
        mBetterVideoPlayer.setInitialPosition(start);
        mBetterVideoPlayer.setInitialPosition(start);
        mBetterVideoPlayer.setSource(Uri.parse(vidurl));
        mBetterVideoPlayer.enableSwipeGestures(getWindow());
        mBetterVideoPlayer.enableDoubleTapGestures(getResources().getInteger(R.integer.video_DoubleTapSeekToXMs));
        if(suburl.contains("http://") || suburl.contains("https://")) {
            mBetterVideoPlayer.setCaptions(Uri.parse(suburl), CaptionsView.CMime.SUBRIP);
            mBetterVideoPlayer.getToolbar().inflateMenu(R.menu.activity_videoplayer_menu_sub);
        } else {
            mBetterVideoPlayer.getToolbar().inflateMenu(R.menu.activity_videoplayer_menu_nosub);
        }
        final String suburlF=suburl;
        final String vidurlF=vidurl;

        mBetterVideoPlayer.getToolbar().setOnMenuItemClickListener(new android.support.v7.widget.Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_vid_activateSubs:
                        if(suburl.contains("http://") || suburl.contains("https://")) { mBetterVideoPlayer.setCaptions(Uri.parse(suburlF), CaptionsView.CMime.SUBRIP); }
                        break;
                    case R.id.nav_vid_desactivateSubs:
                        mBetterVideoPlayer.removeCaptions();
                        break;
                    case R.id.nav_vid_hide:
                        mBetterVideoPlayer.removeCaptions();
                        break;
                    case R.id.nav_vid_rescale:
                        rescalePlayer(mBetterVideoPlayer);
                        break;
                }
                hide();
                return false;
            }
        });
        mBetterVideoPlayer.setProgressCallback(new BetterVideoProgressCallback() {
            @Override
            public void onVideoProgressUpdate(int position, int duration) {
                if(position > IMAt_old) {
                    IMAt = position;
                    IMAt_old = position;
                    Log.v("onVideoProgressUpdate",""+position);
                }
            }
        });
        mBetterVideoPlayer.setCallback(new BetterVideoCallback() {
            @Override public void onStarted(BetterVideoPlayer player) { rescalePlayer(player);  Log.i("mBetterVideoPlayer", "onStarted");  }
            @Override public void onPaused(BetterVideoPlayer player) { rescalePlayer(player); Log.i("mBetterVideoPlayer", "onPaused");  }
            @Override public void onPreparing(BetterVideoPlayer player) { Log.i("mBetterVideoPlayer", "onPreparing"); }
            @Override public void onPrepared(BetterVideoPlayer player) {    Log.i("mBetterVideoPlayer", "onPrepared"); player.seekTo(start); /*TODO: Auto scale video*/ }
            @Override public void onBuffering(int percent) {
                Log.i("mBetterVideoPlayer", "onBuffering: "+percent+"%");
                if(RescaleCouter == 0) {
                    rescalePlayer(mBetterVideoPlayer);
                    Log.d("mBetterVideoPlayer", "Auto-Rescaling video");
                    RescaleCouter = RescaleCouter +1;
                } else {
                    RescaleCouter = RescaleCouter +1;
                    if(RescaleCouter>2) {RescaleCouter=0;} else { Log.d("mBetterVideoPlayer", "notRescalingYet: "+RescaleCouter); }
                }
            }

            @Override
            public void onError(BetterVideoPlayer player, Exception e) {
                //Log.i(TAG, "Error " +e.getMessage());
                if(e.getMessage().contains("died")) {
                    Log.wtf("OnError",e);
                    Log.e("VideoPlayerV2","ServerDied error restarting at currentpos:"+IMAt);
                    Toast to = Toast.makeText(VideoPlayerV2.this ,getResources().getString(R.string.video_reloaderror),Toast.LENGTH_LONG);
                    to.show();
                    setupPlayer(vidurlF,suburlF,t,IMAt);
                    hide();
                }
            }

            @Override
            public void onCompletion(BetterVideoPlayer player) {
                if(player.getCurrentPosition()+2000 < player.getDuration()) {
                    Log.e("VideoPlayerV2","onCompletion: currentPos != duration restarting at currentpos:"+IMAt+" /"+player.getDuration());
                    Toast to = Toast.makeText(VideoPlayerV2.this ,getResources().getString(R.string.video_reloaderror),Toast.LENGTH_LONG);
                    to.show();
                    int wasAt = player.getCurrentPosition();
                    setupPlayer(vidurlF,suburlF,t,IMAt-2);
                    hide();
                    return;
                }
                Log.i("mBetterVideoPlayer", "onCompletion");
            }

            @Override
            public void onToggleControls(BetterVideoPlayer player, boolean isShowing) {
                rescalePlayer(player);
            }

        });

        mBetterVideoPlayer.getToolbar().setTitle(title);
        mBetterVideoPlayer.getToolbar()
                .setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
        mBetterVideoPlayer.getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    void rescalePlayer(BetterVideoPlayer player) {
        FrameLayout lay = findViewById(R.id.layout);
        TextureView tex = findViewById(R.id.scaleThisFuckingVideo);
        player.onSurfaceTextureSizeChanged(tex.getSurfaceTexture(),lay.getWidth(),lay.getHeight());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mBetterVideoPlayer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}

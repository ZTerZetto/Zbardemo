package com.example.zzx.zbar_demo.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.entity.AppConfig;
import com.pili.pldroid.player.IMediaController;
import com.pili.pldroid.player.widget.PLVideoTextureView;


/**
 * Created by pengchenghu on 2019/2/27.
 * Author Email: 15651851181@163.com
 * Describe: 视频控制栏
 */
public class CustomMediaController extends FrameLayout implements IMediaController {
    private static final String TAG = "PLMediaController";

    private IMediaController.MediaPlayerControl mPlayer;
    private Context mContext;
    private Context mParentContext;
    private PopupWindow mWindow;
    private int mAnimStyle;  // 动画样式
    private View mAnchor;     // 锚点
    private View mRoot;       // 控制栏页面
    private int mScreenHeight; // 屏幕高度
    private int mScreenWidth;  // 屏幕宽度
    private int mStatusBarHeight; // 状态栏高度
    private boolean mShowing;  // 控制栏是否显示
    private boolean mIsFullScreen; // 是否全屏
    private static int sDefaultTimeout = 3000;

    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private boolean mFromXml = false;
    private RelativeLayout mMediaControllerAll; // 弹窗
    private TextView mFileNameTextView;  // 视频名称
    private ImageView mBackImageView;  // 返回
    private ImageView mPauseImageView; // 暂停播放
    private ImageView mCaptureImageView; // 截屏
    private ImageView mScreenImageView; // 全屏/还原

    private AudioManager mAM;

    private PLVideoTextureView mPlVideoTextureView;

    // handler消息处理
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long pos;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
            }
        }
    };

    public CustomMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mFromXml = true;
        initController(context);
    }

    // 构造函数
    public CustomMediaController(Context context) {
        super(context);
        getScreenInfo();  // 获取屏幕尺寸
        mParentContext = context;
        if (!mFromXml && initController(context))
            initFloatingWindow();
    }

    // 初始化上下文环境
    private boolean initController(Context context) {
        mContext = context.getApplicationContext();
        mAM = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        return true;
    }

    // 当View中所有的子控件均被映射成xml后触发
    @Override
    public void onFinishInflate() {
        if (mRoot != null)
            initControllerView(mRoot);
        super.onFinishInflate();
    }

    private void initFloatingWindow() {
        mWindow = new PopupWindow(mContext);
        mWindow.setFocusable(false);
        mWindow.setBackgroundDrawable(null);
        mWindow.setOutsideTouchable(true);
        mAnimStyle = android.R.style.Animation;
    }

    /**
     * Create the view that holds the widgets that control playback. Derived
     * classes can override this to create their own.
     *
     * @return The controller view.
     */
    protected View makeControllerView() {
        return ((LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(getResources().getIdentifier( // 布局
                "media_controller", "layout", mContext.getPackageName()), this);
    }
    private void initControllerView(View v) {
        mMediaControllerAll = (RelativeLayout) v.findViewById(getResources().getIdentifier(
                "mediacontroller_all", "id", mContext.getPackageName()));
        if(mMediaControllerAll != null){
            mMediaControllerAll.setOnClickListener(mMediaControllerListener);
        }

        mBackImageView = (ImageView) v.findViewById(getResources().getIdentifier(
                "mediacontroller_back", "id", mContext.getPackageName()));
        if(mBackImageView != null) {
            // do something
            mBackImageView.setOnClickListener(mBackListener);
        }

        mFileNameTextView = (TextView) v.findViewById(getResources().getIdentifier(
                "mediacontroller_file_name", "id", mContext.getPackageName()));

        mPauseImageView = (ImageView) v.findViewById(getResources().getIdentifier(
                "mediacontroller_play_pause", "id", mContext.getPackageName()));
        if(mPauseImageView != null){
            // do something
            mPauseImageView.setOnClickListener(mPlayAndPauseListener);
        }
        mCaptureImageView = (ImageView) v.findViewById(getResources().getIdentifier(
                "mediacontroller_screenshot", "id", mContext.getPackageName()));
        if(mCaptureImageView != null){
            // do something
            mCaptureImageView.setOnClickListener(mCaptureImageListener);
        }
        mScreenImageView = (ImageView) v.findViewById(getResources().getIdentifier(
                "mediacontroller_origin_full", "id", mContext.getPackageName()));
        if(mScreenImageView != null){
            // do something
            mScreenImageView.setOnClickListener(mFullAndOriginScreenListener);
        }

    }

    /**
     * <p>
     * Change the animation style resource for this controller.
     * </p>
     *
     * <p>
     * If the controller is showing, calling this method will take effect only
     * the next time the controller is shown.
     * </p>
     *
     * @param animationStyle
     * animation style to use when the controller appears and disappears.
     * Set to -1 for the default animation, 0 for no animation,
     * or a resource identifier for an explicit animation.
     *
     */
    public void setAnimationStyle(int animationStyle) {
        mAnimStyle = animationStyle;
    }

    public interface OnShownListener {
        public void onShown();
    }

    private OnShownListener mShownListener;

    public void setOnShownListener(OnShownListener l) {
        mShownListener = l;
    }

    public interface OnHiddenListener {
        public void onHidden();
    }

    private OnHiddenListener mHiddenListener;

    public void setOnHiddenListener(OnHiddenListener l) {
        mHiddenListener = l;
    }

    public void setFileName(String name) {
        if (mFileNameTextView != null)
            mFileNameTextView.setText(name);
    }

    // 触摸监听实现
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    // 按键监听实现
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getRepeatCount() == 0
                && (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE)) {
            doPauseResume();
            show(sDefaultTimeout);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_MENU) {
            hide();
            return true;
        } else {
            show(sDefaultTimeout);
        }
        return super.dispatchKeyEvent(event);
    }

    // 更新播放/暂停图标
    private void updatePausePlay() {
        if (mRoot == null || mPauseImageView == null)
            return;

        if (mPlayer.isPlaying())
            mPauseImageView.setImageResource(R.mipmap.mediacontroller_pause);
        else
            mPauseImageView.setImageResource(R.mipmap.mediacontroller_play);
    }
    // 暂停/播放视频
    private void doPauseResume() {
        if (mPlayer.isPlaying())
            mPlayer.pause();
        else
            mPlayer.start();
        updatePausePlay();
    }
    // 全屏
    private void doFullScreen(Activity activity){
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  // 竖屏->横屏

        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏

        ViewGroup.LayoutParams lp = mAnchor.getLayoutParams();  // 设定播放器尺寸
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = (int)(mScreenHeight / AppConfig.ASPECT_RATIO);
        mAnchor.setLayoutParams(lp);

        mWindow.setHeight((int)(mScreenHeight / AppConfig.ASPECT_RATIO));// 设置控制栏高度

        mScreenImageView.setImageResource(getResources().getIdentifier( // 设置图标
                "mediacontroller_originscreen", "mipmap", mContext.getPackageName()));

        mIsFullScreen = true;
    }
    // 还原
    public void doOriginScreen(Activity activity){
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  // 竖屏->横屏

        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏

        ViewGroup.LayoutParams lp = mAnchor.getLayoutParams();  // 设定播放器尺寸
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = (int)(mScreenWidth / AppConfig.ASPECT_RATIO);
        mAnchor.setLayoutParams(lp);

        mWindow.setHeight((int)(mScreenWidth / AppConfig.ASPECT_RATIO));  // 设置控制栏高度

        mScreenImageView.setImageResource(getResources().getIdentifier( // 设置图标
                "mediacontroller_fullscreen", "mipmap", mContext.getPackageName()));

        mIsFullScreen = false;
    }
    // 截屏
    private void doCaptureScreen(){
        mPlVideoTextureView.captureImage(0);
    }

    // 点击消息响应
    // 返回按钮
    private OnClickListener mBackListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "You has clicked back");
            Activity activity = (Activity) mParentContext; // 获取Activity
            int orient = activity.getRequestedOrientation();
            if(orient == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
                // 竖屏->横屏
                activity.finish();
            }else if(orient == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                // 横屏->竖屏
                doOriginScreen(activity);
            }

            hide();
        }
    };
    // 暂停/播放按钮
    private OnClickListener mPlayAndPauseListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "You has clicked play/pause");
            doPauseResume();
            show(sDefaultTimeout);
        }
    };
    // 截屏按钮
    private OnClickListener mCaptureImageListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "You has clicked capture image");
            doCaptureScreen();
            show(sDefaultTimeout);
        }
    };
    // 全屏/还原按钮
    private OnClickListener mFullAndOriginScreenListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "You has clicked full or origin screen");

            Activity activity = (Activity) mParentContext; // 获取Activity
            int orient = activity.getRequestedOrientation();
            if(orient == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
                // 竖屏->横屏
                doFullScreen(activity);
            }else{
                // 横屏->竖屏
                doOriginScreen(activity);
            }

            hide();
        }
    };
    // 点击空白处
    private OnClickListener mMediaControllerListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "You has clicked mediacontroller");
            hide();
        }
    };

    /**
     * Set the view that acts as the anchor for the control view.
     *
     * - This can for example be a VideoView, or your Activity's main view.
     * - AudioPlayer has no anchor view, so the view parameter will be null.
     *
     * @param view
     * The view to which to anchor the controller when it is visible.
     */
    @Override
    public void setAnchorView(View view) {
        mAnchor = view;
        if (mAnchor == null) {
            sDefaultTimeout = 0; // show forever
        }
        if (!mFromXml) {
            removeAllViews();
            mRoot = makeControllerView();  // 获取图像资源
            mWindow.setContentView(mRoot);// 弹窗加载视图
            mWindow.setWidth(LayoutParams.MATCH_PARENT); // 设置宽度=父布局
            mWindow.setHeight((int)(mScreenWidth/AppConfig.ASPECT_RATIO)); // 设置高度=自适应
        }
        initControllerView(mRoot);
    }

    @Override
    public void setMediaPlayer(MediaPlayerControl mediaPlayerControl) {
        mPlayer = mediaPlayerControl;
        updatePausePlay();
    }

    @Override
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Show the controller on screen. It will go away automatically after
     * 'timeout' milliseconds of inactivity.
     *
     * @param timeout
     * The timeout in milliseconds. Use 0 to show the controller until hide() is called.
     */
    @Override
    public void show(int timeout) {
        if (!mShowing) {
            if (mAnchor != null && mAnchor.getWindowToken() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    mAnchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
            }

            if (mFromXml) {
                setVisibility(View.VISIBLE);
            } else {
                int[] location = new int[2];

                if (mAnchor != null) {
                    mAnchor.getLocationOnScreen(location);
                    Rect anchorRect = new Rect(location[0], location[1],
                            location[0] + mAnchor.getWidth(), location[1]
                            + mAnchor.getHeight());

                    mWindow.setAnimationStyle(mAnimStyle);
                    mWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY,
                            anchorRect.left, anchorRect.top);
                } else {
                    Rect anchorRect = new Rect(location[0], location[1],
                            location[0] + mRoot.getWidth(), location[1]
                            + mRoot.getHeight());

                    mWindow.setAnimationStyle(mAnimStyle);
                    mWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY,
                            anchorRect.left, anchorRect.top);
                }
            }
            mShowing = true;
            if (mShownListener != null)
                mShownListener.onShown();
        }
        updatePausePlay();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT),
                    timeout);
        }
    }

    @Override
    public void hide() {
        if (mShowing) {
            if (mAnchor != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    //mAnchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                }
            }
            try {
                if (mFromXml)
                    setVisibility(View.GONE);
                else
                    mWindow.dismiss();
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "MediaController already removed");
            }
            mShowing = false;
            if (mHiddenListener != null)
                mHiddenListener.onHidden();
        }
    }

    @Override
    public boolean isShowing() {
        return mShowing;
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
    }

    // 设定播放器
    public void setPlVideoTextureView(PLVideoTextureView plVideoTextureView){
        mPlVideoTextureView = plVideoTextureView;
    }

    // 返回弹窗对象
    public PopupWindow getWindow() {
        return mWindow;
    }

    // 获取屏幕的宽高度
    private void getScreenInfo(){
        DisplayMetrics dm2 = getResources().getDisplayMetrics();
        mScreenHeight = dm2.heightPixels;
        mScreenWidth = dm2.widthPixels;

        int resourceId = this.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        mStatusBarHeight = this.getResources().getDimensionPixelSize(resourceId);
    }
}
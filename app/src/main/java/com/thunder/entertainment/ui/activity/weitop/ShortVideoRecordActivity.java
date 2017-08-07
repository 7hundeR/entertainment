package com.thunder.entertainment.ui.activity.weitop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.qiniu.pili.droid.shortvideo.PLAudioEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLCameraSetting;
import com.qiniu.pili.droid.shortvideo.PLCaptureFrameListener;
import com.qiniu.pili.droid.shortvideo.PLConcatStateListener;
import com.qiniu.pili.droid.shortvideo.PLErrorCode;
import com.qiniu.pili.droid.shortvideo.PLFaceBeautySetting;
import com.qiniu.pili.droid.shortvideo.PLFocusListener;
import com.qiniu.pili.droid.shortvideo.PLMicrophoneSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordStateListener;
import com.qiniu.pili.droid.shortvideo.PLShortVideoRecorder;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoFrame;
import com.thunder.entertainment.R;
import com.thunder.entertainment.common.utils.Config;
import com.thunder.entertainment.common.utils.RecordSettings;
import com.thunder.entertainment.common.utils.ToastUtils;
import com.thunder.entertainment.ui.weight.FocusIndicator;
import com.thunder.entertainment.ui.weight.SectionProgressBar;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by beibeizhu on 17/8/2.
 */

public class ShortVideoRecordActivity extends Activity implements PLRecordStateListener, PLConcatStateListener, PLFocusListener {

    private static final String TAG = "ShortVideoRecordActivity";

    /**
     * NOTICE: KIWI needs extra cost
     */
    private static final boolean USE_KIWI = true;

    private PLShortVideoRecorder mShortVideoRecorder;

    private SectionProgressBar mSectionProgressBar;
    private ProgressDialog mProcessingDialog;
    private View mRecordBtn;
    private View mDeleteBtn;
    private View mConcatBtn;
    private View mSwitchCameraBtn;
    private View mSwitchFlashBtn;
    private FocusIndicator mFocusIndicator;
    private SeekBar mAdjustBrightnessSeekBar;

    private boolean mFlashEnabled;
    private String mRecordErrorMsg;
    private boolean mIsEditVideo = false;

    private GestureDetector mGestureDetector;

    private PLCameraSetting mCameraSetting;

//    private KiwiTrackWrapper mKiwiTrackWrapper;
//    private KwControlView mControlView;

    private int mFocusIndicatorX;
    private int mFocusIndicatorY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_short_video_record);

        mSectionProgressBar = (SectionProgressBar) findViewById(R.id.record_progressbar);
        GLSurfaceView preview = (GLSurfaceView) findViewById(R.id.preview);
        mRecordBtn = findViewById(R.id.record);
        mDeleteBtn = findViewById(R.id.delete);
        mConcatBtn = findViewById(R.id.concat);
        mSwitchCameraBtn = findViewById(R.id.switch_camera);
        mSwitchFlashBtn = findViewById(R.id.switch_flash);
        mFocusIndicator = (FocusIndicator) findViewById(R.id.focus_indicator);
        mAdjustBrightnessSeekBar = (SeekBar) findViewById(R.id.adjust_brightness);

        mProcessingDialog = new ProgressDialog(this);
        mProcessingDialog.setMessage("处理中...");
        mProcessingDialog.setCancelable(false);

        mShortVideoRecorder = new PLShortVideoRecorder();
        mShortVideoRecorder.setRecordStateListener(this);
        mShortVideoRecorder.setFocusListener(this);

        int previewSizeRatio = getIntent().getIntExtra("PreviewSizeRatio", 0);
        int previewSizeLevel = getIntent().getIntExtra("PreviewSizeLevel", 3);
        int encodingSizeLevel = getIntent().getIntExtra("EncodingSizeLevel", 10);
        int encodingBitrateLevel = getIntent().getIntExtra("EncodingBitrateLevel", 2);

        mCameraSetting = new PLCameraSetting();
        PLCameraSetting.CAMERA_FACING_ID facingId = chooseCameraFacingId();
        mCameraSetting.setCameraId(facingId);
        mCameraSetting.setCameraPreviewSizeRatio(getPreviewSizeRatio(previewSizeRatio));
        mCameraSetting.setCameraPreviewSizeLevel(getPreviewSizeLevel(previewSizeLevel));

        PLMicrophoneSetting microphoneSetting = new PLMicrophoneSetting();

        PLVideoEncodeSetting videoEncodeSetting = new PLVideoEncodeSetting(this);
        videoEncodeSetting.setEncodingSizeLevel(getEncodingSizeLevel(encodingSizeLevel));
        videoEncodeSetting.setEncodingBitrate(getEncodingBitrateLevel(encodingBitrateLevel));

        PLAudioEncodeSetting audioEncodeSetting = new PLAudioEncodeSetting();

        PLRecordSetting recordSetting = new PLRecordSetting();
        recordSetting.setMaxRecordDuration(RecordSettings.DEFAULT_MAX_RECORD_DURATION);
        recordSetting.setVideoCacheDir(Config.VIDEO_STORAGE_DIR);
        String videoName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".mp4";
        recordSetting.setVideoFilepath(Config.RECORD_FILE_PATH + videoName);

        PLFaceBeautySetting faceBeautySetting = new PLFaceBeautySetting(1.0f, 0.5f, 0.5f);

        mShortVideoRecorder.prepare(preview, mCameraSetting, microphoneSetting, videoEncodeSetting,
                audioEncodeSetting, USE_KIWI ? null : faceBeautySetting, recordSetting);

//        if (USE_KIWI) {
//            StickerConfigMgr.setSelectedStickerConfig(null);
//
//            mKiwiTrackWrapper = new KiwiTrackWrapper(this, mCameraSetting.getCameraId());
//            mKiwiTrackWrapper.onCreate(this);
//
//            mControlView = (KwControlView) findViewById(R.id.kiwi_control_layout);
//            mControlView.setOnEventListener(mKiwiTrackWrapper.initUIEventListener());
//            mControlView.setVisibility(View.VISIBLE);
//
//            mShortVideoRecorder.setVideoFilterListener(new PLVideoFilterListener() {
//                private int surfaceWidth;
//                private int surfaceHeight;
//                private boolean isTrackerOnSurfaceChangedCalled;
//
//                @Override
//                public void onSurfaceCreated() {
//                    mKiwiTrackWrapper.onSurfaceCreated(ShortVideoRecordActivity.this);
//                }
//
//                @Override
//                public void onSurfaceChanged(int width, int height) {
//                    surfaceWidth = width;
//                    surfaceHeight = height;
//                }
//
//                @Override
//                public void onSurfaceDestroy() {
//                    mKiwiTrackWrapper.onSurfaceDestroyed();
//                }
//
//                @Override
//                public int onDrawFrame(int texId, int texWidth, int texHeight, long l) {
//                    if (!isTrackerOnSurfaceChangedCalled) {
//                        isTrackerOnSurfaceChangedCalled = true;
//                        mKiwiTrackWrapper.onSurfaceChanged(surfaceWidth, surfaceHeight, texWidth, texHeight);
//                    }
//                    return mKiwiTrackWrapper.onDrawFrame(texId, texWidth, texHeight);
//                }
//            });
//        }

        mSectionProgressBar.setFirstPointTime(RecordSettings.DEFAULT_MIN_RECORD_DURATION);
        mSectionProgressBar.setTotalTime(RecordSettings.DEFAULT_MAX_RECORD_DURATION);

        mRecordBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (mShortVideoRecorder.beginSection()) {
                        mSwitchCameraBtn.setEnabled(false);
                        v.setActivated(true);
                    } else {

                        ToastUtils.s(ShortVideoRecordActivity.this, "无法开始视频段录制");
                    }
                } else if (action == MotionEvent.ACTION_UP) {
                    mShortVideoRecorder.endSection();
                    mSwitchCameraBtn.setEnabled(true);
                    v.setActivated(false);
                }

                return false;
            }
        });
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                mFocusIndicatorX = (int) e.getX() - mFocusIndicator.getWidth() / 2;
                mFocusIndicatorY = (int) e.getY() - mFocusIndicator.getHeight() / 2;
                mShortVideoRecorder.manualFocus(mFocusIndicator.getWidth(), mFocusIndicator.getHeight(), (int) e.getX(), (int) e.getY());
                return false;
            }
        });
        preview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });
        onSectionCountChanged(0, 0);
    }

    public void onCaptureFrame(View v) {
        mShortVideoRecorder.captureFrame(new PLCaptureFrameListener() {
            @Override
            public void onFrameCaptured(PLVideoFrame capturedFrame) {
                if (capturedFrame == null) {
                    Log.e(TAG, "capture frame failed");
                    return;
                }

                Log.i(TAG, "captured frame width: " + capturedFrame.getWidth() + " height: " + capturedFrame.getHeight() + " timestamp: " + capturedFrame.getTimestampMs());
                try {
                    FileOutputStream fos = new FileOutputStream(Config.CAPTURED_FRAME_FILE_PATH);
                    capturedFrame.toBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.s(ShortVideoRecordActivity.this, "截帧已保存到路径：" + Config.CAPTURED_FRAME_FILE_PATH);
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onClickBack(View v) {
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mRecordBtn.setEnabled(false);
//        if (mKiwiTrackWrapper != null) {
//            mKiwiTrackWrapper.onResume(this);
//        }
        mShortVideoRecorder.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (mKiwiTrackWrapper != null) {
//            mKiwiTrackWrapper.onPause(this);
//        }
        mShortVideoRecorder.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mKiwiTrackWrapper != null) {
//            mKiwiTrackWrapper.onDestroy(this);
//        }
        mShortVideoRecorder.destroy();
    }

    public void onClickDelete(View v) {
        if (!mShortVideoRecorder.deleteLastSection()) {
            ToastUtils.s(this, "回删视频段失败");
        }
    }

    public void onClickConcat(View v) {
        mProcessingDialog.show();
        showChooseDialog();
    }

    public void onClickSwitchCamera(View v) {
//        if (mKiwiTrackWrapper != null) {
//            mKiwiTrackWrapper.switchCamera(mCameraSetting.getCameraId());
//        }
        mShortVideoRecorder.switchCamera();

    }

    public void onClickSwitchFlash(View v) {
        mFlashEnabled = !mFlashEnabled;
        mShortVideoRecorder.setFlashEnabled(mFlashEnabled);
        mSwitchFlashBtn.setActivated(mFlashEnabled);
    }

    @Override
    public void onReady() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwitchFlashBtn.setVisibility(mShortVideoRecorder.isFlashSupport() ? View.VISIBLE : View.GONE);
                mFlashEnabled = false;
                mSwitchFlashBtn.setActivated(mFlashEnabled);
                mRecordBtn.setEnabled(true);
                refreshSeekBar();
                ToastUtils.s(ShortVideoRecordActivity.this, "可以开始拍摄咯");
            }
        });
    }

    @Override
    public void onError(int code) {
        if (code == PLErrorCode.ERROR_SETUP_CAMERA_FAILED) {
            mRecordErrorMsg = "摄像头配置错误";
        } else if (code == PLErrorCode.ERROR_SETUP_MICROPHONE_FAILED) {
            mRecordErrorMsg = "麦克风配置错误";
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.s(ShortVideoRecordActivity.this, mRecordErrorMsg);
            }
        });
    }

    @Override
    public void onDurationTooShort() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.s(ShortVideoRecordActivity.this, "该视频段太短了");
            }
        });
    }

    @Override
    public void onRecordStarted() {
        Log.i(TAG, "record start time: " + System.currentTimeMillis());
        mSectionProgressBar.setCurrentState(SectionProgressBar.State.START);
    }

    @Override
    public void onRecordStopped() {
        Log.i(TAG, "record stop time: " + System.currentTimeMillis());
        mSectionProgressBar.setCurrentState(SectionProgressBar.State.PAUSE);
    }

    @Override
    public void onSectionIncreased(long incDuration, long totalDuration, int sectionCount) {
        Log.i(TAG, "section increased incDuration: " + incDuration + " totalDuration: " + totalDuration + " sectionCount: " + sectionCount);
        onSectionCountChanged(sectionCount, totalDuration);
        mSectionProgressBar.addBreakPointTime(totalDuration);
    }

    @Override
    public void onSectionDecreased(long decDuration, long totalDuration, int sectionCount) {
        Log.i(TAG, "section decreased decDuration: " + decDuration + " totalDuration: " + totalDuration + " sectionCount: " + sectionCount);
        onSectionCountChanged(sectionCount, totalDuration);
        mSectionProgressBar.removeLastBreakPoint();
    }

    @Override
    public void onRecordCompleted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.s(ShortVideoRecordActivity.this, "已达到拍摄总时长");
            }
        });
    }

    @Override
    public void onConcatProgressUpdate(int num, int sectionCount) {

    }

    @Override
    public void onConcatFailed(final int errorCode) {
        Log.i(TAG, "onConcatFailed: errorCode" + errorCode);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProcessingDialog.dismiss();
                ToastUtils.s(ShortVideoRecordActivity.this, "拼接视频段失败: " + errorCode);
            }
        });
    }

    @Override
    public void onConcatSuccess(final String filePath) {
        Log.i(TAG, "concat sections success filePath: " + filePath);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProcessingDialog.dismiss();
                if (mIsEditVideo) {
                    VideoEditActivity.start(ShortVideoRecordActivity.this, filePath);
                } else {
                    PlaybackActivity.start(ShortVideoRecordActivity.this, filePath);
                }
            }
        });
    }

    private void refreshSeekBar() {
        final int max = mShortVideoRecorder.getMaxExposureCompensation();
        final int min = mShortVideoRecorder.getMinExposureCompensation();
        boolean brightnessAdjustAvailable = (max != 0 || min != 0);
        Log.e(TAG, "max/min exposure compensation: " + max + "/" + min + " brightness adjust available: " + brightnessAdjustAvailable);

        findViewById(R.id.brightness_panel).setVisibility(View.GONE);
//        findViewById(R.id.brightness_panel).setVisibility(brightnessAdjustAvailable ? View.VISIBLE : View.GONE);
        mAdjustBrightnessSeekBar.setOnSeekBarChangeListener(!brightnessAdjustAvailable ? null : new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i <= Math.abs(min)) {
                    mShortVideoRecorder.setExposureCompensation(i + min);
                } else {
                    mShortVideoRecorder.setExposureCompensation(i - max);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mAdjustBrightnessSeekBar.setMax(max + Math.abs(min));
        mAdjustBrightnessSeekBar.setProgress(Math.abs(min));
        mAdjustBrightnessSeekBar.setVisibility(View.GONE);
    }

    private void onSectionCountChanged(final int count, final long totalTime) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeleteBtn.setEnabled(count > 0);
                mConcatBtn.setEnabled(totalTime >= RecordSettings.DEFAULT_MIN_RECORD_DURATION);
            }
        });
    }

    private PLCameraSetting.CAMERA_PREVIEW_SIZE_RATIO getPreviewSizeRatio(int position) {
        return RecordSettings.PREVIEW_SIZE_RATIO_ARRAY[position];
    }

    private PLCameraSetting.CAMERA_PREVIEW_SIZE_LEVEL getPreviewSizeLevel(int position) {
        return RecordSettings.PREVIEW_SIZE_LEVEL_ARRAY[position];
    }

    private PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL getEncodingSizeLevel(int position) {
        return RecordSettings.ENCODING_SIZE_LEVEL_ARRAY[position];
    }

    private int getEncodingBitrateLevel(int position) {
        return RecordSettings.ENCODING_BITRATE_LEVEL_ARRAY[position];
    }

    private PLCameraSetting.CAMERA_FACING_ID chooseCameraFacingId() {
        if (PLCameraSetting.hasCameraFacing(PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_3RD)) {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_3RD;
        } else if (PLCameraSetting.hasCameraFacing(PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT)) {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT;
        } else {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_BACK;
        }
    }

    private void showChooseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.if_edit_video));
        builder.setPositiveButton(getString(R.string.dlg_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mIsEditVideo = true;
                mShortVideoRecorder.concatSections(ShortVideoRecordActivity.this);
            }
        });
        builder.setNegativeButton(getString(R.string.dlg_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mIsEditVideo = false;
                mShortVideoRecorder.concatSections(ShortVideoRecordActivity.this);
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    @Override
    public void onManualFocusStart(boolean result) {
        if (result) {
            Log.i(TAG, "manual focus begin success");
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFocusIndicator.getLayoutParams();
            lp.leftMargin = mFocusIndicatorX;
            lp.topMargin = mFocusIndicatorY;
            mFocusIndicator.setLayoutParams(lp);
            mFocusIndicator.focus();
        } else {
            mFocusIndicator.focusCancel();
            Log.i(TAG, "manual focus not supported");
        }
    }

    @Override
    public void onManualFocusStop(boolean result) {
        Log.i(TAG, "manual focus end result: " + result);
        if (result) {
            mFocusIndicator.focusSuccess();
        } else {
            mFocusIndicator.focusFail();
        }
    }

    @Override
    public void onManualFocusCancel() {
        Log.i(TAG, "manual focus canceled");
        mFocusIndicator.focusCancel();
    }

    @Override
    public void onAutoFocusStart() {
        Log.i(TAG, "auto focus start");
    }

    @Override
    public void onAutoFocusStop() {
        Log.i(TAG, "auto focus stop");
    }
}

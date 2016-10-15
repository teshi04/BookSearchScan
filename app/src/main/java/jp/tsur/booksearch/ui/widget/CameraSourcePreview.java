package jp.tsur.booksearch.ui.widget;

/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.Manifest;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.RequiresPermission;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

import jp.tsur.booksearch.camera.GraphicOverlay;


public class CameraSourcePreview extends ViewGroup {

    private static final String TAG = "CameraSourcePreview";

    private SurfaceView surfaceView;
    private boolean startRequested;
    private boolean surfaceAvailable;
    private CameraSource cameraSource;

    private GraphicOverlay overlay;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        startRequested = false;
        surfaceAvailable = false;

        surfaceView = new SurfaceView(context);
        surfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(surfaceView);
        addView(new OverlayImageView(context));
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void start(CameraSource cameraSource) throws IOException, SecurityException {
        if (cameraSource == null) {
            stop();
        }

        this.cameraSource = cameraSource;

        if (this.cameraSource != null) {
            startRequested = true;
            startIfReady();
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException, SecurityException {
        this.overlay = overlay;
        start(cameraSource);
    }

    public void stop() {
        if (cameraSource != null) {
            cameraSource.stop();
        }
    }

    public void release() {
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private void startIfReady() throws IOException, SecurityException {
        if (startRequested && surfaceAvailable) {
            cameraSource.start(surfaceView.getHolder());
            if (overlay != null) {
                Size size = cameraSource.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());
                if (isPortraitMode()) {
                    overlay.setCameraInfo(min, max, cameraSource.getCameraFacing());
                } else {
                    overlay.setCameraInfo(max, min, cameraSource.getCameraFacing());
                }
                overlay.clear();
            }
            startRequested = false;
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            surfaceAvailable = true;
            try {
                startIfReady();
            } catch (SecurityException se) {
                Log.e(TAG, "Do not have permission to start the camera", se);
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            surfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = 320;
        int height = 240;
        if (cameraSource != null) {
            Size size = cameraSource.getPreviewSize();
            if (size != null) {
                width = size.getWidth();
                height = size.getHeight();
            }
        }

        final int layoutWidth = right - left;
        final int layoutHeight = bottom - top;

        int childWidth;
        int childHeight;

        if (layoutHeight > layoutWidth) {
            childHeight = layoutHeight;
            childWidth = (int) (((float) layoutHeight / (float) width) * height);
        } else {
            childWidth = layoutWidth;
            childHeight = (int) (((float) layoutWidth / (float) width) * height);
        }

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(0, 0, childWidth, childHeight);
        }

        try {
            startIfReady();
        } catch (SecurityException se) {
            Log.e(TAG, "Do not have permission to start the camera", se);
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    private boolean isPortraitMode() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static class OverlayImageView extends ImageView {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public OverlayImageView(Context context) {
            this(context, null);
        }

        public OverlayImageView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public OverlayImageView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            paint.setColor(Color.argb(140, 0, 0, 0));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth() - (int) (getWidth() * 0.2);
            int height = width / 2;
            int top = (getHeight() - height) / 2;

            canvas.save();
            canvas.drawRect(0, 0, getWidth(), top, paint);
            canvas.drawRect(0, top, (getWidth() - width) / 2, top + height, paint);
            canvas.drawRect(width, top, getWidth(), top + height, paint);
            canvas.drawRect(0, top + height, getWidth(), getHeight(), paint);
            canvas.restore();
        }
    }
}

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
package jp.tsur.booksearch.camera;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * Factory for creating a tracker and associated graphic to be associated with a new barcode.  The
 * multi-processor uses this factory to create barcode trackers as needed -- one for each barcode.
 */
public class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {

    private GraphicOverlay<BarcodeGraphic> graphicOverlay;
    private BarcodeGraphicTracker.BarcodeGraphicTrackerListener listener;

    public BarcodeTrackerFactory(GraphicOverlay<BarcodeGraphic> barcodeGraphicOverlay, BarcodeGraphicTracker.BarcodeGraphicTrackerListener listener) {
        graphicOverlay = barcodeGraphicOverlay;
        this.listener = listener;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        BarcodeGraphic graphic = new BarcodeGraphic(graphicOverlay);
        return new BarcodeGraphicTracker(graphicOverlay, graphic, listener);
    }

}

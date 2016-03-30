package jp.tsur.booksearch.camera;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

public class BarcodeGraphicTracker extends Tracker<Barcode> {

    private GraphicOverlay<BarcodeGraphic> overlay;
    private BarcodeGraphic graphic;
    BarcodeGraphicTrackerListener listener;

    public interface BarcodeGraphicTrackerListener {
        void onDetectIsbn(String isbn);
    }

    BarcodeGraphicTracker(GraphicOverlay<BarcodeGraphic> overlay, BarcodeGraphic graphic, BarcodeGraphicTrackerListener listener) {
        this.overlay = overlay;
        this.graphic = graphic;
        this.listener = listener;
    }

    @Override
    public void onNewItem(int id, Barcode item) {
        if (item.valueFormat == Barcode.ISBN) {
            overlay.add(graphic);
            graphic.updateItem(item);
            listener.onDetectIsbn(item.displayValue);
        }
    }

    @Override
    public void onUpdate(Detector.Detections<Barcode> detectionResults, Barcode item) {

    }

    @Override
    public void onMissing(Detector.Detections<Barcode> detectionResults) {
    }

    @Override
    public void onDone() {
    }
}

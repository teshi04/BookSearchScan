package jp.tsur.booksearch.camera;

import android.graphics.RectF;

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
        int width = overlay.getWidth() - (int) (overlay.getWidth() * 0.2);
        int height = width / 2;
        int top = (overlay.getHeight() - height) / 2;

        RectF rect = new RectF(item.getBoundingBox());
        rect.left = graphic.translateX(rect.left);
        rect.top = graphic.translateY(rect.top);
        rect.right = graphic.translateX(rect.right);
        rect.bottom = graphic.translateY(rect.bottom);

        if (item.valueFormat == Barcode.ISBN) {
            if (rect.top > top && rect.bottom < top + height
                    && rect.left > (overlay.getWidth() - width) / 2 && rect.right < width) {
                overlay.add(graphic);
                graphic.updateItem(item);
                listener.onDetectIsbn(item.displayValue);
            }
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

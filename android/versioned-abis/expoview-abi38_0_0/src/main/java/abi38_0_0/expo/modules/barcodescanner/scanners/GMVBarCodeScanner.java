package abi38_0_0.expo.modules.barcodescanner.scanners;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import abi38_0_0.org.unimodules.interfaces.barcodescanner.BarCodeScannerResult;
import abi38_0_0.org.unimodules.interfaces.barcodescanner.BarCodeScannerSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import abi38_0_0.expo.modules.barcodescanner.utils.FrameFactory;

public class GMVBarCodeScanner extends ExpoBarCodeScanner {

  private String TAG = GMVBarCodeScanner.class.getSimpleName();

  private BarcodeDetector mBarcodeDetector;

  public GMVBarCodeScanner(Context context) {
    super(context);
    mBarcodeDetector = new BarcodeDetector.Builder(mContext)
        .setBarcodeFormats(0)
        .build();
  }

  @Override
  public BarCodeScannerResult scan(byte[] data, int width, int height, int rotation) {
    try {
      List<BarCodeScannerResult> results = scan(FrameFactory.buildFrame(data, width, height, rotation));
      return results.size() > 0 ? results.get(0) : null;
    } catch (Exception e) {
      // Sometimes data has different size than width and height would suggest:
      // ByteBuffer.wrap(data).capacity() < width * height.
      // When given such arguments, Frame cannot be built and IllegalArgumentException is thrown.
      // See https://github.com/expo/expo/issues/2422.
      // In such case we can't do anything about it but ignore the frame.
      Log.e(TAG, "Failed to detect barcode: " + e.getMessage());
      return null;
    }
  }

  @Override
  public List<BarCodeScannerResult> scanMultiple(Bitmap bitmap) {
    return scan(FrameFactory.buildFrame(bitmap));
  }

  private List<BarCodeScannerResult> scan(abi38_0_0.expo.modules.barcodescanner.utils.Frame frame) {
    try {
      SparseArray<Barcode> result = mBarcodeDetector.detect(frame.getFrame());
      List<BarCodeScannerResult> results = new ArrayList<>();

      int width = frame.getDimensions().getWidth();
      int height = frame.getDimensions().getHeight();

      for (int i = 0; i < result.size(); i++) {

        Barcode barcode = result.get(result.keyAt(i));
        List<Integer> cornerPoints = new ArrayList<>();
        for (Point point : barcode.cornerPoints) {
          Integer x = Integer.valueOf(point.x);
          Integer y = Integer.valueOf(point.y);
          cornerPoints.addAll(Arrays.asList(x, y));
        }
        results.add(new BarCodeScannerResult(barcode.format, barcode.rawValue, cornerPoints, height, width));
      }

      return results;
    } catch (Exception e) {
      // for some reason, sometimes the very first preview frame the camera passes back to us
      // doesn't have the correct amount of data (data.length is too small for the height and width)
      // which throws, so we just return an empty list
      // subsequent frames are all the correct length & don't seem to throw
      Log.e(TAG, "Failed to detect barcode: " + e.getMessage());
      return Collections.emptyList();
    }
  }

  @Override
  public void setSettings(BarCodeScannerSettings settings) {
    List<Integer> newBarCodeTypes = parseBarCodeTypesFromSettings(settings);
    if (areNewAndOldBarCodeTypesEqual(newBarCodeTypes)) {
      return;
    }

    int barcodeFormats = 0;
    for (Integer code : newBarCodeTypes) {
      barcodeFormats = barcodeFormats | code;
    }

    mBarCodeTypes = newBarCodeTypes;
    if (mBarcodeDetector != null) {
      mBarcodeDetector.release();
    }
    mBarcodeDetector = new BarcodeDetector.Builder(mContext)
        .setBarcodeFormats(barcodeFormats)
        .build();
  }

  @Override
  public boolean isAvailable() {
    return mBarcodeDetector.isOperational();
  }
}

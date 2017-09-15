package com.yongyida.robot.voice.camera.utils;

import android.app.Activity;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class CameraUtils {

	private static final String TAG = "CamParaUtil";
	private CameraSizeComparator sizeComparator = new CameraSizeComparator();
	private static CameraUtils myCamPara = null;
	private static final Pattern COMMA_PATTERN = Pattern.compile(",");
	private DiffComparator diffComparator = new DiffComparator();
	private CameraUtils() {

	}

	public static CameraUtils getInstance() {
		if (myCamPara == null) {
			myCamPara = new CameraUtils();
			return myCamPara;
		} else {
			return myCamPara;
		}
	}

	public Size getPropPreviewSize(List<Camera.Size> list, float th,
			int minWidth) {
		Collections.sort(list, sizeComparator);

		int i = 0;
		for (Size s : list) {
			if ((s.width >= minWidth) && equalRate(s, th)) {
				Log.i(TAG, "PreviewSize:w = " + s.width + "h = " + s.height);
				break;
			}
			i++;
		}
		if (i == list.size()) {
			i = 0;
		}
		return list.get(i);

	}

	public Size getPropPictureSize(List<Camera.Size> list, float th,
			int minWidth) {
		Collections.sort(list, sizeComparator);

		int i = 0;
		for (Size s : list) {
			Log.e(TAG, "PictureSize : w = " + s.width + "h = " + s.height);
			if ((s.width >= minWidth) && equalRate(s, th)) {
				Log.i(TAG, "PictureSize : w = " + s.width + "h = " + s.height);
//				break;
			}
			i++;
		}
		if (i == list.size()) {
			i = 0;
		}
		return list.get(i);

	}
	public Size getBestPictureSize(List<Camera.Size> list, float th,
								   int defaultWidth) {
		List<Diff> list_diff = new ArrayList<Diff>();
		if(list.size() > 0){
			for (int i = 0; i < list.size(); i++) {
				if(equalRate(list.get(i), th)){
					list_diff.add(new Diff(i, Math.abs(list.get(i).width - defaultWidth)));
				}

			}
			Collections.sort(list_diff, diffComparator);
			if(list_diff.size() > 0){
				return list.get(list_diff.get(0).getIndex());
			}else{
				return null;
			}

		}else{
			return null;
		}




	}

	public static Size getOptimalPreviewSize(Activity currentActivity,
											 List<Size> sizes, double targetRatio) {
		// Use a very small tolerance because we want an exact match.
		final double ASPECT_TOLERANCE = 0.001;
		if (sizes == null) return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		// Because of bugs of overlay and layout, we sometimes will try to
		// layout the viewfinder in the portrait orientation and thus get the
		// wrong size of mSurfaceView. When we change the preview size, the
		// new overlay will be created before the old one closed, which causes
		// an exception. For now, just get the screen size

		Display display = currentActivity.getWindowManager().getDefaultDisplay();
		int targetHeight = Math.min(display.getHeight(), display.getWidth());

		if (targetHeight <= 0) {
			// We don't know the size of SurfaceView, use screen height
			targetHeight = display.getHeight();
		}

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio. This should not happen.
		// Ignore the requirement.
		if (optimalSize == null) {
			Log.w(TAG, "No preview size match the aspect ratio");
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}
	public boolean equalRate(Size s, float rate) {
		float r = (float) (s.width) / (float) (s.height);
		if (Math.abs(r - rate) <= 0.03) {
			return true;
		} else {
			return false;
		}
	}

	public class CameraSizeComparator implements Comparator<Camera.Size> {
		public int compare(Size lhs, Size rhs) {
			if (lhs.width == rhs.width) {
				return 0;
			} else if (lhs.width > rhs.width) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	private static class SizeComparator implements Comparator<Camera.Size> {
		public int compare(Size lhs, Size rhs) {
			if (lhs.width == rhs.width)
				return 0;
			else if (lhs.width > rhs.width)
				return -1;
			else
				return 1;
		}
	}

	private static class DiffComparator implements Comparator<Diff> {
		public int compare(Diff lhs, Diff rhs) {
			if (lhs.diff == rhs.diff)
				return 0;
			else if (lhs.diff > rhs.diff)
				return 1;
			else
				return -1;
		}
	}
	class Diff{
		int index;
		int diff;
		public Diff(int index, int diff){
			this.index = index;
			this.diff = diff;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		public int getDiff() {
			return diff;
		}
		public void setDiff(int diff) {
			this.diff = diff;
		}
	}
	public static Size getOptimalSize(List<Camera.Size> list, int width) {
		Collections.sort(list, new SizeComparator());

		for (Size s : list) {
			if (s.width >= width)
				return s;
		}
		return list.get(list.size() - 1);
	}
	public static void printSize(List<Size> sizes) {
		for (Size s : sizes) {
			Log.d(TAG, s.width + " x " + s.height);
		}
	}
	public void printSupportPreviewSize(Camera.Parameters params) {
		List<Size> previewSizes = params.getSupportedPreviewSizes();
		for (int i = 0; i < previewSizes.size(); i++) {
			Size size = previewSizes.get(i);
		}

	}

	public void printSupportPictureSize(Camera.Parameters params) {
		List<Size> pictureSizes = params.getSupportedPictureSizes();
		for (int i = 0; i < pictureSizes.size(); i++) {
			Size size = pictureSizes.get(i);
		}
	}

	public void printSupportFocusMode(Camera.Parameters params) {
		List<String> focusModes = params.getSupportedFocusModes();
		for (String mode : focusModes) {
			Log.i(TAG, "focusModes--" + mode);
		}
	}

	public static void prepareMatrix(Matrix matrix, boolean mirror,
			int displayOrientation, int viewWidth, int viewHeight) {
		matrix.setScale(mirror ? -1 : 1, 1);

		// This is the value for android.hardware.Camera.setDisplayOrientation.
		if (displayOrientation != 0)
			matrix.postRotate(displayOrientation);

		// Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
		// UI coordinates range from (0, 0) to (width, height).
		matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
		matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
	}


}

package com.yongyida.robot.voice.camera.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.yongyida.robot.voice.camera.exception.VoiceCameraApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FileUtil {

	private static final String TAG = "PhotosFileUtil";
	static String str = null;
	static Date date = null;
	static File file = null;

	/** 分别是app端图片保存路径、机器人端保存大图路径、机器人端保存缩略图路径 */
	private static final String APP_PICTURES_NAME = "PlayCamera";
	private static final String ROBOT_BIG_PICTURES_NAME = "BigPhotosCamera";
	private static final String ROBOT_PICTURES_NAME = "PhotosCamera";

	/** app，机器人保存路径 */
	public static String appPath;
	public static String robotBigPath;
	public static String robotPath;

	public static SaveCompleteListener mListener;
	public interface SaveCompleteListener{
		void onSaveComplete();
		void onShowImage(Bitmap bitmap);
	}

	public static void setSaveListener(SaveCompleteListener listener){
		mListener = listener;
	}
	// 初始化图片保存路径
	private String getPhotoPath(String string) {
		String storagePath = null;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String parentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
			storagePath = parentPath + "/" + string;
			// Log.i("222","storagePath" + storagePath);
			File file = new File(storagePath);
			if (!file.exists()) {
				file.mkdir();
			}
		}
		return storagePath;
	}

	// 保存图片到/SDcard目录
	@SuppressLint("SimpleDateFormat")
	public void saveBitmap(Context mContext, Bitmap b) {
		if(mListener != null){
			//更新相册按钮的图片
			mListener.onShowImage(b);
		}
		// 保存图片到相应的文件夹
		appPath = getPhotoPath(APP_PICTURES_NAME);
		robotBigPath = getPhotoPath(ROBOT_BIG_PICTURES_NAME);
		robotPath = getPhotoPath(ROBOT_PICTURES_NAME);

		// 获取当前时间，进一步转化为字符串
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		date = new Date();
		str = format.format(date);

		savePhotosForApp(appPath, b);
		//savePhotosForBigRobot(robotBigPath, b);
		savePhotosForRobot(robotPath, b);

		Intent intent= new Intent();
		intent.setAction("com.yydrobot.resource.close");
		mContext.sendBroadcast(intent);

/*		mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
				Uri.parse("file://"+ Environment.getExternalStorageDirectory())));*/
		mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
				Uri.parse("file://"+ Environment.getExternalStorageDirectory())));

		if(mListener != null){
			mListener.onSaveComplete();
		}

	}

	private void savePhotosForApp(String path, Bitmap b) {
		Bitmap rotaBitmap = null;
		//除了y20都做了90度的调整
		String camera_rotateKey = "persist.yongyida.camera_rotate";
        String value = VoiceCameraApplication.getSystemProperty(camera_rotateKey);
		Log.i(TAG,"persist.yongyida.camera_rotate="+ value );
		if(Boolean.valueOf(value)){
			rotaBitmap = getThumbnailBitmap(b);
		}else{
			Matrix matrix = new Matrix();
			matrix.postRotate(90f);
			Bitmap rotaBitmap1 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),b.getHeight(), matrix, false);
			rotaBitmap = getThumbnailBitmap(rotaBitmap1);
		}
		if (path != null) {
			file = new File(path, str + ".jpg");
			try {
				FileOutputStream fout = new FileOutputStream(file);
				rotaBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
				fout.flush();
				fout.close();
			} catch (IOException e) {
				Log.i(TAG, "save bitmap error");
				e.printStackTrace();
			}
		}
	}

	private void savePhotosForBigRobot(String path, Bitmap b) {

		if (path != null) {
			file = new File(path, str + ".jpg");
			try {
				FileOutputStream fout = new FileOutputStream(file);
				b.compress(Bitmap.CompressFormat.JPEG, 100, fout);
				fout.flush();
				fout.close();
			} catch (IOException e) {
				Log.i(TAG, "save bitmap error");
				e.printStackTrace();
			}
		}
	}

	private void savePhotosForRobot(String path, Bitmap b) {

		if (path != null && b != null) {
			Bitmap bitmap = b;
			file = new File(path, str + ".jpg");
			try {

				FileOutputStream fout = new FileOutputStream(file);

				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
				fout.flush();
				fout.close();
			} catch (IOException e) {
				Log.i(TAG, "save bitmap error");
				e.printStackTrace();
			}
		}
	}

	public Bitmap getThumbnailBitmap(Bitmap b) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//		if (baos.toByteArray().length / 1024 > 1024) {
//			baos.reset();
//			b.compress(Bitmap.CompressFormat.JPEG, 50, baos);
//		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		int hh = 1280;// 这里设置高度为800f
		int ww = 1024;// 这里设置宽度为480f
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

		return bitmap;

	}
	public static Bitmap cQuality(Bitmap bitmap){
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		int beginRate = 100;
		//第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bOut);
		while(bOut.size()/1024>100){  //如果压缩后大于100Kb，则提高压缩率，重新压缩
			beginRate -=10;
			bOut.reset();
			bitmap.compress(Bitmap.CompressFormat.JPEG, beginRate, bOut);
		}
		ByteArrayInputStream bInt = new ByteArrayInputStream(bOut.toByteArray());
		Bitmap newBitmap = BitmapFactory.decodeStream(bInt);
		if(newBitmap!=null){
			return newBitmap;
		}else{
			return bitmap;
		}
	}

	/**
	 * 在指定的位置创建指定的文件
	 *
	 * @param filePath 完整的文件路径
	 * @param mkdir    是否创建相关的文件夹
	 * @throws Exception
	 */
	public static void mkFile(String filePath, boolean mkdir) throws Exception {
		File file = new File(filePath);
		file.getParentFile().mkdirs();
		file.createNewFile();
		file = null;
	}

	/**
	 * 在指定的位置创建文件夹
	 *
	 * @param dirPath 文件夹路径
	 * @return 若创建成功，则返回True；反之，则返回False
	 */
	public static boolean mkDir(String dirPath) {
		return new File(dirPath).mkdirs();
	}

	/**
	 * 删除指定的文件
	 *
	 * @param filePath 文件路径
	 * @return 若删除成功，则返回True；反之，则返回False
	 */
	public static boolean delFile(String filePath) {
		return new File(filePath).delete();
	}

	/**
	 * 删除指定的文件夹
	 *
	 * @param dirPath 文件夹路径
	 * @param delFile 文件夹中是否包含文件
	 * @return 若删除成功，则返回True；反之，则返回False
	 */
	public static boolean delDir(String dirPath, boolean delFile) {
		if (delFile) {
			File file = new File(dirPath);
			if (file.isFile()) {
				return file.delete();
			} else if (file.isDirectory()) {
				if (file.listFiles().length == 0) {
					return file.delete();
				} else {
					int zfiles = file.listFiles().length;
					File[] delfile = file.listFiles();
					for (int i = 0; i < zfiles; i++) {
						if (delfile[i].isDirectory()) {
							delDir(delfile[i].getAbsolutePath(), true);
						}
						delfile[i].delete();
					}
					return file.delete();
				}
			} else {
				return false;
			}
		} else {
			return new File(dirPath).delete();
		}
	}

	/**
	 * 复制文件/文件夹 若要进行文件夹复制，请勿将目标文件夹置于源文件夹中
	 *
	 * @param source   源文件（夹）
	 * @param target   目标文件（夹）
	 * @param isFolder 若进行文件夹复制，则为True；反之为False
	 * @throws Exception
	 */
	public static void copy(String source, String target, boolean isFolder)
			throws Exception {
		if (isFolder) {
			(new File(target)).mkdirs();
			File a = new File(source);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (source.endsWith(File.separator)) {
					temp = new File(source + file[i]);
				} else {
					temp = new File(source + File.separator + file[i]);
				}
				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(target + "/" + (temp.getName()).toString());
					byte[] b = new byte[1024];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {
					copy(source + "/" + file[i], target + "/" + file[i], true);
				}
			}
		} else {
			int byteread = 0;
			File oldfile = new File(source);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(source);
				File file = new File(target);
				file.getParentFile().mkdirs();
				file.createNewFile();
				FileOutputStream fs = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
			}
		}
	}

	/**
	 * 移动指定的文件（夹）到目标文件（夹）
	 *
	 * @param source   源文件（夹）
	 * @param target   目标文件（夹）
	 * @param isFolder 若为文件夹，则为True；反之为False
	 * @return
	 * @throws Exception
	 */
	public static boolean move(String source, String target, boolean isFolder)
			throws Exception {
		copy(source, target, isFolder);
		if (isFolder) {
			return delDir(source, true);
		} else {
			return delFile(source);
		}
	}

	/**
	 * 获取缓存文件夹
	 *
	 * @param context
	 * @return
	 */
	public static String getDiskCacheDir(Context context) {
		String cachePath;

		cachePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		//isExternalStorageEmulated()设备的外存是否是用内存模拟的，是则返回true
//		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageEmulated()) {
//			cachePath = context.getExternalCacheDir().getAbsolutePath();
//		} else {
//			cachePath = context.getCacheDir().getAbsolutePath();
//		}
		return cachePath;
	}

	/**
	 * 从sd卡获取图片资源
	 * @return
	 */
	public List<String> getImagePathFromSD(String string) {
		// 图片列表
		List<String> imagePathList = new ArrayList<String>();
		// 得到sd卡内image文件夹的路径   File.separator(/)
		String filePath = getPhotoPath(string);
		// 得到该路径文件夹下所有的文件
		File fileAll = new File(filePath);
		File[] files = fileAll.listFiles();
		// 将所有的文件存入ArrayList中,并过滤所有图片格式的文件
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (checkIsImageFile(file.getPath())) {
				imagePathList.add(file.getPath());
			}
		}
		Collections.sort(imagePathList,Collections.reverseOrder());
		// 返回得到的图片列表
		return imagePathList;
	}

	/**
	 * 检查扩展名，得到图片格式的文件
	 * @param fName  文件名
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public static boolean checkIsImageFile(String fName) {
		boolean isImageFile = false;
		// 获取扩展名
		String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
				fName.length()).toLowerCase();
		if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")
				|| FileEnd.equals("jpeg")|| FileEnd.equals("bmp") ) {
			isImageFile = true;
		} else {
			isImageFile = false;
		}
		return isImageFile;
	}

    /**
     * 根据文件路径读取本地图片返回bitmap
     * @param pathString
     * @return
     */
	public static Bitmap getDiskBitmap(String pathString)
	{
		Bitmap bitmap = null;
		try
		{
			File file = new File(pathString);
			if(file.exists())
			{
				bitmap = BitmapFactory.decodeFile(pathString);
			}
		} catch (Exception e)
		{
			// TODO: handle exception
		}

		return bitmap;
	}

}

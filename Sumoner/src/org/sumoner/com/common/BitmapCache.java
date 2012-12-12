package org.sumoner.com.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

/**
 * ビットマップをキャッシュするクラスです
 * 
 * @author arichi
 * 
 */
public class BitmapCache {

	// TODO 画面のサイズよって自動的にスケールを変更する仕掛け
	// ビットップの格納本体
	private static Map<Integer, Bitmap> cache = new HashMap<Integer, Bitmap>();

	// リソースファイル
	private static Resources res;
	private static int windowHeight;
	private static int windowWidth;
	private static float dpiScale;
	private static BitmapFactory.Options option;

	/**
	 * リソースファイルを設定する
	 * 
	 * @param res
	 */
	public static void setRes(Resources res) {
		BitmapCache.res = res;
	}

	/**
	 * Winodwのサイズを設定する
	 * 
	 * @param height
	 * @param width
	 */
	public static void setWindowSize(int height, int width,float scale) {
		windowHeight = height;
		windowWidth = width;
		dpiScale = scale;
	}

	/**
	 * 画像ファイルを取得します
	 * 
	 * @param key
	 * @return
	 */
	public static Bitmap getImage(int key) {
		Bitmap ref = cache.get(key);
		if (ref == null || ref.isRecycled()) {
			setImage(key);
			ref = cache.get(key);
		}
		return ref;
	}

	/**
	 * 画像ファイルを取得します
	 * 
	 * @param key
	 * @return
	 */
	public static Bitmap getImage(int key, float scale) {
		Bitmap ref = cache.get(key);
		if (ref == null || ref.isRecycled()) {
			setImage(key, scale);
			ref = cache.get(key);
		}
		return ref;
	}

	/**
	 * 画像ファイルを設定します
	 * 
	 * @param key
	 * @param image
	 */
	public static void setImage(int key) {
		// Bitmap image = BitmapFactory.decodeResource(res, key);
		if (cache.get(key) != null && !cache.get(key).isRecycled()) {
			return;
		}
		cache.put(key, loadBitmap(key));
	}

	/**
	 * リサイズして画像ファイルを設定します
	 * 
	 * @param key
	 * @param scale
	 */
	public static void setImage(int key, float scale) {
		// Bitmap image = BitmapFactory.decodeResource(res, key);
		if (cache.get(key) != null && !cache.get(key).isRecycled()) {
			// 画像がすでに格納されている場合
			BitmapFactory.Options option = new BitmapFactory.Options();
			// 実際に読み込まないで情報だけ取得する
			option.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(res, key, option);
			if (cache.get(key).getWidth() == option.outWidth) {
				// 同じ縮尺なので、そのままで再読み込みはしない
				return;
			}
		}
		// リサイズマトリクス
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		// ビットマップ作成
		Bitmap src = loadBitmap(key);
		try {
			src = Bitmap.createBitmap(src, 0, 0, src.getWidth(),
					src.getHeight(), matrix, true);
		} catch (OutOfMemoryError e) {
			java.lang.System.gc();
			src = Bitmap.createBitmap(src, 0, 0, src.getWidth(),
					src.getHeight(), matrix, true);

		}
		cache.put(key, src);
	}

	/**
	 * リサイズして画像ファイルを設定します
	 * 
	 * @param key
	 * @param height
	 * @param width
	 */
	public static void setImage(int key, int height, int width) {
		BitmapFactory.Options option = new BitmapFactory.Options();
		// 実際に読み込まないで情報だけ取得する
		option.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, key, option);
		if (cache.get(key) != null && !cache.get(key).isRecycled()) {
			// 画像がすでに格納されている場合
			if (cache.get(key).getWidth() == option.outWidth) {
				// 格納されていた画像のサイズが同じ場合だったとき
				return;
			}
		}
		// リサイズマトリクス
		Matrix matrix = new Matrix();
		// ビットマップ作成
		Bitmap src = loadBitmap(key);
		int x = src.getWidth();
		int y = src.getHeight();
		float fx = (float) width / x;
		float fy = (float) height / y;
		matrix.setScale(fx, fy);
		try {
			src = Bitmap.createBitmap(src, 0, 0, src.getWidth(),
					src.getHeight(), matrix, true);
		} catch (OutOfMemoryError e) {
			java.lang.System.gc();
			src = Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
		}
		cache.put(key, src);
	}

	/**
	 * 指定したイメージを破棄します
	 * 
	 * @param key
	 */
	public static void destoryImage(int key) {
		// Bitmapのリサイクル指定
		cache.get(key).recycle();
		cache.put(key, null);

		System.gc();
		// Cache要素から削除
		cache.remove(key);
	}

	/**
	 * キャッシュしてあるすべてのイメージを破棄します
	 */
	public static void destoryAllImage() {
		// Bitmapのリサイクル指定
		if (cache != null && cache.size() > 0) {
			Collection<Bitmap> images = cache.values();
			for (Iterator<Bitmap> i = images.iterator(); i.hasNext();) {
				Bitmap dispose = i.next();
				if (dispose != null && !dispose.isRecycled()) {
					dispose.recycle();
					dispose = null;
				}
			}
			// Cache要素から削除
			cache.clear();
		}
		res = null;
	}

	/**
	 * ビットマップをロードする
	 * 
	 * @param resId
	 * @return
	 */
	private static Bitmap loadBitmap(int resId) {
		if (option == null) {
			option = new BitmapFactory.Options();
		}
		Bitmap src = null;
		int sample_size = 2;

		// 実際に読み込まないで情報だけ取得する
		// んで、スケールを決める
		option.inJustDecodeBounds = true;
		// BitmapFactory.decodeFile(file.getAbsolutePath(), option);
		BitmapFactory.decodeResource(res, resId, option);
		if ((option.outWidth * option.outHeight) > 1048576) {
			// １Mピクセル超えてる
			double out_area = (option.outWidth * option.outHeight) / 1048576.0;
			sample_size = (int) (Math.sqrt(out_area) + 1);
		} else {
			// 小さいのでそのまま
			sample_size = 1;
		}

		// 実際に読み込むモード
		option.inJustDecodeBounds = false;
		// スケーリングする係数
		option.inSampleSize = sample_size;
		// 画像を読み込む
		// src = BitmapFactory.decodeFile(file.getAbsolutePath(), option);
		src = BitmapFactory.decodeResource(res, resId, option);
		if (src != null) {
			int src_width = src.getWidth();
			int src_height = src.getHeight();

			// 表示利用域に合わせたサイズを計算(基本はWVGA 442*800 ステータスバーを消さないため)
			float heightScale = (float) windowHeight / 442;
			float widthScale = (float) windowWidth / 800;
			
			// リサイズマトリクス
			Matrix matrix = new Matrix();
			matrix.setScale(dpiScale, dpiScale);

			// ビットマップ作成
			src = Bitmap.createBitmap(src, 0, 0, src_width, src_height, matrix,
					true);
		}
		return src;
	}

	/**
	 * ベストフィットなスケーリング率を求める
	 * 
	 * @param dest_width
	 *            目的のサイズ（幅）
	 * @param dest_height
	 *            　目的のサイズ（高さ）
	 * @param src_width
	 *            　元のサイズ（幅）
	 * @param src_height
	 *            　元のサイズ（高さ）
	 * @return
	 */
	public static float getFitScale(int dest_width, int dest_height,
			int src_width, int src_height) {
		float ret = 0;

		if (dest_width < dest_height) {
			// 縦が長い
			if (src_width < src_height) {
				// 縦が長い
				ret = (float) dest_height / (float) src_height;

				if ((src_width * ret) > dest_width) {
					// 縦に合わせると横がはみ出る
					ret = (float) dest_width / (float) src_width;
				}
			} else {
				// 横が長い
				ret = (float) dest_width / (float) src_width;
			}
		} else {
			// 横が長い
			if (src_width < src_height) {
				// 縦が長い
				ret = (float) dest_height / (float) src_height;
			} else {
				// 横が長い
				ret = (float) dest_width / (float) src_width;

				if ((src_height * ret) > dest_height) {
					// 横に合わせると縦がはみ出る
					ret = (float) dest_height / (float) src_height;
				}
			}
		}
		return ret;
	}

}

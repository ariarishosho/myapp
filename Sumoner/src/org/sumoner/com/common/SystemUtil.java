package org.sumoner.com.common;

import android.os.Debug;
import android.util.Log;

public class SystemUtil {

	public static void writeRuntimeMemory(String ward) {
		Runtime runtime = Runtime.getRuntime();

		// トータルメモリ
		Log.d("Runtime",
				ward + ": " + "totalMemory[KB] = "
						+ (int) (runtime.totalMemory() / 1024));
		// 空きメモリ
		Log.d("Runtime",
				ward + ": " + "freeMemory[KB] = "
						+ (int) (runtime.freeMemory() / 1024));
		// 現在使用しているメモリ
		Log.d("Runtime",
				ward
						+ ": "
						+ "usedMemory[KB] = "
						+ (int) ((runtime.totalMemory() - runtime.freeMemory()) / 1024));
		// Dalvikで使用できる最大メモリ
		Log.d("Runtime",
				ward + ": " + "maxMemory[KB] = "
						+ (int) (runtime.maxMemory() / 1024));

		// NativeHeapの消費メモリ
		Log.d("Runtime",
				ward + ": " + "nativeHeap[KB] = "
						+ (int) (Debug.getNativeHeapAllocatedSize() / 1024));

	}
}

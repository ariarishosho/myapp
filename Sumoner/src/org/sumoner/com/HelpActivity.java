/*
 * Copyright (C) 2009 The Android Open Source Project
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

// Android JET demonstration code:
// See the JetBoyView.java file for examples on the use of the JetPlayer class.

package org.sumoner.com;

import org.sumoner.com.common.SystemUtil;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class HelpActivity extends Activity implements
		View.OnClickListener {
	// Viewコンポーネント
	private ImageButton mButtonRetrun;// 戻るボタン

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		mButtonRetrun = (ImageButton) findViewById(R.id.helpReturnImageButton);
		mButtonRetrun.setOnClickListener(this);
		}
	/**
	 * キーダウンイベント
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return super.onKeyDown(keyCode, msg);
		}
		return false;
	}

	/**
	 * キーアップイベント
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent msg) {
		return false;
	}

	/**
	 * 画面タッチイベント
	 */
	@Override
	public boolean onTouchEvent(MotionEvent msg) {
		return false;
	}

	@Override
	public void onClick(View v) {
		SystemUtil.writeRuntimeMemory(this.getLocalClassName() + "onClick");
		if (mButtonRetrun.equals(v)) {
			// すべてのイメージファイルを破棄
			finish();
		}
	}

}

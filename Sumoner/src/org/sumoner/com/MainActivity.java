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

import org.sumoner.com.MainView.MainThread;
import org.sumoner.com.common.BitmapCache;
import org.sumoner.com.common.Const;
import org.sumoner.com.common.GameUtil;
import org.sumoner.com.common.MetaInfo;
import org.sumoner.com.common.SystemUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener,
		OnDoubleTapListener, OnGestureListener {

	private GestureDetector gestureDetector;
	private MetaInfo inf;

	// ビュー本体とスレッド
	private MainThread mMainThread;
	private MainView mMainView;

	// コンポーネント郡
	private Button mButtonStart;// 開始ボタン
	private Button mButtonBattle;// 狩場へボタン
	private Button mButtonPasture;// 牧場へボタン
	private Button mButtonHelp;// ヘルプボタン
	// private Button mButtonCombination;// 図鑑へボタン
	// private Button mButtonUpgrade;// カスタムへボタン

	private Button mButtonReStart;// 戻るボタン
	private Button mButtonGiveUp;// ギブアップ

	private TextView mNegoResultTextView;// 交渉結果

	private LinearLayout mLayputTimer;// 時間＋お金情報レイアウト
	private TextView mTextTimer;// 残り時間テキスト
	// private TextView mTextPoketMoney;// 所持金テキスト

	private ImageButton mButtonRetrun;// 戻るボタン
	private ImageButton mButtonPouse;// 一時停止ボタン

	// private ImageView mImageOffset;// 単なる飾りイメージ

	private ImageButton mButtonDown;// ステージランク降下ボタン
	private ImageButton mButtonUp;// ステージランク上昇ボタン
	private TextView mTextStageRank;// ステージランクテキスト
	private Integer currentStage = 1;

	private TableLayout mLayoutResult;// 結果テーブル
	private TextView mTextTitleResult;// 結果テーブルタイトルテキスト
	private TextView mTextResult;// 判定結果テキスト
	private TextView mTextTitleMonster;// 獲得モンスタータイトルテキスト
	private ImageView mImageGetMonster;// 獲得モンスターイメージ
	private Button mButtonBack;// OKボタン
	private Toast toast;

	/**
	 * Required method from parent class
	 * 
	 * @param savedInstanceState
	 *            - The previous instance of this app
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SystemUtil.writeRuntimeMemory(this.getLocalClassName() + "onCreate");
		setContentView(R.layout.main);

		// メタ情報取得
		inf = GameUtil.loadMetaInfo();
		currentStage = inf.stage + 1;

		mMainView = (MainView) findViewById(R.id.MainView);
		mMainThread = mMainView.getThread();
		mMainView.setMetaInfo(inf);
		mMainView.setStage(currentStage);

		mButtonStart = (Button) findViewById(R.id.mainButtonStart);
		mButtonStart.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		mButtonStart.setOnClickListener(this);
		mButtonBattle = (Button) findViewById(R.id.mainButtonBattle);
		mButtonBattle.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		mButtonBattle.setOnClickListener(this);
		mButtonPasture = (Button) findViewById(R.id.mainButtonPasture);
		mButtonPasture.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		mButtonPasture.setOnClickListener(this);
		mButtonHelp = (Button) findViewById(R.id.mainButtonHelp);
		mButtonHelp.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		mButtonHelp.setOnClickListener(this);

		// mButtonCombination = (Button)
		// findViewById(R.id.mainButtonCombination);
		// mButtonCombination.setOnClickListener(this);
		// mButtonUpgrade = (Button) findViewById(R.id.mainButtonUpgrade);
		// mButtonUpgrade.setOnClickListener(this);

		mButtonReStart = (Button) findViewById(R.id.mainButtonReStart);
		mButtonReStart.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		mButtonReStart.setOnClickListener(this);
		mButtonGiveUp = (Button) findViewById(R.id.mainButtonGiveUp);
		mButtonGiveUp.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		mButtonGiveUp.setOnClickListener(this);
		mNegoResultTextView = (TextView) findViewById(R.id.mainNegoResultMoney);

		mNegoResultTextView.setTypeface(Typeface.createFromAsset(getAssets(),
				"snickles.ttf"));

		mLayputTimer = (LinearLayout) findViewById(R.id.mainTimerLayout);
		mTextTimer = (TextView) findViewById(R.id.mainTextTimer);
		mTextTimer.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));

		mButtonRetrun = (ImageButton) findViewById(R.id.mainReturnImageButton);
		mButtonRetrun.setOnClickListener(this);

		mButtonPouse = (ImageButton) findViewById(R.id.mainPouseImageButton);
		mButtonPouse.setOnClickListener(this);

		// mImageOffset = (ImageView) findViewById(R.id.mainImageOffset);

		mButtonDown = (ImageButton) findViewById(R.id.mainImageButtonArrowBack);
		mButtonDown.setOnClickListener(this);
		mButtonUp = (ImageButton) findViewById(R.id.mainImageButtonArrowFront);
		mButtonUp.setOnClickListener(this);
		mTextStageRank = (TextView) findViewById(R.id.mainTextSecne);
		mTextStageRank.setText(String.valueOf(currentStage));
		mTextStageRank.setTypeface(Typeface.createFromAsset(getAssets(),
				"Anagram Shadow NF.ttf"));
		mLayoutResult = (TableLayout) findViewById(R.id.mainTabelResult);
		mTextTitleResult = (TextView) findViewById(R.id.mainTextResult);
		mTextTitleResult.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		mTextResult = (TextView) findViewById(R.id.mainContentTextPocketMoney);
		mTextResult.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));

		mTextTitleMonster = (TextView) findViewById(R.id.mainTitleTextGetMonster);
		mTextTitleMonster.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		mImageGetMonster = (ImageView) findViewById(R.id.mainGetMonsterImageView);
		mButtonBack = (Button) findViewById(R.id.mainButtonExit);
		mButtonBack.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		mButtonBack.setOnClickListener(this);

		mMainView.setTimerView(mTextTimer);
		// mMainView.setGetMoneyView(mTextPoketMoney);

		mMainView.setResultViews(mLayoutResult, mTextResult, mImageGetMonster);
		mMainView.setNegoResultView(mNegoResultTextView);
		gestureDetector = new GestureDetector(getApplicationContext(), this);

		// 一旦全部消す
		setAllInvisible();
		// 初期表示設定
		mButtonBattle.setVisibility(View.VISIBLE);// 狩場へボタン
		mButtonPasture.setVisibility(View.VISIBLE);// 牧場へボタン
		mButtonHelp.setVisibility(View.VISIBLE);//ヘルプへボタン
		// mButtonCombination.setVisibility(View.VISIBLE);// 図鑑へボタン
		// mButtonUpgrade.setVisibility(View.VISIBLE);// カスタムへボタン

		// Toastのインスタンスを生成
		toast = Toast.makeText(getApplicationContext(), getResources()
				.getString(R.string.noticefullMonster), Toast.LENGTH_SHORT);
		// 表示位置を設定
		toast.setGravity(Gravity.CENTER, 0, 0);

	}

	/**
	 * Handles component interaction
	 * 
	 * @param v
	 *            The object which has been clicked
	 */
	@Override
	public void onClick(View v) {
		SystemUtil.writeRuntimeMemory(this.getLocalClassName() + "onClick");
		if (mButtonBattle.equals(v)) {
			// バトルのボタンが押された場合
			if (mMainThread.getGameState() == MainThread.STATE_START) {
				// 一旦全部消す
				setAllInvisible();
				mButtonStart.setVisibility(View.VISIBLE);
				mButtonDown.setVisibility(View.VISIBLE);
				mButtonUp.setVisibility(View.VISIBLE);
				mTextStageRank.setVisibility(View.VISIBLE);
				mButtonRetrun.setVisibility(View.VISIBLE);
				mMainThread.setGameState(MainThread.STATE_PLAY);
			}
			// 開始ボタンが押された場合
			else if (mMainThread.getGameState() == MainThread.STATE_PLAY) {
				mLayputTimer.setVisibility(View.VISIBLE);
				mButtonPouse.setVisibility(View.VISIBLE);// 一時停止ボタン
				// mImageOffset.setVisibility(View.VISIBLE);// 単なる飾りイメージ
				// ほかのアクティビティから帰ってきた場合、Threadを再生成しているので、再取得
				this.mMainThread = mMainView.getThread();
				mMainThread.setGameState(MainThread.STATE_RUNNING);
			}
		} else if (mButtonStart.equals(v)) {
			// 一旦全部消す
			setAllInvisible();
			mLayputTimer.setVisibility(View.VISIBLE);
			mButtonPouse.setVisibility(View.VISIBLE);// 一時停止ボタン
			// mImageOffset.setVisibility(View.VISIBLE);// 単なる飾りイメージ
			// ほかのアクティビティから帰ってきた場合、Threadを再生成しているので、再取得
			this.mMainThread = mMainView.getThread();
			mMainThread.setGameState(MainThread.STATE_RUNNING);
		} else if (mButtonRetrun.equals(v)) {
			// 一旦全部消す
			setAllInvisible();
			// 初期表示設定
			mButtonBattle.setVisibility(View.VISIBLE);// 狩場へボタン
			mButtonPasture.setVisibility(View.VISIBLE);// 牧場へボタン
			mButtonHelp.setVisibility(View.VISIBLE);// 牧場へボタン
			// mButtonCombination.setVisibility(View.VISIBLE);// 図鑑へボタン
			// mButtonUpgrade.setVisibility(View.VISIBLE);// カスタムへボタン

			mMainThread.setGameState(MainThread.STATE_START);
		} else if (mButtonPasture.equals(v)) {
			// 牧場ボタン起動
			mMainThread.setRunning(false);
			Intent intent = new Intent(getApplicationContext(),
					PastureActivity.class);// Thisを使わず再取得して渡す。メモリリーク対策
			// 次画面のアクティビティ起動
			startActivity(intent);
		} else if (mButtonHelp.equals(v)) {
			// ヘルプボタン起動
			mMainThread.setRunning(false);
			Intent intent = new Intent(getApplicationContext(),
					HelpActivity.class);// Thisを使わず再取得して渡す。メモリリーク対策
			// 次画面のアクティビティ起動
			startActivity(intent);
		}
		// else if (mButtonCombination.equals(v)) {
		// // 図鑑ボタン起動
		// Intent intent = new Intent(getApplication(),
		// CombinationActivity.class);
		// // 次画面のアクティビティ起動
		// startActivity(intent);
		// } else if (mButtonUpgrade.equals(v)) {
		// // アップグレードボタン起動
		// Intent intent = new Intent(getApplication(), UpgradeActivity.class);
		// // 次画面のアクティビティ起動
		// startActivityForResult(intent, 1);
		// }
		else if (mButtonPouse.equals(v)) {
			// ポーズボタン
			mButtonReStart.setVisibility(View.VISIBLE);
			mButtonGiveUp.setVisibility(View.VISIBLE);
			mButtonPouse.setVisibility(View.INVISIBLE);
			mMainThread.setGameState(MainThread.STATE_PAUSE);
		} else if (mButtonReStart.equals(v)) {
			// 戻るボタン
			mButtonReStart.setVisibility(View.INVISIBLE);
			mButtonGiveUp.setVisibility(View.INVISIBLE);
			mButtonPouse.setVisibility(View.VISIBLE);
			mMainThread.setGameState(MainThread.STATE_RUNNING);
		} else if (mButtonGiveUp.equals(v)) {
			// ギブアップボタン
			// 一旦全部消す
			setAllInvisible();
			mLayoutResult.setVisibility(View.VISIBLE);
			mMainThread.setGameState(MainThread.STATE_GIVE_UP);

		} else if (mButtonDown.equals(v)) {
			// ステージダウン
			currentStage--;
			if (currentStage <= 0) {
				currentStage = 1;
			}
			mTextStageRank.setText(currentStage.toString());
			mMainView.setStage(currentStage);
		} else if (mButtonUp.equals(v)) {
			// ステージアップ
			currentStage++;
			if (currentStage > Const.SYSTEM_MAX_STAGE) {
				// 最大ステージを超えた場合
				currentStage = Const.SYSTEM_MAX_STAGE;
			} else if (currentStage > inf.stage + 1) {
				// クリアステージ+1を超えた場合
				currentStage = inf.stage + 1;

			}

			mTextStageRank.setText(currentStage.toString());
			mMainView.setStage(currentStage);
		} else if (mButtonBack.equals(v)) {
			// OKボタン
			// 一旦全部消す
			setAllInvisible();
			mButtonStart.setVisibility(View.VISIBLE);
			mButtonDown.setVisibility(View.VISIBLE);
			mButtonUp.setVisibility(View.VISIBLE);
			mTextStageRank.setVisibility(View.VISIBLE);
			mButtonRetrun.setVisibility(View.VISIBLE);
			mMainThread.setGameState(MainThread.STATE_PLAY);
		} else {
			Log.d("JB VIEW", "unknown click " + v.getId());
			Log.d("JB VIEW", "state is  " + mMainThread.mState);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mMainThread == null) {
			mMainThread = mMainView.getThread();
		}
	}

	/**
	 * onStopのタイミングだと次のActivityが起動しており キャッシュやContextのクリアすると ヌルポで落ちる
	 */
	@Override
	public void onPause() {
		super.onPause();
		BitmapCache.destoryAllImage();
		this.mMainThread = null;
		GameUtil.terminateContext();
		System.gc();
	}

	private void setAllInvisible() {
		// コンポーネント郡
		mButtonStart.setVisibility(View.GONE);// 狩場へボタン
		mButtonBattle.setVisibility(View.GONE);// 狩場へボタン
		mButtonPasture.setVisibility(View.GONE);// 牧場へボタン
		mButtonHelp.setVisibility(View.GONE);// ヘルプへボタン
		// mButtonCombination.setVisibility(View.GONE);// 図鑑へボタン
		// mButtonUpgrade.setVisibility(View.GONE);// カスタムへボタン
		mNegoResultTextView.setVisibility(View.GONE);
		mButtonReStart.setVisibility(View.GONE);// 戻るボタン
		mButtonGiveUp.setVisibility(View.GONE);// ギブアップ

		mButtonRetrun.setVisibility(View.GONE);// 戻るボタン
		mLayputTimer.setVisibility(View.GONE);// 時間＋お金情報レイアウト
		mButtonPouse.setVisibility(View.GONE);// 一時停止ボタン
		mButtonDown.setVisibility(View.GONE);// ステージランク降下ボタン
		mButtonUp.setVisibility(View.GONE);// ステージランク上昇ボタン
		mTextStageRank.setVisibility(View.GONE);// ステージランクテキスト

		mLayoutResult.setVisibility(View.GONE);// 結果テーブル
		// mTextGetMoney.setVisibility(View.GONE);// 獲得賞金テキスト
		// mImageGetMonster.setVisibility(View.GONE);// 獲得モンスターイメージ
		// mButtonBack.setVisibility(View.GONE);// OKボタン
	}

	/**
	 * キーダウンイベント
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return super.onKeyDown(keyCode, msg);
		} else {
			return mMainThread.doKeyDown(keyCode, msg);
		}
	}

	/**
	 * キーアップイベント
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent msg) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return super.onKeyUp(keyCode, msg);
		} else {
			return mMainThread.doKeyUp(keyCode, msg);
		}
	}

	/**
	 * 画面タッチイベント
	 */
	@Override
	public boolean onTouchEvent(MotionEvent msg) {
		super.onTouchEvent(msg);
		Log.d("TEST", "onTouchEvent");
		gestureDetector.onTouchEvent(msg);
		return mMainThread.doTouch(msg);
	}

	/**
	 * その他ジェスチャー
	 */
	@Override
	public boolean onDoubleTap(MotionEvent msg) {
		// メッセージを表示
		if (mMainThread.getGameState() == MainThread.STATE_RUNNING
				&& mMainThread.getFullMonsterFlg()) {
			toast.show();
		}
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent msg) {
		// TODO 自動生成されたメソッド・スタブ
		return mMainThread.doDoubleTap(msg);
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent msg) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean onDown(MotionEvent msg) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public void onShowPress(MotionEvent msg) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public boolean onSingleTapUp(MotionEvent msg) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent msg1, MotionEvent msg2, float f,
			float f1) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public void onLongPress(MotionEvent msg) {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public boolean onFling(MotionEvent msg1, MotionEvent msg2, float f, float f1) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}
}

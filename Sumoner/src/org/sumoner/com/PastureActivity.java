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

import org.sumoner.com.PastureView.PastureThread;
import org.sumoner.com.common.BitmapCache;
import org.sumoner.com.common.GameUtil;
import org.sumoner.com.common.SystemUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class PastureActivity extends Activity implements View.OnClickListener,
		OnDoubleTapListener, OnGestureListener {

	private GestureDetector gestureDetector;
	private PastureThread mPastureThread;
	private PastureView mPastureView;// 本体ビュー

	// 詳細ダイアログViewコンポーネント
	ImageView imageView;
	private TextView noTitleView;// Noタイトル
	private TextView noContentView;// No
	private TextView nameTitleView;// Nameタイトル
	private TextView nameContentView;// Name
	private TextView lvTitleView;// Lvタイトル
	private TextView lvContentView;// LV
	private TextView statusTitleView;// statusタイトル
	private TextView hpContentView;// hp
	private TextView baseSkillTitleView;// 基本スキルタイトル
	private TextView baseSkillView;// 基本スキル
	private TextView baseAbilityTitleView1;// 基本アビリティタイトル
	private TextView baseAbilityView1;// 基本アビリティ
	private TextView baseAbilityView2;// 基本ビリティ
	private TextView additionalAbilityTitleView1;// 追加アビリティタイトル
	private TextView additionalAbilityView1;// 追加アビリティ
	private TextView additionalAbilityView2;// 追加アビリティ
	private TextView additionalAbilityView3;// 追加アビリティ

	// 配合確認ダイアログViewコンポーネント
	ImageView parendView1;
	ImageView parendView2;

	private ImageButton mButtonReturn;
	AlertDialog mReleaseDialog;
	AlertDialog mDeatilDialog;
	AlertDialog mCombiDialog;

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
		setContentView(R.layout.pasture);
		mPastureView = (PastureView) findViewById(R.id.PastureView);
		mPastureThread = mPastureView.getThread();
		mButtonReturn = (ImageButton)findViewById(R.id.pastrueReturnImageButton);
		mButtonReturn.setOnClickListener(this);
		// GestureDetecotorクラスのインスタンス生成
		gestureDetector = new GestureDetector(this, this);
		// mTextBattale = (TextView) findViewById(R.id.pastrueText);
		// mTextBattale.setTypeface(Typeface.createFromAsset(getAssets(),
		// "APJapanesefont.ttf"));
		// mTextLeave = (TextView) findViewById(R.id.pastrueSeeText);
		// mTextLeave.setTypeface(Typeface.createFromAsset(getAssets(),
		// "APJapanesefont.ttf"));
		createDialog();
		mPastureView.setReleaseDialog(mReleaseDialog);
		mPastureView.setDetailDialog(mDeatilDialog);
		mPastureView.setmCombineDialog(mCombiDialog);
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
		if (mButtonReturn.equals(v)) {
			mPastureThread.setRunning(false);
			GameUtil.terminateContext();
			BitmapCache.destoryAllImage();
			finish();
		} else {
			Log.d("JB VIEW", "unknown click " + v.getId());

			Log.d("JB VIEW", "state is  " + mPastureThread.mState);

		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mPastureThread = null;
		mPastureView = null;
		mButtonReturn = null;

		System.gc();
	}

	/**
	 * キーダウンイベント
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return super.onKeyDown(keyCode, msg);
		} else {
			return mPastureThread.doKeyDown(keyCode, msg);
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
			return mPastureThread.doKeyUp(keyCode, msg);
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
		return mPastureThread.doTouch(msg);
	}

	@Override
	public boolean onDown(MotionEvent msg) {
		// TODO 自動生成されたメソッド・スタ
		return false;
	}

	@Override
	public boolean onFling(MotionEvent msg1, MotionEvent msg2, float velocityX,
			float velocityY) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public void onLongPress(MotionEvent ms) {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public boolean onScroll(MotionEvent msg1, MotionEvent msg2,
			float distanceX, float distanceY) {
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
	public boolean onDoubleTap(MotionEvent msg) {
		// TODO 自動生成されたメソッド・スタブ
		return mPastureThread.doDoubleTap(msg);

	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent msg) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent msg) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	/**
	 * ダイアログを生成する
	 */
	private void createDialog() {
		// ************リリース時のダイアログ設定************
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
		// メッセージ設定
		mBuilder.setMessage("モンスターを手放しますか?");
		mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mPastureThread.setSelectRelease(true);
				mReleaseDialog.cancel();
			}
		});

		mBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mPastureThread.setSelectRelease(false);
						mReleaseDialog.cancel();
					}
				});
		mBuilder.setCancelable(true);
		this.mReleaseDialog = mBuilder.create();

		// ************詳細ダイアログ設定************
		mBuilder = new AlertDialog.Builder(this);
		// メッセージ設定
		LayoutInflater factory = LayoutInflater.from(PastureActivity.this);
		View inputView = factory.inflate(R.layout.monster_data, null);

		// Viewを独自拡張しているため、親Viewから子ビューを取り出して設定
		imageView = (ImageView) inputView
				.findViewById(R.id.combinationPlayerImageView);
		noTitleView = (TextView) inputView
				.findViewById(R.id.combination_No_TitleText);
		noTitleView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		noContentView = (TextView) inputView
				.findViewById(R.id.combination_No_ContentText);
		noContentView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		nameTitleView = (TextView) inputView
				.findViewById(R.id.combination_Name_TitleText);
		nameTitleView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		nameContentView = (TextView) inputView
				.findViewById(R.id.combination_Name_ContetText);
		nameContentView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		lvTitleView = (TextView) inputView
				.findViewById(R.id.combination_Lv_TitleText);
		lvTitleView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		lvContentView = (TextView) inputView
				.findViewById(R.id.combination_LV_ContetText);
		lvContentView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		statusTitleView = (TextView) inputView
				.findViewById(R.id.combination_Status_TitleText);
		statusTitleView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		hpContentView = (TextView) inputView
				.findViewById(R.id.combination_Hp_ContetText);
		hpContentView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		baseSkillTitleView = (TextView) inputView
				.findViewById(R.id.combination_BaseSkill_TitleText);
		baseSkillTitleView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		baseSkillView = (TextView) inputView
				.findViewById(R.id.combination_BaseSkill_ContetText);
		baseSkillView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		baseAbilityTitleView1 = (TextView) inputView
				.findViewById(R.id.combination_BaseAbility_TitleText);
		baseAbilityTitleView1.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		baseAbilityView1 = (TextView) inputView
				.findViewById(R.id.combination_BaseAbility_ContetText1);
		baseAbilityView1.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		baseAbilityView2 = (TextView) inputView
				.findViewById(R.id.combination_BaseAbility_ContetText2);
		baseAbilityView2.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		additionalAbilityTitleView1 = (TextView) inputView
				.findViewById(R.id.combination_AddAbility_TitleText);
		additionalAbilityTitleView1.setTypeface(Typeface.createFromAsset(
				getAssets(), "APJapanesefont.ttf"));
		additionalAbilityView1 = (TextView) inputView
				.findViewById(R.id.combination_AddAbility_ContetText1);
		additionalAbilityView1.setTypeface(Typeface.createFromAsset(
				getAssets(), "APJapanesefont.ttf"));
		additionalAbilityView2 = (TextView) inputView
				.findViewById(R.id.combination_AddAbility_ContetText2);
		additionalAbilityView2.setTypeface(Typeface.createFromAsset(
				getAssets(), "APJapanesefont.ttf"));
		additionalAbilityView3 = (TextView) inputView
				.findViewById(R.id.combination_AddAbility_ContetText3);
		additionalAbilityView3.setTypeface(Typeface.createFromAsset(
				getAssets(), "APJapanesefont.ttf"));
		setAllInvisible();
		mPastureView.setImageView(imageView);
		mPastureView.setNoContentView(noContentView);
		mPastureView.setNameContentView(nameContentView);
		mPastureView.setLvContentView(lvContentView);
		mPastureView.setHpContentView(hpContentView);
		mPastureView.setBaseSkillView(baseSkillView);
		mPastureView.setBaseAbilityView1(baseAbilityView1);
		mPastureView.setBaseAbilityView2(baseAbilityView2);
		mPastureView.setAdditionalAbilityView1(additionalAbilityView1);
		mPastureView.setAdditionalAbilityView2(additionalAbilityView2);
		mPastureView.setAdditionalAbilityView3(additionalAbilityView3);
		mBuilder.setView(inputView);
		mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mDeatilDialog.cancel();
			}
		});
		mBuilder.setCancelable(true);
		this.mDeatilDialog = mBuilder.create();

		// ************配合時のダイアログ設定************
		mBuilder = new AlertDialog.Builder(this);
		inputView = factory.inflate(R.layout.combination, null);
		// Viewを独自拡張しているため、親Viewから子ビューを取り出して設定
		parendView1 = (ImageView) inputView
				.findViewById(R.id.combinationParent1);
		// Viewを独自拡張しているため、親Viewから子ビューを取り出して設定
		parendView2 = (ImageView) inputView
				.findViewById(R.id.combinationParent2);
		mPastureView.setParentimageView1(parendView1);
		mPastureView.setParentimageView2(parendView2);
		mBuilder.setView(inputView);
		mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mPastureThread.setSelectCombination(true);
				mCombiDialog.cancel();
			}
		});

		mBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mPastureThread.setSelectCombination(false);
						mCombiDialog.cancel();
					}
				});
		mBuilder.setCancelable(true);
		this.mCombiDialog = mBuilder.create();
	}

	// 一旦Viewコンポーネントを不可視化
	private void setAllInvisible() {
		imageView.setVisibility(View.GONE);
		noContentView.setVisibility(View.GONE);
		nameContentView.setVisibility(View.GONE);
		lvContentView.setVisibility(View.GONE);
		hpContentView.setVisibility(View.GONE);
		baseSkillView.setVisibility(View.GONE);
		baseAbilityView1.setVisibility(View.GONE);
		baseAbilityView2.setVisibility(View.GONE);
		additionalAbilityView1.setVisibility(View.GONE);
		additionalAbilityView2.setVisibility(View.GONE);
		additionalAbilityView3.setVisibility(View.GONE);
	}
}

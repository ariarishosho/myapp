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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sumoner.com.common.BitmapCache;
import org.sumoner.com.common.GameUtil;
import org.sumoner.com.common.MetaInfo;
import org.sumoner.com.common.Monster;
import org.sumoner.com.common.SystemUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class CombinationActivity extends Activity implements
		View.OnClickListener {
	// Viewコンポーネント
	ImageView imageView;
	private ImageButton mButtonRetrun;// 戻るボタン
	private TextView noTitleView;// Noタイトル
	private TextView noContentView;// No
	private TextView nameTitleView;// Nameタイトル
	private TextView nameContentView;// Name
	private TextView lvTitleView;// Lvタイトル
	private TextView lvContentView;// LV
	private TextView statusTitleView;// statusタイトル
	private TextView hpContentView;// hp
	private TextView attackContentView;// attackタイトル
	private TextView baseSkillTitleView;// 基本スキルタイトル
	private TextView baseSkillView;// 基本スキル
	private TextView baseAbilityTitleView1;// 基本アビリティタイトル
	private TextView baseAbilityView1;// 基本アビリティ
	private TextView baseAbilityView2;// 基本ビリティ
	private TextView additionalAbilityTitleView1;// 追加アビリティタイトル
	private TextView additionalAbilityView1;// 追加アビリティ
	private TextView additionalAbilityView2;// 追加アビリティ
	private TextView additionalAbilityView3;// 追加アビリティ

	private List<Monster> mHoldMonsters = new ArrayList<Monster>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.monster_data);

		imageView = (ImageView) findViewById(R.id.combinationPlayerImageView);
//		mButtonRetrun = (ImageButton) findViewById(R.id.combinationReturnImageButton);
//		mButtonRetrun.setOnClickListener(this);
		noTitleView = (TextView) findViewById(R.id.combination_No_TitleText);
		noTitleView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		noContentView = (TextView) findViewById(R.id.combination_No_ContentText);
		noContentView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		nameTitleView = (TextView) findViewById(R.id.combination_Name_TitleText);
		nameTitleView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		nameContentView = (TextView) findViewById(R.id.combination_Name_ContetText);
		nameContentView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		lvTitleView = (TextView) findViewById(R.id.combination_Lv_TitleText);
		lvTitleView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		lvContentView = (TextView) findViewById(R.id.combination_LV_ContetText);
		lvContentView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		statusTitleView = (TextView) findViewById(R.id.combination_Status_TitleText);
		statusTitleView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		hpContentView = (TextView) findViewById(R.id.combination_Hp_ContetText);
		hpContentView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
//		attackContentView = (TextView) findViewById(R.id.combination_Attack_ContetText);
		attackContentView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		baseSkillTitleView = (TextView) findViewById(R.id.combination_BaseSkill_TitleText);
		baseSkillTitleView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		baseSkillView = (TextView) findViewById(R.id.combination_BaseSkill_ContetText);
		baseSkillView.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		baseAbilityTitleView1 = (TextView) findViewById(R.id.combination_BaseAbility_TitleText);
		baseAbilityTitleView1.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		baseAbilityView1 = (TextView) findViewById(R.id.combination_BaseAbility_ContetText1);
		baseAbilityView1.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		baseAbilityView2 = (TextView) findViewById(R.id.combination_BaseAbility_ContetText2);
		baseAbilityView2.setTypeface(Typeface.createFromAsset(getAssets(),
				"APJapanesefont.ttf"));
		additionalAbilityTitleView1 = (TextView) findViewById(R.id.combination_AddAbility_TitleText);
		additionalAbilityTitleView1.setTypeface(Typeface.createFromAsset(
				getAssets(), "APJapanesefont.ttf"));
		additionalAbilityView1 = (TextView) findViewById(R.id.combination_AddAbility_ContetText1);
		additionalAbilityView1.setTypeface(Typeface.createFromAsset(
				getAssets(), "APJapanesefont.ttf"));
		additionalAbilityView2 = (TextView) findViewById(R.id.combination_AddAbility_ContetText2);
		additionalAbilityView2.setTypeface(Typeface.createFromAsset(
				getAssets(), "APJapanesefont.ttf"));
		additionalAbilityView3 = (TextView) findViewById(R.id.combination_AddAbility_ContetText3);
		additionalAbilityView3.setTypeface(Typeface.createFromAsset(
				getAssets(), "APJapanesefont.ttf"));
		setAllInvisible();
		// 保持モンスター情報を取得
		mHoldMonsters = GameUtil.loadMonster();
		// 軌道画面からモンスターのIDを受け取る
		Intent i = getIntent();
		int ArrayResId = i.getIntExtra("resId", R.array.No001);
		updateForcusMons(ArrayResId);

	}

	/**
	 * フォーカスの当たっているモンスター情報を更新します
	 *
	 * @param resId
	 */
	private void updateForcusMons(int resId) {

		Monster target = null;

		for (Iterator<Monster> iterator = mHoldMonsters.iterator(); iterator
				.hasNext();) {
			Monster m = iterator.next();
			if (m.ArrayResId == resId) {
				target = m;
			}
		}
		Matrix matrix = new Matrix();
		matrix.setScale(2.0f, 2.0f);
		Bitmap mainBitmap = Bitmap.createBitmap(target.getBitmap(), 0, 0,
				target.getBitmap().getWidth(), target.getBitmap().getHeight(),
				matrix, true);
		// 画像
		imageView.setImageBitmap(mainBitmap);
		imageView.setVisibility(View.VISIBLE);
		// No
		noContentView.setText(target.No);
		noContentView.setVisibility(View.VISIBLE);
		// 種族名
		nameContentView.setText(target.name);
		nameContentView.setVisibility(View.VISIBLE);
		// レベル
		lvContentView.setText(target.Lv);
		lvContentView.setVisibility(View.VISIBLE);
		// HP
		hpContentView.setText("HP:" + String.valueOf(target.fullHp));
		hpContentView.setVisibility(View.VISIBLE);
		// 攻撃力
		attackContentView.setText("攻撃力:" + String.valueOf(target.attack));
		attackContentView.setVisibility(View.VISIBLE);
		// 攻撃スキル
		baseSkillView.setText(target.baseSkill.name);
		baseSkillView.setVisibility(View.VISIBLE);
		// アビリティ
		if (target.holdBaseAbilities.size() > 0) {
			baseAbilityView1.setText(target.holdBaseAbilities.get(0));// 基本アビリティ
			baseAbilityView1.setVisibility(View.VISIBLE);
		}
		if (target.holdBaseAbilities.size() > 1) {
			baseAbilityView2.setText(target.holdBaseAbilities.get(1));// 基本アビリティ
			baseAbilityView2.setVisibility(View.VISIBLE);
		}
		// 継承アビリティ
		if (target.holdAddAbilities != null) {
			if (target.holdAddAbilities.size() > 0) {
				additionalAbilityView1.setVisibility(View.VISIBLE);
				additionalAbilityView1.setText(target.holdAddAbilities.get(0));// 追加アビリティ
			}

			if (target.holdAddAbilities.size() > 1) {
				additionalAbilityView2.setVisibility(View.VISIBLE);
				additionalAbilityView2.setText(target.holdAddAbilities.get(1));// 追加アビリティ
			}

			if (target.holdAddAbilities.size() > 2) {
				additionalAbilityView3.setVisibility(View.VISIBLE);
				additionalAbilityView3.setText(target.holdAddAbilities.get(2));// 追加アビリティ
			}
		}
	}

	// 一旦Viewコンポーネントを不可視化
	private void setAllInvisible() {
		imageView.setVisibility(View.GONE);
		noContentView.setVisibility(View.GONE);
		nameContentView.setVisibility(View.GONE);
		lvContentView.setVisibility(View.GONE);
		hpContentView.setVisibility(View.GONE);
		attackContentView.setVisibility(View.GONE);
		baseSkillView.setVisibility(View.GONE);
		baseAbilityView1.setVisibility(View.GONE);
		baseAbilityView2.setVisibility(View.GONE);
		additionalAbilityView1.setVisibility(View.GONE);
		additionalAbilityView2.setVisibility(View.GONE);
		additionalAbilityView3.setVisibility(View.GONE);
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
			BitmapCache.destoryAllImage();
			finish();
		}
	}

}

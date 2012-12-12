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

import org.sumoner.com.common.BitmapCache;
import org.sumoner.com.common.GameUtil;
import org.sumoner.com.common.GridItem;
import org.sumoner.com.common.MetaInfo;
import org.sumoner.com.common.Monster;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

public class UpgradeActivity extends Activity implements View.OnClickListener,
		OnItemClickListener {

	// Viewコンポーネント
	private GridView gridView;
	private BitmapAdapter adapter;
	private TextView itemNameView;// 商品名
	private TextView itemComtentView;// 説明
	private TextView itemPriceView;// 価格
	private ImageButton mBackButton;// 戻るボタン
	private TextView shelfTextView;// 棚名
	private ImageButton mFrontButton;// 進む

	private ImageButton mButtonReturn;
	private int currntPage = 1;// 表示中のグリッド

	AlertDialog buyAlertDialog;
	AlertDialog equipAlertDialog;
	int itemId;// 選択中のアイテムID
	long itemPrice;
	Monster player;
	MetaInfo metainfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upgrade);

		ArrayList<GridItem> list = GameUtil.getUpgradeList(currntPage);
		adapter = new BitmapAdapter(getApplicationContext(),
				R.layout.list_item, list);

		gridView = (GridView) findViewById(R.id.upgradeGridView);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(this);

		itemNameView = (TextView) findViewById(R.id.upgradeTitleText);
		itemComtentView = (TextView) findViewById(R.id.upgradeContetText);
		itemPriceView = (TextView) findViewById(R.id.upgradePriceText);

		mBackButton = (ImageButton) findViewById(R.id.upgradeImageButtonArrowBack);
		shelfTextView = (TextView) findViewById(R.id.upgradeTextShelf);
		mBackButton.setOnClickListener(this);
		mFrontButton = (ImageButton) findViewById(R.id.upgradeImageButtonArrowFront);
		mFrontButton.setOnClickListener(this);

		mButtonReturn = (ImageButton) findViewById(R.id.upgradeReturnImageButton);
		mButtonReturn.setOnClickListener(this);

		// 現状の取得
		metainfo = GameUtil.loadMetaInfo();
		player = GameUtil.loadPlayer();
		// Toast toast = Toast.makeText(getApplicationContext(), "Toast表示します。",
		// Toast.LENGTH_LONG);

		// 購入時のダイアログ設定
		AlertDialog.Builder buyDialogBuilder = new AlertDialog.Builder(this);
		// メッセージ設定
		buyDialogBuilder.setMessage("購入しますか？");
		buyDialogBuilder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (metainfo.money - itemPrice < 0) {
							// 所持金が足りない場合
							buyAlertDialog.setMessage("金額が足りません");
							buyAlertDialog.cancel();
						}
						// 購入済みアイテムとしてセーブ
						GameUtil.saveBoughtItem(itemId);
						// 所持金を設定
						metainfo.money = metainfo.money - itemPrice;
						// TODO 特殊モンスターの購入処理
						GameUtil.saveMetaInfo(metainfo);
						// ダイアログを閉じる
						buyAlertDialog.cancel();
						if (currntPage == 2) {
							// 装備の場合、装備確認も実施
							equipAlertDialog.show();
						}
						// 品目リストを更新
						updateItemList();
						// メター情報のを登録
						metainfo = GameUtil.loadMetaInfo();
					}
				});
		buyDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						buyAlertDialog.cancel();
					}
				});
		buyDialogBuilder.setCancelable(true);
		this.buyAlertDialog = buyDialogBuilder.create();

		AlertDialog.Builder equipDialogBuilder = new AlertDialog.Builder(this);
		// アラートダイアログのメッセージを設定します
		equipDialogBuilder.setMessage("装備しますか？");
		// アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
		equipDialogBuilder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// OKの場合、プレーヤーの情報を更新
						GameUtil.savePlayer(itemId, player.classResId);
						// 品目リストを更新
						updateItemList();
					}
				});

		// アラートダイアログの否定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
		equipDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						equipAlertDialog.cancel();
					}
				});
		// アラートダイアログのキャンセルが可能かどうかを設定します
		equipDialogBuilder.setCancelable(true);
		this.equipAlertDialog = equipDialogBuilder.create();
	}

	/**
	 * クリックイベント
	 */
	@Override
	public void onClick(View v) {
		if (mButtonReturn.equals(v)) {
			// 返すデータ(Intent&Bundle)の作成
			// Intent data = new Intent();
			// setResult(RESULT_OK,data);
			// すべてのイメージファイルを破棄
			BitmapCache.destoryAllImage();
			finish();
			return;
		} else if (mBackButton.equals(v)) {
			// 表示pageをプラス
			if (currntPage > 1) {
				currntPage--;
			}
		} else if (mFrontButton.equals(v)) {
			if (currntPage < 4) {
				// 表示pageをマイナス
				currntPage++;
			}
		}
		updateItemList();
	}

	private void updateItemList() {
		switch (currntPage) {
		case 1:
			shelfTextView.setText(R.string.shelf01_name);
			break;

		case 2:
			shelfTextView.setText(R.string.shelf02_name);
			break;

		case 3:
			shelfTextView.setText(R.string.shelf03_name);
			break;
		}
		// グリッド内で表示するデータをロード
		ArrayList<GridItem> list = GameUtil.getUpgradeList(currntPage);
		adapter = new BitmapAdapter(getApplicationContext(),
				R.layout.list_item, list);
		// アダプタに設定して、表示
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(this);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		itemId = R.id.intgerDataText;
		// 商品について、arrayのIDを取得
		TextView v = (TextView) view.findViewById(itemId);
		int redId = Integer.parseInt(v.getText().toString());
		TypedArray itemTa = getResources().obtainTypedArray(redId);
		itemNameView.setText(itemTa.getString(0));// 名前
		itemComtentView.setText(itemTa.getString(1));// 説明
		itemPrice = itemTa.getInt(3, 0);
		itemPriceView.setText(String.valueOf(itemPrice));// 価格１
		itemTa.getInt(4, 0);// 価格２

		// 購入済みかどうかを設定
		CheckBox c = (CheckBox) view.findViewById(R.id.boolDataCheckbox);
		if (c.isChecked()) {
			// 購入済みの場合
			if (currntPage == 2)
				equipAlertDialog.show();
		} else {
			// 未購入の場合を表示します
			buyAlertDialog.show();
		}
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
}
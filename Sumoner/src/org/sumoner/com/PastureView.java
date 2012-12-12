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
 *
 */

// Android JET demonstration code:
// All inline comments related to the use of the JetPlayer class are preceded by "JET info:"

package org.sumoner.com;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.sumoner.com.common.BitmapCache;
import org.sumoner.com.common.Const;
import org.sumoner.com.common.DoubleTapGameEvent;
import org.sumoner.com.common.FrontBackComparator;
import org.sumoner.com.common.GameEvent;
import org.sumoner.com.common.GameUtil;
import org.sumoner.com.common.KeyGameEvent;
import org.sumoner.com.common.MetaInfo;
import org.sumoner.com.common.Monster;
import org.sumoner.com.common.TouchGameEvent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PastureView extends SurfaceView implements SurfaceHolder.Callback {

	// the number of asteroids that must be destroyed
	public static final int mSuccessThreshold = 50;

	// used to calculate level for mutes and trigger clip
	public int mHitStreak = 0;

	// total number asteroids you need to hit.
	public int mHitTotal = 0;

	// which music bed is currently playing?
	public int mCurrentBed = 0;

	// JET info: the JetBoyThread receives all the events from the JET player
	// JET info: through the OnJetEventListener interface.
	class PastureThread extends Thread {

		/**
		 * State-tracking constants.
		 */
		public static final int STATE_START = -1;
		public static final int STATE_PLAY = 0;
		public static final int STATE_LOSE = 1;
		public static final int STATE_PAUSE = 2;
		public static final int STATE_RUNNING = 3;
		public static final int STATE_PASTURE = 4;// フィールド変数が増えてきてメモリの無駄だから、パフォーマンスによってはクラスを分ける

		public boolean mInitialized = false;// ゲーム自体のイニシャル処理
		public boolean mInitializedState = false;// 各処理のイニシャル処理

		/** Queue for GameEvents */
		protected ConcurrentLinkedQueue<GameEvent> mEventQueue = new ConcurrentLinkedQueue<GameEvent>();

		/** Context for processKey to maintain state accross frames * */
		protected Object mKeyContext = null;

		// start, play, running, lose are the states we use
		public int mState;

		// has laser been fired and for how long?
		// user for fx logic on laser fire
		boolean mLaserOn = false;
		long mLastCombineTime = 0;
		long mCurrentTime;

		// フィールドモンスター

		private List<Monster> mMonsters;
		private int mBattleMemberNum = Const.SYSTEM_MAX_BATTLEMONSTER;// バトルメンバー数
		private List<String> mBattleMemberName = new Vector<String>(
				mBattleMemberNum);// バトルメンバーの名前（No XXX）を格納
		private List<Bitmap> mBattleMemberImage = new Vector<Bitmap>(
				mBattleMemberNum);// バトルメンバーの画像を格納
		private int mFrameSize = 100;// バトルメンバーの枠のサイズ
		private int mFramePadding = 10;
		private int mFrameMargin = 0;
		private Monster mReleaseMonster;// 手放すモンスター
		private Monster parent_1 = null;// 配合親モンスター
		private Monster parent_2 = null;// 配合親モンスター
		private Monster child = null;// 配合子モンスター
		private MetaInfo mMetainfo;
		private Paint mHitPaint = new Paint();
		private Paint mCommonPaint = new Paint();
		private Paint mBackGroundPaint = new Paint();
		private Paint mFriendPaint = new Paint();
		private Paint mTextPaint = new Paint();
		private Matrix mMatrix = new Matrix();

		// how much do we move the asteroids per beat?
		private int mPixelMoveX = 25;

		private Random mRandom = new Random();

		/** Message handler used by thread to interact with TextView */
		private Handler mHandler;

		/** Handle to the surface manager object we interact with */
		private SurfaceHolder mSurfaceHolder;

		/** Handle to the application context, used to e.g. fetch Drawables. */
		private Context mContext;

		/** Indicate whether the surface has been created & is ready to draw */
		private boolean mRun = false;

		// one second - used to update timer
		private int mTaskIntervalInMillis = 1000;

		/**
		 * Current height of the surface/canvas.
		 *
		 * @see #setSurfaceSize
		 */
		private int mCanvasHeight = 1;

		/**
		 * Current width of the surface/canvas.
		 *
		 * @see #setSurfaceSize
		 */
		private int mCanvasWidth = 1;

		// right to left scroll tracker for near and far BG

		private int mFieldWidth = 2400; // フィールドの全体サイズ
		private int mFieldHight = 300;// フィールドの全体サイズ
		private float mFieldX = 0;// フィールド中のオフセット（理論値オフセット）
		private float mFieldY = 0;// フィールド中のオフセット（理論値オフセット）
		private float mTouchX;// タッチX座標
		private float mTouchY;// タッチY座標
		private boolean mDoubleTap;// ダブルタプの２回処理制御
		private String actionDebug = "";
		boolean mRelease = false;
		boolean mCombine = false;
		boolean mCombineResult = false;
		String battletitle;
		String goodbye;
		String combine;
		String hold;
		double deg; // 角度
		// 牧場のセクターごとの区切り座標
		// 1次要素：セクター　２次要素：セクターの境界座標（{x1,y1,x2,y2}の用に格納セクター長方形）　
		float[][] sector = { { 0, 170, 780, 380 }, { 800, 170, 810, 380 } };

		// リソース
		Resources mRes;
		SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();

		/**
		 * This is the constructor for the main worker bee
		 *
		 * @param surfaceHolder
		 * @param context
		 * @param handler
		 */
		public PastureThread(SurfaceHolder surfaceHolder, Context context,
				Handler handler) {

			mSurfaceHolder = surfaceHolder;
			mHandler = handler;
			mContext = context;
			mRes = context.getResources();

			// always set state to start, ensure we come in from front door if
			// app gets tucked into background
			mState = STATE_PASTURE;

			// two background since we want them moving at different speeds
			// mBackgroundImageFar = BitmapFactory.decodeResource(mRes,
			// R.drawable.background_2);

			// mLaserShot = BitmapFactory.decodeResource(mRes,
			// R.drawable.laser);

			// mBackgroundImageNear = BitmapFactory.decodeResource(mRes,
			// R.drawable.background_3);

			// bitmap格納処理
			BitmapCache.setRes(mRes);
			// ユーティリティの使用設定
			GameUtil.setContext(mContext);
			setInitialGameState();
		}

		/**
		 * ゲーム状態初期化
		 */
		private void setInitialGameState() {
			// 味方モンスターの設定
			mMonsters = new Vector<Monster>();
			mMonsters = GameUtil.loadMonster();
			BitmapCache.setImage(R.drawable.waku_1, mFrameSize, mFrameSize);
			BitmapCache.setImage(R.drawable.waku_3, mFrameSize, mFrameSize);
			BitmapCache.setImage(R.drawable.waku_4, mFrameSize, mFrameSize);
			BitmapCache.setImage(R.drawable.waku_5, mFrameSize, mFrameSize);
			BitmapCache.setImage(R.drawable.cancel_battale_member, 0.5f);
			mInitialized = true;
			mInitializedState = false;
		}

		/**
		 * 牧場ステートの初期処理
		 */
		private void setInitialPastureState() {
			for (int i = 0; i < mBattleMemberNum; i++) {
				mBattleMemberName.add(null);
				mBattleMemberImage.add(null);
			}

			for (Monster monster : mMonsters) {
				if (monster.battleFlg) {
					// バトルメンバーの登録
					mBattleMemberName.set(monster.battlePlace, monster.No);
					Bitmap mem = monster.getBitmap();
					mem = Bitmap.createScaledBitmap(mem, mFrameSize
							- mFramePadding, mFrameSize - mFramePadding, true);
					mBattleMemberImage.set(monster.battlePlace, mem);
					// 動き用の角度を設定
					deg = mRandom.nextInt(35);
				}
				monster.dmX = (int) sector[monster.sector][0]
						+ mRandom.nextInt(600);
				monster.dmY = (int) sector[monster.sector][1]
						+ mRandom.nextInt(80);
			}
			mHitPaint.setColor(Color.WHITE);
			mInitializedState = true;// ステートの初期化完了
			mCommonPaint.setAntiAlias(true);
			mCommonPaint.setARGB(255, 51, 0, 153);
			mCommonPaint.setTextSize(35f);
			mCommonPaint.setTypeface(Typeface.createFromAsset(
					mContext.getAssets(), "APJapanesefont.ttf"));
			Shader s = new RadialGradient(mCanvasWidth / 2, mCanvasHeight / 2,
					mCanvasWidth * 0.9f, Color.YELLOW, Color.WHITE,
					Shader.TileMode.CLAMP);
			mBackGroundPaint.setShader(s);// グラデーションをセット

			mFriendPaint.setAntiAlias(true);
			mFriendPaint.setARGB(255, 51, 0, 153);
			mFriendPaint.setTextSize(23f);
			mFriendPaint.setTypeface(Typeface.createFromAsset(
					mContext.getAssets(), "APJapanesefont.ttf"));
			mTextPaint.setAntiAlias(true);
			mTextPaint.setARGB(255, 255, 0, 51);
			mTextPaint.setTextSize(20.0f);
			mTextPaint.setTypeface(Typeface.createFromAsset(
					mContext.getAssets(), "APJapanesefont.ttf"));
			battletitle = mRes.getString(R.string.BattleMember_1);
			goodbye = mRes.getString(R.string.GoodByeMember);
			combine = mRes.getString(R.string.Combine);
			hold = mRes.getString(R.string.Hold);
			mMetainfo = GameUtil.loadMetaInfo();

			sdf1 = new SimpleDateFormat("HH:mm:ss");
			date = new Date();
		}

		private void doDraw(Canvas canvas) {
			if (mState == STATE_PASTURE) {
				doDrawPasture(canvas);
			}
		}

		private void doDrawPasture(Canvas canvas) {
			try {
				if (mCombineResult) {
					// 交渉中
					canvas.drawPaint(mBackGroundPaint);
					// canvas.drawARGB(100, 255, 255, 0);
					mMatrix.setScale(2.0f, 2.0f);
					mMatrix.postTranslate(mCanvasWidth / 2
							- child.getBitmap().getWidth(), mCanvasHeight / 2
							- child.getBitmap().getHeight());
					canvas.drawBitmap(child.getBitmap(), mMatrix, null);

				} else {
					// 背景描画処理
					canvas.drawBitmap(
							BitmapCache.getImage(R.drawable.background_2), 0,
							0, null);
					canvas.drawBitmap(
							BitmapCache.getImage(R.drawable.background_pusture), 0,
							0, null);
					// 柵の描画
					// for (float[] saku : sector) {
					// for (float i = saku[0]; i < saku[2]; i += 50) {
					// canvas.drawBitmap(
					// BitmapCache.getImage(R.drawable.crystal_1), i
					// - mFieldX, saku[1], null);
					// canvas.drawBitmap(
					// BitmapCache.getImage(R.drawable.crystal_1), i
					// - mFieldX, saku[3], null);
					// }
					// for (float i = saku[1]; i < saku[3]; i += 50) {
					// canvas.drawBitmap(
					// BitmapCache.getImage(R.drawable.crystal_1),
					// saku[0] - mFieldX, i, null);
					// canvas.drawBitmap(
					// BitmapCache.getImage(R.drawable.crystal_1),
					// saku[2] - mFieldX, i, null);
					// }
					// }

					// バトルメンバー描画
					for (int i = 0; i < mBattleMemberNum; i++) {
						canvas.drawBitmap(
								BitmapCache.getImage(R.drawable.waku_1), i
										* mFrameSize, mFrameMargin, null);
						canvas.drawBitmap(BitmapCache
								.getImage(R.drawable.cancel_battale_member), i
								* mFrameSize, mFrameMargin, null);

						if (mBattleMemberName.get(i) != null) {
							canvas.drawBitmap(mBattleMemberImage.get(i), i
									* mFrameSize + 5, mFrameMargin + 5, null);
						}
						canvas.drawText(battletitle, i * mFrameSize
								+ mFramePadding + 13, mFrameSize
								- mFramePadding, mTextPaint);
					}
					// 配合描画
					canvas.drawBitmap(BitmapCache.getImage(R.drawable.waku_4),
							(mBattleMemberNum) * mFrameSize, mFrameMargin, null);
					if (parent_1 != null) {
						canvas.drawBitmap(parent_1.getBitmap(),
								(mBattleMemberNum) * mFrameSize + 5,
								mFrameMargin + 5, null);
					}
					canvas.drawBitmap(BitmapCache
							.getImage(R.drawable.cancel_battale_member),
							(mBattleMemberNum) * mFrameSize, mFrameMargin, null);
					canvas.drawText(combine, (mBattleMemberNum) * mFrameSize
							+ 30, mFrameSize - mFramePadding, mTextPaint);

					// モンスター保持数
					canvas.drawBitmap(BitmapCache.getImage(R.drawable.waku_5),
							(mBattleMemberNum + 1) * mFrameSize, mFrameMargin,
							null);
					canvas.drawText(
							String.valueOf(mMonsters.size() + "/"
									+ Const.SYSTEM_MAX_HOLDMONSTER),
							(mBattleMemberNum + 1) * mFrameSize+20, 50,
							mCommonPaint);
					canvas.drawText(hold, (mBattleMemberNum+1) * mFrameSize
							+ 10, mFrameSize - mFramePadding, mTextPaint);

					// お別れ描画
					canvas.drawBitmap(BitmapCache.getImage(R.drawable.waku_3),
							(mBattleMemberNum + 2) * mFrameSize, mFrameMargin,
							null);
					canvas.drawBitmap(BitmapCache.getImage(R.drawable.release),
							(mBattleMemberNum + 2) * mFrameSize,
							mFrameMargin + 5, null);
					canvas.drawText(goodbye, (mBattleMemberNum + 2)
							* mFrameSize+15, mFrameSize - mFramePadding,
							mTextPaint);

					// モンスター描画
					Collections.sort(mMonsters, new FrontBackComparator());
					canvas.save();
					for (Monster monster : mMonsters) {
						mMatrix.setScale(monster.vector, 1.0f);
						mMatrix.postTranslate(monster.dmX - mFieldX,
								monster.dmY);
						// drawBitmapMeshには直接Matrixを渡せないため、Canvas自体に渡す
						canvas.setMatrix(mMatrix);
						deg++;
						if (deg * 10 > 360) {
							deg = 0;
						}
						double rad = Math.toRadians(deg * 10); // 角度をﾗｼﾞｱﾝ値に変換
						double rad2 = Math.toRadians(deg * 5); // 角度をﾗｼﾞｱﾝ値に変換
						float[] array = {
								0,
								0,
								(float) (monster.getBitmap().getWidth() + 5 * Math
										.cos(rad)),
								(float) (-5 * Math.sin(rad)),
								(float) (7 * Math.cos(rad2)),
								monster.getBitmap().getHeight(),
								monster.getBitmap().getWidth(),
								monster.getBitmap().getHeight() };
						// モンスター画像を設定
						canvas.drawBitmapMesh(monster.getBitmap(), 1, 1, array,
								0, null, 0, null);
					}
					canvas.restore();
					for (Monster monster : mMonsters) {
						// モンスター情報描画
						canvas.drawText(monster.Lv + " " + monster.name,
								monster.vector == 1 ? monster.dmX - mFieldX
										: monster.dmX
												- mFieldX
												- monster.getBitmap()
														.getWidth(),
								monster.dmY - 10, mFriendPaint);

					}
				}
			} catch (Exception e) {
				// メモリ解放処理系でエラーが起きた場合は握りつぶす
				return;
			}
		}

		/**
		 * the heart of the worker bee
		 */
		@Override
		public void run() {
			long free = Runtime.getRuntime().freeMemory();
			Log.d("TEST", String.valueOf(free));

			while (mRun) {

				// Handlerを使用したスレッドのポストは一時保留。
				if (mCurrentTime <= System.currentTimeMillis() - 80) {
					Canvas c = null;
					if (mState == STATE_PASTURE) {
						// 配合情報更新
						// updateMonsterCombine();

						if (!mInitializedState) {
							// ステートの初期処理
							setInitialPastureState();
						}
						// ゲームステート更新
						updateGameState();
						updatePasture();
					}

					try {
						c = mSurfaceHolder.lockCanvas(null);
						// synchronized (mSurfaceHolder) {
						doDraw(c);
						// }
					} finally {
						if (c != null) {
							mSurfaceHolder.unlockCanvasAndPost(c);
						}
					}// end finally block
					mCurrentTime = System.currentTimeMillis();
				}// end while mrun block
			}
			// ビットマップの解放処理
			synchronized (mSurfaceHolder) {
				for (Bitmap reak : mBattleMemberImage) {
					if (reak != null && !reak.isRecycled()) {
						reak.recycle();
						reak = null;
					}
				}
			}
			Log.v("thread", "*************PastureThread end************");
		}

		/**
		 * 実行中のゲーム状態を設定するメインメソッド
		 */
		protected void updateGameState() {
			// ゲーム内の状態を更新する
			while (mRun) {
				// イベントキューから取り出す
				GameEvent event = mEventQueue.poll();
				if (event == null)
					break;
				// calls
				if (event instanceof KeyGameEvent) {
					mKeyContext = processKeyEvent((KeyGameEvent) event,
							mKeyContext);
				} else if (event instanceof TouchGameEvent) {
					// タップイベント
					TouchGameEvent touchEvent = (TouchGameEvent) event;
					switch (touchEvent.msg.getAction()) {
					case MotionEvent.ACTION_UP:
						synchronized (mSurfaceHolder) {
							// タッチ中フラグ
							for (Iterator<Monster> iterator = mMonsters
									.iterator(); iterator.hasNext();) {
								Monster m = iterator.next();
								if (m.druggingFlg) {
									// モンスタのdx,dyではユーザビリティが悪いため、画像の中心を使用する。
									float x = m.vector == 1 ? m.dmX
											+ m.getBitmap().getWidth() / 2
											: m.dmX - m.getBitmap().getWidth()
													/ 2;
									float y = m.dmY;

									// ドラッグしているモンスターを離した場合
									boolean outFlg = true;// 檻外フラグ
									// 檻の中に落とされたかをチェック
									for (int i = 0; i < sector.length; i++) {
										float[] join = sector[i];
										if (m.dmX > join[0] && m.dmY > join[1]
												&& m.dmX < join[2]
												&& m.dmY < join[3]) {
											m.sector = i;
											outFlg = false;
											break;
										}
									}
									// どの枠に落とされたかをチェック
									for (int i = 0; i < mBattleMemberNum + 3; i++) {
										// メンバー枠のサイズは64px*64px
										if (x > i * mFrameSize
												&& x < (i + 1) * mFrameSize
												&& y < mFrameSize) {
											if (i < mBattleMemberNum) {
												// 枠内に落とされた場合
												if (mBattleMemberName
														.contains(m.No)) {
													// 既にいる場合、または配合後
													continue;
												}
												// 枠表示用のモンスター画像生成
												Bitmap mem = m.getBitmap();
												mem = Bitmap
														.createScaledBitmap(
																mem,
																mFrameSize
																		- mFramePadding,
																mFrameSize
																		- mFramePadding,
																true);
												// バトルメンバーへ登録
												// スペースに空きがない場合、交換
												String monsterName = mBattleMemberName
														.get(i);
												// 元々バトルメンバーだったモンスターをバトルメンバーからはずす
												for (Iterator<Monster> search = mMonsters
														.iterator(); search
														.hasNext();) {
													Monster searchMonster = search
															.next();
													if (searchMonster.No == monsterName) {
														searchMonster.battleFlg = false;
														searchMonster.battlePlace = 99;
													}
												}
												mBattleMemberName.set(i, m.No);
												mBattleMemberImage.set(i, mem);

												// バトルメンバ-情報更新
												m.battleFlg = true;
												m.battlePlace = i;
												// }

												// モンスター檻なの中へ再描画
												m.dmX = (int) sector[m.sector][0]
														+ mRandom.nextInt(20);
												m.dmY = (int) sector[m.sector][1]
														+ mRandom.nextInt(20);
												outFlg = false;
												break;
											} else if (i == mBattleMemberNum) {
												// 配合
												if (parent_1 == null) {
													// 親１を設定
													parent_1 = m;
												} else {
													// 親２を設定
													parent_2 = m;
													Message msg = mHandler
															.obtainMessage();
													Bundle b = new Bundle();
													b.putString("combine",
															"combine");
													msg.setData(b);
													mHandler.sendMessage(msg);
												}

											} else if (i == mBattleMemberNum + 1) {
												// モンスター数
												// 特に何もしない
											} else if (i == mBattleMemberNum + 2) {
												// お別れ
												if (mMonsters.size() > 1) {

													Message msg = mHandler
															.obtainMessage();
													Bundle b = new Bundle();
													b.putString("release",
															"release");
													msg.setData(b);
													mHandler.sendMessage(msg);
													mReleaseMonster = m;
												}
											}
										}
									}
									// if (mMonsters.size() > 1) {
									// // お別れ処理　上部60pix
									// if (x >= (mCanvasWidth - 200 - 80)
									// && x <= (mCanvasWidth - 200 + 80)
									// && y >= 0 && y <= mFrameSize) {
									// Message msg = mHandler
									// .obtainMessage();
									// Bundle b = new Bundle();
									// b.putString("release", "release");
									// msg.setData(b);
									// mHandler.sendMessage(msg);
									// mReleaseMonster = m;
									// }
									// }
									if (outFlg) {
										// 檻の外側へ落とされたら,元のセクターの初期位置へ
										m.dmX = (int) sector[m.sector][0]
												+ mRandom.nextInt(20);
										m.dmY = (int) sector[m.sector][1]
												+ mRandom.nextInt(20);
									} else {
										// 檻の移動か、バトルメンバーの変更をした場合、セーブ
										GameUtil.saveMonster(mMonsters);
									}
									m.druggingFlg = false;
								}
							}
						}
						actionDebug = "ACTION_UP";
						break;
					case MotionEvent.ACTION_DOWN:
						float x = touchEvent.msg.getX();
						float y = touchEvent.msg.getY();
						synchronized (mSurfaceHolder) {
							// モンスターをタップしたかを判定
							for (Iterator<Monster> iterator = mMonsters
									.iterator(); iterator.hasNext();) {
								Monster m = iterator.next();
								// 対象モンスター座標範囲
								int targetRangeXFrom = m.vector == 1 ? m.dmX
										: m.dmX - m.getBitmap().getWidth();
								int targetRangeXTo = m.vector == 1 ? m.dmX
										+ m.getBitmap().getWidth() : m.dmX;
								int targetRangeYFrom = m.dmY;
								int targetRangeYTo = m.dmY
										+ m.getBitmap().getHeight();
								if (x > targetRangeXFrom && x < targetRangeXTo
										&& y > targetRangeYFrom
										&& y < targetRangeYTo) {
									// ドラッグ中フラグ
									m.druggingFlg = true;
									this.mTouchX = m.dmX;
									this.mTouchY = m.dmY;
									// 一匹つかんだら終了
									break;
								}
							}
							// 枠内のボタンを押下したかを判定
							for (int i = 0; i < mBattleMemberNum + 1; i++) {
								// メンバー枠のサイズは64px*64px
								if (x <= (i + 1) * mFrameSize
										&& x >= i * mFrameSize
										&& y <= mFrameSize + mFrameMargin) {
									if (i < mBattleMemberNum) {
										// バトルメンバのキャンセル処理
										if (mBattleMemberName.get(i) != null) {
											// バトルメンバーのローカル保持データを削除
											// 元々バトルメンバーだったモンスターをバトルメンバーからはずす
											for (Iterator<Monster> search = mMonsters
													.iterator(); search
													.hasNext();) {
												Monster searchMonster = search
														.next();
												if (searchMonster.No == mBattleMemberName
														.get(i)) {
													searchMonster.battleFlg = false;
													searchMonster.battlePlace = 99;
												}
											}
											mBattleMemberName.set(i, null);
											mBattleMemberImage.set(i, null);
											// 檻の移動か、バトルメンバーの変更をした場合、セーブ
											GameUtil.saveMonster(mMonsters);
										}
									} else {
										// 配合のキャンセル処理
										parent_1 = null;
									}
								}

							}
						}
						actionDebug = "ACTION_DOWN";
						break;

					case MotionEvent.ACTION_MOVE:
						// スライド中の座標を設定
						this.mTouchX = touchEvent.msg.getX();
						this.mTouchY = touchEvent.msg.getY();
						actionDebug = "ACTION_MOVE";
						if (mState == STATE_PASTURE) {
							// スワイプ処理
							this.mFieldX = mFieldX
									+ (mTouchX - touchEvent.msg.getX()) / 10;
							if (mFieldX < 0) {
								mFieldX = 0;
							} else if (mFieldX > mFieldWidth) {
								mFieldX = mFieldWidth;
							}
						}
						break;
					}
				} else if (event instanceof DoubleTapGameEvent) {
					DoubleTapGameEvent doubleTapEvent = (DoubleTapGameEvent) event;
					float x = doubleTapEvent.msg.getX();
					float y = doubleTapEvent.msg.getY();
					// モンスターをタップしたかを判定
					synchronized (mSurfaceHolder) {
						for (Iterator<Monster> iterator = mMonsters.iterator(); iterator
								.hasNext();) {
							Monster m = iterator.next();
							// 対象モンスター座標範囲
							int targetRangeXFrom = m.vector == 1 ? m.dmX
									: m.dmX - m.getBitmap().getWidth();
							int targetRangeXTo = m.vector == 1 ? m.dmX
									+ m.getBitmap().getWidth() : m.dmX;
							int targetRangeYFrom = m.dmY;
							int targetRangeYTo = m.dmY
									+ m.getBitmap().getHeight();
							if (x > targetRangeXFrom && x < targetRangeXTo
									&& y > targetRangeYFrom
									&& y < targetRangeYTo) {
								this.mTouchX = m.dmX;
								this.mTouchY = m.dmY;

								// 詳細情報を表示
								Message msg = mHandler.obtainMessage();
								Bundle b = new Bundle();
								b.putInt("detail", m.ArrayResId);
								msg.setData(b);
								mHandler.sendMessage(msg);
							}
						}
					}
				}
			}
		}

		/**
		 * 配合処理
		 */
		private void combainMonster() {
			if (parent_1 == null || parent_2 == null) {
				return;
			}
			Monster m = null;
			//レベルが上のほう主体となる。同レベルでは半々
			if(parent_1.Lv.equals(parent_2.Lv)){
			if (mRandom.nextInt(10) > 5) {
				m = parent_1;
			} else {
				m = parent_2;
			}}else if(Integer.parseInt( parent_1.Lv.replaceAll("Lv","")) > Integer.parseInt( parent_2.Lv.replaceAll("Lv",""))){
				m = parent_1;
			}else{
				m = parent_2;
			}
			// 両親が持っている術のアビリティを取得
			List<String> parentAblities = new ArrayList<String>();
			parentAblities.addAll(parent_1.holdBaseAbilities);
			parentAblities.addAll(parent_1.holdAddAbilities);
			parentAblities.addAll(parent_2.holdBaseAbilities);
			parentAblities.addAll(parent_2.holdAddAbilities);
			List<String> inheritedAbilities = setInheritedAbilities(
					parentAblities, m);
			m.holdAddAbilities = inheritedAbilities;
			this.child = m;
			return;
		}

		/**
		 * 継承されるアビリティの管理
		 *
		 * @param parentAbilities
		 * @param child
		 * @return
		 */
		private List<String> setInheritedAbilities(
				List<String> parentAbilities, Monster child) {
			synchronized (mSurfaceHolder) {
				List<String> inheritedAbility = new ArrayList<String>();

				// 継承候補のアビリティを設定
				for (Iterator<String> iterator = parentAbilities.iterator(); iterator
						.hasNext();) {
					String ability = iterator.next();
					// 子供が持っていないアビリティが継承される
					if (!child.holdBaseAbilities.contains(ability)) {

						int ran = mRandom.nextInt(256);
						// よりLvが高いアビリティほど継承されづらい
						if (ability.endsWith("Lv5")) {
							if (ran > 220) {
								if (!inheritedAbility.contains(ability)) {
									inheritedAbility.add(ability);
								}
							}
						} else if (ability.endsWith("Lv4")) {
							if (ran > 200) {
								if (!inheritedAbility.contains(ability)) {
									inheritedAbility.add(ability);
								}
							}
						} else if (ability.endsWith("Lv3")) {
							if (ran > 160) {
								if (!inheritedAbility.contains(ability)) {
									inheritedAbility.add(ability);
								}
							}
						} else if (ability.endsWith("Lv2")) {
							if (ran > 100) {
								if (!inheritedAbility.contains(ability)) {
									inheritedAbility.add(ability);
								}
							}
						} else if (ability.endsWith("Lv1")) {
							if (ran > 80) {
								if (!inheritedAbility.contains(ability)) {
									inheritedAbility.add(ability);
								}
							}
						}
					}
				}
				// 継承候補のアビリティを３つ以内にしぼる
				while (true) {
					if (inheritedAbility.size() <= 3) {
						break;
					}
					int ran = mRandom.nextInt(inheritedAbility.size());
					inheritedAbility.remove(ran);
				}
				return inheritedAbility;
			}
		}

		/**
		 * 牧場内でのモンスター情報を更新します
		 */
		private void updatePasture() {
			// モンスターのリリース処理
			updateRelease();
			updateCombine();
			// 各モンスター行動設定
			synchronized (mSurfaceHolder) {
				for (Monster m : mMonsters) {

					if (m.druggingFlg) {
						// つかみ中
						m.dmX = (int) mTouchX;
						m.dmY = (int) mTouchY;
					} else {
						// ランダム行動係数
						m.actionTimeCount--;
						if (m.actionTimeCount < 0) {
							// 新しい情報で更新
							m.actionTimeCount = mRandom.nextInt(50);
							m.actionParam = mRandom.nextInt(9);
						}

						float[] join = sector[m.sector];
						if (m.dmX < join[0] + 30) {
							m.dmX += m.speed;
							m.actionTimeCount = 0;
						} else if (m.dmY < join[1] + 30) {
							m.dmY += 3;
							m.actionTimeCount = 0;
						} else if (m.dmX > join[2] - 30) {
							m.dmX -= m.speed;
							m.actionTimeCount = 0;
						} else if (m.dmY > join[3] - 30) {
							m.dmY -= m.speed;
							m.actionTimeCount = 0;
						} else {
							switch (m.actionParam) {
							case 0:
								// 停止
								break;
							case 1:
								// 上
								m.dmY -= m.speed;
								break;
							case 2:
								// 右上
								m.dmX += m.speed;
								m.dmY -= m.speed;
								if (m.vector == -1) {
									m.dmX -= m.getBitmap().getWidth();
								}
								m.vector = 1;
								break;
							case 3:
								// 右
								m.dmX += m.speed;
								if (m.vector == -1) {
									m.dmX -= m.getBitmap().getWidth();
								}
								m.vector = 1;
								break;
							case 4:
								// 右下
								m.dmX += m.speed;
								m.dmY += m.speed;
								if (m.vector == -1) {
									m.dmX -= m.getBitmap().getWidth();
								}
								m.vector = 1;
								break;
							case 5:
								// 下
								m.dmY += m.speed;
								break;
							case 6:
								// 左下
								m.dmX -= m.speed;
								m.dmY += m.speed;
								if (m.vector == 1) {
									m.dmX += m.getBitmap().getWidth();
								}
								m.vector = -1;
								break;
							case 7:
								// 左
								m.dmX -= m.speed;
								if (m.vector == 1) {
									m.dmX += m.getBitmap().getWidth();
								}
								m.vector = -1;
								break;
							case 8:
								// 左上
								m.dmX -= m.speed;
								m.dmY -= m.speed;
								if (m.vector == 1) {
									m.dmX += m.getBitmap().getWidth();
								}
								m.vector = -1;
								break;
							}
						}
					}
				}
			}
		}

		/**
		 * 交配情報を更新する
		 */
		private void updateCombine() {
			if (mCombine) {
				// 配合処理
				combainMonster();
				mLastCombineTime = System.currentTimeMillis();
				mCombine = false;
				mCombineResult = true;
			}
			if (child != null) {
				if (parent_1 != null && parent_2 != null) {
					// 元々バトルメンバーだったモンスターをバトルメンバーからはずす
					for (int i = 0; i < mBattleMemberNum; i++) {
						if (mBattleMemberName.get(i) != null
								&& (parent_1.No == mBattleMemberName.get(i) || parent_2.No == mBattleMemberName
										.get(i))) {
							mBattleMemberName.set(i, null);
							mBattleMemberImage.set(i, null);
						}
					}
					// Monstersリストを編集
					synchronized (mSurfaceHolder) {
						mMonsters.add(child);
						mMonsters.remove(parent_1);
						mMonsters.remove(parent_2);

						// 交配モンスター情報を初期化
						parent_1 = parent_2 = null;
					}
				}
				if (System.currentTimeMillis() - mLastCombineTime > 2000) {
					mCombineResult = false;
					child = null;
				}
			}
		}

		/**
		 * モンスターを手放す処理を更新する
		 */
		private void updateRelease() {
			// バトルメンバーから外す
			if (mRelease && mReleaseMonster != null) {
				for (int i = 0; i < mBattleMemberNum; i++) {
					if (mBattleMemberName.get(i) != null
							&& mReleaseMonster.No == mBattleMemberName.get(i)) {
						mBattleMemberName.set(i, null);
						mBattleMemberImage.set(i, null);
					}
				}
				synchronized (mSurfaceHolder) {
					mMonsters.remove(mReleaseMonster);
					mReleaseMonster = null;
				}
				mRelease = false;
			}
		}

		/**
		 * This method handles the state updates that can be caused by key press
		 * events. Key events may mean different things depending on what has
		 * come before, to support this concept this method takes an opaque
		 * context object as a parameter and returns an updated version. This
		 * context should be set to null for the first event then should be set
		 * to the last value returned for subsequent events.
		 */
		protected Object processKeyEvent(KeyGameEvent event, Object context) {
			// Log.d(TAG, "key code is " + event.keyCode + " " + (event.up ?
			// "up":"down"));

			// If it is a key up on the fire key make sure we mute the
			// associated sound
			if (event.up) {
				if (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
					return null;
				}
			}
			// If it is a key down on the fire key start playing the sound and
			// update the context
			// to indicate that a key has been pressed and to ignore further
			// presses
			else {
				if (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER
						&& (context == null)) {
					return event;
				}
			}

			// Return the context unchanged
			return context;
		}

		/**
		 * Used to signal the thread whether it should be running or not.
		 * Passing true allows the thread to run; passing false will shut it
		 * down if it's already running. Calling start() after this was most
		 * recently called with false will result in an immediate shutdown.
		 *
		 * @param b
		 *            true to run, false to shut down
		 */
		public void setRunning(boolean b) {
			mRun = b;
			if (mRun == false) {
				interrupted();
			}
		}

		//
		// private void callActivity(int arrayResId) {
		// synchronized (mSurfaceHolder) {
		// Intent intent = new Intent(
		// getContext().getApplicationContext(),
		// CombinationActivity.class);
		// intent.putExtra("resId", arrayResId);
		// getContext().startActivity(intent);
		// }
		// }

		/**
		 * returns the current int value of game state as defined by state
		 * tracking constants
		 *
		 * @return
		 */
		public int getGameState() {
			synchronized (mSurfaceHolder) {
				return mState;
			}
		}

		/**
		 * Sets the game mode. That is, whether we are running, paused, in the
		 * failure state, in the victory state, etc.
		 *
		 * @see #setState(int, CharSequence)
		 * @param mode
		 *            one of the STATE_* constants
		 */
		public void setGameState(int mode) {
			synchronized (mSurfaceHolder) {
				setGameState(mode, null);
			}
		}

		/**
		 * モンスターを手放すか結果を設定する
		 *
		 * @param b
		 */
		public void setSelectRelease(boolean b) {
			mRelease = b;
		}

		/**
		 * モンスターを配合するかの結果を設定する
		 *
		 * @param b
		 */
		public void setSelectCombination(boolean b) {
			mCombine = b;
		}

		/**
		 * Sets state based on input, optionally also passing in a text message.
		 *
		 * @param state
		 * @param message
		 */
		public void setGameState(int state, CharSequence message) {

			synchronized (mSurfaceHolder) {

				// change state if needed
				if (mState != state) {
					mState = state;
				}

				if (mState == STATE_PLAY) {
					Resources res = mContext.getResources();
					// mBackgroundImageFar = BitmapFactory.decodeResource(res,
					// R.drawable.background_2);
					//
					// // don't forget to resize the background image
					// mBackgroundImageFar = Bitmap.createScaledBitmap(
					// mBackgroundImageFar, mCanvasWidth * 2,
					// mCanvasHeight, true);
					//
					// mBackgroundImageNear = BitmapFactory.decodeResource(res,
					// R.drawable.background_1);
					//
					// // don't forget to resize the background image
					// mBackgroundImageNear = Bitmap.createScaledBitmap(
					// mBackgroundImageNear, mCanvasWidth * 2,
					// mCanvasHeight, true);

				} else if (mState == STATE_RUNNING) {
					// When we enter the running state we should clear any old
					// events in the queue
					mEventQueue.clear();

					// And reset the key state so we don't think a button is
					// pressed when it isn't
					mKeyContext = null;
				} else if (mState == STATE_PASTURE) {
					Resources res = mContext.getResources();
					// mBackgroundImageFar = BitmapFactory.decodeResource(res,
					// R.drawable.background_2);
					//
					// // don't forget to resize the background image
					// mBackgroundImageFar = Bitmap.createScaledBitmap(
					// mBackgroundImageFar, mCanvasWidth * 2,
					// mCanvasHeight, true);
					//
					// mBackgroundImageNear = BitmapFactory.decodeResource(res,
					// R.drawable.background_1);
					//
					// // don't forget to resize the background image
					// mBackgroundImageNear = Bitmap.createScaledBitmap(
					// mBackgroundImageNear, mCanvasWidth * 2,
					// mCanvasHeight, true);
				}
			}
		}

		/**
		 * Add key press input to the GameEvent queue
		 */
		public boolean doKeyDown(int keyCode, KeyEvent msg) {
			mEventQueue.add(new KeyGameEvent(keyCode, false, msg));
			return true;
		}

		/**
		 * Add key press input to the GameEvent queue
		 */
		public boolean doKeyUp(int keyCode, KeyEvent msg) {
			mEventQueue.add(new KeyGameEvent(keyCode, true, msg));
			return true;
		}

		/**
		 * タッチイベントをキューに設定
		 */
		public boolean doTouch(MotionEvent msg) {
			mEventQueue.add(new TouchGameEvent(msg));
			return true;
		}

		/**
		 * ダブルタップベントをキューに設定
		 */
		public boolean doDoubleTap(MotionEvent msg) {
			mEventQueue.add(new DoubleTapGameEvent(msg));
			return true;
		}

		/* Callback invoked when the surface dimensions change. */
		public void setSurfaceSize(int width, int height) {
			// synchronized to make sure these all change atomically
			synchronized (mSurfaceHolder) {
				mCanvasWidth = width;
				mCanvasHeight = height;
				// don't forget to resize the background image
				// mBackgroundImageFar = Bitmap.createScaledBitmap(
				// mBackgroundImageFar, width * 2, height, true);

				// don't forget to resize the background image
				// mBackgroundImageNear = Bitmap.createScaledBitmap(
				// mBackgroundImageNear, width * 2, height, true);
			}
		}

		/**
		 * Pauses the physics update & animation.
		 */
		public void pause() {
			synchronized (mSurfaceHolder) {
				if (mState == STATE_RUNNING)
					setGameState(STATE_PAUSE);
				// if (mTimerTask != null) {
				// mTimerTask.cancel();
				// }
			}
		}

	}// end thread class

	public static final String TAG = "JetBoy";

	/** The thread that actually draws the animation */
	private PastureThread thread;
	private AlertDialog mReleaseDialog;
	private AlertDialog mCombineDialog;
	private ImageView parentimageView1;
	private ImageView parentimageView2;

	// 詳細側のダイアログ
	private AlertDialog mDetailDialog;
	ImageView imageView;
	private TextView noContentView;// No
	private TextView nameContentView;// Name
	private TextView lvContentView;// LV
	private TextView hpContentView;// hp
	private TextView baseSkillView;// 基本スキル
	private TextView baseAbilityView1;// 基本アビリティ
	private TextView baseAbilityView2;// 基本アビリティ
	private TextView additionalAbilityView1;// 追加アビリティ
	private TextView additionalAbilityView2;// 追加アビリティ
	private TextView additionalAbilityView3;// 追加アビリティ

	/**
	 * コンストラクタ
	 *
	 * @param context
	 * @param attrs
	 */
	public PastureView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		// create thread only; it's started in surfaceCreated()
		// except if used in the layout editor.
		if (isInEditMode() == false) {
			if (isInEditMode() == false) {
				thread = new PastureThread(getHolder(), getContext()
						.getApplicationContext(), new Handler() {
					@Override
					public void handleMessage(Message m) {
						String s = m.getData().getString("release");
						if (s != null) {
							// メッセージがポストされた場合
							mReleaseDialog.show();
						}
						int resId = m.getData().getInt("detail");
						if (resId != 0) {
							// メッセージがポストされた場合
							updateForcusMons(resId);
							mDetailDialog.show();
						}
						s = m.getData().getString("combine");
						if (s != null) {
							// メッセージがポストされた場合
							parentimageView1.setImageBitmap(thread.parent_1
									.getBitmap());
							parentimageView2.setImageBitmap(thread.parent_2
									.getBitmap());
							mCombineDialog.show();
						}
					}
				});
			}
		}

		setFocusable(true); // make sure we get key events

		Log.d(TAG, "@@@ done creating view!");
	}

	/**
	 * Standard window-focus override. Notice focus lost so we can pause on
	 * focus lost. e.g. user switches to take a call.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (!hasWindowFocus) {
			if (thread != null)
				thread.pause();
		}
	}

	/**
	 * Fetches the animation thread corresponding to this LunarView.
	 *
	 * @return the animation thread
	 */
	public PastureThread getThread() {
		return thread;
	}

	@Override
	/* Callback invoked when the surface dimensions change. */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		thread.setSurfaceSize(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		if (thread.getState() == Thread.State.TERMINATED) {
			thread = new PastureThread(getHolder(), getContext()
					.getApplicationContext(), new Handler() {
				@Override
				public void handleMessage(Message m) {
					String s = m.getData().getString("release");
					if (s != null) {
						// メッセージがポストされた場合
						mReleaseDialog.show();
					}
					int resId = m.getData().getInt("detail");
					if (resId != 0) {
						// メッセージがポストされた場合
						updateForcusMons(resId);
						mDetailDialog.show();
					}
					s = m.getData().getString("combine");
					if (s != null) {
						// メッセージがポストされた場合
						mCombineDialog.show();
					}
				}
			});
		}
		thread.setRunning(true);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {

			}
		}
	}

	/**
	 * フォーカスの当たっているモンスター情報を更新します
	 *
	 * @param resId
	 */
	private void updateForcusMons(int resId) {

		Monster target = null;

		for (Iterator<Monster> iterator = thread.mMonsters.iterator(); iterator
				.hasNext();) {
			Monster m = iterator.next();
			if (m.ArrayResId == resId) {
				target = m;
			}
		}
		setAllInvisible();
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
		hpContentView.setText("HP:" + String.valueOf(target.fullHp) + " 攻撃力:"
				+ String.valueOf(target.attack));
		hpContentView.setVisibility(View.VISIBLE);

		// 攻撃スキル
		baseSkillView.setText(target.baseSkill.name);
		baseSkillView.setVisibility(View.VISIBLE);
		// アビリティ
		if (target.holdBaseAbilities.size() > 0) {
			baseAbilityView1.setText(target.holdBaseAbilities.get(0));// 基本アビリティ
			baseAbilityView1.setVisibility(View.VISIBLE);
		}
		if (target.holdBaseAbilities.size() > 1
				&& !target.holdBaseAbilities.get(1).equals("")) {
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
		baseSkillView.setVisibility(View.GONE);
		baseAbilityView1.setVisibility(View.GONE);
		baseAbilityView2.setVisibility(View.GONE);
		additionalAbilityView1.setVisibility(View.GONE);
		additionalAbilityView2.setVisibility(View.GONE);
		additionalAbilityView3.setVisibility(View.GONE);
	}

	public void setReleaseDialog(AlertDialog mDailog) {
		this.mReleaseDialog = mDailog;
	}

	public void setDetailDialog(AlertDialog mDailog) {
		this.mDetailDialog = mDailog;
	}

	public ImageView getImageView() {
		return imageView;
	}

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}

	public TextView getNoContentView() {
		return noContentView;
	}

	public void setNoContentView(TextView noContentView) {
		this.noContentView = noContentView;
	}

	public TextView getNameContentView() {
		return nameContentView;
	}

	public void setNameContentView(TextView nameContentView) {
		this.nameContentView = nameContentView;
	}

	public TextView getLvContentView() {
		return lvContentView;
	}

	public void setLvContentView(TextView lvContentView) {
		this.lvContentView = lvContentView;
	}

	public TextView getHpContentView() {
		return hpContentView;
	}

	public void setHpContentView(TextView hpContentView) {
		this.hpContentView = hpContentView;
	}

	public TextView getBaseSkillView() {
		return baseSkillView;
	}

	public void setBaseSkillView(TextView baseSkillView) {
		this.baseSkillView = baseSkillView;
	}

	public TextView getBaseAbilityView1() {
		return baseAbilityView1;
	}

	public void setBaseAbilityView1(TextView baseAbilityView1) {
		this.baseAbilityView1 = baseAbilityView1;
	}

	public TextView getBaseAbilityView2() {
		return baseAbilityView2;
	}

	public void setBaseAbilityView2(TextView baseAbilityView2) {
		this.baseAbilityView2 = baseAbilityView2;
	}

	public TextView getAdditionalAbilityView1() {
		return additionalAbilityView1;
	}

	public void setAdditionalAbilityView1(TextView additionalAbilityView1) {
		this.additionalAbilityView1 = additionalAbilityView1;
	}

	public TextView getAdditionalAbilityView2() {
		return additionalAbilityView2;
	}

	public void setAdditionalAbilityView2(TextView additionalAbilityView2) {
		this.additionalAbilityView2 = additionalAbilityView2;
	}

	public TextView getAdditionalAbilityView3() {
		return additionalAbilityView3;
	}

	public void setAdditionalAbilityView3(TextView additionalAbilityView3) {
		this.additionalAbilityView3 = additionalAbilityView3;
	}

	public ImageView getParentimageView1() {
		return parentimageView1;
	}

	public ImageView getParentimageView2() {
		return parentimageView2;
	}

	public void setParentimageView2(ImageView parentimageView2) {
		this.parentimageView2 = parentimageView2;
	}

	public void setParentimageView1(ImageView parentimageView1) {
		this.parentimageView1 = parentimageView1;
	}

	public AlertDialog getmCombineDialog() {
		return mCombineDialog;
	}

	public void setmCombineDialog(AlertDialog mCombineDialog) {
		this.mCombineDialog = mCombineDialog;
	}
}

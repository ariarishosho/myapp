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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.sumoner.com.common.BitmapCache;
import org.sumoner.com.common.Const;
import org.sumoner.com.common.DoubleTapGameEvent;
import org.sumoner.com.common.FrontBackComparator;
import org.sumoner.com.common.GameEvent;
import org.sumoner.com.common.GameUtil;
import org.sumoner.com.common.KeyGameEvent;
import org.sumoner.com.common.LineComparator;
import org.sumoner.com.common.MetaInfo;
import org.sumoner.com.common.Monster;
import org.sumoner.com.common.Skill;
import org.sumoner.com.common.TouchGameEvent;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;

public class MainView extends SurfaceView implements SurfaceHolder.Callback {

	// the number of asteroids that must be destroyed
	public static final int mSuccessThreshold = 50;
	public boolean mWinFlg = false;// 勝利フラグ
	public boolean mLooseFlg = false;// 敗北フラグ
	// used to calculate level for mutes and trigger clip
	public int mHitStreak = 0;

	// total number asteroids you need to hit.
	public int mHitTotal = 0;

	// which music bed is currently playing?
	public int mCurrentBed = 0;

	// a lazy graphic fudge for the initial title splash
	// private Bitmap mTitleBG;
	//
	// private Bitmap mTitleBG2;

	// JET info: the JetBoyThread receives all the events from the JET player
	// JET info: through the OnJetEventListener interface.
	class MainThread extends Thread {

		/** Message handler used by thread to interact with TextView */
		private Handler mHandler;
		/** Handle to the surface manager object we interact with */
		private SurfaceHolder mSurfaceHolder;
		/** Handle to the application context, used to e.g. fetch Drawables. */
		private Context mContext;

		/**
		 * 状態定数
		 */
		public static final int STATE_START = 0;
		public static final int STATE_PLAY = 1;
		public static final int STATE_PAUSE = 2;
		public static final int STATE_RUNNING = 3;
		public static final int STATE_WIN = 4;
		public static final int STATE_LOSE = 5;
		public static final int STATE_GIVE_UP = 6;

		public boolean mInitialized = false;// ゲーム自体のイニシャル処理
		public boolean mInitializedState = false;// 各処理のイニシャル処理

		/** Queue for GameEvents */
		protected ConcurrentLinkedQueue<GameEvent> mEventQueue = new ConcurrentLinkedQueue<GameEvent>();

		/** Context for processKey to maintain state accross frames * */
		protected Object mKeyContext = null;

		// the timer display in seconds
		public int mTimerLimit;

		// used for internal timing logic.
		public final int TIMER_LIMIT = 300;

		// string value for timer display
		private String mTimerValue = "5:00";

		// start, play, running, lose are the states we use
		public int mState;

		// has laser been fired and for how long?
		// user for fx logic on laser fire
		boolean mLaserOn = false;

		// 交渉用タイマー
		long mNegoTime = 0;
		boolean mNegoresultShow = false;// 交渉結果表示フラグ
		String mNegoResltMessage;// 交渉結果表示メッセージ
		// フィールドモンスター
		private List<Monster> mMonsters = new ArrayList<Monster>();
		private List<Monster> mHoldMonsters = new ArrayList<Monster>();
		private Paint mHitPaint = new Paint();
		private Paint mNegoStatusPaint = new Paint();
		private Paint mHpHealthPaint = new Paint();
		private Paint mHpCoutionPaint = new Paint();
		private Paint mHpEmergencyPaint = new Paint();
		private Paint mBackGroundPaint = new Paint();
		private Paint mFriendPaint = new Paint();
		private Paint mEnemyPaint = new Paint();
		private Paint mTarget = new Paint();
		private Paint mFoucus = new Paint();
		private RectF rect;
		private Monster startMonster;
		private Monster endMonster;
		private Matrix mMatrix = new Matrix();
		private long mPassedTime;// クールタイム経過時間
		private long mCurrentTime;
		// ゲーム内乱数
		private Random mRandom = new Random();

		// スレッド実行中フラグ
		private boolean mRun = false;

		// 時間制御
		private Timer mTimer = null;
		private TimerTask mTimerTask = null;
		private int mTaskIntervalInMillis = 1000;
		// 獲得賞金
		private int getMoney = 0;

		// 契約成立パラメーター
		private int negoSuccess = 1;
		private boolean fullMonsterFlg = false;

		// 画面情報(画面サイズやDpiの違いによってリサイズ処理が必要)
		private int mCanvasHeight = 1;
		private int mCanvasWidth = 1;
		private float mCanvasScale = 1;

		// right to left scroll tracker for near and far BG
		private int mBGFarMoveX = 0;
		private int mBGNearMoveX = 0;
		private int mFieldWidth = 2400; // フィールドの全体サイズ
		private int mFieldHight = 300;// フィールドの全体サイズ
		private float mFieldX = 0;// フィールド中のオフセット（理論値オフセット）
		private float mFieldY = 0;// フィールド中のオフセット（理論値オフセット）
		private float mTouchX;// タッチX座標
		private float mTouchY;// タッチY座標
		private boolean mTouching;// タッチ中
		private String actionDebug = "";

		double deg; // 角度

		boolean negotiation = false; // 交渉中フラグ
		private Monster mNegotiationTarget;
		private float negotiationState = 0.2f;// 交渉中スータス（1で完了）
		private List<Float> negotiationParam = new ArrayList<Float>();
		private float negotiationAddParam = 0.05f;
		// リソース
		Resources mRes;

		/**
		 * This is the constructor for the main worker bee
		 *
		 * @param surfaceHolder
		 * @param context
		 * @param handler
		 */
		public MainThread(SurfaceHolder surfaceHolder, Context context,
				Handler handler) {
			mSurfaceHolder = surfaceHolder;
			mHandler = handler;
			mContext = context;
			mRes = context.getResources();
			// ステート初期化
			mState = STATE_START;
			// bitmap格納処理
			BitmapCache.setRes(mRes);
			WindowManager wm = (WindowManager) mContext
					.getSystemService(mContext.WINDOW_SERVICE);
			// ディスプレイのインスタンス生成
			DisplayMetrics metrics = new DisplayMetrics();
			wm.getDefaultDisplay().getMetrics(metrics);
			mCanvasHeight = metrics.heightPixels;
			mCanvasWidth = metrics.widthPixels;
			mCanvasScale = metrics.scaledDensity;
			Log.d("test", "density=" + metrics.density);
			Log.d("test", "densityDpi=" + metrics.densityDpi);
			Log.d("test", "scaledDensity=" + metrics.scaledDensity);
			Log.d("test", "widthPixels=" + metrics.widthPixels);
			Log.d("test", "heightPixels=" + metrics.heightPixels);
			Log.d("test", "xDpi=" + metrics.xdpi);
			Log.d("test", "yDpi=" + metrics.ydpi);
			BitmapCache
					.setWindowSize(mCanvasHeight, mCanvasWidth, mCanvasScale);

			// ユーティリティの使用設定
			GameUtil.setContext(mContext);
			setInitialGameState();
		}

		private void doDraw(Canvas canvas) {
			try {
				if (mState == STATE_RUNNING) {
					doDrawRunning(canvas);
				} else if (mState == STATE_START) {
					// if (mTitleBG2 == null) {
					// mTitleBG2 = BitmapFactory.decodeResource(mRes,
					// R.drawable.background_2);
					// }
					doDrawReady(canvas);
				} else if (mState == STATE_PLAY || mState == STATE_LOSE) {
					// if (mTitleBG2 == null) {
					// mTitleBG2 = BitmapFactory.decodeResource(mRes,
					// R.drawable.background_2);
					// }
					doDrawPlay(canvas);
				} else if (mState == STATE_PAUSE || mState == STATE_WIN) {
					doDrawPouse(canvas);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("canvas", "canvaserro");
			}
		}

		/**
		 * Draws current state of the game Canvas.
		 */
		private void doDrawPouse(Canvas canvas) throws Exception {
			doDrawRunning(canvas);
			canvas.drawARGB(100, 211, 211, 211);
		}

		/**
		 * Draws current state of the game Canvas.
		 */
		private void doDrawRunning(Canvas canvas) throws Exception {
			if (negotiation) {
				// 交渉中
				canvas.drawPaint(mBackGroundPaint);
				// canvas.drawARGB(100, 255, 255, 0);
				mMatrix.setScale(2.0f, 2.0f);
				mMatrix.postTranslate(mCanvasWidth / 2
						- mNegotiationTarget.getBitmap().getWidth(),
						mCanvasHeight / 2
								- mNegotiationTarget.getBitmap().getHeight());
				canvas.drawBitmap(mNegotiationTarget.getBitmap(), mMatrix, null);
				// mStatusPaint.setColor(Color.RED);
				canvas.drawRect(0, mCanvasHeight - 50, negotiationState < 0 ? 0
						: mCanvasWidth * negotiationState, mCanvasHeight - 10,
						mNegoStatusPaint);

			} else {
				// // 背景描画処理
				canvas.drawBitmap(
						BitmapCache.getImage(R.drawable.background_2), 0, 0,
						null);
				canvas.drawBitmap(
						BitmapCache.getImage(R.drawable.background_def), 0, 0,
						null);
				Collections.sort(mMonsters, new FrontBackComparator());
				canvas.save();
				// モンスター描画
				for (Monster monster : mMonsters) {
					mMatrix.setScale(monster.vector, 1.0f);
					mMatrix.postTranslate(monster.dmX - mFieldX, monster.dmY);
					// drawBitmapMeshには直接Matrixを渡せないため、Canvas自体に渡す
					canvas.setMatrix(mMatrix);

					// 影を設定
					rect.set(0, monster.getBitmap().getHeight() - 25, monster
							.getBitmap().getWidth(), monster.getBitmap()
							.getHeight() + 3);
					if (endMonster != null && endMonster == monster) {
						// フォーカスされている敵の場合
						canvas.drawOval(rect, mFoucus);
					} else if (startMonster != null && startMonster == monster) {
						// フォーカスしている味方の場合
						canvas.drawOval(rect, mTarget);
					} else {
						// 通常の影
						canvas.drawOval(rect, mHitPaint);
					}
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
									.cos(rad)), (float) (-5 * Math.sin(rad)),
							(float) (7 * Math.cos(rad2)),
							monster.getBitmap().getHeight(),
							monster.getBitmap().getWidth(),
							monster.getBitmap().getHeight() };

					if (monster.damages.isEmpty()) {
						// モンスター画像を設定
						canvas.drawBitmapMesh(monster.getBitmap(), 1, 1, array,
								0, null, 0, null);

					} else {
						canvas.drawBitmapMesh(monster.getBitmap(), 1, 1, array,
								0, null, 0, mHitPaint);
						// Canvas自体に既に位置情報を設定済みのためOffset値は０でよい
						canvas.drawBitmap(BitmapCache.getImage(R.drawable.hit),
								0, 0, null);
					}

				}
				canvas.restore();

				for (Monster monster : mMonsters) {
					mMatrix.setScale(monster.vector * monster.baseSkill.scale,
							monster.baseSkill.scale);
					// エフェクト画像を設定
					if (monster.baseSkill.attackFlg) {
						mMatrix.postTranslate(monster.baseSkill.dmX - mFieldX,
								monster.baseSkill.dmY);
						canvas.drawBitmap(monster.baseSkill.getEffectFlame(),
								mMatrix, null);
					}

					// HPバーを設定
					int hpBar = monster.hp * 100 / monster.fullHp;
					Paint p = null;

					canvas.drawRect(monster.vector == 1 ? monster.dmX - mFieldX
							: monster.dmX - mFieldX
									- monster.getBitmap().getWidth(),
							monster.dmY + monster.getBitmap().getHeight(),
							monster.vector == 1 ? monster.dmX - mFieldX + 100
									: monster.dmX - mFieldX
											- monster.getBitmap().getWidth()
											+ 100, monster.dmY
									+ monster.getBitmap().getHeight() + 5,
							mHitPaint);
					if (hpBar <= 20) {
						p = mHpEmergencyPaint;
					} else if (hpBar <= 50) {
						p = mHpCoutionPaint;
					} else {
						p = mHpHealthPaint;
					}
					canvas.drawRect(monster.vector == 1 ? monster.dmX - mFieldX
							: monster.dmX - mFieldX
									- monster.getBitmap().getWidth(),
							monster.dmY + monster.getBitmap().getHeight(),
							monster.vector == 1 ? monster.dmX - mFieldX + hpBar
									: monster.dmX - mFieldX
											- monster.getBitmap().getWidth()
											+ hpBar, monster.dmY
									+ monster.getBitmap().getHeight() + 5, p);

					// モンスター情報を設定
					if (monster.enemyFlg) {
						if (monster.druggingFlg) {
							canvas.drawBitmap(BitmapCache.getImage(
									R.drawable.battle, 0.5f),
									monster.vector == 1 ? monster.dmX - mFieldX
											- 30 : monster.dmX - mFieldX
											- monster.getBitmap().getWidth()
											- 40, monster.dmY - 30, null);
						}
						canvas.drawText(monster.Lv + " " + monster.name,
								monster.vector == 1 ? monster.dmX - mFieldX
										: monster.dmX
												- mFieldX
												- monster.getBitmap()
														.getWidth(),
								monster.dmY - 5, mEnemyPaint);
					} else {
						canvas.drawText(monster.Lv + " " + monster.name,
								monster.vector == 1 ? monster.dmX - mFieldX
										: monster.dmX
												- mFieldX
												- monster.getBitmap()
														.getWidth(),
								monster.dmY - 5, mFriendPaint);
					}

					if (!monster.enemyFlg && monster.druggingFlg) {
						// 味方、かつドラッグ中
						if (endMonster == null) {
							rect.set(monster.tmX - 50, monster.tmY - 15,
									monster.tmX + 50, monster.tmY + 15);
							canvas.drawOval(rect, mTarget);
						}
					}
				}
				// *******************************デバック用*************************************
				// for (Monster m : mMonsters) {
				// canvas.drawText(
				// actionDebug + "：論理オフセット=" + String.valueOf(mFieldX),
				// mTouchX, mTouchY, mHitPaint);
				// // モンスターのオフセット座標
				// canvas.drawCircle(m.dmX, m.dmY, 5, mHitPaint);
				// int srcX = 0;
				// int srcY = 0;
				//
				// // スキルの間合い範囲
				// srcX = m.dmX + m.getBitmap().getWidth() / 2 * m.vector;
				// srcY = m.dmY + m.getBitmap().getHeight() / 2;
				// // スキルの間合い範囲
				// int rangeXFrom = srcX - m.baseSkill.rengeX;
				// int rangeXTo = srcX + m.baseSkill.rengeX;
				// int rangeYFrom = srcY - m.baseSkill.rengeY;
				// int rangeYTo = srcY + m.baseSkill.rengeY;
				// canvas.drawRect(rangeXFrom, rangeYFrom, rangeXTo, rangeYTo,
				// mHitPaint);
				//
				// if (m.baseSkill.attackFlg) {
				//
				// // スキルのアタックレンジ
				// canvas.drawRect(m.baseSkill.attackRangeXFrom,
				// m.baseSkill.attackRangeYFrom,
				// m.baseSkill.attackRangeXTo,
				// m.baseSkill.attackRangeYTo, mHitPaint);
				// // スキルのオフセット値
				// canvas.drawCircle(m.baseSkill.dmX, m.baseSkill.dmY, 5,
				// mHitPaint);
				// }
				// // エフェクト画像を設定
				// if (m.addSkill_1 != null && m.addSkill_1.attackFlg) {
				//
				// // スキルのアタックレンジ
				// canvas.drawRect(m.addSkill_1.attackRangeXFrom,
				// m.addSkill_1.attackRangeYFrom,
				// m.addSkill_1.attackRangeXTo,
				// m.addSkill_1.attackRangeYTo, mHitPaint);
				// // スキルのオフセット値
				// canvas.drawCircle(m.addSkill_1.dmX, m.addSkill_1.dmY,
				// 5, mHitPaint);
				// // スキルの間合い範囲
				// srcX = m.dmX + m.getBitmap().getWidth() / 2 * m.vector;
				// srcY = m.dmY + m.getBitmap().getHeight() / 2;
				// rangeXFrom = srcX - m.addSkill_1.rengeX;
				// rangeXTo = srcX + m.addSkill_1.rengeX;
				// rangeYFrom = srcY - m.addSkill_1.rengeY;
				// rangeYTo = srcY + m.addSkill_1.rengeY;
				//
				// canvas.drawRect(rangeXFrom, rangeYFrom, rangeXTo,
				// rangeYTo, mHitPaint);
				// }
				// if (m.addSkill_2 != null && m.addSkill_2.attackFlg) {
				//
				// // スキルのアタックレンジ
				// canvas.drawRect(m.addSkill_2.attackRangeXFrom,
				// m.addSkill_2.attackRangeYFrom,
				// m.addSkill_2.attackRangeXTo,
				// m.addSkill_2.attackRangeYTo, mHitPaint);
				// // スキルのオフセット値
				// canvas.drawCircle(m.addSkill_2.dmX, m.baseSkill.dmY, 5,
				// mHitPaint);
				// // スキルの間合い範囲
				// srcX = m.dmX + m.getBitmap().getWidth() / 2 * m.vector;
				// srcY = m.dmY + m.getBitmap().getHeight() / 2;
				// rangeXFrom = srcX - m.addSkill_2.rengeX;
				// rangeXTo = srcX + m.addSkill_2.rengeX;
				// rangeYFrom = srcY - m.addSkill_2.rengeY;
				// rangeYTo = srcY + m.addSkill_2.rengeY;
				//
				// canvas.drawRect(rangeXFrom, rangeYFrom, rangeXTo,
				// rangeYTo, mHitPaint);
				// }
				// }
				// ********************************デバック用*************************************/

			}
		}

		private void doDrawReady(Canvas canvas) throws Exception {
			canvas.drawBitmap(BitmapCache.getImage(R.drawable.background_2), 0,
					0, null);
			canvas.drawBitmap(BitmapCache.getImage(R.drawable.top, 0.85f),
					(mCanvasWidth - BitmapCache.getImage(R.drawable.top, 0.85f)
							.getWidth()) / 2, 0, null);
			canvas.drawBitmap(BitmapCache.getImage(R.drawable.subject, 0.65f),
					(mCanvasWidth - BitmapCache.getImage(R.drawable.subject, 0.65f)
							.getWidth()) / 2, 0, null);

		}

		private void doDrawPlay(Canvas canvas) throws Exception {
			canvas.drawBitmap(BitmapCache.getImage(R.drawable.background_2), 0,
					0, null);
		}

		/**
		 * ゲーム状態初期化
		 */
		private void setInitialGameState() {
			mTimerLimit = TIMER_LIMIT;
			// 味方モンスターの設定
			// 初回起動確認処理
			GameUtil.gameInit();
			// ***********************デバッグ様データ投入**********************************
			// Vector<Monster> dummyData = new Vector<Monster>();
			// dummyData.add(GameUtil.getBaseMonsterInfo(R.array.No002));
			// dummyData.add(GameUtil.getBaseMonsterInfo(R.array.No003));
			// GameUtil.saveMonster(dummyData);
			// MetaInfo meta = new MetaInfo();
			// meta.money = 100;
			// meta.appointments1Rank = 1;
			// meta.appointments2Rank = 1;
			// meta.appointments3Rank = 1;
			// List list = new ArrayList<MetaInfo.MiniMonster>();
			// MetaInfo.MiniMonster min = meta.new MiniMonster();
			// min.ArrayId = R.array.No002;
			// min.holdAddAbilityNames = new ArrayList<String>();
			// min.addSkillNames = new ArrayList<String>();
			// list.add(min);
			//
			// min = meta.new MiniMonster();
			// min.ArrayId = R.array.No003;
			// min.holdAddAbilityNames = new ArrayList<String>();
			// min.addSkillNames = new ArrayList<String>();
			// list.add(min);
			// meta.takenMonster = list;
			// GameUtil.saveMetaInfo(meta);

			// 保存情報の取得
			mHoldMonsters = GameUtil.loadMonster();
			mMonsters = new ArrayList<Monster>();
			if (mHoldMonsters.size() >= Const.SYSTEM_MAX_HOLDMONSTER) {
				fullMonsterFlg = true;
			}
			// 戦闘要員だけを抽出
			synchronized (mSurfaceHolder) {
				for (Iterator<Monster> iterator = mHoldMonsters.iterator(); iterator
						.hasNext();) {
					Monster m = iterator.next();
					if (m.battleFlg) {
						mMonsters.add(m);
					}
				}
			}
			// 戦闘要員を並べ替え
			Collections.sort(mMonsters, new LineComparator());
			// mMonsters.addElement(GameUtil.loadPlayer());
			mTimer = new Timer();
			mInitialized = true;
			mInitializedState = false;
			mHitStreak = 0;
			mHitTotal = 0;
		}

		/**
		 * ラニングステートの初期処理
		 */
		private void setInitialRunningState() {
			// 敵モンスターの設定
			mMonsters.addAll(GameUtil.getEnemyMonsterInfo(Stage));

			// 交渉パラメータの初期化
			mNegotiationTarget = null;
			negotiationAddParam = 0;
			negotiationState = 0;

			int frendM = 0;
			int enemyM = 0;

			for (Monster monster : mMonsters) {
				if (monster.enemyFlg) {
					enemyM++;
					// 敵の場合
					monster.dmX = mCanvasWidth - 30;
					// monster.dmX = 850 - enemyM * 70;
					monster.dmY = (150 + enemyM * 80)
							- monster.getBitmap().getHeight();
					// 次いでにVectorも設定
					monster.vector = -1;
				} else {
					frendM++;
					// 見方の場合
					monster.dmX = 30;
					// monster.dmX = frendM * 70;
					monster.dmY = (150 + frendM * 80)
							- monster.getBitmap().getHeight();

					// 交渉パラメータの設定
					negotiationAddParam += monster.negoAttack;
					negotiationState += monster.negoAttack;
				}
				monster.deg = mRandom.nextInt(36);
			}

			// ヒット時の透過処理用
			mHitPaint.setAntiAlias(true);
			mHitPaint.setAlpha(60);

			Shader s = new RadialGradient(mCanvasWidth / 2, mCanvasHeight / 2,
					mCanvasWidth * 0.9f, Color.YELLOW, Color.WHITE,
					Shader.TileMode.CLAMP);
			mBackGroundPaint.setShader(s);// グラデーションをセット
			// mCommonPaint.setARGB(100, 255, 255, 0);
			s = new LinearGradient(0, 0, mCanvasWidth, mCanvasHeight,
					Color.BLUE, Color.RED, Shader.TileMode.CLAMP);
			mNegoStatusPaint.setShader(s);
			mHpHealthPaint.setARGB(200, 0, 51, 255);
			mHpCoutionPaint.setARGB(200, 255, 255, 0);
			mHpEmergencyPaint.setARGB(200, 255, 0, 51);
			mFriendPaint.setAntiAlias(true);
			mFriendPaint.setARGB(255, 51, 0, 153);
			mFriendPaint.setTextSize(23f);
			mFriendPaint.setTypeface(Typeface.createFromAsset(
					mContext.getAssets(), "APJapanesefont.ttf"));
			mEnemyPaint.setAntiAlias(true);
			mEnemyPaint.setARGB(255, 255, 0, 51);
			mEnemyPaint.setTextSize(23.0f);
			mEnemyPaint.setTypeface(Typeface.createFromAsset(
					mContext.getAssets(), "APJapanesefont.ttf"));
			mTarget.setARGB(100, 51, 0, 255);
			mTarget.setAntiAlias(true);
			mTarget.setStrokeWidth(7);
			mTarget.setStyle(Paint.Style.FILL); // スタイルは線(Stroke)を指定する

			mFoucus.setARGB(100, 204, 0, 0);
			mFoucus.setStrokeWidth(7);
			mFoucus.setStyle(Paint.Style.FILL); // スタイルは線(Stroke)を指定する
			mFoucus.setAntiAlias(true);

			rect = new RectF();
			mInitializedState = true;// ステートの初期化完了
			mWinFlg = false; // 終了時の処理
			mLooseFlg = false;
		}

		/**
		 * スレッドの実行メイン
		 */
		@Override
		public void run() {

			// while running do stuff in this loop...bzzz!
			while (mRun) {
				// Handlerを使用したスレッドポストは一時保留。
				if (mCurrentTime <= System.currentTimeMillis() - 80) {
					Canvas c = null;
					if (mState == STATE_RUNNING) {
						if (!mInitializedState) {
							// ステートの初期処理
							setInitialRunningState();
						}
						updateGameState();

						if (!negotiation) {
							updateMonster();
						} else {
							updateNagotiation();
						}
						mPassedTime = System.currentTimeMillis();

						// kick off the timer task for counter update if not
						// already
						// initialized
						if (mTimerTask == null) {
							mTimerTask = new TimerTask() {
								@Override
								public void run() {
									doTaskManager();
								}
							};
							mTimer.schedule(mTimerTask, mTaskIntervalInMillis);
						}// end of TimerTask init block

					}// end of STATE_RUNNING block
					else if (mState == STATE_PLAY && !mInitialized) {
						setInitialGameState();
					} else if (mState == STATE_LOSE && mLooseFlg) {
						mInitialized = false;

					} else if (mState == STATE_WIN && mWinFlg) {
						mInitialized = false;
						// 勝利して一回だけ個々を通る
						if (mNegotiationTarget != null) {
							synchronized (mSurfaceHolder) {
								mNegotiationTarget.enemyFlg = false;
								mHoldMonsters.add(mNegotiationTarget);
							}
						}
						GameUtil.saveMonster(mHoldMonsters);
						mWinFlg = false;
					} else if (mState == STATE_GIVE_UP) {
						mLooseFlg = true;
					}

					try {
						c = mSurfaceHolder.lockCanvas(null);
						doDraw(c);
					} finally {
						// do this in a finally so that if an exception is
						// thrown
						// during the above, we don't leave the Surface in an
						// inconsistent state
						if (c != null) {
							mSurfaceHolder.unlockCanvasAndPost(c);
						}
					}// end finally block
					mCurrentTime = System.currentTimeMillis();
				}
			}// end while mrun block
			Log.v("thread", "*************MainThread end************");
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
						mTouching = false;
						actionDebug = "ACTION_UP";
						// モンスターをタップしたかを判定
						synchronized (mSurfaceHolder) {

							if (endMonster != null) {
								for (Iterator<Monster> iterator = mMonsters
										.iterator(); iterator.hasNext();) {
									Monster m = iterator.next();
									if (!m.enemyFlg && m.druggingFlg) {
										// ここからは敵の判定条件
										m.druggingFlg = false;
										m.target = endMonster;
									}
								}
							}
						}
						// ターゲットモンスターの初期化
						startMonster = null;
						endMonster = null;
						break;
					case MotionEvent.ACTION_DOWN:
						mTouching = true;
						this.mTouchX = touchEvent.msg.getX();
						this.mTouchY = touchEvent.msg.getY();
						actionDebug = "ACTION_DOWN";

						// 交渉時の処理
						if (negotiation) {
							negotiationParam.add(negotiationAddParam);
						} else {
							// 掴んだ時の処理
							float downX = touchEvent.msg.getX();
							float downY = touchEvent.msg.getY();

							// モンスターをタップしたかを判定
							synchronized (mSurfaceHolder) {
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
									if (downX > targetRangeXFrom
											&& downX < targetRangeXTo
											&& downY > targetRangeYFrom
											&& downY < targetRangeYTo) {
										if (!m.enemyFlg) {

											// ドラッグしている訳ではなく、ターゲット指定のフラグとして使用する
											// if (m.druggingFlg) {
											// m.druggingFlg = false;
											// } else {
											// m.druggingFlg = true;
											// }
											// 味方の場合
											m.druggingFlg = true;
											m.tmX = downX;
											m.tmY = downY;
											startMonster = m;
											break;

										}
									} else {
										m.druggingFlg = false;
									}

								}
							}
						}

						break;
					case MotionEvent.ACTION_MOVE:
						actionDebug = "ACTION_MOVE";
						float moveX = touchEvent.msg.getX();
						float moveY = touchEvent.msg.getY();
						// モンスターをタップしたかを判定
						synchronized (mSurfaceHolder) {
							for (Iterator<Monster> iterator = mMonsters
									.iterator(); iterator.hasNext();) {
								Monster m = iterator.next();

								if (!m.enemyFlg && m.druggingFlg) {
									// 常に味方の場合は情報を更新
									m.tmX = moveX;
									m.tmY = moveY;

								} else if (m.enemyFlg) {
									// 対象モンスター座標範囲
									int targetRangeXFrom = m.vector == 1 ? m.dmX
											: m.dmX - m.getBitmap().getWidth();
									int targetRangeXTo = m.vector == 1 ? m.dmX
											+ m.getBitmap().getWidth() : m.dmX;
									int targetRangeYFrom = m.dmY;
									int targetRangeYTo = m.dmY
											+ m.getBitmap().getHeight();
									if (moveX > targetRangeXFrom
											&& moveX < targetRangeXTo
											&& moveY > targetRangeYFrom
											&& moveY < targetRangeYTo) {
										// 敵までドラッグしてきた場合は、endMonsterを登録
										endMonster = m;
										break;
									} else {
										endMonster = null;
									}
								}

							}
						}
						break;
					}
				} else if (event instanceof DoubleTapGameEvent) {
					DoubleTapGameEvent doubleTapEvent = (DoubleTapGameEvent) event;
					float x = doubleTapEvent.msg.getX();
					float y = doubleTapEvent.msg.getY();

					synchronized (mSurfaceHolder) {
						for (Iterator<Monster> iterator = mMonsters.iterator(); iterator
								.hasNext();) {
							Monster m = iterator.next();
							if (m.enemyFlg) {
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
									// 敵モンスターの
									setNagotiation(m);
								}
							}
						}
					}
				}
			}
		}

		/**
		 * 交渉するモンスターを設定します
		 */
		private void setNagotiation(Monster target) {
			// 牧場がいっぱいかノ判定
			if (!fullMonsterFlg) {
				boolean holdFlg = false;// 保持モンスターフラグ
				for (Monster m : mHoldMonsters) {
					if (m.No == target.No) {
						// すでに保持しているモンスター
						holdFlg = true;
					}
				}
				if (!holdFlg) {
					// 牧場がいっぱい出なく、保持していない場合
					negotiation = true;
					mNegotiationTarget = target;
				}
			}
		}

		/**
		 * 交渉中のパラメータを設定します
		 */
		private void updateNagotiation() {
			// 交渉モンスターの抵抗値
			negotiationState -= mNegotiationTarget.negoRegist;
			for (Iterator<Float> iterator = negotiationParam.iterator(); iterator
					.hasNext();) {
				// 手持ちモンスタの総交渉値
				negotiationState += iterator.next();
				iterator.remove();
			}

			if (negotiationState < 0) {
				// 契約失敗
				negotiation = false;// 交渉終了
				mNegotiationTarget = null;// ターゲットを初期化
				mNegoResltMessage = mRes.getString(R.string.negosiationMiss);
				mNegoresultShow = true;// 交渉結果の表示フラグをオン
			} else if (negotiationState > negoSuccess) {
				// 契約成功
				negotiation = false;// 交渉終了
				mNegoResltMessage = mRes.getString(R.string.negosiationSuccess);
				mNegoresultShow = true;// 交渉結果の表示フラグをオン
			}
		}

		/**
		 * モンスターの行動を更新します TODO ハードコーディングと常時策敵処理を後からなんとかする。多分、処理スペックがたりない
		 *
		 */
		private void updateMonster() {

			// HP設定
			setMonsterDamage();

			// 各モンスターの最も近い敵を探す
			synchronized (mSurfaceHolder) {
				for (Iterator<Monster> iterator = mMonsters.iterator(); iterator
						.hasNext();) {
					Monster m = iterator.next();
					double reachYLenght = 0;// 敵とのY軸距離
					double reachXLenght = 0;// 敵とのX軸距離
					Monster nearMonster = null;// 正面いる勢力が違うモンスター
					Monster closeMonster = null;
					boolean vert = false;
					for (Monster research : mMonsters) {
						// if (research.druggingFlg && !m.enemyFlg) {
						// // 攻撃対象指定してある場合
						// nearMonster = research;
						// float abs = Math.abs(m.dmY - research.dmY);
						// if (reachYLenght > abs || reachYLenght == 0) {
						// reachYLenght = abs;
						// }
						// break;
						// } else
						if (m.enemyFlg != research.enemyFlg) {
							// 攻撃あ相手を指定していない場合、かつ敵見方が違う場合
							// ①自分の前にいる敵を優先的に攻撃する
							float abs = Math.abs(m.dmY - research.dmY);
							if (reachYLenght > abs || reachYLenght == 0) {
								reachYLenght = abs;
								nearMonster = research;
							}
							// ② 最も近い的を攻撃する(至近距離に相手がいる場合)
							abs = Math.abs(m.dmX - research.dmX);
							if (reachXLenght > abs || reachXLenght == 0) {
								reachXLenght = abs;
								closeMonster = research;
							}
						}
					}
					// 策敵対象が見つからない（どちらかが全滅）
					if (nearMonster != null) {
						if (reachYLenght > 15
								|| (m.target != null && Math.abs(m.dmY
										- m.target.dmY) > 15)) {
							vert = true;
						}
						if (reachYLenght < 10 && reachXLenght < 220
								&& closeMonster != null) {
							nearMonster = closeMonster;
						}
						if (m.enemyFlg) {
							setEnemyMonsterMove(m, nearMonster, m.enemyFlg,
									vert);
						} else {
							setFriendMonsterMove(m, m.target, m.enemyFlg, vert);
						}
					} else {
						if (m.enemyFlg) {
							// 敗北
							mLooseFlg = true;
						} else {
							// 勝利
							mWinFlg = true;
						}
					}
				}
				// 相打ち
				if (mMonsters.size() == 0) {
					// 勝利
					mWinFlg = true;
				}
			}
		}

		private void setFriendMonsterMove(Monster m, Monster nearMonster,
				boolean EnemyFlg, boolean vertical) {

			if (nearMonster == null)
				return;
			/*
			 * 攻撃の基点は攻撃モンスターの中心からの距離で判定 攻撃される側のモンスターのヒット判定は画像ファイルに触れたかで判定
			 */
			// 元モンスターの中心座標
			int srcX = m.dmX + m.getBitmap().getWidth() / 2 * m.vector;
			int srcY = m.dmY + m.getBitmap().getHeight() / 2;

			// 対象モンスターの中心座標
			int targetX = nearMonster.dmX + m.getBitmap().getWidth() / 2
					* m.vector;
			int targetY = nearMonster.dmY + m.getBitmap().getHeight() / 2;

			// 対象モンスター座標範囲
			int targetRangeXFrom = nearMonster.vector == 1 ? nearMonster.dmX
					: nearMonster.dmX - nearMonster.getBitmap().getWidth();
			int targetRangeXTo = nearMonster.vector == 1 ? nearMonster.dmX
					+ nearMonster.getBitmap().getWidth() : nearMonster.dmX;
			int targetRangeYFrom = nearMonster.dmY;
			int targetRangeYTo = nearMonster.dmY
					+ nearMonster.getBitmap().getHeight();

			// スキルの間合い範囲
			int rangeXFrom = srcX - m.baseSkill.rengeX;
			int rangeXTo = srcX + m.baseSkill.rengeX;
			int rangeYFrom = srcY - m.baseSkill.rengeY;
			int rangeYTo = srcY + m.baseSkill.rengeY;

			if (mCanvasWidth < m.dmX) {
				// 枠内へ戻す
				m.dmX--;
			} else if (m.dmX < 0) {
				// 枠内へ戻す
				m.dmX++;
			} else if (mCanvasHeight - 50 < m.dmY) {
				// 枠内へ戻す
				m.dmY--;
			} else if (m.dmY < 50) {
				// 枠内へ戻す
				m.dmY++;

			} else if (rangeXFrom <= targetRangeXTo
					&& rangeXTo >= targetRangeXFrom
					&& rangeYFrom <= targetRangeYTo
					&& rangeYTo >= targetRangeYFrom) {
				// 間合い判定

				// 向き判定
				if (srcX > targetX) {
					m.vector = -1;
				} else {
					m.vector = 1;
				}
				// クールタイム判定
				if (mPassedTime - m.baseSkill.lastAttackTime > m.baseSkill.cooltime
						- m.ability.upCoolDown) {
					// 間合いに入った敵がいた場合、攻撃アクションへ
					m.baseSkill.attackFlg = true;
					// 現在の時間を最終攻撃時間に設定
					m.baseSkill.lastAttackTime = mPassedTime;
				} else {
					// クールタイム中はちょっと動く
					if (mRandom.nextInt(7) > 3)
						m.dmX -= m.speed * m.vector;
				}

			} else {

				// ランダム行動係数
				int range;
				if (vertical) {
					range = 15;
				} else {
					range = 9;
				}
				int r = mRandom.nextInt(range);

				if (r > 8) {
					// 縦移動
					if (srcY > targetY) {
						m.dmY -= m.speed;
					} else {
						m.dmY += m.speed;
					}
				} else if (r > 2) {
					// 横移動
					if (srcX > targetX) {
						m.dmX -= m.speed;
						if (m.vector == 1) {
							m.dmX += m.getBitmap().getWidth();
						}
						m.vector = -1;

					} else {
						m.dmX += m.speed;
						if (m.vector == -1) {
							m.dmX -= m.getBitmap().getWidth();
						}
						m.vector = 1;
					}
				}
			}

			// 攻撃処理
			setAttack(m, nearMonster, srcX, srcY, EnemyFlg, m.baseSkill);
			if (m.addSkill_1 != null) {
				setAttack(m, nearMonster, srcX, srcY, EnemyFlg, m.addSkill_1);
			}
			if (m.addSkill_2 != null) {
				setAttack(m, nearMonster, srcX, srcY, EnemyFlg, m.addSkill_2);
			}
		}

		/**
		 * モンスターの行動、攻撃判定を設定する。
		 *
		 * @param m
		 *            比べる元のモンスター
		 * @param nearMonster
		 *            最も近いモンスター
		 * @param EnemyFlg
		 *            敵Flg
		 * @param vertical
		 *            立て移動フラグ
		 */
		private void setEnemyMonsterMove(Monster m, Monster nearMonster,
				boolean EnemyFlg, boolean vertical) {

			/*
			 * 攻撃の基点は攻撃モンスターの中心からの距離で判定 攻撃される側のモンスターのヒット判定は画像ファイルに触れたかで判定
			 */
			// 元モンスターの中心座標
			int srcX = m.dmX + m.getBitmap().getWidth() / 2 * m.vector;
			int srcY = m.dmY + m.getBitmap().getHeight() / 2;

			// 対象モンスターの中心座標
			int targetX = nearMonster.dmX + m.getBitmap().getWidth() / 2
					* m.vector;
			int targetY = nearMonster.dmY + m.getBitmap().getHeight() / 2;

			// 対象モンスター座標範囲
			int targetRangeXFrom = nearMonster.vector == 1 ? nearMonster.dmX
					: nearMonster.dmX - nearMonster.getBitmap().getWidth();
			int targetRangeXTo = nearMonster.vector == 1 ? nearMonster.dmX
					+ nearMonster.getBitmap().getWidth() : nearMonster.dmX;
			int targetRangeYFrom = nearMonster.dmY;
			int targetRangeYTo = nearMonster.dmY
					+ nearMonster.getBitmap().getHeight();

			// スキルの間合い範囲
			int rangeXFrom = srcX - m.baseSkill.rengeX;
			int rangeXTo = srcX + m.baseSkill.rengeX;
			int rangeYFrom = srcY - m.baseSkill.rengeY;
			int rangeYTo = srcY + m.baseSkill.rengeY;

			if (mCanvasWidth < m.dmX) {
				// 枠内へ戻す
				m.dmX--;
			} else if (m.dmX < 0) {
				// 枠内へ戻す
				m.dmX++;
			} else if (mCanvasHeight - 50 < m.dmY) {
				// 枠内へ戻す
				m.dmY--;
			} else if (m.dmY < 50) {
				// 枠内へ戻す
				m.dmY++;
			} else if (rangeXFrom <= targetRangeXTo
					&& rangeXTo >= targetRangeXFrom
					&& rangeYFrom <= targetRangeYTo
					&& rangeYTo >= targetRangeYFrom) {
				// 間合い判定

				// 向き判定
				if (srcX > targetX) {
					m.vector = -1;
				} else {
					m.vector = 1;
				}
				// クールタイム判定
				if (mPassedTime - m.baseSkill.lastAttackTime > m.baseSkill.cooltime
						- m.ability.upCoolDown) {
					// 間合いに入った敵がいた場合、攻撃アクションへ
					m.baseSkill.attackFlg = true;
					// 現在の時間を最終攻撃時間に設定
					m.baseSkill.lastAttackTime = mPassedTime;
				} else {
					// クールタイム中はちょっと動く
					if (mRandom.nextInt(7) > 3)
						m.dmX -= m.speed * m.vector;
				}

			} else {
				// ランダム行動係数
				int range;
				if (vertical) {
					range = 15;
				} else {
					range = 9;
				}
				int r = mRandom.nextInt(range);

				if (r > 8) {
					// 縦移動
					if (srcY > targetY) {
						m.dmY -= m.speed;
					} else {
						m.dmY += m.speed;
					}
				} else if (r > 2) {
					// 横移動
					if (srcX > targetX) {
						m.dmX -= m.speed;
						if (m.vector == 1) {
							m.dmX += m.getBitmap().getWidth();
						}
						m.vector = -1;

					} else {
						m.dmX += m.speed;
						if (m.vector == -1) {
							m.dmX -= m.getBitmap().getWidth();
						}
						m.vector = 1;
					}
				}
				// }
			}

			// 攻撃処理
			setAttack(m, nearMonster, srcX, srcY, EnemyFlg, m.baseSkill);
			if (m.addSkill_1 != null) {
				setAttack(m, nearMonster, srcX, srcY, EnemyFlg, m.addSkill_1);
			}
			if (m.addSkill_2 != null) {
				setAttack(m, nearMonster, srcX, srcY, EnemyFlg, m.addSkill_2);
			}
		}

		private void setAttack(Monster m, Monster nearMonster, int srcX,
				int srcY, boolean EnemyFlg, Skill skill) {
			// 攻撃モーション
			if (skill.attackFlg) {
				// 攻撃アクション中、かつヒット状態の攻撃段階である
				setSkillRange(m, nearMonster, srcX, srcY, skill);
				// 攻撃判定有の範囲
				int attackRangeXFrom = skill.attackRangeXFrom;
				int attackRangeXTo = skill.attackRangeXTo;
				int attackRangeYFrom = skill.attackRangeYFrom;
				int attackRangeYTo = skill.attackRangeYTo;

				// 攻撃ヒット判定。攻撃の当たり判定は最も近い敵だけではなく、すべてのモンスターが対象
				if (skill.hitFlameFlg) {
					for (Iterator<Monster> iterator = mMonsters.iterator(); iterator
							.hasNext();) {
						Monster reach = iterator.next();
						// 敵味方が違う場合
						if (EnemyFlg != reach.enemyFlg) {
							int reachRangeXFrom = reach.vector == 1 ? reach.dmX
									: reach.dmX - reach.getBitmap().getWidth();
							int reachRangeXTo = reach.vector == 1 ? reach.dmX
									+ nearMonster.getBitmap().getWidth()
									: reach.dmX;
							int raechRangeYFrom = reach.dmY;
							int reachRangeYTo = reach.dmY
									+ reach.getBitmap().getHeight();

							if (attackRangeXFrom <= reachRangeXTo
									&& attackRangeXTo >= reachRangeXFrom
									&& attackRangeYFrom <= reachRangeYTo
									&& attackRangeYTo >= raechRangeYFrom) {
								// ダメージを設定
								int damage = skill.baseDamage + m.attack;
								if (skill.kind.contains(Const.normal)) {
									// ノーマル
									damage = (int) (damage * reach.ability.registPhysical);
								} else if (skill.kind.contains(Const.fire)) {
									// 炎
									damage = (int) (damage * reach.ability.registFire);
								} else if (skill.kind.contains(Const.ice)) {
									// 氷
									damage = (int) (damage * reach.ability.registIce);
								} else if (skill.kind.contains(Const.thunder)) {
									// 雷
									damage = (int) (damage * reach.ability.registThunder);
								} else if (skill.kind.contains(Const.earth)) {
									// 地
									damage = (int) (damage * reach.ability.registEarth);
								} else if (skill.kind.contains(Const.wind)) {
									// 風
									damage = (int) (damage * reach.ability.registWind);
								}
								reach.damages.add(damage);
								if (mRandom.nextInt(40) <= skill.knockBack
										* reach.ability.registKnockBack
										+ m.ability.addKnockBack) {
									// ノックバック判定
									reach.dmX += 20 * m.vector;
									// ノックバック中は攻撃中止。
									reach.baseSkill.flameCount = 0;
									reach.baseSkill.attackFlg = false;
									reach.baseSkill.hitFlameFlg = false;
								}
							}
						}
					}
				}
			}
		}

		private void setSkillRange(Monster m, Monster nearMonster, int srcX,
				int srcY, Skill skill) {
			// 攻撃アクション中、かつヒット状態の攻撃段階である
			// 攻撃判定有の範囲
			int attackRangeXFrom = 0;
			int attackRangeXTo = 0;
			int attackRangeYFrom = 0;
			int attackRangeYTo = 0;

			// 攻撃の座標を設定
			if (skill.kind.contains(Const.normal)) {
				// ノーマル
				if (skill.kind.equals(Const.SKILL_SLASH_LV1)) {
					// Lv1ノーマル1
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX : skill.dmX
							- skill.attackRengeX;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX : skill.dmX;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 20;

				} else if (skill.kind.equals(Const.SKILL_SLASH_LV2)) {
					// Lv1ノーマル2
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 30
							: skill.dmX - skill.attackRengeX - 30;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 30 : skill.dmX - 30;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 50;

				} else if (skill.kind.equals(Const.SKILL_SLASH_LV3)) {
					// Lv1ノーマル3
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 30
							: skill.dmX - skill.attackRengeX - 30;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 30 : skill.dmX - 30;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 50;

				} else if (skill.kind.equals(Const.SKILL_ENAGYBOLL_LV1)) {
					// Lv2ノーマル1
					if (skill.flameCount >= 7) {
						skill.dmX += 10 * m.vector;
					} else {
						skill.dmX = srcX + (int) skill.positionX * m.vector;
						skill.dmY = srcY + (int) skill.positionY;
					}

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 35
							: skill.dmX - skill.attackRengeX - 35;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 35
							+ skill.attackRengeX : skill.dmX - 35;
					attackRangeYFrom = skill.dmY + 40;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 40;

				} else if (skill.kind.equals(Const.SKILL_ENAGYBOLL_LV2)) {
					// Lv2ノーマル2
					if (skill.flameCount >= 7) {
						skill.dmX += 10 * m.vector;
					} else {
						skill.dmX = srcX + (int) skill.positionX * m.vector;
						skill.dmY = srcY + (int) skill.positionY;
					}

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 50
							+ skill.attackRengeX : skill.dmX - 50;
					attackRangeYFrom = skill.dmY + 60;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 60;

				} else if (skill.kind.equals(Const.SKILL_ENAGYBOLL_LV3)) {
					// Lv2ノーマル3
					if (skill.flameCount >= 7) {
						skill.dmX += 10 * m.vector;
					} else {
						skill.dmX = srcX + (int) skill.positionX * m.vector;
						skill.dmY = srcY + (int) skill.positionY;
					}

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 65
							: skill.dmX - skill.attackRengeX - 65;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 65
							+ skill.attackRengeX : skill.dmX - 65;
					attackRangeYFrom = skill.dmY + 80;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 80;

				} else if (skill.kind.equals(Const.SKILL_ARROW_LV1)) {
					// Lv3ノーマル1
					if (skill.flameCount >= 4) {
						skill.dmX += 10 * m.vector;
					} else {
						skill.dmX = srcX + (int) skill.positionX * m.vector;
						skill.dmY = srcY + (int) skill.positionY;
					}

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 30
							: skill.dmX - skill.attackRengeX - 30;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 60
							+ skill.attackRengeX : skill.dmX - 60;
					attackRangeYFrom = skill.dmY + 30;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 30;

				} else if (skill.kind.equals(Const.SKILL_ARROW_LV2)) {
					// Lv3ノーマル2
					if (skill.flameCount >= 4) {
						skill.dmX += 10 * m.vector;
					} else {
						skill.dmX = srcX + (int) skill.positionX * m.vector;
						skill.dmY = srcY + (int) skill.positionY;
					}

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 30
							: skill.dmX - skill.attackRengeX - 30;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 60
							+ skill.attackRengeX : skill.dmX - 60;
					attackRangeYFrom = skill.dmY + 30;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 30;

				} else if (skill.kind.equals(Const.SKILL_ARROW_LV3)) {
					// Lv3ノーマル3
					if (skill.flameCount >= 4) {
						skill.dmX += 10 * m.vector;
					} else {
						skill.dmX = srcX + (int) skill.positionX * m.vector;
						skill.dmY = srcY + (int) skill.positionY;
					}

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 30
							: skill.dmX - skill.attackRengeX - 30;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 60
							+ skill.attackRengeX : skill.dmX - 60;
					attackRangeYFrom = skill.dmY + 30;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 30;

				}

			}
			// 炎
			else if (skill.kind.contains(Const.fire)) {
				if (skill.kind.equals(Const.SKILL_FIRE_LV1)) {
					// Lv1ファイヤー1の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX : skill.dmX
							- skill.attackRengeX;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX : skill.dmX;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;

				} else if (skill.kind.equals(Const.SKILL_FIRE_LV2)) {
					// Lv1ファイヤー2の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX : skill.dmX
							- skill.attackRengeX;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX : skill.dmX;
					attackRangeYFrom = skill.dmY + 30;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 30;

				} else if (skill.kind.equals(Const.SKILL_FIRE_LV3)) {
					// Lv1ファイヤー3の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX : skill.dmX
							- skill.attackRengeX;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX : skill.dmX;
					attackRangeYFrom = skill.dmY + 30;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 30;

				} else if (skill.kind.equals(Const.SKILL_FLAME_LV1)) {
					// Lv2ファイヤー1の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 40
							: skill.dmX - skill.attackRengeX - 20;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 20 : skill.dmX - 40;
					attackRangeYFrom = skill.dmY + 40;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_FLAME_LV2)) {
					// Lv2ファイヤー2の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 40
							: skill.dmX - skill.attackRengeX - 20;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 20 : skill.dmX - 40;
					attackRangeYFrom = skill.dmY + 40;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_FLAME_LV3)) {
					// Lv2ファイヤー3の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 40
							: skill.dmX - skill.attackRengeX - 20;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 20 : skill.dmX - 40;
					attackRangeYFrom = skill.dmY + 40;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_METEOR_LV1)) {
					// Lv3ファイヤー1の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX - 480 * m.vector;
						skill.dmY = nearMonster.dmY - 200;
					} else if (skill.flameCount <= 19) {
						skill.dmX += 20 * m.vector;
						skill.dmY += 10;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 60
							: skill.dmX - skill.attackRengeX - 60;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 60
							+ skill.attackRengeX : skill.dmX - 60;
					attackRangeYFrom = skill.dmY + 60;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 60;
				} else if (skill.kind.equals(Const.SKILL_METEOR_LV2)) {
					// Lv3ファイヤー2の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX - 480 * m.vector;
						skill.dmY = nearMonster.dmY - 200;
					} else if (skill.flameCount <= 19) {
						skill.dmX += 20 * m.vector;
						skill.dmY += 10;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 60
							: skill.dmX - skill.attackRengeX - 60;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 60
							+ skill.attackRengeX : skill.dmX - 60;
					attackRangeYFrom = skill.dmY + 60;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 60;
				} else if (skill.kind.equals(Const.SKILL_METEOR_LV3)) {
					// Lv3ファイヤー3の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX - 480 * m.vector;
						skill.dmY = nearMonster.dmY - 200;
					} else if (skill.flameCount <= 19) {
						skill.dmX += 20 * m.vector;
						skill.dmY += 10;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 60
							: skill.dmX - skill.attackRengeX - 60;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 60
							+ skill.attackRengeX : skill.dmX - 60;
					attackRangeYFrom = skill.dmY + 60;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 60;
				}
			}
			// 氷
			else if (skill.kind.contains(Const.ice)) {
				if (skill.kind.equals(Const.SKILL_ICE_LV1)) {
					// Lv1アイス1の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 30
							: skill.dmX - skill.attackRengeX - 30;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 30
							+ skill.attackRengeX : skill.dmX - 30;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_ICE_LV2)) {
					// Lv1アイス2の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 50
							+ skill.attackRengeX : skill.dmX - 50;
					attackRangeYFrom = skill.dmY + 30;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 30;
				} else if (skill.kind.equals(Const.SKILL_ICE_LV3)) {
					// Lv1アイス3の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 80
							: skill.dmX - skill.attackRengeX - 80;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 80
							+ skill.attackRengeX : skill.dmX - 80;
					attackRangeYFrom = skill.dmY + 30;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 30;
				} else if (skill.kind.equals(Const.SKILL_FREEZE_LV1)) {
					// Lv2アイス1の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 30
							: skill.dmX - skill.attackRengeX - 30;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 30
							+ skill.attackRengeX : skill.dmX - 30;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_FREEZE_LV2)) {
					// Lv2アイス2の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 30
							: skill.dmX - skill.attackRengeX - 30;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 30
							+ skill.attackRengeX : skill.dmX - 30;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_FREEZE_LV3)) {
					// Lv2アイス3の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 50
							+ skill.attackRengeX : skill.dmX - 50;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 30;
				} else if (skill.kind.equals(Const.SKILL_GLACIAL_AGE_LV1)) {
					// Lv3アイス1の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 30
							: skill.dmX - skill.attackRengeX - 30;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 30
							+ skill.attackRengeX : skill.dmX - 30;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_GLACIAL_AGE_LV2)) {
					// Lv3アイス2の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 30
							: skill.dmX - skill.attackRengeX - 30;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 30
							+ skill.attackRengeX : skill.dmX - 30;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_GLACIAL_AGE_LV3)) {
					// Lv3アイス3の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 30
							: skill.dmX - skill.attackRengeX - 30;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 30
							+ skill.attackRengeX : skill.dmX - 30;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				}
			}
			// 雷
			else if (skill.kind.contains(Const.thunder)) {
				if (skill.kind.equals(Const.SKILL_THUNDER_LV1)) {
					// Lv1サンダー1の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 50
							+ skill.attackRengeX : skill.dmX - 50;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_THUNDER_LV2)) {
					// Lv1サンダー2の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 50
							+ skill.attackRengeX : skill.dmX - 50;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_THUNDER_LV3)) {
					// Lv1サンダー3の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 50
							+ skill.attackRengeX : skill.dmX - 50;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_LIGHTNING_LV1)) {
					// Lv2サンダー1の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 50
							+ skill.attackRengeX : skill.dmX - 50;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_LIGHTNING_LV2)) {
					// Lv2サンダー2の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 50
							+ skill.attackRengeX : skill.dmX - 50;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_LIGHTNING_LV3)) {
					// Lv2サンダー3の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 50
							+ skill.attackRengeX : skill.dmX - 50;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_THUNDERSTORM_LV1)) {
					// Lv3サンダー1の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 50
							+ skill.attackRengeX : skill.dmX - 50;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_THUNDERSTORM_LV2)) {
					// Lv3サンダー2の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 50
							+ skill.attackRengeX : skill.dmX - 50;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_THUNDERSTORM_LV3)) {
					// Lv3サンダー3の場合
					if (skill.flameCount == 0) {
						skill.dmX = nearMonster.dmX
								+ ((int) skill.positionX * m.vector);
						skill.dmY = nearMonster.dmY + (int) skill.positionY;
					}
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX + 50
							+ skill.attackRengeX : skill.dmX - 50;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				}
			}
			// 地
			else if (skill.kind.contains(Const.earth)) {
				if (skill.kind.equals(Const.SKILL_ROCK_LV1)) {
					// Lv1アース1の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 30
							: skill.dmX - skill.attackRengeX - 30;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 30 : skill.dmX - 30;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_ROCK_LV2)) {
					// Lv1アース2の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 50 : skill.dmX - 50;
					attackRangeYFrom = skill.dmY + 30;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 30;
				} else if (skill.kind.equals(Const.SKILL_ROCK_LV3)) {
					// Lv1アース3の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 70
							: skill.dmX - skill.attackRengeX - 70;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 70 : skill.dmX - 70;
					attackRangeYFrom = skill.dmY + 50;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 50;
				} else if (skill.kind.equals(Const.SKILL_RUGGED_MOUNTAIN_LV1)) {
					// Lv2アース1の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 50 : skill.dmX - 50;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_RUGGED_MOUNTAIN_LV2)) {
					// Lv2アース2の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 50 : skill.dmX - 50;
					attackRangeYFrom = skill.dmY + 20;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 20;
				} else if (skill.kind.equals(Const.SKILL_RUGGED_MOUNTAIN_LV3)) {
					// Lv2アース3の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 50 : skill.dmX - 50;
					attackRangeYFrom = skill.dmY + 20;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 20;
				} else if (skill.kind.equals(Const.SKILL_EARTHQUAKE_LV1)) {
					// Lv3アース1の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 50 : skill.dmX - 50;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_EARTHQUAKE_LV2)) {
					// Lv3アース2の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 50 : skill.dmX - 50;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				} else if (skill.kind.equals(Const.SKILL_EARTHQUAKE_LV3)) {
					// Lv3アース3の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 50 : skill.dmX - 50;
					attackRangeYFrom = skill.dmY;
					attackRangeYTo = skill.dmY + skill.attackRengeY;
				}

			}
			// 風
			else if (skill.kind.contains(Const.wind)) {
				if (skill.kind.equals(Const.SKILL_WIND_LV1)) {
					// Lv1ウィンド1の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 50
							: skill.dmX - skill.attackRengeX - 50;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 50 : skill.dmX - 50;
					attackRangeYFrom = skill.dmY + 60;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 60;
				} else if (skill.kind.equals(Const.SKILL_WIND_LV2)) {
					// Lv1ウィンド2の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 70
							: skill.dmX - skill.attackRengeX - 70;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 70 : skill.dmX - 70;
					attackRangeYFrom = skill.dmY + 100;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 100;
				} else if (skill.kind.equals(Const.SKILL_WIND_LV3)) {
					// Lv1ウィンド3の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 110
							: skill.dmX - skill.attackRengeX - 110;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 110 : skill.dmX - 110;
					attackRangeYFrom = skill.dmY + 150;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 150;
				} else if (skill.kind.equals(Const.SKILL_GUST_LV1)) {
					// Lv2ウィンド1の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 15
							: skill.dmX - skill.attackRengeX - 15;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 15 : skill.dmX - 15;
					attackRangeYFrom = skill.dmY + 80;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 80;
				} else if (skill.kind.equals(Const.SKILL_GUST_LV2)) {
					// Lv2ウィンド2の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;
					attackRangeXFrom = m.vector == 1 ? skill.dmX + 15
							: skill.dmX - skill.attackRengeX - 15;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 15 : skill.dmX - 15;
					attackRangeYFrom = skill.dmY + 100;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 100;
				} else if (skill.kind.equals(Const.SKILL_GUST_LV3)) {
					// Lv2ウィンド3の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX + 35
							: skill.dmX - skill.attackRengeX - 35;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 35 : skill.dmX - 35;
					attackRangeYFrom = skill.dmY + 120;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 120;
				} else if (skill.kind.equals(Const.SKILL_TORNADO_LV1)) {
					// Lv3ウィンド1の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX : skill.dmX
							- skill.attackRengeX - 120;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 120 : skill.dmX;
					attackRangeYFrom = skill.dmY + 20;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 20;
				} else if (skill.kind.equals(Const.SKILL_TORNADO_LV2)) {
					// Lv3ウィンド2の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX : skill.dmX
							- skill.attackRengeX - 120;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 120 : skill.dmX;
					attackRangeYFrom = skill.dmY + 20;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 20;
				} else if (skill.kind.equals(Const.SKILL_TORNADO_LV3)) {
					// Lv3ウィンド3の場合
					skill.dmX = srcX + (int) skill.positionX * m.vector;
					skill.dmY = srcY + (int) skill.positionY;

					attackRangeXFrom = m.vector == 1 ? skill.dmX : skill.dmX
							- skill.attackRengeX - 120;
					attackRangeXTo = m.vector == 1 ? skill.dmX
							+ skill.attackRengeX + 120 : skill.dmX;
					attackRangeYFrom = skill.dmY + 20;
					attackRangeYTo = skill.dmY + skill.attackRengeY + 20;
				}
			}

			synchronized (mSurfaceHolder) {
				skill.attackRangeXFrom = attackRangeXFrom;
				skill.attackRangeXTo = attackRangeXTo;
				skill.attackRangeYFrom = attackRangeYFrom;
				skill.attackRangeYTo = attackRangeYTo;
			}

		}

		/**
		 * ダメージ計算し、HPを設定する。HPが0になったモンスターを削除する。 拡張Ｆor文だと非同期だとだめみたい
		 */
		private void setMonsterDamage() {
			synchronized (mSurfaceHolder) {
				for (Iterator<Monster> iterator = mMonsters.iterator(); iterator
						.hasNext();) {
					Monster m = iterator.next();

					for (Iterator<Integer> damage = m.damages.iterator(); damage
							.hasNext();) {
						int stackDamage = damage.next();
						m.hp -= stackDamage;
						if (m.hp <= 0) {
							for (Iterator<Monster> i = mMonsters.iterator(); i
									.hasNext();) {
								Monster attacker = i.next();
								if (attacker.target == m) {
									attacker.target = null;
								}
							}
							iterator.remove();
							break;
						}
						damage.remove();
					}
				}
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
				interrupt();
				if (mTimerTask != null)
					mTimerTask.cancel();
			}

		}

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

					// don't forget to resize the background image
					// mBackgroundImageFar = Bitmap.createScaledBitmap(
					// mBackgroundImageFar, mCanvasWidth * 2,
					// mCanvasHeight, true);
					//
					// mBackgroundImageNear = BitmapFactory.decodeResource(res,
					// R.drawable.background_1);

					// don't forget to resize the background image
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
				}
			}
		}

		public boolean getFullMonsterFlg() {
			synchronized (mSurfaceHolder) {
				return fullMonsterFlg;
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
			}
		}

		/**
		 * Pauses the physics update & animation.
		 */
		public void pause() {
			synchronized (mSurfaceHolder) {
				if (mState == STATE_RUNNING)
					setGameState(STATE_PAUSE);
				if (mTimerTask != null) {
					mTimerTask.cancel();
				}
			}
		}

		/**
		 * タイマーを仕掛ける
		 */
		private void doTaskManager() {
			// Log.d(TAG,"Time left is " + mTimerLimit);

			mTimerLimit = mTimerLimit - 1;
			try {
				// 分オーダー
				int minute = mTimerLimit / 60;
				int second = mTimerLimit % 60;

				if (second > 9) {
					mTimerValue = minute + ":" + second;
				} else {
					mTimerValue = minute + ":0" + second;
				}
			} catch (Exception e1) {
				Log.e(TAG, "doCountDown threw " + e1.toString());
			}

			Message msg = mHandler.obtainMessage();

			Bundle b = new Bundle();
			b.putString("time", mTimerValue);
			// b.putInt("money", getMoney);
			// time's up
			if (mTimerLimit <= 0 || mLooseFlg) {
				b.putString("STATE_LOSE", String.valueOf(STATE_LOSE));
				mTimerTask = null;
				mState = STATE_LOSE;
			} else if (mWinFlg) {
				b.putString("STATE_WIN", String.valueOf(STATE_WIN));
				mTimerTask = null;
				mState = STATE_WIN;
			} else {
				if (mNegoresultShow) {
					// 交渉結果表示
					if (mNegoTime == 0) {
						// タイマーをセット
						mNegoTime = System.currentTimeMillis();
					}
					if (System.currentTimeMillis() - mNegoTime > 2000) {
						// 2秒すぎたら交渉結果表テキストを非表示へ
						mNegoresultShow = false;
						mNegoResltMessage = "";
						mNegoTime = 0;
					}
					b.putString("NegoResult", mNegoResltMessage);
				}
				mTimerTask = new TimerTask() {
					@Override
					public void run() {
						doTaskManager();
					}
				};
				mTimer.schedule(mTimerTask, mTaskIntervalInMillis);
			}
			msg.setData(b);
			mHandler.sendMessage(msg);
		}
	}

	// end thread class

	public static final String TAG = "JetBoy";

	/** The thread that actually draws the animation */
	private MainThread thread;
	private TextView mTimerView;
	// private TextView mPoketMoneyView;
	private TextView mNegoResultViwe;
	private TextView mTextGetMoneyView;
	private ImageView mImageGatMonster;
	private TableLayout mResultLayout;
	private MetaInfo mMetaInfo;
	private int Stage = 1;

	/**
	 * コンストラクタ
	 *
	 * @param context
	 * @param attrs
	 */
	public MainView(Context context, AttributeSet attrs) {
		super(context, attrs);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		if (isInEditMode() == false) {
			thread = new MainThread(holder, context.getApplicationContext(),
					new Handler() {
						@Override
						public void handleMessage(Message m) {
							mTimerView.setText(m.getData().getString("time"));

							if (m.getData().getString("STATE_LOSE") != null) {
								mResultLayout.setVisibility(View.VISIBLE);
								mTextGetMoneyView.setVisibility(View.VISIBLE);
								mTextGetMoneyView.setText(R.string.resultLoose);

								mImageGatMonster.setImageBitmap(null);
							} else if (m.getData().getString("STATE_WIN") != null) {
								mResultLayout.setVisibility(View.VISIBLE);
								mTextGetMoneyView.setVisibility(View.VISIBLE);
								mTextGetMoneyView.setText(R.string.resultWin);
								if (thread.mNegotiationTarget != null) {
									mImageGatMonster
											.setImageBitmap(thread.mNegotiationTarget
													.getBitmap());
								} else {
									mImageGatMonster.setImageBitmap(null);
								}
								if (mMetaInfo.stage < Stage) {
									mMetaInfo.stage = Stage;
									GameUtil.saveMetaInfo(mMetaInfo);
								}

							} else if (m.getData().getString("NegoResult") != null) {
								String message = m.getData().getString(
										"NegoResult");
								if (message.equals("")) {
									mNegoResultViwe
											.setVisibility(View.INVISIBLE);
									mNegoResultViwe.setText(m.getData()
											.getString(""));
								} else {
									String txt = m.getData().getString(
											"NegoResult");
									mNegoResultViwe.setText(txt);
									if (txt.equals(getResources().getString(
											R.string.negosiationSuccess))) {
										mNegoResultViwe
												.setTextColor(Color.BLUE);
									} else {
										mNegoResultViwe.setTextColor(Color.RED);
									}
									mNegoResultViwe.setVisibility(View.VISIBLE);
								}
							}
						}// end handle msg
					});
		}
		setFocusable(true); // make sure we get key events
		Log.d(TAG, "@@@ done creating view!");
	}

	/**
	 * Pass in a reference to the timer view widget so we can update it from
	 * here.
	 *
	 * @param tv
	 */
	public void setTimerView(TextView tv) {
		mTimerView = tv;
	}

	public void setNegoResultView(TextView tv) {
		mNegoResultViwe = tv;
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
	public MainThread getThread() {
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
		// SystemUtil.writeRuntimeMemory(this.getClass().getName()
		// + "surfaceCreated");
		if (thread.getState() == Thread.State.TERMINATED) {
			thread = new MainThread(getHolder(), getContext()
					.getApplicationContext(), new Handler() {
				@Override
				public void handleMessage(Message m) {
					mTimerView.setText(m.getData().getString("time"));

					if (m.getData().getString("STATE_LOSE") != null) {
						mResultLayout.setVisibility(View.VISIBLE);
						mTextGetMoneyView.setVisibility(View.VISIBLE);
						mTextGetMoneyView.setText(R.string.resultLoose);

						mImageGatMonster.setImageBitmap(null);
					} else if (m.getData().getString("STATE_WIN") != null) {
						mResultLayout.setVisibility(View.VISIBLE);
						mTextGetMoneyView.setVisibility(View.VISIBLE);
						mTextGetMoneyView.setText(R.string.resultWin);
						if (thread.mNegotiationTarget != null) {
							mImageGatMonster
									.setImageBitmap(thread.mNegotiationTarget
											.getBitmap());
						} else {
							mImageGatMonster.setImageBitmap(null);
						}
						if (mMetaInfo.stage < Stage) {
							mMetaInfo.stage = Stage;
							GameUtil.saveMetaInfo(mMetaInfo);
						}

					} else if (m.getData().getString("NegoResult") != null) {
						String message = m.getData().getString("NegoResult");
						if (message.equals("")) {
							mNegoResultViwe.setVisibility(View.INVISIBLE);
							mNegoResultViwe.setText(m.getData().getString(""));
						} else {
							String txt = m.getData().getString("NegoResult");
							mNegoResultViwe.setText(txt);
							if (txt.equals(getResources().getString(
									R.string.negosiationSuccess))) {
								mNegoResultViwe.setTextColor(Color.BLUE);
							} else {
								mNegoResultViwe.setTextColor(Color.RED);
							}
							mNegoResultViwe.setVisibility(View.VISIBLE);
						}
					}
				}// end handle msg
			});
			// thread.setGameState(thread.STATE_RUNNING);
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
		// GameUtil.terminateContext;
	}

	public void setResultViews(TableLayout mLayoutResult,
			TextView mTextGetMoney, ImageView mImageGetMonster) {
		this.mTextGetMoneyView = mTextGetMoney;
		this.mImageGatMonster = mImageGetMonster;
		this.mResultLayout = mLayoutResult;
	}

	public void setStage(int stage) {
		this.Stage = stage;
	}

	public void setMetaInfo(MetaInfo inf) {
		this.mMetaInfo = inf;
	}
}
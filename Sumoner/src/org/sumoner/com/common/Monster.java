package org.sumoner.com.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.graphics.Bitmap;

public class Monster {
	/**
	 * 面倒なのでスタティックにアクセスさせる 気が向いたらカプセル化.
	 * データの状態管理が面倒いので、今後リファクタリング対象
	 */
	// 基本情報
	public String name = "";// 表示名
	public String Lv = ""; //レベル
	public String No = "";
	public int ArrayResId;// Array.xmlのId
	private int resId;// モンスター画像id
	public int attack;// 攻撃力
	public int fullHp;// 最大HP
	public int hp; // 残HP

	public int speed;
	public boolean playerFlg = false;// プレイヤーフラグ
	public boolean enemyFlg;// 敵味方フラグ
	public boolean battleFlg;// 戦闘要因フラグ
	public int battlePlace;// 戦闘時の配置
	public Skill baseSkill;// モンスターのスキル
	public Skill addSkill_1;
	public Skill addSkill_2;
	public Ability ability;// モンスターのアビリティの集計（メモリ・処理節約のためスキルの様に１アビリティ、１オブジェクトにしない）
	public List<String> holdBaseAbilities = new ArrayList<String>();// 保持基本アビリティ　キーはConst定義　バリューは名称
	public List<String> holdAddAbilities = new ArrayList<String>();// 保持追加アビリティ


	// 狩場で使用するパラメータ
	public int dmX;// 表示位置X軸
	public int dmY;// 表示位置Y軸
	public int vector = 1;// 進行方向（1 or -1）気が向いたら、Enum化する
	public double deg; // 画像に動きを持たせるための、調整パラメータ
	public Vector<Integer> damages = new Vector<Integer>();// 被ダメージ値
	public boolean negotiationFlg = false; // 交渉中フラグ
	public float negoRegist;// 交渉中の抵抗値
	public float negoAttack;// 交渉追加パラメータ
	public float tmX;//タッチされている座標
	public float tmY;//タッチされている座標
	public Monster target;

	// 牧場で使用するパラメータ
	public int sector;// 牧場のセクター
	public int actionParam; // 行動のランダム係数
	public int actionTimeCount; // 行動時間のランダム係数（本当の時間でやってはないない）
	public boolean druggingFlg = false; // ドラッグ中フラグ

	// プレイヤー向けのデータ
	public int equipResId;// 装備品
	public int classResId;// プレーヤーのクラス

	public Monster(int resId, boolean enemyFlg) {
		if (enemyFlg) {
			// 敵の場合は進行方向を逆に
			vector = -1;
		}
		this.enemyFlg = enemyFlg;
		this.resId = resId;
//		if (resId == R.drawable.dragon) {
//			playerFlg = true;
//		}
	}

	/**
	 * ビットマップを取得する
	 *
	 * @return
	 */
	public Bitmap getBitmap() {
		return BitmapCache.getImage(resId);
	}

	public int getBitmpResId() {
		return resId;
	}
}

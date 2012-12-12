package org.sumoner.com.common;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

public class Skill {

	public String name = "";
	public String kind = "";

	public List<Integer> attackEffResId = new ArrayList<Integer>();
	public List<Boolean> hitFlameList = new ArrayList<Boolean>();

	public float scale;//攻撃画像のサイズ
	public int flameCount = 0;// 描画するフレーム枚数
	public float positionX = 25; // キャラクターとのX軸位置調整 やっぱりいろいろ調整のパラメータは持っておくと使える
	public float positionY = -55; // キャラクターとのY軸位置調整

	// 攻撃アクションフラグ
	public boolean attackFlg = false;//
	public boolean hitFlameFlg = true;; // ヒットするフレーム間のフラグ

	// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
	public int rengeX;// 攻撃範囲X軸
	public int rengeY;// 攻撃範囲Y軸

	// 攻撃の座標
	public int dmX;
	public int dmY;

	// 攻撃の座標を中心としてヒット判定がある範囲
	public int attackRengeX;
	public int attackRengeY;

	// 攻撃判定ありの座標
	public int attackRangeXFrom;
	public int attackRangeXTo;
	public int attackRangeYFrom;
	public int attackRangeYTo;

	// 基礎攻撃値
	public int baseDamage;
	// 基礎ノックバック値(最大１０、最低１)
	public int knockBack;
	// 基礎クールタイム
	public long cooltime;
	public long lastAttackTime;

	public int skillRank;// スキルのランク;
	public int skillKind;// スキルの種類;

	/**
	 * エフェクトの画像取得
	 *
	 * @return
	 */
	public Bitmap getEffectFlame() {
		// Bitmap effect = attackEffect[flameCount];
		Bitmap effect = BitmapCache.getImage(attackEffResId.get(flameCount));
		this.flameCount++;
		if (flameCount >= attackEffResId.size() - 1) {
			flameCount = 0;
			// 攻撃終了時 攻撃アクション終了
			attackFlg = false;
		}
		if (hitFlameList.get(flameCount)) {
			hitFlameFlg = true;
		} else {
			hitFlameFlg = false;
		}
		return effect;
	}

}

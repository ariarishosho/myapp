package org.sumoner.com.common;

public class Ability {

	public String name = "";
	public String kind = "";

	// ステータスアップ系
	public int upSpeed;// スピードアップ
	public int upAttack;// 攻撃力アップ
	public int upHp;// HPアップ

	// 攻撃付加効果
	public int absorbedHp;// HP吸収　未実装
	public int addKnockBack;// ノックバック攻撃
	public long upCoolDown;// 攻撃のクールタイム時間を下げる

	// 耐性系(0.0が最高、1.0が最低)
	public float registKnockBack = 1.0f;// ノックバック耐性
	public float registPhysical = 1.0f;// 物理耐性
	public float registFire = 1.0f;// 炎耐性
	public float registIce = 1.0f;// 氷耐性
	public float registEarth = 1.0f;// 地耐性
	public float registWind = 1.0f;// 風耐性
	public float registThunder = 1.0f;// 雷耐性

	// 特殊系
	public int plusMoney;// お金プラス　未実装
	public int plusNegotiation;// 交渉プラス　未実装
}

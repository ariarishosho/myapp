package org.sumoner.com.common;

public final class Const {
	// スキル種別
	public static String normal = "normal";
	public static String fire = "fire";
	public static String ice = "ice";
	public static String thunder = "thunder";
	public static String earth = "earth";
	public static String wind = "wind";
	// スキルランク
	public static String low = "_low";
	public static String middle = "_middle";
	public static String hight = "_hight";
	// スキルレベル
	public static String lv1 = "_lv1";
	public static String lv2 = "_lv2";
	public static String lv3 = "_lv3";

	// スキル系のシステム内部固定値　
	// リースファイルのIDを毎回読み込んで、比較させていると重すぎるため。
	// Skillクラスを継承させてinstance ofでもいいか？

	// ノーマル系
	public static String SKILL_SLASH_LV1 = normal + low + lv1;
	public static String SKILL_SLASH_LV2 = normal + low + lv2;
	public static String SKILL_SLASH_LV3 = normal + low + lv3;
	public static String SKILL_ENAGYBOLL_LV1 = normal + middle + lv1;
	public static String SKILL_ENAGYBOLL_LV2 = normal + middle + lv2;
	public static String SKILL_ENAGYBOLL_LV3 = normal + middle + lv3;
	public static String SKILL_ARROW_LV1 = normal + hight + lv1;
	public static String SKILL_ARROW_LV2 = normal + hight + lv2;
	public static String SKILL_ARROW_LV3 = normal + hight + lv3;

	// 炎系
	public static String SKILL_FIRE_LV1 = fire + low + lv1;
	public static String SKILL_FIRE_LV2 = fire + low + lv2;
	public static String SKILL_FIRE_LV3 = fire + low + lv3;
	public static String SKILL_FLAME_LV1 = fire + middle + lv1;
	public static String SKILL_FLAME_LV2 = fire + middle + lv2;
	public static String SKILL_FLAME_LV3 = fire + middle + lv3;
	public static String SKILL_METEOR_LV1 = fire + hight + lv1;
	public static String SKILL_METEOR_LV2 = fire + hight + lv2;
	public static String SKILL_METEOR_LV3 = fire + hight + lv3;

	// 氷系
	public static String SKILL_ICE_LV1 = ice + low + lv1;
	public static String SKILL_ICE_LV2 = ice + low + lv2;
	public static String SKILL_ICE_LV3 = ice + low + lv3;
	public static String SKILL_FREEZE_LV1 = ice + middle + lv1;
	public static String SKILL_FREEZE_LV2 = ice + middle + lv2;
	public static String SKILL_FREEZE_LV3 = ice + middle + lv3;
	public static String SKILL_GLACIAL_AGE_LV1 = ice + hight + lv1;
	public static String SKILL_GLACIAL_AGE_LV2 = ice + hight + lv2;
	public static String SKILL_GLACIAL_AGE_LV3 = ice + hight + lv3;

	// 雷系
	public static String SKILL_THUNDER_LV1 = thunder + low + lv1;
	public static String SKILL_THUNDER_LV2 = thunder + low + lv2;
	public static String SKILL_THUNDER_LV3 = thunder + low + lv3;
	public static String SKILL_LIGHTNING_LV1 = thunder + middle + lv1;
	public static String SKILL_LIGHTNING_LV2 = thunder + middle + lv2;
	public static String SKILL_LIGHTNING_LV3 = thunder + middle + lv3;
	public static String SKILL_THUNDERSTORM_LV1 = thunder + hight + lv1;
	public static String SKILL_THUNDERSTORM_LV2 = thunder + hight + lv2;
	public static String SKILL_THUNDERSTORM_LV3 = thunder + hight + lv3;

	// 地系
	public static String SKILL_ROCK_LV1 = earth + low + lv1;
	public static String SKILL_ROCK_LV2 = earth + low + lv2;
	public static String SKILL_ROCK_LV3 = earth + low + lv3;
	public static String SKILL_RUGGED_MOUNTAIN_LV1 = earth + middle + lv1;
	public static String SKILL_RUGGED_MOUNTAIN_LV2 = earth + middle + lv2;
	public static String SKILL_RUGGED_MOUNTAIN_LV3 = earth + middle + lv3;
	public static String SKILL_EARTHQUAKE_LV1 = earth + hight + lv1;
	public static String SKILL_EARTHQUAKE_LV2 = earth + hight + lv2;
	public static String SKILL_EARTHQUAKE_LV3 = earth + hight + lv3;

	// 風系
	public static String SKILL_WIND_LV1 = wind + low + lv1;
	public static String SKILL_WIND_LV2 = wind + low + lv2;
	public static String SKILL_WIND_LV3 = wind + low + lv3;
	public static String SKILL_GUST_LV1 = wind + middle + lv1;
	public static String SKILL_GUST_LV2 = wind + middle + lv2;
	public static String SKILL_GUST_LV3 = wind + middle + lv3;
	public static String SKILL_TORNADO_LV1 = wind + hight + lv1;
	public static String SKILL_TORNADO_LV2 = wind + hight + lv2;
	public static String SKILL_TORNADO_LV3 = wind + hight + lv3;

	// アビリティのシステム内部固定値
	public static String ABILITY_UPSPEED = "upspeed";
	public static String ABILITY_UPATTACK = "upAttack";
	public static String ABILITY_UPHP = "upHp";
	public static String ABILITY_ADDNUMBERATTACK = "addNumberAttack";
	public static String ABILITY_ADDNUMBERKIND = "addNumberKind";
	public static String ABILITY_ABSORBEDHP = "absorbedHp";
	public static String ABILITY_ADDKNOCKBACK = "addKnockBack";
	public static String ABILITY_REGISTKNOCKBACK = "registKnockBack";
	public static String ABILITY_REGISTPHYSICAL = "registPhysical";
	public static String ABILITY_REGISTFIRE = "registFire";
	public static String ABILITY_PLUSMONEY = "plusMoney";
	public static String ABILITY_PLUSNEGOTIATION = "plusNegotiation";

	// システム定数
	public static int SYSTEM_MAX_BATTLEMONSTER = 3;
	public static int SYSTEM_MAX_HOLDMONSTER = 12;
	public static int SYSTEM_MAX_STAGE = 89;
}

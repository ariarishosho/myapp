package org.sumoner.com.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.sumoner.com.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.content.res.TypedArray;

public class GameUtil {
	public static final String PREFS_NAME = "org.sumoner.com";

	// リソースファイル
	private static Resources res;
	private static Context context;
	private static SharedPreferences pref;

	private static final String PREFIX_PLAYER = "player_";
	private static final String PLAYER_CLASS = "player_class";
	private static final String PLAYER_EQUIP = "player_equip";

	private static final String ARRAY_RES_ID = "ArrayResId";
	private static final String ADDITIONAL_SKILL_1 = "AdditionalSkill_1";
	private static final String ADDITIONAL_SKILL_2 = "AdditionalSkill_2";
	private static final String ADDITIONAL_ABILITY_1 = "AdditionalAbblity_1";
	private static final String ADDITIONAL_ABILITY_2 = "AdditionalAbblity_2";
	private static final String ADDITIONAL_ABILITY_3 = "AdditionalAbblity_3";
	private static final String SECTOR_NO = "sectorNo";
	private static final String BATTLE_FLG = "battle";
	private static final String BATTLE_PlACE = "place";

	private static final String PREFIX_ITEM = "item_";

	private static final String PREFIX_META = "meta_";
	private static final String MONEY = "money";
	private static final String STAGE = "SCENE";
	private static final String TAKEN_MONSTER = "takenmonster";
	private static final String INIT_GAME = "Welcome";

	/**
	 * コンテキストを設定する
	 *
	 * @param res
	 */
	public static void setContext(Context context) {
		GameUtil.context = context;
		GameUtil.res = context.getResources();
	}

	public static void terminateContext() {
		GameUtil.context = null;
		GameUtil.res = null;
	}

	/**
	 * モンスターの基本情報を取得する
	 *
	 * @param ArrayResId
	 * @param enemyFlg
	 * @return
	 */
	public static Monster getBaseMonsterInfo(int ArrayResId) {
		Monster m = null;
		// リソース参照用
		TypedArray ta = res.obtainTypedArray(ArrayResId);
		// 文字列参照用
		String[] params = res.getStringArray(ArrayResId);

		if (params != null) {
			m = new Monster(ta.getResourceId(1, 0), false);
			m.ArrayResId = ArrayResId;
			m.No = ta.getString(0);
			m.name = ta.getString(2);
			m.fullHp = ta.getInt(3, 100);
			m.attack = ta.getInt(4, 10);
			BitmapCache.setImage(ta.getResourceId(1, 0), ta.getFloat(5, 0.5f));
			m.baseSkill = setSkillParam(ta.getString(6));
			String[] abilitesName = { ta.getString(9), ta.getString(10),
					ta.getString(11), ta.getString(12), ta.getString(13),
					ta.getString(14) };
			m.ability = setAblitiesParam(abilitesName, m.holdBaseAbilities,
					null);
			m.negoRegist = ta.getFloat(15, 0);
			m.negoAttack = ta.getFloat(16, 0);
			m.Lv = ta.getString(17);
			m.speed = 2;

			// ステータスの更新
			m.speed += m.ability.upSpeed;
			m.attack += m.ability.upAttack;
			m.fullHp += m.ability.upHp;
			m.hp = m.fullHp;
		}
		return m;
	}

	/**
	 * モンスターの基本情報を取得する。キャッシュする画像サイズを指定できる
	 *
	 * @param ArrayResId
	 * @param enemyFlg
	 * @return
	 */
	public static Monster getBaseMonsterInfo(int ArrayResId, float scale) {
		Monster m = null;
		// リソース参照用
		TypedArray ta = res.obtainTypedArray(ArrayResId);
		// 文字列参照用
		String[] params = res.getStringArray(ArrayResId);

		if (params != null) {
			m = new Monster(ta.getResourceId(1, 0), false);
			m.ArrayResId = ArrayResId;
			m.No = ta.getString(0);
			m.name = ta.getString(2);
			m.fullHp = ta.getInt(3, 100);
			m.attack = ta.getInt(4, 10);
			BitmapCache.setImage(ta.getResourceId(1, 0), scale);
			m.baseSkill = setSkillParam(ta.getString(6));

			String[] abilitesName = { ta.getString(9), ta.getString(10),
					ta.getString(11), ta.getString(12), ta.getString(13),
					ta.getString(14) };
			m.ability = setAblitiesParam(abilitesName, m.holdBaseAbilities,
					null);
			m.speed = 2;
			// ステータスの更新
			m.speed += m.ability.upSpeed;
			m.attack += m.ability.upAttack;
			m.fullHp += m.ability.upHp;
			m.hp = m.fullHp;
		}
		return m;
	}

	/**
	 * 敵モンスター情報を取得します
	 *
	 * @param scene
	 *            　面
	 * @return
	 */
	public static Vector<Monster> getEnemyMonsterInfo(Integer scene) {

		Vector<Monster> monsters = new Vector<Monster>();
		int resId = res.getIdentifier("map" + String.valueOf(scene), "array",
				context.getPackageName());

		if (resId == 0)
			return null;
		TypedArray ta = res.obtainTypedArray(resId);
		for (int i = 0; i < ta.length(); i++) {
			Monster m = getBaseMonsterInfo(ta.getResourceId(i, 0));
			m.enemyFlg = true;
			monsters.addElement(m);
		}
		return monsters;
	}

	public static void gameInit() {
		pref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE
				| Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);

		boolean initFlg = pref.getBoolean(INIT_GAME, true);
		if (initFlg) {
			// 初回起動
			Editor prefsPrivateEditor = pref.edit();

			// 手持ちモンスターを設定
			Vector<Monster> monsters = new Vector<Monster>();
			Monster m = getBaseMonsterInfo(R.array.No001);
			m.battleFlg = true;
			m.enemyFlg = false;
			m.playerFlg = false;
			monsters.addElement(m);
			saveMonster(monsters);

			// メタ情報を設定
			MetaInfo metaindfo = new MetaInfo();
			metaindfo.appointments1Rank = 1;
			metaindfo.appointments2Rank = 1;
			metaindfo.appointments3Rank = 1;
			metaindfo.money = 0;
			MetaInfo.MiniMonster minMonster = metaindfo.new MiniMonster();
			minMonster.ArrayId = R.array.No001;
			metaindfo.takenMonster = new ArrayList<MetaInfo.MiniMonster>();
			metaindfo.takenMonster.add(minMonster);

			saveMetaInfo(metaindfo);

			prefsPrivateEditor.putBoolean(INIT_GAME, false);
			prefsPrivateEditor.commit();
		}
	}

	/**
	 * SharedPreferencesへデータを保存します
	 */
	public static void saveMonster(List<Monster> monsters) {
		pref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		Editor prefsPrivateEditor = pref.edit();
		if (monsters == null || monsters.isEmpty())
			return;

		int addNo = 1;// 格納情報へ付加するナンバリング
		for (Iterator<Monster> iterator = monsters.iterator(); iterator
				.hasNext();) {
			String data = null;
			Monster m = iterator.next();
			if (!m.playerFlg && !m.enemyFlg) {
				// 味方モンスター情報
				prefsPrivateEditor.putInt(String.valueOf(addNo) + ARRAY_RES_ID,
						m.ArrayResId);
				prefsPrivateEditor.putString(String.valueOf(addNo)
						+ ADDITIONAL_SKILL_1, m.addSkill_1 == null ? null
						: m.addSkill_1.name);
				prefsPrivateEditor.putString(String.valueOf(addNo)
						+ ADDITIONAL_SKILL_2, m.addSkill_2 == null ? null
						: m.addSkill_2.name);
				if (m.holdAddAbilities.size() > 0) {
					data = m.holdAddAbilities.get(0);
				} else {
					data = null;
				}
				prefsPrivateEditor.putString(String.valueOf(addNo)
						+ ADDITIONAL_ABILITY_1, data);
				if (m.holdAddAbilities.size() > 1) {
					data = m.holdAddAbilities.get(1);
				} else {
					data = null;
				}
				prefsPrivateEditor.putString(String.valueOf(addNo)
						+ ADDITIONAL_ABILITY_2, data);
				if (m.holdAddAbilities.size() > 2) {
					data = m.holdAddAbilities.get(2);
				} else {
					data = null;
				}
				prefsPrivateEditor.putString(String.valueOf(addNo)
						+ ADDITIONAL_ABILITY_3, data);
				prefsPrivateEditor.putInt(String.valueOf(addNo) + SECTOR_NO,
						m.sector);

				prefsPrivateEditor.putBoolean(String.valueOf(addNo)
						+ BATTLE_FLG, m.battleFlg);
				prefsPrivateEditor.putInt(String.valueOf(addNo) + BATTLE_PlACE,
						m.battlePlace);
				addNo++;
			} else {
				// 敵モンスターやプレイヤーは削除
				iterator.remove();
			}
		}
		// 登録抹消されたモンスターを削除する処理(この時点では味方モンスターのみの配列)
		for (int i = addNo; i <= Const.SYSTEM_MAX_HOLDMONSTER; i++) {
			prefsPrivateEditor.remove(String.valueOf(i) + ARRAY_RES_ID);
			prefsPrivateEditor.remove(String.valueOf(i) + ADDITIONAL_SKILL_1);
			prefsPrivateEditor.remove(String.valueOf(i) + ADDITIONAL_SKILL_2);
			prefsPrivateEditor.remove(String.valueOf(i) + ADDITIONAL_ABILITY_1);
			prefsPrivateEditor.remove(String.valueOf(i) + ADDITIONAL_ABILITY_2);
			prefsPrivateEditor.remove(String.valueOf(i) + ADDITIONAL_ABILITY_3);
			prefsPrivateEditor.remove(String.valueOf(i) + SECTOR_NO);
			prefsPrivateEditor.remove(String.valueOf(i) + BATTLE_FLG);
			prefsPrivateEditor.remove(String.valueOf(i) + BATTLE_PlACE);
		}

		prefsPrivateEditor.commit();
	}

	/**
	 * SharedPreferencesからデータをロードします
	 */
	public static Vector<Monster> loadMonster() {
		Vector<Monster> monsters = new Vector<Monster>();
		pref = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		for (int addNo = 1; addNo <= Const.SYSTEM_MAX_HOLDMONSTER; addNo++) {

			int ArrayResId = pref.getInt(String.valueOf(addNo) + ARRAY_RES_ID,
					0);
			if (ArrayResId == 0) {
				continue;
			}
			Monster m = getBaseMonsterInfo(ArrayResId);
			m.addSkill_1 = setSkillParam(pref.getString(String.valueOf(addNo)
					+ ADDITIONAL_SKILL_1, null));
			m.addSkill_1 = setSkillParam(pref.getString(String.valueOf(addNo)
					+ ADDITIONAL_SKILL_2, null));
			String[] data = new String[3];
			data[0] = pref.getString(String.valueOf(addNo)
					+ ADDITIONAL_ABILITY_1, null);
			data[1] = pref.getString(String.valueOf(addNo)
					+ ADDITIONAL_ABILITY_2, null);
			data[2] = pref.getString(String.valueOf(addNo)
					+ ADDITIONAL_ABILITY_3, null);
			m.ability = setAblitiesParam(data, m.holdAddAbilities, m.ability);
			m.sector = pref.getInt(String.valueOf(addNo) + SECTOR_NO, 0);
			m.battleFlg = pref.getBoolean(String.valueOf(addNo) + BATTLE_FLG,
					false);
			m.battlePlace = pref.getInt(String.valueOf(addNo) + BATTLE_PlACE,
					99);

			// 見方補正(敵より弱いモンスターしかいないため、若干の補正が必要+Lv2-3前後相当のパラメータ）
			m.attack += 2;
			m.fullHp += 10;
			// ステータスの更新（GetBaseMosterInfoでベースアビリティのHP・スピード・攻撃力）
			// がすでに加算されているため、継承分のみのパラメータを加算
			Ability a = setAblitiesParam(data, new ArrayList<String>(), null);
			m.speed += a.upSpeed;
			m.attack += a.upAttack;
			m.fullHp += a.upHp;

			m.hp = m.fullHp;
			monsters.addElement(m);
		}
		return monsters;
	}

	/**
	 * SharedPreferencesへデータを保存します
	 */
	public static void savePlayer(int equipId, int classId) {
		pref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		Editor prefsPrivateEditor = pref.edit();

		// プレーヤーのクラス設定
		prefsPrivateEditor.putInt(PREFIX_PLAYER + PLAYER_CLASS, classId);
		// プレイヤーの装備品設定
		prefsPrivateEditor.putInt(PREFIX_PLAYER + PLAYER_EQUIP, equipId);
		prefsPrivateEditor.commit();
	}

	/**
	 * SharedPreferencesからプレーヤーデータをロードします
	 */
	public static Monster loadPlayer() {
		pref = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		// プレーヤーの基本情報を取得
		int classId = pref.getInt(PREFIX_PLAYER + PLAYER_CLASS, R.array.No001);
		Monster player = getBaseMonsterInfo(classId);
		// 装備品のデータを取得
		int equipId = pref
				.getInt(PREFIX_PLAYER + PLAYER_EQUIP, R.array.equip_1);
		TypedArray equipTa = res.obtainTypedArray(equipId);

		player.equipResId = equipId;
		player.classResId = classId;
		player.addSkill_1 = setSkillParam(equipTa.getString(8));
		player.addSkill_2 = setSkillParam(equipTa.getString(9));
		String[] data = new String[3];
		data[0] = pref.getString(equipTa.getString(11), null);
		data[1] = pref.getString(equipTa.getString(12), null);
		data[2] = pref.getString(equipTa.getString(13), null);
		player.ability = setAblitiesParam(data, player.holdAddAbilities,
				player.ability);

		// ステータスの更新（GetBaseMosterInfoでベースアビリティのHP・スピード・攻撃力）
		// がすでに加算されているため、継承分のみのパラメータを加算
		Ability a = setAblitiesParam(data, new ArrayList<String>(), null);
		player.speed += a.upSpeed;
		player.attack += a.upAttack;
		player.hp += a.upHp;
		player.playerFlg = true;
		return player;
	}

	/**
	 * アップグレードデータリストを取得します
	 *
	 * @param currnetPage
	 * @return
	 */
	public static ArrayList<GridItem> getUpgradeList(int currnetPage) {
		ArrayList<GridItem> list = new ArrayList<GridItem>();
		int resId = 0;
		switch (currnetPage) {
		case 1:
			// 設備の場合
			resId = R.array.appointments;
			break;

		case 2:
			// 装備の場合
			resId = R.array.equip;
			break;

		case 3:
			// スペシャルの場合
			resId = R.array.special;
			break;

		default:
			break;
		}
		// 購入済みのアイテムリストを取得
		List<String> boughtItems = loadBoughtItem(currnetPage);

		// リソース参照用
		TypedArray ta = res.obtainTypedArray(resId);
		for (int i = 0; i < ta.length(); i++) {
			TypedArray itemTa = res.obtainTypedArray(ta.getResourceId(i, 0));
			GridItem girdItem = new GridItem(itemTa.getResourceId(2, 0),
					itemTa.getString(0));
			// 説明文
			girdItem.strData1 = itemTa.getString(1);
			girdItem.IntegerData = ta.getResourceId(i, 0);// arrayのID
			// 価格１
			girdItem.IntegerData1 = itemTa.getInt(3, 0);
			// 価格２
			girdItem.IntegerData1 = itemTa.getInt(4, 0);
			// 購入済み
			if (boughtItems.contains(girdItem.strData)) {
				girdItem.boolData = true;
			} else {
				girdItem.boolData = false;
			}

			list.add(girdItem);
		}
		return list;
	}

	/**
	 * 購入済みのアイテムをセーブする
	 */
	public static void saveBoughtItem(int resid) {
		String idName = res.getResourceName(resid);
		pref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		Editor prefsPrivateEditor = pref.edit();
		prefsPrivateEditor.putString(PREFIX_ITEM + idName, idName);
		prefsPrivateEditor.commit();
	}

	/**
	 * 購入済みのアイテム一覧をロードする この辺が多くなりすぎてきたら、全データ取得して、プレフィックスで拾ってくるようにする
	 *
	 * @return
	 */
	public static ArrayList<String> loadBoughtItem(int currnetPage) {
		ArrayList<String> list = new ArrayList<String>();
		pref = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);

		switch (currnetPage) {
		case 1:
			// 設備
			TypedArray ta = res.obtainTypedArray(R.array.appointments);
			for (int i = 0; i < ta.length(); i++) {
				list.add(pref.getString(
						PREFIX_ITEM
								+ res.getResourceName(ta.getResourceId(i, 0)),
						""));
			}
			//
			// list.add(pref.getString(
			// PREFIX_ITEM + res.getResourceName(R.array.appointments_1_1),
			// ""));
			// list.add(pref.getString(
			// PREFIX_ITEM + res.getResourceName(R.array.appointments_1_2),
			// ""));
			// list.add(pref.getString(
			// PREFIX_ITEM + res.getResourceName(R.array.appointments_2_1),
			// ""));
			// list.add(pref.getString(
			// PREFIX_ITEM + res.getResourceName(R.array.appointments_3_1),
			// ""));
			break;
		case 2:
			// 装備
			ta = res.obtainTypedArray(R.array.equip);
			for (int i = 0; i < ta.length(); i++) {

				list.add(pref.getString(
						PREFIX_ITEM
								+ res.getResourceName(ta.getResourceId(i, 0)),
						""));
			}
			// list.add(pref.getString(
			// PREFIX_ITEM + res.getResourceName(R.array.equip_1), ""));
			// list.add(pref.getString(
			// PREFIX_ITEM + res.getResourceName(R.array.equip_2), ""));
			// list.add(pref.getString(
			// PREFIX_ITEM + res.getResourceName(R.array.equip_3), ""));
			break;
		case 3:// スペシャル
			ta = res.obtainTypedArray(R.array.special);
			for (int i = 0; i < ta.length(); i++) {

				list.add(pref.getString(
						PREFIX_ITEM
								+ res.getResourceName(ta.getResourceId(i, 0)),
						""));
			}
			// list.add(pref.getString(
			// PREFIX_ITEM + res.getResourceName(R.array.special_1), ""));
			// list.add(pref.getString(
			// PREFIX_ITEM + res.getResourceName(R.array.special_2), ""));

			break;
		}

		// 取得できなかったデータを削除する
		for (Iterator<String> i = list.iterator(); i.hasNext();) {
			String itme = i.next();
			if (itme == "" || itme == null) {
				i.remove();
			}
		}

		return list;
	}

	/**
	 * システム情報をセーブします
	 */
	public static void saveMetaInfo(MetaInfo meta) {
		if (meta == null) {
			return;
		}
		pref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		Editor prefsPrivateEditor = pref.edit();
		// prefsPrivateEditor.putLong(PREFIX_META + MONEY, meta.money);
		prefsPrivateEditor.putInt(PREFIX_META + STAGE, meta.stage);

		prefsPrivateEditor.commit();
	}

	/**
	 * システム情報をロードします
	 *
	 * @return
	 */
	public static MetaInfo loadMetaInfo() {
		MetaInfo meta = new MetaInfo();

		pref = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		/*
		 * 所持金設定
		 */
		// meta.money = pref.getLong(PREFIX_META + MONEY, 0);

		/*
		 * クリアステージ設定
		 */
		meta.stage = pref.getInt(PREFIX_META + STAGE, 0);


		return meta;
	}

	/**
	 * アビリティを設定します
	 *
	 * @param abilitesName
	 *            Stringに設定してある名前
	 * @param holdAbilities
	 *            保持アビリティ情報を格納する
	 * @param abl
	 *            アビリティのパラメータを格納する。Null可、インスタンスを渡した場合は加算される
	 *
	 * @return
	 */
	private static Ability setAblitiesParam(String[] abilitesName,
			List<String> holdAbilities, Ability abl) {
		if (abl == null) {
			abl = new Ability();
		}
		for (String name : abilitesName) {
			if (name != null && !name.equals("")) {
				if (holdAbilities == null) {
					holdAbilities = new ArrayList<String>();
				}
				holdAbilities.add(name);
				if (name.equals(res.getString(R.string.ability01_name))) {
					// スピードアップLv1
					abl.upSpeed += 2;
					abl.upCoolDown += 50;

				} else if (name.equals(res.getString(R.string.ability02_name))) {
					// スピードアップLv2
					abl.upSpeed += 3;
					abl.upCoolDown += 75;
				} else if (name.equals(res.getString(R.string.ability03_name))) {
					// スピードアップLv3
					abl.upSpeed += 4;
					abl.upCoolDown += 100;
				} else if (name.equals(res.getString(R.string.ability04_name))) {
					// スピードアップLv4
					abl.upSpeed += 5;
					abl.upCoolDown += 125;
				} else if (name.equals(res.getString(R.string.ability05_name))) {
					// スピードアップLv5
					abl.upSpeed += 6;
					abl.upCoolDown += 150;
				} else if (name.equals(res.getString(R.string.ability06_name))) {
					// 攻撃力アップLv1
					abl.upAttack += 5;

				} else if (name.equals(res.getString(R.string.ability07_name))) {
					// 攻撃力アップLv2
					abl.upAttack += 15;

				} else if (name.equals(res.getString(R.string.ability08_name))) {
					// 攻撃力アップLv3
					abl.upAttack += 30;

				} else if (name.equals(res.getString(R.string.ability09_name))) {
					// 攻撃力アップLv4
					abl.upAttack += 60;

				} else if (name.equals(res.getString(R.string.ability10_name))) {
					// 攻撃力アップLv5
					abl.upAttack += 120;

				} else if (name.equals(res.getString(R.string.ability11_name))) {
					// HPアップLv1
					abl.upHp += 20;

				} else if (name.equals(res.getString(R.string.ability12_name))) {
					// HPアップLv2
					abl.upHp += 100;

				} else if (name.equals(res.getString(R.string.ability13_name))) {
					// HPアップLv3
					abl.upHp += 300;

				} else if (name.equals(res.getString(R.string.ability14_name))) {
					// HPアップLv4
					abl.upHp += 600;

				} else if (name.equals(res.getString(R.string.ability15_name))) {
					// HPアップLv5
					abl.upHp += 1000;

				} else if (name.equals(res.getString(R.string.ability16_name))) {
					// HP吸収 Lv1
					abl.absorbedHp += 5;
				} else if (name.equals(res.getString(R.string.ability17_name))) {
					// HP吸収 Lv2
					abl.absorbedHp += 5;
				} else if (name.equals(res.getString(R.string.ability18_name))) {
					// HP吸収 Lv3
					abl.absorbedHp += 5;
				} else if (name.equals(res.getString(R.string.ability19_name))) {
					// HP吸収 Lv4
					abl.absorbedHp += 5;
				} else if (name.equals(res.getString(R.string.ability20_name))) {
					// HP吸収 Lv5
					abl.absorbedHp += 5;
				} else if (name.equals(res.getString(R.string.ability21_name))) {
					// ノックバック攻撃Lv1
					abl.addKnockBack += 1;
				} else if (name.equals(res.getString(R.string.ability22_name))) {
					// ノックバック攻撃Lv2
					abl.addKnockBack += 2;
				} else if (name.equals(res.getString(R.string.ability23_name))) {
					// ノックバック攻撃Lv3
					abl.addKnockBack += 3;
				} else if (name.equals(res.getString(R.string.ability24_name))) {
					// ノックバック攻撃Lv4
					abl.addKnockBack += 4;
				} else if (name.equals(res.getString(R.string.ability25_name))) {
					// ノックバック攻撃Lv5
					abl.addKnockBack += 5;
				} else if (name.equals(res.getString(R.string.ability26_name))) {
					// ノックバック耐性Lv1
					abl.registKnockBack -= 0.1f;
				} else if (name.equals(res.getString(R.string.ability27_name))) {
					// ノックバック耐性Lv2
					abl.registKnockBack -= 0.3f;
				} else if (name.equals(res.getString(R.string.ability28_name))) {
					// ノックバック耐性Lv3
					abl.registKnockBack -= 0.5f;
				} else if (name.equals(res.getString(R.string.ability29_name))) {
					// ノックバック耐性Lv4
					abl.registKnockBack -= 0.8f;
				} else if (name.equals(res.getString(R.string.ability30_name))) {
					// ノックバック耐性Lv5
					abl.registKnockBack -= 1.0f;
				} else if (name.equals(res.getString(R.string.ability31_name))) {
					// ノーマル耐性Lv1
					abl.registPhysical -= 0.1f;
				} else if (name.equals(res.getString(R.string.ability32_name))) {
					// ノーマル耐性Lv2
					abl.registPhysical -= 0.3f;
				} else if (name.equals(res.getString(R.string.ability33_name))) {
					// ノーマル耐性Lv3
					abl.registPhysical -= 0.5f;
				} else if (name.equals(res.getString(R.string.ability34_name))) {
					// ノーマル耐性Lv4
					abl.registPhysical -= 0.8f;
				} else if (name.equals(res.getString(R.string.ability35_name))) {
					// ノーマル耐性Lv5
					abl.registPhysical -= 1.0f;
				} else if (name.equals(res.getString(R.string.ability36_name))) {
					// 炎耐性Lv1
					abl.registFire -= 0.1f;
				} else if (name.equals(res.getString(R.string.ability37_name))) {
					// 炎耐性Lv2
					abl.registFire -= 0.3f;
				} else if (name.equals(res.getString(R.string.ability38_name))) {
					// 炎耐性Lv3
					abl.registFire -= 0.5f;
				} else if (name.equals(res.getString(R.string.ability39_name))) {
					// 炎耐性Lv4
					abl.registFire -= 0.8f;
				} else if (name.equals(res.getString(R.string.ability40_name))) {
					// 炎耐性Lv5
					abl.registFire -= 1.0f;
				} else if (name.equals(res.getString(R.string.ability41_name))) {
					// 氷耐性Lv1
					abl.registIce -= 0.1f;
				} else if (name.equals(res.getString(R.string.ability42_name))) {
					// 氷耐性Lv2
					abl.registIce -= 0.3f;
				} else if (name.equals(res.getString(R.string.ability43_name))) {
					// 氷耐性Lv3
					abl.registIce -= 0.5f;
				} else if (name.equals(res.getString(R.string.ability44_name))) {
					// 氷耐性Lv4
					abl.registIce -= 0.8f;
				} else if (name.equals(res.getString(R.string.ability45_name))) {
					// 氷耐性Lv5
					abl.registIce -= 1.0f;
				} else if (name.equals(res.getString(R.string.ability46_name))) {
					// 雷耐性Lv1
					abl.registThunder -= 0.1f;
				} else if (name.equals(res.getString(R.string.ability47_name))) {
					// 雷耐性Lv2
					abl.registThunder -= 0.3f;
				} else if (name.equals(res.getString(R.string.ability48_name))) {
					// 雷耐性Lv3
					abl.registThunder -= 0.5f;
				} else if (name.equals(res.getString(R.string.ability49_name))) {
					// 雷耐性Lv4
					abl.registThunder -= 0.8f;
				} else if (name.equals(res.getString(R.string.ability50_name))) {
					// 雷耐性Lv5
					abl.registThunder -= 1.0f;
				} else if (name.equals(res.getString(R.string.ability51_name))) {
					// 地耐性Lv1
					abl.registEarth -= 0.1f;
				} else if (name.equals(res.getString(R.string.ability52_name))) {
					// 地耐性Lv2
					abl.registEarth -= 0.3f;
				} else if (name.equals(res.getString(R.string.ability53_name))) {
					// 地耐性Lv3
					abl.registEarth -= 0.5f;
				} else if (name.equals(res.getString(R.string.ability54_name))) {
					// 地耐性Lv4
					abl.registEarth -= 0.8f;
				} else if (name.equals(res.getString(R.string.ability55_name))) {
					// 地耐性Lv5
					abl.registEarth -= 1.0f;
				} else if (name.equals(res.getString(R.string.ability56_name))) {
					// 風耐性Lv1
					abl.registWind -= 0.1f;
				} else if (name.equals(res.getString(R.string.ability57_name))) {
					// 風耐性Lv2
					abl.registWind -= 0.3f;
				} else if (name.equals(res.getString(R.string.ability58_name))) {
					// 風耐性Lv3
					abl.registWind -= 0.5f;
				} else if (name.equals(res.getString(R.string.ability59_name))) {
					// 風耐性Lv4
					abl.registWind -= 0.8f;
				} else if (name.equals(res.getString(R.string.ability60_name))) {
					// 風耐性Lv5
					abl.registWind -= 1.0f;
				} else if (name.equals(res.getString(R.string.ability61_name))) {
					// 賞金アップLv1
					abl.plusMoney += 1.2f;
				} else if (name.equals(res.getString(R.string.ability62_name))) {
					// 賞金アップLv2
					abl.plusMoney += 1.5f;
				} else if (name.equals(res.getString(R.string.ability63_name))) {
					// 賞金アップLv3
					abl.plusMoney += 2.0f;
				} else if (name.equals(res.getString(R.string.ability64_name))) {
					// 契約アップLv1
					abl.plusNegotiation += 1.1f;
				} else if (name.equals(res.getString(R.string.ability65_name))) {
					// 契約アップLv2
					abl.plusNegotiation += 1.3f;
				} else if (name.equals(res.getString(R.string.ability66_name))) {
					// 契約アップLv3
					abl.plusNegotiation += 1.5f;
				}
			}
		}

		// 耐性系の最低値は0
		if (abl.registKnockBack < 0)
			abl.registKnockBack = 0;
		if (abl.registPhysical < 0)
			abl.registPhysical = 0;
		if (abl.registFire < 0)
			abl.registFire = 0;
		if (abl.registIce < 0)
			abl.registIce = 0;
		if (abl.registEarth < 0)
			abl.registEarth = 0;
		if (abl.registThunder < 0)
			abl.registThunder = 0;
		if (abl.registWind < 0)
			abl.registWind = 0;
		return abl;
	}

	/**
	 * スキルの情報を設定して返却します
	 *
	 * @param skillName
	 * @return
	 */
	private static Skill setSkillParam(String skillName) {
		if (skillName == "" || skillName == null)
			return null;

		Skill skill = new Skill();
		if (skillName.equals(res.getString(R.string.skill01_name))) {
			// Lv1ノーマル１
			skill.name = skillName;
			skill.kind = Const.SKILL_SLASH_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 25; // キャラクターとのX軸位置調整
									// やっぱりいろいろ調整のパラメータは持っておくと使える
			skill.positionY = -55; // キャラクターとのY軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 50;// 攻撃範囲X軸
			skill.rengeY = 60;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 50;
			skill.attackRengeY = 60;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 1000l;

			skill.attackEffResId.add(R.drawable.slash_1);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.slash_2);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.slash_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.slash_4);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.slash_1);
			BitmapCache.setImage(R.drawable.slash_2);
			BitmapCache.setImage(R.drawable.slash_3);
			BitmapCache.setImage(R.drawable.slash_4);
			skill.scale = 0.5f;
		} else if (skillName.equals(res.getString(R.string.skill02_name))) {
			// Lv1ノーマル2
			skill.name = skillName;
			skill.kind = Const.SKILL_SLASH_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定
			// パラメーター設定
			skill.positionX = 25; // キャラクターとのX軸位置調整
									// やっぱりいろいろ調整のパラメータは持っておくと使える
			skill.positionY = -65; // キャラクターとのY軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 70;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 70;
			skill.attackRengeY = 80;
			// 基礎攻撃値
			skill.baseDamage = 6;
			// 基礎ノックバック値
			skill.knockBack = 2;
			// 基礎クールタイム
			skill.cooltime = 950l;
			skill.attackEffResId.add(R.drawable.slash_1);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.slash_2);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.slash_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.slash_4);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.slash_1);
			BitmapCache.setImage(R.drawable.slash_2);
			BitmapCache.setImage(R.drawable.slash_3);
			BitmapCache.setImage(R.drawable.slash_4);
			skill.scale = 0.7f;
		} else if (skillName.equals(res.getString(R.string.skill03_name))) {
			// Lv1ノーマル3
			skill.name = skillName;
			skill.kind = Const.SKILL_SLASH_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定
			// パラメーター設定
			skill.positionX = 25; // キャラクターとのX軸位置調整
									// やっぱりいろいろ調整のパラメータは持っておくと使える
			skill.positionY = -75; // キャラクターとのY軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 90;// 攻撃範囲X軸
			skill.rengeY = 100;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 90;
			skill.attackRengeY = 100;
			// 基礎攻撃値
			skill.baseDamage = 12;
			// 基礎ノックバック値
			skill.knockBack = 4;
			// 基礎クールタイム
			skill.cooltime = 900l;

			skill.attackEffResId.add(R.drawable.slash_1);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.slash_2);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.slash_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.slash_4);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.slash_1);
			BitmapCache.setImage(R.drawable.slash_2);
			BitmapCache.setImage(R.drawable.slash_3);
			BitmapCache.setImage(R.drawable.slash_4);
			skill.scale = 1f;
		} else if (skillName.equals(res.getString(R.string.skill04_name))) {
			// Lv1ファイヤー1
			skill.name = skillName;
			skill.kind = Const.SKILL_FIRE_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 25; // キャラクターとの画像X軸位置調整
			skill.positionY = -50; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 40;// 攻撃範囲X軸
			skill.rengeY = 40;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 50;
			skill.attackRengeY = 50;
			// 基礎攻撃値
			skill.baseDamage = 6;
			// 基礎ノックバック値
			skill.knockBack = 4;
			// 基礎クールタイム
			skill.cooltime = 2500l;

			skill.attackEffResId.add(R.drawable.lv1_fire_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_fire_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_fire_3);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_fire_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_fire_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_fire_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_fire_7);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.lv1_fire_1);
			BitmapCache.setImage(R.drawable.lv1_fire_2);
			BitmapCache.setImage(R.drawable.lv1_fire_3);
			BitmapCache.setImage(R.drawable.lv1_fire_4);
			BitmapCache.setImage(R.drawable.lv1_fire_5);
			BitmapCache.setImage(R.drawable.lv1_fire_6);
			BitmapCache.setImage(R.drawable.lv1_fire_7);
			skill.scale = 0.5f;
		} else if (skillName.equals(res.getString(R.string.skill05_name))) {
			// Lv1ファイヤー2
			skill.name = skillName;
			skill.kind = Const.SKILL_FIRE_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 25; // キャラクターとの画像X軸位置調整
			skill.positionY = -65; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 60;// 攻撃範囲X軸
			skill.rengeY = 50;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 85;
			skill.attackRengeY = 85;
			// 基礎攻撃値
			skill.baseDamage = 10;
			// 基礎ノックバック値
			skill.knockBack = 8;
			// 基礎クールタイム
			skill.cooltime = 2400l;

			skill.attackEffResId.add(R.drawable.lv1_fire_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_fire_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_fire_3);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_fire_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_fire_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_fire_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_fire_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_fire_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_fire_7);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.lv1_fire_1);
			BitmapCache.setImage(R.drawable.lv1_fire_2);
			BitmapCache.setImage(R.drawable.lv1_fire_3);
			BitmapCache.setImage(R.drawable.lv1_fire_4);
			BitmapCache.setImage(R.drawable.lv1_fire_5);
			BitmapCache.setImage(R.drawable.lv1_fire_6);
			BitmapCache.setImage(R.drawable.lv1_fire_7);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill06_name))) {
			// Lv1ファイヤー3
			skill.name = skillName;
			skill.kind = Const.SKILL_FIRE_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 25; // キャラクターとの画像X軸位置調整
			skill.positionY = -85; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 70;// 攻撃範囲X軸
			skill.rengeY = 60;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 105;
			skill.attackRengeY = 95;
			// 基礎攻撃値
			skill.baseDamage = 20;
			// 基礎ノックバック値
			skill.knockBack = 10;
			// 基礎クールタイム
			skill.cooltime = 2300l;

			skill.attackEffResId.add(R.drawable.lv1_fire_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_fire_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_fire_3);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_fire_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_fire_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_fire_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_fire_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_fire_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_fire_7);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.lv1_fire_1);
			BitmapCache.setImage(R.drawable.lv1_fire_2);
			BitmapCache.setImage(R.drawable.lv1_fire_3);
			BitmapCache.setImage(R.drawable.lv1_fire_4);
			BitmapCache.setImage(R.drawable.lv1_fire_5);
			BitmapCache.setImage(R.drawable.lv1_fire_6);
			BitmapCache.setImage(R.drawable.lv1_fire_7);
			skill.scale = 0.8f;

		} else if (skillName.equals(res.getString(R.string.skill07_name))) {
			// Lv2ファイヤー1
			skill.name = skillName;
			skill.kind = Const.SKILL_FLAME_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -100; // キャラクターとの画像X軸位置調整
			skill.positionY = -50; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 150;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 110;
			skill.attackRengeY = 120;
			// 基礎攻撃値
			skill.baseDamage = 30;
			// 基礎ノックバック値
			skill.knockBack = 12;
			// 基礎クールタイム
			skill.cooltime = 2300l;

			skill.attackEffResId.add(R.drawable.lv2_fire_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_fire_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_fire_3);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_8);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_fire_9);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.lv2_fire_1);
			BitmapCache.setImage(R.drawable.lv2_fire_2);
			BitmapCache.setImage(R.drawable.lv2_fire_3);
			BitmapCache.setImage(R.drawable.lv2_fire_4);
			BitmapCache.setImage(R.drawable.lv2_fire_5);
			BitmapCache.setImage(R.drawable.lv2_fire_6);
			BitmapCache.setImage(R.drawable.lv2_fire_7);
			BitmapCache.setImage(R.drawable.lv2_fire_8);
			BitmapCache.setImage(R.drawable.lv2_fire_8);
			skill.scale = 0.8f;
		} else if (skillName.equals(res.getString(R.string.skill08_name))) {
			// Lv2ファイヤー2
			skill.name = skillName;
			skill.kind = Const.SKILL_FLAME_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -100; // キャラクターとの画像X軸位置調整
			skill.positionY = -50; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 170;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 130;
			skill.attackRengeY = 140;
			// 基礎攻撃値
			skill.baseDamage = 35;
			// 基礎ノックバック値
			skill.knockBack = 16;
			// 基礎クールタイム
			skill.cooltime = 2200l;

			skill.attackEffResId.add(R.drawable.lv2_fire_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_fire_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_fire_3);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_8);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_fire_9);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.lv2_fire_1);
			BitmapCache.setImage(R.drawable.lv2_fire_2);
			BitmapCache.setImage(R.drawable.lv2_fire_3);
			BitmapCache.setImage(R.drawable.lv2_fire_4);
			BitmapCache.setImage(R.drawable.lv2_fire_5);
			BitmapCache.setImage(R.drawable.lv2_fire_6);
			BitmapCache.setImage(R.drawable.lv2_fire_7);
			BitmapCache.setImage(R.drawable.lv2_fire_8);
			BitmapCache.setImage(R.drawable.lv2_fire_8);
			skill.scale = 1f;

		} else if (skillName.equals(res.getString(R.string.skill09_name))) {
			// Lv2ファイヤー3
			skill.name = skillName;
			skill.kind = Const.SKILL_FLAME_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定
			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -80; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 170;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 130;
			skill.attackRengeY = 180;
			// 基礎攻撃値
			skill.baseDamage = 40;
			// 基礎ノックバック値
			skill.knockBack = 18;
			// 基礎クールタイム
			skill.cooltime = 2200l;

			skill.attackEffResId.add(R.drawable.lv2_fire_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_fire_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_fire_3);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_fire_8);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_fire_9);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.lv2_fire_1);
			BitmapCache.setImage(R.drawable.lv2_fire_2);
			BitmapCache.setImage(R.drawable.lv2_fire_3);
			BitmapCache.setImage(R.drawable.lv2_fire_4);
			BitmapCache.setImage(R.drawable.lv2_fire_5);
			BitmapCache.setImage(R.drawable.lv2_fire_6);
			BitmapCache.setImage(R.drawable.lv2_fire_7);
			BitmapCache.setImage(R.drawable.lv2_fire_8);
			BitmapCache.setImage(R.drawable.lv2_fire_8);
			skill.scale = 1.2f;

		} else if (skillName.equals(res.getString(R.string.skill10_name))) {
			// Lv3_ファイヤー１
			skill.name = skillName;
			skill.kind = Const.SKILL_METEOR_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 25; // キャラクターとのX軸位置調整
									// やっぱりいろいろ調整のパラメータは持っておくと使える
			skill.positionY = -55; // キャラクターとのY軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 300;// 攻撃範囲X軸
			skill.rengeY = 60;// 攻撃範囲Y軸
			// 攻撃の座標を起点としてヒット判定がある範囲
			skill.attackRengeX = 60;
			skill.attackRengeY = 60;
			// 基礎攻撃値
			skill.baseDamage = 2;
			// 基礎ノックバック値
			skill.knockBack = 5;
			// 基礎クールタイム
			skill.cooltime = 6000l;

			for (int i = 0; i <= 5; i++) {
				skill.attackEffResId.add(R.drawable.lv3_fire_1);
				skill.hitFlameList.add(false);
				skill.attackEffResId.add(R.drawable.lv3_fire_2);
				skill.hitFlameList.add(false);
				skill.attackEffResId.add(R.drawable.lv3_fire_3);
				skill.hitFlameList.add(false);
			}
			skill.attackEffResId.add(R.drawable.lv3_fire_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_8);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_9);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_10);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_11);
			skill.hitFlameList.add(true);
			BitmapCache.setImage(R.drawable.lv3_fire_1);
			BitmapCache.setImage(R.drawable.lv3_fire_2);
			BitmapCache.setImage(R.drawable.lv3_fire_3);
			BitmapCache.setImage(R.drawable.lv3_fire_4);
			BitmapCache.setImage(R.drawable.lv3_fire_5);
			BitmapCache.setImage(R.drawable.lv3_fire_6);
			BitmapCache.setImage(R.drawable.lv3_fire_7);
			BitmapCache.setImage(R.drawable.lv3_fire_8);
			BitmapCache.setImage(R.drawable.lv3_fire_9);
		} else if (skillName.equals(res.getString(R.string.skill11_name))) {
			// Lv3_ファイヤー2
			skill.name = skillName;
			skill.kind = Const.SKILL_METEOR_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 25; // キャラクターとのX軸位置調整
									// やっぱりいろいろ調整のパラメータは持っておくと使える
			skill.positionY = -55; // キャラクターとのY軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 300;// 攻撃範囲X軸
			skill.rengeY = 60;// 攻撃範囲Y軸
			// 攻撃の座標を起点としてヒット判定がある範囲
			skill.attackRengeX = 60;
			skill.attackRengeY = 60;
			// 基礎攻撃値
			skill.baseDamage = 2;
			// 基礎ノックバック値
			skill.knockBack = 5;
			// 基礎クールタイム
			skill.cooltime = 6000l;

			for (int i = 0; i <= 5; i++) {
				skill.attackEffResId.add(R.drawable.lv3_fire_1);
				skill.hitFlameList.add(false);
				skill.attackEffResId.add(R.drawable.lv3_fire_2);
				skill.hitFlameList.add(false);
				skill.attackEffResId.add(R.drawable.lv3_fire_3);
				skill.hitFlameList.add(false);
			}
			skill.attackEffResId.add(R.drawable.lv3_fire_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_8);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_9);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_10);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_11);
			skill.hitFlameList.add(true);
			BitmapCache.setImage(R.drawable.lv3_fire_1);
			BitmapCache.setImage(R.drawable.lv3_fire_2);
			BitmapCache.setImage(R.drawable.lv3_fire_3);
			BitmapCache.setImage(R.drawable.lv3_fire_4);
			BitmapCache.setImage(R.drawable.lv3_fire_5);
			BitmapCache.setImage(R.drawable.lv3_fire_6);
			BitmapCache.setImage(R.drawable.lv3_fire_7);
			BitmapCache.setImage(R.drawable.lv3_fire_8);
			BitmapCache.setImage(R.drawable.lv3_fire_9);
		} else if (skillName.equals(res.getString(R.string.skill12_name))) {
			// Lv3_ファイヤー3
			skill.name = skillName;
			skill.kind = Const.SKILL_METEOR_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 25; // キャラクターとのX軸位置調整
									// やっぱりいろいろ調整のパラメータは持っておくと使える
			skill.positionY = -55; // キャラクターとのY軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 300;// 攻撃範囲X軸
			skill.rengeY = 60;// 攻撃範囲Y軸
			// 攻撃の座標を起点としてヒット判定がある範囲
			skill.attackRengeX = 60;
			skill.attackRengeY = 60;
			// 基礎攻撃値
			skill.baseDamage = 2;
			// 基礎ノックバック値
			skill.knockBack = 5;
			// 基礎クールタイム
			skill.cooltime = 6000l;

			for (int i = 0; i <= 5; i++) {
				skill.attackEffResId.add(R.drawable.lv3_fire_1);
				skill.hitFlameList.add(false);
				skill.attackEffResId.add(R.drawable.lv3_fire_2);
				skill.hitFlameList.add(false);
				skill.attackEffResId.add(R.drawable.lv3_fire_3);
				skill.hitFlameList.add(false);
			}
			skill.attackEffResId.add(R.drawable.lv3_fire_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_8);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_9);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_8);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_9);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_10);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_fire_11);
			skill.hitFlameList.add(true);
			BitmapCache.setImage(R.drawable.lv3_fire_1);
			BitmapCache.setImage(R.drawable.lv3_fire_2);
			BitmapCache.setImage(R.drawable.lv3_fire_3);
			BitmapCache.setImage(R.drawable.lv3_fire_4);
			BitmapCache.setImage(R.drawable.lv3_fire_5);
			BitmapCache.setImage(R.drawable.lv3_fire_6);
			BitmapCache.setImage(R.drawable.lv3_fire_7);
			BitmapCache.setImage(R.drawable.lv3_fire_8);
			BitmapCache.setImage(R.drawable.lv3_fire_9);
		} else if (skillName.equals(res.getString(R.string.skill13_name))) {
			// Lv1アイス1
			skill.name = skillName;
			skill.kind = Const.SKILL_ICE_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -20; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 100;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 70;
			skill.attackRengeY = 80;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 3000l;

			skill.attackEffResId.add(R.drawable.lv1_ice_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_ice_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_7);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv1_ice_1);
			BitmapCache.setImage(R.drawable.lv1_ice_2);
			BitmapCache.setImage(R.drawable.lv1_ice_3);
			BitmapCache.setImage(R.drawable.lv1_ice_4);
			BitmapCache.setImage(R.drawable.lv1_ice_5);
			BitmapCache.setImage(R.drawable.lv1_ice_6);
			BitmapCache.setImage(R.drawable.lv1_ice_7);
			skill.scale = 0.5f;

		} else if (skillName.equals(res.getString(R.string.skill14_name))) {
			// Lv1アイス2
			skill.name = skillName;
			skill.kind = Const.SKILL_ICE_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -150; // キャラクターとの画像X軸位置調整
			skill.positionY = -45; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 110;// 攻撃範囲X軸
			skill.rengeY = 100;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 80;
			skill.attackRengeY = 90;
			// 基礎攻撃値
			skill.baseDamage = 13;
			// 基礎ノックバック値
			skill.knockBack = 4;
			// 基礎クールタイム
			skill.cooltime = 3000l;

			skill.attackEffResId.add(R.drawable.lv1_ice_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_ice_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_7);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv1_ice_1);
			BitmapCache.setImage(R.drawable.lv1_ice_2);
			BitmapCache.setImage(R.drawable.lv1_ice_3);
			BitmapCache.setImage(R.drawable.lv1_ice_4);
			BitmapCache.setImage(R.drawable.lv1_ice_5);
			BitmapCache.setImage(R.drawable.lv1_ice_6);
			BitmapCache.setImage(R.drawable.lv1_ice_7);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill15_name))) {
			// Lv1アイス3
			skill.name = skillName;
			skill.kind = Const.SKILL_ICE_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -200; // キャラクターとの画像X軸位置調整
			skill.positionY = -60; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 130;// 攻撃範囲X軸
			skill.rengeY = 110;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 100;
			skill.attackRengeY = 110;
			// 基礎攻撃値
			skill.baseDamage = 18;
			// 基礎ノックバック値
			skill.knockBack = 6;
			// 基礎クールタイム
			skill.cooltime = 1000l;

			skill.attackEffResId.add(R.drawable.lv1_ice_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_ice_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_ice_7);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.lv1_ice_1);
			BitmapCache.setImage(R.drawable.lv1_ice_2);
			BitmapCache.setImage(R.drawable.lv1_ice_3);
			BitmapCache.setImage(R.drawable.lv1_ice_4);
			BitmapCache.setImage(R.drawable.lv1_ice_5);
			BitmapCache.setImage(R.drawable.lv1_ice_6);
			BitmapCache.setImage(R.drawable.lv1_ice_7);
			skill.scale = 0.8f;
		} else if (skillName.equals(res.getString(R.string.skill16_name))) {
			// Lv2アイス1
			skill.name = skillName;
			skill.kind = Const.SKILL_FREEZE_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -20; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 180;// 攻撃範囲X軸
			skill.rengeY = 120;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 90;
			skill.attackRengeY = 160;
			// 基礎攻撃値
			skill.baseDamage = 25;
			// 基礎ノックバック値
			skill.knockBack = 6;
			// 基礎クールタイム
			skill.cooltime = 1000l;

			skill.attackEffResId.add(R.drawable.lv2_ice_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_ice_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_ice_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_ice_7);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv2_ice_1);
			BitmapCache.setImage(R.drawable.lv2_ice_2);
			BitmapCache.setImage(R.drawable.lv2_ice_3);
			BitmapCache.setImage(R.drawable.lv2_ice_4);
			BitmapCache.setImage(R.drawable.lv2_ice_5);
			BitmapCache.setImage(R.drawable.lv2_ice_6);
			BitmapCache.setImage(R.drawable.lv2_ice_7);
			skill.scale = 0.5f;
		} else if (skillName.equals(res.getString(R.string.skill17_name))) {
			// Lv2アイス2
			skill.name = skillName;
			skill.kind = Const.SKILL_FREEZE_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -30; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 180;// 攻撃範囲X軸
			skill.rengeY = 120;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 120;
			skill.attackRengeY = 180;
			// 基礎攻撃値
			skill.baseDamage = 30;
			// 基礎ノックバック値
			skill.knockBack = 7;
			// 基礎クールタイム
			skill.cooltime = 980l;

			skill.attackEffResId.add(R.drawable.lv2_ice_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_ice_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_ice_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_ice_7);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv2_ice_1);
			BitmapCache.setImage(R.drawable.lv2_ice_2);
			BitmapCache.setImage(R.drawable.lv2_ice_3);
			BitmapCache.setImage(R.drawable.lv2_ice_4);
			BitmapCache.setImage(R.drawable.lv2_ice_5);
			BitmapCache.setImage(R.drawable.lv2_ice_6);
			BitmapCache.setImage(R.drawable.lv2_ice_7);
			skill.scale = 0.6f;
		} else if (skillName.equals(res.getString(R.string.skill18_name))) {
			// Lv2アイス3
			skill.name = skillName;
			skill.kind = Const.SKILL_FREEZE_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -30; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 190;// 攻撃範囲X軸
			skill.rengeY = 120;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 130;
			skill.attackRengeY = 180;
			// 基礎攻撃値
			skill.baseDamage = 35;
			// 基礎ノックバック値
			skill.knockBack = 8;
			// 基礎クールタイム
			skill.cooltime = 980l;

			skill.attackEffResId.add(R.drawable.lv2_ice_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_ice_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_ice_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_ice_7);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv2_ice_1);
			BitmapCache.setImage(R.drawable.lv2_ice_2);
			BitmapCache.setImage(R.drawable.lv2_ice_3);
			BitmapCache.setImage(R.drawable.lv2_ice_4);
			BitmapCache.setImage(R.drawable.lv2_ice_5);
			BitmapCache.setImage(R.drawable.lv2_ice_6);
			BitmapCache.setImage(R.drawable.lv2_ice_7);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill19_name))) {
			// Lv3アイス1
			skill.name = skillName;
			skill.kind = Const.SKILL_GLACIAL_AGE_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -20; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 100;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 170;
			skill.attackRengeY = 120;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv3_ice_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_ice_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_ice_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_ice_7);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv3_ice_1);
			BitmapCache.setImage(R.drawable.lv3_ice_2);
			BitmapCache.setImage(R.drawable.lv3_ice_3);
			BitmapCache.setImage(R.drawable.lv3_ice_4);
			BitmapCache.setImage(R.drawable.lv3_ice_5);
			BitmapCache.setImage(R.drawable.lv3_ice_6);
			BitmapCache.setImage(R.drawable.lv3_ice_7);

		} else if (skillName.equals(res.getString(R.string.skill20_name))) {
			// Lv3アイス2
			skill.name = skillName;
			skill.kind = Const.SKILL_GLACIAL_AGE_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -20; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 100;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 170;
			skill.attackRengeY = 120;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv3_ice_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_ice_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_ice_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_ice_7);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv3_ice_1);
			BitmapCache.setImage(R.drawable.lv3_ice_2);
			BitmapCache.setImage(R.drawable.lv3_ice_3);
			BitmapCache.setImage(R.drawable.lv3_ice_4);
			BitmapCache.setImage(R.drawable.lv3_ice_5);
			BitmapCache.setImage(R.drawable.lv3_ice_6);
			BitmapCache.setImage(R.drawable.lv3_ice_7);

		} else if (skillName.equals(res.getString(R.string.skill21_name))) {
			// Lv3アイス3
			skill.name = skillName;
			skill.kind = Const.SKILL_GLACIAL_AGE_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -20; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 100;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 170;
			skill.attackRengeY = 120;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv3_ice_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_ice_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_ice_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_ice_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_ice_7);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_ice_7);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv3_ice_1);
			BitmapCache.setImage(R.drawable.lv3_ice_2);
			BitmapCache.setImage(R.drawable.lv3_ice_3);
			BitmapCache.setImage(R.drawable.lv3_ice_4);
			BitmapCache.setImage(R.drawable.lv3_ice_5);
			BitmapCache.setImage(R.drawable.lv3_ice_6);
			BitmapCache.setImage(R.drawable.lv3_ice_7);

		} else if (skillName.equals(res.getString(R.string.skill22_name))) {
			// Lv1サンダー1
			skill.name = skillName;
			skill.kind = Const.SKILL_THUNDER_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -100; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 50;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 50;
			skill.attackRengeY = 170;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv1_thunder_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_thunder_7);
			skill.hitFlameList.add(true);

			BitmapCache.setImage(R.drawable.lv1_thunder_1);
			BitmapCache.setImage(R.drawable.lv1_thunder_2);
			BitmapCache.setImage(R.drawable.lv1_thunder_3);
			BitmapCache.setImage(R.drawable.lv1_thunder_4);
			BitmapCache.setImage(R.drawable.lv1_thunder_5);
			BitmapCache.setImage(R.drawable.lv1_thunder_6);
			BitmapCache.setImage(R.drawable.lv1_thunder_1);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill23_name))) {
			// Lv1サンダー2
			skill.name = skillName;
			skill.kind = Const.SKILL_THUNDER_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -100; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 70;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 50;
			skill.attackRengeY = 170;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv1_thunder_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_thunder_7);
			skill.hitFlameList.add(true);

			BitmapCache.setImage(R.drawable.lv1_thunder_1);
			BitmapCache.setImage(R.drawable.lv1_thunder_2);
			BitmapCache.setImage(R.drawable.lv1_thunder_3);
			BitmapCache.setImage(R.drawable.lv1_thunder_4);
			BitmapCache.setImage(R.drawable.lv1_thunder_5);
			BitmapCache.setImage(R.drawable.lv1_thunder_6);
			BitmapCache.setImage(R.drawable.lv1_thunder_1);
			skill.scale = 0.8f;
		} else if (skillName.equals(res.getString(R.string.skill24_name))) {
			// Lv1サンダー3
			skill.name = skillName;
			skill.kind = Const.SKILL_THUNDER_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -130; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 80;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 50;
			skill.attackRengeY = 200;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv1_thunder_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_thunder_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_thunder_7);
			skill.hitFlameList.add(true);

			BitmapCache.setImage(R.drawable.lv1_thunder_1);
			BitmapCache.setImage(R.drawable.lv1_thunder_2);
			BitmapCache.setImage(R.drawable.lv1_thunder_3);
			BitmapCache.setImage(R.drawable.lv1_thunder_4);
			BitmapCache.setImage(R.drawable.lv1_thunder_5);
			BitmapCache.setImage(R.drawable.lv1_thunder_6);
			BitmapCache.setImage(R.drawable.lv1_thunder_1);
			skill.scale = 1f;
		} else if (skillName.equals(res.getString(R.string.skill25_name))) {
			// Lv2サンダー1
			skill.name = skillName;
			skill.kind = Const.SKILL_LIGHTNING_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -100; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 100;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 60;
			skill.attackRengeY = 200;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv2_thunder_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_thunder_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_thunder_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_thunder_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_thunder_7);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv2_thunder_1);
			BitmapCache.setImage(R.drawable.lv2_thunder_2);
			BitmapCache.setImage(R.drawable.lv2_thunder_3);
			BitmapCache.setImage(R.drawable.lv2_thunder_4);
			BitmapCache.setImage(R.drawable.lv2_thunder_5);
			BitmapCache.setImage(R.drawable.lv2_thunder_6);
			BitmapCache.setImage(R.drawable.lv2_thunder_1);
			skill.scale = 0.5f;

		} else if (skillName.equals(res.getString(R.string.skill26_name))) {
			// Lv2サンダー2
			skill.name = skillName;
			skill.kind = Const.SKILL_LIGHTNING_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -140; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 120;// 攻撃範囲X軸
			skill.rengeY = 90;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 70;
			skill.attackRengeY = 250;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;
			skill.attackEffResId.add(R.drawable.lv2_thunder_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_thunder_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_thunder_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_thunder_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_thunder_7);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv2_thunder_1);
			BitmapCache.setImage(R.drawable.lv2_thunder_2);
			BitmapCache.setImage(R.drawable.lv2_thunder_3);
			BitmapCache.setImage(R.drawable.lv2_thunder_4);
			BitmapCache.setImage(R.drawable.lv2_thunder_5);
			BitmapCache.setImage(R.drawable.lv2_thunder_6);
			BitmapCache.setImage(R.drawable.lv2_thunder_1);
			skill.scale = 0.75f;
		} else if (skillName.equals(res.getString(R.string.skill27_name))) {
			// Lv2サンダー3
			skill.name = skillName;
			skill.kind = Const.SKILL_LIGHTNING_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -180; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 120;// 攻撃範囲X軸
			skill.rengeY = 90;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 90;
			skill.attackRengeY = 270;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv2_thunder_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_thunder_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_thunder_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_thunder_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_thunder_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_thunder_7);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv2_thunder_1);
			BitmapCache.setImage(R.drawable.lv2_thunder_2);
			BitmapCache.setImage(R.drawable.lv2_thunder_3);
			BitmapCache.setImage(R.drawable.lv2_thunder_4);
			BitmapCache.setImage(R.drawable.lv2_thunder_5);
			BitmapCache.setImage(R.drawable.lv2_thunder_6);
			BitmapCache.setImage(R.drawable.lv2_thunder_1);
			skill.scale = 0.85f;
		} else if (skillName.equals(res.getString(R.string.skill28_name))) {
			// Lv3サンダー1
			skill.name = skillName;
			skill.kind = Const.SKILL_THUNDERSTORM_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -140; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 50;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 60;
			skill.attackRengeY = 280;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv3_thunder_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_thunder_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_thunder_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_thunder_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_8);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_8);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv3_thunder_1);
			BitmapCache.setImage(R.drawable.lv3_thunder_2);
			BitmapCache.setImage(R.drawable.lv3_thunder_3);
			BitmapCache.setImage(R.drawable.lv3_thunder_4);
			BitmapCache.setImage(R.drawable.lv3_thunder_5);
			BitmapCache.setImage(R.drawable.lv3_thunder_6);
			BitmapCache.setImage(R.drawable.lv3_thunder_7);
			BitmapCache.setImage(R.drawable.lv3_thunder_8);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill29_name))) {
			// Lv3サンダー2
			skill.name = skillName;
			skill.kind = Const.SKILL_THUNDERSTORM_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -140; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 50;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 60;
			skill.attackRengeY = 280;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv3_thunder_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_thunder_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_thunder_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_thunder_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_8);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_8);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv3_thunder_1);
			BitmapCache.setImage(R.drawable.lv3_thunder_2);
			BitmapCache.setImage(R.drawable.lv3_thunder_3);
			BitmapCache.setImage(R.drawable.lv3_thunder_4);
			BitmapCache.setImage(R.drawable.lv3_thunder_5);
			BitmapCache.setImage(R.drawable.lv3_thunder_6);
			BitmapCache.setImage(R.drawable.lv3_thunder_7);
			BitmapCache.setImage(R.drawable.lv3_thunder_8);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill30_name))) {
			// Lv3サンダー3
			skill.name = skillName;
			skill.kind = Const.SKILL_THUNDERSTORM_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -140; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 50;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 60;
			skill.attackRengeY = 280;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv3_thunder_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_thunder_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_thunder_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_thunder_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_8);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_thunder_8);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv3_thunder_1);
			BitmapCache.setImage(R.drawable.lv3_thunder_2);
			BitmapCache.setImage(R.drawable.lv3_thunder_3);
			BitmapCache.setImage(R.drawable.lv3_thunder_4);
			BitmapCache.setImage(R.drawable.lv3_thunder_5);
			BitmapCache.setImage(R.drawable.lv3_thunder_6);
			BitmapCache.setImage(R.drawable.lv3_thunder_7);
			BitmapCache.setImage(R.drawable.lv3_thunder_8);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill31_name))) {
			// Lv1アース1
			skill.name = skillName;
			skill.kind = Const.SKILL_ROCK_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -35; // キャラクターとの画像X軸位置調整
			skill.positionY = -70; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 80;// 攻撃範囲X軸
			skill.rengeY = 60;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 100;
			skill.attackRengeY = 100;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv1_earth_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_earth_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_earth_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_earth_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_earth_5);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv1_earth_1);
			BitmapCache.setImage(R.drawable.lv1_earth_2);
			BitmapCache.setImage(R.drawable.lv1_earth_3);
			BitmapCache.setImage(R.drawable.lv1_earth_4);
			BitmapCache.setImage(R.drawable.lv1_earth_5);
			skill.scale = 1f;
		} else if (skillName.equals(res.getString(R.string.skill32_name))) {
			// Lv1アース2
			skill.name = skillName;
			skill.kind = Const.SKILL_ROCK_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 0; // キャラクターとの画像X軸位置調整
			skill.positionY = -90; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 90;// 攻撃範囲X軸
			skill.rengeY = 60;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 110;
			skill.attackRengeY = 110;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv1_earth_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_earth_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_earth_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_earth_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_earth_5);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv1_earth_1);
			BitmapCache.setImage(R.drawable.lv1_earth_2);
			BitmapCache.setImage(R.drawable.lv1_earth_3);
			BitmapCache.setImage(R.drawable.lv1_earth_4);
			BitmapCache.setImage(R.drawable.lv1_earth_5);
			skill.scale = 1.2f;
		} else if (skillName.equals(res.getString(R.string.skill33_name))) {
			// Lv1アース3
			skill.name = skillName;
			skill.kind = Const.SKILL_ROCK_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 0; // キャラクターとの画像X軸位置調整
			skill.positionY = -120; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 90;// 攻撃範囲X軸
			skill.rengeY = 60;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 120;
			skill.attackRengeY = 120;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv1_earth_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_earth_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_earth_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_earth_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_earth_5);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv1_earth_1);
			BitmapCache.setImage(R.drawable.lv1_earth_2);
			BitmapCache.setImage(R.drawable.lv1_earth_3);
			BitmapCache.setImage(R.drawable.lv1_earth_4);
			BitmapCache.setImage(R.drawable.lv1_earth_5);
			skill.scale = 1.5f;
		} else if (skillName.equals(res.getString(R.string.skill34_name))) {
			// Lv2アース1
			skill.name = skillName;
			skill.kind = Const.SKILL_RUGGED_MOUNTAIN_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 30; // キャラクターとの画像X軸位置調整
			skill.positionY = -80; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 100;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 120;
			skill.attackRengeY = 120;
			// 基礎攻撃値
			skill.baseDamage = 7;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv2_earth_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_earth_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_earth_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_earth_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_earth_6);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv2_earth_1);
			BitmapCache.setImage(R.drawable.lv2_earth_2);
			BitmapCache.setImage(R.drawable.lv2_earth_3);
			BitmapCache.setImage(R.drawable.lv2_earth_4);
			BitmapCache.setImage(R.drawable.lv2_earth_5);
			BitmapCache.setImage(R.drawable.lv2_earth_6);
			skill.scale = 0.8f;
		} else if (skillName.equals(res.getString(R.string.skill35_name))) {
			// Lv2アース2
			skill.name = skillName;
			skill.kind = Const.SKILL_RUGGED_MOUNTAIN_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 40; // キャラクターとの画像X軸位置調整
			skill.positionY = -110; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 110;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 130;
			skill.attackRengeY = 130;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv2_earth_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_earth_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_earth_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_earth_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_earth_6);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv2_earth_1);
			BitmapCache.setImage(R.drawable.lv2_earth_2);
			BitmapCache.setImage(R.drawable.lv2_earth_3);
			BitmapCache.setImage(R.drawable.lv2_earth_4);
			BitmapCache.setImage(R.drawable.lv2_earth_5);
			BitmapCache.setImage(R.drawable.lv2_earth_6);
			skill.scale = 1f;
		} else if (skillName.equals(res.getString(R.string.skill36_name))) {
			// Lv2アース3
			skill.name = skillName;
			skill.kind = Const.SKILL_RUGGED_MOUNTAIN_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 50; // キャラクターとの画像X軸位置調整
			skill.positionY = -130; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 120;// 攻撃範囲X軸
			skill.rengeY = 80;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 140;
			skill.attackRengeY = 140;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv2_earth_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_earth_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_earth_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_earth_4);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_earth_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_earth_6);
			skill.hitFlameList.add(false);

			BitmapCache.setImage(R.drawable.lv2_earth_1);
			BitmapCache.setImage(R.drawable.lv2_earth_2);
			BitmapCache.setImage(R.drawable.lv2_earth_3);
			BitmapCache.setImage(R.drawable.lv2_earth_4);
			BitmapCache.setImage(R.drawable.lv2_earth_5);
			BitmapCache.setImage(R.drawable.lv2_earth_6);
			skill.scale = 1.2f;
		} else if (skillName.equals(res.getString(R.string.skill37_name))) {
			// Lv3アース1
			skill.name = skillName;
			skill.kind = Const.SKILL_EARTHQUAKE_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 0; // キャラクターとの画像X軸位置調整
			skill.positionY = -50; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 200;// 攻撃範囲X軸
			skill.rengeY = 120;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 280;
			skill.attackRengeY = 200;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv3_earth_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_earth_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_earth_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_8);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_9);
			skill.hitFlameList.add(false);

			skill.attackEffResId.add(R.drawable.lv3_earth_7);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.lv3_earth_1);
			BitmapCache.setImage(R.drawable.lv3_earth_2);
			BitmapCache.setImage(R.drawable.lv3_earth_3);
			BitmapCache.setImage(R.drawable.lv3_earth_4);
			BitmapCache.setImage(R.drawable.lv3_earth_5);
			BitmapCache.setImage(R.drawable.lv3_earth_6);
			BitmapCache.setImage(R.drawable.lv3_earth_7);
			BitmapCache.setImage(R.drawable.lv3_earth_8);
			BitmapCache.setImage(R.drawable.lv3_earth_9);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill38_name))) {
			// Lv3アース2
			skill.name = skillName;
			skill.kind = Const.SKILL_EARTHQUAKE_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 0; // キャラクターとの画像X軸位置調整
			skill.positionY = -50; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 200;// 攻撃範囲X軸
			skill.rengeY = 120;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 280;
			skill.attackRengeY = 200;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv3_earth_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_earth_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_earth_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_8);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_9);
			skill.hitFlameList.add(false);

			skill.attackEffResId.add(R.drawable.lv3_earth_7);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.lv3_earth_1);
			BitmapCache.setImage(R.drawable.lv3_earth_2);
			BitmapCache.setImage(R.drawable.lv3_earth_3);
			BitmapCache.setImage(R.drawable.lv3_earth_4);
			BitmapCache.setImage(R.drawable.lv3_earth_5);
			BitmapCache.setImage(R.drawable.lv3_earth_6);
			BitmapCache.setImage(R.drawable.lv3_earth_7);
			BitmapCache.setImage(R.drawable.lv3_earth_8);
			BitmapCache.setImage(R.drawable.lv3_earth_9);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill39_name))) {
			// Lv3アース3
			skill.name = skillName;
			skill.kind = Const.SKILL_EARTHQUAKE_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = 0; // キャラクターとの画像X軸位置調整
			skill.positionY = -50; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 200;// 攻撃範囲X軸
			skill.rengeY = 120;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 280;
			skill.attackRengeY = 200;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv3_earth_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_5);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_earth_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_earth_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_8);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_9);
			skill.hitFlameList.add(false);

			skill.attackEffResId.add(R.drawable.lv3_earth_7);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.lv3_earth_1);
			BitmapCache.setImage(R.drawable.lv3_earth_2);
			BitmapCache.setImage(R.drawable.lv3_earth_3);
			BitmapCache.setImage(R.drawable.lv3_earth_4);
			BitmapCache.setImage(R.drawable.lv3_earth_5);
			BitmapCache.setImage(R.drawable.lv3_earth_6);
			BitmapCache.setImage(R.drawable.lv3_earth_7);
			BitmapCache.setImage(R.drawable.lv3_earth_8);
			BitmapCache.setImage(R.drawable.lv3_earth_9);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill40_name))) {
			// Lv1ウィンド1
			skill.name = skillName;
			skill.kind = Const.SKILL_WIND_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -120; // キャラクターとの画像X軸位置調整
			skill.positionY = -115; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 50;// 攻撃範囲X軸
			skill.rengeY = 50;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 140;
			skill.attackRengeY = 60;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv1_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_wind_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_wind_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_8);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_7);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.lv1_wind_1);
			BitmapCache.setImage(R.drawable.lv1_wind_2);
			BitmapCache.setImage(R.drawable.lv1_wind_3);
			BitmapCache.setImage(R.drawable.lv1_wind_4);
			BitmapCache.setImage(R.drawable.lv1_wind_5);
			BitmapCache.setImage(R.drawable.lv1_wind_6);
			BitmapCache.setImage(R.drawable.lv1_wind_7);
			BitmapCache.setImage(R.drawable.lv1_wind_8);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill41_name))) {
			// Lv1ウィンド2
			skill.name = skillName;
			skill.kind = Const.SKILL_WIND_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -150; // キャラクターとの画像X軸位置調整
			skill.positionY = -165; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 60;// 攻撃範囲X軸
			skill.rengeY = 60;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 150;
			skill.attackRengeY = 70;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv1_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_wind_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_wind_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_8);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_7);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.lv1_wind_1);
			BitmapCache.setImage(R.drawable.lv1_wind_2);
			BitmapCache.setImage(R.drawable.lv1_wind_3);
			BitmapCache.setImage(R.drawable.lv1_wind_4);
			BitmapCache.setImage(R.drawable.lv1_wind_5);
			BitmapCache.setImage(R.drawable.lv1_wind_6);
			BitmapCache.setImage(R.drawable.lv1_wind_7);
			BitmapCache.setImage(R.drawable.lv1_wind_8);
			skill.scale = 0.8f;
		} else if (skillName.equals(res.getString(R.string.skill42_name))) {
			// Lv1ウィンド3
			skill.name = skillName;
			skill.kind = Const.SKILL_WIND_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -190; // キャラクターとの画像X軸位置調整
			skill.positionY = -205; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 70;// 攻撃範囲X軸
			skill.rengeY = 70;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 160;
			skill.attackRengeY = 80;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv1_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_wind_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_wind_7);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_8);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_earth_7);
			skill.hitFlameList.add(false);
			BitmapCache.setImage(R.drawable.lv1_wind_1);
			BitmapCache.setImage(R.drawable.lv1_wind_2);
			BitmapCache.setImage(R.drawable.lv1_wind_3);
			BitmapCache.setImage(R.drawable.lv1_wind_4);
			BitmapCache.setImage(R.drawable.lv1_wind_5);
			BitmapCache.setImage(R.drawable.lv1_wind_6);
			BitmapCache.setImage(R.drawable.lv1_wind_7);
			BitmapCache.setImage(R.drawable.lv1_wind_8);
			skill.scale = 1f;
		} else if (skillName.equals(res.getString(R.string.skill43_name))) {
			// Lv2ウィンド1
			skill.name = skillName;
			skill.kind = Const.SKILL_GUST_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -115; // キャラクターとの画像X軸位置調整
			skill.positionY = -115; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 90;// 攻撃範囲X軸
			skill.rengeY = 70;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 200;
			skill.attackRengeY = 100;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv2_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_wind_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_wind_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_wind_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_wind_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_wind_6);
			skill.hitFlameList.add(true);

			BitmapCache.setImage(R.drawable.lv2_wind_1);
			BitmapCache.setImage(R.drawable.lv2_wind_2);
			BitmapCache.setImage(R.drawable.lv2_wind_3);
			BitmapCache.setImage(R.drawable.lv2_wind_4);
			BitmapCache.setImage(R.drawable.lv2_wind_5);
			BitmapCache.setImage(R.drawable.lv2_wind_6);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill44_name))) {
			// Lv2ウィンド2
			skill.name = skillName;
			skill.kind = Const.SKILL_GUST_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -145; // キャラクターとの画像X軸位置調整
			skill.positionY = -145; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 100;// 攻撃範囲X軸
			skill.rengeY = 70;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 250;
			skill.attackRengeY = 100;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv2_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_wind_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_wind_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_wind_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_wind_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_wind_6);
			skill.hitFlameList.add(true);

			BitmapCache.setImage(R.drawable.lv2_wind_1);
			BitmapCache.setImage(R.drawable.lv2_wind_2);
			BitmapCache.setImage(R.drawable.lv2_wind_3);
			BitmapCache.setImage(R.drawable.lv2_wind_4);
			BitmapCache.setImage(R.drawable.lv2_wind_5);
			BitmapCache.setImage(R.drawable.lv2_wind_6);
			skill.scale = 0.8f;
		} else if (skillName.equals(res.getString(R.string.skill45_name))) {
			// Lv2ウィンド3
			skill.name = skillName;
			skill.kind = Const.SKILL_GUST_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -175; // キャラクターとの画像X軸位置調整
			skill.positionY = -175; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 110;// 攻撃範囲X軸
			skill.rengeY = 70;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 270;
			skill.attackRengeY = 100;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv2_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_wind_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_wind_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_wind_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_wind_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv2_wind_6);
			skill.hitFlameList.add(true);

			BitmapCache.setImage(R.drawable.lv2_wind_1);
			BitmapCache.setImage(R.drawable.lv2_wind_2);
			BitmapCache.setImage(R.drawable.lv2_wind_3);
			BitmapCache.setImage(R.drawable.lv2_wind_4);
			BitmapCache.setImage(R.drawable.lv2_wind_5);
			BitmapCache.setImage(R.drawable.lv2_wind_6);
			skill.scale = 1f;
		} else if (skillName.equals(res.getString(R.string.skill46_name))) {
			// Lv3ウィンド1
			skill.name = skillName;
			skill.kind = Const.SKILL_TORNADO_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -155; // キャラクターとの画像X軸位置調整
			skill.positionY = -115; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 100;// 攻撃範囲X軸
			skill.rengeY = 100;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 180;
			skill.attackRengeY = 180;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv3_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_wind_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_wind_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_wind_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_wind_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_wind_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_wind_7);
			skill.hitFlameList.add(true);

			skill.attackEffResId.add(R.drawable.lv3_wind_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_wind_7);
			skill.hitFlameList.add(true);

			skill.attackEffResId.add(R.drawable.lv3_wind_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_wind_7);
			skill.hitFlameList.add(true);
			BitmapCache.setImage(R.drawable.lv3_wind_1, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_2, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_3, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_4, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_5, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_6, 1.5f);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill47_name))) {
			// Lv3ウィンド2
			skill.name = skillName;
			skill.kind = Const.SKILL_TORNADO_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -155; // キャラクターとの画像X軸位置調整
			skill.positionY = -115; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 100;// 攻撃範囲X軸
			skill.rengeY = 100;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 180;
			skill.attackRengeY = 180;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv3_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_wind_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_wind_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_wind_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_wind_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_wind_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_wind_7);
			skill.hitFlameList.add(true);

			skill.attackEffResId.add(R.drawable.lv3_wind_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_wind_7);
			skill.hitFlameList.add(true);

			skill.attackEffResId.add(R.drawable.lv3_wind_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_wind_7);
			skill.hitFlameList.add(true);
			BitmapCache.setImage(R.drawable.lv3_wind_1, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_2, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_3, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_4, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_5, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_6, 1.5f);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill48_name))) {
			// Lv3ウィンド3
			skill.name = skillName;
			skill.kind = Const.SKILL_TORNADO_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -155; // キャラクターとの画像X軸位置調整
			skill.positionY = -115; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 100;// 攻撃範囲X軸
			skill.rengeY = 100;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 180;
			skill.attackRengeY = 180;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv3_wind_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_wind_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_wind_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_wind_4);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv3_wind_5);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_wind_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_wind_7);
			skill.hitFlameList.add(true);

			skill.attackEffResId.add(R.drawable.lv3_wind_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_wind_7);
			skill.hitFlameList.add(true);

			skill.attackEffResId.add(R.drawable.lv3_wind_6);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv3_wind_7);
			skill.hitFlameList.add(true);
			BitmapCache.setImage(R.drawable.lv3_wind_1, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_2, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_3, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_4, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_5, 1.5f);
			BitmapCache.setImage(R.drawable.lv3_wind_6, 1.5f);
			skill.scale = 0.65f;
		} else if (skillName.equals(res.getString(R.string.skill49_name))) {
			// Lv2ノーマル１
			skill.name = skillName;
			skill.kind = Const.SKILL_ENAGYBOLL_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -20; // キャラクターとの画像X軸位置調整
									// やっぱりいろいろ調整のパラメータは持っておくと使える
			skill.positionY = -70; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 150;// 攻撃範囲X軸
			skill.rengeY = 40;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 30;
			skill.attackRengeY = 30;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv1_charge_1);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_charge_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_charge_3);
			skill.hitFlameList.add(false);
			for (int i = 0; i <= 2; i++) {
				skill.attackEffResId.add(R.drawable.lv1_charge_4);
				skill.hitFlameList.add(false);
				skill.attackEffResId.add(R.drawable.lv1_charge_5);
				skill.hitFlameList.add(false);
			}
			skill.attackEffResId.add(R.drawable.lv1_charge_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_charge_7);
			skill.hitFlameList.add(true);

			for (int i = 0; i <= 10; i++) {
				skill.attackEffResId.add(R.drawable.lv1_charge_8);
				skill.hitFlameList.add(true);
			}

			BitmapCache.setImage(R.drawable.lv1_charge_1);
			BitmapCache.setImage(R.drawable.lv1_charge_2);
			BitmapCache.setImage(R.drawable.lv1_charge_3);
			BitmapCache.setImage(R.drawable.lv1_charge_4);
			BitmapCache.setImage(R.drawable.lv1_charge_5);
			BitmapCache.setImage(R.drawable.lv1_charge_6);
			BitmapCache.setImage(R.drawable.lv1_charge_7);
			BitmapCache.setImage(R.drawable.lv1_charge_8);
			skill.scale = 0.5f;
		} else if (skillName.equals(res.getString(R.string.skill50_name))) {
			// Lv2ノーマル2
			skill.name = skillName;
			skill.kind = Const.SKILL_ENAGYBOLL_LV2;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -30; // キャラクターとの画像X軸位置調整
									// やっぱりいろいろ調整のパラメータは持っておくと使える
			skill.positionY = -100; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 190;// 攻撃範囲X軸
			skill.rengeY = 40;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 50;
			skill.attackRengeY = 50;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv1_charge_1);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_charge_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_charge_3);
			skill.hitFlameList.add(false);
			for (int i = 0; i <= 2; i++) {
				skill.attackEffResId.add(R.drawable.lv1_charge_4);
				skill.hitFlameList.add(false);
				skill.attackEffResId.add(R.drawable.lv1_charge_5);
				skill.hitFlameList.add(false);
			}
			skill.attackEffResId.add(R.drawable.lv1_charge_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_charge_7);
			skill.hitFlameList.add(true);

			for (int i = 0; i <= 10; i++) {
				skill.attackEffResId.add(R.drawable.lv1_charge_8);
				skill.hitFlameList.add(true);
			}

			BitmapCache.setImage(R.drawable.lv1_charge_1);
			BitmapCache.setImage(R.drawable.lv1_charge_2);
			BitmapCache.setImage(R.drawable.lv1_charge_3);
			BitmapCache.setImage(R.drawable.lv1_charge_4);
			BitmapCache.setImage(R.drawable.lv1_charge_5);
			BitmapCache.setImage(R.drawable.lv1_charge_6);
			BitmapCache.setImage(R.drawable.lv1_charge_7);
			BitmapCache.setImage(R.drawable.lv1_charge_8);
			skill.scale = 0.8f;
		} else if (skillName.equals(res.getString(R.string.skill51_name))) {
			// Lv2ノーマル3
			skill.name = skillName;
			skill.kind = Const.SKILL_ENAGYBOLL_LV3;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -40; // キャラクターとの画像X軸位置調整
									// やっぱりいろいろ調整のパラメータは持っておくと使える
			skill.positionY = -110; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 200;// 攻撃範囲X軸
			skill.rengeY = 40;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 60;
			skill.attackRengeY = 60;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv1_charge_1);
			skill.hitFlameList.add(true);
			skill.attackEffResId.add(R.drawable.lv1_charge_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_charge_3);
			skill.hitFlameList.add(false);
			for (int i = 0; i <= 2; i++) {
				skill.attackEffResId.add(R.drawable.lv1_charge_4);
				skill.hitFlameList.add(false);
				skill.attackEffResId.add(R.drawable.lv1_charge_5);
				skill.hitFlameList.add(false);
			}
			skill.attackEffResId.add(R.drawable.lv1_charge_6);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv1_charge_7);
			skill.hitFlameList.add(true);

			for (int i = 0; i <= 10; i++) {
				skill.attackEffResId.add(R.drawable.lv1_charge_8);
				skill.hitFlameList.add(true);
			}

			BitmapCache.setImage(R.drawable.lv1_charge_1);
			BitmapCache.setImage(R.drawable.lv1_charge_2);
			BitmapCache.setImage(R.drawable.lv1_charge_3);
			BitmapCache.setImage(R.drawable.lv1_charge_4);
			BitmapCache.setImage(R.drawable.lv1_charge_5);
			BitmapCache.setImage(R.drawable.lv1_charge_6);
			BitmapCache.setImage(R.drawable.lv1_charge_7);
			BitmapCache.setImage(R.drawable.lv1_charge_8);
			skill.scale = 1f;
		} else if (skillName.equals(res.getString(R.string.skill52_name))) {
			// Lv3ノーマル1
			skill.name = skillName;
			skill.kind = Const.SKILL_ARROW_LV1;// nameでリソースファイルから文字列比較するとパフォーマンスが悪くなるため、疑似命名空間を設定

			// パラメーター設定
			skill.positionX = -50; // キャラクターとの画像X軸位置調整
									// やっぱりいろいろ調整のパラメータは持っておくと使える
			skill.positionY = -180; // キャラクターとの画像Y軸位置調整
			// 間合い範囲(使用モンスターの中心からの距離で攻撃が届く範囲)
			skill.rengeX = 250;// 攻撃範囲X軸
			skill.rengeY = 40;// 攻撃範囲Y軸
			// 攻撃の座標を中心としてヒット判定がある範囲
			skill.attackRengeX = 140;
			skill.attackRengeY = 50;
			// 基礎攻撃値
			skill.baseDamage = 3;
			// 基礎ノックバック値
			skill.knockBack = 1;
			// 基礎クールタイム
			skill.cooltime = 4000l;

			skill.attackEffResId.add(R.drawable.lv2_charge_1);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_charge_2);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_charge_3);
			skill.hitFlameList.add(false);
			skill.attackEffResId.add(R.drawable.lv2_charge_4);
			skill.hitFlameList.add(false);
			for (int i = 0; i <= 8; i++) {
				skill.attackEffResId.add(R.drawable.lv2_charge_5);
				skill.hitFlameList.add(false);
				skill.attackEffResId.add(R.drawable.lv2_charge_6);
				skill.hitFlameList.add(false);
				skill.attackEffResId.add(R.drawable.lv2_charge_7);
				skill.hitFlameList.add(true);
				skill.attackEffResId.add(R.drawable.lv2_charge_8);
				skill.hitFlameList.add(true);

			}

			BitmapCache.setImage(R.drawable.lv2_charge_1);
			BitmapCache.setImage(R.drawable.lv2_charge_2);
			BitmapCache.setImage(R.drawable.lv2_charge_3);
			BitmapCache.setImage(R.drawable.lv2_charge_4);
			BitmapCache.setImage(R.drawable.lv2_charge_5);
			BitmapCache.setImage(R.drawable.lv2_charge_6);
			BitmapCache.setImage(R.drawable.lv2_charge_7);
			BitmapCache.setImage(R.drawable.lv2_charge_8);
			skill.scale = 0.65f;

		}

		return skill;
	}

}

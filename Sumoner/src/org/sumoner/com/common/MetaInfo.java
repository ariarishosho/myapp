package org.sumoner.com.common;

import java.util.List;

/**
 *
 * アプリケーション内の様々なメタなデータを格納します
 *
 * @author arichi
 *
 */
public class MetaInfo {
	/**
	 * 所持金
	 */
	public long money;

	/**
	 * クリアステージ
	 */
	public int stage;

	/**
	 * 今までに取得したことのあるモンスターNo
	 */
	public List<MiniMonster> takenMonster;

	/**
	 * Monsterオブジェクトほどではない最低限のモンスター情報
	 *
	 * @author ARICHI_SHOTA
	 *
	 */
	public class MiniMonster {
		public int ArrayId;
		public List<String> addSkillNames;;
		public List<String> holdAddAbilityNames;
	}

	/**
	 * 配合促進設備のランク
	 */
	public int appointments1Rank;
	/**
	 * 継承促進設備のランク
	 */
	public int appointments2Rank;
	/**
	 * ランクアップ促進設備のランク
	 */
	public int appointments3Rank;
}

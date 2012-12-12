package org.sumoner.com.common;

import java.util.Comparator;


/**
 * モンスターの表示位置をソートする
 * @author arichi
 *
 */
public class LineComparator implements Comparator<Monster> {

	@Override
	public int compare(Monster lhs, Monster rhs) {

		int l = lhs.battlePlace ;
		int r = rhs.battlePlace ;

		if(l > r)return 1;
		else if (l== r) return 0;
		else return -1;
	}

}

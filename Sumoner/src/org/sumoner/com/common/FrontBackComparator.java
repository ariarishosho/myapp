package org.sumoner.com.common;

import java.util.Comparator;


/**
 * モンスターの表示位置をソートする
 * @author arichi
 *
 */
public class FrontBackComparator implements Comparator<Monster> {

	@Override
	public int compare(Monster lhs, Monster rhs) {

		float l = lhs.dmY +lhs.getBitmap().getHeight();
		float r = rhs.dmY +rhs.getBitmap().getHeight();

		if(l > r)return 1;
		else if (l== r) return 0;
		else return -1;
	}

}

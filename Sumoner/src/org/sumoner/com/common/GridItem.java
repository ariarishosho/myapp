package org.sumoner.com.common;

public class GridItem {
	public int resId;// 画像のリソースID
	public String strData;// 表示用文字列
	public String strData1;// 文字列
	public String strData2;// 文字列
	public Integer IntegerData;
	public Integer IntegerData1;
	public Integer IntegerData2;
	public boolean boolData;
	public boolean boolData1;
	public boolean boolData2;

	public GridItem(int resId, String arg0) {
		this.resId = resId;
		this.strData = arg0;
	}

	public GridItem(int resId, String arg0, String arg1, String arg2) {
		this.resId = resId;
		this.strData = arg0;
		this.strData1 = arg1;
		this.strData2 = arg2;
	}

	public GridItem(int resId, String arg0, Integer arg1) {
		this.resId = resId;
		this.strData = arg0;
		this.IntegerData = arg1;
	}
}

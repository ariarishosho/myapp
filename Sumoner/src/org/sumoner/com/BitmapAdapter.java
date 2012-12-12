package org.sumoner.com;

import java.util.List;

import org.sumoner.com.common.BitmapCache;
import org.sumoner.com.common.GridItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class BitmapAdapter extends ArrayAdapter<GridItem> {

	private int resourceId;

	public BitmapAdapter(Context context, int resource, List<GridItem> objects) {
		super(context, resource, objects);
		resourceId = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// View自体がない場合は新規に設定
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resourceId, null);
		}

		View view = convertView;
		// Viewを独自拡張しているため、親Viewから子ビューを取り出して設定
		ImageView iamge = (ImageView) view.findViewById(R.id.itmeImageView);
		int resId = getItem(position).resId;
		// BitmapCache.setImage(resId,60,60);
		iamge.setImageBitmap(BitmapCache.getImage(resId));

		if (getItem(position).strData != null) {
			TextView text = (TextView) view.findViewById(R.id.strDataText);
			text.setText(getItem(position).strData);
		}
		if (getItem(position).strData1 != null) {
			TextView text = (TextView) view.findViewById(R.id.strDataText1);
			text.setText(getItem(position).strData1);
		}
		if (getItem(position).strData2 != null) {
			TextView text = (TextView) view.findViewById(R.id.strDataText2);
			text.setText(getItem(position).strData2);
		}
		if (getItem(position).IntegerData != null) {
			TextView text = (TextView) view.findViewById(R.id.intgerDataText);
			text.setText(getItem(position).IntegerData.toString());
		}
		if (getItem(position).IntegerData1 != null) {
			TextView text = (TextView) view.findViewById(R.id.intgerDataText1);
			text.setText(getItem(position).IntegerData1.toString());
		}
		if (getItem(position).IntegerData2 != null) {
			TextView text = (TextView) view.findViewById(R.id.intgerDataText2);
			text.setText(getItem(position).IntegerData2.toString());
		}
		CheckBox check = (CheckBox) view.findViewById(R.id.boolDataCheckbox);
		check.setChecked(getItem(position).boolData);
		check = (CheckBox) view.findViewById(R.id.boolDataCheckbox1);
		check.setChecked(getItem(position).boolData1);
		check = (CheckBox) view.findViewById(R.id.boolDataCheckbox2);
		check.setChecked(getItem(position).boolData2);

		return view;
	}

}
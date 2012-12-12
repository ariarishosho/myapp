package org.sumoner.com.common;

import android.view.KeyEvent;

/**
 * ユーザーキーイベントの情報
 */
public class KeyGameEvent extends GameEvent {
	/**
	 * Simple constructor to make populating this event easier.
	 */
	public KeyGameEvent(int keyCode, boolean up, KeyEvent msg) {
		this.keyCode = keyCode;
		this.msg = msg;
		this.up = up;
	}

	public int keyCode;
	public KeyEvent msg;
	public boolean up;
}
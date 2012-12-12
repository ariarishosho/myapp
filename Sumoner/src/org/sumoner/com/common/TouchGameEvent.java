package org.sumoner.com.common;

import android.view.MotionEvent;

/**
 * ユーザータッチイベントの情報 the standard onKey
 */
public class TouchGameEvent extends GameEvent {
	/**
	 * Simple constructor to make populating this event easier.
	 */
	public TouchGameEvent(MotionEvent msg) {
		this.msg = msg;
	}

	public MotionEvent msg;
}
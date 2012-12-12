package org.sumoner.com.common;

import android.view.MotionEvent;

/**
 * ユーザータッチイベントの情報 the standard onKey
 */
public class DoubleTapGameEvent extends GameEvent {
	/**
	 * Simple constructor to make populating this event easier.
	 */
	public DoubleTapGameEvent(MotionEvent msg) {
		this.msg = msg;
	}

	public MotionEvent msg;
}
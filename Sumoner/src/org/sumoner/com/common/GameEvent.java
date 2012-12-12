package org.sumoner.com.common;

/**
 * Base class for any external event passed to the JetBoyThread. This can
 * include user input, system events, network input, etc.
 */
public class GameEvent {
	public GameEvent() {
		eventTime = System.currentTimeMillis();
	}

	long eventTime;
}
package com.yongyida.robot.voice.camera.utils;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ThreadUtil {

	private static ScheduledThreadPoolExecutor executor;

	public static synchronized ScheduledThreadPoolExecutor getExecutor() {

		if (executor == null) {
			executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime()
					.availableProcessors());

		}
		return executor;

	}

}

package com.lvrenyang.utils;

import java.io.File;

import android.os.Environment;

/**
 * �ⲿ�Ϳ�ͨѶ��Ҫʹ�õ���Ϣ�������ﶨ�塣
 * 
 * @author Administrator
 * 
 */
public class Constant {

	public static final int MSG_NETHEARTBEATTHREAD_UPDATESTATUS = 100100;
	public static final int MSG_BTHEARTBEATTHREAD_UPDATESTATUS = 100200;
	public static final int MSG_USBHEARTBEATTHREAD_UPDATESTATUS = 100300;

	public static final String dumpfile = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator
			+ "dump.txt";
}

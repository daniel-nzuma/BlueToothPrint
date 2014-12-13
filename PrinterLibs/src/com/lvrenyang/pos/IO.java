package com.lvrenyang.pos;

import android.os.Environment;

import com.lvrenyang.rwbt.BTRWThread;
import com.lvrenyang.rwusb.USBRWThread;
import com.lvrenyang.rwwifi.NETRWThread;
import com.lvrenyang.utils.DataUtils;
import com.lvrenyang.utils.FileUtils;

/**
 * IO ��������͵ײ�Ľӿڡ� �ϲ��д������IO����ĺ����� IO ����rwbt��rwwifi��rwusb��rwbuf�ȵײ㺯����
 * 
 * @author Administrator
 * 
 */
public class IO {

	public static final String debugtxt = Environment
			.getExternalStorageDirectory() + "/debug.txt";

	public static final int PORT_NET = 1;
	public static final int PORT_BT = 2;
	public static final int PORT_USB = 3;

	private static int curPort = PORT_BT;

	public static synchronized int GetCurPort() {
		return curPort;
	}

	public static synchronized void SetCurPort(int port) {
		curPort = port;
	}

	public static synchronized int Write(byte[] buffer, int offset, int count) {
		FileUtils.AddToFile(DataUtils.bytesToStr(buffer, offset, count)
				+ "\r\n", debugtxt);
		if (curPort == PORT_NET) {
			return NETRWThread.Write(buffer, offset, count);
		} else if (curPort == PORT_BT) {
			return BTRWThread.Write(buffer, offset, count);
		} else if (curPort == PORT_USB) {
			return USBRWThread.Write(buffer, offset, count);
		} else {
			return 0;
		}
	}

	public static synchronized int Read(byte[] buffer, int byteOffset,
			int byteCount, int timeout) {
		if (curPort == PORT_NET) {
			return NETRWThread.Read(buffer, byteOffset, byteCount, timeout);
		} else if (curPort == PORT_BT) {
			return BTRWThread.Read(buffer, byteOffset, byteCount, timeout);
		} else if (curPort == PORT_USB) {
			return USBRWThread.Read(buffer, byteOffset, byteCount, timeout);
		} else {
			return 0;
		}
	}

	public static synchronized boolean Request(byte sendbuf[], int sendlen,
			int requestlen, byte recbuf[], Integer reclen, int timeout) {
		if (curPort == PORT_NET) {
			return NETRWThread.Request(sendbuf, sendlen, requestlen, recbuf,
					reclen, timeout);
		} else if (curPort == PORT_BT) {
			return BTRWThread.Request(sendbuf, sendlen, requestlen, recbuf,
					reclen, timeout);
		} else if (curPort == PORT_USB) {
			return USBRWThread.Request(sendbuf, sendlen, requestlen, recbuf,
					reclen, timeout);
		} else {
			return false;
		}
	}

	public static synchronized void ClrRec() {
		if (curPort == PORT_NET)
			NETRWThread.ClrRec();
		else if (curPort == PORT_BT)
			BTRWThread.ClrRec();
		else if (curPort == PORT_USB)
			USBRWThread.ClrRec();
	}

	public static synchronized boolean IsEmpty() {
		if (curPort == PORT_NET)
			return NETRWThread.IsEmpty();
		else if (curPort == PORT_BT)
			return BTRWThread.IsEmpty();
		else if (curPort == PORT_USB)
			return USBRWThread.IsEmpty();
		else
			return true;
	}

	public static synchronized byte GetByte() {
		if (curPort == PORT_NET)
			return NETRWThread.GetByte();
		else if (curPort == PORT_BT)
			return BTRWThread.GetByte();
		else if (curPort == PORT_USB)
			return USBRWThread.GetByte();
		else
			return 0;
	}

	public static synchronized boolean IsOpened() {
		if (curPort == PORT_NET)
			return NETRWThread.IsOpened();
		else if (curPort == PORT_BT)
			return BTRWThread.IsOpened();
		else if (curPort == PORT_USB)
			return USBRWThread.IsOpened();
		else
			return false;
	}

}
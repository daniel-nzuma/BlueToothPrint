package com.lvrenyang.rwusb;

import android.annotation.TargetApi;
import android.app.PendingIntent;
//import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.lvrenyang.utils.DataUtils;

/**
 * ��һ�㣬USB���� Χ����USBPort������
 * 
 * @author Administrator
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class USBDriver {

	/**
	 * probe��disconnect����ʹ��
	 * 
	 * @param port
	 * @param id
	 * @return
	 */
	int probe(USBPort port, USBDeviceId id[]) {
		if (null == port || null == id)
			return RTNCode.NULLPOINTER;
		if ((null == port.mUsbManager) || (null == port.mUsbDevice)
				|| (null == port.mPermissionIntent))
			return RTNCode.NULLPOINTER;

		for (int i = 0; i < id.length; i++)
			if (id[i].idVendor == port.mUsbDevice.getVendorId()
					&& id[i].idProduct == port.mUsbDevice.getProductId()) {

				if (!port.mUsbManager.hasPermission(port.mUsbDevice))
					return RTNCode.NOPERMISSION;

				// ö�٣��Ѷ�д���ƶ˿�ʲô�ĸ�Ū������Ȼ��set
				outer: for (int k = 0; k < port.mUsbDevice.getInterfaceCount(); k++) {
					port.mUsbInterface = port.mUsbDevice.getInterface(k);
					port.mUsbEndpointOut = null;
					port.mUsbEndpointIn = null;
					for (int j = 0; j < port.mUsbInterface.getEndpointCount(); j++) {
						UsbEndpoint endpoint = port.mUsbInterface
								.getEndpoint(j);
						if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT
								&& endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
							port.mUsbEndpointOut = endpoint;
						} else if (endpoint.getDirection() == UsbConstants.USB_DIR_IN
								&& endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
							port.mUsbEndpointIn = endpoint;
						}

						// ����ڵ�һ���ӿھ��ҵ��˷���Ҫ��Ķ˵㣬��ôbreak;
						if ((null != port.mUsbEndpointOut)
								&& (null != port.mUsbEndpointIn))
							break outer;
					}
				}
				if (null == port.mUsbInterface)
					return RTNCode.NULLPOINTER;
				if ((null == port.mUsbEndpointOut)
						|| (null == port.mUsbEndpointIn))
					return RTNCode.NULLPOINTER;
				port.mUsbDeviceConnection = port.mUsbManager
						.openDevice(port.mUsbDevice);
				if (null == port.mUsbDeviceConnection)
					return RTNCode.NULLPOINTER;
				port.mUsbDeviceConnection.claimInterface(port.mUsbInterface,
						true);
				return RTNCode.OK;
			}

		return RTNCode.ERROR;
	}

	/**
	 * disconnect��probe����ʹ��
	 * 
	 * @param port
	 */
	void disconnect(USBPort port) {
		if (null == port)
			return;
		if ((null != port.mUsbInterface) && (null != port.mUsbDeviceConnection)) {
			port.mUsbDeviceConnection.releaseInterface(port.mUsbInterface);
			port.mUsbDeviceConnection.close();
		}
	}

	int write(USBPort port, byte[] buffer, int offset, int count, int timeout) {
		if (null == port || null == buffer)
			return RTNCode.NULLPOINTER;
		if (null == port.mUsbEndpointOut)
			return RTNCode.NULLPOINTER;
		if (null == port.mUsbDeviceConnection)
			return RTNCode.NULLPOINTER;
		if (count < 0 || offset < 0 || timeout <= 0)
			return RTNCode.INVALPARAM;
		byte[] data = new byte[count];
		DataUtils.copyBytes(buffer, offset, data, 0, count);
		return port.mUsbDeviceConnection.bulkTransfer(port.mUsbEndpointOut,
				data, data.length, timeout);
	}

	int read(USBPort port, byte[] buffer, int offset, int count, int timeout) {
		if (null == port || null == buffer)
			return RTNCode.NULLPOINTER;
		if (null == port.mUsbEndpointIn)
			return RTNCode.NULLPOINTER;
		if (null == port.mUsbDeviceConnection)
			return RTNCode.NULLPOINTER;
		if (count < 0 || offset < 0 || timeout <= 0)
			return RTNCode.INVALPARAM;
		byte[] data = new byte[count];
		int recnt = port.mUsbDeviceConnection.bulkTransfer(port.mUsbEndpointIn,
				data, data.length, timeout);
		DataUtils.copyBytes(data, 0, buffer, offset, recnt);
		return recnt; // ���ض�ȡ���ֽ���
	}

	int ctl(USBPort port, int requestType, int request, int value, int index,
			byte[] buffer, int length, int timeout) {
		if (null == port)
			return RTNCode.NULLPOINTER;
		if (null == port.mUsbDeviceConnection)
			return RTNCode.NULLPOINTER;

		return port.mUsbDeviceConnection.controlTransfer(requestType, request,
				value, index, buffer, length, timeout);
	}

	public static class RTNCode {

		public static final int OK = 0;
		public static final int ERROR = -1000;
		/**
		 * ��ָ�����
		 */
		public static final int NULLPOINTER = -1001;
		public static final int NOPERMISSION = -1002;

		/**
		 * ����������Ҫ����n��Ҫ���ڵ���0����������С��0
		 */
		public static final int INVALPARAM = -1003;

		public static final int EXCEPTION = -1004;

		public static final int NOTCONNECTED = -1005;

		public static final int NOTOPENED = -1006;

		public static final int NOTIMPLEMENTED = -1007;
	}

	public static class USBDeviceId {
		int idVendor;
		int idProduct;

		public USBDeviceId(int vid, int pid) {
			idVendor = vid;
			idProduct = pid;
		}
	}

	/**
	 * ����ֻ��һ�����캯����һЩ����
	 * 
	 * @author Administrator
	 * 
	 */
	public static class USBPort implements Parcelable {

		UsbManager mUsbManager;
		UsbDevice mUsbDevice;
		PendingIntent mPermissionIntent;

		UsbInterface mUsbInterface;
		UsbEndpoint mUsbEndpointOut, mUsbEndpointIn;
		UsbDeviceConnection mUsbDeviceConnection;

		public USBPort(UsbManager usbManager, UsbDevice usbDevice,
				PendingIntent permissionIntent) {
			this.mUsbManager = usbManager;
			this.mUsbDevice = usbDevice;
			this.mPermissionIntent = permissionIntent;
		}

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel out, int flag) {
			// TODO Auto-generated method stub
			out.writeValue(mUsbManager);
			out.writeValue(mUsbDevice);
			out.writeValue(mPermissionIntent);
			out.writeValue(mUsbInterface);
			out.writeValue(mUsbEndpointOut);
			out.writeValue(mUsbEndpointIn);
			out.writeValue(mUsbDeviceConnection);
		}

		public static final Parcelable.Creator<USBPort> CREATOR = new Creator<USBPort>() {

			@Override
			public USBPort createFromParcel(Parcel in) {
				return new USBPort(in);
			}

			@Override
			public USBPort[] newArray(int size) {
				// TODO Auto-generated method stub
				return new USBPort[size];
			}
		};

		public USBPort(Parcel in) {
			mUsbManager = (UsbManager) in.readValue(UsbManager.class
					.getClassLoader());
			mUsbDevice = (UsbDevice) in.readValue(UsbDevice.class
					.getClassLoader());
			mPermissionIntent = (PendingIntent) in
					.readValue(PendingIntent.class.getClassLoader());
			mUsbInterface = (UsbInterface) in.readValue(UsbInterface.class
					.getClassLoader());
			mUsbEndpointOut = (UsbEndpoint) in.readValue(UsbEndpoint.class
					.getClassLoader());
			mUsbEndpointIn = (UsbEndpoint) in.readValue(UsbEndpoint.class
					.getClassLoader());
			mUsbDeviceConnection = (UsbDeviceConnection) in
					.readValue(UsbDeviceConnection.class.getClassLoader());
		}
	}
}

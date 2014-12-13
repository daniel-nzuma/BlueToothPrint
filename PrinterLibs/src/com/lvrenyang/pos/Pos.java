package com.lvrenyang.pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.lvrenyang.encryp.DES2;
import com.lvrenyang.pos.Cmd.ESCCmd;
import com.lvrenyang.utils.DataUtils;

public class Pos {

	private static final String TAG = "Pos";

	/**
	 * ����percount���ֽڵ����ݣ�֮���м䴩��һ��Test���������Ա����������
	 * 
	 * @param buffer
	 * @param offset
	 * @param count
	 * @param percount
	 * @return
	 */
	private static synchronized int POS_Write_Safety(byte[] buffer, int offset,
			int count, int percount) {
		int idx = 0;
		int curcount = 0;
		byte[] reset = { 0x1b, 0x40 };
		byte[] precbuf = new byte[1];
		int timeout = 1000;
		if (POS_QueryStatus(precbuf,timeout)) {
			while (idx < count) {
				if (count - idx > percount)
					curcount = percount;
				else
					curcount = count - idx;

				IO.Write(buffer, offset + idx, curcount);
				if (!POS_QueryStatus(precbuf,timeout))
					break;
				else
					IO.Write(reset, 0, reset.length);
				idx += curcount;
			}
		}
		return idx;
	}

	public static void saveDataToBin(String fileName, byte[] data) {
		File f = new File(Environment.getExternalStorageDirectory().getPath(),
				fileName);
		try {
			f.createNewFile();
		} catch (IOException e) {
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
			fOut.write(data, 0, data.length);
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	// nWidth����Ϊ8�ı���,���ֻ�����ϲ���Ƽ���
	// ֮����Ū��һά���飬����Ϊһά�����ٶȻ��һ��
	private static int[] p0 = { 0, 0x80 };
	private static int[] p1 = { 0, 0x40 };
	private static int[] p2 = { 0, 0x20 };
	private static int[] p3 = { 0, 0x10 };
	private static int[] p4 = { 0, 0x08 };
	private static int[] p5 = { 0, 0x04 };
	private static int[] p6 = { 0, 0x02 };

	// 1����Ϊ1��ͼƬ��������ӡ������
	@SuppressWarnings("unused")
	private static byte[] pixToCmd(byte[] src, int nWidth, int nMode) {
		// nWidth = 384; nHeight = 582;
		int nHeight = src.length / nWidth;
		byte[] data = new byte[8 + (src.length / 8)];
		data[0] = 0x1d;
		data[1] = 0x76;
		data[2] = 0x30;
		data[3] = (byte) (nMode & 0x01);
		data[4] = (byte) ((nWidth / 8) % 0x100);// (xl+xh*256)*8 = nWidth
		data[5] = (byte) ((nWidth / 8) / 0x100);
		data[6] = (byte) ((nHeight) % 0x100);// (yl+yh*256) = nHeight
		data[7] = (byte) ((nHeight) / 0x100);
		int k = 0;
		for (int i = 8; i < data.length; i++) {
			// ���У�û�м�Ȩ
			data[i] = (byte) (p0[src[k]] + p1[src[k + 1]] + p2[src[k + 2]]
					+ p3[src[k + 3]] + p4[src[k + 4]] + p5[src[k + 5]]
					+ p6[src[k + 6]] + src[k + 7]);
			k = k + 8;
		}
		return data;

	}

	private static byte[] eachLinePixToCmd(byte[] src, int nWidth, int nMode) {
		int nHeight = src.length / nWidth;
		int nBytesPerLine = nWidth / 8;
		byte[] data = new byte[nHeight * (8 + nBytesPerLine)];
		int offset = 0;
		int k = 0;
		for (int i = 0; i < nHeight; i++) {
			offset = i * (8 + nBytesPerLine);
			data[offset + 0] = 0x1d;
			data[offset + 1] = 0x76;
			data[offset + 2] = 0x30;
			data[offset + 3] = (byte) (nMode & 0x01);
			data[offset + 4] = (byte) (nBytesPerLine % 0x100);
			data[offset + 5] = (byte) (nBytesPerLine / 0x100);
			data[offset + 6] = 0x01;
			data[offset + 7] = 0x00;
			for (int j = 0; j < nBytesPerLine; j++) {
				data[offset + 8 + j] = (byte) (p0[src[k]] + p1[src[k + 1]]
						+ p2[src[k + 2]] + p3[src[k + 3]] + p4[src[k + 4]]
						+ p5[src[k + 5]] + p6[src[k + 6]] + src[k + 7]);
				k = k + 8;
			}
		}

		return data;
	}

	/**
	 * ��ARGBͼת��Ϊ��ֵͼ��0����ڣ�1�����
	 * 
	 * @param mBitmap
	 * @return
	 */
	public static byte[] bitmapToBWPix(Bitmap mBitmap) {

		int[] pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];
		byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight()];

		mBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0,
				mBitmap.getWidth(), mBitmap.getHeight());

		// for the toGrayscale, we need to select a red or green or blue color
		ImageProcessing.format_K_dither16x16(pixels, mBitmap.getWidth(),
				mBitmap.getHeight(), data);

		return data;
	}

	@SuppressWarnings("unused")
	private static void saveDataToBin(byte[] data) {
		File f = new File(Environment.getExternalStorageDirectory().getPath(),
				"Btatotest.bin");
		try {
			f.createNewFile();
		} catch (IOException e) {
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
			fOut.write(data, 0, data.length);
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	public static void saveMyBitmap(Bitmap mBitmap, String name) {
		File f = new File(Environment.getExternalStorageDirectory().getPath(),
				name);
		try {
			f.createNewFile();
		} catch (IOException e) {
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
			mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

	}

	public static void POS_PrintPicture(Bitmap mBitmap, int nWidth, int nMode) {

		// ��ת�ڰף��ٵ��ú�������λͼ
		// ��ת�ڰ�
		int width = ((nWidth + 7) / 8) * 8;
		int height = mBitmap.getHeight() * width / mBitmap.getWidth();
		height = ((height + 7) / 8) * 8;
		Bitmap rszBitmap = ImageProcessing.resizeImage(mBitmap, width, height);
		Bitmap grayBitmap = ImageProcessing.toGrayscale(rszBitmap);
		byte[] dithered = bitmapToBWPix(grayBitmap);

		byte[] data = eachLinePixToCmd(dithered, width, nMode);
		// ��width = 384ʱ��һ������ռ48���ֽڣ�����8���ֽ�ͷ���ܹ�width/8+8���ֽڡ�
		int nBytesPerLine = width / 8 + 8;
		int nLinesPerTest = 1;
		if (IO.GetCurPort() == IO.PORT_BT)
			nLinesPerTest = 30;
		else if (IO.GetCurPort() == IO.PORT_NET)
			nLinesPerTest = 5;
		else if (IO.GetCurPort() == IO.PORT_USB)
			nLinesPerTest = 60;

		POS_Write_Safety(data, 0, data.length, nBytesPerLine * nLinesPerTest);

	}

	private static byte[] thresholdToBWPic(Bitmap mBitmap) {
		int[] pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];
		byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight()];

		mBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0,
				mBitmap.getWidth(), mBitmap.getHeight());

		// for the toGrayscale, we need to select a red or green or blue color
		ImageProcessing.format_K_threshold(pixels, mBitmap.getWidth(),
				mBitmap.getHeight(), data);

		return data;
	}

	public static void POS_PrintBWPic(Bitmap mBitmap, int nWidth, int nMode) {
		// ��ת�ڰף��ٵ��ú�������λͼ
		// ��ת�ڰ�
		int width = ((nWidth + 7) / 8) * 8;
		int height = mBitmap.getHeight() * width / mBitmap.getWidth();
		height = ((height + 7) / 8) * 8;
		Bitmap rszBitmap = ImageProcessing.resizeImage(mBitmap, width, height);
		Bitmap grayBitmap = ImageProcessing.toGrayscale(rszBitmap);
		saveMyBitmap(grayBitmap, "gray.png");
		byte[] dithered = thresholdToBWPic(grayBitmap);

		byte[] data = eachLinePixToCmd(dithered, width, nMode);
		// ��width = 384ʱ��һ������ռ48���ֽڣ�����8���ֽ�ͷ���ܹ�width/8+8���ֽڡ�
		int nBytesPerLine = width / 8 + 8;
		int nLinesPerTest = 1;
		if (IO.GetCurPort() == IO.PORT_BT)
			nLinesPerTest = 30;
		else if (IO.GetCurPort() == IO.PORT_NET)
			nLinesPerTest = 5;
		else if (IO.GetCurPort() == IO.PORT_USB)
			nLinesPerTest = 60;

		POS_Write_Safety(data, 0, data.length, nBytesPerLine * nLinesPerTest);
	}

	// nFontType 0 ��׼ 1 ѹ�� ������ָ��
	public static void POS_S_TextOut(String pszString, String encoding,
			int nOrgx, int nWidthTimes, int nHeightTimes, int nFontType,
			int nFontStyle) {
		if (nOrgx > 65535 | nOrgx < 0 | nWidthTimes > 7 | nWidthTimes < 0
				| nHeightTimes > 7 | nHeightTimes < 0 | nFontType < 0
				| nFontType > 4 | (pszString.length() == 0))
			return;

		Cmd.ESCCmd.ESC_dollors_nL_nH[2] = (byte) (nOrgx % 0x100);
		Cmd.ESCCmd.ESC_dollors_nL_nH[3] = (byte) (nOrgx / 0x100);

		byte[] intToWidth = { 0x00, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70 };
		byte[] intToHeight = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
		Cmd.ESCCmd.GS_exclamationmark_n[2] = (byte) (intToWidth[nWidthTimes] + intToHeight[nHeightTimes]);

		byte[] tmp_ESC_M_n = Cmd.ESCCmd.ESC_M_n;
		if ((nFontType == 0) || (nFontType == 1))
			tmp_ESC_M_n[2] = (byte) nFontType;
		else
			tmp_ESC_M_n = new byte[0];

		// ������
		// �ݲ�֧��ƽ������
		Cmd.ESCCmd.GS_E_n[2] = (byte) ((nFontStyle >> 3) & 0x01);

		Cmd.ESCCmd.ESC_line_n[2] = (byte) ((nFontStyle >> 7) & 0x03);
		Cmd.ESCCmd.FS_line_n[2] = (byte) ((nFontStyle >> 7) & 0x03);

		Cmd.ESCCmd.ESC_lbracket_n[2] = (byte) ((nFontStyle >> 9) & 0x01);

		Cmd.ESCCmd.GS_B_n[2] = (byte) ((nFontStyle >> 10) & 0x01);

		Cmd.ESCCmd.ESC_V_n[2] = (byte) ((nFontStyle >> 12) & 0x01);

		byte[] pbString = null;
		try {
			pbString = pszString.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			return;
		}

		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.ESCCmd.ESC_dollors_nL_nH, Cmd.ESCCmd.GS_exclamationmark_n,
				tmp_ESC_M_n, Cmd.ESCCmd.GS_E_n, Cmd.ESCCmd.ESC_line_n,
				Cmd.ESCCmd.FS_line_n, Cmd.ESCCmd.ESC_lbracket_n,
				Cmd.ESCCmd.GS_B_n, Cmd.ESCCmd.ESC_V_n, pbString });

		IO.Write(data, 0, data.length);

	}

	public static void POS_FeedLine() {
		byte[] data = DataUtils.byteArraysToBytes(new byte[][] { Cmd.ESCCmd.CR,
				Cmd.ESCCmd.LF });
		// byte[] data = Cmd.ESCCmd.LF;
		IO.Write(data, 0, data.length);
	}

	public static void POS_S_Align(int align) {
		if (align < 0 || align > 2)
			return;
		byte[] data = Cmd.ESCCmd.ESC_a_n;
		data[2] = (byte) align;
		IO.Write(data, 0, data.length);
	}

	public static void POS_SetLineHeight(int nHeight) {
		if (nHeight < 0 || nHeight > 255)
			return;
		byte[] data = Cmd.ESCCmd.ESC_3_n;
		data[2] = (byte) nHeight;
		IO.Write(data, 0, data.length);
	}

	public static void POS_S_SetBarcode(String strCodedata, int nOrgx,
			int nType, int nWidthX, int nHeight, int nHriFontType,
			int nHriFontPosition) {
		if (nOrgx < 0 | nOrgx > 65535 | nType < 0x41 | nType > 0x49
				| nWidthX < 2 | nWidthX > 6 | nHeight < 1 | nHeight > 255)
			return;

		byte[] bCodeData = null;
		try {
			bCodeData = strCodedata.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			return;
		}
		;

		Cmd.ESCCmd.ESC_dollors_nL_nH[2] = (byte) (nOrgx % 0x100);
		Cmd.ESCCmd.ESC_dollors_nL_nH[3] = (byte) (nOrgx / 0x100);
		Cmd.ESCCmd.GS_w_n[2] = (byte) nWidthX;
		Cmd.ESCCmd.GS_h_n[2] = (byte) nHeight;
		Cmd.ESCCmd.GS_f_n[2] = (byte) (nHriFontType & 0x01);
		Cmd.ESCCmd.GS_H_n[2] = (byte) (nHriFontPosition & 0x03);
		Cmd.ESCCmd.GS_k_m_n_[2] = (byte) nType;
		Cmd.ESCCmd.GS_k_m_n_[3] = (byte) bCodeData.length;

		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.ESCCmd.ESC_dollors_nL_nH, Cmd.ESCCmd.GS_w_n,
				Cmd.ESCCmd.GS_h_n, Cmd.ESCCmd.GS_f_n, Cmd.ESCCmd.GS_H_n,
				Cmd.ESCCmd.GS_k_m_n_, bCodeData });
		IO.Write(data, 0, data.length);

	}

	public static void POS_S_SetQRcode(String strCodedata, int nWidthX,
			int nErrorCorrectionLevel) {

		if (nWidthX < 2 | nWidthX > 6 | nErrorCorrectionLevel < 1
				| nErrorCorrectionLevel > 4)
			return;

		byte[] bCodeData = null;
		try {
			bCodeData = strCodedata.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			return;
		}
		;

		Cmd.ESCCmd.GS_w_n[2] = (byte) nWidthX;
		Cmd.ESCCmd.GS_k_m_v_r_nL_nH[4] = (byte) nErrorCorrectionLevel;
		Cmd.ESCCmd.GS_k_m_v_r_nL_nH[5] = (byte) (bCodeData.length & 0xff);
		Cmd.ESCCmd.GS_k_m_v_r_nL_nH[6] = (byte) ((bCodeData.length & 0xff00) >> 8);

		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.ESCCmd.GS_w_n, Cmd.ESCCmd.GS_k_m_v_r_nL_nH, bCodeData });
		IO.Write(data, 0, data.length);

	}

	public static void POS_EPSON_SetQRCode(String strCodedata, int nWidthX,
			int nErrorCorrectionLevel) {
		if (nWidthX < 2 | nWidthX > 6 | nErrorCorrectionLevel < 1
				| nErrorCorrectionLevel > 4)
			return;

		byte[] bCodeData = null;
		try {
			bCodeData = strCodedata.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			return;
		}
		;

		Cmd.ESCCmd.GS_w_n[2] = (byte) nWidthX;
		Cmd.ESCCmd.GS_leftbracket_k_pL_pH_cn_69_n[7] = (byte) (47 + nErrorCorrectionLevel);
		Cmd.ESCCmd.GS_leftbracket_k_pL_pH_cn_80_m__d1dk[3] = (byte) ((bCodeData.length + 3) & 0xff);
		Cmd.ESCCmd.GS_leftbracket_k_pL_pH_cn_80_m__d1dk[4] = (byte) (((bCodeData.length + 3) & 0xff00) >> 8);

		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.ESCCmd.GS_w_n, Cmd.ESCCmd.GS_leftbracket_k_pL_pH_cn_67_n,
				Cmd.ESCCmd.GS_leftbracket_k_pL_pH_cn_69_n,
				Cmd.ESCCmd.GS_leftbracket_k_pL_pH_cn_80_m__d1dk, bCodeData,
				Cmd.ESCCmd.GS_leftbracket_k_pL_pH_cn_fn_m });
		IO.Write(data, 0, data.length);

	}

	public static void POS_SetKey(byte[] key) {
		byte[] data = Cmd.ESCCmd.DES_SETKEY;
		for (int i = 0; i < key.length; i++) {
			data[i + 5] = key[i];
		}
		// ����DES��Կ����ӡ�����᷵�أ���Ҫ������������
		IO.Write(data, 0, data.length);
	}

	public static boolean POS_CheckKey(byte[] key, byte[] random) {
		boolean result = false;
		final int HeaderSize = 5;
		byte[] recHeader = new byte[HeaderSize];
		byte[] recData = null;
		int rec = 0;
		int recDataLen = 0;
		byte[] randomlen = new byte[2];
		randomlen[0] = (byte) (random.length & 0xff);
		randomlen[1] = (byte) ((random.length >> 8) & 0xff);
		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.ESCCmd.DES_ENCRYPT, randomlen, random });
		IO.Write(data, 0, data.length);
		rec = IO.Read(recHeader, 0, HeaderSize, 1000);
		if (rec != HeaderSize)
			return false;
		recDataLen = (recHeader[3] & 0xff) + ((recHeader[4] << 8) & 0xff);
		recData = new byte[recDataLen];
		rec = IO.Read(recData, 0, recDataLen, 1000);
		if (rec != recDataLen)
			return false;

		byte[] encrypted = recData;
		byte[] decrypted = new byte[encrypted.length + 1];
		/**
		 * �����ݽ��н���
		 */
		DES2 des2 = new DES2();
		// ��ʼ����Կ
		des2.yxyDES2_InitializeKey(key);
		des2.yxyDES2_DecryptAnyLength(encrypted, decrypted, encrypted.length);
		result = DataUtils.bytesEquals(random, 0, decrypted, 0, random.length);
		if (!result) {
			Log.v(TAG + " random", DataUtils.bytesToStr(random));
			Log.v(TAG + " decryp", DataUtils.bytesToStr(decrypted));
		}
		return result;
	}

	/**
	 * ��λ��ӡ��
	 */
	public static void POS_Reset() {
		byte[] data = Cmd.ESCCmd.ESC_ALT;
		IO.Write(data, 0, data.length);
	}

	/**
	 * �����ƶ���λ
	 * 
	 * @param nHorizontalMU
	 * @param nVerticalMU
	 */
	public static void POS_SetMotionUnit(int nHorizontalMU, int nVerticalMU) {
		if (nHorizontalMU < 0 || nHorizontalMU > 255 || nVerticalMU < 0
				|| nVerticalMU > 255)
			return;

		byte[] data = Cmd.ESCCmd.GS_P_x_y;
		data[2] = (byte) nHorizontalMU;
		data[3] = (byte) nVerticalMU;
		IO.Write(data, 0, data.length);
	}

	/**
	 * �����ַ����ʹ���ҳ
	 * 
	 * @param nCharSet
	 * @param nCodePage
	 */
	public static void POS_SetCharSetAndCodePage(int nCharSet, int nCodePage) {
		if (nCharSet < 0 | nCharSet > 15 | nCodePage < 0 | nCodePage > 19
				| (nCodePage > 10 & nCodePage < 16))
			return;

		Cmd.ESCCmd.ESC_R_n[2] = (byte) nCharSet;
		Cmd.ESCCmd.ESC_t_n[2] = (byte) nCodePage;
		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				ESCCmd.ESC_R_n, ESCCmd.ESC_t_n });
		IO.Write(data, 0, data.length);
	}

	/**
	 * �����ַ��Ҽ��
	 * 
	 * @param nDistance
	 */
	public static void POS_SetRightSpacing(int nDistance) {
		if (nDistance < 0 | nDistance > 255)
			return;

		Cmd.ESCCmd.ESC_SP_n[2] = (byte) nDistance;
		byte[] data = Cmd.ESCCmd.ESC_SP_n;
		IO.Write(data, 0, data.length);
	}

	/**
	 * ���ô�ӡ������
	 * 
	 * @param nWidth
	 */
	public static void POS_S_SetAreaWidth(int nWidth) {
		if (nWidth < 0 | nWidth > 65535)
			return;

		byte nL = (byte) (nWidth % 0x100);
		byte nH = (byte) (nWidth / 0x100);
		Cmd.ESCCmd.GS_W_nL_nH[2] = nL;
		Cmd.ESCCmd.GS_W_nL_nH[3] = nH;
		byte[] data = Cmd.ESCCmd.GS_W_nL_nH;
		IO.Write(data, 0, data.length);
	}

	public static void POS_FillZero(int nCount) {
		byte[] data = new byte[nCount];
		IO.Write(data, 0, data.length);
	}

	/**
	 * ʹ��1D 72 01��������ȡ��ӡ��״̬��
	 * 
	 * @param precbuf
	 *            ����Ϊ1���ֽ����飬�洢���ص�״̬��
	 * @param timeout
	 * @return
	 */
	public static boolean POS_QueryStatus(byte precbuf[], int timeout) {

		int retry;
		byte pcmdbuf[] = { 0x1D, 0x72, 0x01 };

		retry = 3;
		while (retry > 0) {
			retry--;

			/**
			 * ���ݽ��չ���
			 */
			IO.ClrRec();
			IO.Write(pcmdbuf, 0, pcmdbuf.length);
			if(IO.Read(precbuf, 0, 1, timeout) == 1)
				return true;
		}

		return false;
	}

}

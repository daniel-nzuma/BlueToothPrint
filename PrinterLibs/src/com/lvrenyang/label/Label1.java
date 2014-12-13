package com.lvrenyang.label;

import com.lvrenyang.pos.IO;
import com.lvrenyang.utils.DataUtils;

public class Label1 {

	
	
	/**
	 * ����: ָʾһ�� Page ҳ��Ŀ�ʼ�������� Page ҳ�Ĵ�С���ο��������ҳ����ת�Ƕȡ�
	 * @param startx ҳ����ʼ��x����
	 * @param starty ҳ����ʼ��y����
	 * @param width ҳ��ҳ�� startx + width �ķ�ΧΪ[1,384]����дSDK��ʱ�򣬸ô�ӡ��һ�еĴ�ӡ����Ϊ384�㡣����㲻ȷ��ÿ�д�ӡ��������ο���ӡ������顣һ����˵��384,576,832�����ֹ��
	 * @param height ҳ��ҳ�� starty + height �ķ�Χ[1,936]����дSDK��ʱ��������936���������ֵ����ȷ������ʹ�ӡ������Դ�йء�������ˣ�Ҳ�������ҳ�����ù��󡣽���ҳ���ҳ�����úͱ�ǩֽƥ�伴�ɡ�
	 * @param rotate ҳ����ת�� rotate��ȡֵ��ΧΪ{0,1}��Ϊ0��ҳ�治��ת��ӡ��Ϊ1��ҳ����ת90�ȴ�ӡ��
	 */
	public static void PageBegin(int startx, int starty, int width, int height, int rotate)
	{
		byte[] data = new byte[12];

		data[0] = 0x1A;
		data[1] = 0x5B;
		data[2] = 0x01;
		
		data[3] = (byte) (startx & 0xFF);
		data[4] = (byte) ((startx >> 8) & 0xFF);
		data[5] = (byte) (starty & 0xFF);
		data[6] = (byte) ((starty >> 8) & 0xFF);

		data[7] = (byte) (width & 0xFF);
		data[8] = (byte) ((width >> 8) & 0xFF);
		data[9] = (byte) (height & 0xFF);
		data[10] = (byte) ((height >> 8) & 0xFF);

		data[11] = (byte) (rotate & 0xFF);

		IO.Write(data, 0, data.length);
		
	}
	
	/**
	 * ����: ֻ��һ��Pageҳ��Ľ�����
	 */
	public static void PageEnd()
	{
		byte[] data = new byte[] { 0x1A, 0x5D, 0x00 };

		IO.Write(data, 0, data.length);
	}

	/**
	* ����: �� Page ҳ�ϵ����ݴ�ӡ����ǩֽ�ϡ�
	* num: ��ӡ�Ĵ�����1-255��
	*/
	public static void PagePrint(int num)
	{
		byte[] data = new byte[]{0x1A, 0x4F, 0x01, 0x01};

		data[3] = (byte) (num & 0xFF);
		
		IO.Write(data, 0, data.length);
	}

	/**
	* ����: �� Page ҳ����ָ��λ�û����ı���ֻ��һ��չ������ӡ������ռ�õ����������ֵ��
	* startx: �����ı���ʼλ�� x ���꣬ȡֵ��Χ��[0, Page_Width-1]
	* starty: �����ı���ʼλ�� y ���꣬ȡֵ��Χ��[0, Page_Height-1]
	* font: ѡ�����壬��Чֵ��ΧΪ{16, 24, 32, 48, 64, 80, 96}
	* style: �ַ����
	*			����λ	����
	*			0		�Ӵֱ�־λ���� 1 ����Ӵ֣����������岻�Ӵ֡�
	*			1		�»��߱�־λ���� 1 �ı����»��ߣ����������»��ߡ�
	*			2		���ױ�־λ���� 1 �ı�����(�ڵװ���)�����㲻���ס�
	*			3		ɾ���߱�־λ���� 1 �ı���ɾ���ߣ���������ɾ���ߡ�
	*			[5,4]	��ת��־λ��00 ��ת 0�� �� 01 ��ת 90�㣻 10 ��ת 180�㣻 11 ��ת 270�㣻
	*			[11,8]	�����ȷŴ�����
	*			[15,12]	����߶ȷŴ�����
	* str: ��00��β���ַ���������
	*/
	public static void DrawPlainText(int startx, int starty, int font, int style, byte[] str)
	{
		int datalen = 11 + str.length + 1;
		byte[] data = new byte[datalen];
		
		data[0] = 0x1A;
		data[1] = 0x54;
		data[2] = 0x01;

		data[3] = (byte) (startx & 0xFF);
		data[4] = (byte) ((startx >> 8) & 0xFF);
		data[5] = (byte) (starty & 0xFF);
		data[6] = (byte) ((starty >> 8) & 0xFF);

		data[7] = (byte) (font & 0xFF);
		data[8] = (byte) ((font >> 8) & 0xFF);
		data[9] = (byte) (style & 0xFF);
		data[10] = (byte) ((style >> 8) & 0xFF);

		DataUtils.copyBytes(str, 0, data, 11, str.length);
		data[datalen - 1] = 0;

		IO.Write(data, 0, data.length);
	}

	/**
	* ����: �� Page ҳָ����������һ��ֱ�߶Ρ�
	* startx: ֱ�߶���ʼ�� x ����ֵ��ȡֵ��Χ��[0, Page_Width-1]��
	* starty: ֱ�߶���ʼ�� y ����ֵ��ȡֵ��Χ��[0��Page_Height-1]��
	* endx: ֱ�߶���ֹ�� x ����ֵ��ȡֵ��Χ��[0, Page_Width-1]��
	* endy: ֱ�߶���ֹ�� y ����ֵ��ȡֵ��Χ��[0,Page_Height-1]��
	* width: ֱ�߶��߿�ȡֵ��Χ��[1��Page_Height-1]��
	* color: ֱ�߶���ɫ��ȡֵ��Χ��{0, 1}���� Color Ϊ 1 ʱ���߶�Ϊ��ɫ���� Color Ϊ 0 ʱ���߶�Ϊ��ɫ��
	*/
	public static void DrawLine(int startx, int starty, int endx, int endy, int width, int color)
	{
		byte[] data = new byte[14];

		data[0] = 0x1A;
		data[1] = 0x5C;
		data[2] = 0x01;
		
		data[3] = (byte) (startx & 0xFF);
		data[4] = (byte) ((startx >> 8) & 0xFF);
		data[5] = (byte) (starty & 0xFF);
		data[6] = (byte) ((starty >> 8) & 0xFF);

		data[7] = (byte) (endx & 0xFF);
		data[8] = (byte) ((endx >> 8) & 0xFF);
		data[9] = (byte) (endy & 0xFF);
		data[10] = (byte) ((endy >> 8) & 0xFF);

		data[11] = (byte) (width & 0xFF);
		data[12] = (byte) ((width >> 8) & 0xFF);

		data[13] = (byte) (color & 0xFF);

		IO.Write(data, 0, data.length);
	}

	/**
	* ����: �� Page ҳָ��λ�û���ָ����С�ľ��ο�
	* left: ���ο����Ͻ� x ����ֵ��ȡֵ��Χ��[0, Page_Width-1]��
	* top: ���ο����Ͻ� y ����ֵ��ȡֵ��Χ��[0, Page_Height-1]��
	* right: ���ο����½� x ����ֵ��ȡֵ��Χ��[0, Page_Width-1]��
	* bottom: ���ο����½� y ����ֵ��ȡֵ��Χ��[0, Page_Height-1]��
	* boardwidth: ���ο��߿�
	* bordercolor: ���ο�����ɫ����ֱ��Χ{0��1}���� Color = 1 ʱ�����ƺ�ɫ���ο�Color = 0 ʱ�����ư�ɫ���ο�
	*/
	public static void DrawBox(int left, int top, int right, int bottom, int borderwidth, int bordercolor)
	{
		byte[] data = new byte[14];

		data[0] = 0x1A;
		data[1] = 0x26;
		data[2] = 0x01;
		
		data[3] = (byte) (left & 0xFF);
		data[4] = (byte) ((left >> 8) & 0xFF);
		data[5] = (byte) (top & 0xFF);
		data[6] = (byte) ((top >> 8) & 0xFF);

		data[7] = (byte) (right & 0xFF);
		data[8] = (byte) ((right >> 8) & 0xFF);
		data[9] = (byte) (bottom & 0xFF);
		data[10] = (byte) ((bottom >> 8) & 0xFF);

		data[11] = (byte) (borderwidth & 0xFF);
		data[12] = (byte) ((borderwidth >> 8) & 0xFF);

		data[13] = (byte) (bordercolor & 0xFF);

		IO.Write(data, 0, data.length);
	}

	/**
	* ����: �� Page ҳָ��λ�û��ƾ��ο顣
	* left: ���ο����Ͻ� x ����ֵ��ȡֵ��Χ��[0, Page_Width-1]��
	* top: ���ο����Ͻ� y ����ֵ��ȡֵ��Χ��[0, Page_Height-1]��
	* right: ���ο����½� x ����ֵ��ȡֵ��Χ��[0, Page_Width-1]��
	* bottom: ���ο����½� y ����ֵ��ȡֵ��Χ��[0, Page_Height-1]��
	* color: ���ο���ɫ��ȡֵ��Χ��{0, 1}���� Color Ϊ 1 ʱ�����ο�Ϊ��ɫ���� Color Ϊ 0ʱ�����ο�Ϊ��ɫ��
	*/
	public static void DrawRectangel(int left, int top, int right, int bottom, int color)
	{
		byte[] data = new byte[12];

		data[0] = 0x1A;
		data[1] = 0x2A;
		data[2] = 0x00;
		
		data[3] = (byte) (left & 0xFF);
		data[4] = (byte) ((left >> 8) & 0xFF);
		data[5] = (byte) (top & 0xFF);
		data[6] = (byte) ((top >> 8) & 0xFF);

		data[7] = (byte) (right & 0xFF);
		data[8] = (byte) ((right >> 8) & 0xFF);
		data[9] = (byte) (bottom & 0xFF);
		data[10] = (byte) ((bottom >> 8) & 0xFF);

		data[11] = (byte) (color & 0xFF);

		IO.Write(data, 0, data.length);
	}

	/**
	* ����: �� Page ҳָ��λ�û���һά���롣
	* startx: �������Ͻ� x ����ֵ��ȡֵ��Χ��[0, Page_Width-1]��
	* starty: �������Ͻ� y ����ֵ��ȡֵ��Χ��[0, Page_Height-1]��
	* type: ��ʶ�������ͣ�ȡֵ��Χ��[0, 29]����ֵ�������£�
	*		type	����	����	����ֵ��Χ��ʮ���ƣ�
	0 UPC-A     11          48-57
	1 UPC-E     6           48-57
	2 EAN13     12          48-57
	3 EAN8      7           48-57
	4 CODE39     1-         48-57,65-90,32,36,37,43,45,46,47
	5 I25        1-ż��     48-57
	6 CODABAR    1-         48-57,65-68,36,43,45,46,47,58
	7 CODE93     1-255      0-127
	8 CODE128    2-255      0-127
	9 CODE11
	10 MSI
	11 "128M",     // ���Ը��������л�����ģʽ-> !096 - !105
	12 "EAN128",   // �Զ��л�����ģʽ
	13 "25C",      // 25C Check use mod 10-> ��������ǰ�油0�� 10�ı���-[(����λ������֮��<��������)+(ż��λ����֮��)*3]
	14 "39C",      // 39�a�ęz��a��횴��䡸�z��a����ֵ���ձ��������ʾ�������������ֵ�ۼ����ٳ���43���õ����N���ٲ�������ľ��a��Ԫ������z��a��Ԫ��
	15 "39",       // Full ASCII 39 Code, �����ַ��������ɱ�ʾ��������ʾ, 39C ͬ���ǰ���Full ASCII, ע���խ�ȴ���
	16 "EAN13+2",  // �������������� 7-12 ��λ����ʼΪ 1011 ���Ϊ 01 ��(_0*10+_1) Mod 4-> 0--AA 1--AB 2--BA 3--BB
	17 "EAN13+5",  // �����벿��ͬ�ϣ�ģʽ((_0+_2+_4)*3+(_1+_3)*9) mod 10 ->"bbaaa", "babaa", "baaba", "baaab", "abbaa", "aabba", "aaabb", "ababa", "abaab", "aabab
	18 "EAN8+2",   // ͬ EAN13+2
	19 "EAN8+5",   // ͬ EAN13+5
	20 "POST",     // ������˵�����Ǹߵ����룬���ǿ�խ����
	21 "UPCA+2",   // ������� EAN
	22 "UPCA+5",   // ������� EAN
	23 "UPCE+2",   // ������� EAN
	24 "UPCE+5",   // ������� EAN
	25 "CPOST",    // ���Բ���ӡ������
	26 "MSIC",     // ���������Ϊ�����ټ���һ�μ����
	27 "PLESSEY",  // ���Բ���ӡ������
	28 "ITF14",    // 25C ���֣� ��һ����ǰ��0����������ʱ��۳����һ�������������Ϊ��β��
	29 "EAN14"
	* height: ��������߶ȡ�
	* uniwidth: �����������ȡֵ��Χ��[1, 4]����ֵ�������£�
	*		Widthȡֵ	�༶���뵥λ��ȣ�mm��	����������խ�������	������������������
	1			0.125					0.125					0.25
	2			0.25					0.25					0.50
	3			0.375					0.375					0.75
	4			0.50					0.50					1.0
	* rotate: ��ʾ������ת�Ƕȡ�ȡֵ��Χ��[0, 3]����ֵ�������£�
	*		Rotateȡֵ	����
	0			���벻��ת���ơ�
	1			������ת 90����ơ�
	2			������ת 180����ơ�
	3			������ת 270����ơ�
	* str: �� 0x00 ��β���ı��ַ���������
	*/
	public static void DrawBarcode(int startx, int starty, int type, int height, int unitwidth, int rotate, byte[] str)
	{
		int datalen = 11 + str.length + 1;
		byte[] data = new byte[datalen];

		data[0] = 0x1A;
		data[1] = 0x30;
		data[2] = 0x00;

		data[3] = (byte) (startx & 0xFF);
		data[4] = (byte) ((startx >> 8) & 0xFF);
		data[5] = (byte) (starty & 0xFF);
		data[6] = (byte) ((starty >> 8) & 0xFF);

		data[7] = (byte) (type & 0xFF);
		data[8] = (byte) (height & 0xFF);
		data[9] = (byte) (unitwidth & 0xFF);
		data[10] = (byte) (rotate & 0xFF);

		DataUtils.copyBytes(str, 0, data, 11, str.length);
		data[datalen - 1] = 0;

		IO.Write(data, 0, data.length);
	}

	/**
	* ����: �� Page ҳָ��λ�û��� QRCode �롣
	* startx: QRCode �����Ͻ� x ����ֵ��ȡֵ��Χ��[0��Page_Width-1]��
	* starty: QRCode �����Ͻ� y ����ֵ��ȡֵ��Χ��[0, Page_Height-1]��
	* version: ָ���ַ��汾��ȡֵ��Χ��[0,20]���� version Ϊ 0 ʱ����ӡ�������ַ��������Զ�����汾�š�
	* ecc: ָ������ȼ���ȡֵ��Χ��[1, 4]����ֵ�������£�
	*		ECC	����ȼ�
	1	L��7%���;������ݶࡣ
	2	M��15%���о���
	3	Q���Ż�����
	4	H��30%����߾��������١�
	* unitwidth: QRCode ����飬ȡֵ��Χ��[1, 4]����ֵ������һά����ָ���������UniWidth��ͬ��
	* rotate: QRCode ����ת�Ƕȣ�ȡֵ��Χ��[0, 3]����ֵ������һά����ָ���������Rotate ��ͬ��
	* str: �� 0x00 ��ֹ�� QRCode �ı��ַ���������
	*/
	public static void DrawQRCode(int startx, int starty, int version, int ecc, int unitwidth, int rotate, byte[] str)
	{
		int datalen = 11 + str.length + 1;
		byte[] data = new byte[datalen];

		data[0] = 0x1A;
		data[1] = 0x31;
		data[2] = 0x00;

		data[3] = (byte) (version & 0xFF);
		data[4] = (byte) (ecc & 0xFF);

		data[5] = (byte) (startx & 0xFF);
		data[6] = (byte) ((startx >> 8) & 0xFF);
		data[7] = (byte) (starty & 0xFF);
		data[8] = (byte) ((starty >> 8) & 0xFF);

		data[9] = (byte) (unitwidth & 0xFF);
		data[10] = (byte) (rotate & 0xFF);

		DataUtils.copyBytes(str, 0, data, 11, str.length);
		data[datalen - 1] = 0;

		IO.Write(data, 0, data.length);
	}

	/**
	* ����:  �� Page ҳָ��λ�û��� PDF417 ���� ��
	* startx: PDF417 �����Ͻ� x ����ֵ��ȡֵ��Χ��[0��Page_Width-1]��
	* starty: PDF417 �����Ͻ� y ����ֵ��ȡֵ��Χ��[0, Page_Height-1]��
	* colnum: ColNum Ϊ����������ÿ�����ɶ������֡�һ������Ϊ 17*UnitWidth ���㡣�����ɴ�ӡ���Զ�������������Χ�޶�Ϊ 3~90��ColNum ��ȡֵ��Χ��[1,30]��
	* lwratio: ��߱ȡ�ȡֵ��Χ��[3,5]��
	* ecc: ����ȼ���ȡֵ��Χ��[0. 8]��
	*		eccȡֵ	��������	�ɴ����������ֽڣ�
	0		2			1108
	1		4			1106
	2		8			1101
	3		16			1092
	4		32			1072
	5		64			1024
	6		128			957
	7		256			804
	8		512			496
	* unitwidth: PDF417 ����飬ȡֵ��Χ��[1, 3]����ֵ������һά����ָ��������� UniWidth ��ͬ��
	* rotate: PDF417 ����ת�Ƕȣ�ȡֵ��Χ��[0, 3]����ֵ������һά����ָ��������� Rotate ��ͬ��
	* str: �� 0x00 ��ֹ�� PDF417 �ı��ַ���������
	*/
	public static void DrawPDF417(int startx, int starty, int colnum, int lwratio, int ecc, int unitwidth, int rotate, byte[] str)
	{
		int datalen = 12 + str.length + 1;
		byte[] data = new byte[datalen];

		data[0] = 0x1A;
		data[1] = 0x31;
		data[2] = 0x01;

		data[3] = (byte) (colnum & 0xFF);
		data[4] = (byte) (ecc & 0xFF);
		data[5] = (byte) (lwratio & 0xFF);

		data[6] = (byte) (startx & 0xFF);
		data[7] = (byte) ((startx >> 8) & 0xFF);
		data[8] = (byte) (starty & 0xFF);
		data[9] = (byte) ((starty >> 8) & 0xFF);

		data[10] = (byte) (unitwidth & 0xFF);
		data[11] = (byte) (rotate & 0xFF);

		DataUtils.copyBytes(str, 0, data, 12, str.length);
		data[datalen - 1] = 0;

		IO.Write(data, 0, data.length);
	}

	/**
	* ����: �� Page ҳָ��λ�û���λͼ��
	* startx: λͼ���Ͻ� x ����ֵ��ȡֵ��Χ��[0, Page_Width]��
	* starty: λͼ���Ͻ� y ����ֵ��ȡֵ��Χ��[0, Page_Height]��
	* width: λͼ�����ؿ�ȡ�
	* height: λͼ�����ظ߶ȡ�
	* style: λͼ��ӡ��Ч����λ�������£�
	*		λ		����
	0		���ױ�־λ���� 1 λͼ���״�ӡ������������ӡ��
	[2:1]	��ת��־λ�� 00 ��ת 0�� �� 01 ��ת 90�㣻 10 ��ת 180�㣻 11 ��ת 270��
	[7:3]	������
	[11:8]	λͼ��ȷŴ�����
	[15:16]	λͼ�߶ȷŴ�����
	* pdata: λͼ�ĵ������ݡ�
	*/
	public static void DrawBitmap(int startx, int starty, int width, int height, int style, byte[] pdata)
	{
		int datalen = 13 + width * height / 8;
		byte[] data = new byte[datalen];

		data[0] = 0x1A;
		data[1] = 0x21;
		data[2] = 0x01;

		data[3] = (byte) (startx & 0xFF);
		data[4] = (byte) ((startx >> 8) & 0xFF);
		data[5] = (byte) (starty & 0xFF);
		data[6] = (byte) ((starty >> 8) & 0xFF);

		data[7] = (byte) (width & 0xFF);
		data[8] = (byte) ((width >> 8) & 0xFF);
		data[9] = (byte) (height & 0xFF);
		data[10] = (byte) ((height >> 8) & 0xFF);

		data[11] = (byte) (style & 0xFF);
		data[12] = (byte) ((style >> 8) & 0xFF);

		DataUtils.copyBytes(pdata, 0, data, 13, pdata.length);

		IO.Write(data, 0, data.length);
	}
}

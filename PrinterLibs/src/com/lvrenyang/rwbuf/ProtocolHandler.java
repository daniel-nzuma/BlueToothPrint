package com.lvrenyang.rwbuf;

public class ProtocolHandler {

	public int Count;
	public byte Buffer[];
	int MaxSize;
	
	public int ProtoHeaderOut = 0x03FF; //
	public int ProtoHeaderIn = 0x03FE; // 
	public int KcCmd,KcPara;
	
	public ProtocolHandler(int MAX_SIZE)
	{
		Count = KcCmd = KcPara = 0;
		MaxSize = MAX_SIZE;
		Buffer = new byte[MaxSize];
	}
	
	/* ������ȫ�����ʱ�򣬲Ż�����KcCmd��KcPara������true��ʾ�ɹ�����һ��������򷵻�false */
	public void HandleKcUartChar(byte ch)
	{
	    if (Count == 0)
	    {   
	    	// Check is the start byte OK
	        if (ch == (byte)(ProtoHeaderIn >> 8))
	        {
	            Buffer[0] = (byte)(ProtoHeaderIn >> 8);
	            Count = 1;
	        }
	    }
	    else
	    {   // Start to receive
	        if (Count >= MaxSize)
	        {   // Package is too large, which is invalid
	            Count = 0;
	        }
	        else
	        {
	            Buffer[Count++] = ch;
	        }
	        if (Buffer[1] != (byte)(ProtoHeaderIn))
	            Count = 0;
	        if (Count >= 12)
	        {   // package received OK?
	            int len;
	            len = Buffer[8]+Buffer[9]*0x100+12;
	            
	            if (Count >= len)
	            {   // package is ready?
	                // package is valid
                    KcCmd = Buffer[2] | (Buffer[3] << 8);
                    KcPara = Buffer[4] | (Buffer[5] << 8)
                                | (Buffer[6] << 16) | (Buffer[7] << 24);
	                Count = 0;
	            }
	        }
	    }
	    
	}
}

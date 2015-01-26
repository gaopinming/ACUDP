package AC.Actions;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import AC.Utils.WR;

public class Req_Quit {
	public Req_Quit(byte[] Req_Data_True, String ip, int port, byte[] Req_Data) {
		System.out.println("收到" + ip + ":" + port + ":" + "发来的下线请求报文："
				+ WR.Getbyte2HexString(Req_Data_True));
		/*
		 * 创建随机数ReqID byte[]
		 */
		byte[] ReqID=new byte[2];
		int ReqID_int = (int) (1 + Math.random() * 32767);
		for (int i = 0; i < 2; i++) {
			int offset = (ReqID.length - 1 - i) * 8;
			ReqID[i] = (byte) ((ReqID_int >>> offset) & 0xff);
		}
		System.out.println("生成的ReqID内容："+ReqID_int);
		System.out.println("验证ReqID内容："+ ((ReqID[0] << 8)+(ReqID[1] & 0xFF)));
		
		Req_Data[6]=ReqID[0];
		Req_Data[7]=ReqID[1];
		short typet=6;
		Req_Data[1]=(byte)typet;
		
		short ErrCodet=0;	//下线成功
		Req_Data[14]=(byte)ErrCodet;
//			short ErrCodet=1;	//下线被拒绝
//			Req_Data[14]=(byte)ErrCodet;
//			short ErrCodet=2;	//下线出错
//			Req_Data[14]=(byte)ErrCodet;
		
		//生成下线请求响应报文
		byte[] Ack_Data_Quit=new byte[16];
		for(int l=0;l<Ack_Data_Quit.length;l++){
			Ack_Data_Quit[l]=Req_Data[l];
		}
		
		System.out.println("准备发送下线回复报文给：" + ip + ":" + port + ":"
				+ WR.Getbyte2HexString(Ack_Data_Quit));

		DatagramSocket scoket_Quie_ACK;
		try {
			scoket_Quie_ACK = new DatagramSocket();
			DatagramPacket data = new DatagramPacket(Ack_Data_Quit, Ack_Data_Quit.length,
					InetAddress.getByName(ip), port);
			scoket_Quie_ACK.send(data);
			scoket_Quie_ACK.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WR.space();
	}
}

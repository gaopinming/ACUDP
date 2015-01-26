package AC.Actions;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import AC.Utils.WR;
import AC.Utils.Make_Challenge;

public class Req_Challenge {

	public Req_Challenge(byte[] Req_Data_True, String ip, int port, byte[] Req_Data) {
		System.out.println("收到" + ip + ":" + port + ":"
				+ "发来的Challenge请求报文：" + WR.Getbyte2HexString(Req_Data_True));
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
		
		short typet=2;
		Req_Data[1]=(byte)typet;
		
		short ErrCodet=0;	//请求Challenge成功
		Req_Data[14]=(byte)ErrCodet;
//			short ErrCodet=1;	//请求Challenge被拒绝
//			Req_Data[14]=(byte)ErrCodet;
//			short ErrCodet=2;	//此链接已建立
//			Req_Data[14]=(byte)ErrCodet;
//			short ErrCodet=3;	//有一个用户正在认证过程中，请稍后再试
//			Req_Data[14]=(byte)ErrCodet;
//			short ErrCodet=4;	//此用户请求Challenge失败（发生错误）
//			Req_Data[14]=(byte)ErrCodet;
		
		Req_Data[6]=ReqID[0];
		Req_Data[7]=ReqID[1];
		
		short atrnumt=1;
		Req_Data[15]=(byte)atrnumt;
		short atrtypt=3;
		Req_Data[16]=(byte)atrtypt;
		int atrleg=18;
		Req_Data[17]=(byte)((atrleg)&0xff);
		
		//构建portal协议中的Challenge包
		byte[] Challenge=new byte[16];
		//生成challenge
		String challengeStr=Make_Challenge.getChallenge();
		try {
			Challenge=challengeStr.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("生成的challenge内容："+challengeStr);
		System.out.println("验证challenge内容："+new String(Challenge));
		
		for(int i=0;i<Challenge.length;i++){
			Req_Data[18+i]=Challenge[i];
		}

		//生成challenge响应报文
		byte[] Ack_Data_challenge=new byte[34];
		for(int l=0;l<Ack_Data_challenge.length;l++){
			Ack_Data_challenge[l]=Req_Data[l];
		}
		System.out.println("准备发送Challenge回复报文给：" + ip + ":" + port + ":"
				+ WR.Getbyte2HexString(Ack_Data_challenge));
		//发送challenge响应报文
		DatagramSocket scoket_Challenge_Ack;
		try {
			scoket_Challenge_Ack = new DatagramSocket();
			DatagramPacket data = new DatagramPacket(Ack_Data_challenge, Ack_Data_challenge.length,
					InetAddress.getByName(ip), port);
			scoket_Challenge_Ack.send(data);
			scoket_Challenge_Ack.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WR.space();
	}
}

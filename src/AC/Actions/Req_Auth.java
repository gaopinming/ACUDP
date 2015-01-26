package AC.Actions;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import AC.Utils.WR;

public class Req_Auth {

	public Req_Auth(byte[] Req_Data_True, String ip, int port, byte[] Req_Data) {
		System.out.println("收到" + ip + ":" + port + ":" + "发来的认证请求报文："
				+ WR.Getbyte2HexString(Req_Data_True));
		
		short typet=4;
		Req_Data[1]=(byte)typet;
		
		short ErrCodet=0;	//此用户认证成功
		Req_Data[14]=(byte)ErrCodet;
//			short ErrCodet=1;	//此用户认证请求被拒绝
//			Req_Data[14]=(byte)ErrCodet;
//			short ErrCodet=2;	//此链接已建立
//			Req_Data[14]=(byte)ErrCodet;
//			short ErrCodet=3;	//有一个用户正在认证过程中，请稍后再试
//			Req_Data[14]=(byte)ErrCodet;
//			short ErrCodet=4;	//此用户认证失败（发生错误）
//			Req_Data[14]=(byte)ErrCodet;
		
		short atrnumt=0;
		Req_Data[15]=(byte)atrnumt;
		
		//生成Auth响应报文
		byte[] Ack_Data_Auth=new byte[16];
		for(int l=0;l<Ack_Data_Auth.length;l++){
			Ack_Data_Auth[l]=Req_Data[l];
		}

		System.out.println("准备发送认证回复报文给：" + ip + ":" + port + ":"
				+ WR.Getbyte2HexString(Ack_Data_Auth));

		DatagramSocket scoket_Auth_ACK;
		try {
			scoket_Auth_ACK = new DatagramSocket();
			DatagramPacket data = new DatagramPacket(Ack_Data_Auth, Ack_Data_Auth.length,
					InetAddress.getByName(ip), port);
			scoket_Auth_ACK.send(data);
			scoket_Auth_ACK.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WR.space();
	}

	
}

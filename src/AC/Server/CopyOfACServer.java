package AC.Server;
/*
 * 0-1 ver Ver字段是协议的版本号，长度为 1 字节，Ver = 0x02或01
 * 2-3 type Type字段定义报文的类型，长度为 1 字节
 * 4-5 pap/chap 01/00 Pap/Chap字段定义此用户的认证方式，长度为 1 字节，只对Type值为 0x03 的认证请求报文有意义
 * 6-7 00 Rsv目前为保留字段，长度为 1 字节，在所有报文中值为0 
 * 8-11 Serial NoSerialNo字段为报文的序列号，长度为 2字节，由PortalServer随机生成,Portal Server必须尽量保证不同认证流程的SerialNo在一定时间内不得重复，在同一个认证流程中所有报文的SerialNo相同
 * 12-15 ReqID ReqID字段长度为 2 个字节，由BAS设备随机生成，尽量使得在一定时间内ReqID不重复。
 * 16-23 UserIP	UserIP字段为Portal用户的IP地址，长度为 4 字节，其值由PortalServer根据其获得的IP地址填写
 * 24-27 UserPort UserPort字段目前没有用到，长度为 2 字节，在所有报文中其值为0
 * 28-29 ErrCode  长度为 1字节
 *                	(1)、对于Type值为1、3、7的报文，ErrCode字段无意义，其值为0；
 *				  	(2)、当Type值为 2 时：
 *					ErrCode＝0，表示BAS设备告诉PortalServer请求Challenge成功；
 *					ErrCode＝1，表示BAS设备告诉PortalServer请求Challenge被拒绝； 
 *					ErrCode＝2，表示BAS设备告诉PortalServer此链接已建立；
 *					ErrCode＝3，表示BAS设备告诉PortalServer有一个用户正在认证过程中，请稍后再试；
 *					ErrCode＝4，则表示BAS设备告诉PortalServer此用户请求Challenge失败（发生错误）；
 *					(3)、当Type值为 4 时：
 *					ErrCode＝0，表示BAS设备告诉PortalServer此用户认证成功；
 *					ErrCode＝1，表示BAS设备告诉PortalServer此用户认证请求被拒绝；
 *					ErrCode＝2，表示BAS设备告诉PortalServer此链接已建立； 
 *					ErrCode＝3，表示BAS设备告诉PortalServer有一个用户正在认证过程中，请稍后再试；
 * 					ErrCode＝4 ，表示BAS设备告诉PortalServer此用户认证失败（发生错误）；
 *					(4)、当Type值为 5 时：
 *					ErrCode＝0，表示此报文是PortalServer发给BAS设备的请求下线报文；
 *					ErrCode＝1，表示此报文是在PortalServer没有收到BAS设备发来的对各种请求的响应报文，而定时器时间到（即超时）时由PortalServer发给BAS设备的报文；
 *					(5)、当Type值为 6 时：
 *					ErrCode＝0，表示BAS设备告诉PortalServer此用户下线成功；
 * 					ErrCode＝1，表示BAS设备告诉PortalServer此用户下线被拒绝；
 * 					ErrCode＝2,  表示BAS设备告诉PortalServer此用户下线失败（发生错误）；
 * 30-31 AttrNum 属性个数
 */

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import AC.Utils.WR;
import AC.Utils.Make_Challenge;

public class CopyOfACServer extends Thread {

	DatagramPacket data = null;

	public CopyOfACServer(DatagramPacket data) {

		this.data = data;
	}
	
	public void run() {
		byte[] Req_Data = data.getData();  //构建缓存包大小1024
		byte[] Req_Data_Base=new byte[16];  //构建接收基础包大小16
		for(int l=0;l<Req_Data_Base.length;l++){
			Req_Data_Base[l]=Req_Data[l];
		}
		
		//获取客户端的ip和端口号
		String ip = data.getAddress().getHostAddress();
		int port = data.getPort();
		//构建portal协议中的字段包
		byte[] Ver=new byte[1];
		byte[] Type=new byte[1];
		byte[] Mod=new byte[1];
		byte[] Rsvd=new byte[1];
		byte[] SerialNo=new byte[2];
		byte[] ReqID=new byte[2];
		byte[] UserIP=new byte[4];
		byte[] UserPort=new byte[2];
		byte[] ErrCode=new byte[1];
		byte[] AttrNum=new byte[1];
		
		//给各字段包赋初始值为接收到的包的值
		Ver[0]=Req_Data[0];
		Type[0]=Req_Data[1];
		Mod[0]=Req_Data[2];
		Rsvd[0]=Req_Data[3];
		SerialNo[0]=Req_Data[4];
		SerialNo[1]=Req_Data[5];
		ReqID[0]=Req_Data[6];
		ReqID[1]=Req_Data[7];
		UserIP[0]=Req_Data[8];
		UserIP[1]=Req_Data[9];
		UserIP[2]=Req_Data[10];
		UserIP[3]=Req_Data[11];
		UserPort[0]=Req_Data[12];
		UserPort[1]=Req_Data[13];
		ErrCode[0]=Req_Data[14];
		AttrNum[0]=Req_Data[15];
		
		
		
		String username=null;
		String userpass=null;
		String chappass=null;
		String challengeString=null;
		int pos=16;
		int AN=(int)(AttrNum[0] & 0xFF);
		if(AN>0){
			int num=1;
			while(num<=AN){
				int type=(int)(Req_Data[pos] & 0xFF);
				pos=pos+1;
				int len=((int)(Req_Data[pos] & 0xFF))-2;
				pos=pos+1;
				byte[] buf=new byte[len];
				for(int l=0;l<buf.length;l++){
					buf[l]=Req_Data[pos+l];
				}
				pos=pos+len;
				if(type==1){
					username=new String(buf);
				}else if(type==2){
					userpass=new String(buf);
				}else if(type==3){
					challengeString=new String(buf);
				}else if(type==4){
					chappass=WR.Getbyte2HexString(buf);
				}
				num=num+1;
			}
		}
		
		byte[] Req_Data_True=new byte[pos];  //构建接收基础包大小16
		for(int l=0;l<Req_Data_True.length;l++){
			Req_Data_True[l]=Req_Data[l];
		}
		
		/*
		 * 创建随机数SerialNo byte[]   portal server创建
		 */
//		short SerialNo_int = (short) (1 + Math.random() * 32767);
//		for (int i = 0; i < 2; i++) {
//			int offset = (SerialNo.length - 1 - i) * 8;
//			SerialNo[i] = (byte) ((SerialNo_int >>> offset) & 0xff);
//		}
		
		
		
		
//************************************************************************************************************
		
		
		//如果接收到的报文为 challenge请求报文
		if ((int)(Type[0] & 0xFF)==1){
			
			System.out.println("收到" + ip + ":" + port + ":"
					+ "发来的Challenge请求报文：" + WR.Getbyte2HexString(Req_Data_True));
			/*
			 * 创建随机数ReqID byte[]
			 */
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
		
		//如果接收到的报文为 认证请求报文
		else if ((int)(Type[0] & 0xFF)==3) {
			System.out.println("收到" + ip + ":" + port + ":" + "发来的认证请求报文："
					+ WR.Getbyte2HexString(Req_Data_True));
			System.out.println("用户名： " + username);
			if ((int)(Mod[0] & 0xFF)==0){
				System.out.println("CHAP-Password： " + chappass);
			}else if ((int)(Mod[0] & 0xFF)==1){
				System.out.println("PAP模式明文Password： " + userpass);
			}
			
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
		
		//如果接收到的报文为 下线请求报文
		else if ((int)(Type[0] & 0xFF)==5) {
			System.out.println("收到" + ip + ":" + port + ":" + "发来的下线请求报文："
					+ WR.Getbyte2HexString(Req_Data_True));
			
			if((ErrCode[0] & 0xFF)==1){
				if(((int)(ReqID[0] & 0xFF)==0)&&((int)(ReqID[1] & 0xFF)==0))
				System.out.println("Challenge请求超时回报！！");
				else if((!(((int)(ReqID[0] & 0xFF)==0)&&((int)(ReqID[1] & 0xFF)==0)))&&(int)(AttrNum[0] & 0xFF)==2)
				System.out.println("Auth用户认证请求超时回报！！");
				else
				System.out.println("其他请求超时回报！！");
				
			}
			else{
			/*
			 * 创建随机数ReqID byte[]
			 */
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
			}
			WR.space();

		}
		
		//如果接收到的报文为 认证成功确认报文
		else if ((int)(Type[0] & 0xFF)==7) {
			System.out.println("收到" + ip + ":" + port + ":" + "发来的认证成功确认报文："
					+ WR.Getbyte2HexString(Req_Data_True));
			if((int)(Req_Data_True[15] & 0xFF)==0){
				System.out.println("用户【" + ip + "】登录成功！");
			}
			WR.space();
		}

	
	
	
	
	
	
	}

	public static void openServer() throws Exception {

		@SuppressWarnings("resource")
		DatagramSocket socket = new DatagramSocket(2000);
		System.out.println("UDP服务开启，端口号2000  ！");
		WR.space();
		while (true) {
			byte[] b = new byte[1024];
			DatagramPacket data = new DatagramPacket(b, b.length);
			socket.receive(data);
			new CopyOfACServer(data).start();
		}
	}

}

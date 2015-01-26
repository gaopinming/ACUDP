package test;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import AC.Utils.MD5;
import AC.Utils.Make_Challenge;

public class ChapPasswordTest {
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		

		/*
		 * 创建随机数ReqID byte[]
		 */
		byte[] ReqID=new byte[2];
		int ReqID_int = (int) (1 + Math.random() * 32767);
		ReqID_int=15294;	//测试
		for (int i = 0; i < 2; i++) {
			int offset = (ReqID.length - 1 - i) * 8;
			ReqID[i] = (byte) ((ReqID_int >>> offset) & 0xff);
		}
		System.out.println("生成的ReqID内容："+ReqID_int);
		int chapid=(int)(ReqID[1] & 0xFF);
		System.out.println("通过ReqID取低字节 获取的ChapID内容："+chapid);
			   
		//生成challenge
		byte[] Challenge=new byte[16];
		String challengeStr=Make_Challenge.getChallenge();
		challengeStr="cu7aysxgjg3p84gs";	//测试
		try {
			Challenge=challengeStr.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("生成的challenge内容："+challengeStr);   

		
		//用户明文密码
		String userpassword="iwsiqh025504622";	//测试
		byte[] usp=userpassword.getBytes("UTF-8");
		System.out.println("密码长度："+usp.length);
		
		//初始化chappassword byte[]
		byte[] buf=new byte[1+usp.length+Challenge.length];
		System.out.println("chappassword byte[]长度为："+buf.length);
		
		//给chappassword byte[] 传值      
		/*
		 * Chap_Password的生成：Chap_Password的生成遵循标准的Radious协议中的Chap_Password 生成方法（参见RFC2865）。
		 * 密码加密使用MD5算法，MD5函数的输入为ChapID ＋ Password ＋Challenge 
		 * 其中，ChapID取ReqID的低 8 位，Password的长度不够协议规定的最大长度，其后不需要补零。 
		 */
		buf[0]=ReqID[1];
		
		for(int i=0;i<usp.length;i++){
			buf[1+i]=usp[i];
		}
		
		for(int i=0;i<Challenge.length;i++){
			buf[1+usp.length+i]=Challenge[i];
		}
		
		//生成Chap-Password
		String ChapPassword=new MD5().getMD5(buf);
		System.out.println("Chap-Password为： "+ChapPassword);
		
	}
	
}


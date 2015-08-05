package com.nju.ecg.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.nju.ecg.utils.LogUtil;

public class SocketClient {
	private final String TAG = "SocketClient";
	private String userName;
	private String password;
	private Socket client;
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	private final String IP = "58.216.185.27";
	private final int PORT = 8888;
	private ProgressListener progressListener;
	public SocketClient(String userName, String password, ProgressListener progressListener) {
		this.userName = userName;
		this.password = password;
		this.progressListener = progressListener;
	}
	
	/**
	 * 用户登录, 返回登录结果
	 * @param fileName 文件名
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public int login(String fileName)
	{
		try {
			client = new Socket(IP, PORT);
			InputStream is = client.getInputStream();
			dis = new DataInputStream(is);
			byte[] buffer = new byte[10240];
			int len = dis.read(buffer);
			String hello = new String(buffer, 0, len);
			if ("hello".equals(hello.toLowerCase()))
			{
				dos = new DataOutputStream(client.getOutputStream());
				if (fileName == null)
				{
				    fileName = "";
				}
				dos.write(("delfu[" + userName + "]ff[" + stringMD5(password) + "]ff[" + fileName + "]delfu").getBytes());
				len = dis.read(buffer);
				int result = Integer.parseInt(new String(buffer, 0, len));
				LogUtil.d(TAG, "login result: " + result);
				return result;
			}
		} catch (Exception e) {
			LogUtil.e(TAG, e);
		}
		return -100;
	}
	
	public void send(String filePath) throws IOException
	{
		if (progressListener != null)
		{
			progressListener.onSend(0);
		}
		File file = new File(filePath);
		if (!file.exists())
		{
			throw new FileNotFoundException();
		}
		long fileSize = file.length();
		FileInputStream fis = new FileInputStream(file);
		byte[] buffer = new byte[10240];
		int len = -1;
		int totalSend = 0;
		while ((len = fis.read(buffer)) != -1)
		{
			send(buffer, len);
			totalSend += len;
			if (progressListener != null)
			{
				progressListener.onSend((int)(totalSend * 100 / fileSize));
			}
		}
		if (progressListener != null)
		{
			progressListener.overSend();
		}
	}
	
	public void send(byte[] data, int length) throws IOException
	{
	    dos.write(data, 0 , length);
	}
	
	public void close()
	{
		try {
			if (dis != null)
			{
				dis.close();
			}
			if (dos != null)
			{
				dos.close();
			}
			if (client != null)
			{
				client.close();
			}
		} catch (Exception e) {
			LogUtil.e(TAG, e);
		}
	}
	
	public static String stringMD5(String input) {  
	   try {  
	      // 拿到一个MD5转换器（如果想要SHA1参数换成”SHA1”）  
	      MessageDigest messageDigest =MessageDigest.getInstance("MD5");  
	      // 输入的字符串转换成字节数组  
	      byte[] inputByteArray = input.getBytes();  
	      // inputByteArray是输入字符串转换得到的字节数组  
	      messageDigest.update(inputByteArray);  
	      // 转换并返回结果，也是字节数组，包含16个元素  
	      byte[] resultByteArray = messageDigest.digest();  
	      // 字符数组转换成字符串返回  
	      return byteArrayToHex(resultByteArray);  
	   } catch (NoSuchAlgorithmException e) {  
	      return null;  
	   }  
	}  
	
	public static String byteArrayToHex(byte[] byteArray) {  
	   // 首先初始化一个字符数组，用来存放每个16进制字符  
	   char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9', 'A','B','C','D','E','F' };  
	   // new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））  
	   char[] resultCharArray =new char[byteArray.length * 2];  
	   // 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去  
	   int index = 0;  
	   for (byte b : byteArray) {  
	      resultCharArray[index++] = hexDigits[b>>> 4 & 0xf];  
	      resultCharArray[index++] = hexDigits[b& 0xf];  
	   }  
	   // 字符数组组合成字符串返回  
	   return new String(resultCharArray);  
	}
	
	public interface ProgressListener
	{
		void onSend(int progress);
		void overSend();
	}
}

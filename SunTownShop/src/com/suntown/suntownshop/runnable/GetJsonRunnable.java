package com.suntown.suntownshop.runnable;

import java.io.InputStream;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.suntown.suntownshop.utils.InputStreamUtils;
import com.suntown.suntownshop.utils.XmlParser;
/**
 * �������ݻ�ȡRunnable
 * GET����
 *
 * @author Ǯ��
 * @version 2014��12��20�� ����12:58:39
 *
 */
public class GetJsonRunnable implements Runnable {
	String url;
	int msg_complete;
	int pos = -1;
	//�Ƿ�ΪSOAPЭ�����ݣ�Webservice������������SOAP��װ����Ҫ��ȡ��Ч��Ϣ
	boolean isBySoap = true;
	//������Ϣ,�ڳɹ������ʱ���� 
	String msgPlus = null;
	Handler handler;
	private final static int MSG_ERR_NETWORKERR = -1;

	public GetJsonRunnable(String url, int msg, Handler handler) {
		// TODO Auto-generated constructor stub
		this.url = url;
		this.msg_complete = msg;
		this.handler = handler;
	}
	/**
	 * �и�����Ϣ��Runnable
	 * @param url
	 * @param msg
	 * @param msgPlus ������Ϣ
	 * @param handler
	 */
	public GetJsonRunnable(String url, int msg,String msgPlus, Handler handler) {
		// TODO Auto-generated constructor stub
		this.url = url;
		this.msg_complete = msg;
		this.msgPlus = msgPlus;
		this.handler = handler;
	}
	
	public GetJsonRunnable(String url, int msg, boolean isBySoap,
			Handler handler) {
		// TODO Auto-generated constructor stub
		this.url = url;
		this.msg_complete = msg;
		this.handler = handler;
		this.isBySoap = isBySoap;
	}

	public GetJsonRunnable(String url, int msg, int pos, Handler handler) {
		this.url = url;
		this.msg_complete = msg;
		this.pos = pos;
		this.handler = handler;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			Log.i("fc", "GetJsonRunnable22222");
			InputStream is = InputStreamUtils.getInputStream(url);
			String result;
			if (isBySoap) {
				result = XmlParser.parse(is, "UTF-8", "return");
			} else {
				result = InputStreamUtils.InputStreamTOString(is, "UTF-8");
			}
			if(result.contains("\"RESULT\":\"2\"")){
				 return;
			} 
			
			
			
			System.out.println("result:"+result);
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putString("MSG_JSON", result);
			bundle.putInt("MSG_POS", pos);
			if(msgPlus!=null){
				bundle.putString("MSG_PLUS", msgPlus);
			}
			msg.what = msg_complete;
			msg.setData(bundle);
 			
			handler.sendMessage(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putString("MSG_ERR", e.getMessage());
			if(msgPlus!=null){
				bundle.putString("MSG_PLUS", msgPlus);
			}
			msg.what = MSG_ERR_NETWORKERR;
			msg.setData(bundle);
			handler.sendMessage(msg);
			e.printStackTrace();
		}
	}
}
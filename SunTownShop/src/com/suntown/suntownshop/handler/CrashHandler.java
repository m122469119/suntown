package com.suntown.suntownshop.handler;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;
/**
 * δ�����쳣������
 *
 * @author Ken
 * @version 2015��4��7�� ����12:38:39
 *
 */
public class CrashHandler implements UncaughtExceptionHandler {
	// �����Context����
	private Context mContext;
	// ϵͳĬ�ϵ�UncaughtException������
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	// CrashHandlerʵ��
	private static CrashHandler INSTANCE = new CrashHandler();

	/** ��ֻ֤��һ��CrashHandlerʵ�� */
	private CrashHandler() {
	}

	/** ��ȡCrashHandlerʵ�� ,����ģʽ */
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * ��ʼ��
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		// ��ȡϵͳĬ�ϵ�UncaughtException������
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// ���ø�CrashHandlerΪ�����Ĭ�ϴ�����
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub
		if (!handleException(ex) && mDefaultHandler != null) {  
            //����û�û�д�������ϵͳĬ�ϵ��쳣������������  
            mDefaultHandler.uncaughtException(thread, ex);  
        } else {  
            try {  
                Thread.sleep(3000);  
            } catch (InterruptedException e) {  
                e.printStackTrace(); 
            }  
            //�˳�����  
            android.os.Process.killProcess(android.os.Process.myPid());  
            System.exit(1);  
        }
	}

	/** 
     * �Զ��������,�ռ�������Ϣ ���ʹ��󱨸�Ȳ������ڴ����. 
     *  
     * @param ex 
     * @return true:��������˸��쳣��Ϣ;���򷵻�false. 
     */  
    private boolean handleException(Throwable ex) {  
        if (ex == null) {  
            return false;  
        }  
        //ʹ��Toast����ʾ�쳣��Ϣ  
        new Thread() {  
            @Override  
            public void run() {  
                Looper.prepare();  
                Toast.makeText(mContext, "�ܱ�Ǹ,��������쳣,�����˳�.", Toast.LENGTH_LONG).show();  
                Looper.loop();  
            }  
        }.start();  
 
        return true;  
    }
}
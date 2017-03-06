package com.suntown.suntownshop;

import org.json.JSONObject;

import com.suntown.suntownshop.runnable.GetJsonRunnable;
import com.suntown.suntownshop.utils.FormatValidation;
import com.suntown.suntownshop.utils.SmsContent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
/**
 * �������룬�ӿ�δ��ɣ�������Ҫ���ݽӿ��޸�
 *
 * @author Ǯ��
 * @version 2015��9��21�� ����9:49:35
 *
 */
public class RetrievePasswordActivity extends Activity implements
		OnClickListener, TextWatcher {
	private final static int MSG_GETCHECKCODE = 1;
	private final static int MSG_ERROR = -1;
	private final static String URL_GETCHECKCODE = Constants.DOMAIN_NAME
			+ "axis2/services/sunteslwebservice/checkCodeSend?moblie=";
	private EditText etMobile;
	private EditText etCheckCode;
	private SmsContent smsContent;
	private TextView tvTips;
	private String mMobile;
	private String mCheckCode;
	private Button btnGetCheckCode;
	private Button btnNext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_retrievepassword);
		etMobile = (EditText) findViewById(R.id.et_telphone);
		etMobile.addTextChangedListener(this);
		etCheckCode = (EditText) findViewById(R.id.et_checkcode);
		etCheckCode.addTextChangedListener(this);
		tvTips = (TextView) findViewById(R.id.tv_tips);
		btnGetCheckCode = (Button) findViewById(R.id.btn_checkcode);
		btnGetCheckCode.setOnClickListener(this);
		btnNext = (Button)findViewById(R.id.btn_next);
		btnNext.setOnClickListener(this);
		smsContent = new SmsContent(this, new Handler(), etCheckCode);
	}

	public void close(View v) {
		finish();
	}

	private void getCheckCode() {
		String phone = etMobile.getText().toString();
		if (phone == null || "".equals(phone)) {
			Toast.makeText(this, "�������ֻ�����!", Toast.LENGTH_SHORT).show();
		} else if (!FormatValidation.isMobileNO(phone)) {
			Toast.makeText(this, "�ֻ����벻�Ϸ�!", Toast.LENGTH_SHORT).show();
		} else {

			// ע����ű仯����
			this.getContentResolver().registerContentObserver(
					Uri.parse("content://sms/"), true, smsContent);
			mMobile = phone;
			showProgress(true);
			GetJsonRunnable getJsonRunnable = new GetJsonRunnable(
					URL_GETCHECKCODE + phone, MSG_GETCHECKCODE, handler);
			new Thread(getJsonRunnable).start();
		}
	}

	private ProgressDialog mPDialog;

	public void showProgress(final boolean show) {
		if (show) {
			mPDialog = new ProgressDialog(this);
			// ʵ����
			mPDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// ���ý�������񣬷��ΪԲ�Σ���ת��
			// pDialog.setTitle("Google");
			// ����ProgressDialog ����
			mPDialog.setMessage(getString(R.string.wait_a_minute));
			// ����ProgressDialog ��ʾ��Ϣ
			// pDialog.setIcon(R.drawable.ic_launcher);
			// ����ProgressDialog ����ͼ��
			// mypDialog.setButton();
			// ����ProgressDialog ��һ��Button
			mPDialog.setIndeterminate(false);
			// ����ProgressDialog �Ľ������Ƿ���ȷ
			mPDialog.setCancelable(false);
			// ����ProgressDialog �Ƿ���԰��˻ذ���ȡ��
			mPDialog.show();
		} else {
			if (mPDialog != null && mPDialog.isShowing()) {
				mPDialog.dismiss();
				mPDialog = null;
			}
		}
	}

	private int countdown = 60;

	Runnable runnableTimer = new Runnable() {
		@Override
		public void run() {
			countdown--;
			if (countdown > 0) {
				btnGetCheckCode.setText(countdown + "����ط�");
				handler.postDelayed(this, 1000);
			} else {
				btnGetCheckCode.setText(getString(R.string.getcheckcode));
				btnGetCheckCode.setEnabled(true);
			}
		}
	};

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			showProgress(false);
			Bundle bundle = msg.getData();
			String strMsg;
			JSONObject jsonObj;
			switch (msg.what) {
			case MSG_GETCHECKCODE:
				strMsg = bundle.getString("MSG_JSON");
				try {
					jsonObj = new JSONObject(strMsg);
					int sendState = jsonObj.getInt("RESULT");
					if (sendState == 0) {
						tvTips.setText("�ѷ�����֤�뵽�ֻ�����" + mMobile);
						btnGetCheckCode.setEnabled(false);
						etCheckCode.requestFocus();
						countdown = 60;
						btnGetCheckCode.setText(countdown + "����ط�");
						handler.postDelayed(runnableTimer, 1000);
					} else if (sendState == 1) {
						tvTips.setText("���ֻ�������δע��,��������ȷ���ֻ�����!");
					} else {
						tvTips.setText("��֤�뷢��ʧ�ܣ����Ժ�����!");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Toast.makeText(getApplicationContext(),
							"ERROR:��֤���������:" + e.getMessage(),
							Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}

				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_checkcode:
			getCheckCode();
			break;
		case R.id.btn_next:
			break;
		}
	}

	private void checkButton() {
		mMobile = etMobile.getText().toString();
		mCheckCode = etCheckCode.getText().toString();
		if (FormatValidation.isMobileNO(mMobile) && mCheckCode.length() == 6) {
			btnNext.setEnabled(true);
		} else {
			btnNext.setEnabled(false);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		checkButton();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

}
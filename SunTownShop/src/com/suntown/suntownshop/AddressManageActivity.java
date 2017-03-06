package com.suntown.suntownshop;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.suntown.suntownshop.FragmentPage4.ViewHolder;
import com.suntown.suntownshop.asynctask.PostAsyncTask;
import com.suntown.suntownshop.asynctask.PostAsyncTask.OnCompleteCallback;
import com.suntown.suntownshop.model.Receiver;
import com.suntown.suntownshop.utils.JsonParser;
import com.suntown.suntownshop.utils.XmlParser;
import com.suntown.suntownshop.widget.ConfirmDialog;
import com.suntown.suntownshop.widget.SwipeListView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
/**
 * �ջ���ַ����ҳ��
 *
 * @author Ǯ��
 * @version 2015��4��23�� ����2:36:36
 *
 */
public class AddressManageActivity extends Activity {
	private View loading;
	private View showing;
	private SwipeListView mSwipeListView;
	private int mRightWidth = 0;
	private BaseAdapter adapter;
	private ArrayList<Receiver> list;
	private String mUserId;
	private String mLoginToken;
	private int curIndex;
	private TextView tvTitle;
	private View viewAdd;
	//��ѡ���ַҳ�渴�ã�isSelecting = trueʱΪѡ���ջ���ַ������Ϊ��ַ����
	private boolean isSelecting = false;
	private int selectedId = 0;
	public final static int INTENT_CODE_SELECTING = 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_address_manage);
		loading = findViewById(R.id.loading);
		showing = findViewById(R.id.layout_show);
		tvTitle = (TextView) findViewById(R.id.tv_head_title);
		viewAdd = findViewById(R.id.view_add);
		SharedPreferences mSharedPreferences = getSharedPreferences(
				"suntownshop", 0);
		mUserId = mSharedPreferences.getString("userId", "");
		mLoginToken = mSharedPreferences.getString("m_voucher", "");
		mSwipeListView = (SwipeListView) findViewById(R.id.lv_address);
		mSwipeListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				Receiver receiver = list.get(position);

				modifyReceiver(receiver);

			}
		});
		initAdapter();
		Intent intent = getIntent();
		if (intent.hasExtra("select")) {
			isSelecting = intent.getBooleanExtra("select", false);
			selectedId = intent.getIntExtra("id", 0);
		}
		//�����ѡ���ջ���ַ״̬
		if (isSelecting) {
			tvTitle.setText("ѡ���ջ���ַ");
			//��ֹ����ɾ��
			mSwipeListView.setSwipeable(false);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		initReceiver();
		super.onResume();
	}
	/**
	 * ��ȡ��ַ��Ϣ�ӿڵ�ַ
	 */
	private final static String URL = Constants.DOMAIN_NAME
			+ "axis2/services/sunteslwebservice/getAllAddress";
	/**
	 * ��ʼ���ؼ����������������ȡ��ַ��Ϣ
	 */
	private void initReceiver() {
		showing.setVisibility(View.GONE);
		loading.setVisibility(View.VISIBLE);
		list.clear();
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("memid", mUserId);
		params.put("logintoken", mLoginToken);
		PostAsyncTask postAsyncTask = new PostAsyncTask(URL, callback);
		postAsyncTask.execute(params);
	}
	/**
	 * ��ȡ��ַ��Ϣ��Ļص��ӿ�
	 */
	private OnCompleteCallback callback = new OnCompleteCallback() {

		@Override
		public void onComplete(boolean isOk, String msg) {
			// TODO Auto-generated method stub
			// showProgress(false);
			if (isOk) {
				JSONObject jsonObj;
				try {
					msg = XmlParser.parse(msg, "UTF-8", "return");
					jsonObj = new JSONObject(msg);
					int sendState = jsonObj.getInt("RESULT");
					if (sendState == 0) {
						// ȡ�õ�ַ���ݣ���ʼ����
						JSONArray jsonArray = jsonObj.getJSONArray("RECORD");
						for (int i = 0; i < jsonArray.length(); i++) {
							jsonObj = (JSONObject) jsonArray.opt(i);
							int id = jsonObj.getInt("ID");
							String memid = jsonObj.getString("MEMID");
							String name = jsonObj.getString("RECEIVER");
							String phone = jsonObj.getString("PHONE");
							String address = jsonObj.getString("ADDRESS");
							boolean isDefault = jsonObj.getInt("ISDEFAULT") == 1 ? true
									: false;
							Receiver receiver = new Receiver(id, memid, name,
									phone, address, isDefault);
							if (isDefault) {
								list.add(0, receiver);
							} else {
								list.add(receiver);
							}
						}
					} else {
						list.clear();
					}
					adapter.notifyDataSetChanged();
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "���������ش������Ժ�����...",
							Toast.LENGTH_SHORT).show();

					e.printStackTrace();
				}
			} else {
				Toast.makeText(getApplicationContext(), "���ӳ�ʱ�����Ժ�����...",
						Toast.LENGTH_SHORT).show();
			}
			showing.setVisibility(View.VISIBLE);
			loading.setVisibility(View.GONE);
		}
	};
	/**
	 * ��ʼ����ַ�б�������
	 */
	private void initAdapter() {
		mRightWidth = mSwipeListView.getRightViewWidth();
		list = new ArrayList<Receiver>();
		adapter = new BaseAdapter() {

			@Override
			public View getView(final int position, View convertView,
					ViewGroup parent) {
				// TODO Auto-generated method stub
				ViewHolder holder;
				if (convertView == null) {
					convertView = LayoutInflater.from(
							AddressManageActivity.this).inflate(
							R.layout.address_item, null);
					holder = new ViewHolder();

					holder.item_left = (RelativeLayout) convertView
							.findViewById(R.id.item_left);
					holder.item_right = (RelativeLayout) convertView
							.findViewById(R.id.item_right);
					LinearLayout.LayoutParams lp1 = new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT);
					holder.item_left.setLayoutParams(lp1);
					LinearLayout.LayoutParams lp2 = new LayoutParams(
							mRightWidth, LayoutParams.MATCH_PARENT);
					holder.item_right.setLayoutParams(lp2);
					holder.tvReceiverName = (TextView) convertView
							.findViewById(R.id.tv_receiver_name);
					holder.tvIsDefault = (TextView) convertView
							.findViewById(R.id.tv_isdefault);
					holder.tvReceiverPhone = (TextView) convertView
							.findViewById(R.id.tv_receiver_phone);
					holder.tvReceiverAddress = (TextView) convertView
							.findViewById(R.id.tv_receiver_address);
					holder.viewName = convertView.findViewById(R.id.view_name);
					holder.viewAddress = convertView.findViewById(R.id.view_address);
					holder.viewModify  = convertView.findViewById(R.id.view_modify);
					holder.viewSelect = convertView.findViewById(R.id.view_select);
					holder.cbDefault = (CheckBox)convertView.findViewById(R.id.cb_default);
					holder.cb = (CheckBox) convertView
							.findViewById(R.id.cb_item);
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}
				// ��ԭ��������View����
				if (convertView.getScrollX() > 0) {
					convertView.scrollTo(0, 0);
				}
				final Receiver receiver = list.get(position);
				holder.tvReceiverName.setText(receiver.getName());
				holder.tvReceiverPhone.setText(receiver.getPhone());
				holder.tvReceiverAddress.setText(receiver.getAddress());
				
				holder.item_right.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						deleteReceiver(position);
					}
				});
				if (isSelecting) {
					holder.cb.setVisibility(View.VISIBLE);
					holder.viewModify.setVisibility(View.GONE);
					holder.viewAddress.setVisibility(View.GONE);
					holder.viewName.setVisibility(View.GONE);
					holder.viewSelect.setVisibility(View.VISIBLE);
					holder.tvIsDefault
					.setVisibility(receiver.isDefault() ? View.VISIBLE
							: View.GONE);
					//holder.ivArraw.setVisibility(View.GONE);
					if (receiver.getId() == selectedId) {
						holder.cb.setChecked(true);
					} else {
						holder.cb.setChecked(false);
					}
					holder.cb
							.setOnCheckedChangeListener(new OnCheckedChangeListener() {

								@Override
								public void onCheckedChanged(
										CompoundButton buttonView,
										boolean isChecked) {
									// TODO Auto-generated method stub
									if (isChecked) {
										Intent data = new Intent();
										data.putExtra("id", receiver.getId());
										data.putExtra("name", receiver.getName());
										data.putExtra("address", receiver.getAddress());
										data.putExtra("phone", receiver.getPhone());
										data.putExtra("memid", receiver.getMemid());
										data.putExtra("isdefault", receiver.isDefault());
										setResult(INTENT_CODE_SELECTING, data);
										finish();
									}
								}
							});
				} else {
					holder.cb.setVisibility(View.GONE);
					holder.viewModify.setVisibility(View.VISIBLE);
					holder.viewAddress.setVisibility(View.VISIBLE);
					holder.viewName.setVisibility(View.VISIBLE);
					holder.tvIsDefault.setVisibility(View.GONE);
					holder.viewSelect.setVisibility(View.GONE);
					holder.cbDefault.setChecked(receiver.isDefault());
					holder.cbDefault.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							// TODO Auto-generated method stub
							showProgress(true);
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("memid", mUserId);
							params.put("logintoken", mLoginToken);
							params.put("addressId", String.valueOf(receiver.getId()));
							params.put("address", receiver.getAddress());
							params.put("receiver", receiver.getName());
							params.put("phone", receiver.getPhone());
							params.put("isdefault", String.valueOf(receiver.isDefault()?1:0));
							PostAsyncTask postAsyncTask = new PostAsyncTask(URL_MODIFY,
									callbackModify);
							postAsyncTask.execute(params);
						}
					});
					//holder.ivArraw.setVisibility(View.VISIBLE);
				}
				return convertView;
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}

			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return list.get(position);
			}

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return list.size();
			}
		};
		mSwipeListView.setAdapter(adapter);
	}
	/**
	 * �޸ĵ�ַ�ӿ�
	 */
	private final static String URL_MODIFY = Constants.DOMAIN_NAME
			+ "axis2/services/sunteslwebservice/updateAddress";
	/**
	 * �޸ĵ�ַ�ص��ӿڣ��˴���Ҫ�����޸�֪��Ĭ�ϵ�ַ
	 */
	private OnCompleteCallback callbackModify = new OnCompleteCallback() {

		@Override
		public void onComplete(boolean isOk, String msg) {
			// TODO Auto-generated method stub
			showProgress(false);
			if (isOk) {
				JSONObject jsonObj;
				try {
					msg = XmlParser.parse(msg, "UTF-8", "return");
					jsonObj = new JSONObject(msg);
					int sendState = jsonObj.getInt("RESULT");
					if (sendState == 0) {
						initReceiver();
					} else if(sendState==2){
						Toast.makeText(getApplicationContext(), "���ܱ�����ͬ�ĵ�ַ��Ϣ",
								Toast.LENGTH_SHORT).show();
					}else {
						Toast.makeText(getApplicationContext(), "��ַ����ʧ��",
								Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "���������ش������Ժ�����...",
							Toast.LENGTH_SHORT).show();

					e.printStackTrace();
				}
			} else {
				Toast.makeText(getApplicationContext(), "���ӳ�ʱ�����Ժ�����...",
						Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	/**
	 * �޸ĵ�ַҳ��
	 * @param receiver
	 */
	private void modifyReceiver(Receiver receiver) {
		Intent intent = new Intent(AddressManageActivity.this,
				AddressModifyActivity.class);
		intent.putExtra("id", receiver.getId());
		intent.putExtra("name", receiver.getName());
		intent.putExtra("phone", receiver.getPhone());
		intent.putExtra("address", receiver.getAddress());
		intent.putExtra("isdefault", receiver.isDefault());
		startActivity(intent);
	}

	private final static String URL_DELETE = Constants.DOMAIN_NAME
			+ "axis2/services/sunteslwebservice/deleteAddress";

	private void deleteReceiver(int index) {
		ConfirmDialog dialog = new ConfirmDialog(this, "ȷ��Ҫɾ�����ջ���ַ��?",
				getString(R.string.tips_text),
				getString(R.string.confirm_text),
				getString(R.string.cancel_text));
		if (dialog.ShowDialog()) {
			showProgress(true);
			Receiver receiver = list.get(index);
			curIndex = index;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("memid", mUserId);
			params.put("logintoken", mLoginToken);
			params.put("addressId", String.valueOf(receiver.getId()));
			PostAsyncTask postAsyncTask = new PostAsyncTask(URL_DELETE,
					callbackDelete);
			postAsyncTask.execute(params);
		}
	}

	private OnCompleteCallback callbackDelete = new OnCompleteCallback() {

		@Override
		public void onComplete(boolean isOk, String msg) {
			// TODO Auto-generated method stub
			showProgress(false);
			if (isOk) {
				JSONObject jsonObj;
				try {
					msg = XmlParser.parse(msg, "UTF-8", "return");
					jsonObj = new JSONObject(msg);
					int sendState = jsonObj.getInt("RESULT");
					if (sendState == 0) {
						Toast.makeText(getApplicationContext(), "��ַɾ���ɹ�",
								Toast.LENGTH_SHORT).show();
						list.remove(curIndex);
						adapter.notifyDataSetChanged();
					} else {
						Toast.makeText(getApplicationContext(), "��ַɾ��ʧ��",
								Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "���������ش������Ժ�����...",
							Toast.LENGTH_SHORT).show();

					e.printStackTrace();
				}
			} else {
				Toast.makeText(getApplicationContext(), "���ӳ�ʱ�����Ժ�����...",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	static class ViewHolder {
		RelativeLayout item_left;
		RelativeLayout item_right;
		TextView tvReceiverName;
		TextView tvIsDefault;
		TextView tvReceiverPhone;
		TextView tvReceiverAddress;
		//ImageView ivArraw;
		CheckBox cb;
		CheckBox cbDefault;
		View viewName;
		View viewAddress;
		View viewModify;
		View viewSelect;
	}

	public void close(View v) {
		finish();
	}

	public void newAddress(View v) {
		Intent intent = new Intent(AddressManageActivity.this,
				AddressModifyActivity.class);
		startActivity(intent);
	}

	private ProgressDialog mPDialog;

	public void showProgress(final boolean show) {
		if (show) {
			mPDialog = new ProgressDialog(AddressManageActivity.this);
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
}
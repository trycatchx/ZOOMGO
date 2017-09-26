package com.dmsys.airdiskpro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.dmsys.airdiskpro.model.ContactsConfig;
import com.dmsys.mainbusiness.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContactsHistoryRecordAapter extends BaseAdapter {
	private List<ContactsConfig> list = new ArrayList<ContactsConfig>();
	private LayoutInflater mInflater;
	private Context mContext;
	private BackupOperaListenter mBackupOperaListenter;
	public interface BackupOperaListenter {
		public void recover(ContactsConfig c);

		public void delete(ContactsConfig c);

	};


	public void setBackupOperaListenter(BackupOperaListenter mBackupOperaListenter) {
		this.mBackupOperaListenter = mBackupOperaListenter;
	}

	public ContactsHistoryRecordAapter(Context mContext,
			List<ContactsConfig> list) {
		this.mContext = mContext;
		this.mInflater = LayoutInflater.from(mContext);
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(
					R.layout.contacts_history_record_item, null);
			initHolder(holder, convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		loadData2Holder(list.get(position), holder, position);

		return convertView;
	}

	private void loadData2Holder(ContactsConfig c, ViewHolder holder,
			final int position) {

		holder.tv_time.setText(getStringDate(c.getTime()));

		String numBer = String.format(mContext
				.getString(R.string.DM_Backup_Address_Records_Bak_C_Num),
				String.valueOf(c.getContactsNum()));
		holder.tv_number.setText(numBer);

		String model = String.format(
				mContext.getString(R.string.DM_Backup_Address_Records_From),
				String.valueOf(c.getPhoneModel()));
		holder.tv_model.setText(model);

		holder.recover.setTag(position);
		holder.delete.setTag(position);
	}

	public static String getStringDate(long time) {
		Date currentTime = new Date(time);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	private void initHolder(ViewHolder holder, View convertView) {
		holder.tv_time = (TextView) convertView.findViewById(R.id.tv_backup_time);
		holder.tv_number = (TextView) convertView.findViewById(R.id.tv_backup_number);
		holder.tv_model = (TextView) convertView.findViewById(R.id.tv_backup_phone_name);
		holder.recover = (Button) convertView.findViewById(R.id.btn_contacts_history_recover);
		holder.delete = (Button) convertView.findViewById(R.id.btn_contacts_history_delete);

		holder.recover.setOnClickListener(new BtnOnclickListener());
		holder.delete.setOnClickListener(new BtnOnclickListener());
	}

	private class BtnOnclickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int i = v.getId();
			if (i == R.id.btn_contacts_history_recover) {
				if (v instanceof Button) {

					Button btnRecover = (Button) v;
					int position = (int) (btnRecover.getTag());
					if (mBackupOperaListenter != null) {
						mBackupOperaListenter.recover(list.get(position));
					}


				}

			} else if (i == R.id.btn_contacts_history_delete) {
				if (v instanceof Button) {

					Button btnRecover = (Button) v;
					int position = (int) (btnRecover.getTag());

					if (mBackupOperaListenter != null) {
						mBackupOperaListenter.delete(list.get(position));
					}

				}


			}
		}

	}

	private class ViewHolder {
		public TextView tv_time;
		public TextView tv_number;
		public TextView tv_model;
		public Button recover;
		public Button delete;

	}

}

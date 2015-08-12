package de.nitri.aptimob;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jasypt.util.text.BasicTextEncryptor;

public class NewMessage extends Activity {

	private Message refMessage;
	private EditText etSubject;
	private EditText etBody;

	private Message message = new Message();

	private Object restUrl;

	private SharedPreferences pref;
	private String username;
	private String password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_message);

		pref = PreferenceManager.getDefaultSharedPreferences(this);
		restUrl = pref.getString(AptiMob.PREF_REST_URL,
				"https://www.nitri.de/aptimob/rest/");
		
		refMessage = (Message) getIntent().getSerializableExtra("refMessage");

		TextView tvTo = (TextView) findViewById(R.id.toField);
		etSubject = (EditText) findViewById(R.id.subjectField);
		etBody = (EditText) findViewById(R.id.bodyField);
		Button btnSend = (Button) findViewById(R.id.sendButton);
		
		tvTo.setText(refMessage.getFrom());
		etSubject.setText(refMessage.getSubject());

		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword("swordfish69");

		username = pref.getString(AptiMob.PREF_USERNAME, "");
		password = textEncryptor.decrypt(pref.getString(AptiMob.PREF_PASSWORD, ""));
		
		btnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				message.setDomain(refMessage.getDomain());
				message.setRefId(refMessage.getId());
				message.setSenderId(pref.getInt(AptiMob.PREF_USER_ID, 0));
				List<Integer> idList = new ArrayList<>();
				idList.add(refMessage.getSenderId());
				message.setUserIds(idList);
				message.setFrom(pref.getString(AptiMob.PREF_USERNAME, ""));
				message.setTo(refMessage.getFrom());
				message.setSubject(etSubject.getText().toString());
				message.setBody(etBody.getText().toString());

				Gson gson = new Gson();

				String jsonUserIds = gson.toJson(message.getUserIds());

				String data = "";

				try {
					data = URLEncoder.encode("userIds", "UTF-8") + "=" + URLEncoder.encode(jsonUserIds, "UTF-8");
					data += "&" + URLEncoder.encode("refId", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(message.getRefId()), "UTF-8");
					data += "&" + URLEncoder.encode("senderId", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(message.getSenderId()), "UTF-8");
					data += "&" + URLEncoder.encode("domain", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(message.getDomain()), "UTF-8");
					data += "&" + URLEncoder.encode("to", "UTF-8") + "=" + URLEncoder.encode(message.getTo(), "UTF-8");
					data += "&" + URLEncoder.encode("from", "UTF-8") + "=" + URLEncoder.encode(message.getFrom(), "UTF-8");
					data += "&" + URLEncoder.encode("subject", "UTF-8") + "=" + URLEncoder.encode(message.getSubject(), "UTF-8");
					data += "&" + URLEncoder.encode("body", "UTF-8") + "=" + URLEncoder.encode(message.getBody(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i("SENDING", data);
				RestTask restTask = new RestTask(getBaseContext(), "aptimob.sendMessage",
						username, password);
				restTask.execute(restUrl + "message", data, "POST");
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_message, menu);
		return true;
	}

}

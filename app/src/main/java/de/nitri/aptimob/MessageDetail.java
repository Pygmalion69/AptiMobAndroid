package de.nitri.aptimob;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MessageDetail extends Activity {

	private Message message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_detail);
		
		message = (Message) getIntent().getSerializableExtra("message");

		TextView tvFrom = (TextView) findViewById(R.id.fromField);
		TextView tvSubject = (TextView) findViewById(R.id.subjectField);
		Button btnReply = (Button) findViewById(R.id.replyButton);
		TextView tvBody = (TextView) findViewById(R.id.bodyField);
		
		tvFrom.setText(message.getFrom());
		tvSubject.setText(message.getSubject());
		tvBody.setText(message.getBody());
		
		btnReply.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), NewMessage.class);
				intent.putExtra("refMessage", message);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.message_detail, menu);
		return true;
	}

}

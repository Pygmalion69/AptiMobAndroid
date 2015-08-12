package de.nitri.aptimob;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MessageSentReceiver extends BroadcastReceiver {
	public MessageSentReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals("aptimob.sendMessage")) {
			Toast.makeText(context, context.getResources().getString(R.string.message_sent), Toast.LENGTH_LONG)
					.show();
		}
	}
}

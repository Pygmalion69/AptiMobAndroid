package de.nitri.aptimob;

import android.app.Activity;
import android.os.Bundle;

public class RegisterNewAccount extends Activity {

	private String user;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    user = getIntent().getStringExtra("user");
	    
	}

}

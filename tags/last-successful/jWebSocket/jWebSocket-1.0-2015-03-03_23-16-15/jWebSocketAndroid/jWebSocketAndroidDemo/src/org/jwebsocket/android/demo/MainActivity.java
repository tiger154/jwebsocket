//	---------------------------------------------------------------------------
//	jWebSocket - MainActivity (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.android.demo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 *
 * @author Alexander Schulze
 */
public class MainActivity extends ListActivity {

	/**
	 * Called when the activity is first created.
	 *
	 * @param icicle
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// start tracing to "[/sdcard/]jWebSocketAndroidDemo.trace"
		// Debug.startMethodTracing("/sdcard/jws");
		JWC.init();
		JWC.loadSettings(this);

		String[] lItems = {"Fundamentals", "Canvas Demo", "Camera Demo",
			"Video Demo", "RPC Demo", "Twitter Stream", "Arduino", "Setup"};

		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
				lItems));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
					case 0:
						startActivity(new Intent(MainActivity.this,
								Fundamentals.class));
						break;
					case 1:
						startActivity(new Intent(MainActivity.this,
								CanvasActivity.class));
						break;
					case 2:
						startActivity(new Intent(MainActivity.this,
								CameraActivity.class));
						break;
					case 3:
						startActivity(new Intent(MainActivity.this,
								VideoActivity.class));
						break;
					case 4:
						startActivity(new Intent(MainActivity.this,
								RPCDemoActivity.class));
						break;
					case 5:
						startActivity(new Intent(MainActivity.this,
								TwitterStreamActivity.class));
						break;
					case 6:
						startActivity(new Intent(MainActivity.this,
								ArduinoActivity.class));
						break;
					case 7:
						startActivity(new Intent(MainActivity.this,
								ConfigActivity.class));
						break;
				}
			}
		});
	}

	/**
	 *
	 */
	@Override
	protected void onDestroy() {
		// stop tracing
		// Debug.stopMethodTracing();
		super.onDestroy();
	}
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.android.demo;

import java.util.List;

import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.plugins.rpc.RPCCallable;
import org.jwebsocket.client.plugins.rpc.Rpc;
import org.jwebsocket.client.plugins.rpc.Rrpc;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.plugins.rpc.CommonRpcPlugin;
import org.jwebsocket.token.Token;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author prashant
 */
public class RPCDemoActivity extends Activity implements
		WebSocketClientTokenListener {

	private enum Target {

		ANDROID, BROWSER
	};
	private EditText classTxt;
	private EditText methodTxt;
	private EditText parameterTxt;
	private EditText targetTxt;
	private EditText resultTxt;
	private Button invokeBtn;
	private TextView targetLabel;
	private Boolean useRRPC = false;
	private ImageView statusImage;
	private Target selectedTarget = Target.ANDROID;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.rpc_demo);

		classTxt = (EditText) findViewById(R.id.classTxt);
		methodTxt = (EditText) findViewById(R.id.methodTxt);
		parameterTxt = (EditText) findViewById(R.id.parameterTxt);
		resultTxt = (EditText) findViewById(R.id.resultTxt);
		targetTxt = (EditText) findViewById(R.id.targetTxt);
		targetLabel = (TextView) findViewById(R.id.targetLabel);
		invokeBtn = (Button) findViewById(R.id.invokeBtn);
		statusImage = (ImageView) findViewById(R.id.statusImage);
//	targetRadioGroup = (RadioGroup) findViewById(R.id.radio_group);

		statusImage.setImageResource(R.drawable.disconnected);

		final CheckBox checkbox = (CheckBox) findViewById(R.id.rrpcCheckBox);
		checkbox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					targetTxt.setVisibility(EditText.VISIBLE);
					targetLabel.setVisibility(TextView.VISIBLE);
					//targetRadioGroup.setVisibility(RadioGroup.VISIBLE);
					useRRPC = true;
				} else {
					targetTxt.setVisibility(EditText.GONE);
					targetLabel.setVisibility(TextView.GONE);
					//targetRadioGroup.setVisibility(RadioGroup.GONE);
					useRRPC = false;
				}
				predefinedValues();
				targetLabel.invalidate();
				targetTxt.invalidate();
				//targetRadioGroup.invalidate();

			}
		});

		invokeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				sendMethodInvokeToken();
			}
		});

//		final RadioButton radio_browser = (RadioButton) findViewById(R.id.radio_browser);
//		final RadioButton radio_android = (RadioButton) findViewById(R.id.radio_android);
//		radio_browser.setOnClickListener(radio_listener);
//		radio_android.setOnClickListener(radio_listener);

	}

	private void predefinedValues() {
		if (useRRPC) {
			if (selectedTarget.equals(Target.ANDROID)) {
				classTxt.setText("org.jwebsocket.android.demo.RPCDemoActivity");
				methodTxt.setText("rrpcTest1");
			} else if (selectedTarget.equals(Target.BROWSER)) {
				classTxt.setText("demo");
				methodTxt.setText("rrpcTest1");
			}
		} else {
			classTxt.setText("org.jwebsocket.rpc.sample.SampleRPCLibrary");
			methodTxt.setText("getMD5");
		}
	}

//	private OnClickListener radio_listener = new OnClickListener() {
//		public void onClick(View v) {
//			// Perform action on clicks
//			RadioButton rb = (RadioButton) v;
//			if (rb.getId() == R.id.radio_android) {
//				selectedTarget = Target.ANDROID;
//			}
//			if (rb.getId() == R.id.radio_browser) {
//				selectedTarget = Target.BROWSER;
//			}
//			predefinedValues();
//		}
//	};
	@Override
	public boolean onCreateOptionsMenu(Menu aMenu) {
		MenuInflater lMenInfl = getMenuInflater();
		lMenInfl.inflate(R.menu.rpc_demo_menu, aMenu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.rpcMenuExit:
				this.finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void sendMethodInvokeToken() {
		// TODO:validate the text fields first
		String lClassName = classTxt.getText().toString().trim();
		String lMethodName = methodTxt.getText().toString().trim();
		String lParameter = parameterTxt.getText().toString().trim();
		String lTarget = targetTxt.getText().toString().trim();

		// If we make a simple rpc
		if (useRRPC) {
			new Rrpc(lClassName, lMethodName).to(lTarget).send(lParameter).call();
		} else {
			//Sending "" or "null" will send null.
			if ("".equals(lParameter) || "null".equals(lParameter)) {
				new Rpc(lClassName, lMethodName).call();
			} else {
				new Rpc(lClassName, lMethodName).send(lParameter).call();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		connect();
		RPCDemoActivity.mContext = getApplicationContext();
	}

	@Override
	protected void onPause() {
		super.onPause();
		disConnect();
	}

	private void connect() {
		try {
			JWC.addListener(this);
			JWC.open();
		} catch (WebSocketException ex) {
			resultTxt.setText(ex.getMessage());
		}
	}

	private void disConnect() {
		try {
			JWC.removeListener(this);
			JWC.close();
		} catch (WebSocketException ex) {
			// TODO: log exception
		}
	}

	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		if ((CommonRpcPlugin.RPC_TYPE).equals(aToken.getString("reqType"))) {
			if (aToken.getInteger("code") == 0) {
				resultTxt.setText(aToken.getString("result"));
			} else if (aToken.getInteger("code") == -1) {
				resultTxt.setText(aToken.getString("msg"));
			}
		}
	}

	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
		if (statusImage != null) {
			statusImage.setImageResource(R.drawable.authenticated);
		}
	}

	@Override
	public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
	}

	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
	}

	@Override
	public void processOpening(WebSocketClientEvent aEvent) {
	}

	@Override
	public void processReconnecting(WebSocketClientEvent aEvent) {
	}
	private static Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(
					mContext,
					msg.getData().get("method") + " has been called by the server (args "
					+ msg.getData().get("args"), Toast.LENGTH_SHORT).show();
		}
	};
	private static Context mContext;

	@RPCCallable(C2CAuthorized = true)
	public static void rrpcTest1() {
		Bundle b = new Bundle();
		b.putString("method", "rrpcTest1");
		b.putString("args", "null");
		Message msg = new Message();
		msg.setData(b);
		handler.sendMessage(msg);
	}

	@RPCCallable(C2CAuthorized = true)
	public static void rrpcTest1(String arg1) {
		Bundle b = new Bundle();
		b.putString("method", "rrpcTest1");
		b.putString("args", arg1);
		Message msg = new Message();
		msg.setData(b);
		handler.sendMessage(msg);
	}

	@RPCCallable(C2CAuthorized = true)
	public static void rrpcTest1(int arg1) {
		Bundle b = new Bundle();
		b.putString("method", "rrpcTest1");
		b.putString("args", String.valueOf(arg1));
		Message msg = new Message();
		msg.setData(b);
		handler.sendMessage(msg);
	}

	@RPCCallable(C2CAuthorized = true)
	public static void rrpcTest2(List<String> aList, List<List<Integer>> aList2) {
		Bundle b = new Bundle();
		b.putString("method", "rrpcTest2");
		b.putString("args", aList.toString() + ", " + aList2.toString());
		Message msg = new Message();
		msg.setData(b);
		handler.sendMessage(msg);
	}

	@RPCCallable(C2CAuthorized = true)
	public static void receiveMessage(String aMessage) {
		Bundle b = new Bundle();
		b.putString("method", "receiveMessage");
		b.putString("args", aMessage);
		Message msg = new Message();
		msg.setData(b);
		handler.sendMessage(msg);
	}

	public static void rrpcTest3() {
		Bundle b = new Bundle();
		b.putString("method", "rrpcTest3");
		b.putString("args", "null");
		Message msg = new Message();
		msg.setData(b);
		handler.sendMessage(msg);
	}
}

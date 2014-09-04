package com.bde.light.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

public class MyActivityUtils {
	
	/**
	 * show Toast
	 * @param context
	 * @param id
	 */
	public static void toast(Context context,int id) {
		Toast.makeText(context, id, Toast.LENGTH_SHORT).show();
	}
	
	public static AlertDialog makeAlertDialog(Context context, int titleId,
			int messageId, int postiveId, OnClickListener positiveListener,
			int NegativeId, OnClickListener negativeListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(titleId);
		builder.setMessage(messageId);
		builder.setPositiveButton(postiveId, positiveListener);
		builder.setNegativeButton(NegativeId, negativeListener);
		return builder.create();
	}
}

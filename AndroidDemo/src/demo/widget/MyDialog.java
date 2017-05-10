package demo.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import demo.android.R;

/**
 * 自定义对话框
 * @author Daimon
 * @version 3.0
 * @since 12/15/2013
 */

public abstract class MyDialog implements DialogInterface {
	
	private Dialog dialog;										//内置对话框
	
	public MyDialog(Context context) {
		dialog = new Dialog(context, R.style.Theme_Dialog);
//		dialog.setContentView(R.layout.my_dialog);
	}
	
	/**
	 * 设置对话框尺寸
	 */
	
	public void setSize(int width, int height)
	{
		Window window = dialog.getWindow();
		window.setLayout(width, height);
	}
	
	public boolean isShowing()
	{
		return dialog.isShowing();
	}
	
	public void show()
	{
		dialog.show();
	}
	
	public void hide()
	{
		dialog.hide();
	}

	@Override
	public void cancel() {
		dialog.cancel();
	}

	@Override
	public void dismiss() {
		dialog.dismiss();
	}
	
	public abstract Button getButton(int whichButton);
	
	/**
	 * 设置标题
	 * @param title 标题文本
	 */
	
	public abstract MyDialog setTitle(String title);
	
	/**
	 * 设置文本消息
	 * @param message 显示文本
	 */
	
	public abstract MyDialog setMessage(String message);
	
	/**
	 * 设置内容布局
	 * @param view 自定义视图
	 */
	
	public abstract MyDialog setContentView(View view);
	
	/**
	 * 设置左边的按钮
	 * @param text 按钮显示文本
	 * @param listener 按钮监听器
	 */
	
	public abstract MyDialog setPositiveButton(String text, OnClickListener listener);
	
	/**
	 * 设置右边的按钮
	 * @param text 按钮显示文本
	 * @param listener 按钮监听器
	 */
	
	public abstract MyDialog setNegativeButton(String text, OnClickListener listener);
	
	public void setCancelable(boolean cancelable)
	{
		dialog.setCancelable(cancelable);
	}
	
	public void setOnCancelListener(OnCancelListener onCancelListener)
	{
		dialog.setOnCancelListener(onCancelListener);
	}
	
	public void setCancelMessage(Message msg)
	{
		dialog.setCancelMessage(msg);
	}
	
	public void setCanceledOnTouchOutside(boolean cancel)
	{
		dialog.setCanceledOnTouchOutside(cancel);
	}
	
	public void setOnKeyListener(OnKeyListener onKeyListener)
	{
		dialog.setOnKeyListener(onKeyListener);
	}
}
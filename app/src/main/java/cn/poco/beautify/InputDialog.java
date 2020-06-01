package cn.poco.beautify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.ShareData;

public class InputDialog extends FrameLayout
{
	private Context m_context;
	private LinearLayout m_inputFr;
	private TextView m_okBtn;
	private EditText m_editText;
	private ImageView m_delBtn;

	private InputCallback m_cb;
	private String m_lastText;
	private boolean m_showSoftInput;
	private int m_softInputHeight;
	private boolean m_isFirstUp;
	private boolean m_canChange;
	protected GlobalListener m_lst;
	private View m_rootView;

	public InputDialog(Context context, InputCallback cb)
	{
		super(context);
		m_isFirstUp = true;
		m_showSoftInput = false;
		m_context = context;
		m_cb = cb;
		initUI();
		m_rootView = getRootView();
		if(m_rootView != null){
			m_lst = new GlobalListener(this);
			m_rootView.getViewTreeObserver().addOnGlobalLayoutListener(m_lst);
		}
	}

	@SuppressLint("NewApi")
	private void initUI()
	{
		FrameLayout.LayoutParams fl;
		LinearLayout.LayoutParams ll;
		m_inputFr = new LinearLayout(m_context);
		m_inputFr.setBackgroundColor(0xb2000000);
		int padding = ShareData.PxToDpi_xhdpi(30);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.TOP;
		fl.topMargin = ShareData.PxToDpi_xhdpi(-100) * 2;
		m_inputFr.setMinimumHeight(ShareData.PxToDpi_xhdpi(94));
		this.addView(m_inputFr, fl);
		{
			FrameLayout editFr = new FrameLayout(m_context);
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.weight = 1;
			ll.gravity = Gravity.CENTER_VERTICAL;
			editFr.setLayoutParams(ll);
			editFr.setPadding(padding, 0, ShareData.PxToDpi_xhdpi(20), 0);
			m_inputFr.addView(editFr);
			{
				LayoutInflater inflater = ((Activity)m_context).getLayoutInflater();
				inflater.inflate(R.layout.edittext, editFr);
				m_editText = (EditText)editFr.findViewById(R.id.edittext);
				m_editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				m_editText.setMaxLines(3);
				m_editText.setPadding(0, ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(20));
				m_editText.setOnTouchListener(m_touchLst);
				m_editText.setTextColor(Color.WHITE);
				m_editText.addTextChangedListener(m_watcher);
				m_editText.setBackgroundColor(0x00000000);
				fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.CENTER_VERTICAL;
				m_editText.setLayoutParams(fl);

				m_delBtn = new ImageView(m_context);
				m_delBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)m_context, R.drawable.input_delete_press, R.drawable.input_delete_press));
				m_delBtn.setOnClickListener(m_clickListener);
				fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.CENTER_VERTICAL|Gravity.RIGHT;
				editFr.addView(m_delBtn, fl);
			}

			m_okBtn = new TextView(m_context);
			m_okBtn.setBackgroundResource(R.drawable.beauty_text_add_btn);
			m_okBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			m_okBtn.setTextColor(Color.WHITE);
			m_okBtn.setText("OK");
			m_okBtn.setGravity(Gravity.CENTER);
			m_okBtn.setOnClickListener(m_clickListener);
			ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(100), LinearLayout.LayoutParams.MATCH_PARENT);
			ll.gravity = Gravity.CENTER_VERTICAL;
			m_okBtn.setLayoutParams(ll);
			m_inputFr.addView(m_okBtn);
		}
	}

	private View.OnClickListener m_clickListener = new View.OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			if(v == m_okBtn)
			{
				/*String text = m_editText.getText().toString();
				text = text.replace('\n', '$');
				text = text.replace('\r', '$');*/
				if(m_cb != null)
				{
					m_cb.onOk();
				}
			}
			else if(v == m_delBtn)
			{
				m_editText.setText("");
			}
		}
	};

	private View.OnTouchListener m_touchLst = new View.OnTouchListener()
	{

		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			if(event.getAction() == MotionEvent.ACTION_UP)
			{
				m_showSoftInput = true;
				resizeInputFr();
			}
			return false;
		}
	};

	private TextWatcher m_watcher = new TextWatcher()
	{

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void afterTextChanged(Editable s)
		{
			if (!s.toString().equals(""))
			{
				m_delBtn.setVisibility(View.VISIBLE);
			} else {
				m_delBtn.setVisibility(View.GONE);
			}
			String text = s.toString();
			text = text.replace('\n', '$');
			text = text.replace('\r', '$');
			if(m_cb != null && m_canChange)
			{
				m_cb.onChange(m_lastText, text);
				m_lastText = text;
			}
			else
			{
				m_canChange = true;
			}
		}
	};

	public void setDatas(String text)
	{
		m_canChange = false;
		m_lastText = text;

		if(m_cb != null)
		{
			m_cb.onChange("", text);
		}
		text = text.replaceAll("[$]", "\n");
		m_editText.setText(text);
		m_editText.setSelection(text.length());
	}

	public void show()
	{
		this.setVisibility(View.VISIBLE);
		m_showSoftInput = true;
//		System.out.println("show: ");
		if(m_softInputHeight > 100)
		{
			if(m_cb != null)
			{
				m_cb.onShowSoftInput(true, m_softInputHeight);
			}
		}
		m_softInputHeight = 0;
		m_editText.setFocusable(true);
		m_editText.setFocusableInTouchMode(true);
		m_editText.requestFocus();
		m_editText.findFocus();

		showSoftInput(m_editText);
	}

	public void hide()
	{
		hideSoftInput(m_editText);
	}

	public void hide1()
	{
		this.setVisibility(View.GONE);
		m_showSoftInput = false;
		m_canChange = false;
		if(m_cb != null)
		{
			m_cb.onShowSoftInput(false, m_softInputHeight);
		}
	}

	public void clearAll()
	{
		hideSoftInput(m_editText);
		this.removeAllViews();
		if(m_inputFr != null)
		{
			m_inputFr.removeAllViews();
			m_inputFr = null;
		}
		if(m_editText != null)
		{
			m_editText.removeTextChangedListener(m_watcher);
			m_watcher = null;
		}
		m_cb = null;
		m_showSoftInput = false;

		if(m_rootView != null){
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
			{
				m_rootView.getViewTreeObserver().removeGlobalOnLayoutListener(m_lst);
			}
			else
			{
				m_rootView.getViewTreeObserver().removeOnGlobalLayoutListener(m_lst);
			}
			m_rootView = null;
		}
		if(m_lst != null)
		{
			m_lst.clearAll();
			m_lst = null;
		}
		m_touchLst = null;
	}

	public void resizeInputFr()
	{
		FrameLayout.LayoutParams fl;
//		System.out.println("resizeInputFr");

		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.BOTTOM;
		fl.bottomMargin = m_softInputHeight;
		m_inputFr.setLayoutParams(fl);
		m_inputFr.setAlpha(1f);
		m_inputFr.setMinimumHeight(ShareData.PxToDpi_xhdpi(94));
	}

	public void showSoftInput(View v)
	{
		InputMethodManager manager = (InputMethodManager)m_context.
				getSystemService(Context.INPUT_METHOD_SERVICE);
		if (manager != null) {
			manager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
//			manager.toggleSoftInputFromWindow(v.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public void hideSoftInput(View v)
	{
		InputMethodManager manager = (InputMethodManager)m_context.
				getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public int GetKeyboardHeight()
	{
		Rect r = new Rect();
		getWindowVisibleDisplayFrame(r);
		int visibleHeight = r.height();
		Display display = ((Activity)getContext()).getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		int m_screenWidth = metrics.widthPixels;
		int m_screenHeight = metrics.heightPixels;
		if(m_screenWidth > m_screenHeight)
		{
			m_screenWidth += m_screenHeight;
			m_screenHeight = m_screenWidth - m_screenHeight;
			m_screenWidth -= m_screenHeight;
		}
		int height = m_screenHeight;
		int heightDiff = height - visibleHeight;
//		System.out.println("heightDiff: " + heightDiff);
//		System.out.println("m_count: " + m_count);
		if(heightDiff > 100)
		{
//			m_softInputHeight = heightDiff;
			return heightDiff;
		}
		else
		{
			return 0;
		}
	}

	public static class GlobalListener implements ViewTreeObserver.OnGlobalLayoutListener
	{
		InputDialog m_view;

		public GlobalListener(InputDialog view)
		{
			m_view = view;
		}

		@Override
		public void onGlobalLayout()
		{
			if(m_view != null)
			{
				int height = m_view.GetKeyboardHeight();
				int dis = height - m_view.m_softInputHeight;
				if(height > 0)
				{
					m_view.m_softInputHeight = height;
				}
//				System.out.println("dis: " + dis);
//				System.out.println("height: " + height);
//				System.out.println("m_showSoftInput: " + m_view.m_showSoftInput);
//				System.out.println("m_softInputHeight: " + m_view.m_softInputHeight);
				if(dis >= 0 && m_view.m_showSoftInput && m_view.m_softInputHeight > 100 && m_view.m_isFirstUp)
				{
					m_view.m_isFirstUp = false;
					if(m_view.m_cb != null)
					{
						m_view.m_cb.onShowSoftInput(true, m_view.m_softInputHeight);
					}
					m_view.resizeInputFr();
				}
				else if(dis <= 0 && m_view.m_showSoftInput && m_view.m_softInputHeight == 0 && m_view.m_isFirstUp)
				{
					m_view.hideSoftInput(m_view.m_editText);
					m_view.showSoftInput(m_view.m_editText);
				}
				else if(dis < -200 && m_view.m_showSoftInput && m_view.m_softInputHeight > 100)
				{
					if(height <= 200)
					{
						m_view.hide1();
					}
					else
					{
//						System.out.println("resizeInputFr: ");
						if(m_view.m_cb != null)
						{
							m_view.m_cb.onShowSoftInput(true, m_view.m_softInputHeight);
						}
						m_view.resizeInputFr();
					}
				}
			}
		}

		public void clearAll()
		{
			m_view = null;
		}
	}

	public static interface InputCallback{
		public void onOk();

		public void onShowSoftInput(boolean show, int softHeight);

		public void onChange(String lastText, String text);

		public void onCancel();
	}
}

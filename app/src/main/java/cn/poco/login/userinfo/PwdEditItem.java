package cn.poco.login.userinfo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.NumberKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.poco.interphoto2.R;
import cn.poco.login.widget.EditTextWithDel;
import cn.poco.tianutils.ShareData;

import static cn.poco.tianutils.ShareData.PxToDpi_xhdpi;

/**
 * Created by lgd on 2017/2/15.
 */

public class PwdEditItem extends RelativeLayout
{
	private boolean isHidePwd = true;
	protected EditTextWithDel editText;
	private ImageView pswShowIcon;

	public PwdEditItem(Context context)
	{
		super(context);
		init();
	}

	private void init()
	{
		LayoutParams rl;
		setBackgroundColor(0x4d000000);
		setMinimumHeight(PxToDpi_xhdpi(100));

		editText = new EditTextWithDel(getContext(), -1, R.drawable.login_delete_logo);
		editText.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		editText.setBackgroundColor(0x00000000);
		editText.setPadding(0, 0, PxToDpi_xhdpi(5), 0);
		editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f);
		editText.setTextColor(Color.WHITE);
		editText.setHintTextColor(0xff666666);
//		editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		editText.setTypeface(Typeface.MONOSPACE, 0);
		editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		editText.setSingleLine();
		editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
		editText.setHint(getResources().getString(R.string.login_password));
		editText.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
		editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
		editText.setKeyListener(new NumberKeyListener()
		{
			@Override
			protected char[] getAcceptedChars()
			{
				String passwordRuleStr = getContext().getString(R.string.rule_password);
				return passwordRuleStr.toCharArray();
			}

			@Override
			public int getInputType()
			{
				return InputType.TYPE_TEXT_VARIATION_PASSWORD;
			}
		});
		editText.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if(!hasFocus)
				{
					if(v instanceof EditTextWithDel)
					{
						EditTextWithDel editText = (EditTextWithDel)v;
						editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
					}
				}
				else
				{
					if(v instanceof EditTextWithDel)
					{
						EditTextWithDel editText = (EditTextWithDel)v;
						editText.setDrawable();
					}
				}
			}
		});

		rl = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		rl.addRule(RelativeLayout.LEFT_OF, R.id.login_edit_right_icon);
		rl.leftMargin = PxToDpi_xhdpi(40);
		rl.rightMargin = PxToDpi_xhdpi(40);
		addView(editText, rl);

		pswShowIcon = new ImageView(getContext());
		pswShowIcon.setImageResource(R.drawable.login_pwd_state_invisiable);
		pswShowIcon.setId(R.id.login_edit_right_icon);
		int padding = ShareData.PxToDpi_xhdpi(20);
		pswShowIcon.setPadding(padding, padding, padding * 2, padding);
		rl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		addView(pswShowIcon, rl);
		pswShowIcon.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(isHidePwd)
				{
					isHidePwd = false;
					pswShowIcon.setImageResource(R.drawable.login_pwd_state_visiable);
					editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					editText.setSelection(editText.length());

				}
				else
				{
					isHidePwd = true;
					pswShowIcon.setImageResource(R.drawable.login_pwd_state_invisiable);
					editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
					editText.setSelection(editText.length());
				}

			}
		});
	}

	public EditTextWithDel getEditText()
	{
		return editText;
	}

	public ImageView getPswShowIcon()
	{
		return pswShowIcon;
	}
}

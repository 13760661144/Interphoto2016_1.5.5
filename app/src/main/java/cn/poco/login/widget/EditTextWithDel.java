package cn.poco.login.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class EditTextWithDel extends EditText {
	private final static String TAG = "EditTextWithDel";
    private Drawable imgInable;
    private Drawable imgAble;
    private TextWatcher textWatcher = null;
	public EditTextWithDel(Context context,int imgInableRes,int imgAbleRes) {
        super(context);	
		initDel(imgInableRes, imgAbleRes);
	}
	
	public EditTextWithDel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EditTextWithDel(Context context) {
		super(context);
	}

	public void initDel(int imgInableRes, int imgAbleRes) {
        if(imgInableRes != -1){
            imgInable = getResources().getDrawable(imgInableRes);
        }
        if(imgAbleRes != -1){
            imgAble = getResources().getDrawable(imgAbleRes);
        }
        if(textWatcher == null){
            textWatcher = new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    setDrawable();
                }
            };
        }
        addTextChangedListener(textWatcher);
        setDrawable();

        setOnFocusChangeListener(new OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    EditTextWithDel editText = (EditTextWithDel)v;
                    editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
                else
                {
                    EditTextWithDel editText = (EditTextWithDel)v;
                    editText.setDrawable();
                }
            }
        });
    }

	public void setDrawable() {
		// TODO Auto-generated method stub
		if(length() < 1){
            setCompoundDrawablesWithIntrinsicBounds(null, null, imgInable, null);
        }
        else{
            setCompoundDrawablesWithIntrinsicBounds(null, null, imgAble, null);
        }
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
        if (imgAble != null && event.getAction() == MotionEvent.ACTION_UP) {
            int eventX = (int) event.getRawX();
            int eventY = (int) event.getRawY();
            Rect rect = new Rect();
            getGlobalVisibleRect(rect);
            rect.left = rect.right - imgAble.getIntrinsicWidth() - 20;
            if(rect.contains(eventX, eventY))
                setText("");
        }
		return super.onTouchEvent(event);
	}



}

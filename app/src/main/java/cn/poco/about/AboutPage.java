package cn.poco.about;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import cn.poco.about.site.AboutPageSite;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.SysConfig;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.ShareData;

//import com.amap.api.a.a.l;

public class AboutPage extends IPage {
    private static final String TAG = "关于页";
    protected AboutPageSite m_site;
    protected LinearLayout m_mainLayout;
    protected LinearLayout m_topBarLayout;
    protected ImageView m_backBtn;
    protected ImageView m_interPhoto;
    protected TextView m_version;
    protected RelativeLayout m_bottomLayout;
    private int mLogoClickCount = 0;
    private String m_password = "1308";
    private AlertDialog m_passwordInputDlg;
    private EditText m_editText;

    public AboutPage(Context context, BaseSite site) {
        super(context, site);

        m_site = (AboutPageSite) site;
        InitUI();
		TongJiUtils.onPageStart(getContext(), TAG);
    }


    @Override
    public void SetData(HashMap<String, Object> params) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onBack() {
        m_site.OnBack(getContext());

		TongJiUtils.onPageEnd(getContext(), TAG);
	}

	@Override
	public void onResume()
	{
		TongJiUtils.onPageResume(getContext(), TAG);
		super.onResume();
	}

	@Override
	public void onPause()
	{
		TongJiUtils.onPagePause(getContext(), TAG);
		super.onPause();
	}

    protected void InitUI() {
        int heightDis = ShareData.m_screenRealHeight - ShareData.m_screenHeight; //虚拟键盘高度

        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        m_mainLayout = new LinearLayout(getContext());
        m_mainLayout.setOrientation(LinearLayout.VERTICAL);
        m_mainLayout.setBackgroundColor(0xFF000000);
        addView(m_mainLayout, fl);
        {
            m_backBtn = new ImageView(getContext());
            m_backBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity) getContext(), R.drawable.framework_back_btn, R.drawable.framework_back_btn));
            m_backBtn.setId(R.id.about_back);
            LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.LEFT | Gravity.CENTER;
            m_mainLayout.addView(m_backBtn, ll);
            m_backBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    m_site.OnBack(getContext());
                }
            });

            m_interPhoto = new ImageView(getContext());
            m_interPhoto.setImageResource(R.drawable.about_interphoto);
            ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.CENTER_HORIZONTAL;
            ll.topMargin = ShareData.PxToDpi_xhdpi(80);
            m_mainLayout.addView(m_interPhoto, ll);
            m_interPhoto.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View arg0)
                {
                    mLogoClickCount++;
                    if(mLogoClickCount >= 5)
                    {
                        mLogoClickCount = 0;
                        if(!SysConfig.IsDebug())
                        {
                            if(m_passwordInputDlg != null && !m_passwordInputDlg.isShowing())
                            {
                                m_passwordInputDlg.show();
                                m_editText.setText("");
                            }
                        }
                        else
                        {
                            SysConfig.SetDebug(!SysConfig.IsDebug());
                            if(SysConfig.IsDebug()) Toast.makeText(getContext(), "已启动调试模式!", Toast.LENGTH_LONG).show();
                            else Toast.makeText(getContext(), "已关闭调试模式!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

            m_version = new TextView(getContext());
            m_version.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
            m_version.setText(SysConfig.GetAppVer(getContext()));
            m_version.setTextColor(0xFFEEEEEE);
            ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.CENTER_HORIZONTAL;
            ll.topMargin = ShareData.PxToDpi_xhdpi(30);
            ll.bottomMargin = ShareData.PxToDpi_xhdpi(100);
            m_mainLayout.addView(m_version, ll);

//            String introText = "印象，\n献给热爱生活，\n刷新色彩的光影记录者。\n\n" +
//                    "不同的时间，不同的心情，\n不同的地点，不同的色温，不同的人...\n构成了你独特的作品。\n\n" +
//                    "创作之余，\n我们相信，\n你可以在这里，\n遇见更多丰富的内心。\n做一款应用和我们的印象电子杂志同名，\n见证10年。";
            String introText = getResources().getString(R.string.about_interphoto) + getResources().getString(R.string.about_show1)+ getResources().getString(R.string.about_show2)+getResources().getString(R.string.about_show3)+getResources().getString(R.string.about_show4);

            TextView impression = new TextView(getContext());
            impression.setText(introText);
            impression.setTextColor(0xFFAAAAAA);
            impression.setGravity(Gravity.CENTER_HORIZONTAL);
            impression.setLineSpacing(ShareData.PxToDpi_xhdpi(12), 1.2f);
            impression.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            impression.setPadding(ShareData.PxToDpi_xhdpi(50),0,ShareData.PxToDpi_xhdpi(50),0);
            ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.CENTER_HORIZONTAL;
            ll.bottomMargin = ShareData.PxToDpi_xhdpi(12);
            m_mainLayout.addView(impression, ll);

            m_bottomLayout = new RelativeLayout(getContext());
            m_bottomLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            m_mainLayout.addView(m_bottomLayout, ll);
            {
                LinearLayout bottomLayout = new LinearLayout(getContext());
                bottomLayout.setOrientation(LinearLayout.HORIZONTAL);
                RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                rl.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                rl.bottomMargin = ShareData.PxToDpi_xhdpi(50) + heightDis;
                m_bottomLayout.addView(bottomLayout, rl);
                {
                    ImageView bottomImg = new ImageView(getContext());
                    bottomImg.setImageResource(R.drawable.about_icon);
                    bottomImg.setId(R.id.about_icon);
                    ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    ll.gravity = Gravity.CENTER;
                    bottomLayout.addView(bottomImg, ll);

                    TextView bottomTv = new TextView(getContext());
                    bottomTv.setText("Beauty,lnc.");
                    bottomTv.setTextColor(0xFF666666);
                    bottomTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                    ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    ll.leftMargin = ShareData.PxToDpi_xhdpi(12);
                    bottomLayout.addView(bottomTv, ll);
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getResources().getString(R.string.password_input_title));
            m_editText = new EditText(getContext());
            m_editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    hideSoftInput();
                    if(m_passwordInputDlg != null)
                    {
                        m_passwordInputDlg.dismiss();
                    }
                }
            });
            builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if(m_editText.getText().toString().equals(m_password))
                    {
                        SysConfig.SetDebug(!SysConfig.IsDebug());
                        if(SysConfig.IsDebug()) Toast.makeText(getContext(), "已启动调试模式!", Toast.LENGTH_LONG).show();
                        else Toast.makeText(getContext(), "已关闭调试模式!", Toast.LENGTH_LONG).show();
                    }
                    hideSoftInput();
                    if(m_passwordInputDlg != null)
                    {
                        m_passwordInputDlg.dismiss();
                    }
                }
            });
            m_passwordInputDlg = builder.create();
            m_passwordInputDlg.setView(m_editText);
            m_passwordInputDlg.setCanceledOnTouchOutside(false);
            m_passwordInputDlg.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            m_passwordInputDlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    public void hideSoftInput()
    {
        InputMethodManager manager= (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(m_passwordInputDlg != null && manager != null)
        {
            manager.hideSoftInputFromWindow(m_passwordInputDlg.getCurrentFocus().getWindowToken(), 0);
        }
    }

}

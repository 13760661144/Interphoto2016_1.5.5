package cn.poco.video.page;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.poco.home.PageNumComponent;
import cn.poco.interphoto2.R;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.ShareData;

/**
 * Created by admin on 2017/7/14.
 */

public class ReminderPage extends FrameLayout implements View.OnClickListener {
    private Context mContext;
    private ViewPager mViewPager;
    private PageNumComponent pageNumComponent;
    private Button okBtn;
    private int mCurrPos = 0;
    private int CAMERA = 0;
    private int VIDEO = 1;
    private int[] imag = {
            R.drawable.home_diaog_imag1, R.drawable.home_diaog_imag2
    };
    private String[] title = {
            getContext().getResources().getString(R.string.version_updata_title1),
            getContext().getResources().getString(R.string.version_updata_title2)

    };
    private String[] text = {
            getContext().getResources().getString(R.string.version_updata_text1),
            getContext().getResources().getString(R.string.version_updata_text2)

    };

    public ReminderPage(Context context) {
        super(context);
        this.mContext = context;

        initView();
    }

    private void initView() {

        setBackgroundColor(Color.parseColor("#7F333333"));

        RelativeLayout mLayout = new RelativeLayout(mContext);
        mLayout.setBackgroundColor(Color.parseColor("#333333"));
        FrameLayout.LayoutParams mParams = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(560), ShareData.PxToDpi_xhdpi(920));
        mParams.gravity = Gravity.CENTER;
        this.addView(mLayout, mParams);

        mViewPager = new ViewPager(mContext);
        mViewPager.setId(R.id.home_dialog_viewPager);
        mViewPager.addOnPageChangeListener(new MyChangePageClickListener());
        mViewPager.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(710)));
        mViewPager.setAdapter(new MyAdapter(imag));
        mLayout.addView(mViewPager);


        okBtn = new Button(mContext);
        okBtn.setText( getContext().getResources().getString(R.string.version_updata_okBtn));
        okBtn.setId(R.id.home_dialog_okBtn);
        okBtn.setOnClickListener(this);
        okBtn.setTextColor(Color.parseColor("#333333"));
        okBtn.setBackgroundColor(Color.parseColor("#ffc433"));
        okBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(ShareData.PxToDpi_xhdpi(235), ShareData.PxToDpi_xhdpi(70));
        mLayoutParams.addRule(RelativeLayout.BELOW, R.id.home_dialog_viewPager);
        mLayoutParams.bottomMargin = ShareData.PxToDpi_xhdpi(60);
        mLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mLayout.addView(okBtn, mLayoutParams);


        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout linearLayout = new LinearLayout(mContext);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.home_dialog_okBtn);
        mLayout.addView(linearLayout, layoutParams);

        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lParams.topMargin = ShareData.PxToDpi_xhdpi(30);
        lParams.bottomMargin = ShareData.PxToDpi_xhdpi(30);
        lParams.gravity = Gravity.CENTER_HORIZONTAL;
        pageNumComponent = new PageNumComponent(mContext);
        pageNumComponent.page_num_out = cn.poco.interphoto2.R.drawable.homepage_dot_out;
        pageNumComponent.page_num_over = cn.poco.interphoto2.R.drawable.homepage_dot_over;
//        pageNumComponent.page_num_margin = ShareData.PxToDpi_xhdpi(15);
        pageNumComponent.UpdatePageNum(0, imag.length);
        linearLayout.addView(pageNumComponent, lParams);


    }


    @Override
    public void onClick(View v) {
        if (v == okBtn) {
            if (mCurrPos == CAMERA) {
                TagMgr.SetTag(getContext(), Tags.HOME_CAMERA_TIP + SysConfig.GetAppVerNoSuffix(getContext()));
                setVisibility(View.GONE);
             //   MyFramework.SITE_Open(PocoCamera.main, CameraPageSite.class, null, Framework.ANIM_TRANSLATION_LEFT);

            }
            if (mCurrPos == VIDEO) {
                TagMgr.SetTag(getContext(), Tags.HOME_CAMERA_TIP + SysConfig.GetAppVerNoSuffix(getContext()));
                setVisibility(View.GONE);
                //MyFramework.SITE_Open(PocoCamera.main, VideoAlbumSite.class, null, Framework.ANIM_TRANSLATION_LEFT);
            }
        }

    }

    class MyChangePageClickListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            pageNumComponent.UpdatePageNum(position, imag.length);
            mCurrPos = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    class MyAdapter extends PagerAdapter {

        private int[] imag;

        public MyAdapter(int[] imag) {
            this.imag = imag;

        }

        @Override
        public int getCount() {
            return imag.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(320));
            RelativeLayout relativeLayout = new RelativeLayout(mContext);
            ImageView imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(imag[position]);
            imageView.setId(R.id.home_dialog_image);
            relativeLayout.addView(imageView, layoutParams);

            TextView titleView = new TextView(mContext);
            titleView.getPaint().setFakeBoldText(true);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            titleView.setTextColor(Color.parseColor("#FFFFFF"));
            titleView.setId(R.id.home_dialog_titleView);
            titleView.setText(title[position]);
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            layoutParams.topMargin = ShareData.PxToDpi_xhdpi(50);
            layoutParams.bottomMargin = ShareData.PxToDpi_xhdpi(30);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.home_dialog_image);
            relativeLayout.addView(titleView, layoutParams);

            View view = new View(mContext);
            view.setId(R.id.home_dialog_line);
            view.setBackgroundColor(Color.parseColor("#454545"));
            layoutParams = new RelativeLayout.LayoutParams(ShareData.PxToDpi_xhdpi(90), ShareData.PxToDpi_xhdpi(2));
            layoutParams.addRule(RelativeLayout.BELOW, R.id.home_dialog_titleView);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            relativeLayout.addView(view, layoutParams);


            TextView updataText = new TextView(mContext);
            updataText.setLineSpacing(1.5f, 1.5f);
            updataText.setId(R.id.home_dialog_updateText);
            updataText.setTextColor(Color.parseColor("#aaaaaa"));
            updataText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            updataText.setText(text[position]);
            layoutParams = new RelativeLayout.LayoutParams(ShareData.PxToDpi_xhdpi(400), ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = ShareData.PxToDpi_xhdpi(40);
            layoutParams.bottomMargin = ShareData.PxToDpi_xhdpi(60);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.home_dialog_line);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            relativeLayout.addView(updataText, layoutParams);
            container.addView(relativeLayout);

            return relativeLayout;
        }
    }

 /*   class MyRelativeLayout extends  FrameLayout{

        private Paint mPaint;


        public MyRelativeLayout(Context context) {
            super(context);
           mPaint = new Paint();
            mPaint.setColor(Color.BLACK);
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            //绘制阴影，param1：模糊半径；param2：x轴大小：param3：y轴大小；param4：阴影颜色
            mPaint.setShadowLayer(10F, 15F, 15F, Color.BLACK);
            RectF rect = new RectF(0 , 0, ShareData.PxToDpi_xhdpi(570), ShareData.PxToDpi_xhdpi(930));
            canvas.drawRect(rect, mPaint);
        }

    }*/
}

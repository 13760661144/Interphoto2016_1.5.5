package cn.poco.MaterialMgr2;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.poco.tianutils.ShareData;


public class RecylerViewV1 extends LinearLayout {
    public final static int REFRESH_PRE = 1;
    public final static int REFRESH_ING = 2;
    public final static int REFRESH_WILL = 3;

    private RecyclerView m_recyclerView;
    public TextView m_textView;
    private RefreshImp m_cb;
    private float m_deboost = 0.09f;
    private ImageView m_img;
    public static int m_centerPosition = (int) ((ShareData.m_screenHeight - ShareData.PxToDpi_xhdpi(80) - ShareData.PxToDpi_xhdpi(380))/2f);
    public static int m_height = ShareData.m_screenHeight - ShareData.PxToDpi_xhdpi(80);
    public static int m_initPosition = m_height - m_centerPosition;
    private boolean m_initTopMargin;
    public static int m_centerHeight = (int) ((ShareData.m_screenHeight - ShareData.PxToDpi_xhdpi(80) - ShareData.PxToDpi_xhdpi(380))/2f);
    private int m_moveDy = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    public static boolean m_refreshHide = false;
    private int needReLayoutCount = (int) (RecylerViewV1.m_height/ (ShareData.PxToDpi_xhdpi(380) + 0.5f));
    public RecylerViewV1(Context context) {
        super(context);
        initUI();
    }

    public RecylerViewV1(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI();
    }

    public RecylerViewV1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initUI();
    }

    private void initUI()
    {
        this.setOrientation(LinearLayout.VERTICAL);
        m_textView = new TextView(getContext());
        LayoutParams ll = new LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(100));
        ll.topMargin = -ShareData.PxToDpi_xhdpi(100);
        m_textView.setText("下拉刷新");
        m_textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12);
        m_textView.setGravity(Gravity.CENTER);
        m_textView.setLayoutParams(ll);
        this.addView(m_textView);

        m_recyclerView = new RecyclerView(getContext())
        {

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                super.onLayout(changed, l, t, r, b);
                if(changed)
                {
                    m_centerPosition = (this.getHeight() - ShareData.PxToDpi_xhdpi(380))/2;
                    m_initPosition = this.getHeight() - m_centerPosition;
                    m_height = this.getHeight();
                }

//                if(!m_initTopMargin)
//                {
//                    updateImgPosition(m_recyclerView);
//                }

            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent e) {
                LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams) m_textView.getLayoutParams();
                if(ll.topMargin < -ShareData.PxToDpi_xhdpi(100))
                {
                    return true;
                }
                return super.onInterceptTouchEvent(e);
            }

            @Override
            public boolean onTouchEvent(MotionEvent e) {
                if(m_flag9)
                {
                    return true;
                }
                return super.onTouchEvent(e);
            }
        };
        ll = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        m_recyclerView.setLayoutParams(ll);
        this.addView(m_recyclerView);
        m_recyclerView.setOnTouchListener(m_onTouch1);
//        m_recyclerView.addOnScrollListener(m_scrollListener);
        m_recyclerView.setChildDrawingOrderCallback(new RecyclerView.ChildDrawingOrderCallback() {
            @Override
            public int onGetChildDrawingOrder(int childCount, int i) {

                if(m_recyclerView.getAdapter().getItemCount() <= needReLayoutCount + 2)
                {
                    updateImgPosition(m_recyclerView);
                }
                else if(childCount == needReLayoutCount + 1 || childCount == needReLayoutCount + 2)
                {
                    updateImgPosition(m_recyclerView);
                }
                return i;
            }
        });
    }


    ViewTreeObserver.OnPreDrawListener m_onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            if(!m_initTopMargin)
            {
//                updateImgPosition(m_recyclerView);
                Log.i("MMM","m_initTopMargin === " + m_initTopMargin);
                initTopMargin();
            }
            return true;
        }
    };

    public int getRefreshState()
    {
        return m_refreshState;
    }

    public RecyclerView.OnScrollListener m_scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
//            if(!m_initTopMargin)
//            {
//                updateImgPosition(recyclerView);
//                return;
//            }

                if(m_isfinishingAnim && dy != 0)
                {
//                    if((getScollYDistance(recyclerView)) != 0)
//                    {
//                        int count = recyclerView.getChildCount();
//                        if(count > 0)
//                        {
//                            for(int i = 0; i < count; i++)
//                            {
//                                View view = recyclerView.getChildAt(i);
//                                if(view instanceof ThemeItemView)
//                                {
//                                    ThemeItemView itemView = (ThemeItemView) view;
//                                    FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams)itemView.m_img.getLayoutParams();
//                                    fl.topMargin = (int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f) + (int)(getScollYDistance(recyclerView)*m_deboost);
//
////                            Log.i("AAA","scrollY = " + (getScollYDistance(recyclerView)));
//                                    itemView.m_img.setLayoutParams(fl);
//                                }
//                            }
//                        }
//                    }
                    updateImgPosition(recyclerView);
                }
        }
    };


    public void updateImgPosition(RecyclerView recyclerView)
    {
        int count = recyclerView.getChildCount();
        Log.i("iii","childCount = " + count);
           if(count > 0)
             {
                 m_initTopMargin = true;
                     for(int i = 0; i < count; i++)
                     {
                        View view = recyclerView.getChildAt(i);
                         if(view instanceof ThemeItemView)
                         {
                           ThemeItemView itemView = (ThemeItemView) view;
                           FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams)itemView.m_img.getLayoutParams();
//                           fl.topMargin = (int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f) + (int)(getScollYDistance(recyclerView)*m_deboost);
                           int temp = itemView.getTop();
//                             Log.i("iii","temp = " + temp);
                             if(temp < m_centerPosition)
                             {
                                 if(temp < 0)
                                 {
                                     temp = -temp + m_centerPosition;
                                 }
                                 else
                                 {
                                     temp = m_centerPosition - temp;
                                 }

                                 fl.topMargin = (int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f + temp*m_deboost);
                             }
                             else
                             {
                                 temp = temp - m_centerPosition;
                                 fl.topMargin = (int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f -  temp*m_deboost);
                             }
                           itemView.m_img.setLayoutParams(fl);
                        }
                   }
           }
    }


    private int getTextViewTopMargin()
    {
       int out = 0;
        LinearLayout.LayoutParams ll = (LayoutParams) m_textView.getLayoutParams();
        out = ll.topMargin;
        return out;
    }


    public void changeRefreshState(int type)
    {
        switch (type)
        {
            case REFRESH_ING:
                m_textView.setText("正在刷新");
                break;
            case REFRESH_PRE:
                m_textView.setText("下拉刷新");
                break;
            case REFRESH_WILL:
                m_textView.setText("释放刷新");
                break;
        }
    }

    public void setRefreshCB(RefreshImp imp)
    {
        m_cb = imp;
    }

    private void changeImgPosition1()
    {
        int count = m_recyclerView.getChildCount();
        if(count > 0)
        {
            LayoutParams ll = (LayoutParams) m_textView.getLayoutParams();
            int tempTopDistans = ll.topMargin + ShareData.PxToDpi_xhdpi(100);

            for(int i = 0; i < count; i++)
            {
                View view = m_recyclerView.getChildAt(i);
                if(view instanceof ThemeItemView)
                {
                    ThemeItemView itemView = (ThemeItemView) view;
                    FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams)itemView.m_img.getLayoutParams();
//                    int topMargin = (int) ((int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f) - tempTopDistans*0.1f);
                    int curTemp = fl.topMargin;
                    int topMargin = (int) (getStartTopMargin(i) - tempTopDistans*0.2f);
                    fl.topMargin = topMargin;
                    itemView.m_img.setLayoutParams(fl);
                }
            }
        }
    }

    private int getStartTopMargin(int position)
    {
        int out = 0;
//        int temp = ShareData.PxToDpi_xhdpi(380)*position;
        int temp = 0;
        ThemeItemView itemView = (ThemeItemView) m_recyclerView.getChildAt(position);
        if(itemView != null)
        {
            temp = itemView.getTop();
        }
        if(temp < m_centerPosition)
        {
            if(temp < 0)
            {
                temp = -temp + m_centerPosition;
            }
            else
            {
                temp = m_centerPosition - temp;

            }
            out = (int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f + temp*m_deboost);
        }
        else
        {
            temp = temp - m_centerPosition;
            out = (int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f - temp*m_deboost);
        }
        return out;
    }

    public int getScollYDistance(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisiableChildView = layoutManager.findViewByPosition(position);
        if(position == -1)
        {
            return 0;
        }
        else
        {
            int itemHeight = firstVisiableChildView.getHeight();
            return (position) * itemHeight - firstVisiableChildView.getTop();
        }
    }


    public void setLayoutManager(LinearLayoutManager manager)
    {
        m_recyclerView.setLayoutManager(manager);
    }

    public void addItemDecoration(DividerItemDecoration divd)
    {
        m_recyclerView.addItemDecoration(divd);
    }
    public void setAdapter(RecyclerView.Adapter adapter)
    {
        m_recyclerView.setAdapter(adapter);
    }

    public void addOnScrollListener(RecyclerView.OnScrollListener listener)
    {
        m_recyclerView.addOnScrollListener(listener);
    }

    public RecyclerView getRecylerView()
    {
        return m_recyclerView;
    }

    public RecyclerView.Adapter getAdapter()
    {
        return m_recyclerView.getAdapter();
    }

    interface RefreshImp
    {
        public void refresh();

        public void refreshfinish();
    }


    private float m_downY = 0;
    private float m_moveDistance = 0;
    private float m_standard = -ShareData.PxToDpi_xhdpi(100);
    private boolean m_once9;
    private boolean m_flag9;
    private float m_curMoveY = 0;
    public static int m_refreshState = REFRESH_PRE;

    private boolean m_initEndBottom = false;
    OnTouchListener m_onTouch1 = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if(!m_isfinishingAnim)
            {
//                Log.i("AAAA","1");
                return true;
            }
            LayoutParams ll = (LayoutParams) m_textView.getLayoutParams();
            int curTopMargin = ll.topMargin;
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    m_downY = event.getY();
                    if(m_refreshState == REFRESH_ING)
                    {
                        m_flag9 = true;
                    }
                    cancelAnim();
                    break;
                case MotionEvent.ACTION_MOVE:
                    m_curMoveY = event.getY();
                    if(!m_once9)
                    {
                        m_once9 = true;
                        m_downY = event.getY();
                        cancelAnim();
                    }
                    m_moveDistance = event.getY() - m_downY;
                    if(m_moveDistance > 0)
                    {
                        m_moveDistance = (float) Math.ceil(m_moveDistance*0.2f);
                    }
                    else
                    {
                        m_moveDistance = (float) Math.floor(m_moveDistance*0.2f);
                    }
                    m_downY = event.getY();
                    if((int) (curTopMargin + m_moveDistance) > m_standard && m_refreshState == REFRESH_ING)
                    {
                        ll.topMargin = (int) (curTopMargin + m_moveDistance);
                        m_textView.setLayoutParams(ll);
                        m_flag9 = true;
//                        Log.i("AAAA","2");
                        changeImgPosition1();
//                        Log.i("ZZZ","m_moveDy = " + m_moveDy);
                        if((int) (curTopMargin + m_moveDistance) <= -ShareData.PxToDpi_xhdpi(3))
                        fininshAnim1(true);
                    }
                    else if((int) (curTopMargin + m_moveDistance) > m_standard && isVisTop1())
                    {
                        ll.topMargin = (int) (curTopMargin + m_moveDistance);
                        m_textView.setLayoutParams(ll);
                        m_flag9 = true;
//                        Log.i("AAAA","3");
                        changeImgPosition1();
                        if(m_refreshState != REFRESH_ING)
                        {
                            if(ll.topMargin > 0)
                            {
                                m_refreshState = REFRESH_WILL;
                                changeRefreshState(REFRESH_WILL);
                            }
                            else
                            {
                                m_refreshState = REFRESH_PRE;
                                changeRefreshState(REFRESH_PRE);
                            }
                        }
                    }
                    else if((int) (curTopMargin + m_moveDistance) < m_standard && isVisBottom2())
                    {
                        if(!m_initEndBottom)
                        {
                            initEndTopMar();
                            m_initEndBottom = true;
                        }
                        ll.topMargin = (int) (curTopMargin + m_moveDistance);
                        m_textView.setLayoutParams(ll);
                        m_flag9 = true;
//                        Log.i("AAAA","4");
                        changeImgPosition2();
                    }
                    else
                    {
//                        m_refreshState = REFRESH_PRE;
//                        if(ll.topMargin != -ShareData.PxToDpi_xhdpi(100))
//                        {
//                            ll.topMargin = -ShareData.PxToDpi_xhdpi(100);
//                            m_textView.setLayoutParams(ll);
//                            changeImgPosition1();
//                        }
                        m_flag9 = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if(m_flag9)
                    {
                        reSetAnim();
                    }
                    m_flag9 = false;
                    m_once9 = false;
                    m_initEndBottom = false;
                    break;
            }

            LayoutParams ll1 = (LayoutParams) m_textView.getLayoutParams();
            if(ll1.topMargin > -ShareData.PxToDpi_xhdpi(95))
            {
//                Log.i("AAAA","5");
                return true;
            }
//            Log.i("VVV","m_flag9 = " + m_flag9);
            return m_flag9;
        }
    };


    ValueAnimator valueAnimator;
    public void reSetAnim()
    {
        int end = 0;
        final LayoutParams ll = (LayoutParams) m_textView.getLayoutParams();
        int temp = ll.topMargin;
        if(temp >= -ShareData.PxToDpi_xhdpi(100))
        {
            if(temp > 0)
            {
                end = 0;
            }
            else
            {
                end = -ShareData.PxToDpi_xhdpi(100);
            }
        }
        else
        {
            end = -ShareData.PxToDpi_xhdpi(100);
        }

        valueAnimator = ValueAnimator.ofInt(ll.topMargin,end);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ll.topMargin = (int) animation.getAnimatedValue();
                m_textView.setLayoutParams(ll);
                if(ll.topMargin < -ShareData.PxToDpi_xhdpi(100))
                {
                    changeImgPosition2();
                }
                else
                {
                    changeImgPosition1();
                }

            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(ll.topMargin == 0)
                {
                    m_refreshState = REFRESH_ING;
                    changeRefreshState(REFRESH_ING);
                    if(m_cb != null)
                    {
                        m_cb.refresh();
                    }
                    m_once9 = false;
                    m_flag9 = false;
                }
                else
                {
                    m_refreshState = REFRESH_PRE;
                    changeRefreshState(REFRESH_PRE);
                    m_once9 = false;
                    m_flag9 = false;
                    m_recyclerView.stopScroll();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        valueAnimator.start();
    }



    public void resetAll2()
    {
        m_refreshHide = false;
        m_initTopMargin = false;
        m_refreshState = REFRESH_PRE;
        changeRefreshState(REFRESH_PRE);
        m_once9 = false;

        LayoutParams ll = (LayoutParams) m_textView.getLayoutParams();
        ll.topMargin = -ShareData.PxToDpi_xhdpi(100);
        m_textView.setLayoutParams(ll);


        int count = m_recyclerView.getChildCount();
        if(count > 0)
        {
            for(int i = 0; i < count; i++)
            {
                View view = m_recyclerView.getChildAt(i);
                if(view instanceof ThemeItemView)
                {
                    ThemeItemView itemView = (ThemeItemView) view;
                    FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams)itemView.m_img.getLayoutParams();
                    fl.topMargin = (int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f);
                    itemView.m_img.setLayoutParams(fl);
                }
            }
        }

    }


    private boolean m_isfinishingAnim = true;
    public void fininshAnim1(final boolean isfast)
    {
        m_refreshHide = false;
        if(m_refreshState == REFRESH_ING)
        {
            LayoutParams ll = (LayoutParams) m_textView.getLayoutParams();
            ValueAnimator valueAnimator = ValueAnimator.ofInt(ll.topMargin,-ShareData.PxToDpi_xhdpi(100));
            if(isfast)
            {
                valueAnimator.setDuration(150);
            }
            else
            {
                valueAnimator.setDuration(300);
            }
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    LayoutParams ll = (LayoutParams) m_textView.getLayoutParams();
                    ll.topMargin = (int) animation.getAnimatedValue();
                    m_textView.setLayoutParams(ll);

//                    changeImgPosition(-((int)animation.getAnimatedValue() + ShareData.PxToDpi_xhdpi(100)));
                    changeImgPosition1();
                }
            });

            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    m_isfinishingAnim = false;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if(isfast)
                    {
                        m_refreshHide = true;
                    }
                        m_refreshState = REFRESH_PRE;
                        changeRefreshState(REFRESH_PRE);
                    m_once9 = false;
                    m_isfinishingAnim = true;
//                    m_initTopMargin = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
//                    LayoutParams ll = (LayoutParams) m_textView.getLayoutParams();
//                    int temp = ll.topMargin;
//                    if(temp < 0)
//                    {
//                        m_refreshState = REFRESH_PRE;
//                        changeRefreshState(REFRESH_PRE);
//                    }
//                    else
//                    {
//                        m_refreshState = REFRESH_WILL;
//                        changeRefreshState(REFRESH_WILL);
//                    }
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            valueAnimator.start();
        }

    }

    private void cancelAnim()
    {
        if(valueAnimator != null)
        {
            valueAnimator.cancel();
            valueAnimator = null;
        }
    }

    private ArrayList<Integer> m_endArr = new ArrayList();


    private void initEndTopMar()
    {
        m_endArr.clear();
        int count = m_recyclerView.getChildCount();
        if(count > 0)
        {
            int a = count*ShareData.PxToDpi_xhdpi(380) - m_recyclerView.getHeight();
            if(a <= 0)
            {
             for(int i = 0; i < count;i++)
             {
                ThemeItemView itemView = (ThemeItemView) m_recyclerView.getChildAt(i);
                FrameLayout.LayoutParams fl= (FrameLayout.LayoutParams) itemView.m_img.getLayoutParams();
                m_endArr.add(fl.topMargin);
             }
            }
            else
            {
                int startY = 0;
                int out = 0;
                for(int i = 0; i < count; i++)
                {
                    int temp = 0;
                    if(i == 0)
                    {
                        temp = -a;
                    }
                    else
                    {
                        temp = (ShareData.PxToDpi_xhdpi(380) - a) + ShareData.PxToDpi_xhdpi(380)*(i - 1);
                    }

                    if(temp < m_centerPosition)
                    {
                        if(temp < 0)
                        {
                            temp = -temp + m_centerPosition;
                        }
                        else
                        {
                            temp = m_centerPosition - temp;

                        }
                        out = (int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f + temp*m_deboost);
                    }
                    else
                    {
                        temp = temp - m_centerPosition;
                        out = (int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f - temp*m_deboost);
                    }
                    m_endArr.add(out);
                }
            }
        }
    }

    private void changeImgPosition2()
    {
        int count = m_recyclerView.getChildCount();
        if(count > 0)
        {
            LayoutParams ll = (LayoutParams) m_textView.getLayoutParams();
            int tempTopDistans = ll.topMargin + ShareData.PxToDpi_xhdpi(100);

            for(int i = count - 1; i >= 0; i--)
            {
                View view = m_recyclerView.getChildAt(i);
                if(view instanceof ThemeItemView)
                {
                    ThemeItemView itemView = (ThemeItemView) view;
                    FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams)itemView.m_img.getLayoutParams();
//                    int topMargin = (int) ((int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f) - tempTopDistans*0.1f) + (int)(getScollYDistance(m_recyclerView)*0.06f);
                    int temp = 0;
                    int index = count - 1 - i;
                    int size = m_endArr.size();
                    if(m_endArr != null && m_endArr.size() > 0 && (size - index - 1) < m_endArr.size() && (size - index - 1) >= 0)
                    {
                        temp = m_endArr.get(size - index - 1);
                    }
                    int topMargin = (int) (temp - tempTopDistans*0.2f);
                    fl.topMargin = topMargin;
                    itemView.m_img.setLayoutParams(fl);
                }
            }
        }
    }
    private void initTopMargin()
    {
        int count = m_recyclerView.getHeight()/ShareData.PxToDpi_xhdpi(380);
        LinearLayoutManager manager = (LinearLayoutManager) m_recyclerView.getLayoutManager();
        int visiLastIndex = manager.findLastVisibleItemPosition();
        int childCount = m_recyclerView.getChildCount();
//        Log.i("iii","childCount = " + childCount);
//        Log.i("iii","visiLastIndex = " + visiLastIndex);
        if(childCount > 0 && m_centerPosition != 0 && visiLastIndex >= 0)
        {
            for(int i = 0 ;i < childCount;i++)
            {
                if(i <= count)
                {
                    View view = m_recyclerView.getChildAt(i);
                    if(view instanceof ThemeItemView)
                    {
                        ThemeItemView itemView = (ThemeItemView) view;
                        FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams)itemView.m_img.getLayoutParams();
//                           fl.topMargin = (int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f) + (int)(getScollYDistance(recyclerView)*m_deboost);
                        int temp = ShareData.PxToDpi_xhdpi(380)*i;
//                        Log.i("iii","m_centerPosition == " + m_centerPosition);
                        if(temp < m_centerPosition)
                        {
                            if(temp < 0)
                            {
                                temp = -temp + m_centerPosition;
                            }
                            else
                            {
                                temp = m_centerPosition - temp;
                            }

                            fl.topMargin = (int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f + temp*m_deboost);
                        }
                        else
                        {
                            temp = temp - m_centerPosition;
                            fl.topMargin = (int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f -  temp*m_deboost);
                        }

                        if(i == 0)
                        {
//                            Log.i("iii","0TopMargin = " + fl.topMargin);
                        }
                        itemView.m_img.setLayoutParams(fl);
                        m_initTopMargin = true;
                    }
                }
            }
        }
    }

    public boolean isVisTop1()
    {
        LinearLayoutManager layoutManager = (LinearLayoutManager) m_recyclerView.getLayoutManager();
        //屏幕中最后一个可见子项的position
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        View view = layoutManager.findViewByPosition(firstVisibleItemPosition);
        //当前屏幕所看到的子项个数
        int visibleItemCount = layoutManager.getChildCount();
        //当前RecyclerView的所有子项个数
        int totalItemCount = layoutManager.getItemCount();
        //RecyclerView的滑动状态
        int state = m_recyclerView.getScrollState();

        int count = m_recyclerView.getChildCount();


        if(visibleItemCount > 0 && firstVisibleItemPosition == 0 && view.getTop() >= 0)
        {
            return true;
        }
        else if(count == 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    public boolean isVisBottom2(){
        LinearLayoutManager layoutManager = (LinearLayoutManager) m_recyclerView.getLayoutManager();
        //屏幕中最后一个可见子项的position
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        View view = layoutManager.findViewByPosition(lastVisibleItemPosition);
        //当前屏幕所看到的子项个数
        int visibleItemCount = layoutManager.getChildCount();
        //当前RecyclerView的所有子项个数
        int totalItemCount = layoutManager.getItemCount();
        //RecyclerView的滑动状态
        int state = m_recyclerView.getScrollState();
        if(visibleItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1 && view.getBottom() <= m_recyclerView.getHeight()){
            return true;
        }else {
            return false;
        }
    }


}

package cn.poco.MaterialMgr2;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import cn.poco.draglistview.DragItem;
import cn.poco.draglistview.DragItemAdapter;
import cn.poco.resource.BaseRes;
import cn.poco.resource.FilterRes;
import cn.poco.resource.LightEffectRes;
import cn.poco.resource.TextRes;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.GlideImageLoader;

/**
 * Created by admin on 2016/9/9.
 */
public class ManageListAdapter extends DragItemAdapter<BaseRes, ManageListAdapter.ManageViewHolder>
{
	private Context m_context;
	private int m_thumbSize;
	private ClickListener m_listener;
	public ManageListAdapter(Context context)
	{
		super(true);
		setHasStableIds(true);
		m_context = context;
		m_thumbSize = ShareData.PxToDpi_xhdpi(80);
	}

	public void SetClickListener(ClickListener lis)
	{
		m_listener = lis;
	}

	public void ClearCache()
	{
		if(mItemList != null)
		{
			mItemList.clear();
		}
		GlideImageLoader.Clear(m_context);
	}

	@Override
	public ManageViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		ManageItemView view = new ManageItemView(m_context);
		RecyclerView.LayoutParams rl = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		view.setLayoutParams(rl);
		ManageViewHolder holder = new ManageViewHolder(view, view.m_dragIcon);
		return holder;
	}

	@Override
	public void onBindViewHolder(ManageViewHolder holder, int position)
	{
		super.onBindViewHolder(holder, position);
		final ManageItemView view = holder.m_view;
		BaseRes res = mItemList.get(position);
		String key = "";
		if(view != null)
		{


			view.SetData(res);
		/*	if(res instanceof TextRes)
			{
				view.Scan(!((TextRes)res).m_isHide);
			}
			else if(res instanceof FilterRes)
			{
				view.Scan(!((FilterRes)res).m_isHide);
			}
			else if(res instanceof LightEffectRes)
			{
				view.Scan(!((LightEffectRes)res).m_isHide);
			}*/

		if (res.m_type ==  BaseRes.TYPE_LOCAL_RES){
			view.Scan(false);
			view.m_scanIcon.setVisibility(View.GONE);
		}else {
			view.m_scanIcon.setVisibility(View.VISIBLE);
		}
			GlideImageLoader.LoadRoundImg(view.m_thumb, m_context, (String)res.m_thumb, ShareData.PxToDpi_xhdpi(10), false);
			view.m_title.setText(res.m_name);
		}

	}

	@Override
	public long getItemId(int position)
	{
		return mItemList.get(position).m_id;
	}

	class ManageViewHolder extends DragItemAdapter.ViewHolder
	{
		ManageItemView m_view;
		public ManageViewHolder(View itemView, View grabView)
		{
			super(itemView, grabView);
			m_view = (ManageItemView)itemView;
			m_view.m_scanIcon.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if(m_listener != null)
					{
						m_listener.onHideBtn(m_view, m_view.GetData(), getAdapterPosition());
					}
				}
			});
		}
	}

	static class ManageDragItem extends DragItem
	{
		private float m_touchOffsetX;

		public ManageDragItem(Context context)
		{
			super(context);
		}

		public ManageDragItem(Context context, int layoutId)
		{
			super(context, layoutId);
		}

		public ManageDragItem(Context context, View view)
		{
			super(context, view);
		}

		@Override
		protected void setPosition(float touchX, float touchY)
		{
			touchX = touchX - m_touchOffsetX;
			super.setPosition(touchX, touchY);
		}

		@Override
		protected void startDrag(View parent, View startFromView, float touchX, float touchY)
		{
			m_touchOffsetX = touchX - startFromView.getMeasuredWidth() / 2 - 20;
			show();
			onBindDragView(startFromView, mDragView);
			onMeasureDragView(startFromView, mDragView);
			onStartDragAnimation(mDragView);

			float parentX = 0;
			float parentY = 0;

			if(parent != null)
			{
				parentX = parent.getX();
				parentY = parent.getY();
			}

			float startX = parentX + startFromView.getX() - (mDragView.getMeasuredWidth() - startFromView.getMeasuredWidth()) / 2 + mDragView
					.getMeasuredWidth() / 2;
			float startY = parentY + startFromView.getY() - (mDragView.getMeasuredHeight() - startFromView.getMeasuredHeight()) / 2 + mDragView
					.getMeasuredHeight() / 2;

			if (mSnapToTouch) {
				mPosTouchDx = 0;
				mPosTouchDy = 0;
				setPosition(touchX, touchY);
				touchX = touchX - m_touchOffsetX;
				setAnimationDx(startX - touchX);
				setAnimationDY(startY - touchY);

				PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("AnimationDx", mAnimationDx, 0);
				PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("AnimationDY", mAnimationDy, 0);
				ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, pvhX, pvhY);
				anim.setInterpolator(new DecelerateInterpolator());
				anim.setDuration(ANIMATION_DURATION);
				anim.start();
			} else {
				mPosTouchDx = startX - touchX;
				mPosTouchDy = startY - touchY;
				setPosition(touchX, touchY);
			}
		}
	}

	public static interface ClickListener
	{
		public void onHideBtn(View view, BaseRes res, int position);
	}
}

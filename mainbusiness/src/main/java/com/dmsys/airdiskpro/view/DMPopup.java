package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;

import com.dmsys.mainbusiness.R;

public class DMPopup extends PopupWindows implements OnDismissListener {
	private View mRootView;
	private ImageView mArrowUp;
	private ImageView mArrowDown;
	private LayoutInflater mInflater;
	private ViewGroup mTrack;
	private LinearLayout mScroller;
	private OnPopuItemClickListener mItemClickListener;
	private OnDismissListener mDismissListener;
	private Context context;
	
	private int mChildPos;
    private int mInsertPos;
    private int mAnimStyle;
    private int mOrientation;
    private int rootWidth=0;
    
    private int curPos;
    
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    
    public static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_CENTER = 3;
	public static final int ANIM_REFLECT = 4;
	public static final int ANIM_AUTO = 5;
	
    /**
     * Constructor for default vertical layout
     * 
     * @param context  Context
     */
    public DMPopup(Context context) {
        this(context, VERTICAL);
    }

    /**
     * Constructor allowing orientation override
     * 
     * @param context    Context
     * @param orientation Layout orientation, can be vartical or horizontal
     */
    public DMPopup(Context context, int orientation) {
        super(context);
        this.context = context;
        
        mOrientation = orientation;
        
        mInflater  = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (mOrientation == HORIZONTAL) {
            setRootViewId(R.layout.popup_horizontal);
        } else {
            setRootViewId(R.layout.popup_vertical);
        }

        mAnimStyle 	= ANIM_AUTO;
        mChildPos 	= 0;
    }

	/**
	 * Set root view.
	 * 
	 * @param id Layout resource id
	 */
	public void setRootViewId(int id) {
		mRootView	= (ViewGroup) mInflater.inflate(id, null);
		mTrack 		= (ViewGroup) mRootView.findViewById(R.id.tracks);

		mArrowDown 	= (ImageView) mRootView.findViewById(R.id.arrow_down);
		mArrowUp 	= (ImageView) mRootView.findViewById(R.id.arrow_up);

		mScroller	= (LinearLayout) mRootView.findViewById(R.id.scroller);
		
		//This was previously defined on show() method, moved here to prevent force close that occured
		//when tapping fastly on a view to show quickaction dialog.
		//Thanx to zammbi (github.com/zammbi)
		mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		//this is make of user
		setContentView(mRootView);
	}
	
	/**
	 * Set animation style
	 * 
	 * @param mAnimStyle animation style, default is set to ANIM_AUTO
	 */
	public void setAnimStyle(int mAnimStyle) {
		this.mAnimStyle = mAnimStyle;
	}
	
	/**
	 * Set listener for action item clicked.
	 * 
	 * @param listener Listener
	 */
	public void setOnPopuItemClickListener(OnPopuItemClickListener listener) {
		mItemClickListener = listener;
	}
	
	public int getCurIndex(){
		return curPos;
	}
	
	/*
	 * 添加一个ListView
	 */
	public void addView(View container) {
		if(container== null) return;
		mTrack.setBackgroundResource(Color.TRANSPARENT);
		mTrack.addView(container, mInsertPos);
		mChildPos++;
		mInsertPos++;
	}
	
	public void show(View anchor,View defDisplayView) {
		if(mTrack.getChildCount() == 0) {
	         mTrack.setBackgroundResource(Color.TRANSPARENT);
	         mTrack.addView(defDisplayView);
		}
		show (anchor);
		
	}
	//设置盘popuwindows 的宽度和长度
	public void setLayoutParams(int width,int height) {
		super.setLayoutParams(width, height);
	}
	
	/**
	 * Show quickaction popup. Popup is automatically positioned, on top or bottom of anchor view.
	 * 
	 */
	public void show (View anchor) {
		preShow();
		int xPos, yPos, arrowPos;
		int[] location 		= new int[2];
		
		//获取显示在指定控件的左上角的（x,y）
		anchor.getLocationOnScreen(location);

		Rect anchorRect 	= new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] 
		                	+ anchor.getHeight());

		//mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	
		int rootHeight 		= mRootView.getMeasuredHeight();
		
		if (rootWidth == 0) {
			rootWidth		= mRootView.getMeasuredWidth();
		}
		
		int screenWidth 	= mWindowManager.getDefaultDisplay().getWidth();
		int screenHeight	= mWindowManager.getDefaultDisplay().getHeight();
		//显示在指定控件的上方高度，也就是左上角（x，y）y的值
		int dyTop			= anchorRect.top;
		//显示在指定控件的下方高度，也就是 屏幕高度 减去     右下角（x，y）y的值
		int dyBottom		= screenHeight - anchorRect.bottom;
		//上方的空间比下方大，所以判断是否显示在上方

		boolean onTop		= (dyTop > dyBottom) ? true : false;
		
		//automatically get X coord of popup (top left)
		if ((anchorRect.left + rootWidth) > screenWidth) {
			//anchor的右下角（x,y）的x值,减去要显示的popup窗口的宽度
			xPos = anchorRect.left - (rootWidth - anchor.getWidth());
			xPos = (xPos < 0) ? 0 : xPos;
			//arrowPos这个值不必理会
			arrowPos = anchorRect.centerX() - xPos;

		} else {
			//如果控件宽度大于显示的popup窗口宽度
			if (anchor.getWidth() > rootWidth) {
				
				//做向上伸展的动画动画开始位置与 anchor的右上角 坐标对齐
				if(onTop) {
					xPos = anchorRect.left - (rootWidth - anchor.getWidth());
					xPos = (xPos < 0) ? 0 : xPos;
				} else {
					// 做向下伸展的动画 动画开始位置和anchor第一个控件对齐 
					xPos = anchor.getPaddingLeft();
				}
				// 动画出现位置和anchor的正中央对齐
				// xPos = anchorRect.centerX() - (rootWidth/2);
			} else {
				xPos = anchorRect.left;
			}

			arrowPos = anchorRect.centerX() - xPos;
		}

		

		if (onTop) {
			if (rootHeight > dyTop) {
				yPos = 15;
			} else {
				yPos = anchorRect.top - rootHeight;
			}
		} else {
			yPos = anchorRect.bottom;
		}
		//设置popup的开口方向小图标，这里不需要这种小图标
//		showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);
		
		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}
	
	
	/**
	 * Show arrow
	 * 
	 * @param whichArrow arrow type resource id
	 * @param requestedX distance from left screen
	 */
	private void showArrow(int whichArrow, int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

        final int arrowWidth = mArrowUp.getMeasuredWidth();

        showArrow.setVisibility(View.VISIBLE);
        
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();
       
        param.leftMargin = requestedX - arrowWidth / 2;
        
        hideArrow.setVisibility(View.INVISIBLE);
    }
	
	/**
	 * Set listener for window dismissed. This listener will only be fired if the quicakction dialog is dismissed
	 * by clicking outside the dialog or clicking on sticky item.
	 */
	public void setOnDismissListener(OnDismissListener listener) {
		setOnDismissListener(this);
		
		mDismissListener = listener;
	}
	
	@Override
	public void onDismiss() {
		if (mDismissListener != null) {
			mDismissListener.onDismiss();
		}
	}
	
	/**
	 * Listener for item click
	 *
	 */
	public interface OnPopuItemClickListener {
		public void onItemClick(DMPopup source, int pos, int actionId);
	}
	
	/**
	 * Listener for window dismiss
	 * 
	 */
	public interface OnDismissListener {
		public void onDismiss();
	}
}
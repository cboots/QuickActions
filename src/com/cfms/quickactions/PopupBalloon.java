package com.cfms.quickactions;

import com.cfms.podfusion.DebugLog;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Popup window that can enclose any content view in a popup balloon style tool tip.
 * 
 */
public class PopupBalloon extends CustomPopupWindow {
	private final View root;
	private final ImageView mArrowUp;
	private final ImageView mArrowDown;
	private final FrameLayout mContentLayout;
	private final LayoutInflater inflater;
	private final Context context;
	
	public static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_CENTER = 3;
	public static final int ANIM_AUTO = 4;
	
	private static final int OVERLAP = 8;
	private static final String TAG = "PopupBalloon";
	
	private int animStyle;
	
	/**
	 * Constructor.
	 *
	 * @param anchor  {@link View} on where the popup should be displayed
	 * @param touchToDismiss If true, touching anywhere on the screen will dismiss the popup.
	 */
	public PopupBalloon(View anchor, boolean touchToDismiss) {
		super(anchor);
		context		= anchor.getContext();
		inflater 	= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		root		= (ViewGroup) inflater.inflate(R.layout.popup_balloon, null);
		
		mArrowDown 	= (ImageView) root.findViewById(R.id.arrow_down);
		mArrowUp 	= (ImageView) root.findViewById(R.id.arrow_up);
		mContentLayout = (FrameLayout) root.findViewById(R.id.content_layout);
		setContentView(root);
		
		if(touchToDismiss)
		{
			// when a touch event happens anywhere
			// make the window go away
			window.setTouchInterceptor(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_UP) {
						close();
						return true;
					}
					
					return false;
				}
			});
		}
	}

	
	/**
	 * Set animation style
	 * 
	 * @param animStyle animation style, default is set to ANIM_AUTO
	 */
	public void setAnimStyle(int animStyle) {
		this.animStyle = animStyle;
	}

	/**
	 * Sets the content view of the popup.  Can be anything reasonable.  Transparent backgrounds recommended
	 *
	 * @param content the new content.  Old view will be removed
	 */
	public void setContent(View content)
	{
		this.mContentLayout.removeAllViews();
		this.mContentLayout.addView(content, 
				new FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.FILL_PARENT,
						FrameLayout.LayoutParams.FILL_PARENT));		
	}
	

	
	/**
	 * Show popup window
	 */
	public void show () {
		preShow();

		int[] location 		= new int[2];
		
		anchor.getLocationOnScreen(location);

		Rect anchorRect 	= new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] 
		                	+ anchor.getHeight());

		root.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		int rootWidth 		= root.getMeasuredWidth();
		int rootHeight 		= root.getMeasuredHeight();

		int screenWidth 	= windowManager.getDefaultDisplay().getWidth();
		//int screenHeight 	= windowManager.getDefaultDisplay().getHeight();

		
		int xPos 			= anchorRect.centerX() - rootWidth/2;
		if(xPos + rootWidth > screenWidth)
		{
			xPos = screenWidth - rootWidth;
		}
		if(xPos < 0)
		{
			xPos = 0;
		}
		
		
		int yPos	 		= anchorRect.top - rootHeight + OVERLAP;

		boolean onTop		= true;
		
		// display on bottom
		if (rootHeight > anchorRect.top) {
			yPos 	= anchorRect.bottom - OVERLAP;
			onTop	= false;
		}
		DebugLog.i(TAG, "XPos: "+xPos);
		showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), anchorRect.centerX()-xPos);
		
		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
		
		window.showAtLocation(this.anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}
	
	/**
	 * Set animation style
	 * 
	 * @param screenWidth Screen width
	 * @param requestedX distance from left screen
	 * @param onTop flag to indicate where the popup should be displayed. Set TRUE if displayed on top of anchor and vice versa
	 */
	private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
		int arrowPos = requestedX - mArrowUp.getMeasuredWidth()/2;

		switch (animStyle) {
		case ANIM_GROW_FROM_LEFT:
			window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
			break;
					
		case ANIM_GROW_FROM_RIGHT:
			window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
			break;
					
		case ANIM_GROW_FROM_CENTER:
			window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
		break;
					
		case ANIM_AUTO:
			if (arrowPos <= screenWidth/4) {
				window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
			} else if (arrowPos > screenWidth/4 && arrowPos < 3 * (screenWidth/4)) {
				window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
			} else {
				window.setAnimationStyle((onTop) ? R.style.Animations_PopDownMenu_Right : R.style.Animations_PopDownMenu_Right);
			}
					
			break;
		}
	}
	
	/**
	 * Show arrow
	 * 
	 * @param whichArrow arrow type resource id
	 * @param requestedX distance from left side of popup
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
}
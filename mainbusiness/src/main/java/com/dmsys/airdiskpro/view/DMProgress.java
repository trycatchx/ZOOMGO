package com.dmsys.airdiskpro.view;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ProgressBar;

public class DMProgress extends ProgressBar{
	 private static final Interpolator DEFAULT_INTERPOLATER = new AccelerateDecelerateInterpolator();

	    private ValueAnimator animator;
	    private ValueAnimator animatorSecondary;
	    private boolean animate = true;
	    private int lastProgress = 0;

	    public DMProgress(Context context, AttributeSet attrs, int defStyle) {
	        super(context, attrs, defStyle);
	    }

	    public DMProgress(Context context, AttributeSet attrs) {
	        super(context, attrs);
	    }

	    public DMProgress(Context context) {
	        super(context);
	    }

	    public boolean isAnimate() {
	        return animate;
	    }

	    public void setAnimate(boolean animate) {
	        this.animate = animate;
	    }

	    @Override
	    public synchronized void setProgress(int progress) {
	        if (!animate) {
	            super.setProgress(progress);
	            return;
	        }
	        if (animator != null) {
	        	animator.cancel();
	        }
	        if(progress == getMax()) {
	        	DMProgress.super.setProgress(progress);
	        	return;
	        }
	            
	        if (animator == null) {
	            animator = ValueAnimator.ofInt(getProgress(), progress);
	            animator.setInterpolator(DEFAULT_INTERPOLATER);
	            animator.addUpdateListener(new AnimatorUpdateListener() {

	                @Override
	                public void onAnimationUpdate(ValueAnimator animation) {
	                	DMProgress.super.setProgress((Integer) animation.getAnimatedValue());
	                }
	            });
	        } else {
	            animator.setIntValues(getProgress(), progress);
	        }
	       
	        animator.start();

	    }

	    @Override
	    public synchronized void setSecondaryProgress(int secondaryProgress) {
	        if (!animate) {
	            super.setSecondaryProgress(secondaryProgress);
	            return;
	        }
	        if (animatorSecondary != null)
	            animatorSecondary.cancel();
	        if (animatorSecondary == null) {
	            animatorSecondary = ValueAnimator.ofInt(getProgress(), secondaryProgress);
	            animatorSecondary.setInterpolator(DEFAULT_INTERPOLATER);
	            animatorSecondary.addUpdateListener(new AnimatorUpdateListener() {

	                @Override
	                public void onAnimationUpdate(ValueAnimator animation) {
	                	DMProgress.super.setSecondaryProgress((Integer) animation
	                            .getAnimatedValue());
	                }
	            });
	            
	        } else
	            animatorSecondary.setIntValues(getProgress(), secondaryProgress);
	        animatorSecondary.start();
	    }

	    @Override
	    protected void onDetachedFromWindow() {
	        super.onDetachedFromWindow();
	        if (animator != null)
	            animator.cancel();
	        if (animatorSecondary != null)
	            animatorSecondary.cancel();
	    }
}

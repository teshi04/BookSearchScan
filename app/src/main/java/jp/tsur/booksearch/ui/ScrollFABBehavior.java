package jp.tsur.booksearch.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class ScrollFABBehavior extends FloatingActionButton.Behavior {

    private static final Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static final int DURATION_TIME = 200;
    private int translationY;
    private boolean animatingRunning;

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                       FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {

        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                        nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                               View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        if (dyConsumed > 0 && !animatingRunning) {
            animateOut(child);
        } else if (dyConsumed < 0 && !animatingRunning) {
            animateIn(child);
        }
    }

    private void animateOut(View view) {
        if (translationY == 0) {
            translationY = getTranslationY(view);
        }

        view.animate()
                .translationY(translationY)
                .setDuration(DURATION_TIME)
                .setInterpolator(INTERPOLATOR)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        animatingRunning = false;
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        animatingRunning = true;
                    }
                });
    }

    private void animateIn(View view) {
        view.animate()
                .translationY(0)
                .setDuration(DURATION_TIME)
                .setInterpolator(INTERPOLATOR)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        animatingRunning = false;
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        animatingRunning = true;
                    }
                });
    }

    private int getTranslationY(View view) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        return view.getHeight() + lp.bottomMargin;
    }
}

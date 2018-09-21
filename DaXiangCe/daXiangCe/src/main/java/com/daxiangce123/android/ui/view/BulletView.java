package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.data.CommentEntity;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.BulletManager;
import com.daxiangce123.android.util.Utils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import de.hdodenhof.circleimageview.CircleImageView;

public class BulletView extends LinearLayout {
    private CircleImageView avater;
    private TextView comment;
    private static final int TRANSLATE_DURATION_MILLIS = 1100;
    public boolean isShowInscreen = false;
    private static final int TRANSLATE_WAIT_MILLIS = 2000;
    private int apperPostion = 0;

    public BulletView(Context context) {
        super(context);
        initUi();
    }

    public BulletView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUi();
    }

    public BulletView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initUi();
    }

    private void initUi() {
        setOrientation(LinearLayout.HORIZONTAL);
        LayoutInflater.from(getContext()).inflate(R.layout.bullet_item, this, true);
        int padding = Utils.getDip(3);
        setPadding(padding, padding, padding, padding);
        comment = (TextView) findViewById(R.id.tv_bullet_content);
        avater = (CircleImageView) findViewById(R.id.civ_bullet);
        ViewHelper.setAlpha(this, 0);
    }

    public void setCommentEntity(CommentEntity comment) {
        apperPostion = (int) (BulletManager.translateY * 1.5);
        isShowInscreen = true;
        this.comment.setText(comment.getMsg());
        ImageManager.instance().loadAvater(avater, comment.getUserId());
        ViewHelper.setTranslationY(this, -apperPostion);
        requestLayout();
        showAnimation();
        setTag(comment);
    }

    public void showAnimation() {
        ViewPropertyAnimator.animate(this).setDuration(TRANSLATE_DURATION_MILLIS).setStartDelay(1500).translationYBy(apperPostion).alpha(1f).setInterpolator(new DecelerateInterpolator(0.5f))
                .setListener(new AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hideAnimation();
                        BulletManager.instance().removeAnmatingInScreen();
                        BulletManager.instance().popAnimation();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        // TODO Auto-generated method stub

                    }
                });
    }

    public void hideAnimation() {
        ViewPropertyAnimator.animate(this).setDuration(TRANSLATE_DURATION_MILLIS - 400).setStartDelay(TRANSLATE_WAIT_MILLIS).translationYBy(-getHeight() + BulletManager.translateY).alpha(0f)
                .setInterpolator(new AccelerateInterpolator()).setListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isShowInscreen = false;

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub

            }
        });
    }
    
}

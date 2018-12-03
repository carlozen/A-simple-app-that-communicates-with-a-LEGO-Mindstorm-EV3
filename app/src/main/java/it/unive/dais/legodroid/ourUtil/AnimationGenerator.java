package it.unive.dais.legodroid.ourUtil;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public final class AnimationGenerator {

    public static void setFadingAnimation(View view) {

        Animation fadeIn =  new AlphaAnimation(0.3f,1.0f);
        Animation fadeOut =  new AlphaAnimation(1.0f,0.3f);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animation.setStartOffset(0);
                animation.setDuration(1200);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(fadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animation.setStartOffset(1000);
                animation.setDuration(1200);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(fadeOut);

    }

    public static void setGlowingAnimation(View view, GradientDrawable shape) {

        shape.setColor(Color.RED);
        view.setBackground(shape);
        Animation redIn =  new AlphaAnimation(0.7f,1.0f);
        Animation redOut =  new AlphaAnimation(1.0f,0.7f);
        redIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animation.setStartOffset(0);
                animation.setDuration(1200);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(redOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        redOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animation.setStartOffset(1000);
                animation.setDuration(1200);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(redIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(redOut);
    }
}

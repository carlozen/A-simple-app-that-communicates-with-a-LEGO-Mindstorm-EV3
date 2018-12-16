package it.unive.dais.legodroid.ourUtil;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

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

    public static void translationAnimation (View view, int newX, int newY) {
       /* TranslateAnimation translateAnimation = new TranslateAnimation(view.getX(), newX, view.getY(), newY);
        translateAnimation.setDuration(1000);
        translateAnimation.setFillAfter(true);
        view.setAnimation(translateAnimation);
        view.startAnimation(translateAnimation);
        */

       Point startingPoint = new Point((int)view.getX(), (int)view.getY());
       Point point = new Point(newX,newY);

       // ObjectAnimator centerChangeAnim = ObjectAnimator.ofObject(view, "centerpoint", new PointEvaluator<Point>(), fromPoint, toPoint);
       // centerChangeAnim.start();

        class PointEvaluator implements TypeEvaluator<Point> {
            @Override
            public Point evaluate(float t, Point startPoint, Point endPoint) {
                int x = (int) (startPoint.x + t * (endPoint.x - startPoint.x));
                int y = (int) (startPoint.y + t * (endPoint.y - startPoint.y));
                return new Point(x,y);
            }
        }

    }

    public static void setGlowingAnimation(View view) {

        view.setBackgroundColor(Color.RED);
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

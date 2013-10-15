package com.google.gode.yadview_harness;

import android.content.Context;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewSwitcher;

import com.google.code.yadview.DayView;
import com.google.code.yadview.DayViewOnCreateContextMenuListener;
import com.google.code.yadview.DayViewOnKeyListener;
import com.google.code.yadview.DayViewOnLongClickListener;
import com.google.code.yadview.EventResource;
import com.google.code.yadview.events.ShowDateInCurrentViewEvent;
import com.google.code.yadview.events.ShowDateInDayViewEvent;
import com.google.code.yadview.impl.DefaultDayViewFactory;
import com.google.code.yadview.impl.DefaultDayViewResources;
import com.google.code.yadview.impl.DefaultUtilFactory;
import com.google.common.eventbus.Subscribe;
import com.google.gode.yadview_harness.AlternateEventRenderer.AlternateRendererDayViewResources;

public class YadviewHarnessDayViewFactory extends DefaultDayViewFactory {

    private Animation mInAnimationForward;
    private Animation mOutAnimationForward;
    private Animation mInAnimationBackward;
    private Animation mOutAnimationBackward;

    public YadviewHarnessDayViewFactory(ViewSwitcher vs, EventResource eventResource, Context context) {
        super(vs, eventResource, context);
        
        mInAnimationForward = AnimationUtils.loadAnimation(context, R.anim.slide_left_in);
        mOutAnimationForward = AnimationUtils.loadAnimation(context, R.anim.slide_left_out);
        mInAnimationBackward = AnimationUtils.loadAnimation(context, R.anim.slide_right_in);
        mOutAnimationBackward = AnimationUtils.loadAnimation(context, R.anim.slide_right_out);


    }
    
    @Override
    public View makeView() {
        DefaultDayViewResources resources = new AlternateRendererDayViewResources(getContext());
        DefaultUtilFactory utilFactory = new DefaultUtilFactory("yadview_harness.prefs");
        DayView dv = new DayView(getContext(),getViewSwitcher(), getEventLoader(), 1, utilFactory, resources, new  AlternateEventRenderer(getContext(), resources,utilFactory));
        dv.setLayoutParams(new ViewSwitcher.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        
        dv.setOnCreateContextMenuListener(new DayViewOnCreateContextMenuListener(getContext(), dv, utilFactory, resources, getEventResource()));
        dv.setOnLongClickListener(new DayViewOnLongClickListener(getContext(), dv, resources, utilFactory));
        dv.setOnKeyListener(new DayViewOnKeyListener(getContext(), dv));

        
        
        dv.getEventBus().register(this);
        
        return dv;
    }
    
    @Subscribe
    public void handleShowDateEvent(ShowDateInCurrentViewEvent e){
        goTo(e.getShowDate(), e.getShowTime());
    }
    
    @Subscribe
    public void handleShowDateEvent(ShowDateInDayViewEvent e){
        goTo(e.getSelectedTime(), e.getSelectedTime());
    }
    
    private void goTo(Time showDate, Time showTime) {
        if (getViewSwitcher() == null) {
            return;
        }

        DayView currentView = (DayView) getViewSwitcher().getCurrentView();

        // How does goTo time compared to what's already displaying?
        int diff = currentView.compareToVisibleTimeRange(showTime);

        if (diff == 0) {
            // In visible range. No need to switch view
            currentView.setSelected(showTime, true, false);
        } else {
            // Figure out which way to animate
            if (diff > 0) {
                getViewSwitcher().setInAnimation(mInAnimationForward);
                getViewSwitcher().setOutAnimation(mOutAnimationForward);
            } else {
                getViewSwitcher().setInAnimation(mInAnimationBackward);
                getViewSwitcher().setOutAnimation(mOutAnimationBackward);
            }

            DayView next = (DayView) getViewSwitcher().getNextView();
            if (true) {
                next.setFirstVisibleHour(currentView.getFirstVisibleHour());
            }

            next.setSelected(showTime, true, false);
            next.reloadEvents();
            getViewSwitcher().showNext();
            next.requestFocus();
            next.updateTitle();
            next.restartCurrentTimeUpdates();        
    }

    

    }
    
}
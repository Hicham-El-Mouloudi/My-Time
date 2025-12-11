package com.ensao.mytime.home.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;

import com.ensao.mytime.home.model.InvocationData;
import com.ensao.mytime.home.model.Quote;
import com.ensao.mytime.home.model.SingleInvocation;
import com.ensao.mytime.home.repository.ExternalContentInterface;
import com.ensao.mytime.home.repository.RetrofitQuoteService;

import java.util.Calendar;
import java.util.List;

public class MotivationViewModel extends AndroidViewModel {

    private final ExternalContentInterface repository;
    private final SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable hideBadgeRunnable;

    private final MutableLiveData<Quote> _quote = new MutableLiveData<>();
    public LiveData<Quote> quote = _quote;

    private final MutableLiveData<Boolean> _isInvocationTime = new MutableLiveData<>();
    public LiveData<Boolean> isInvocationTime = _isInvocationTime;

    public MotivationViewModel(Application application) {
        super(application);
        this.repository = new RetrofitQuoteService(application.getApplicationContext());
        this.prefs = application.getSharedPreferences("UserSettings", Context.MODE_PRIVATE);
        checkInvocationTime();
    }

    public void loadDailyQuote() {
        repository.getDailyQuote(new ExternalContentInterface.QuoteCallback() {
            @Override
            public void onSuccess(Quote loadedQuote) {
                _quote.setValue(loadedQuote);
            }

            @Override
            public void onFailure(String errorMessage) {
                _quote.setValue(null);
            }
        });
    }

    public void checkInvocationTime() {
        if (hideBadgeRunnable != null) {
            handler.removeCallbacks(hideBadgeRunnable);
        }

        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);

        int morningStart = prefs.getInt(InvocationData.KEY_MORNING_START_HOUR, InvocationData.DEFAULT_MORNING_START);
        int morningEnd = prefs.getInt(InvocationData.KEY_MORNING_END_HOUR, InvocationData.DEFAULT_MORNING_END);
        int eveningStart = prefs.getInt(InvocationData.KEY_EVENING_START_HOUR, InvocationData.DEFAULT_EVENING_START);
        int eveningEnd = prefs.getInt(InvocationData.KEY_EVENING_END_HOUR, InvocationData.DEFAULT_EVENING_END);

        boolean shouldShowBadge = false;
        long delayMillis = -1;

        if (currentHour >= morningStart && currentHour < morningEnd) {
            if (!areInvocationsCompletedForPeriod(InvocationData.KEY_LAST_MORNING_SESSION_END)) {
                shouldShowBadge = true;
                delayMillis = calculateDelay(now, morningEnd);
            }
        } else if (currentHour >= eveningStart && currentHour < eveningEnd) {
            if (!areInvocationsCompletedForPeriod(InvocationData.KEY_LAST_EVENING_SESSION_END)) {
                shouldShowBadge = true;
                delayMillis = calculateDelay(now, eveningEnd);
            }
        }

        _isInvocationTime.setValue(shouldShowBadge);

        if (shouldShowBadge && delayMillis > 0) {
            hideBadgeRunnable = () -> _isInvocationTime.postValue(false);
            handler.postDelayed(hideBadgeRunnable, delayMillis);
        }
    }

    private boolean areInvocationsCompletedForPeriod(String periodKey) {
        List<SingleInvocation> list = (periodKey.equals(InvocationData.KEY_LAST_MORNING_SESSION_END)) ? InvocationData.MORNING_INVOCATIONS : InvocationData.EVENING_INVOCATIONS;
        for (SingleInvocation invocation : list) {
            String countKey = periodKey + "_" + invocation.getText();
            int savedCount = prefs.getInt(countKey, invocation.getInitialCount());
            if (savedCount > 0) return false;
        }
        return true;
    }

    private long calculateDelay(Calendar now, int endHour) {
        Calendar endTime = (Calendar) now.clone();
        endTime.set(Calendar.HOUR_OF_DAY, endHour);
        endTime.set(Calendar.MINUTE, 0);
        endTime.set(Calendar.SECOND, 0);
        return endTime.getTimeInMillis() - now.getTimeInMillis();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (hideBadgeRunnable != null) {
            handler.removeCallbacks(hideBadgeRunnable);
        }
    }
}
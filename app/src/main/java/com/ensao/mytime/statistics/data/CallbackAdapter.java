package com.ensao.mytime.statistics.data;

import com.ensao.mytime.Activityfeature.Repos.CallBackAfterDbOperation;

/**
 * Adapter that bridges CallBackAfterDbOperation (from Activityfeature)
 * to StatisticsCallback (from statistics package).
 * 
 * This ensures decoupling - the statistics package doesn't directly
 * depend on CallBackAfterDbOperation interface.
 */
public class CallbackAdapter<T> implements CallBackAfterDbOperation<T> {

    private final StatisticsCallback<T> statisticsCallback;

    public CallbackAdapter(StatisticsCallback<T> statisticsCallback) {
        this.statisticsCallback = statisticsCallback;
    }

    @Override
    public void onComplete(T DbOpResult) {
        if (statisticsCallback != null) {
            statisticsCallback.onComplete(DbOpResult);
        }
    }

    /**
     * Factory method to create an adapter from a StatisticsCallback
     */
    public static <T> CallbackAdapter<T> from(StatisticsCallback<T> callback) {
        return new CallbackAdapter<>(callback);
    }
}

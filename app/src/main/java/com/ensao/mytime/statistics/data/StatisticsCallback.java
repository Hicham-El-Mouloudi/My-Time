package com.ensao.mytime.statistics.data;

/**
 * Callback interface for asynchronous statistics data operations.
 * 
 * @param <T> The type of data returned by the operation
 */
public interface StatisticsCallback<T> {
    void onComplete(T result);
}

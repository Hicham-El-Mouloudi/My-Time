package com.ensao.mytime.Activityfeature.Repos;

public interface CallBackAfterDbOperation<T>{

    void onComplete(T DbOpResult);


}

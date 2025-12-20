package com.ensao.mytime.home.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import com.ensao.mytime.home.model.InvocationData;
import com.ensao.mytime.home.model.SingleInvocation;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class InvocationViewModel extends AndroidViewModel {

    private final MutableLiveData<Object[]> _invocationContent = new MutableLiveData<>();
    public LiveData<Object[]> invocationContent = _invocationContent;

    private final MutableLiveData<Boolean> _allInvocationsCompleted = new MutableLiveData<>(false);
    public LiveData<Boolean> allInvocationsCompleted = _allInvocationsCompleted;

    private final SharedPreferences prefs;

    public InvocationViewModel(Application application) {
        super(application);
        prefs = application.getSharedPreferences("UserSettings", Context.MODE_PRIVATE);
    }

    public void determineInvocation() {
        int morningEnd = prefs.getInt(InvocationData.KEY_MORNING_END_HOUR, InvocationData.DEFAULT_MORNING_END);
        int eveningEnd = prefs.getInt(InvocationData.KEY_EVENING_END_HOUR, InvocationData.DEFAULT_EVENING_END);

        String periodKey = getCurrentPeriodKey(morningEnd, eveningEnd);

        String title;
        List<SingleInvocation> invocationList;

        if (periodKey.equals(InvocationData.KEY_LAST_MORNING_SESSION_END)) {
            title = InvocationData.MORNING_TITLE;
            invocationList = handleSessionLoading(InvocationData.MORNING_INVOCATIONS, periodKey, morningEnd);
        } else if (periodKey.equals(InvocationData.KEY_LAST_EVENING_SESSION_END)) {
            title = InvocationData.EVENING_TITLE;
            invocationList = handleSessionLoading(InvocationData.EVENING_INVOCATIONS, periodKey, eveningEnd);
        } else {
            title = InvocationData.DEFAULT_TITLE;
            invocationList = Arrays.asList(new SingleInvocation(InvocationData.DEFAULT_TEXT, 0));
        }

        _invocationContent.setValue(new Object[]{title, invocationList});
        _allInvocationsCompleted.postValue(areAllInvocationsCompleted(invocationList));
    }

    private List<SingleInvocation> handleSessionLoading(List<SingleInvocation> defaultList, String periodKey, int endHour) {
        long lastSessionEndTimestamp = prefs.getLong(periodKey, 0L);
        if (System.currentTimeMillis() < lastSessionEndTimestamp) {
            return loadInvocationState(defaultList, periodKey);
        } else {
            //Forcer la réinitialisation des compteurs et de l'état de complétion
            List<SingleInvocation> freshList = defaultList.stream()
                    .map(inv -> new SingleInvocation(inv.getText(), inv.getInitialCount()))
                    .collect(Collectors.toList());
            saveInvocationState(freshList, periodKey, endHour); // Sauvegarde la nouvelle session
            return freshList;
        }
    }

    public void saveCurrentState(List<SingleInvocation> currentList) {
        int morningEnd = prefs.getInt(InvocationData.KEY_MORNING_END_HOUR, InvocationData.DEFAULT_MORNING_END);
        int eveningEnd = prefs.getInt(InvocationData.KEY_EVENING_END_HOUR, InvocationData.DEFAULT_EVENING_END);
        String periodKey = getCurrentPeriodKey(morningEnd, eveningEnd);

        if (!periodKey.isEmpty()) {
            int endHour = periodKey.equals(InvocationData.KEY_LAST_MORNING_SESSION_END) ? morningEnd : eveningEnd;
            saveInvocationState(currentList, periodKey, endHour);
            _allInvocationsCompleted.postValue(areAllInvocationsCompleted(currentList));
        }
    }

    private void saveInvocationState(List<SingleInvocation> list, String periodKey, int endHour) {
        SharedPreferences.Editor editor = prefs.edit();
        for (SingleInvocation invocation : list) {
            String countKey = periodKey + "_" + invocation.getText();
            editor.putInt(countKey, invocation.getCurrentCount());
        }

        Calendar expirationTime = Calendar.getInstance();
        expirationTime.set(Calendar.HOUR_OF_DAY, endHour);
        expirationTime.set(Calendar.MINUTE, 0);
        expirationTime.set(Calendar.SECOND, 0);

        if (expirationTime.getTimeInMillis() < System.currentTimeMillis()) {
            expirationTime.add(Calendar.DAY_OF_YEAR, 1);
        }
        editor.putLong(periodKey, expirationTime.getTimeInMillis());
        editor.apply();
    }

    private List<SingleInvocation> loadInvocationState(List<SingleInvocation> list, String periodKey) {
        List<SingleInvocation> mutableList = list.stream()
                .map(inv -> new SingleInvocation(inv.getText(), inv.getInitialCount()))
                .collect(Collectors.toList());

        for (SingleInvocation invocation : mutableList) {
            String countKey = periodKey + "_" + invocation.getText();
            int savedCount = prefs.getInt(countKey, invocation.getInitialCount());
            invocation.setCurrentCount(savedCount);
        }
        return mutableList;
    }

    private String getCurrentPeriodKey(int morningEnd, int eveningEnd) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int morningStart = prefs.getInt(InvocationData.KEY_MORNING_START_HOUR, InvocationData.DEFAULT_MORNING_START);
        int eveningStart = prefs.getInt(InvocationData.KEY_EVENING_START_HOUR, InvocationData.DEFAULT_EVENING_START);

        if (currentHour >= morningStart && currentHour < morningEnd) {
            return InvocationData.KEY_LAST_MORNING_SESSION_END;
        } else if (currentHour >= eveningStart && currentHour < eveningEnd) {
            return InvocationData.KEY_LAST_EVENING_SESSION_END;
        } else {
            return "";
        }
    }

    private boolean areAllInvocationsCompleted(List<SingleInvocation> list) {
        if (list == null || list.isEmpty() || list.get(0).getInitialCount() == 0) {
            return false; // Ce n'est pas une liste d'invocations valide (ex: message par défaut)
        }
        // Vérifie si TOUS les compteurs sont à zéro
        return list.stream().allMatch(invocation -> invocation.getCurrentCount() == 0);
    }
}
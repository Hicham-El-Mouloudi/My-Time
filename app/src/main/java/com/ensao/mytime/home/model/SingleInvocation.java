package com.ensao.mytime.home.model;

public class SingleInvocation {
    private final String text;
    private final int initialCount;
    private int currentCount;

    public SingleInvocation(String text, int initialCount) {
        this.text = text;
        this.initialCount = initialCount;
        this.currentCount = initialCount;
    }

    public String getText() {
        return text;
    }

    public int getInitialCount() {
        return initialCount;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    // Setter pour charger l'état sauvegardé depuis le ViewModel
    public void setCurrentCount(int count) {
        this.currentCount = count;
    }

    // Méthode pour décrémenter le compteur
    public void decrement() {
        if (currentCount > 0) {
            currentCount--;
        }
    }

    // Méthode pour réinitialiser le compteur à l'état initial
    public void reset() {
        currentCount = initialCount;
    }
}
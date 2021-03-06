package ca.sfu.cmpt276.be.parentapp.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ca.sfu.cmpt276.be.parentapp.model.Child;
import ca.sfu.cmpt276.be.parentapp.model.Coin;

/**
 * Handles the logic of the coin flip.
 * Flips random side, stores the history of the coin flip, and
 * remembers the last child that picked
 */

public class CoinFlipManager {

    private final DataManager dataManager = DataManager.getInstance();
    private final ArrayList<Coin> coinFlipHistory = DataManager.getInstance().getCoinFlipHistory();
    private final ChildManager childManager = new ChildManager();
    private final ArrayList<Child> coinFlipQueue = DataManager.getInstance().getCoinFlipQueue();
    private final List<CoinObserver> observers = new ArrayList<>();

    public void registerChangeCallback(CoinObserver obs) {
        observers.add(obs);
    }

    public void unRegisterChangeCallback(CoinObserver obs) {
        observers.remove(obs);
    }

    public String flipRandomCoin(String userChoice, boolean userOverride) {

        Random rand = new Random();
        int randomInt = rand.nextInt(2);
        String result;

        switch (randomInt) {
            case 0:
                result = "Heads";
                break;
            case 1:
                result = "Tails";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + randomInt);
        }

        if (userOverride) {
            saveCoinFlip(result, userChoice, new Child("None"));
        } else {
            saveCoinFlip(result, userChoice, coinFlipQueue.get(0));
            moveToEndQueue();
        }
        notifyValueHasChanged();
        serializeCoinflips();

        return result;
    }


    public void saveCoinFlip(String result, String userChoice, Child childPicked) {

        LocalDateTime creationTime = LocalDateTime.now();
        Coin coinGame = new Coin(creationTime, childPicked, userChoice, result);
        coinFlipHistory.add(0, coinGame);

    }

    public void moveToEndQueue() {
        if (childManager.size() > 0) {
            Child first = coinFlipQueue.get(0);
            coinFlipQueue.remove(first);
            coinFlipQueue.add(first);
        }
    }

    public void moveToFrontQueue(int index) {
        Child fromIndex = coinFlipQueue.get(index);
        coinFlipQueue.remove(fromIndex);
        coinFlipQueue.add(0, fromIndex);
        serializeCoinflips();

    }

    public Coin getCoinFlipGame(int index) {
        return this.coinFlipHistory.get(index);
    }

    public ArrayList<Child> getCoinFlipQueue() {
        return coinFlipQueue;
    }

    public String getCoinFlipID(int index) { return this.coinFlipHistory.get(index).getPickerId(); }

    public ArrayList<Coin> getCoinList() {
        return this.coinFlipHistory;
    }

    private void notifyValueHasChanged() {
        for (CoinObserver obs : observers) {
            obs.notifyCounterChanged();
        }
    }

    private void serializeCoinflips() {
        dataManager.serializeCoinflips();
    }

    public interface CoinObserver {
        void notifyCounterChanged();
    }

}

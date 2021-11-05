package ca.sfu.cmpt276.be.parentapp.model;

import android.widget.Toast;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import ca.sfu.cmpt276.be.parentapp.CoinFlipActivity;

public class CoinFlipManager {

    private static CoinFlipManager instance;

    ArrayList<Coin> coinFlipHistory = new ArrayList<Coin>();

    public int getNextChild() {
        return nextChild;
    }

    public void setNextChild(int nextChild) {
        this.nextChild = nextChild;
    }

    int nextChild = 0;
    private List<CoinObserver> observers = new ArrayList<>();


    ChildManager childManager = ChildManager.getInstance();


    public static CoinFlipManager getInstance() {
        if (instance == null) {
            instance = new CoinFlipManager();
        }
        return instance;
    }


    public interface CoinObserver {
        void notifyCounterChanged();
    }

    public void registerChangeCallback(CoinObserver obs) {
        observers.add(obs);
    }
    public void unRegisterChangeCallback(CoinObserver obs) {
        observers.remove(obs);
    }
    private void notifyValueHasChanged() {
        for (CoinObserver obs : observers) {
            obs.notifyCounterChanged();
        }
    }

    public String flipRandomCoin(String userChoice){

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

        String childPick;

        if (childManager.getAll().size()==0) {
            childPick = null;
        }
        else {
            childPick = childManager.get(nextChild).getName();
            if (childManager.getAll().size() <= nextChild+1){
                this.nextChild = 0;
            }
            else {
                this.nextChild = this.nextChild + 1;
            }
        }

        saveCoinFlip(result,userChoice, childPick);

        notifyValueHasChanged();

        return result;
    }

    public String emptyFlip(){
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
        notifyValueHasChanged();
        return result;
    }

    public void saveCoinFlip(String result, String userChoice, String childPicked){

        LocalDateTime creationTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String time = creationTime.format(formatter);

        Coin coinflip = new Coin(time, childPicked, userChoice, result);
        coinFlipHistory.add(0,coinflip);

    }

    public Child getLastChildPicked(){
        ChildManager childManager = ChildManager.getInstance();
        this.nextChild = this.nextChild + 1;
        return childManager.get(this.nextChild-1);
    }

    public Coin getCoinFlipGame(int index){
        return this.coinFlipHistory.get(index);
    }

    public ArrayList<Coin> getCoinList() { return this.coinFlipHistory; }

    public int currentGamesPlayed(){ return this.coinFlipHistory.size()-1; }
}

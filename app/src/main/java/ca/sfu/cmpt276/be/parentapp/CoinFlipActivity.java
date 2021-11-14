package ca.sfu.cmpt276.be.parentapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import java.util.ArrayList;
import java.util.Objects;

import ca.sfu.cmpt276.be.parentapp.databinding.ActivityCoinflipBinding;
import ca.sfu.cmpt276.be.parentapp.model.ChildManager;
import ca.sfu.cmpt276.be.parentapp.model.CoinFlipManager;

/**
Activity with animations to simulate a real coin flip
 Sends and process info from CoinFLipManager
 */

public class CoinFlipActivity extends AppCompatActivity implements CoinFlipManager.CoinObserver {

    MediaPlayer player;
    CoinFlipManager coinFlipManager = new CoinFlipManager();
    boolean userOverride = false;

    ChildManager childManager = new ChildManager();
    private AppBarConfiguration appBarConfiguration;
    private ActivityCoinflipBinding binding;
    private ImageView coin;
    Spinner spinner;

    ArrayAdapter<String> adp1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCoinflipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setupButton();
        updateSpinner();
    }

    private void updateSpinner() {

        getSpinner();
        spinner = findViewById(R.id.spinner_childQueue);
        spinner.setOnItemSelectedListener(new PickChildClass());

    }

    private void getSpinner() {
        spinner = findViewById(R.id.spinner_childQueue);

        ArrayList<String> list = new ArrayList<>();
        for (int i=0; i<coinFlipManager.getCoinFlipQueue().size();i++){
            list.add(coinFlipManager.getCoinFlipQueue().get(i).getName());
        }
        list.add("None");

        adp1 = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, list);
        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adp1);
        //return spinner;
    }

    private void updateSpinner2(){
        Spinner spinner = findViewById(R.id.spinner_childQueue);
        adp1.clear();

        ArrayList<String> list = new ArrayList<>();
        for (int i=0; i<coinFlipManager.getCoinFlipQueue().size();i++){
            list.add(coinFlipManager.getCoinFlipQueue().get(i).getName());
        }
        list.add("None");

        adp1.addAll(list);
        adp1.notifyDataSetChanged();

    }

    private void setupButton() {

        coin = findViewById(R.id.coin_image);

        Button flipButton = findViewById(R.id.flip_coin_button);
        flipButton.setOnClickListener(view ->
            flipCoin("Null")
        );

        Button headsButton = findViewById(R.id.heads_button);
        headsButton.setOnClickListener(view ->
            flipCoin("Heads")
        );

        Button tailsButton = findViewById(R.id.tails_button);
        tailsButton.setOnClickListener(view ->
            flipCoin("Tails")
        );

        flipButton.setVisibility(View.GONE);

    }

    private void flipCoin(String userChoice) {
        coin.animate().setDuration(3100).rotationXBy(2160).setListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                String result = coinFlipManager.flipRandomCoin(userChoice,userOverride);
                spinner.setSelection(0);
                switch (result) {
                    case "Heads":
                        coin.setImageResource(R.drawable.coin_heads);
                        break;
                    case "Tails":
                        coin.setImageResource(R.drawable.coin_tails);
                        break;
                }
                userOverride=false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                coin.setImageResource(R.drawable.coin_blur);
                player = MediaPlayer.create(CoinFlipActivity.this, R.raw.coin_sound);
                player.start();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.coinflip, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.coinHistoryButton) {
            Intent intent = new Intent(getApplicationContext(), FlipHistoryActivity.class);
            startActivity(intent);
        }

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateDisplayCoinResult() {

        TextView tv = findViewById(R.id.flip_result);
        if (coinFlipManager.getCoinList().size() > 0) {
            tv.setText(coinFlipManager.getCoinFlipGame(0).getResult());
        }

        updateSpinner2();
    }

    @Override
    protected void onResume() {
        super.onResume();
        coinFlipManager.registerChangeCallback(this);
        updateDisplayCoinResult();
    }

    @Override
    protected void onPause() {
        super.onPause();
        coinFlipManager.unRegisterChangeCallback(this);
    }

    @Override
    public void notifyCounterChanged() {
        updateDisplayCoinResult();

    }

    private class PickChildClass implements android.widget.AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i<childManager.size()) {
                coinFlipManager.moveToFrontQueue(i);
                userOverride = false;
                spinner.setSelection(0);
            }
            else {
                userOverride = true;

            }
            updateDisplayCoinResult();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }
}
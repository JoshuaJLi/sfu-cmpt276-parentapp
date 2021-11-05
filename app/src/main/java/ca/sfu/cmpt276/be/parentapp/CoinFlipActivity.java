package ca.sfu.cmpt276.be.parentapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import org.w3c.dom.Text;

import java.util.Objects;

import ca.sfu.cmpt276.be.parentapp.databinding.ActivityCoinflipBinding;
import ca.sfu.cmpt276.be.parentapp.model.ChildManager;
import ca.sfu.cmpt276.be.parentapp.model.CoinFlipManager;

public class CoinFlipActivity extends AppCompatActivity implements CoinFlipManager.CoinObserver {

    private AppBarConfiguration appBarConfiguration;
    private ActivityCoinflipBinding binding;

    private ImageView coin;
    MediaPlayer player;
    CoinFlipManager coinFlipManager = CoinFlipManager.getInstance();
    ChildManager childManager = ChildManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCoinflipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        initializeCoin();
        setupButton();
    }

    private void setupButton() {

        Button flipButton = (Button) findViewById(R.id.flipCoinButton);
        flipButton.setOnClickListener(view -> {
            flipCoin("Null");
        });

        Button headsButton = (Button) findViewById(R.id.headsButton);
        headsButton.setOnClickListener(view -> {
            flipCoin("Heads");
        });

        Button tailsButton = (Button) findViewById(R.id.tailsButton);
        tailsButton.setOnClickListener(view -> {
            flipCoin("Tails");
        });

        TextView nextChild = (TextView) findViewById(R.id.coinNextChild);

        if (childManager.getAll().size() == 0) {
            headsButton.setVisibility(View.GONE);
            tailsButton.setVisibility(View.GONE);
            nextChild.setVisibility(View.GONE);
        } else {
            flipButton.setVisibility(View.GONE);
            if (coinFlipManager.getNextChild() >= childManager.getAll().size()){
                Toast.makeText(this,"list reset", Toast.LENGTH_SHORT).show();
                coinFlipManager.setNextChild(0);
            }
            nextChild.setText("Next child: "+childManager.get(coinFlipManager.getNextChild()).getName());
        }


    }

    private void initializeCoin() {
        coin = findViewById(R.id.coin_image);
        TextView tv = findViewById(R.id.flipResult);
        String result = coinFlipManager.emptyFlip();
        switch (result) {
            case "Heads":
                coin.setImageResource(R.drawable.coin_heads);
                tv.setText("Heads");
                break;
            case "Tails":
                coin.setImageResource(R.drawable.coin_tails);
                tv.setText("Tails");
                break;
        }
    }

    private void flipCoin(String userChoice) {
        coin.animate().setDuration(3100).rotationXBy(2160).setListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                String result = coinFlipManager.flipRandomCoin(userChoice);
                switch (result) {
                    case "Heads":
                        coin.setImageResource(R.drawable.coin_heads);
                        break;
                    case "Tails":
                        coin.setImageResource(R.drawable.coin_tails);
                        break;
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                coin.setImageResource(R.drawable.coin_blur);
                player = MediaPlayer.create(CoinFlipActivity.this,R.raw.coin_sound);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if (item.getItemId() == R.id.coinHistoryButton){
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
        TextView tv = findViewById(R.id.flipResult);
        if (coinFlipManager.getCoinList().size() > 0){
            tv.setText(coinFlipManager.getCoinFlipGame(0).getResult());
        }

        TextView nextChild = (TextView) findViewById(R.id.coinNextChild);
        nextChild.setText("Next child: "+childManager.get(coinFlipManager.getNextChild()).getName());

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
}
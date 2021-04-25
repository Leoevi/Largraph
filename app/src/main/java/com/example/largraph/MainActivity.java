package com.example.largraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onBtnStart(View view) {
        // Prevents back button during gameplay to get player to the next level
        GameActivity.currentLevel = 0;

        // https://developer.android.com/training/basics/firstapp/starting-activity
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
}
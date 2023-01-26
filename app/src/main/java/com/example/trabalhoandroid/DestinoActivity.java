package com.example.trabalhoandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DestinoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destino);

        Button botaoFechar = findViewById(R.id.button_fechar);
        botaoFechar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DestinoActivity.this, "Encerrando Aplicação", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        });
    }
}
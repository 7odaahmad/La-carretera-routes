package com.saber.lacarreteraroutes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText fromEditText,toEditText,costEditText;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromEditText = findViewById(R.id.fromEditText);
        toEditText = findViewById(R.id.toEditText);
        costEditText = findViewById(R.id.costEditText);

    }

    public void startButtonAction(View view){
        String from = fromEditText.getText().toString();
        String to = toEditText.getText().toString();
        String cost = costEditText.getText().toString();

        if (from.trim().equals("")){
            Toast.makeText(this, getResources().getString(R.string.main_screen_empty_from_error), Toast.LENGTH_LONG).show();
            return;
        }

        if (to.trim().equals("")){
            Toast.makeText(this, getResources().getString(R.string.main_screen_empty_to_error), Toast.LENGTH_LONG).show();
            return;
        }

        if (cost.trim().equals("")){
            Toast.makeText(this, getResources().getString(R.string.main_screen_empty_cost_error), Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this,MapsActivity.class);
        intent.putExtra("start",fromEditText.getText().toString());
        intent.putExtra("end",toEditText.getText().toString());
        intent.putExtra("cost",costEditText.getText().toString());
        startActivity(intent);
    }

}

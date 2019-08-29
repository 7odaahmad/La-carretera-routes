package com.saber.lacarreteraroutes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText costEditText;
    Spinner fromSpinner,toSpinner;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    ArrayList fromLocations,toLocations;
    ArrayList locationsKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpinner = findViewById(R.id.fromSpinner);
        toSpinner = findViewById(R.id.toSpinner);
        costEditText = findViewById(R.id.costEditText);

        fromLocations = new ArrayList();
        toLocations = new ArrayList();
        locationsKeys = new ArrayList();

        final ArrayAdapter<String> fromAdapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,fromLocations){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        fromAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final ArrayAdapter<String> toAdapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,toLocations){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        toAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);
        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Lines");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fromLocations.clear();
                fromAdapter.clear();
                fromAdapter.add(getResources().getString(R.string.main_screen_start_location));

                toLocations.clear();
                toAdapter.clear();
                toAdapter.add(getResources().getString(R.string.main_screen_destination));

                locationsKeys.clear();

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){
                    fromAdapter.add(childDataSnapshot.getValue().toString());
                    toAdapter.add(childDataSnapshot.getValue().toString());

                    locationsKeys.add(childDataSnapshot.getKey());
                }

                fromAdapter.notifyDataSetChanged();
                toAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Firebase", "Failed to read value.", error.toException());
            }
        });
    }

    public void startButtonAction(View view){
        String from = fromSpinner.getSelectedItem().toString();
        String to = toSpinner.getSelectedItem().toString();
        String cost = costEditText.getText().toString();

        String fromKey = locationsKeys.get(fromSpinner.getSelectedItemPosition() - 1).toString();
        String endKey = locationsKeys.get(toSpinner.getSelectedItemPosition() - 1).toString();

        if (from.trim().equals(getResources().getString(R.string.main_screen_start_location))){
            Toast.makeText(this, getResources().getString(R.string.main_screen_empty_from_error), Toast.LENGTH_LONG).show();
            return;
        }

        if (to.trim().equals(getResources().getString(R.string.main_screen_destination))){
            Toast.makeText(this, getResources().getString(R.string.main_screen_empty_to_error), Toast.LENGTH_LONG).show();
            return;
        }

        if (from.equals(to)){
            Toast.makeText(this, getResources().getString(R.string.main_screen_same_locations_error), Toast.LENGTH_LONG).show();
            return;
        }

        if (!cost.matches("\\d+(.\\d+)?")){
            Toast.makeText(this, getResources().getString(R.string.main_screen_empty_cost_error), Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this,MapsActivity.class);
        intent.putExtra("start",from);
        intent.putExtra("end",to);
        intent.putExtra("startKey",fromKey);
        intent.putExtra("endKey",endKey);
        intent.putExtra("cost",costEditText.getText().toString());
        startActivity(intent);
    }

}

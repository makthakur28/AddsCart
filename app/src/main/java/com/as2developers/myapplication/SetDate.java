package com.as2developers.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;

public class SetDate extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    TextView selectedDate;
    String SelectedDateS;
    Button selectDate,continue_btn;
    //for slide navigation bar
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_date);
        setContentView(R.layout.activity_set_date);
        selectedDate = (TextView) findViewById(R.id.selectedDate);
        selectDate = (Button) findViewById(R.id.selectDate);
        continue_btn = findViewById(R.id.btn_continue);

        //hooks for navigation bar
        drawerLayout =findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.dummy_content,R.string.dummy_content);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        //initialize calender
        Calendar calendar = Calendar.getInstance();

        //get year
        int year = calendar.get(Calendar.YEAR);
        //get month
        int month = calendar.get(Calendar.MONTH);
        //get day
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        SelectedDateS ="";
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //initialize date picker dialog
                DatePickerDialog datePicker = new DatePickerDialog(SetDate.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        i1+=1;
                        SelectedDateS = i2 +"/" + i1 + "/" +i;
                        selectedDate.setText(SelectedDateS);
                    }
                },year,month,day
                );

                //Disable date
                datePicker.getDatePicker().setMinDate(System.currentTimeMillis()+24*60*60*1000);
                datePicker.getDatePicker().setMaxDate(System.currentTimeMillis()+5*24*60*60*1000);
                datePicker.show();
            }
        });

        continue_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SelectedDateS.isEmpty()){
                    Toast.makeText(SetDate.this, "Please Select a Date!", Toast.LENGTH_SHORT).show();
                }
                else {
                    startActivity(new Intent(SetDate.this, ConfirmPickupActivity.class).putExtra("selectedDate", SelectedDateS));
                }
            }
        });
    }
    //also for navigation bar
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.profile:
                startActivity(new Intent(this,ProfilePage.class));
                break;
            case R.id.pickup:
                Toast.makeText(this, "Opening to a new pickup..", Toast.LENGTH_SHORT).show();
                break;
            case R.id.howItWorks:
                startActivity(new Intent(this,HowItWorks.class));
                break;
            case R.id.home:
                startActivity(new Intent(this,SelectLocationFromMap.class));
                finish();
                break;
            case R.id.aboutUs:
                startActivity(new Intent(this,AboutUs.class));
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}

package com.example.sit305ass1;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    //CONSTANTS
    final String [] currencies = {"USD", "AUD", "EUR", "JPY", "GBP"};
    final String [] efficiency = {"mpg", "km/L"};
    final String [] fuel = {"Gallons", "Litres", };
    final String [] distance = {"Kilometers", "Nautical Miles"};
    final String [] temperature = {"Celsius", "Fahrenheit", "Kelvin"};
    final double usdToAud = 1.55;
    final double usdToEur = 0.92;
    final double usdToJpy = 148.50;
    final double usdToGbp = 0.78;
    final double mpgToKml = 0.425;
    final double gallonToLitre = 3.785;
    final double nauticalMileToKilometer = 1.852;

    //INITS
    DecimalFormat df;
    Spinner unitFromSpinner, unitToSpinner;
    EditText userInput;

    Button calculateButton;
    TextView output, inputUnit;
    ArrayAdapter<String> adapterCurrencies, adapterEfficiency, adapterFuel, adapterDistance, adapterTemperature, adapterFull;
    ArrayList<String> listToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //GOING FORWARD USE BIG DECIMAL, DUE TO FLOATING POINT INACCURACY
        df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        //Widgets from xml
        unitFromSpinner = findViewById(R.id.unitFromSpinner);
        unitToSpinner = findViewById(R.id.unitToSpinner);
        userInput = findViewById(R.id.userInput);
        output = findViewById(R.id.output);
        calculateButton = findViewById(R.id.calculateButton);
        inputUnit = findViewById(R.id.inputUnit);

        //List that user will see in the first spinner (split so only relevant items will be shown dependent on
        listToShow = new ArrayList<>(Arrays.asList(currencies));
        listToShow.addAll(Arrays.asList(efficiency));
        listToShow.addAll(Arrays.asList(fuel));
        listToShow.addAll(Arrays.asList(distance));
        listToShow.addAll(Arrays.asList(temperature));
        //Same thing here for the adapters
        adapterCurrencies = new ArrayAdapter<>(this, R.layout.custom_spinner_style, currencies);
        adapterCurrencies.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        adapterEfficiency = new ArrayAdapter<>(this, R.layout.custom_spinner_style, efficiency);
        adapterEfficiency.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        adapterFuel = new ArrayAdapter<>(this, R.layout.custom_spinner_style, fuel);
        adapterFuel.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        adapterDistance = new ArrayAdapter<>(this, R.layout.custom_spinner_style, distance);
        adapterDistance.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        adapterTemperature = new ArrayAdapter<>(this, R.layout.custom_spinner_style, temperature);
        adapterTemperature.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        adapterFull = new ArrayAdapter<>(this, R.layout.custom_spinner_style, listToShow);
        adapterFull.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

        // UNITS FROM SPINNER
        unitFromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l)
            {
                String item = adapterView.getItemAtPosition(pos).toString();
                //Toast.makeText(MainActivity.this, "Selected Item: " + item, Toast.LENGTH_SHORT).show();
                updateUnitToSpinner(item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });

        unitFromSpinner.setAdapter(adapterFull);

        // UNITS TO SPINNER
        unitToSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l)
            {
                String item = adapterView.getItemAtPosition(pos).toString();
                calculateButton.setText("Convert to " + item);
                //Toast.makeText(MainActivity.this, "Selected Item: " + item, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });
    }

    //CAN'T PARSE VALUES AS PARAMETERS BECAUSE OF BUTTON FUNCTIONALITY??
    public void calculateResult(View view)
    {
        // ENSURES THE USER HAS ENTERED A VALUE AND PROVIDES FEEDBACK IF NO
        if (userInput.getText().toString().isEmpty())
        {
            Toast.makeText(MainActivity.this, "Please enter a value to be converted", Toast.LENGTH_SHORT).show();
            return;
        }

        double result = 0;
        double userValue = Double.parseDouble(userInput.getText().toString());
        String unitFrom = unitFromSpinner.getSelectedItem().toString();
        String unitTo = unitToSpinner.getSelectedItem().toString();
        int unitFromPos = adapterFull.getPosition(unitFrom);
        int unitToPos = adapterFull.getPosition(unitTo);



        // CHECKS THE INPUTS FROM THE USER INTO THE UNITS FROM SPINNER, AND CHOOSES WHICH LOGIC TO APPLY (UNITS TO ARE DEALT WITH IN RESPECTIVE CATEGORIES)
        if (unitFromPos == 0)
            result = usdToCurrency(unitToPos, userValue);
        else if (unitFromPos <= 4)
            result = usdToCurrency(unitToPos,currencyToUsd(unitFromPos, userValue));
        else if (unitFromPos <= 6)
            result = convertEfficiency(unitFromPos, unitToPos, userValue);
        else if (unitFromPos <= 8)
            result = convertFuel(unitFromPos, unitToPos, userValue);
        else if (unitFromPos <= 10)
            result = convertDistance(unitFromPos, unitToPos, userValue);
        else
            result = convertTemperature(unitFromPos, unitToPos, userValue);

        //THIS IS THE ACTUAL OUTPUT THE USER SEES ON SCREEN
        output.setText(userValue + " " + unitFrom + " equals\n" + df.format(result) + " " + unitTo );
    }

    private double usdToCurrency(int unitToPos, double userValue)
    {
        double result = 0.0;
        switch (unitToPos)
        {
            case 0: //USD -> USD
                result = userValue;
                break;
            case 1: //USD -> AUD
                result = (userValue * usdToAud);
                break;
            case 2: //USD -> EUR
                result = (userValue * usdToEur);
                break;
            case 3: //USD -> JPY
                result = (userValue * usdToJpy);
                break;
            case 4: //USD -> GBP
                result = (userValue * usdToGbp);
                break;
        }

        return result;
    }
    private double currencyToUsd(int unitFromPos, double userValue)
    {
        double result = 0.0;
        switch (unitFromPos)
        {
            case 0: //USD -> USD
                result = userValue;
                break;
            case 1: //AUD -> USD
                result = (userValue / usdToAud);
                break;
            case 2: //EUR -> USD
                result = (userValue / usdToEur);
                break;
            case 3: //JPY -> USD
                result = (userValue / usdToJpy);
                break;
            case 4: //GBP -> USD
                result = (userValue / usdToGbp);
                break;
        }

        return result;
    }

    public double convertEfficiency(int unitFromPos, int unitToPos, double userValue)
    {
        // CHECKS ALL POSSIBLE CONDITIONS THAT CAN RESULT IN A NECESSARY CALCULATION (see below.)
        if (unitFromPos == 5 && unitToPos == 6) return userValue * mpgToKml;        //MPG -> KML
        else if (unitFromPos == 6 && unitToPos == 5) return userValue / mpgToKml;   //KML -> MPG
        else return userValue;
    }
    public double convertFuel(int unitFromPos, int unitToPos, double userValue)
    {
        // CHECKS ALL POSSIBLE CONDITIONS THAT CAN RESULT IN A NECESSARY CALCULATION (see below.)
        if (unitFromPos == 7 && unitToPos == 8) return userValue * gallonToLitre;       //G -> L
        else if (unitFromPos == 8 && unitToPos == 7) return userValue / gallonToLitre;  //L -> G
        else return userValue;
    }
    public double convertDistance(int unitFromPos, int unitToPos, double userValue)
    {
        // CHECKS ALL POSSIBLE CONDITIONS THAT CAN RESULT IN A NECESSARY CALCULATION (see below.)
        if (unitFromPos == 9 && unitToPos == 10) return userValue * nauticalMileToKilometer;        //NM -> KM
        else if (unitFromPos == 10 && unitToPos == 9) return userValue / nauticalMileToKilometer;   //KM -> NM
        else return userValue;
    }
    public double convertTemperature(int unitFromPos, int unitToPos, double userValue)
    {
        // CHECKS ALL POSSIBLE CONDITIONS THAT CAN RESULT IN A NECESSARY CALCULATION (see below.)
        if (unitFromPos == 11 && unitToPos == 12) return (userValue * 1.8) + 32;                //C -> F
        else if (unitFromPos == 11 && unitToPos == 13) return userValue + 273.15;               //C -> K
        else if (unitFromPos == 12 && unitToPos == 11) return (userValue - 32) / 1.8;           //F -> C
        else if (unitFromPos == 12 && unitToPos == 13) return ((userValue - 32) / 1.8) + 273.15;//F -> K
        else if (unitFromPos == 13 && unitToPos == 11) return userValue - 273.15;               //K -> C
        else if (unitFromPos == 13 && unitToPos == 12) return ((userValue - 273.15) - 32) / 1.8;//K -> F
        else return userValue;  //RETURNS VALUE ENTERED BECAUSE SAME UNIT WAS SELECTED FOR CALCULATION
    }
    public void updateUnitToSpinner(String item)
    {
        int pos = adapterFull.getPosition(item);
        inputUnit.setText(item);

        if (pos <= 4)
            unitToSpinner.setAdapter(adapterCurrencies);
        else if (pos <= 6)
            unitToSpinner.setAdapter(adapterEfficiency);
        else if (pos <= 8)
            unitToSpinner.setAdapter(adapterFuel);
        else if (pos <= 10)
            unitToSpinner.setAdapter(adapterDistance);
        else
            unitToSpinner.setAdapter(adapterTemperature);

    }
}
package com.example.misfacturas;

import static com.google.android.material.internal.ContextUtils.getActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.Settings;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
Button btAgregar,btLimpiar,btCopiar;
EditText etDato;
    public static File dir = new File(Environment.getExternalStoragePublicDirectory(""), "Facturas");

    @Override
    protected void onStart() {
        SharedPreferences sh = getSharedPreferences("Facturas", MODE_PRIVATE);
        String facturas = sh.getString("facturas", "");
        etDato.setText(facturas);
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btAgregar=findViewById(R.id.btAgregar);
        etDato=findViewById(R.id.etDato);
        btLimpiar=findViewById(R.id.btLimpiar);
        btCopiar=findViewById(R.id.btCopiar);

        btAgregar.setOnClickListener(this);
        btLimpiar.setOnClickListener(this);
        btCopiar.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btAgregar:
                startActivity(new Intent(this,Buscar.class));
                break;
            case R.id.btLimpiar:

                SharedPreferences sharedPreferences = getSharedPreferences("Facturas",MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                myEdit.putString("facturas", "");
                myEdit.commit();
                etDato.setText("");
                break;
            case R.id.btCopiar:
                SharedPreferences sh = getSharedPreferences("Facturas", MODE_PRIVATE);
                String facturas = sh.getString("facturas", "");
                etDato.setText(facturas);


                askForPermissions();
                generarCSV();
                copyToClipboard(etDato.getText().toString());
                break;
        }
    }

    public void copyToClipboard(String copyText) {
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("url", copyText); clipboard.setPrimaryClip(clip);
        @SuppressLint("RestrictedApi") Toast toast = Toast.makeText(this, "Copiado", Toast.LENGTH_SHORT); toast.show();
    }

    public void generarCSV()
    {
        File myFile;
        try {
            myFile = new File(Environment.getExternalStoragePublicDirectory(""), "Factura");
            myFile.createNewFile();
            String nombre= "Facturas "+(System.currentTimeMillis()/100)+".csv";
            FileWriter fileWriter=new FileWriter(dir.toString()+"/"+nombre);
             fileWriter.append("NIT|NRO FACTURA|NRO AUTORIZACION|FECHA|TOTAL|TOTAL FISCAL|CODIGO DE CONTROL|NIT CLIENTE||||\n");
             fileWriter.append(etDato.getText().toString());

            fileWriter.close();
            Toast.makeText(this, "Archivo copia do en /Facturas/"+nombre, Toast.LENGTH_LONG).show();
           /* myFile = new File(Environment.getExternalStoragePublicDirectory(""), "csvFile.csv");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(etDato.getText().toString());

            myOutWriter.close();
            fOut.close();
            */

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                createDir();
            }
        }
    }

    public void askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                return;
            }
            createDir();
        }
    }

    public void createDir(){
        if (!dir.exists()){
            dir.mkdirs();
        }
    }

}
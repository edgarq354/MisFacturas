package com.example.misfacturas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.misfacturas.R;
import com.google.zxing.Result;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Buscar extends AppCompatActivity  implements me.dm7.barcodescanner.zxing.ZXingScannerView.ResultHandler {
    private static final String FLASH_STATE = "FLASH_STATE";

    private me.dm7.barcodescanner.zxing.ZXingScannerView mScannerView;
    private boolean mFlash;

    EditText etNombre;
    FloatingActionButton fbExportar;

    ArrayList<String> facturas = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_buscar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etNombre=findViewById(R.id.etNombre);
        fbExportar=findViewById(R.id.fbExportar);

        ViewGroup contentFrame = findViewById(R.id.content_frame);
        mScannerView = new me.dm7.barcodescanner.zxing.ZXingScannerView(this);
        contentFrame.addView(mScannerView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        if (ActivityCompat.checkSelfPermission(Buscar.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            pedirPermisoCamara();
        }
        else if ((ActivityCompat.checkSelfPermission(Buscar.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||(ActivityCompat.checkSelfPermission(Buscar.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            pedirPermisoAlmacenamiento();
        }

        facturas = new ArrayList<String>();
        int numero= (int) (Math.random() * 11 + 1);
        etNombre.setText("Mi Factura "+ numero);
        fbExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dato="";
                for (int i=0; i<facturas.size(); i++ ){
                    dato+=facturas.get(i);
                    dato+="\n";
                }



               // exportarCSV();
            }
        });

    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        mScannerView.setFlash(mFlash);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, mFlash);
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.e("Countents", rawResult.getText());
        Log.e("Format",rawResult.getBarcodeFormat().toString());

        //verificar si es el mismo numero.
        String texto= rawResult.getText()+"\n";
        facturas.add(texto);

        SharedPreferences sharedPreferences = getSharedPreferences("Facturas",MODE_PRIVATE);

        String listaFacturas = sharedPreferences.getString("facturas", "");

        if (listaFacturas.lastIndexOf(texto)<0){
            texto+=listaFacturas;
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            myEdit.putString("facturas", texto);
            myEdit.commit();
            Toast.makeText(this,"Se agrego ("+texto+")",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"Ya existe ("+texto+")",Toast.LENGTH_SHORT).show();
        }




        finish();
    }

    private int obtener_codigo(String texto) {
        int codigo=0;
        String dato="";
        char[] caracteres = texto.toCharArray();

        for (int i=0; i<caracteres.length;i++)
        {
            if(caracteres[i]=='-'){
                i=caracteres.length;
            }else{
                dato=dato+caracteres[i];
            }

        }
        codigo=Integer.parseInt(dato);
        return codigo;
    }

    public void toggleFlash(View v) {
        mFlash = !mFlash;
        mScannerView.setFlash(mFlash);
    }

    public void pedirPermisoAlmacenamiento(){
        final String[] CAMERA_PERMISSIONS = { Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE };

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //YA LO CANCELE Y VOUELVO A PERDIR EL PERMISO.

            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle("Atención!");
            dialogo1.setMessage("Debes otorgar permisos de acceso a ALMACENAMIENTO.");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Solicitar permiso", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    dialogo1.cancel();
                    ActivityCompat.requestPermissions(Buscar.this,
                            CAMERA_PERMISSIONS,
                            1);

                }
            });
            dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    dialogo1.cancel();

                }
            });
            dialogo1.show();
        } else {
            ActivityCompat.requestPermissions(Buscar.this,
                    CAMERA_PERMISSIONS,
                    1);
        }
    }
    public void pedirPermisoCamara(){
        if(ContextCompat.checkSelfPermission(Buscar.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(Buscar.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA},0);
        }
    }

    public void exportarCSV(){

        File fileImagen=new File(Environment.getExternalStorageDirectory(),"FacturasCSV");
        boolean isCreada=fileImagen.exists();
        String nombreImagen="";
        if(isCreada==false){
            isCreada=fileImagen.mkdirs();
        }
        if(isCreada==true){
            nombreImagen=(System.currentTimeMillis()/1000)+".csv";
        }

        String path2=Environment.getExternalStorageDirectory()+
                File.separator+"FacturasCSV"+File.separator+nombreImagen;


        try {
            FileWriter fileWriter=new FileWriter(path2);
            for (int i=0; i<facturas.size(); i++ ){
                fileWriter.append(facturas.get(i));
                fileWriter.append("\n");
            }

            fileWriter.close();
            Toast.makeText(this,"Se creo el archivo correctamento ("+nombreImagen+")",Toast.LENGTH_LONG).show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
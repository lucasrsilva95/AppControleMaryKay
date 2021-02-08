package com.example.controlemk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.widget.Toast;

import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.PedidosRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BackupBanco {
    public Context context;
    public SQLiteDatabase conexao;
    public BancoOpenHelper openH;

    public BackupBanco(Context context){
        this.context = context;
        openH = new BancoOpenHelper(context);
        VendOpenHelper openH2 = new VendOpenHelper(context);
        ProdVendOpenHelper openH3 = new ProdVendOpenHelper(context);
        ClientesOpenHelper openH4 = new ClientesOpenHelper(context);
        conexao = openH.getWritableDatabase();
    }

    public boolean salvarBackup(){
        try {
            File pasta = new File(Environment.getExternalStorageDirectory() + "/ControleMK/Backups");
            if(!pasta.exists()){
                pasta.mkdirs();
            }
            OutputStream outputStream = new FileOutputStream(
                    new File(pasta + "/BANCO.db"));
            return salvarBackup(outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean salvarBackup(OutputStream outputStream){
        try{
            InputStream inputStream = new FileInputStream(
                    new File(Environment.getDataDirectory() + "/data/com.example.controlemk/databases/BANCO"));


            byte[] buffer = new byte[1024];
            int comprimento;

            while((comprimento = inputStream.read(buffer)) > 0){
                outputStream.write(buffer, 0, comprimento);
            }
            inputStream.close();
            outputStream.close();
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean restaurarBackup()  {
        try {
            InputStream inputStream = new FileInputStream(
                    new File(Environment.getExternalStorageDirectory() + "/ControleMK/Backups/BANCO.db"));
            return restaurarBackup(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean restaurarBackup(InputStream inputStream){
        try{
            OutputStream outputStream = new FileOutputStream(
                    new File(Environment.getDataDirectory() + "/data/com.example.controlemk/databases/BANCO"));

            byte[] buffer = new byte[1024];
            int comprimento;

            while((comprimento = inputStream.read(buffer)) > 25){
                outputStream.write(buffer, 0, comprimento);
            }
            inputStream.close();
            outputStream.close();

            try{
                VendasRepositorio v = new VendasRepositorio(context);
                ProdutosRepositorio p = new ProdutosRepositorio(context);
                ClientesRepositorio c = new ClientesRepositorio(context);
                PedidosRepositorio ped = new PedidosRepositorio(context);
            }catch (SQLiteException e){

            }
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

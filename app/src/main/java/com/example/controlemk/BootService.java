package com.example.controlemk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;

public class BootService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
//            Toast.makeText(context,"Android Iniciado",Toast.LENGTH_LONG).show();
            ClientesRepositorio cliRep = new ClientesRepositorio(context);
            cliRep.definirNotificacoes(false);
        }
    }
}

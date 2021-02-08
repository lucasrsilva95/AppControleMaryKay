package com.example.controlemk;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;

import java.io.Serializable;
import java.util.List;

public class Receiver extends BroadcastReceiver {

    ClientesRepositorio cliRep;
    VendasRepositorio vendRep;
    Bundle bundle;

    List<Venda> debitosHoje;


    @Override
    public void onReceive(Context context, Intent intent) {

        cliRep = new ClientesRepositorio(context);
        vendRep = new VendasRepositorio(context);
        debitosHoje = cliRep.debitosDeHoje();
        Intent it = new Intent(context, Debitos.class);
        it.putExtra("DEBITOS_HOJE", (Serializable) debitosHoje);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,it,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyDebito")
                .setContentTitle("Você possui pagamentos para receber Hoje")
                .setContentText(String.format("Nº de pagamentos: %d             Valor Total: R$%.2f", debitosHoje.size(), totParcelasDeHoje()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_monetization_on_black_24dp)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(201, builder.build());
    }

    public float totParcelasDeHoje(){
        float tot = 0.0f;
        for(Venda parc:debitosHoje){
            float valParc = Float.parseFloat(parc.datasPag.get(0).split("=")[1]);
            tot += valParc;
        }
        return tot;
    }
}

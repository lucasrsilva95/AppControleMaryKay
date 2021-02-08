package com.example.controlemk.Dominio.Repositorio;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;

import androidx.preference.PreferenceManager;

import com.example.controlemk.Dominio.Entidades.Cliente;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.OperacoesDatas;
import com.example.controlemk.Receiver;
import com.example.controlemk.database.BancoOpenHelper;
import com.example.controlemk.database.ClientesOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClientesRepositorio {

    private SQLiteDatabase conexao;
    private BancoOpenHelper openH;

    public BackupManager backupManager;
    private boolean notif;
    private int horaNotif, minutoNotif;

    private Context context;

    public ClientesRepositorio(Context context) {
        this.context = context;
        openH = new BancoOpenHelper(context);
        backupManager = new BackupManager(context);
        definirConfigs();
    }

    public void inserir(Cliente cliente) {

        conexao = openH.getWritableDatabase();

        ContentValues cVal = new ContentValues();
        cVal.put("NOME", cliente.nome);
        cVal.put("TELEFONE", cliente.telefone);
        cVal.put("ENDERECO", cliente.endereço);
        cVal.put("DETALHES", cliente.detalhes);

        conexao.insertOrThrow("CLIENTES", null, cVal);

        conexao.close();

        backupManager.dataChanged();
    }

    public void alterar(Cliente cliente) {

        conexao = openH.getWritableDatabase();

        ContentValues cVal = new ContentValues();
        cVal.put("NOME", cliente.nome);
        cVal.put("TELEFONE", cliente.telefone);
        cVal.put("ENDERECO", cliente.endereço);
        cVal.put("DETALHES", cliente.detalhes);

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(cliente.id);
        conexao.update("CLIENTES", cVal, "ID = ?", parametros);

        conexao.close();

        backupManager.dataChanged();
    }

    public void excluir(int id) {

        conexao = openH.getWritableDatabase();

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(id);

        conexao.delete("CLIENTES", "ID = ?", parametros);

        conexao.close();

        backupManager.dataChanged();
    }

    public List<Cliente> buscarTodos() {

        conexao = openH.getReadableDatabase();

        List<Cliente> clientes = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("   SELECT ID, NOME, TELEFONE, ENDERECO, DETALHES");
        sql.append("   FROM CLIENTES");
        Cursor resultado = conexao.rawQuery(sql.toString(), null);
        if (resultado.getCount() > 0) {
            resultado.moveToFirst();
            do {
                Cliente cli = new Cliente();
                cli.id = resultado.getInt(resultado.getColumnIndexOrThrow("ID"));
                cli.nome = resultado.getString(resultado.getColumnIndexOrThrow("NOME"));
                cli.telefone = resultado.getString(resultado.getColumnIndexOrThrow("TELEFONE"));
                cli.endereço = resultado.getString(resultado.getColumnIndexOrThrow("ENDERECO"));
                cli.detalhes = resultado.getString(resultado.getColumnIndexOrThrow("DETALHES"));
                clientes.add(cli);
            } while (resultado.moveToNext());
        }

        conexao.close();

        return clientes;

    }

    public Cliente buscarCliente(int id) {

        conexao = openH.getReadableDatabase();

        Cliente cli = new Cliente();

        StringBuilder sql = new StringBuilder();
        sql.append("   SELECT ID, NOME, TELEFONE, ENDERECO, DETALHES");
        sql.append("   FROM CLIENTES");
        sql.append(" WHERE ID = ?");

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(id);

        Cursor resultado = conexao.rawQuery(sql.toString(), parametros);

        if (resultado.getCount() > 0) {
            resultado.moveToFirst();

            cli.id = resultado.getInt(resultado.getColumnIndexOrThrow("ID"));
            cli.nome = resultado.getString(resultado.getColumnIndexOrThrow("NOME"));
            cli.telefone = resultado.getString(resultado.getColumnIndexOrThrow("TELEFONE"));
            cli.endereço = resultado.getString(resultado.getColumnIndexOrThrow("ENDERECO"));
            cli.detalhes = resultado.getString(resultado.getColumnIndexOrThrow("DETALHES"));

            conexao.close();

            return cli;
        }

        conexao.close();

        return null;
    }

    public void atualizarClientes(){
        VendasRepositorio vendRep = new VendasRepositorio(context);
        for(Venda venda:vendRep.buscarTodos()){
            if(buscarClientePeloNome(venda.nome) == null){
                Cliente cliente = new Cliente();
                cliente.nome = venda.nome;
                inserir(cliente);
            }
        }
    }

    public boolean backupBanco(){
        try{
            InputStream inputStream = new FileInputStream(
                    new File(Environment.getDataDirectory() + "/data/com.example.controlemk/databases/CLIENTES"));

            File pasta = new File(Environment.getExternalStorageDirectory() + "/ControleMK/Backups");
            if(!pasta.exists()){
                pasta.mkdirs();
            }
            OutputStream outputStream = new FileOutputStream(
                    new File(pasta + "/CLIENTES_bkp"));

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

    public boolean restaurarBackupBanco(){
        try{
            InputStream inputStream = new FileInputStream(
                    new File(Environment.getExternalStorageDirectory() + "/ControleMK/Backups/CLIENTES_bkp"));

            OutputStream outputStream = new FileOutputStream(
                    new File(Environment.getDataDirectory() + "/data/com.example.controlemk/databases/CLIENTES"));

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

    public Cliente buscarClientePeloNome(String nome){
        nome = nome.replaceAll(" ","");
        for(Cliente cli:buscarTodos()){
            String nomeSemEspaço = cli.nome.replaceAll(" ","");
            if(nomeSemEspaço.contentEquals(nome)){
                return cli;
            }
        }
        return null;
    }

    public List<String> buscarNomesDosClientes(){
        List<String> clientes = new ArrayList<>();
        for(Cliente cli:buscarTodos()){
            clientes.add(cli.nome);
        }
        Collections.sort(clientes, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        return clientes;
    }

    public List<Venda> debitosClientes(){
        List<Venda> debitos = new ArrayList<>();
        for(Cliente cli:buscarTodos()){
            for(Venda venda:cli.vendasPorPagamentosCliente(context)){
;                if(venda.datasNaoPagas.contains(venda.datasPag.get(0))){
                    debitos.add(venda);
                }
            }
        }
        return debitos;
    }

    public List<Venda> debitosDeHoje(){
        OperacoesDatas opDatas = new OperacoesDatas(context);
        List<Venda> debitosHoje = new ArrayList<>();
        for(Venda venda:debitosClientes()){
            if(venda.datasPag.get(0).contains(opDatas.dataAtual())){
                debitosHoje.add(venda);
            }
        }
        return debitosHoje;
    }

    public List<Venda> parcPagasClientes(){
        List<Venda> parcelas = new ArrayList<>();
        for(Cliente cli:buscarTodos()){
            for(Venda venda:cli.vendasPorPagamentosCliente(context)){
                if(!venda.datasNaoPagas.contains(venda.datasPag.get(0))){
                    parcelas.add(venda);
                }
            }
        }
        return parcelas;
    }

    public List<Venda> todasParcClientes(){
        List<Venda> parcelas = new ArrayList<>();
        for(Cliente cli:buscarTodos()){
            for(Venda venda:cli.vendasPorPagamentosCliente(context)){
                parcelas.add(venda);
            }
        }
        return parcelas;
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "Pagamentos Diários";
            String description = "Notificações que mostrarão os pagamentos que vencem no dia, assim como o valor total destes pagamentos";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyDebito",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void definirNotificacoes(boolean notifDebitosHoje){

        createNotificationChannel();

        OperacoesDatas opDatas = new OperacoesDatas(context);
        int codeExcluir = 0;
        List<Venda> debitos = debitosClientes();
        if (debitos.size() > 0) {

            int code = 1;
            List<String> datasNotif = new ArrayList<>();
            for (Venda debito:debitos) {
                String data = debito.datasPag.get(0).split("=")[0];
                    if (!datasNotif.contains(data) && ((opDatas.dataEmDia(data) && notifDebitosHoje) || (!notifDebitosHoje && opDatas.eNotifFutura(data,horaNotif,minutoNotif)))) {
                        datasNotif.add(data);
                        int dia = Integer.parseInt(data.substring(0,2));
                        int mes = Integer.parseInt(data.substring(3,5));
                        int ano = Integer.parseInt(data.substring(6,10));

                        Calendar dataNotif = Calendar.getInstance();
                        dataNotif.set(ano,mes-1,dia, horaNotif, minutoNotif,0);

                        Intent intent = new Intent(context, Receiver.class);
                        PendingIntent pendingIntent;
                        while (codeExcluir <= 90) {         //Excluir todas as notificações
                            pendingIntent = PendingIntent.getBroadcast(context, codeExcluir,intent,PendingIntent.FLAG_NO_CREATE);
                            if (pendingIntent != null) {
                                pendingIntent.cancel();
                            }
                            codeExcluir++;
                        }
                        pendingIntent = PendingIntent.getBroadcast(context, code,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                        alarm.set(AlarmManager.RTC_WAKEUP, dataNotif.getTimeInMillis(), pendingIntent);
                        code++;
                    }
            }
        }
    }

    public void definirConfigs(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        notif = prefs.getBoolean("switch_notific", true);
        String horaConfig = prefs.getString("horaNotif", "09:00");
        horaNotif = Integer.parseInt(horaConfig.substring(0,2));
        minutoNotif = Integer.parseInt(horaConfig.substring(3,5));
    }



    public List<Cliente> ordenarClientesAZ() {
        List<Cliente> cliOrdenados = buscarTodos();
        Collections.sort(cliOrdenados, new Comparator<Cliente>() {
            @Override
            public int compare(Cliente o1, Cliente o2) {
                return o1.nome.compareTo(o2.nome);
            }
        });
        return cliOrdenados;
    }

    public List<Cliente> ordenarClientesZA() {
        List<Cliente> cliOrdenados = ordenarClientesAZ();
        Collections.reverse(cliOrdenados);
        return cliOrdenados;
    }

    public List<Cliente> ordenarClientesPorDatasDown() {
        List<Cliente> dados = buscarTodos();
        List<Cliente> cliSemVenda = new ArrayList<>();
        List<Cliente> cliComVenda = dados;
        for (int i1 = dados.size() - 1; i1 >= 1; i1--) {
            if (dados.get(i1).ultimaVendaCliente(context) != null) {
                for (int i2 = i1 - 1; i2 >= 0; i2--) {
                    if (dados.get(i2).ultimaVendaCliente(context) != null) {
                        int ano1 = Integer.parseInt(dados.get(i1).ultimaVendaCliente(context).dataVenda.substring(6));
                        int mes1 = Integer.parseInt(dados.get(i1).ultimaVendaCliente(context).dataVenda.substring(3, 5));
                        int dia1 = Integer.parseInt(dados.get(i1).ultimaVendaCliente(context).dataVenda.substring(0, 2));
                        int ano2 = Integer.parseInt(dados.get(i2).ultimaVendaCliente(context).dataVenda.substring(6));
                        int mes2 = Integer.parseInt(dados.get(i2).ultimaVendaCliente(context).dataVenda.substring(3, 5));
                        int dia2 = Integer.parseInt(dados.get(i2).ultimaVendaCliente(context).dataVenda.substring(0, 2));
                        if (ano1 < ano2) {
                            dados.add(i1 + 1, dados.get(i2));
                            dados.remove(i2);
                        } else if (ano1 == ano2) {
                            if (mes1 < mes2) {
                                dados.add(i1 + 1, dados.get(i2));
                                dados.remove(i2);
                            } else if (mes1 == mes2) {
                                if (dia1 < dia2) {
                                    dados.add(i1 + 1, dados.get(i2));
                                    dados.remove(i2);
                                }
                            }
                        }
                    }else if(!cliSemVenda.contains(dados.get(i2))){
//                        cliComVenda.remove(dados.get(i2));
                        cliSemVenda.add(dados.get(i2));
                    }
                }
            }else if(!cliSemVenda.contains(dados.get(i1))){
//                cliComVenda.remove(dados.get(i1));
                cliSemVenda.add(dados.get(i1));
            }
        }
        dados.removeAll(cliSemVenda);
        dados.addAll(cliSemVenda);
//        cliComVenda.addAll(cliSemVenda);
        return dados;
    }

    public List<Cliente> ordenarClientesPorDatasUp() {
        List<Cliente> dados = ordenarClientesPorDatasDown();
        List<Cliente> cliSemVenda = new ArrayList<>();
        for(Cliente cli:dados){
            if(cli.ultimaVendaCliente(context) == null){
                cliSemVenda.add(cli);
            }
        }
        Collections.reverse(dados);
        dados.removeAll(cliSemVenda);
        dados.addAll(cliSemVenda);
        return dados;
    }

}

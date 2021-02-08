package com.example.controlemk.Dominio.Repositorio;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.controlemk.Adapters.VendasClienteAdapter;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.database.BancoOpenHelper;
import com.example.controlemk.database.VendOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class VendasRepositorio {

    public SQLiteDatabase conexao;
    public BancoOpenHelper openH;

    public ProdutosRepositorio prodRep;
    public Context context;

    public BackupManager backupManager;

    public VendasRepositorio(Context context) {
        this.context = context;
        openH = new BancoOpenHelper(context);
            conexao = openH.getWritableDatabase();
//        BancoOpenHelper openH = new BancoOpenHelper(context);
        prodRep = new ProdutosRepositorio(context);
        backupManager = new BackupManager(context);
    }

    public void inserir(Venda venda){

        conexao = openH.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("DATA_VENDA", venda.dataVenda);
        cv.put("NOME", venda.nome);
        cv.put("PRODUTOS",venda.convertProdListParaString(venda.produtos));
        cv.put("TOTAL",venda.total);
        cv.put("DATAS_PAG",venda.convertDatasPagParaString(venda.datasPag));
        cv.put("DATAS_NAO_PAGAS",venda.convertDatasPagParaString(venda.datasPag));
        cv.put("EFETIVADA", venda.efetivada);

        conexao.insertOrThrow("VENDAS",null,cv);

        conexao.close();

        backupManager.dataChanged();
    }

    public void alterar(Venda venda){

        conexao = openH.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("DATA_VENDA", venda.dataVenda);
        cv.put("NOME", venda.nome);
        cv.put("PRODUTOS",venda.convertProdListParaString(venda.produtos));
        cv.put("TOTAL",venda.calcValTotVenda());
        cv.put("DATAS_PAG",venda.convertDatasPagParaString(venda.datasPag));
        cv.put("DATAS_NAO_PAGAS",venda.convertDatasPagParaString(venda.datasNaoPagas));
        cv.put("EFETIVADA", venda.efetivada);

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(venda.id);

        conexao.update("VENDAS",cv,"ID = ?",parametros);

        conexao.close();

        backupManager.dataChanged();
    }

    public void excluir(int codigo){

        conexao = openH.getWritableDatabase();

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(codigo);
        conexao.delete("VENDAS","ID = ?",parametros);

        conexao.close();

        backupManager.dataChanged();
    }

    public List<Venda> buscarTodos(){

//        openH = new VendOpenHelper(context);
        conexao = openH.getWritableDatabase();

        List<Venda> vendas= new ArrayList<Venda>();

        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ID,DATA_VENDA,NOME,PRODUTOS,TOTAL,DATAS_PAG,DATAS_NAO_PAGAS, EFETIVADA");
        sql.append(" FROM VENDAS");
        Cursor resultado = conexao.rawQuery(sql.toString(),null);
        if (resultado.getCount() > 0) {
            resultado.moveToFirst();
            do {
                Venda venda = new Venda();

                venda.id = resultado.getInt(resultado.getColumnIndexOrThrow("ID"));
                venda.dataVenda = resultado.getString(resultado.getColumnIndexOrThrow("DATA_VENDA"));
                venda.nome = resultado.getString(resultado.getColumnIndexOrThrow("NOME"));
                venda.produtos = venda.convertStringParaProdList(resultado.getString(resultado.getColumnIndexOrThrow("PRODUTOS")));
                venda.total = resultado.getFloat(resultado.getColumnIndexOrThrow("TOTAL"));
                venda.datasPag = venda.convertStringParaDatasPag(resultado.getString(resultado.getColumnIndexOrThrow("DATAS_PAG")));
                venda.datasNaoPagas = venda.convertStringParaDatasPag(resultado.getString(resultado.getColumnIndexOrThrow("DATAS_NAO_PAGAS")));
                venda.efetivada = resultado.getInt(resultado.getColumnIndexOrThrow("EFETIVADA")) > 0;

                if (venda.arrumarValoresParcelas()) {
                    alterar(venda);
                }
                vendas.add(venda);

            } while (resultado.moveToNext());
        }

        conexao.close();

        return vendas;
    }

    public List<Venda> buscarTodasEfetivadas(){
        List<Venda> vendasEfetiv = new ArrayList<>();
        for(Venda venda:buscarTodos()){
            if(venda.efetivada){
                vendasEfetiv.add(venda);
            }
        }
        return vendasEfetiv;
    }

    public List<Venda> buscarTodasEncomendas(){
        List<Venda> encomendas = new ArrayList<>();
        for(Venda venda:buscarTodos()){
            if(!venda.efetivada){
                encomendas.add(venda);
            }
        }
        return encomendas;
    }


    public Venda buscarVenda(int codigo){

        conexao = openH.getWritableDatabase();

        Venda venda = new Venda();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ID,DATA_VENDA,NOME,PRODUTOS,TOTAL,DATAS_PAG,DATAS_NAO_PAGAS, EFETIVADA");
        sql.append("  FROM VENDAS");
        sql.append("  WHERE ID = ?");

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(codigo);

        Cursor resultado = conexao.rawQuery(sql.toString(),parametros);

        if (resultado.getCount() > 0) {
            resultado.moveToFirst();
            venda.id = resultado.getInt(resultado.getColumnIndexOrThrow("ID"));
            venda.dataVenda = resultado.getString(resultado.getColumnIndexOrThrow("DATA_VENDA"));
            venda.nome = resultado.getString(resultado.getColumnIndexOrThrow("NOME"));
            venda.produtos = venda.convertStringParaProdList(resultado.getString(resultado.getColumnIndexOrThrow("PRODUTOS")));
            venda.total = resultado.getFloat(resultado.getColumnIndexOrThrow("TOTAL"));
            venda.datasPag = venda.convertStringParaDatasPag(resultado.getString(resultado.getColumnIndexOrThrow("DATAS_PAG")));
            venda.datasNaoPagas = venda.convertStringParaDatasPag(resultado.getString(resultado.getColumnIndexOrThrow("DATAS_NAO_PAGAS")));
            venda.efetivada = resultado.getInt(resultado.getColumnIndexOrThrow("EFETIVADA")) > 0;

            conexao.close();

            return venda;
        }
        conexao.close();

        return null;
    }

    public void limparLista(){

        conexao = openH.getWritableDatabase();

        String[] parametros = new String[1];
        conexao.delete("VENDAS",null,null);
        backupManager.dataChanged();

        conexao.close();

    }

    public void novaLista(List<Venda> vendas){
        limparLista();
        for (Venda vend:vendas){
            inserir(vend);
        }
    }

    public boolean backupBanco(){
        try{
            InputStream inputStream = new FileInputStream(
                    new File(Environment.getDataDirectory() + "/data/com.example.controlemk/databases/VENDAS"));

            File pasta = new File(Environment.getExternalStorageDirectory() + "/ControleMK/Backups");
            if(!pasta.exists()){
                pasta.mkdirs();
            }
            OutputStream outputStream = new FileOutputStream(
                    new File(pasta + "/VENDAS_bkp"));

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
        int versao = context.openOrCreateDatabase("VENDAS",Context.MODE_ENABLE_WRITE_AHEAD_LOGGING,null).getVersion();
        try{
            InputStream inputStream = new FileInputStream(
                    new File(Environment.getExternalStorageDirectory() + "/ControleMK/Backups/VENDAS_bkp"));

            OutputStream outputStream = new FileOutputStream(
                    new File(Environment.getDataDirectory() + "/data/com.example.controlemk/databases/VENDAS"));

            byte[] buffer = new byte[1024];
            int comprimento;

            while((comprimento = inputStream.read(buffer)) > 0){
                outputStream.write(buffer, 0, comprimento);
            }


//            int versao2 = context.openOrCreateDatabase("VENDAS",Context.MODE_ENABLE_WRITE_AHEAD_LOGGING,null).getVersion();
//            context.openOrCreateDatabase("VENDAS",Context.MODE_PRIVATE,null).setVersion(versao);

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

    public List<Venda> vendasComProd(List<Venda> vendas,int id){
        List<Venda> resp = new ArrayList<>();
        for(Venda vend:vendas){
            for(Produto prod:vend.produtos){
                if(prod.id == id){
                    resp.add(vend);
                    break;
                }
            }
        }
        return resp;
    }

    public List<Venda> ordenarVendasPorDataDown(List<Venda> vendas) {
        final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        Collections.sort(vendas, new Comparator<Venda>() {
            @Override
            public int compare(Venda o1, Venda o2) {
                Date d1 = null;
                Date d2 = null;
                try {
                    d1 = formato.parse(o1.dataVenda);
                    d2 = formato.parse(o2.dataVenda);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return d1.compareTo(d2);
            }
        });
        return vendas;
    }

    public List<Venda> ordenarVendasPorDataUp(List<Venda> vendas) {
        final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        Collections.sort(vendas, new Comparator<Venda>() {
            @Override
            public int compare(Venda o1, Venda o2) {
                Date d1 = null;
                Date d2 = null;
                try {
                    d1 = formato.parse(o1.dataVenda);
                    d2 = formato.parse(o2.dataVenda);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return d2.compareTo(d1);
            }
        });
        return vendas;
    }

    public List<Venda> ordenarVendasPorDataPagamentoDown(List<Venda> vendas) {
        final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        Collections.sort(vendas, new Comparator<Venda>() {
            @Override
            public int compare(Venda o1, Venda o2) {
                Date d1 = null;
                Date d2 = null;
                try {
                    d1 = formato.parse(o1.datasPag.get(0).split("=")[0]);
                    d2 = formato.parse(o2.datasPag.get(0).split("=")[0]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return d1.compareTo(d2);
            }
        });
        return vendas;
    }

    public List<Venda> ordenarVendasPorDataPagamentoUp(List<Venda> vendas) {
        final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        Collections.sort(vendas, new Comparator<Venda>() {
            @Override
            public int compare(Venda o1, Venda o2) {
                Date d1 = null;
                Date d2 = null;
                try {
                    d1 = formato.parse(o1.datasPag.get(0).split("=")[0]);
                    d2 = formato.parse(o2.datasPag.get(0).split("=")[0]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return d2.compareTo(d1);
            }
        });
        return vendas;
    }

    public List<Venda> ordenarClientesAZ(List<Venda> vendas) {
        List<Venda> cliOrdenados = vendas;
        Collections.sort(cliOrdenados, new Comparator<Venda>() {
            @Override
            public int compare(Venda o1, Venda o2) {
                return o1.nome.compareTo(o2.nome);
            }
        });
        return cliOrdenados;
    }

    public List<Venda> ordenarClientesZA(List<Venda> vendas) {
        List<Venda> cliOrdenados = ordenarClientesAZ(vendas);
        Collections.reverse(cliOrdenados);
        return cliOrdenados;
    }

    public float somaTotalVendas(List<Venda> vendas){
        float soma = 0.0f;
        for(Venda vend:vendas){
            soma += vend.total;
        }
        return soma;
    }
}

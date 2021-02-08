package com.example.controlemk.Dominio.Repositorio;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.controlemk.Dominio.Entidades.Pedido;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.database.BancoOpenHelper;
import com.example.controlemk.database.PedidoOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PedidosRepositorio {

    public SQLiteDatabase conexao;
    private BancoOpenHelper openH;

    public ProdutosRepositorio prodRep;
    private BackupManager backupManager;
    public Context context;


    public PedidosRepositorio(Context context) {
        this.context = context;
        openH = new BancoOpenHelper(context);
        prodRep = new ProdutosRepositorio(context);
        backupManager = new BackupManager(context);
    }

    public void inserir(Pedido pedido){

        conexao = openH.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("DATA", pedido.data);
        cv.put("PRODUTOS",pedido.convertProdListParaString(pedido.produtos));
        cv.put("TOTAL_BRUTO",pedido.totalBruto);
        cv.put("TOTAL_CUSTO",pedido.totalCusto);
        cv.put("DESCONTO",pedido.desconto);

        conexao.insertOrThrow("PEDIDOS",null,cv);

        conexao.close();

        backupManager.dataChanged();
    }

    public void alterar(Pedido pedido){

        conexao = openH.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("DATA", pedido.data);
        cv.put("PRODUTOS",pedido.convertProdListParaString(pedido.produtos));
        cv.put("TOTAL_BRUTO",pedido.totalBruto);
        cv.put("TOTAL_CUSTO",pedido.totalCusto);
        cv.put("DESCONTO",pedido.desconto);

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(pedido.id);

        conexao.update("PEDIDOS",cv,"ID = ?",parametros);

        conexao.close();

        backupManager.dataChanged();
    }

    public void excluir(int codigo){

        conexao = openH.getWritableDatabase();

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(codigo);
        conexao.delete("PEDIDOS","ID = ?",parametros);

        conexao.close();

        backupManager.dataChanged();
    }

    public List<Pedido> buscarTodos(){

        conexao = openH.getReadableDatabase();

        List<Pedido> pedidos= new ArrayList<Pedido>();

        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ID,DATA,PRODUTOS,TOTAL_BRUTO,TOTAL_CUSTO,DESCONTO");
        sql.append(" FROM PEDIDOS");
        Cursor resultado = conexao.rawQuery(sql.toString(),null);
        if (resultado.getCount() > 0) {
            resultado.moveToFirst();
            do {
                Pedido pedido = new Pedido();
                pedido.id = resultado.getInt(resultado.getColumnIndexOrThrow("ID"));
                pedido.data = resultado.getString(resultado.getColumnIndexOrThrow("DATA"));
                pedido.produtos = pedido.convertStringParaProdList(resultado.getString(resultado.getColumnIndexOrThrow("PRODUTOS")));
                pedido.totalBruto = resultado.getFloat(resultado.getColumnIndexOrThrow("TOTAL_BRUTO"));
                pedido.totalCusto = resultado.getFloat(resultado.getColumnIndexOrThrow("TOTAL_CUSTO"));
                pedido.desconto = resultado.getFloat(resultado.getColumnIndexOrThrow("DESCONTO"));
                consertarProdsPedido(pedido);
                pedidos.add(pedido);

            } while (resultado.moveToNext());
        }

        conexao.close();

        return pedidos;
    }


    public Pedido buscarPedido(int codigo){

        conexao = openH.getReadableDatabase();

        Pedido pedido = new Pedido();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ID,DATA,PRODUTOS,TOTAL_BRUTO,TOTAL_CUSTO,DESCONTO");
        sql.append(" FROM PEDIDOS");
        sql.append("  WHERE ID = ?");

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(codigo);

        Cursor resultado = conexao.rawQuery(sql.toString(),parametros);

        if (resultado.getCount() > 0) {
            resultado.moveToFirst();
            pedido.id = resultado.getInt(resultado.getColumnIndexOrThrow("ID"));
            pedido.data = resultado.getString(resultado.getColumnIndexOrThrow("DATA"));
            pedido.produtos = pedido.convertStringParaProdList(resultado.getString(resultado.getColumnIndexOrThrow("PRODUTOS")));
            pedido.totalBruto = resultado.getFloat(resultado.getColumnIndexOrThrow("TOTAL_BRUTO"));
            pedido.totalCusto = resultado.getFloat(resultado.getColumnIndexOrThrow("TOTAL_CUSTO"));
            pedido.desconto = resultado.getFloat(resultado.getColumnIndexOrThrow("DESCONTO"));

            conexao.close();

            return pedido;
        }

        conexao.close();

        return null;
    }

    public void consertarProdsPedido(Pedido ped){
        boolean resp = false;
        ProdutosRepositorio prodRep = new ProdutosRepositorio(context);
        List<Produto> prodsNovos = new ArrayList<>();
        for(Produto prod:ped.produtos){
            if(prodRep.buscarprodId(prod.id) == null){
                resp = true;
                prod.id = prodRep.buscarIdProduto(prod.nome);
                if(prod.id == 0){
                    continue;
                }
                prod.codigo = prodRep.buscarprodId(prod.id).codigo;
            }
            prodsNovos.add(prod);
        }
        ped.produtos.clear();
        ped.produtos.addAll(prodsNovos);
        if(resp){
            alterar(ped);
        }
    }

    public boolean backupBanco(){
        try{
            InputStream inputStream = new FileInputStream(
                    new File(Environment.getDataDirectory() + "/data/com.example.controlemk/databases/PEDIDOS"));

            File pasta = new File(Environment.getExternalStorageDirectory() + "/ControleMK/backups");
            if(!pasta.exists()){
                pasta.mkdirs();
            }
            OutputStream outputStream = new FileOutputStream(
                    new File(pasta + "/PEDIDOS_bkp"));

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
                    new File(Environment.getExternalStorageDirectory() + "ControleMK/backups/PEDIDOS_bkp"));

            OutputStream outputStream = new FileOutputStream(
                    new File(Environment.getDataDirectory() + "/data/com.example.controlemk/databases/PEDIDOS"));

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


    public void limparLista(){

        conexao = openH.getWritableDatabase();

        String[] parametros = new String[1];
        conexao.delete("PEDIDOS",null,null);

        conexao.close();

        backupManager.dataChanged();
    }

    public void novaLista(List<Pedido> pedidos){
        limparLista();
        for (Pedido pedido:pedidos){
            inserir(pedido);
        }
    }

    public List<Pedido> pedidosComProd(List<Pedido> pedidos,int id){
        List<Pedido> resp = new ArrayList<>();
        for(Pedido ped:pedidos){
            for(Produto prod:ped.produtos){
                if(prod.id == id){
                    resp.add(ped);
                    break;
                }
            }
        }
        return resp;
    }

    public boolean eBissexto(int ano){
        boolean resp = false;
        if ((ano%400 == 0) || ((ano%4==0) && !(ano%100==0))){
            resp = true;
        }
        return resp;
    }

    public List<Pedido> ordenarPedidosPorDataDown(List<Pedido> pedidos) {
        List<Pedido> pedidosOrd = pedidos;

        for (int i1 = pedidos.size() - 1; i1 >= 1; i1--) {
            for (int i2 = i1 - 1; i2 >= 0; i2--) {
                String data1 = pedidos.get(i1).data;
                String data2 = pedidos.get(i2).data;
                int ano1 = Integer.parseInt(data1.substring(6,10));
                int mes1 = Integer.parseInt(data1.substring(3, 5));
                int dia1 = Integer.parseInt(data1.substring(0, 2));
                int ano2 = Integer.parseInt(data2.substring(6, 10));
                int mes2 = Integer.parseInt(data2.substring(3, 5));
                int dia2 = Integer.parseInt(data2.substring(0, 2));
                int hora1 = 0;
                int minuto1 = 0;
                int hora2 = 0;
                int minuto2 = 0;
                if (data1.length() > 13 && data1.length() > 13) {
                    hora1 = Integer.parseInt(data1.substring(13, 15));
                    minuto1 = Integer.parseInt(data1.substring(16, 18));
                    hora2 = Integer.parseInt(data2.substring(13, 15));
                    minuto2 = Integer.parseInt(data2.substring(16, 18));
                }
                if (ano1 < ano2) {
                    pedidosOrd.add(i1 + 1, pedidosOrd.get(i2));
                    pedidosOrd.remove(i2);
                } else if (ano1 == ano2) {
                    if (mes1 < mes2) {
                        pedidosOrd.add(i1 + 1, pedidosOrd.get(i2));
                        pedidosOrd.remove(i2);
                    } else if (mes1 == mes2) {
                        if (dia1 < dia2) {
                            pedidosOrd.add(i1 + 1, pedidosOrd.get(i2));
                            pedidosOrd.remove(i2);
                        } else if (dia1 == dia2) {
                            if (hora1 < hora2) {
                                pedidosOrd.add(i1 + 1, pedidosOrd.get(i2));
                                pedidosOrd.remove(i2);
                            } else if (hora1 == hora2) {
                                if (minuto1 < minuto2) {
                                    pedidosOrd.add(i1 + 1, pedidosOrd.get(i2));
                                    pedidosOrd.remove(i2);
                                }
                            }
                        }
                    }
                }
            }
        }
        return pedidosOrd;
    }

    public List<Pedido> ordenarPedidosPorDataUp(List<Pedido> pedidos) {
        List<Pedido> dados = ordenarPedidosPorDataDown(pedidos);
        Collections.reverse(dados);
        return dados;
    }

    public List<Pedido> ordenarPedidosPorCustoDown(List<Pedido> pedidos) {


        Collections.sort(pedidos, new Comparator<Pedido>() {
            @Override
            public int compare(Pedido o1, Pedido o2) {
                return ((int) (o1.totalCusto - o2.totalCusto));
            }
        });

        return pedidos;
    }

    public List<Pedido> ordenarPedidosPorCustoUp(List<Pedido> pedidos) {
        List<Pedido> dados = ordenarPedidosPorCustoDown(pedidos);
        Collections.reverse(dados);
        return dados;
    }

    public List<Pedido> ordenarPedidosPorValorBrutoDown(List<Pedido> pedidos) {

        List<Pedido> pedidosOrdenados = pedidos;
        Collections.sort(pedidosOrdenados, new Comparator<Pedido>() {
            @Override
            public int compare(Pedido o1, Pedido o2) {
                return ((int) (o1.totalBruto - o2.totalBruto));
            }
        });

        return pedidosOrdenados;
    }

    public List<Pedido> ordenarPedidosPorValorBrutoUp(List<Pedido> pedidos) {
        List<Pedido> dados = ordenarPedidosPorValorBrutoDown(pedidos);
        Collections.reverse(dados);
        return dados;
    }

    public List<Pedido> ordenarPedidosPorDescontoPercentDown(List<Pedido> pedidos) {

        List<Pedido> pedidosOrdenados = pedidos;
        Collections.sort(pedidosOrdenados, new Comparator<Pedido>() {
            @Override
            public int compare(Pedido o1, Pedido o2) {
                return ((int) (o1.desconto*100 - o2.desconto*100));
            }
        });

        return pedidosOrdenados;
    }

    public List<Pedido> ordenarPedidosPorDescontoPercentUp(List<Pedido> pedidos) {
        List<Pedido> dados = ordenarPedidosPorDescontoPercentDown(pedidos);
        Collections.reverse(dados);
        return dados;
    }


}

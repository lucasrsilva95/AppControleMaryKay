package com.example.controlemk.Dominio.Entidades;

import android.content.Context;

import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.example.controlemk.OperacoesDatas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cliente implements Serializable {
    public int id;
    public String nome;
    public String telefone;
    public String endereço;
    public String detalhes;

    public Cliente() {
        this.telefone = "";
        this.endereço = "";
        this.detalhes = "";
    }

    public List<Venda> vendasCliente(Context context){
        VendasRepositorio vendRep = new VendasRepositorio(context);
        List<Venda> vendasCli = new ArrayList<>();
        for(Venda venda:vendRep.buscarTodos()){
            if(nome.contentEquals(venda.nome) && venda.efetivada){
                vendasCli.add(venda);
            }
        }
        vendasCli = vendRep.ordenarVendasPorDataUp(vendasCli);
        return vendasCli;
    }

    public List<Venda> encomendasCliente(Context context){
        VendasRepositorio vendRep = new VendasRepositorio(context);
        List<Venda> vendasCli = new ArrayList<>();
        for(Venda venda:vendRep.buscarTodos()){
            if(nome.contentEquals(venda.nome) && !venda.efetivada){
                vendasCli.add(venda);
            }
        }
        vendasCli = vendRep.ordenarVendasPorDataUp(vendasCli);
        return vendasCli;
    }

    public List<Venda> encomendasPorProdutoCliente(Context context){
        List<Venda> encomendasPorProd = new ArrayList<>();
        VendasRepositorio vendRep = new VendasRepositorio(context);
        for(Venda vend:encomendasCliente(context)){
            for(Produto prod:vend.produtos){
                Venda v = vendRep.buscarVenda(vend.id);
                v.produtos.clear();
                v.produtos.add(prod);
                encomendasPorProd.add(v);
            }
        }
        return encomendasPorProd;
    }

    public Venda ultimaVendaCliente(Context context){
        if (vendasCliente(context).size() > 0) {
            return vendasCliente(context).get(0);
        } else {
            return null;
        }
    }

    public List<String> datasPagamentosCliente(Context context){
        List<String> datasPag = new ArrayList<>();
        for(Venda venda:vendasCliente(context)){
            for(String data:venda.datasPag){
                if(!datasPag.contains(data)){
                    datasPag.add(data);
                }
            }
        }
        OperacoesDatas op = new OperacoesDatas(context);
        return op.ordenarDatasDown(datasPag);
    }

    public String proxPagamentoCliente(Context context){
        if(debitosCliente(context).size() > 0){
            String pag = debitosCliente(context).get(0).datasPag.get(0).split("=")[0];
            return pag;
        }else{
            return "---";
        }
    }

    public List<Venda> vendasPorPagamentosCliente(Context context){
        List<Venda> vendas = new ArrayList<>();
        for(Venda venda:vendasCliente(context)){
            for (String data:venda.datasPag) {
                VendasRepositorio vendRep = new VendasRepositorio(context);
                Venda v = vendRep.buscarVenda(venda.id);
                v.total = v.total/v.datasPag.size();
                v.datasPag.clear();
                v.datasPag.add(data);
                vendas.add(v);
            }
        }
        return vendas;
    }

    public List<Venda> debitosCliente(Context context){
        VendasRepositorio vendRep = new VendasRepositorio(context);
        List<Venda> listDebitos = new ArrayList<>();
        for(Venda venda:vendasPorPagamentosCliente(context)){
            if(venda.datasNaoPagas.contains(venda.datasPag.get(0))){
                listDebitos.add(venda);
            }
        }
        return vendRep.ordenarVendasPorDataPagamentoDown(listDebitos);
    }

    public float valDebitosCliente(Context context){
        ClientesRepositorio cliRep = new ClientesRepositorio(context);
        float debitos = 0.0f;
        for(Venda venda:vendasPorPagamentosCliente(context)){
            if(venda.datasNaoPagas.contains(venda.datasPag.get(0))){
                debitos += Float.parseFloat((venda.datasPag.get(0).split("=")[1]).replace(",","."));
            }
        }
        return debitos;
    }
    public String convertProdListParaString(List<Produto> prods){
        String result = "";
        if (prods.size() > 0){
            for(Produto prod:prods){
                result = result.concat(String.format("%08d;%s;%d;%s;%.2f;%s-",prod.codigo,prod.nome,prod.quantidade,prod.categoria,prod.preço,prod.detalhes));
            }
            return result;
        }
        return null;
    }

    public List<Produto> convertStringParaProdList(String s){
        List<Produto> produtos = new ArrayList<>();
        if (s != null) {
            s = s.replace(",",".");
            String[] p = s.split("-");
            for(int i=0;i<p.length;i++){
                Produto prod = new Produto();
                String[] cat = p[i].split(";");
                prod.codigo = Integer.parseInt(cat[0]);
                prod.nome = cat[1];
                prod.quantidade = Integer.parseInt(cat[2]);
                prod.categoria = cat[3];
                prod.preço = Float.parseFloat(cat[4]);
                if (cat.length > 5) {
                    prod.detalhes = cat[5];
                }
                produtos.add(prod);
            }
        }
        return produtos;
    }



}

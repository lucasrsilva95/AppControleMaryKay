package com.example.controlemk.Dominio.Entidades;

import android.content.Context;

import com.example.controlemk.Dominio.Repositorio.PedidosRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;

import java.io.Serializable;
import java.util.List;

public class Produto implements Serializable {

    public int id;
    public int codigo;
    public String nome;
    public String categoria;
    public int quantidade;
    public float preço;
    public String detalhes;
    public int quantNoEstoque;

    public Produto() {
        this.detalhes = "";
    }

    public Venda ultimaVenda(Context context){
        VendasRepositorio vendRep = new VendasRepositorio(context);
        for(Venda venda:vendRep.ordenarVendasPorDataUp(vendRep.buscarTodasEfetivadas())){
            if(venda.prodEmVenda(codigo) != null){
                return venda;
            }
        }
        return null;
    }

    public Pedido ultimoPedido(Context context){
        PedidosRepositorio pedRep = new PedidosRepositorio(context);
        List<Pedido> pedidos = pedRep.pedidosComProd(pedRep.buscarTodos(), id);
        if(pedidos.size() > 0){
            return pedidos.get(0);
        }else{
            return null;
        }
    }

    public int unidadesVendidas(Context context){
        VendasRepositorio vendRep = new VendasRepositorio(context);
        int quant = 0;
        for(Venda venda:vendRep.vendasComProd(vendRep.buscarTodasEfetivadas(),id)){
            quant += venda.prodEmVenda(codigo).quantidade;
        }
        return quant;
    }
}

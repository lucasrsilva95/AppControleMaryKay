package com.example.controlemk.Dominio.Entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

public class Pedido implements Serializable {

    public int id;
    public String data;
    public List<Produto> produtos;
    public float totalBruto;
    public float totalCusto;
    public float desconto;

    public Pedido() {
        this.produtos = new ArrayList<Produto>();
    }

    public float calcValTotPedido(){
        float tot = 0.0f;
        for (Produto p:produtos){
            tot += p.quantidade * p.preço;
        }
        return tot;
    }

    public void calcValoresPedido(){
        this.totalBruto = calcValTotPedido();
        this.totalCusto = this.totalBruto * (1-this.desconto);
    }

    public Produto prodNoPedido(int codigoProd){
        List<Produto> prods = produtos;
        for(Produto p:prods){
            if(p.codigo == codigoProd){
                return p;
            }
        }
        return null;
    }

    public int quantProdPedido(){
        int quant = 0;
        for(Produto prod:produtos){
            quant += prod.quantidade;
        }
        return quant;
    }


    public String convertProdListParaString(List<Produto> prods){
        String result = "";
        if (prods.size() > 0){
            for(Produto prod:prods){
                result = result.concat(String.format("%d;&&;%08d;&&;%s;&&;%d;&&;%s;&&;%.2f;&&;%s-&&-",prod.id,prod.codigo,prod.nome,prod.quantidade,prod.categoria,prod.preço,prod.detalhes));
            }
            return result;
        }
        return null;
    }

    public List<Produto> convertStringParaProdList(String s){
        List<Produto> produtos = new ArrayList<>();
        s = s.replace(",",".");
        String[] p = s.split("-&&-");
        for(int i=0;i<p.length;i++){
            Produto prod = new Produto();
            String[] cat = p[i].split(";&&;");
            int pos = 0;
            try{
                prod.id = Integer.parseInt(cat[pos]);
                prod.codigo = Integer.parseInt(cat[pos+1]);
            }catch (NumberFormatException ex){
                pos = -1;
                prod.codigo = 0;
            }
            prod.id = Integer.parseInt(cat[pos+1]);
            prod.nome = cat[pos+2];
            prod.quantidade = Integer.parseInt(cat[pos+3]);
            prod.categoria = cat[pos+4];
            prod.preço = Float.parseFloat(cat[pos+5]);
            if (cat.length == pos+7) {
                prod.detalhes = cat[pos+6];
            }
            produtos.add(prod);
        }
        return produtos;
    }

}

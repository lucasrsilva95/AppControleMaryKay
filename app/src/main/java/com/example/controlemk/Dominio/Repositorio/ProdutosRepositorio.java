package com.example.controlemk.Dominio.Repositorio;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.example.controlemk.Dominio.Entidades.Cliente;
import com.example.controlemk.Dominio.Entidades.Pedido;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.database.BancoOpenHelper;
import com.example.controlemk.database.ProdVendOpenHelper;

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

public class ProdutosRepositorio {

    private SQLiteDatabase conexao;
    private BancoOpenHelper openH;

    private BackupManager backupManager;

    private Context context;

    public ProdutosRepositorio(Context context) {
        this.context = context;
        openH = new BancoOpenHelper(context);
        backupManager = new BackupManager(context);
    }

    public long inserir(Produto produto) {

        conexao = openH.getWritableDatabase();

        ContentValues cVal = new ContentValues();
        cVal.put("CODIGO", produto.codigo);
        cVal.put("NOME", produto.nome);
        cVal.put("CATEGORIA", produto.categoria);
        cVal.put("QUANTIDADE", produto.quantidade);
        cVal.put("QUANT_ESTOQUE", produto.quantNoEstoque);
        cVal.put("PRECO", produto.preço);
        cVal.put("DETALHES", produto.detalhes);


        long id = conexao.insertOrThrow("PRODUTOS", null, cVal);

        conexao.close();

        backupManager.dataChanged();

        return id;

    }

    public void alterar(Produto produto) {

        conexao = openH.getWritableDatabase();

        ContentValues cVal = new ContentValues();
        cVal.put("CODIGO", produto.codigo);
        cVal.put("NOME", produto.nome);
        cVal.put("CATEGORIA", produto.categoria);
        cVal.put("QUANTIDADE", produto.quantidade);
        cVal.put("QUANT_ESTOQUE", produto.quantNoEstoque);
        cVal.put("PRECO", produto.preço);
        cVal.put("DETALHES", produto.detalhes);

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(produto.id);
        conexao.update("PRODUTOS", cVal, "ID = ?", parametros);

        conexao.close();

        backupManager.dataChanged();
    }

    public void excluir(int id) {

        conexao = openH.getWritableDatabase();

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(id);

        conexao.delete("PRODUTOS", "ID = ?", parametros);

        conexao.close();

        excluirProdDasVendasEPedidos(id);
        backupManager.dataChanged();
    }

    public void excluirProdDasVendasEPedidos(int id){
        VendasRepositorio vendRep = new VendasRepositorio(context);
        PedidosRepositorio pedRep = new PedidosRepositorio(context);
        List<Venda> vendasProd = vendRep.vendasComProd(vendRep.buscarTodos(),id);
        for(Venda vend:vendasProd){
            vend.produtos.remove(vend.prodEmVenda(id));
            vendRep.alterar(vend);
        }
        List<Pedido> pedidosProd = pedRep.pedidosComProd(pedRep.buscarTodos(), id);
        for(Pedido ped:pedidosProd){
            ped.produtos.remove(ped.prodNoPedido(id));
            pedRep.alterar(ped);
        }
    }

    public List<Produto> buscarTodos() {

        conexao = openH.getWritableDatabase();

        List<Produto> produtos = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("   SELECT ID,CODIGO, NOME, CATEGORIA, QUANTIDADE, QUANT_ESTOQUE, PRECO, DETALHES");
        sql.append("   FROM PRODUTOS");
        Cursor resultado = conexao.rawQuery(sql.toString(), null);
        if (resultado.getCount() > 0) {
            resultado.moveToFirst();
            do {
                Produto prod = new Produto();
                prod.id = resultado.getInt(resultado.getColumnIndexOrThrow("ID"));
                prod.codigo = resultado.getInt(resultado.getColumnIndexOrThrow("CODIGO"));
                prod.nome = resultado.getString(resultado.getColumnIndexOrThrow("NOME"));
                prod.categoria = resultado.getString(resultado.getColumnIndexOrThrow("CATEGORIA"));
                prod.quantidade = resultado.getInt(resultado.getColumnIndexOrThrow("QUANTIDADE"));
                prod.quantNoEstoque = resultado.getInt(resultado.getColumnIndexOrThrow("QUANT_ESTOQUE"));
                prod.preço = resultado.getFloat(resultado.getColumnIndexOrThrow("PRECO"));
                prod.detalhes = resultado.getString(resultado.getColumnIndexOrThrow("DETALHES"));

                produtos.add(prod);
            } while (resultado.moveToNext());
        }

        conexao.close();

        return produtos;

    }

    public Produto buscarprodId(int id) {

        conexao = openH.getWritableDatabase();

        Produto prod = new Produto();

        StringBuilder sql = new StringBuilder();
        sql.append("   SELECT ID, CODIGO, NOME, CATEGORIA, QUANTIDADE, QUANT_ESTOQUE, PRECO, DETALHES");
        sql.append("   FROM PRODUTOS");
        sql.append(" WHERE ID = ?");

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(id);

        Cursor resultado = conexao.rawQuery(sql.toString(), parametros);

        if (resultado.getCount() > 0) {
            resultado.moveToFirst();

            prod.id = resultado.getInt(resultado.getColumnIndexOrThrow("ID"));
            prod.codigo = resultado.getInt(resultado.getColumnIndexOrThrow("CODIGO"));
            prod.nome = resultado.getString(resultado.getColumnIndexOrThrow("NOME"));
            prod.categoria = resultado.getString(resultado.getColumnIndexOrThrow("CATEGORIA"));
            prod.quantidade = resultado.getInt(resultado.getColumnIndexOrThrow("QUANTIDADE"));
            prod.quantNoEstoque = resultado.getInt(resultado.getColumnIndexOrThrow("QUANT_ESTOQUE"));
            prod.preço = resultado.getFloat(resultado.getColumnIndexOrThrow("PRECO"));
            prod.detalhes = resultado.getString(resultado.getColumnIndexOrThrow("DETALHES"));

            conexao.close();

            return prod;
        }

        conexao.close();

        return null;
    }

    public Produto buscarprodCodigo(int codigo) {

        conexao = openH.getWritableDatabase();

        Produto prod = new Produto();

        StringBuilder sql = new StringBuilder();
        sql.append("   SELECT ID, CODIGO, NOME, CATEGORIA, QUANTIDADE, QUANT_ESTOQUE, PRECO, DETALHES");
        sql.append("   FROM PRODUTOS");
        sql.append(" WHERE CODIGO = ?");

        String[] parametros = new String[1];
        parametros[0] = Integer.toString(codigo);

        Cursor resultado = conexao.rawQuery(sql.toString(), parametros);

        if (resultado.getCount() > 0) {
            resultado.moveToFirst();

            prod.id = resultado.getInt(resultado.getColumnIndexOrThrow("ID"));
            prod.codigo = resultado.getInt(resultado.getColumnIndexOrThrow("CODIGO"));
            prod.nome = resultado.getString(resultado.getColumnIndexOrThrow("NOME"));
            prod.categoria = resultado.getString(resultado.getColumnIndexOrThrow("CATEGORIA"));
            prod.quantidade = resultado.getInt(resultado.getColumnIndexOrThrow("QUANTIDADE"));
            prod.quantNoEstoque = resultado.getInt(resultado.getColumnIndexOrThrow("QUANT_ESTOQUE"));
            prod.preço = resultado.getFloat(resultado.getColumnIndexOrThrow("PRECO"));
            prod.detalhes = resultado.getString(resultado.getColumnIndexOrThrow("DETALHES"));

            conexao.close();

            return prod;
        }

        conexao.close();

        return null;
    }

    public void limparLista(){

        conexao = openH.getWritableDatabase();

        String[] parametros = new String[1];
        conexao.delete("PRODUTOS",null,null);

        conexao.close();

        backupManager.dataChanged();
    }

    public Produto listContemProd(List<Produto> list, Produto prod){
        for(Produto p:list){
            if(p.codigo == prod.codigo){
                return p;
            }
        }
        return null;
    }

    public List<Produto> prodsComVendas(List<Produto> prods){
        List<Produto> prodsComVenda = new ArrayList<>();
        for(Produto p:prods){
            if(p.ultimaVenda(context) != null){
                prodsComVenda.add(p);
            }
        }
        return prodsComVenda;
    }

    public List<Produto> ordenarProdutosAZ() {
        List<Produto> cliOrdenados = buscarTodos();
        Collections.sort(cliOrdenados, new Comparator<Produto>() {
            @Override
            public int compare(Produto o1, Produto o2) {
                return o1.nome.compareTo(o2.nome);
            }
        });
        return cliOrdenados;
    }

    public List<Produto> ordenarProdutosZA() {
        List<Produto> cliOrdenados = ordenarProdutosAZ();
        Collections.reverse(cliOrdenados);
        return cliOrdenados;
    }

    public List<Produto> ordenarProdutosPorUltimaVendaDown(List<Produto> produtos) {
        final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        List<Produto> produtosComVenda = prodsComVendas(produtos);
        List<Produto> produtosSemVenda = buscarTodos();
        produtosSemVenda.removeAll(produtosComVenda);
        Collections.sort(produtosComVenda, new Comparator<Produto>() {
            @Override
            public int compare(Produto o1, Produto o2) {
                Date d1 = null;
                Date d2 = null;
                try {
                    d1 = formato.parse(o1.ultimaVenda(context).dataVenda);
                    d2 = formato.parse(o2.ultimaVenda(context).dataVenda);
                } catch (ParseException e){
                    e.printStackTrace();
                }
                return d1.compareTo(d2);
            }
        });
        produtosComVenda.addAll(produtosSemVenda);
        return produtosComVenda;
    }

    public List<Produto> ordenarProdutosPorUltimaVendaUp(List<Produto> produtos) {
        final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        List<Produto> produtosComVenda = prodsComVendas(produtos);
        List<Produto> produtosSemVenda = buscarTodos();
        produtosSemVenda.removeAll(produtosComVenda);
        Collections.sort(produtosComVenda, new Comparator<Produto>() {
            @Override
            public int compare(Produto o1, Produto o2) {
                Date d1 = null;
                Date d2 = null;
                try {
                    d1 = formato.parse(o1.ultimaVenda(context).dataVenda);
                    d2 = formato.parse(o2.ultimaVenda(context).dataVenda);
                } catch (ParseException e){
                    e.printStackTrace();
                }
                return d2.compareTo(d1);
            }
        });
        produtosComVenda.addAll(produtosSemVenda);
        return produtosComVenda;
    }

    public List<Produto> ordenarProdutosPorUnidadesVendidasDown(List<Produto> produtos) {
        Collections.sort(produtos, new Comparator<Produto>() {
            @Override
            public int compare(Produto o1, Produto o2) {
                return ((int) (o1.unidadesVendidas(context) - o2.unidadesVendidas(context)));
            }
        });

        return produtos;
    }

    public List<Produto> ordenarProdutosPorUnidadesVendidasUp(List<Produto> produtos) {
        Collections.sort(produtos, new Comparator<Produto>() {
            @Override
            public int compare(Produto o1, Produto o2) {
                return ((int) (o2.unidadesVendidas(context) - o1.unidadesVendidas(context)));
            }
        });

        return produtos;
    }

    public List<String> listaDeCategorias(List<Produto> prods){
        List<String> cats = new ArrayList<>();
        for(Produto p:prods){
            p.categoria = p.categoria.trim();
            if("".contains(p.categoria)){
                p.categoria = "Sem Categoria";
            }
            if (!cats.contains(p.categoria)) {
                cats.add(p.categoria);
            }
        }
        Collections.sort(cats, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toUpperCase().compareTo(o2.toUpperCase());
            }
        });
        return cats;
    }

    public void inserirProdutosIniciais(){
        String listProd = textoProdutosIniciais();
        listProd = listProd.replaceAll("\n \n","\n");
        String[] listCat = listProd.split("CATEGORIA:");
        for (String cat:listCat) {
            if (!"".contains(cat)) {
                String categ = cat.split("\n")[0];
//                cat = cat.replace(categ,"");
                String[] listProd2 = cat.split("\n");
                for(String prodString:listProd2){
                    if (!"".contains(prodString) && !prodString.contentEquals(categ)) {
                        Produto prod = new Produto();
                        String[] caract = prodString.split("-");
                        caract[0] = caract[0].replaceAll(" ","");
                        prod.codigo = Integer.parseInt(caract[0]);
                        prod.categoria = categ;
                        prod.nome = caract[1];
                        Log.d("ProdsIniciais","Nome: " + prod.nome);
                        caract[2] = caract[2].replaceAll(" ","");
                        caract[2] = caract[2].replaceAll(",",".");
                        prod.preço = Float.parseFloat(caract[2].substring(2));
                        inserir(prod);
                    }
                }
            }
        }

    }

    public List<Produto> addCategorias(List<Produto> prods){
        if (prods.size() != 0) {
            List<Produto> semCat = new ArrayList<>();
            List<Produto> comCat = new ArrayList<>();
            Collections.sort(prods, new Comparator<Produto>() {
                @Override
                public int compare(Produto o1, Produto o2) {
                    return o1.nome.compareTo(o2.nome);
                }
            });
            for(Produto p:prods){
                if (p.categoria.replaceAll(" ","").length() > 0) {
                    while(" ".contains(p.categoria.substring(0,1))){
                        p.categoria = p.categoria.substring(1);
                    }
                }
                if("".contains(p.categoria)){
                    p.categoria = "Sem Categoria";
                    semCat.add(p);
                }else{
                    comCat.add(p);
                }
            }
            Collections.sort(comCat, new Comparator<Produto>() {
                @Override
                public int compare(Produto o1, Produto o2) {
                    return o1.categoria.compareTo(o2.categoria);
                }
            });
            comCat.addAll(semCat);
            prods = comCat;
            Produto p = new Produto();
            p.categoria = prods.get(0).categoria;
            prods.add(0, p);
            for(int i = 1; i < prods.size(); i++){
                if(!(prods.get(i).categoria.contains(prods.get(i-1).categoria))){
                    Produto p2 = new Produto();
                    p2.categoria = prods.get(i).categoria;
                    prods.add(i, p2);
                }
            }
        }

        return prods;
    }


    public List<Produto> prodsDaCategoria(String categ, List<Produto> produtos){
        List<Produto> prods = new ArrayList<>();
        for(Produto p:produtos){
            p.categoria = p.categoria.trim();
            if(p.categoria.contentEquals(categ)){
                prods.add(p);
            }
        }
        return prods;
    }

    public void atualizarQuantEstoqueProds(List<Produto> prods, boolean somar){
        if(somar){
            for(Produto p:prods){
                p.quantNoEstoque = buscarprodCodigo(p.codigo).quantNoEstoque;
                p.quantNoEstoque += p.quantidade;
                alterar(p);
            }
        }else{
            for(Produto p:prods){
                p.quantNoEstoque = buscarprodCodigo(p.codigo).quantNoEstoque;
                if (p.quantNoEstoque > 0) {
                    p.quantNoEstoque -= p.quantidade;
                    alterar(p);
                }
            }
        }
    }

    public void recriarEstoque(){
        PedidosRepositorio pedRep = new PedidosRepositorio(context);
        VendasRepositorio vendRep = new VendasRepositorio(context);
        for(Produto p:buscarTodos()){
            if (p.quantNoEstoque != 0) {
                p.quantNoEstoque = 0;
                alterar(p);
            }
        }
        List<Produto> prods = buscarTodos();
        for(Pedido pedido:pedRep.buscarTodos()){
            for(Produto prod:pedido.produtos) {
                prod.quantNoEstoque = buscarprodId(prod.id).quantNoEstoque;
                prod.quantNoEstoque += prod.quantidade;
                alterar(prod);
            }
        }
        for(Venda venda:vendRep.buscarTodasEfetivadas()){
            for(Produto prod:venda.produtos) {
                if(buscarprodId(prod.id) == null){
                    continue;
                }
                prod.quantNoEstoque = buscarprodId(prod.id).quantNoEstoque;
                prod.quantNoEstoque -= prod.quantidade;
                if(prod.quantNoEstoque < 0){
                    prod.quantNoEstoque = 0;
                }
                alterar(prod);
            }
        }
    }

    public void consertarIdTodosProdutos(){
        PedidosRepositorio pedRep = new PedidosRepositorio(context);
        VendasRepositorio vendRep = new VendasRepositorio(context);

        for(Venda venda:vendRep.buscarTodos()){
            for(Produto prod:venda.produtos){
                if(prod.id == 0){
                    prod.id = buscarIdProduto(prod.nome);
                }
            }
            vendRep.alterar(venda);
        }
        for(Pedido pedido:pedRep.buscarTodos()){
            for(Produto prod:pedido.produtos){
                if(prod.id == 0){
                    prod.id = buscarIdProduto(prod.nome);
                }
            }
            pedRep.alterar(pedido);
        }
    }

    public int buscarIdProduto(String nome){
        for(Produto prod:buscarTodos()){
            if(prod.nome.trim().contentEquals(nome.trim())){
                return prod.id;
            }
        }
        return 0;
    }

    public List<Produto> produtosEmEstoque(){
        List<Produto> estoque = new ArrayList<>();
        for(Produto p:buscarTodos()){
            if(p.quantNoEstoque > 0){
                estoque.add(p);
            }
        }
        return estoque;
    }

    public List<Produto> prodsIndisponiveisEmEstoque(List<Produto> prods){
        List<Produto> indisponiveis = new ArrayList<>();
        for(Produto p:prods){
            int quantEstoque = buscarprodId(p.id).quantNoEstoque;
            if(quantEstoque < p.quantidade){
                indisponiveis.add(p);
            }
        }
        return indisponiveis;
    }

    public String textoProdutosIniciais(){
        String listProd = "CATEGORIA: Base Timewise® 3D\n" +
                " \n" +
                "10142659 - Base Timewise® 3D  Ivory C110  -R$64,90 \n" +
                "\n" +
                "10142660 - Base Timewise® 3D  Ivory W130 - R$64,90 \n" +
                "\n" +
                "10142661 - Base Timewise® 3D  Ivory N140 -R$64,90 \n" +
                "\n" +
                "10142662 - Base Timewise® 3D  Ivory N160 -R$64,90 \n" +
                "\n" +
                "10142664 - Base Timewise® 3D  Beige W100 -R$64,90 \n" +
                "\n" +
                "10142665 - Base Timewise® 3D Beige C110 -R$64,90 \n" +
                "\n" +
                "10142667 - Base Timewise® 3D Beige C120 -R$64,90 \n" +
                "\n" +
                "10142668 - Base Timewise® 3D Beige C130 -R$64,90 \n" +
                "\n" +
                "10142670 - Base Timewise® 3D Beige C140 -R$64,90 \n" +
                "\n" +
                "10142672 - Base Timewise® 3D Beige W160 -R$64,90 \n" +
                "\n" +
                "10142673 - Base Timewise® 3D  Beige C170  -R$64,90 \n" +
                "\n" +
                "10142671 - Base Timewise® 3D Beige N150 -R$64,90 \n" +
                "\n" +
                "10142674 - Base Timewise® 3D Beige W180 -R$64,90 \n" +
                "\n" +
                "10142675 - Base Timewise® 3D Beige N190 -R$64,90 \n" +
                "\n" +
                "10142676 - Base Timewise® 3D Beige N200  -R$64,90 \n" +
                "\n" +
                "10142677 - Base Timewise® 3D Beige N210 -R$64,90 \n" +
                "\n" +
                "10142678 - Base Timewise® 3D Beige C220 -R$64,90 \n" +
                "\n" +
                "10142679 - Base Timewise® 3D  Bronze W100 -R$64,90 \n" +
                "\n" +
                "10142680 - Base Timewise® 3D  Bronze W110 -R$64,90 \n" +
                "\n" +
                "10142685 - Base Timewise® 3D Bronze C160 -R$64,90 \n" +
                "\n" +
                "10142681 - Base Timewise® 3D  Bronze W120 -R$64,90 \n" +
                "\n" +
                "10142684 - Base Timewise® 3D Bronze W150 -R$64,90 \n" +
                "\n" +
                "10142682 - Base Timewise® 3D  Bronze W130 -R$64,90 \n" +
                "\n" +
                "10142683 - Base Timewise® 3D  Bronze W140 -R$64,90 \n" +
                "\n" +
                "10142686 - Base Timewise® 3D Bronze C170 -R$64,90 \n" +
                "\n" +
                "10142663 - Base Timewise® 3D  Ivory W150 -R$64,90\n" +
                "\n" +
                "CATEGORIA: Linha Solar \n" +
                "\n" +
                " 10063274 - Protetor Solar FPS 50 -  R$69,90 \n" +
                "\n" +
                "10050165 - Gel Restaurador Pós Sol -  R$54,90\n" +
                "\n" +
                "CATEGORIA: Produtos de Cuidados com a Pele \n" +
                "\n" +
                " 10150448 - Sistema Timewise ®3D  Normal/SECA - R$299,00 \n" +
                "\n" +
                "10150447 - Sistema Timewise ®3D Mista/OLEOSA - R$299,00 \n" +
                "\n" +
                "10110218 - Creme para Área dos Olhos TimeWise® 3D - R$79,90 \n" +
                "\n" +
                "10110213 - Creme Noturno TimeWise® 3D  Normal/SECA - R$89,90 \n" +
                "\n" +
                "10110214 - Creme Noturno TimeWise® 3D Pele Mista a OLEOSA - R$89,90 \n" +
                "\n" +
                "10110212 - Creme Diurno FPS 30 TimeWise® 3D  Mista/OLEOSA - R$89,90 \n" +
                "\n" +
                "10110211 - Creme Diurno FPS 30 TimeWise® 3D Normal/SECA - R$89,90 \n" +
                "\n" +
                "10110210 - Gel de Limpeza 4 em 1 TimeWise® 3D Mista/OLEOSA - R$59,90 \n" +
                "\n" +
                "10110209 - Gel de Limpeza 4 em 1 TimeWise® 3D Normal/SECA - R$59,90\n" +
                "\n" +
                "CATEGORIA: Produtos Cuidados com a pele ROSTO\n" +
                "\n" +
                "10180470 - Vitamina C Activating Squares™ Timewise® - R$79,90  \n" +
                "\n" +
                "10085573 - Sérum Corretor de Tom Facial TimeWise® - R$119,00 \n" +
                " \n" +
                "10042760 - Solução Noturna TimeWise® - R$89,00 \n" +
                "\n" +
                "10039974 - Tônico Suavizante Hidratante TimeWise® - R$72,00\n" +
                "\n" +
                "CATEGORIA: Produtos de Cuidados com a Pele CORPO\n" +
                "\n" +
                "10084578 - Gel Creme Redutor de Celulite SmoothAction TimeWise® - R$129,00 \n" +
                "\n" +
                "10075664 - Massageador corporal SMOOTH Action TimeWise® Massageador corporal SmoothAction TimeWise® - R$36,00\n" +
                "\n" +
                "10067661 - Loção Corporal Firmadora TargetedAction® TimeWise® - R$124,00\n" +
                "\n" +
                "CATEGORIA: Produtos de Cuidados com a Pele - TW Repair\n" +
                "\n" +
                "10108837 - Sérum Lifiting Avançado VoluFirm TimeWise Repair® - R$169,00 \n" +
                "\n" +
                "10120566 - Creme Diurno com FPS 30 VoluFirm TimeWise Repair® - R$159,00 \n" +
                "\n" +
                "10088897 - Peeling Facial Renovador em Gel TimeWise Repair™ - R$259,00 \n" +
                "\n" +
                "10057096 - Creme Noturno VoluFirm™ TimeWise Repair™ - R$159,00 \n" +
                "\n" +
                "10072912 - Kit Creme Diurno e Noturno VoluFirm TimeWise Repair® - R$302,00 \n" +
                "\n" +
                "10071883 - Preenchedor de Rugas VoluFill™ TimeWise Repair™ - R$119,00 \n" +
                "\n" +
                "10073276 - Sistema VoluFirm™ TimeWise Repair™ - R$665,00 \n" +
                "\n" +
                "10047376 - Creme para a Área dos Olhos VoluFirm™ TimeWise Repair™ - R$119,00\n" +
                "\n" +
                "10053074 - Espuma de Limpeza VoluFirm™ TimeWise Repair™ - R$75,00\n" +
                "\n" +
                "CATEGORIA: Produtos de Cuidados com a Pele  Botanical Effects\n" +
                "\n" +
                "10132974 - Gel Hidratante Botanical Effects® - R$45,90 \n" +
                "\n" +
                "10135751 - Gel de Limpeza Botanical Effects® - R$39,90 \n" +
                "\n" +
                "10084855 - Esfoliante Revigorante Botanical Effects® - R$39,90 \n" +
                "\n" +
                "10084849 - Hidratante FPS 30 Botanical Effects® - R$45,90 \n" +
                "\n" +
                "10084853 - Tônico Refrescante Botanical Effects® - R$59,90\n" +
                "\n" +
                "CATEGORIA: Produtos de Cuidados com a Pele Clear Proof\n" +
                "\n" +
                "10094148 - Máscara Detox Clear Proof - R$79,90\n" +
                "\n" +
                " 10063501 - Loção Purificante para Pele com Tendência à Acne Clear Proof™ - R$109,90\n" +
                "\n" +
                "10092466 - Kit Antiacne Clear Proof™ - R$239,90 \n" +
                "\n" +
                "10063498 - Tônico para Pele com Tendência à Acne Clear Proof™ - R$64,90 \n" +
                "\n" +
                "10088953 - Creme Secativo para Pele com Tendência à Acne Clear Proof™ - R$69,90 \n" +
                "\n" +
                "10063503 - Gel de Limpeza para Pele com Tendência à Acne Clear Proof™ - R$64,90 \n" +
                "\n" +
                "10056749 - Hidratante sem Óleo para Pele com Tendência à Acne Clear Proof™ - R$64,90\n" +
                "\n" +
                "CATEGORIA: Produtos de Cuidados com a Pele  MK Men\n" +
                "\n" +
                "10108360 - Sistema de Cuidados MKMen® - R$159,00 \n" +
                "\n" +
                "10086904 - Gel de Limpeza Facial MKMen® - R$49,00 \n" +
                "\n" +
                "10060198 - Creme Intensivo para a Área dos Olhos MKMen®  - R$60,00 \n" +
                "\n" +
                "10010336 - Espuma para Barbear MKMen® - R$58,00  \n" +
                "\n" +
                "10073605 - Loção Hidratante Intensiva FPS30 MKMen®  - R$69,00 \n" +
                "\n" +
                "10010338 - Gel Refrescante Pós Barba MKMen® - R$47,00\n" +
                "\n" +
                "CATEGORIA:Produtos de Cuidados com a Pele Linha Solar\n" +
                "\n" +
                "10055399 - Protetor Solar FPS 30 Mary Kay® Protetor Solar FPS 30 - R$62,90 \n" +
                "\n" +
                "10074111 - Protetor Labial com FPS 15 Mary Kay® Protetor Labial com FPS 15 - R$34,00\n" +
                "\n" +
                "CATEGORIA:Produtos de Cuidados com a Pele  Cuidados Personalizados\n" +
                "\n" +
                "10125872 - Loção Energizante para os Pés e Pernas Mint BLISS - R$54,00 \n" +
                "\n" +
                "10031573 - Loção Antibrilho e OLEOSIDADE - R$65,00 \n" +
                "\n" +
                "10045655 - Hidratante Noturno Extra EMOLIENTE - R$58,00 \n" +
                "\n" +
                "10041758 - Lenços de Papel Antibrilho da Pele Mary Kay® - R$29,00\n" +
                "\n" +
                "CATEGORIA:Base em Pó Mineral\n" +
                "\n" +
                "10077357 - Base em Pó Mineral Mary Kay® Ivory 1 - R$69,00 \n" +
                "\n" +
                "10077361 - Base em Pó Mineral Mary Kay® Bronze 1 - R$69,00 \n" +
                "\n" +
                "10077360 - Base em Pó Mineral Mary Kay® Beige 2 - R$69,00 \n" +
                "\n" +
                "10077358 - Base em Pó Mineral Mary Kay® Ivory 2 - R$69,00 \n" +
                "\n" +
                "10077359 - Base em Pó Mineral Mary Kay® Beige 1 - R$69,00 \n" +
                "\n" +
                "10077362 - Base em Pó Mineral Mary Kay® Bronze 2 - R$69,00\n" +
                "\n" +
                "CATEGORIA:CC Cream\n" +
                "\n" +
                "10087310 - CC Cream Hidratante Tonalizante Multibenefício FPS 15  Light to MÉDIUM - R$64,00 \n" +
                "\n" +
                "10087309 - CC Cream Hidratante Tonalizante Multibenefício FPS 15  Very LIGHT - R$64,00 \n" +
                "\n" +
                "10087311 - CC Cream Hidratante Tonalizante Multibenefício FPS 15 Medium to DEEP - R$64,00 \n" +
                "\n" +
                "10087312 - CC Cream Hidratante Tonalizante Multibenefício FPS 15 DEEP - R$64,00\n" +
                "\n" +
                "CATEGORIA:Lápis Para os Olhos At Play™\n" +
                "\n" +
                "10158406 - Lápis Para os Olhos At Play™ Matte BLACK - R$21,90 \n" +
                "\n" +
                "10158409 - Lápis Para os Olhos At Play™ Matte BEIGE - R$21,90\n" +
                "\n" +
                "CATEGORIA:Corretivo Perfecting Concealer Mary Kay®\n" +
                "\n" +
                "10092185 - Corretivo Perfecting Concealer Mary Kay PÊSSEGO - R$59,90 \n" +
                "\n" +
                "10092196 - Corretivo Perfecting Concealer Mary Kay  Deep BRONZE - R$59,90 \n" +
                "\n" +
                "10092195 - Corretivo Perfecting Concealer Mary Kay  Light BRONZE - R$59,90 \n" +
                "\n" +
                "10092194 - Corretivo Perfecting Concealer Mary Kay Deep Beige - R$59,90 \n" +
                "\n" +
                "10092193 - Corretivo Perfecting Concealer Mary Kay Light Beige - R$59,90 \n" +
                "\n" +
                "10092192 - Corretivo Perfecting Concealer Mary Kay Deep Ivory - R$59,90 \n" +
                "\n" +
                "10092191 - Corretivo Perfecting Concealer Mary Kay Light Ivory - R$59,90\n" +
                "\n" +
                "CATEGORIA:Corretivo\n" +
                "\n" +
                "10139826 - Corretivo YELLOW - R$41,00\n" +
                "\n" +
                "CATEGORIA:Batom Cremoso Edição Especial\n" +
                "\n" +
                "10022953 - Batom Cremoso Mary Kay®  Edição Especial TOFFEE - R$29,90 \n" +
                "\n" +
                "10022954 - Batom Cremoso Mary Kay® Edição Especial SHELL - R$29,90 \n" +
                "\n" +
                "10022965 - Batom Cremoso Mary Kay® Edição Especial  RED - R$29,90 \n" +
                "\n" +
                "10022961 - Batom Cremoso Mary Kay®  Edição Especial  HIBISCUS - R$29,90 \n" +
                "\n" +
                "10040717 - Batom Cremoso Mary Kay®  Edição Especial  FUCHSIA - R$29,90\n" +
                "\n" +
                "CATEGORIA:Batom Gel Semi Matte Mary Kay®\n" +
                "\n" +
                "10089673 - Batom Gel Semi Matte Mary Kay® Poppy PLEASE - R$39,90 \n" +
                "\n" +
                "10089671 - Batom Gel Semi Matte Mary Kay®  Powerful PINK - R$39,90\n" +
                "\n" +
                "CATEGORIA:Batom Matte Mary Kay®\n" +
                "\n" +
                "10129104 - Batom Matte Mary Kay®  Favorito NUDE - R$49,90 \n" +
                "\n" +
                "10129103 - Batom Matte Mary Kay® Nude PASSIONE - R$49,90 \n" +
                "\n" +
                "10102680 - Batom Matte Mary Kay® Puro MIRTILLO - R$49,90 \n" +
                "\n" +
                "10102679 - Batom Matte Mary Kay® Red AMORE - R$49,90 \n" +
                "\n" +
                "10102678 - Batom Matte Mary Kay® Roma RED - R$49,90 \n" +
                "\n" +
                "10102674 - Batom Matte Mary Kay® Dolce PINK - R$49,90 \n" +
                "\n" +
                "10102671 - Batom Matte Mary Kay® Fiore ROSE - R$49,90 \n" +
                "\n" +
                "10102670 - Batom Matte Mary Kay® Bellissimo NUDE - R$49,90\n" +
                "\n" +
                "CATEGORIA:Brilho para os Lábios NouriShine Plus®\n" +
                "\n" +
                "10057773 - Brilho para os Lábios NouriShine Plus® Pink DIAMOND - R$39,90 \n" +
                "\n" +
                "10052397 - Brilho para os Lábios NouriShine Plus® Au NATUREL - R$39,90\n" +
                "\n" +
                "CATEGORIA:Lápis Retrátil para os Lábios\n" +
                "\n" +
                "10098545 - Lápis Retrátil para os Lábios Mary Kay® ROSE - R$37,90 \n" +
                "\n" +
                "10098544 - Lápis Retrátil para os Lábios Mary Kay® Deep NUDE - R$37,90 \n" +
                "\n" +
                "10098543 - Lápis Retrátil para os Lábios Mary Kay® Medium NUDE - R$37,90\n" +
                "\n" +
                "CATEGORIA:Blush Mary Kay Chromafusion®\n" +
                "\n" +
                "10120411 - Blush Mary Kay Chromafusion® Hint of PINK - R$39,90 \n" +
                "\n" +
                "10120420 - Blush Mary Kay Chromafusion®  WINEBERRY - R$39,90 \n" +
                "\n" +
                "10120419 - Blush Mary Kay Chromafusion® Golden COOPPER - R$39,90 \n" +
                "\n" +
                "10120418 - Blush Mary Kay Chromafusion® Desert ROSE - R$39,90 \n" +
                "\n" +
                "10120414 - Blush Mary Kay Chromafusion® Shy BLUSH - R$39,90 \n" +
                "\n" +
                "10120413 - Blush Mary Kay Chromafusion® Darling PINK - R$39,90 \n" +
                "\n" +
                "10120415 - Blush Mary Kay Chromafusion®  Rogue ROSE - R$39,90 \n" +
                "\n" +
                "10120412 - Blush Mary Kay Chromafusion® Rosy NUDE - R$39,90 \n" +
                "\n" +
                "10120416 - Blush Mary Kay Chromafusion® Juicy Peach - R$39,90 \n" +
                "\n" +
                "10120417 - Blush Mary Kay Chromafusion® Hot Coral - R$39,90\n" +
                "\n" +
                "CATEGORIA:Contorno e Iluminador Mary Kay Chromafusion®\n" +
                "\n" +
                "10129745 - Pó Compacto para Contorno Facial Mary Kay Chromafusion®  LATTE - R$39,90 \n" +
                "\n" +
                "10129761 - Pó Compacto para Contorno Facial Mary Kay Chromafusion® COCOA - R$39,90 \n" +
                "\n" +
                "10129763 - Pó Iluminador Compacto Mary Kay Chromafusion® Honey GLOW - R$39,90 \n" +
                "\n" +
                "10129762 - Pó Iluminador Compacto Mary Kay Chromafusion® GLAZED - R$39,90\n" +
                "\n" +
                "CATEGORIA:Blush Mineral\n" +
                "\n" +
                "10077343 - Blush Mineral Mary Kay acabamento Matte Sunny SPICE - R$39,90\n" +
                "\n" +
                "CATEGORIA:Máscara para os Cílios\n" +
                "\n" +
                "10131236 - Máscara para Cílios Lash Intensity PRETO - R$69,90 \n" +
                "\n" +
                "10084930 - Máscara Alongadora para Cílios Lash Love Mary Kay®  PRETO - R$59,90 \n" +
                "\n" +
                "10084931 - Máscara Alongadora para Cílios Lash Love à Prova D'água Mary Kay®  PRETO - R$59,90 \n" +
                "\n" +
                "10049293 - Máscara para Cílios Lash Love Preto PRETO - R$59,90 \n" +
                "\n" +
                "10035651 - Ultimate Máscara para Cílios Mary Kay® PRETO - R$59,90\n" +
                "\n" +
                "CATEGORIA:Delineadores\n" +
                "\n" +
                "10141205 - Delineador Líquido Em Caneta à Prova D'ÁGUA - R$59,90 \n" +
                " \n" +
                "10084725 - Delineador em Gel para os Olhos  BLACK - R$64,00\n" +
                "\n" +
                "CATEGORIA:Cuidados Personalizados\n" +
                "\n" +
                "10060794 - Spray Fixador de Maquiagem Mary Kay® - R$120,00 \n" +
                "\n" +
                "10074680 - Eye Primer Fixador de Sombras Mary Kay® - R$45,90 \n" +
                "\n" +
                "10058473 - Primer Facial Fixador de Maquiagem FPS 15 - R$72,00 \n" +
                "\n" +
                "10042768 - Demaquilante para a Área dos Olhos Mary Kay® - R$59,90\n" +
                "\n" +
                "10169107 - Kit Microdermoabrasão - R$198,00\n" +
                "\n" +
                "CATEGORIA:Lápis Retrátil para os Olhos\n" +
                "\n" +
                "10084060 - Lápis Retrátil para os Olhos  BLACK - R$39,90 \n" +
                "\n" +
                "10084061 - Lápis Retrátil para os Olhos  Deep BROWN - R$39,90\n" +
                "\n" +
                "CATEGORIA:Pó Bronzeador & Pó Iluminador\n" +
                "\n" +
                "10077353 - Pó Bronzeador Compacto Mary Kay® MEDIUMDAR - R$52,90 \n" +
                "\n" +
                "10077351 - Pó Bronzeador Compacto Mary Kay® LightMedium - R$52,90\n" +
                "\n" +
                "CATEGORIA:Pó Mineral Compacto\n" +
                "\n" +
                "10077346 - Pó Mineral Compacto Mary Kay® Ivory 2 - R$49,00 \n" +
                "\n" +
                "10077349 - Pó Mineral Compacto Mary Kay® Bronze 1 - R$49,00 \n" +
                "\n" +
                "10077350 - Pó Mineral Compacto Mary Kay® Bronze 2 - R$49,00 \n" +
                "\n" +
                "10077348 - Pó Mineral Compacto Mary Kay® Beige 2 - R$49,00 \n" +
                "\n" +
                "10077347 - Pó Mineral Compacto Mary Kay® Beige 1 - R$49,00\n" +
                "\n" +
                "CATEGORIA:Pó Solto Translúcido\n" +
                "\n" +
                "100601821 - Pó Solto Translúcido Mary Kay® - R$75,00\n" +
                "\n" +
                "CATEGORIA:Sobrancelhas\n" +
                "\n" +
                "10125032 - Máscara em Gel para Sobrancelhas  BLONDE - R$49,90 \n" +
                "\n" +
                "10125035 - Máscara em Gel para Sobrancelhas Dark BRUNETTE - R$49,90 \n" +
                "\n" +
                "10125033 - Máscara em Gel para Sobrancelhas Dark BLONDE - R$49,90 \n" +
                "\n" +
                "10127612 - Delineador Retrátil de Precisão para Sobrancelhas Dark BLONDE - R$49,90\n" +
                "\n" +
                "10127613 - Delineador Retrátil de Precisão para Sobrancelhas  BRUNETTE - R$49,90 \n" +
                "\n" +
                "10127614 - Delineador Retrátil de Precisão para Sobrancelhas Dark BRUNETTE - R$49,90 \n" +
                "\n" +
                "10034731 - Lápis Delineador para Sobrancelhas Mary Kay® BRUNETTE - R$39,00 \n" +
                "\n" +
                "10034730 - Lápis Delineador para Sobrancelhas Mary Kay®  Classic BLONDE - R$39,00\n" +
                "\n" +
                "CATEGORIA:Sombra Mary Kay Chromafusion®\n" +
                "\n" +
                "10127878 - Sombra Mary Kay Chromafusion®  BISCOTTI - R$25,90 \n" +
                "\n" +
                "10127877 - Sombra Mary Kay Chromafusion®  Cintilante  CRYSTALLINE - R$25,90 \n" +
                "\n" +
                "10127892 - Sombra Mary Kay Chromafusion® MERLOT - R$25,90 \n" +
                "\n" +
                "10127891 - Sombra Mary Kay Chromafusion® Cintilante Sweet PLUM - R$25,90 \n" +
                "\n" +
                "10127887 - Sombra Mary Kay Chromafusion®  Cintilante  Rose GOLD - R$25,90 \n" +
                "\n" +
                "10127880 - Sombra Mary Kay Chromafusion®  CINNABAR - R$25,90 \n" +
                "\n" +
                "10127888 - Sombra Mary Kay Chromafusion®  Cintilante Shiny PENNY - R$25,90 \n" +
                "\n" +
                "10127889 - Sombra Mary Kay Chromafusion®  BLOSSOM - R$25,90 \n" +
                "\n" +
                "10127893 - Sombra Mary Kay Chromafusion®  Cintilante  MOSS - R$25,90 \n" +
                "\n" +
                "10127881 - Sombra Mary Kay Chromafusion®  MAHOGANY - R$25,90  \n" +
                "\n" +
                "10127879 - Sombra Mary Kay Chromafusion®  HAZELNUT - R$25,90 \n" +
                "\n" +
                "10127883 - Sombra Mary Kay Chromafusion®  ESPRESSO - R$25,90 \n" +
                "\n" +
                "10127882 - Sombra Mary Kay Chromafusion® Cintilante  Rustic - R$25,90 \n" +
                "\n" +
                "10127885 - Sombra Mary Kay Chromafusion® Cintilante Granite - R$25,90 \n" +
                "\n" +
                "10127884 - Sombra Mary Kay Chromafusion®  Cashmere Haze - R$25,90 \n" +
                "\n" +
                "10127890 - Sombra Mary Kay Chromafusion®  Cintilante Golden Mauve - R$25,90 \n" +
                "\n" +
                "10127886 - Sombra Mary Kay Chromafusion®  Onyx - R$25,90\n" +
                "\n" +
                "CATEGORIA:Paleta Mary Kay Chromafusion®\n" +
                "\n" +
                "10141553 - Paleta Mary Kay Chromafusion®  Cool SMOKE - R$119,90 \n" +
                "\n" +
                "10141552 - Paleta Mary Kay Chromafusion®  Sweet PLUM - R$119,90  \n" +
                "\n" +
                "10141551 - Paleta Mary Kay Chromafusion®  Cheer NEUTRALS - R$119,90 \n" +
                "\n" +
                "10141550 - Paleta Mary Kay Chromafusion® Working OVERTIME - R$119,90 \n" +
                "\n" +
                "10141549 - Paleta Mary Kay Chromafusion®  Smoky GREYS - R$119,90\n" +
                "\n" +
                "CATEGORIA:Acessórios/Esponjas\n" +
                "\n" +
                "10114898 - Esponja para Maquiagem Mary Kay® - R$39,90\n" +
                "\n" +
                "CATEGORIA:Estojos\n" +
                "\n" +
                "10131348 - Perfect PALETTE - R$79,90 \n" +
                "\n" +
                "10123805 - Petite Palette™ Mary KAY - R$34,90\n" +
                "\n" +
                "CATEGORIA:Novos Pinceis Mary Kay®\n" +
                "\n" +
                "10128063 - Pincel Oval para Base Líquida Mary Kay® - R$59,90 \n" +
                "\n" +
                "10152928 - Pincel para Base em Pó Mineral Mary Kay® - R$59,90 \n" +
                "\n" +
                "10145686 - Pincel para Côncavo dos Olhos Mary Kay® - R$34,90 \n" +
                "\n" +
                "10122321 - Pincel para Blush Mary Kay® - R$59,90 \n" +
                "\n" +
                "10122322 - Pincel para Côncavo dos Olhos Mary Kay® - R$34,90 \n" +
                "\n" +
                "10122320 - Pincel para Pó Facial Mary Kay® - R$59,90 \n" +
                "\n" +
                "10122325 - Pincel para Base LÍQUIDA - R$59,90 \n" +
                "\n" +
                "10122329 - Pincel para Corretivo e Sombra em CREME - R$34,90 \n" +
                "\n" +
                "10107305 - Kit de PINCÉIS - R$219,90 \n" +
                "\n" +
                "10122328 - Pincel Duo para SOBRANCELHAS - R$34,90 \n" +
                "\n" +
                "10122327 - Pincel para Base em Pó MINERAL - R$59,90 \n" +
                "\n" +
                "10122324 - Pincel para ESFUMAR - R$34,90 \n" +
                "\n" +
                "10122323 - Pincel para Sombra Mary KAY - R$34,90\n" +
                "\n" +
                "CATEGORIA:Novos Pinceis Compactos\n" +
                "\n" +
                "10152934 - Aplicadores para Sombra (2 unidades) - R$15,90 \n" +
                "\n" +
                "10152933 - Pincel Compacto para Blush - R$15,90 \n" +
                "\n" +
                "10152932 - Pincel Compacto para Pó - R$15,90 \n" +
                "\n" +
                "10134453 - Esponja Compacta para Pó - R$15,90\n" +
                "\n" +
                "CATEGORIA:Base Líquida Matte Mary Kay At Play®\n" +
                "\n" +
                "10105372 - Base Líquida Matte Mary Kay At Play® DEEP - R$49,90 \n" +
                "\n" +
                "10105373 - Base Líquida Matte Mary Kay At Play® Deep TAN - R$49,90 \n" +
                "\n" +
                "10105371 - Base Líquida Matte Mary Kay At Play® Medium to DEEP - R$49,90 \n" +
                "\n" +
                "10105368 - Base Líquida Matte Mary Kay At Play®  MEDIUM - R$49,90 \n" +
                "\n" +
                "10105367 - Base Líquida Matte Mary Kay At Play® Light to MÉDIUM - R$49,90 \n" +
                "\n" +
                "10105365 - Base Líquida Matte Mary Kay At Play® Very LIGHT - R$49,90\n" +
                "\n" +
                "CATEGORIA:Bastões At Play\n" +
                "\n" +
                "10120686 - Bastão para Iluminar At Play Rose Gold GLOW - R$49,90 \n" +
                "\n" +
                "10157202 - Kit Contorno e Iluminação Bastões At Play Dark Brown - R$99,80 \n" +
                "\n" +
                "10121196 - Bastão Corretor de Tom At Play Lavender (Ditch Dullness) - R$49,90 \n" +
                "\n" +
                "10120984 - Bastão para Contorno At Play Dark Brown (Get Sculpted 2) - R$49,90\n" +
                "\n" +
                "\n" +
                "\n" +
                "CATEGORIA:Batom Líquido Matte At Play®\n" +
                "\n" +
                "10115331 - Batom Líquido Matte At Play™ Miss Melon - R$37,00 \n" +
                "\n" +
                "10115334 - Batom Líquido Matte At Play™ Mad for Magenta - R$37,00 \n" +
                "\n" +
                "10120170 - Batom Líquido Matte At Play™ Spiced Truffle - R$37,00 \n" +
                "\n" +
                "10115332 - Batom Líquido Matte At Play™ Spicy Red - R$37,00 \n" +
                "\n" +
                "10105227 - Batom Líquido Matte At Play™ Pink Me UP - R$37,00 \n" +
                "\n" +
                "10105226 - Batom Líquido Matte At Play™ Feeling Shy - R$37,00 \n" +
                "\n" +
                "10091997 - Batom Líquido Matte At Play™ Taupe That - R$37,00 \n" +
                "\n" +
                "10091998 - Batom Líquido Matte At Play™ Pink It Over - R$37,00 \n" +
                "\n" +
                "10092003 - Batom Líquido Matte At Play™ Berry Strong - R$37,00\n" +
                "\n" +
                "\n" +
                "CATEGORIA:Batom Líquido Matte Metálico Mary Kay At Play®\n" +
                "\n" +
                "10115330 - Batom Líquido Matte Metálico Mary Kay At Play Strawberry Steel - R$37,00 \n" +
                "\n" +
                "10115329 - Batom Líquido Matte Metálico Mary Kay At Play Champagne Metal - R$37,00 \n" +
                "\n" +
                "10115325 - Batom Líquido Matte Metálico Mary Kay At Play Hot Pink Platinum - R$37,00 \n" +
                "\n" +
                "10115327 - Batom Líquido Matte Metálico Mary Kay At Play Sunkissed Copper - R$37,00 \n" +
                "\n" +
                "10115326 - Batom Líquido Matte Metálico Mary Kay At Play Petal to the MetalR - $37,00\n" +
                "\n" +
                "\n" +
                "CATEGORIA:Trio de Sombras Mary Kay At Play®\n" +
                "\n" +
                "10105066 - Trio de Sombras At Play™ Glowing Rose - R$42,00 \n" +
                "\n" +
                "10092006 - Trio de Sombras At Play™ Morning TOFFEE - R$42,00 \n" +
                "\n" +
                "10092008 - Trio de Sombras At Play™ Tuxedo Twist - R$42,00\n" +
                "\n" +
                "\n" +
                "CATEGORIA:Fragrâncias Femininas Deo Colonia\n" +
                "\n" +
                "10132480 - If You Believe™ Deo Colônia, 60 ml - R$109,00 \n" +
                "\n" +
                "10142267 - At Play™ Deo Colônia, 50 ml - R$59,90\n" +
                "\n" +
                "10129410 - True Passion Noir™ Deo Colônia, 60 ml - R$95,00 \n" +
                "\n" +
                "10129407 - True Passion™ Deo Colônia, 60 ml - R$95,00 \n" +
                "\n" +
                "10130484 - True Passion L'Eau™ Deo Colônia, 60 ml - R$95,00 \n" +
                "\n" +
                "10105859 - Clever Dare™ Deo Colônia, 30 ml - R$99,00 \n" +
                "\n" +
                "10102313 - Black Diamonds™ Deo Colônia, 60 ml - R$109,00 \n" +
                "\n" +
                "10102863 - Hello Brilliant™ Deo Colônia, 50 ml - R$69,00 \n" +
                "\n" +
                "10085102 - Upbeat™ for Her Deo Colônia, 60 ml - R$89,00 \n" +
                "\n" +
                "10094692 - Wish™ Deo Colônia, 60 ml - R$109,00 \n" +
                "\n" +
                "10092683 - Clever™ Deo Colônia, 30 ml - R$99,00\n" +
                "\n" +
                "\n" +
                "CATEGORIA:Fragrâncias Femininas Deo Parfum / Eau de Parfum\n" +
                "\n" +
                "10155375 - Modern Charm Glam™ Deo Parfum, 50 ml - R$129,00 \n" +
                "\n" +
                "10114367 - Modern Charm™ Deo Parfum, 50 ml - R$129,00 \n" +
                "\n" +
                "10078597 - Cityscape™ for Her Eau de Parfum, 50 ml - R$279,00 \n" +
                "\n" +
                "10102311 - Pink Diamonds Intense™ Deo Parfum, 60 ml - R$129,00 \n" +
                "\n" +
                "10115875 - Cityscape™ for Her Eau de Parfum, 50 ml - R$279,00 \n" +
                "\n" +
                "10088536 - Dream Fearlessly Deo Parfum, 50 ml - R$169,00 \n" +
                "\n" +
                "10088537 - Love Fearlessly Deo Parfum, 50 ml - R$169,00 \n" +
                "\n" +
                "10088534 - Live Fearlessly Deo Parfum, 50 ml - R$169,00\n" +
                "\n" +
                "\n" +
                "CATEGORIA:Fragrâncias Masculinas Deo Colonia\n" +
                "\n" +
                "10104974 - Upscale Black® Deo Colônia, 75 ml - R$119,00 \n" +
                "\n" +
                "10092086 - Authentic Hero™ Deo Colônia, 100 ml - R$115,00 \n" +
                "\n" +
                "10060674 - Free Spirit Sport™ Deo Colônia, 100 ml - R$95,00 \n" +
                "\n" +
                "10072774 - Free Spirit™ Deo Colônia, 100 ml - R$95,00 \n" +
                "\n" +
                "10085103 - Upbeat™ for Him Deo Colônia, 75 ml - R$95,00\n" +
                "\n" +
                "\n" +
                "CATEGORIA:Fragrâncias Masculinas Deo Parfum / Eau de Parfum\n" +
                "\n" +
                "10104973 - Upscale Gentleman™ Deo Parfum, 75 ml - R$139,00 \n" +
                "\n" +
                "10126066 - Magnetic Passion™ Edge Deo Parfum, 75 ml - R$159,00  \n" +
                "\n" +
                "10078697 - Cityscape™ for Him Cologne Spray, 59 ml - R$279,00 \n" +
                "\n" +
                "10115876 - Cityscape™ for Him Cologne Spray, 59 ml - R$279,00 \n" +
                "\n" +
                "10088044 - Magnetic Passion™ Deo Parfum, 75 ml - R$159,00";

        return listProd;
    }
}

package com.example.controlemk;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.Adapters.PedidosProdAdapter;
import com.example.controlemk.Adapters.VendasProdAdapter;
import com.example.controlemk.Dominio.Entidades.Pedido;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.PedidosRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;

import java.util.List;

public class DetalhesProduto extends AppCompatActivity {

    public ProdutosRepositorio prodRep;
    public VendasRepositorio vendRep;
    public PedidosRepositorio pedRep;

    public SQLiteDatabase conexao;

    public TextView txtCodigo, txtNome, txtCategoria, txtPreco, txtDetalhes, txtQuantVendidos, txtQuantEstoque;

    public Produto produto;

    public RecyclerView lstVendasProd,lstPedidosProd;

    public int id;

    private AlertDialog.Builder dlgDel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_produto);
        getSupportActionBar().setTitle("Detalhes Produto");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        {
            txtCodigo = findViewById(R.id.txtCodigo);
            txtNome = findViewById(R.id.txtNome);
            txtCategoria = findViewById(R.id.txtCategoria);
            txtPreco = findViewById(R.id.txtPreco);
            txtDetalhes = findViewById(R.id.txtDetalhes);
            lstVendasProd = findViewById(R.id.lstHistVendasProd);
            lstPedidosProd = findViewById(R.id.lstHistPedidosProd);
            txtQuantVendidos = findViewById(R.id.txtProdsVendidos);
            txtQuantEstoque = findViewById(R.id.txtQuantEstoque);
        } // Declaração de Variáveis

        criarConexao();
        verificaParametro();
    }

    private void criarConexao() {

        try {
            vendRep = new VendasRepositorio(this);
            prodRep = new ProdutosRepositorio(this);
            pedRep = new PedidosRepositorio(this);

        } catch (SQLiteException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }

    private void verificaParametro(){

        Bundle bundle = getIntent().getExtras();

        if((bundle != null) && (bundle.containsKey("PRODUTO"))){
            produto = (Produto)bundle.getSerializable("PRODUTO");
            definirParametrosProd();
        }
    }

    public void definirParametrosProd(){
        id = produto.id;
        txtCodigo.setText(String.format("%08d",produto.codigo));
        txtNome.setText(produto.nome);
        txtCategoria.setText(produto.categoria);
        txtPreco.setText(String.format("R$%.2f",produto.preço));
        txtDetalhes.setText(produto.detalhes);
        txtQuantVendidos.setText(String.format("%02d",produto.unidadesVendidas(this)));
        txtQuantEstoque.setText(String.format("%02d", prodRep.buscarprodCodigo(produto.codigo).quantNoEstoque));

        criarLista();
    }

    public void criarLista() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lstVendasProd.setLayoutManager(linearLayoutManager);
        List<Venda> dados = vendRep.ordenarVendasPorDataUp(vendRep.vendasComProd(vendRep.buscarTodasEfetivadas(),produto.id));
        VendasProdAdapter adapterVendas = new VendasProdAdapter(dados, produto.codigo,this);
        lstVendasProd.setAdapter(adapterVendas);


        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        lstPedidosProd.setLayoutManager(linearLayoutManager2);
        List<Pedido> dados2 = pedRep.pedidosComProd(pedRep.buscarTodos(),produto.id);
        PedidosProdAdapter adapterPedidos = new PedidosProdAdapter(produto.codigo, dados2,this);
        lstPedidosProd.setAdapter(adapterPedidos);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detalhes, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {

            case android.R.id.home:
                setResult(10);
                finish();
                break;

            case R.id.menuItem_edit:
                Intent it2 = new Intent(DetalhesProduto.this, NovoProduto.class);
                it2.putExtra("PRODUTO",produto);
                startActivityForResult(it2,2);
                break;
            case R.id.menuItem_del:
                dlgDel = new AlertDialog.Builder(this);
                dlgDel.setTitle("Deletar Produto?");
                dlgDel.setMessage("Tem certeza que deseja deletar o produto? " +
                        "\nO produto também será excluido das vendas e dos pedidos" +
                        "\n\nO produto está presente em: " + vendRep.vendasComProd(vendRep.buscarTodasEfetivadas(),produto.id).size() + " Vendas" +
                        " e " + pedRep.pedidosComProd(pedRep.buscarTodos(), produto.id).size() + " Pedidos");
                dlgDel.setNegativeButton("Não",null);
                dlgDel.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prodRep.excluir(produto.id);
                        Toast.makeText(getApplicationContext(),"Produto Excluido com sucesso",Toast.LENGTH_LONG).show();
                        setResult(3);
                        finish();
                    }
                });
                dlgDel.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        produto = prodRep.buscarprodCodigo(produto.codigo);
        definirParametrosProd();
    }
}

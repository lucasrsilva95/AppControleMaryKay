package com.example.controlemk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.controlemk.Adapters.CategProdVendaAdapter;
import com.example.controlemk.Dominio.Entidades.Cliente;
import com.example.controlemk.Dominio.Entidades.Pedido;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;

import java.util.ArrayList;
import java.util.List;

public class ListaProdsVenda extends AppCompatActivity {

    public VendasRepositorio vendRep;
    public ProdutosRepositorio prodRep;
    public ClientesRepositorio cliRep;

    private Venda venda;
    private Pedido pedido;
    private boolean eVenda;
    private Cliente cliente;

    private Bundle bundle;

    private CategProdVendaAdapter categProdVendaAdapter;
    public SQLiteDatabase conexao;

    public RecyclerView lstProdVenda;

    public TextView txtTotVenda;
    public EditText edtPesq;

    public SearchView searchView;

    private List<Produto> dados,selecionados;
    private List<String> categAbertas;

    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_prod_venda);
        getSupportActionBar().setTitle("Produtos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lstProdVenda = (RecyclerView) findViewById(R.id.lstProdVenda);
        txtTotVenda = (TextView) findViewById(R.id.txtValTotVenda);

        criarConexao();
        verificaParametro();
        categAbertas = prodRep.listaDeCategorias(selecionados);
        if (bundle.containsKey("VENDA")) {
            criarLista(prodRep.produtosEmEstoque());
        } else {
            criarLista(prodRep.buscarTodos());
        }
    }

    private void criarConexao() {

        try {
            vendRep = new VendasRepositorio(this);
            prodRep = new ProdutosRepositorio(this);
            cliRep = new ClientesRepositorio(this);

        } catch (SQLiteException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }

    private void verificaParametro(){

        bundle = getIntent().getExtras();

        if((bundle.containsKey("VENDA") || bundle.containsKey("ENCOMENDA"))){
            if (bundle.containsKey("VENDA")) {
                venda = (Venda)bundle.getSerializable("VENDA");
                eVenda = true;
            } else {
                venda = (Venda)bundle.getSerializable("ENCOMENDA");
                eVenda = false;
            }
            selecionados = venda.produtos;
            venda.total = venda.calcValTotVenda();
            txtTotVenda.setText(String.format("R$%.2f",venda.total));
        }else if(bundle.containsKey("PEDIDO")){
            pedido = (Pedido)bundle.getSerializable("PEDIDO");
            eVenda = false;
            selecionados = pedido.produtos;
            pedido.totalBruto = pedido.calcValTotPedido();
            txtTotVenda.setText(String.format("R$%.2f",pedido.totalBruto));
            getSupportActionBar().setTitle("Catálogo");
        }
    }

    public void criarLista(List<Produto> dados){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lstProdVenda.setLayoutManager(linearLayoutManager);
        dados = prodListAlterada(dados);
        categProdVendaAdapter = new CategProdVendaAdapter( dados,categAbertas,eVenda,selecionados,txtTotVenda,getApplicationContext());
        lstProdVenda.setAdapter(categProdVendaAdapter);

    }


    public void pesquisaText(String text) {
        text = text.toUpperCase().trim();
        List<Produto> dados = prodRep.produtosEmEstoque();
        List<Produto> novosDados = new ArrayList<>();
        for (int i = 0; i < dados.size(); i++) {
            Produto prod = dados.get(i);
            if (prod.nome.toUpperCase().contains(text) || prod.categoria.toUpperCase().contains(text) || Integer.toString(prod.codigo).contains(text)) {
                novosDados.add(dados.get(i));
            }
        }
        criarLista(novosDados);
    }

    public List<Produto> prodListAlterada(List<Produto> resp){
//        List<Produto> resp = prodRep.buscarTodos();
        for(Produto p:selecionados){
            for(Produto prod:resp){
                if (p.codigo == prod.codigo) {
                    resp.set(resp.indexOf(prod),p);
                    break;
                }
            }
        }
        return resp;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_prodlistvenda, menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                pesquisaText(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {

            case android.R.id.home:
                finish();
                break;
            case R.id.itemConfirm:
                if (venda != null) {
                    venda.produtos = selecionados;
                    venda.total = venda.calcValTotVenda();
                    Intent it2 = new Intent(ListaProdsVenda.this, NovaVenda.class);
                    if (venda.efetivada) {
                        it2.putExtra("VENDA",venda);
                    } else {
                        it2.putExtra("ENCOMENDA",venda);
                    }
                    setResult(101,it2);
                } else {
                    pedido.produtos = selecionados;
                    pedido.totalBruto = pedido.calcValTotPedido();
                    Intent it2 = new Intent(this, NovoPedido.class);
                    it2.putExtra("PEDIDO",pedido);
                    setResult(101,it2);
                }
                finish();
                break;
            case R.id.itemNovoProd:
                categAbertas = categProdVendaAdapter.categoriasAbertas();
                Intent it3 = new Intent(ListaProdsVenda.this, NovoProduto.class);
                startActivityForResult(it3, 15);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        criarLista(prodRep.buscarTodos());
    }
}

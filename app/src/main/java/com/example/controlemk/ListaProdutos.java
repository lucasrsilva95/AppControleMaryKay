package com.example.controlemk;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.Adapters.CategProdAdapter;
import com.example.controlemk.Adapters.ProdutoAdapter;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.PedidosRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import static com.example.controlemk.Adapters.HistVendasAdapter.ITEM_DELETAR;
import static com.example.controlemk.Adapters.HistVendasAdapter.ITEM_EDITAR;

public class ListaProdutos extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;
    private TextView txtNomeVendedor;
    private ImageButton botNomeVendedor;
    private String nomeVendedor;

    public VendasRepositorio vendRep;
    public ProdutosRepositorio prodRep;
    public PedidosRepositorio pedRep;

    public CategProdAdapter categAdapter;
    public ProdutoAdapter prodAdapter;
    public SQLiteDatabase conexao;

    public RecyclerView lstProd;

    public EditText edtPesq;
    public ConstraintLayout layoutPesq;

    private List<Produto> dados;

    private List<String> categAbertas;
    private String pesqText = "";

    private boolean comCateg = true;

    private static final int ORD_CATEG = 1, ORD_UN_VENDIDA = 2, ORD_ULT_VENDA_DOWN = 3, ORD_ULT_VENDA_UP = 4;
    private int ordem = ORD_CATEG;
    private MenuItem itemOrdDataVenda, itemOrdCateg, itemOrdUnVendida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_prod);
        NavigationView navView = (NavigationView) findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(this);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawer, R.string.open, R.string.close);
        mDrawer.addDrawerListener(mToggle);
        mToggle.syncState();
        txtNomeVendedor = (TextView) navView.getHeaderView(0).findViewById(R.id.txtNomeVendedor);
        botNomeVendedor = (ImageButton) navView.getHeaderView(0).findViewById(R.id.botNomeVendedor);
        botNomeVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editarNomeVendedor();
            }
        });
        getSupportActionBar().setTitle("Produtos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lstProd = (RecyclerView) findViewById(R.id.lstCategoriasProds);

        categAbertas = new ArrayList<>();

        criarConexao();
        definirNomeVendedor();
        criarLista();
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

    public List<Produto> ordenarProdutos(List<Produto> prods){
        switch (ordem){
            case ORD_UN_VENDIDA:
                return prodRep.ordenarProdutosPorUnidadesVendidasUp(prods);
            case ORD_ULT_VENDA_DOWN:
                return prodRep.ordenarProdutosPorUltimaVendaDown(prods);
            case ORD_ULT_VENDA_UP:
                return prodRep.ordenarProdutosPorUltimaVendaUp(prods);
        }
        return prodRep.buscarTodos();
    }
    public void criarLista(){
//        lstProd.setHasFixedSize(true);
        List<Produto> dados = ordenarProdutos(pesquisaText());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lstProd.setLayoutManager(linearLayoutManager);
        if (ordem == ORD_CATEG) {
            categAdapter = new CategProdAdapter(dados,categAbertas,getApplicationContext());
            lstProd.setAdapter(categAdapter);
        } else {
            prodAdapter = new ProdutoAdapter(dados, getApplicationContext());
            lstProd.setAdapter(prodAdapter);
        }
    }

    public void novoProd(){
        // Rotina a ser executada quando o Botão for apertado
        categAbertas = categAdapter.categoriasAbertas();
        Intent it = new Intent(ListaProdutos.this, NovoProduto.class);
        startActivityForResult(it, 15);
    }

    public List<Produto> pesquisaText() {
        String text = "";
        if (!"".contains(pesqText)) {
            text = pesqText.toUpperCase().trim();
        }
        dados = prodRep.buscarTodos();
        List<Produto> novosDados = new ArrayList<>();
        for (int i = 0; i < dados.size(); i++) {
            Produto prod = dados.get(i);
            if (prod.nome.toUpperCase().contains(text) || prod.categoria.toUpperCase().contains(text) || Integer.toString(prod.codigo).contains(text)) {
                novosDados.add(dados.get(i));
            }
        }
        return novosDados;
    }

    public void editarNomeVendedor(){
        AlertDialog.Builder dialogVendedor = new AlertDialog.Builder(this);
        final EditText edtNomeVendedor = new EditText(this);
        edtNomeVendedor.setText(txtNomeVendedor.getText().toString());
        dialogVendedor
                .setView(edtNomeVendedor)
                .setTitle("Nome do Revendedor")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        nomeVendedor = edtNomeVendedor.getText().toString();
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("nomeRevendedor",nomeVendedor.trim()).apply();
                        txtNomeVendedor.setText(nomeVendedor);
                    }
                })
                .show();

        // Rotina para adicionar ou modificar foto do vendedor
    }

    public void definirNomeVendedor(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        nomeVendedor = prefs.getString("nomeRevendedor","");
        txtNomeVendedor.setText(nomeVendedor);
    }

    public android.text.Spanned negrito(String text){
        return Html.fromHtml("<b>" + text + "</b>");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        final Produto produto;
        if (comCateg) {
            produto = categAdapter.prodAdapter.getContextMenuProduto();
        } else {
            produto = prodAdapter.getContextMenuProduto();
        }
        switch (id){
            case ITEM_EDITAR:
                Intent it2 = new Intent(this, NovoProduto.class);
                it2.putExtra("PRODUTO",produto);
                startActivityForResult(it2,2);
                break;
            case ITEM_DELETAR:
                AlertDialog.Builder dlgDel = new AlertDialog.Builder(this);
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
                        criarLista();
                    }
                });
                dlgDel.show();
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_listprodutos, menu);
        itemOrdDataVenda = menu.findItem(R.id.ordenarDataVenda);
        itemOrdCateg = menu.findItem(R.id.ordenarCateg);
        itemOrdCateg.setTitle(negrito("Categorias:"));
        itemOrdUnVendida = menu.findItem(R.id.ordenarPorUnVendidas);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                char seta_cima = '\u2191';
                itemOrdDataVenda.setTitle("Data da Venda: " + seta_cima);
                pesqText = newText;
                pesquisaText();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        char seta_baixo = '\u2193';
        char seta_cima = '\u2191';

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.novoprod:
                novoProd();
                break;

            case R.id.ordenarCateg:
                itemOrdCateg.setTitle(negrito("Categorias:"));
                itemOrdDataVenda.setTitle("Data da Venda: " + seta_cima);
                itemOrdUnVendida.setTitle("Unidades Vendidas:");
                ordem = ORD_CATEG;
                criarLista();
                Toast.makeText(getApplicationContext(), "Produtos ordenados com sucesso", Toast.LENGTH_LONG).show();
                break;
            case R.id.ordenarPorUnVendidas:
                itemOrdUnVendida.setTitle(negrito("Unidades Vendidas:"));
                ordem = ORD_UN_VENDIDA;
                itemOrdCateg.setTitle("Categorias:");
                itemOrdDataVenda.setTitle("Data da Venda: " + seta_cima);
                criarLista();
                Toast.makeText(getApplicationContext(), "Produtos ordenados com sucesso", Toast.LENGTH_LONG).show();
                break;
            case R.id.ordenarDataVenda:
                itemOrdCateg.setTitle("Categorias:");
                itemOrdUnVendida.setTitle("Unidades Vendidas:");
                if (item.getTitle().toString().contains("Data da Venda: " + seta_baixo)) {
                    item.setTitle(negrito("Data da Venda: " + seta_cima));
                    ordem = ORD_ULT_VENDA_DOWN;
                    criarLista();
                } else {
                    item.setTitle(negrito("Data da Venda: " + seta_baixo));
                    ordem = ORD_ULT_VENDA_UP;
                    criarLista();
                }
                Toast.makeText(getApplicationContext(), "Produtos ordenados com sucesso", Toast.LENGTH_LONG).show();
                break;
        }
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.vendas:
                Intent it2 = new Intent(this, ListaVendas.class);
                startActivity(it2);
                break;
            case R.id.clientes:
                Intent it4 = new Intent(this, ListaClientes.class);
                startActivity(it4);
                break;
            case R.id.encomendas:
                Intent it5 = new Intent(this, ListaEncomendas.class);
                startActivity(it5);
                break;
            case R.id.debitos:
                Intent it6 = new Intent(this, Debitos.class);
                startActivity(it6);
                break;
            case R.id.pedidos:
                Intent it7 = new Intent(this,ListaPedidos.class);
                startActivity(it7);
                break;
            case R.id.estoque:
                Intent it8 = new Intent(this,Estoque.class);
                startActivity(it8);
                break;
            case R.id.config:
                Intent it9 = new Intent(this, SettingsActivity.class);
                startActivityForResult(it9,10);
                break;
        }
        mDrawer.closeDrawer(GravityCompat.START);
        return false;
    }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            definirNomeVendedor();
            comCateg = true;
            prodRep = new ProdutosRepositorio(this);
            criarLista();
        }
}

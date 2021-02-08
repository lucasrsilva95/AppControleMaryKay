package com.example.controlemk;

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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.controlemk.Adapters.CategProdEstoqueAdapter;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class Estoque extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;
    private TextView txtNomeVendedor;
    private ImageButton botNomeVendedor;
    private String nomeVendedor;


    public VendasRepositorio vendRep;
    public ProdutosRepositorio prodRep;

    public SQLiteDatabase conexao;

    private RecyclerView lstEstoque;

    public EditText edtPesq;
    public ConstraintLayout layoutPesq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estoque);
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

        lstEstoque = (RecyclerView) findViewById(R.id.lstEstoque);
        getSupportActionBar().setTitle("Estoque");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        criarConexao();
        definirNomeVendedor();
//        prodRep.recriarEstoque();
        criarLista(prodRep.produtosEmEstoque());
    }

    private void criarConexao() {

        try {
            vendRep = new VendasRepositorio(this);
            prodRep = new ProdutosRepositorio(this);

        } catch (SQLiteException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }

    public void criarLista(List<Produto> dados){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lstEstoque.setLayoutManager(linearLayoutManager);
        CategProdEstoqueAdapter adapter = new CategProdEstoqueAdapter(dados, getApplicationContext());
        lstEstoque.setAdapter(adapter);
    }


    public void pesquisaText(String text) {
        text = text.toUpperCase().trim();
        List<Produto> dados = prodRep.produtosEmEstoque();
        List<Produto> novosDados = new ArrayList<>();
        for (int i = 0; i < dados.size(); i++) {
            Produto prod = dados.get(i);
            if ((prod.nome.toUpperCase().contains(text)) || (Integer.toString(prod.codigo).contains(text)) || (prod.categoria.contains(text)) || (prod.detalhes.contains(text)) || (Float.toString(prod.preço).contains(text))) {
                novosDados.add(dados.get(i));
            }
        }
        criarLista(novosDados);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_estoque, menu);
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


            case R.id.atualizar:
                prodRep.recriarEstoque();
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
            case R.id.produtos:
                Intent it3 = new Intent(this, ListaProdutos.class);
                startActivity(it3);
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
        criarLista(prodRep.produtosEmEstoque());
    }
}

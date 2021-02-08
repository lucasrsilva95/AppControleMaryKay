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

import com.example.controlemk.Adapters.HistVendasAdapter;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class ListaEncomendas extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;
    private TextView txtNomeVendedor;
    private ImageButton botNomeVendedor;
    private String nomeVendedor;

    public VendasRepositorio vendRep;
    public ProdutosRepositorio prodRep;
    public ClientesRepositorio cliRep;

    public SQLiteDatabase conexao;

    public RecyclerView lstEncomendas;

    public EditText edtPesq;
    public ConstraintLayout layoutPesq;

    public HistVendasAdapter encomendasAdapter;

    private List<Venda> dados;

    private MenuItem itemOrdDatas, itemOrdClientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_encomendas);
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
        getSupportActionBar().setTitle("Encomendas");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lstEncomendas = (RecyclerView) findViewById(R.id.lstEncomendas);

        criarConexao();
        definirNomeVendedor();
        criarLista(vendRep.buscarTodasEncomendas());


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

    public void criarLista(List<Venda> dados){
//        lstProd.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lstEncomendas.setLayoutManager(linearLayoutManager);
        encomendasAdapter = new HistVendasAdapter(dados, this);
        lstEncomendas.setAdapter(encomendasAdapter);
    }

    public void novaEncomenda(View view){
        Venda encomenda = new Venda();
        encomenda.efetivada = false;
        Intent it = new Intent(ListaEncomendas.this, NovaVenda.class);
        it.putExtra("ENCOMENDA",encomenda);
        startActivityForResult(it,15);
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


    public void pesquisaText(String text) {
        text = text.toUpperCase().trim();
        List<Venda> dados = vendRep.buscarTodasEncomendas();
        List<Venda> novosDados = new ArrayList<>();
        for (int i = 0; i < dados.size(); i++) {
            Venda venda = dados.get(i);
            if (venda.nome.toUpperCase().contains(text) || venda.dataVenda.toUpperCase().contains(text)) {
                novosDados.add(dados.get(i));
            }
        }
        criarLista(novosDados);
    }

    public android.text.Spanned negrito(String text){
        return Html.fromHtml("<b>" + text + "</b>");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_listencomendas, menu);
        itemOrdDatas = menu.findItem(R.id.ordenar_datas);
        itemOrdDatas.setTitle(negrito("Última Encomenda: \u2191"));
        itemOrdClientes = menu.findItem(R.id.ordenarClientes);

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

        char seta_baixo = '\u2193';
        char seta_cima = '\u2191';
        //noinspection SimplifiableIfStatement
        switch (id) {

            case R.id.ordenar_datas:
                itemOrdClientes.setTitle("Clientes: A-Z");
                if (item.getTitle().toString().contains("Última Encomenda: " + seta_baixo)) {
                    item.setTitle(negrito("Última Encomenda: " + seta_cima));
                    criarLista(vendRep.ordenarVendasPorDataDown(vendRep.buscarTodasEncomendas()));
                } else {
                    item.setTitle(negrito("Última Encomenda: " + seta_baixo));
                    criarLista(vendRep.ordenarVendasPorDataUp(vendRep.buscarTodasEncomendas()));
                }
                Toast.makeText(getApplicationContext(), "Encomendas ordenadas com sucesso", Toast.LENGTH_LONG).show();
                break;
            case R.id.ordenarClientes:
                itemOrdDatas.setTitle("Última Encomenda: " + seta_cima);
                if (item.getTitle().toString().contains("Clientes: A-Z")) {
                    criarLista(vendRep.ordenarClientesAZ(vendRep.buscarTodasEncomendas()));
                    item.setTitle(negrito("Clientes: Z-A"));
                } else {
                    criarLista(vendRep.ordenarClientesZA(vendRep.buscarTodasEncomendas()));
                    item.setTitle(negrito("Clientes: A-Z"));
                }
                Toast.makeText(getApplicationContext(), "Encomendas ordenadas com sucesso", Toast.LENGTH_LONG).show();
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
        criarLista(vendRep.buscarTodasEncomendas());
    }
}

package com.example.controlemk;

import android.content.Context;
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

import com.example.controlemk.Adapters.MesVendaAdapter;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.controlemk.Adapters.HistVendasAdapter.ITEM_DELETAR;
import static com.example.controlemk.Adapters.HistVendasAdapter.ITEM_EDITAR;

public class ListaVendas extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;
    private TextView txtNomeVendedor;
    private ImageButton botNomeVendedor;
    private String nomeVendedor, pesqText = "";

    public VendasRepositorio vendRep;
    public ProdutosRepositorio prodRep;
    private ClientesRepositorio cliRep;

    public MesVendaAdapter mesVendaAdapter;
    public SQLiteDatabase conexao;

    public RecyclerView lstVendas;

    public EditText edtPesq;
    public ConstraintLayout layoutPesq;

    public List<String> mesesAbertos;
    private List<Venda> dados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_vendas);
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
        getSupportActionBar().setTitle("Vendas");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lstVendas = (RecyclerView) findViewById(R.id.lstVendas);

        mesesAbertos = new ArrayList<>();

        criarConexao();
        definirNomeVendedor();
        definirConfigs();
        criarLista();
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
//    public void criarLista(){
//        criarLista(vendRep.ordenarVendasPorDataUp(vendRep.buscarTodasEfetivadas()));
//    }
    public void criarLista(){
//        lstProd.setHasFixedSize(true);
        List<Venda> vendas = pesquisaText();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lstVendas.setLayoutManager(linearLayoutManager);
        mesVendaAdapter = new MesVendaAdapter(vendas, mesesAbertos, this);
        lstVendas.setAdapter(mesVendaAdapter);
    }

    public void novaVenda(View view){
        if(prodRep.produtosEmEstoque().size() > 0) {
            mesesAbertos = mesVendaAdapter.mesesAbertos();
            Intent it = new Intent(ListaVendas.this, NovaVenda.class);
            startActivityForResult(it, 15);
        }else{
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this)
                    .setTitle("Estoque Vazio")
                    .setMessage("Você não tem nenhum produto em estoque.\n\nGostaria de registrar um pedido?")
                    .setNegativeButton("Não",null)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent it = new Intent(ListaVendas.this, NovoPedido.class);
                            startActivityForResult(it, 15);
                        }
                    });
            dlgAlert.show();
        }
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


    public List<Venda> pesquisaText() {
        String text = pesqText.toUpperCase().trim();
        List<Venda> dados = vendRep.buscarTodasEfetivadas();
        List<Venda> novosDados = new ArrayList<>();
        for (int i = 0; i < dados.size(); i++) {
            Venda venda = dados.get(i);
            if (venda.nome.toUpperCase().contains(text) || venda.dataVenda.toUpperCase().contains(text)) {
                novosDados.add(dados.get(i));
            }
        }
        return novosDados;
    }

    public void definirConfigs(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String meses = prefs.getString("MESES_ABERTOS","");
        if(!"".contentEquals(meses)){
            mesesAbertos = new ArrayList<>(Arrays.asList(meses.split(";")));
        }
    }
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        final Venda venda = mesVendaAdapter.vendaAdapter.getContextMenuVenda();
        switch (id){
            case ITEM_EDITAR:
                Intent it2 = new Intent(ListaVendas.this, NovaVenda.class);
                it2.putExtra("EDITAR_VENDA",venda);
                startActivityForResult(it2,2);
                break;
            case ITEM_DELETAR:
                AlertDialog.Builder dlgDel = new AlertDialog.Builder(this);
                final String titulo = getSupportActionBar().getTitle().toString().substring(12);
                dlgDel = new AlertDialog.Builder(this);
                dlgDel.setTitle(String.format("Deletar %s?",titulo));
                dlgDel.setMessage(String.format("Tem certeza que deseja deletar a %s?",titulo));
                dlgDel.setNegativeButton("Não",null);
                dlgDel.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        vendRep.excluir(venda.id);
                        prodRep.atualizarQuantEstoqueProds(venda.produtos, true);
                        cliRep.definirNotificacoes(false);
                        Toast.makeText(getApplicationContext(),String.format("%s Excluida com sucesso",titulo),Toast.LENGTH_LONG).show();
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
        inflater.inflate(R.menu.menu_listvendas, menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
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

        //noinspection SimplifiableIfStatement

        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
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
        vendRep = new VendasRepositorio(this);
        criarLista();
    }
}

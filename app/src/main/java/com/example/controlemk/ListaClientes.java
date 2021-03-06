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

import com.example.controlemk.Adapters.ListaClientesAdapter;
import com.example.controlemk.Dominio.Entidades.Cliente;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import static com.example.controlemk.Adapters.HistVendasAdapter.ITEM_DELETAR;
import static com.example.controlemk.Adapters.HistVendasAdapter.ITEM_EDITAR;

public class ListaClientes extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener{


    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;
    private TextView txtNomeVendedor;
    private ImageButton botNomeVendedor;
    private String nomeVendedor;

    public VendasRepositorio vendRep;
    public ProdutosRepositorio prodRep;
    public ClientesRepositorio cliRep;

    public List<Cliente> dados;

    private ListaClientesAdapter clientesAdapter;
    public SQLiteDatabase conexao;

    public RecyclerView lstClientes;

    public static final int ORD_CLI_AZ = 1, ORD_CLI_ZA = 2, ORD_ULTCOMP_UP = 3, ORD_ULTCOMP_DOWN = 4;
    public int ordem = 1;
    public String pesqText = "";

    public EditText edtPesq;
    public ConstraintLayout layoutPesq;

    private MenuItem itemOrdDatas, itemOrdClientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_clientes);
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
        getSupportActionBar().setTitle("Clientes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lstClientes = (RecyclerView) findViewById(R.id.lstClientes);

        criarConexao();
        definirNomeVendedor();
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

    public List<Cliente> ordenarClientes(){
        cliRep = new ClientesRepositorio(this);
        switch (ordem){
            case ORD_CLI_ZA:
                return cliRep.ordenarClientesZA();
            case ORD_ULTCOMP_DOWN:
                return cliRep.ordenarClientesPorDatasDown();
            case ORD_ULTCOMP_UP:
                return cliRep.ordenarClientesPorDatasUp();
        }
        return cliRep.ordenarClientesAZ();
    }

    public void criarLista(){
        dados = pesquisaText(ordenarClientes());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lstClientes.setLayoutManager(linearLayoutManager);
        clientesAdapter = new ListaClientesAdapter(dados, getApplicationContext());
        lstClientes.setAdapter(clientesAdapter);
    }

    public void novoCliente(View view){
        Intent it = new Intent(ListaClientes.this, NovoCliente.class);
        startActivityForResult(it, 20);
    }



    public List<Cliente> pesquisaText(List<Cliente> dados) {
        String text = pesqText.toUpperCase().trim();
        List<Cliente> novosDados = new ArrayList<>();
        for (int i = 0; i < dados.size(); i++) {
            Cliente cli = dados.get(i);
            if (cli.ultimaVendaCliente(this) != null) {
                if ((cli.nome.toUpperCase().contains(text)) || (cli.ultimaVendaCliente(this).dataVenda.contains(text)) || (cli.telefone.contains(text))|| (cli.proxPagamentoCliente(this).contains(text))) {
                    novosDados.add(dados.get(i));
                }
            } else {
                if ((cli.nome.toUpperCase().contains(text)) || (cli.telefone.contains(text))|| (cli.proxPagamentoCliente(this).contains(text))) {
                    novosDados.add(dados.get(i));
                }
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
        final Cliente cliente = clientesAdapter.getContextMenuCliente();
        switch (id){
            case ITEM_EDITAR:
                Intent it2 = new Intent(this, NovoCliente.class);
                it2.putExtra("CLIENTE",cliente);
                startActivityForResult(it2,2);
                break;
            case ITEM_DELETAR:
                AlertDialog.Builder dlgDel = new AlertDialog.Builder(this);
                dlgDel = new AlertDialog.Builder(this);
                dlgDel.setTitle("Deletar Cliente?");
                dlgDel.setMessage("Tem certeza que deseja deletar o cliente?");
                dlgDel.setNegativeButton("Não",null);
                dlgDel.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cliRep.excluir(cliente.id);
                        Toast.makeText(getApplicationContext(),"Cliente Excluido com sucesso",Toast.LENGTH_LONG).show();
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
        inflater.inflate(R.menu.menu_listclientes, menu);
        itemOrdDatas = menu.findItem(R.id.ordenar_datas);
        itemOrdClientes = menu.findItem(R.id.ordenarClientes);
        itemOrdClientes.setTitle(negrito("Clientes: A-Z"));

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
                criarLista();
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
                if (item.getTitle().toString().contains("Última Compra: " + seta_baixo)) {
                    ordem = ORD_ULTCOMP_DOWN;
                    item.setTitle(negrito("Última Compra: " + seta_cima));
                    criarLista();
                } else {
                    item.setTitle(negrito("Última Compra: " + seta_baixo));
                    ordem = ORD_ULTCOMP_UP;
                    criarLista();
                }
                Toast.makeText(getApplicationContext(), "Clientes ordenados com sucesso", Toast.LENGTH_LONG).show();
                break;

            case R.id.ordenarClientes:
                itemOrdDatas.setTitle("Última Compra: " + seta_cima);
                if (item.getTitle().toString().contains("Clientes: A-Z")) {
                    ordem = ORD_CLI_AZ;
                    criarLista();
                    item.setTitle(negrito("Clientes: Z-A"));
                } else {
                    ordem = ORD_CLI_ZA;
                    criarLista();
                    item.setTitle(negrito("Clientes: A-Z"));
                }
                Toast.makeText(getApplicationContext(), "Clientes ordenados com sucesso", Toast.LENGTH_LONG).show();
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
        criarLista();
    }
}

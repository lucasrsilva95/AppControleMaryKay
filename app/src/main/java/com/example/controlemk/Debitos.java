package com.example.controlemk;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.Adapters.DebitosAdapter;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;


public class Debitos extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;
    private TextView txtNomeVendedor;
    private ImageButton botNomeVendedor;
    private String nomeVendedor;

    public VendasRepositorio vendRep;
    public ProdutosRepositorio prodRep;
    public ClientesRepositorio cliRep;
    public OperacoesDatas opDatas;

    public SQLiteDatabase conexao;

    public RecyclerView lstDebitos;
    public TextView txtTotDebitos;

    public DebitosAdapter adapter;

    public List<Venda> dados,dadosAntigos;

    public String filtro = "",ordem = "",pesqText = "";
    public boolean atualizarTotal = true, pesqDebitosHoje = false;

    private MenuItem itemCliAZ,itemCliDatasParc, itemParcNaoPagas, itemParcPagas, itemTodasParc;

    private SearchView searchView;

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_debitos);
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
        getSupportActionBar().setTitle("Debitos Clientes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lstDebitos = (RecyclerView) findViewById(R.id.lstDebitos);
        txtTotDebitos = (TextView) findViewById(R.id.txtTotDebitos);

        criarConexao();
        definirNomeVendedor();
        dados = definirLista();
        criarLista(dados);
        verificaParametro();

        txtTotDebitos.setText(String.format("R$%.2f",vendRep.somaTotalVendas(dados)));

        txtTotDebitos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                criarLista(definirLista());
            }
        });
    }


    private void criarConexao() {

        try {
            vendRep = new VendasRepositorio(this);
            prodRep = new ProdutosRepositorio(this);
            cliRep = new ClientesRepositorio(this);
            opDatas = new OperacoesDatas(this);

        } catch (SQLiteException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }

    public void verificaParametro(){

        bundle = getIntent().getExtras();

        if((bundle != null) && (bundle.containsKey("DEBITOS_HOJE"))) {
            pesqDebitosHoje = true;
        }
    }

    public void criarLista(List<Venda> debitos){
//        List<Venda> debitos = pesquisaText(edtPesq.getText().toString());
        lstDebitos.setHasFixedSize(true);
        Log.d("Debitos", "criarLista: Inicio LinearLayout");
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        lstDebitos.setLayoutManager(linearLayoutManager2);
        adapter = new DebitosAdapter(debitos, this,txtTotDebitos,ordem);
        Log.d("Debitos", "criarLista: Set Adapter");
        lstDebitos.setAdapter(adapter);
        float total = vendRep.somaTotalVendas(debitos);
        if (!txtTotDebitos.getText().toString().contentEquals(String.format("R$%.2f", total))) {
            txtTotDebitos.setText(String.format("R$%.2f",total));
        }
    }

    public List<Venda>definirLista(){
        List<Venda> novosDados = new ArrayList<>(cliRep.debitosClientes());
        getSupportActionBar().setTitle("Debitos Clientes");
        if (!"".contentEquals(ordem)) {
            if (ordem.contentEquals("Parcelas Pagas")) {
                novosDados = cliRep.parcPagasClientes();
                getSupportActionBar().setTitle("Parcelas Pagas");
            } else if (ordem.contentEquals("Todas as Parcelas")) {
                novosDados = cliRep.todasParcClientes();
                getSupportActionBar().setTitle("Todas as Parcelas");
            }
        }

        novosDados = pesquisaText(novosDados);

        if (!"".contentEquals(filtro)) {
            if(filtro.contentEquals("NomeClienteAZ")){
                return vendRep.ordenarClientesAZ(novosDados);
            }else if(filtro.contentEquals("NomeClienteZA")){
                return vendRep.ordenarClientesZA(novosDados);
            }else if(filtro.contentEquals("DataParcela UP")) {
                return vendRep.ordenarVendasPorDataPagamentoUp(novosDados);
            }else if(filtro.contentEquals("DataParcela DOWN")){
                return vendRep.ordenarVendasPorDataPagamentoDown(novosDados);}
        }

        return vendRep.ordenarVendasPorDataPagamentoDown(novosDados);
    }

    public List<Venda> pesquisaText(List<Venda> dados) {
        String text = pesqText.toUpperCase().trim();
        List<Venda> novosDados = new ArrayList<>();
        if (!"".contentEquals(text)) {
            for(Venda venda:dados){
                if ((venda.nome.toUpperCase().contains(text)) || (venda.datasPag.get(0).contains(text))) {
                    novosDados.add(venda);
                }
            }
        }else{
            return dados;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_listdebitos, menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        itemCliAZ = menu.findItem(R.id.ordenarClientes);
        itemCliDatasParc = menu.findItem(R.id.ordenar_datas_parc);
        itemCliDatasParc.setTitle(negrito("Data Pagamento: \u2191"));
        itemParcPagas = menu.findItem(R.id.parcelasPagas);
        itemParcNaoPagas = menu.findItem(R.id.parcelasNaoPagas);
        itemParcNaoPagas.setTitle(negrito("Parcelas Não Pagas"));
        itemTodasParc = menu.findItem(R.id.todasParcelas);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                pesqText = newText;
                criarLista(definirLista());
                return false;
            }
        });

        if(pesqDebitosHoje){
            searchView.setQuery(opDatas.dataAtual(),true);
            searchView.setIconified(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        char seta_baixo = '\u2193';
        char seta_cima = '\u2191';
        //noinspection SimplifiableIfStatement
        switch (id) {

            case R.id.ordenar_datas_parc:
                itemCliAZ.setTitle("Clientes: A-Z");
                if (item.getTitle().toString().contentEquals("Data Pagamento: " + seta_baixo)) {
                    item.setTitle(negrito("Data Pagamento: " + seta_cima));
                    filtro = "DataParcela DOWN";
                    criarLista(definirLista());
                } else {
                    item.setTitle(negrito("Data Pagamento: " + seta_baixo));
                    filtro = "DataParcela UP";
                    criarLista(definirLista());
                }
                Toast.makeText(getApplicationContext(), "Clientes ordenados com sucesso", Toast.LENGTH_LONG).show();
                break;

            case R.id.ordenarClientes:
                itemCliDatasParc.setTitle("Data Pagamento: " + seta_baixo);
                if (item.getTitle().toString().contentEquals("Clientes: A-Z")) {
                    filtro = "NomeClienteAZ";
                    item.setTitle(negrito("Clientes: Z-A"));
                    criarLista(definirLista());
                } else {
                    filtro = "NomeClienteZA";
                    item.setTitle(negrito("Clientes: A-Z"));
                    criarLista(definirLista());
                }
                break;

            case R.id.parcelasPagas:
                itemParcPagas.setTitle(negrito("Parcelas Pagas"));

                itemParcNaoPagas.setTitle("Parcelas Não Pagas");
                itemTodasParc.setTitle("Todas as Parcelas");
                ordem = "Parcelas Pagas";
                criarLista(definirLista());
                break;

            case R.id.parcelasNaoPagas:
                itemParcNaoPagas.setTitle(negrito("Parcelas Não Pagas"));

                itemParcPagas.setTitle("Parcelas Pagas");
                itemTodasParc.setTitle("Todas as Parcelas");
                ordem = "Parcelas Não Pagas";
                criarLista(definirLista());
                break;

            case R.id.todasParcelas:
                itemTodasParc.setTitle(negrito("Todas as Parcelas"));

                itemParcPagas.setTitle("Parcelas Pagas");
                itemParcNaoPagas.setTitle("Parcelas Não Pagas");
                ordem = "Todas as Parcelas";
                criarLista(definirLista());
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
        criarLista(definirLista());
    }
}

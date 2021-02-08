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

import com.example.controlemk.Adapters.HistPedidosAdapter;
import com.example.controlemk.Dominio.Entidades.Pedido;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.PedidosRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import static com.example.controlemk.Adapters.HistVendasAdapter.ITEM_DELETAR;
import static com.example.controlemk.Adapters.HistVendasAdapter.ITEM_EDITAR;

public class ListaPedidos extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;
    private TextView txtNomeVendedor;
    private ImageButton botNomeVendedor;
    private String nomeVendedor;

    public PedidosRepositorio pedRep;
    public ProdutosRepositorio prodRep;
    private ClientesRepositorio cliRep;

    public SQLiteDatabase conexao;

    public RecyclerView lstPedidos;
    public HistPedidosAdapter pedidosAdapter;

    private List<Pedido> dados;

    private MenuItem itemOrdDatas,itemOrdValBruto, itemOrdCusto, itemOrdDesconto;

    public static final int ORD_DATA_UP = 1,ORD_DATA_DOWN = 2,ORD_CUSTO_UP = 3,ORD_CUSTO_DOWN = 4,ORD_VAL_BRUTO_UP = 5
            ,ORD_VAL_BRUTO_DOWN = 6, ORD_DESCONTO_UP = 7, ORD_DESCONTO_DOWN = 8;
    private int ordem = ORD_DATA_UP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_pedidos);
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
        getSupportActionBar().setTitle("Pedidos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lstPedidos = (RecyclerView) findViewById(R.id.lstPedidos);

        criarConexao();
        definirNomeVendedor();
        criarLista();
    }

    private void criarConexao() {

        try {

            pedRep = new PedidosRepositorio(this);
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

    public List<Pedido> ordenarPedidos(){
        List<Pedido> pedidos = pedRep.buscarTodos();
        switch (ordem){
            case ORD_DATA_UP:       return pedRep.ordenarPedidosPorDataUp(pedidos);
            case ORD_DATA_DOWN:     return pedRep.ordenarPedidosPorDataDown(pedidos);
            case ORD_CUSTO_UP:      return pedRep.ordenarPedidosPorCustoUp(pedidos);
            case ORD_CUSTO_DOWN:    return pedRep.ordenarPedidosPorCustoDown(pedidos);
            case ORD_VAL_BRUTO_UP:  return pedRep.ordenarPedidosPorValorBrutoUp(pedidos);
            case ORD_VAL_BRUTO_DOWN:return pedRep.ordenarPedidosPorValorBrutoDown(pedidos);
            case ORD_DESCONTO_UP:   return pedRep.ordenarPedidosPorDescontoPercentUp(pedidos);
            case ORD_DESCONTO_DOWN: return pedRep.ordenarPedidosPorDescontoPercentDown(pedidos);
        }
        return pedidos;
    }

    public void criarLista(){
//        lstProd.setHasFixedSize(true);
        List<Pedido> pedidos = ordenarPedidos();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lstPedidos.setLayoutManager(linearLayoutManager);
        pedidosAdapter = new HistPedidosAdapter(pedidos,this);
        lstPedidos.setAdapter(pedidosAdapter);
    }

    public void novoPedido(View view){
        Intent it = new Intent(ListaPedidos.this,NovoPedido.class);
        startActivityForResult(it,101);
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
        final Pedido pedido = pedidosAdapter.getPedidoContextMenu();
        switch (id){
            case ITEM_EDITAR:
                Intent it2 = new Intent(this, NovoPedido.class);
                it2.putExtra("EDITAR_PEDIDO",pedido);
                startActivityForResult(it2,2);
                break;
            case ITEM_DELETAR:
                AlertDialog.Builder dlgDel;
                dlgDel = new AlertDialog.Builder(this);
                dlgDel.setTitle("Deletar Pedido?");
                dlgDel.setMessage("Tem certeza que deseja deletar esse pedido?");
                dlgDel.setNegativeButton("Não",null);
                dlgDel.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pedRep.excluir(pedido.id);
                        prodRep.atualizarQuantEstoqueProds(pedido.produtos, false);
                        Toast.makeText(getApplicationContext(),"Pedido deletado com sucesso",Toast.LENGTH_LONG).show();
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
        inflater.inflate(R.menu.menu_listpedidos, menu);
        itemOrdDatas = menu.findItem(R.id.ordenar_datas);
        itemOrdCusto = menu.findItem(R.id.ordenar_custo);
        itemOrdValBruto = menu.findItem(R.id.ordenar_valbruto);
        itemOrdDesconto = menu.findItem(R.id.ordenar_desconto);

        itemOrdDatas.setTitle(negrito("Data do Pedido: \u2193"));

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
                itemOrdCusto.setTitle("Preço de Custo: " + seta_cima);
                itemOrdValBruto.setTitle("Valor Bruto: " + seta_cima);
                itemOrdDesconto.setTitle("Desconto %: " + seta_cima);

                if (item.getTitle().toString().contains("Data do Pedido: " + seta_baixo)) {
                    item.setTitle(negrito("Data do Pedido: " + seta_cima));
                    ordem = ORD_DATA_DOWN;
                    criarLista();
                } else {
                    item.setTitle(negrito("Data do Pedido: " + seta_baixo));
                    ordem = ORD_DATA_UP;
                    criarLista();
                }
                Toast.makeText(getApplicationContext(), "Pedidos ordenados com sucesso", Toast.LENGTH_LONG).show();
                break;
            case R.id.ordenar_custo:
                itemOrdDatas.setTitle("Data do Pedido: " + seta_cima);
                itemOrdValBruto.setTitle("Valor Bruto: " + seta_cima);
                itemOrdDesconto.setTitle("Desconto %: " + seta_cima);

                if (item.getTitle().toString().contains("Preço de Custo: " + seta_baixo)) {
                    item.setTitle(negrito("Preço de Custo: " + seta_cima));
                    ordem = ORD_CUSTO_DOWN;
                    criarLista();
                } else {
                    item.setTitle(negrito("Preço de Custo: " + seta_baixo));
                    ordem = ORD_CUSTO_UP;
                    criarLista();
                }
                Toast.makeText(getApplicationContext(), "Pedidos ordenados com sucesso", Toast.LENGTH_LONG).show();
                break;
            case R.id.ordenar_valbruto:
                itemOrdDatas.setTitle("Data do Pedido: " + seta_cima);
                itemOrdCusto.setTitle("Preço de Custo: " + seta_cima);
                itemOrdDesconto.setTitle("Desconto %: " + seta_cima);

                if (item.getTitle().toString().contains("Valor Bruto: " + seta_baixo)) {
                    item.setTitle(negrito("Valor Bruto: " + seta_cima));
                    ordem = ORD_VAL_BRUTO_DOWN;
                    criarLista();
                } else {
                    item.setTitle(negrito("Valor Bruto: " + seta_baixo));
                    ordem = ORD_VAL_BRUTO_UP;
                    criarLista();
                }
                Toast.makeText(getApplicationContext(), "Pedidos ordenados com sucesso", Toast.LENGTH_LONG).show();
                break;
            case R.id.ordenar_desconto:
                itemOrdDatas.setTitle("Data do Pedido: " + seta_cima);
                itemOrdCusto.setTitle("Preço de Custo: " + seta_cima);
                itemOrdValBruto.setTitle("Valor Bruto: " + seta_cima);

                if (item.getTitle().toString().contains("Desconto %: " + seta_baixo)) {
                    item.setTitle(negrito("Desconto %: " + seta_cima));
                    ordem = ORD_DESCONTO_DOWN;
                    criarLista();
                } else {
                    item.setTitle(negrito("Desconto %: " + seta_baixo));
                    ordem = ORD_DESCONTO_UP;
                    criarLista();
                }
                Toast.makeText(getApplicationContext(), "Pedidos ordenados com sucesso", Toast.LENGTH_LONG).show();
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

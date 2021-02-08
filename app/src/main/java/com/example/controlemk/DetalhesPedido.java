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

import com.example.controlemk.Adapters.ProdsSelVendaAdapter;
import com.example.controlemk.Dominio.Entidades.Pedido;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Repositorio.PedidosRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;

import java.util.List;

public class DetalhesPedido extends AppCompatActivity {


    public ProdutosRepositorio prodRep;
    public PedidosRepositorio pedRep;

    public SQLiteDatabase conexao;

    public Pedido pedido;

    public TextView txtData, txtValTot, txtDescontoPercent, txtValProds, txtDesconto, txtValCusto;

    public RecyclerView lstProds;

    private AlertDialog.Builder dlgDel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_pedido);
        getSupportActionBar().setTitle("Detalhes do Pedido");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        {
            txtData = (TextView) findViewById(R.id.txtData);
            txtValTot = (TextView) findViewById(R.id.txtValTot);
            txtDescontoPercent = (TextView) findViewById(R.id.txtDescontoPercent);
            txtValProds = (TextView) findViewById(R.id.txtValProds);
            txtDesconto = (TextView) findViewById(R.id.txtDesconto);
            txtValCusto = (TextView) findViewById(R.id.txtValCusto);

            lstProds = (RecyclerView) findViewById(R.id.lstProdsPedido);
        } // Declaração de Variáveis

        criarConexao();
        verificaParametro();
    }

    private void criarConexao() {

        try {
            pedRep = new PedidosRepositorio(this);
            prodRep = new ProdutosRepositorio(this);

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

        if((bundle != null) && (bundle.containsKey("PEDIDO"))){
            pedido = (Pedido)bundle.getSerializable("PEDIDO");
            definirParametros();
        }
    }

    private void definirParametros(){
        txtData.setText(pedido.data);
        txtValTot.setText(String.format("R$%.2f",pedido.totalBruto));
        txtValProds.setText(String.format("R$%.2f",pedido.totalBruto));
        txtDescontoPercent.setText(((int)(pedido.desconto*100)) + "%");
        txtDesconto.setText((String.format("R$%.2f",pedido.desconto*pedido.totalBruto)));
        txtValCusto.setText(String.format("R$%.2f",pedido.totalCusto));
        criarLista();
    }

    public void criarLista() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lstProds.setLayoutManager(linearLayoutManager);
        List<Produto> dados = pedido.produtos;
        ProdsSelVendaAdapter adapter = new ProdsSelVendaAdapter(dados, this);
        lstProds.setAdapter(adapter);

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
                Intent it2 = new Intent(DetalhesPedido.this, NovoPedido.class);
                it2.putExtra("EDITAR_PEDIDO",pedido);
                startActivityForResult(it2,2);
                break;
            case R.id.menuItem_del:
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
        pedRep = new PedidosRepositorio(this);
        pedido = pedRep.buscarPedido(pedido.id);
        definirParametros();
    }
}

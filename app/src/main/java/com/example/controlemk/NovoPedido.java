package com.example.controlemk;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.Adapters.ProdsSelVendaAdapter;
import com.example.controlemk.Dominio.Entidades.Pedido;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.PedidosRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;

import java.util.ArrayList;
import java.util.List;

public class NovoPedido extends AppCompatActivity{

    private Button botCalendario;

    private TextView txtValBruto,txtValBruto2,txtDesconto,txtValCusto;
    public ConstraintLayout layoutParc1,layoutParc2,layoutParc3;
    private Spinner spinnerDesconto;
    private ImageView botAddCliente;

    private Bundle bundle;

    private Pedido pedido;
    private PedidosRepositorio pedidoRep;
    private ProdutosRepositorio prodRep;
    private ClientesRepositorio cliRep;
    private SQLiteDatabase conexao;

    private OperacoesDatas opDatas;

    private List<Produto> selecionados;

    private RecyclerView lstProds;
    private ProdsSelVendaAdapter prodAdapter;

    private AlertDialog.Builder dlgAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_pedido);
        getSupportActionBar().setTitle("Novo Pedido");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        {
            botCalendario = (Button) findViewById(R.id.botData);
            lstProds = (RecyclerView) findViewById(R.id.lstProdsPedido);
            txtValBruto = (TextView) findViewById(R.id.txtValTot);
            txtValBruto2 = (TextView) findViewById(R.id.txtValProds);
            txtDesconto = (TextView) findViewById(R.id.txtDesconto);
            txtValCusto = (TextView) findViewById(R.id.txtValCusto);
            spinnerDesconto = (Spinner) findViewById(R.id.spinnerDesconto);
            botAddCliente = (ImageView) findViewById(R.id.botAddCliente);
        }

        criarConexao();



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, descontos());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDesconto.setAdapter(adapter);
        spinnerDesconto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pedido.desconto = (Float.parseFloat((spinnerDesconto.getSelectedItem().toString()).replaceAll("%","")))/100;
                txtDesconto.setText(String.format("R$%.2f",pedido.desconto * pedido.totalBruto));
                pedido.totalCusto = pedido.totalBruto * (1 - pedido.desconto);
                txtValCusto.setText(String.format("R$%.2f",pedido.totalCusto));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        botCalendario.setText(opDatas.dataAtual());
        pedido = new Pedido();
        pedido.data = botCalendario.getText().toString();
        verificaParametro();
        criarLista();
    }

    private void criarConexao() {

        try {
            pedidoRep = new PedidosRepositorio(this);
            prodRep = new ProdutosRepositorio(this);
            opDatas = new OperacoesDatas(this);

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

        if((bundle != null) && ((bundle.containsKey("PEDIDO")) || (bundle.containsKey("EDITAR_PEDIDO")))){
            if ((bundle.containsKey("PEDIDO"))) {
                pedido = (Pedido)bundle.getSerializable("PEDIDO");
            } else {
                pedido = (Pedido)bundle.getSerializable("EDITAR_PEDIDO");
                getSupportActionBar().setTitle("Editar Pedido");
            }
            if (pedido.data == null) {
                pedido.data = opDatas.dataAtual();
            }
            botCalendario.setText(pedido.data);
            spinnerDesconto.setSelection((int) (pedido.desconto*100/5));
            txtDesconto.setText(String.format("R$%.2f",pedido.desconto * pedido.totalBruto));
            txtValBruto.setText(String.format("R$%.2f",pedido.totalBruto));
            txtValBruto2.setText(String.format("R$%.2f",pedido.totalBruto));
            txtValCusto.setText(String.format("R$%.2f",pedido.totalCusto));
        }
    }

    public void criarLista(){
//        lstProd.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lstProds.setLayoutManager(linearLayoutManager);
        prodAdapter = new ProdsSelVendaAdapter(pedido.produtos,getApplicationContext());
        lstProds.setAdapter(prodAdapter);
    }

    private List<String> descontos() {
        List<String> desc = new ArrayList<>();
        for(int i = 0; i < 100 ; i+=5){
            desc.add(i + "%");
        }

        return desc;
    }

    public void botAddProd(View view){
        pedido.data = botCalendario.getText().toString();
        Intent it = new Intent(NovoPedido.this, ListaProdsVenda.class);
        it.putExtra("PEDIDO",pedido);
        startActivityForResult(it, 15);
//        finish();
    }

    public void dataCalendario(View view){
        botCalendario = ((Button)view);
        String data = botCalendario.getText().toString();
        if("00/00/0000".equals(data)){
            data = opDatas.dataAtual();
        }
        int dia = Integer.parseInt(data.substring(0,2));
        int mes = Integer.parseInt(data.substring(3,5));
        int ano = Integer.parseInt(data.substring(6,10));
        DatePickerDialog datePickerVenda = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        botCalendario.setText(String.format("%02d/%02d/%04d",dayOfMonth,month+1,year));
                    }
                },
                ano, mes - 1, dia
        );
        datePickerVenda.show();
    }

    public boolean validaCampos() {
        boolean res = false;

        {
            if (res = (pedido.produtos.size() == 0)) {
                dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setTitle("Aviso");
                dlgAlert.setMessage("Nenhum produto selecionado");
                dlgAlert.setNeutralButton("Ok", null);
                dlgAlert.show();
            }

        }
        return !res;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_venda, menu);
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
            case R.id.itemSalvarVenda:
                if (validaCampos()) {
                    pedido.data = botCalendario.getText().toString();
                    if (pedido.id == 0) {
                        pedidoRep.inserir(pedido);
                        prodRep.atualizarQuantEstoqueProds(pedido.produtos, true);
                    } else {
                        pedidoRep.alterar(pedido);
                        prodRep.recriarEstoque();
                    }
                    Toast.makeText(this, "Pedido Salvo com Sucesso",Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            pedido = (Pedido) data.getExtras().getSerializable("PEDIDO");
            criarLista();

            txtValBruto.setText(String.format("R$%.2f",pedido.totalBruto));
            txtValBruto2.setText(String.format("R$%.2f",pedido.totalBruto));
            pedido.totalCusto = pedido.totalBruto * (1 - pedido.desconto);
            txtValCusto.setText(String.format("R$%.2f",pedido.totalCusto));
            txtDesconto.setText(String.format("R$%.2f",pedido.desconto * pedido.totalBruto));
        }
    }
}

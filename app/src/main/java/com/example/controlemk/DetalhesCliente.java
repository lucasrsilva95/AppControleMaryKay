package com.example.controlemk;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.Adapters.PagamentosClienteAdapter;
import com.example.controlemk.Adapters.ProdsEncomendadosAdapter;
import com.example.controlemk.Adapters.VendasClienteAdapter;
import com.example.controlemk.Dominio.Entidades.Cliente;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;

import java.util.List;

public class DetalhesCliente extends AppCompatActivity {

    public ProdutosRepositorio prodRep;
    public VendasRepositorio vendRep;
    public ClientesRepositorio cliRep;

    public SQLiteDatabase conexao;

    public TextView txtNome, txtTelefone, txtEndereco, txtDetalhes;

    private Cliente cliente;

    private List<Venda> encomendas;

    public RecyclerView lstVendasCliente, lstPagamentosCliente, lstEncomendas;

    public int id;

    private AlertDialog.Builder dlgDel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_cliente);
        getSupportActionBar().setTitle("Detalhes Cliente");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        {
            txtNome = findViewById(R.id.txtNome);
            txtTelefone = findViewById(R.id.txtTelefone);
            txtEndereco = findViewById(R.id.txtEndereco);
            txtDetalhes = findViewById(R.id.txtDetalhes);
            lstVendasCliente = findViewById(R.id.lstVendasCliente);
            lstPagamentosCliente = findViewById(R.id.lstPagamentosCliente);
            lstEncomendas = findViewById(R.id.lstEncomendas);
        } // Declaração de Variáveis
        criarConexao();
        verificaParametro();
    }

    private void criarConexao() {

        try {
            vendRep = new VendasRepositorio(this);
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

        Bundle bundle = getIntent().getExtras();

        if((bundle != null) && (bundle.containsKey("CLIENTE"))){
            cliente = (Cliente)bundle.getSerializable("CLIENTE");
            encomendas = cliente.encomendasPorProdutoCliente(this);
            definirParametrosCliente();
        }
    }

    public void definirParametrosCliente(){
        id = cliente.id;
        txtNome.setText(cliente.nome);
        txtTelefone.setText(cliente.telefone);
        txtEndereco.setText(cliente.endereço);
        txtDetalhes.setText(cliente.detalhes);
        criarListas();
    }

    public void criarListas() {
        lstVendasCliente.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        lstVendasCliente.setLayoutManager(linearLayoutManager1);
        vendRep = new VendasRepositorio(this);
        List<Venda> dados = vendRep.ordenarVendasPorDataUp(cliente.vendasCliente(this));
        VendasClienteAdapter adapter = new VendasClienteAdapter(dados, this);
        lstVendasCliente.setAdapter(adapter);

        lstPagamentosCliente.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        lstPagamentosCliente.setLayoutManager(linearLayoutManager2);
        List<Venda> dados2 = vendRep.ordenarVendasPorDataPagamentoDown(cliente.vendasPorPagamentosCliente(this));
        PagamentosClienteAdapter adapter2 = new PagamentosClienteAdapter(dados2, this);
        lstPagamentosCliente.setAdapter(adapter2);

        lstEncomendas.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager3 = new LinearLayoutManager(this);
        lstEncomendas.setLayoutManager(linearLayoutManager3);
        ProdsEncomendadosAdapter adapter3 = new ProdsEncomendadosAdapter(encomendas, this);
        lstEncomendas.setAdapter(adapter3);
    }

    public void botNovaEncomenda(View view){
        Venda encomenda = new Venda();
        encomenda.nome = cliente.nome;
        encomenda.efetivada = false;
        Intent it = new Intent(DetalhesCliente.this, NovaVenda.class);
        it.putExtra("ENCOMENDA", encomenda);
        startActivityForResult(it, 15);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detalhes_cliente, menu);
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
                Intent it2 = new Intent(DetalhesCliente.this, NovoCliente.class);
                it2.putExtra("CLIENTE",cliente);
                startActivityForResult(it2,2);
                break;
            case R.id.menuItem_del:
                dlgDel = new AlertDialog.Builder(this);
                dlgDel.setTitle("Deletar Cliente?");
                dlgDel.setMessage("Tem certeza que deseja deletar o cliente?");
                dlgDel.setNegativeButton("Não",null);
                dlgDel.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cliRep.excluir(cliente.id);
                        Toast.makeText(getApplicationContext(),"Cliente Excluido com sucesso",Toast.LENGTH_LONG).show();
                        setResult(3);
                        finish();
                    }
                });
                dlgDel.show();
                break;
            case R.id.menuItem_ligar:
                if (cliente.telefone.length() >= 8) {
                    Uri number = Uri.parse("tel:"+cliente.telefone);
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                    startActivity(callIntent);
                } else {
                    Toast.makeText(this, "Telefone inválido", Toast.LENGTH_LONG).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case 8:
                finish();
        }
        cliente = cliRep.buscarCliente(cliente.id);
        definirParametrosCliente();
    }
}

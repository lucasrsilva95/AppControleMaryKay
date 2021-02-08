package com.example.controlemk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.controlemk.Dominio.Entidades.Cliente;
import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;

public class NovoCliente extends AppCompatActivity {

    private int id;
    private boolean atualizar;

    private EditText edtNome, edtTelefone, edtEndereco, edtDetalhes;
    private Button botSalvar;

    private AlertDialog.Builder dlgErro, dlgAlert, dlgSubs;

    private ClientesRepositorio cliRep;

    private Cliente cliente;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_cliente);
        getSupportActionBar().setTitle("Novo Cliente");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        {
            edtNome = (EditText) findViewById(R.id.edtNome);
            edtTelefone = (EditText) findViewById(R.id.edtTelefone);
            edtEndereco = (EditText) findViewById(R.id.edtEndereco);
            edtDetalhes = (EditText) findViewById(R.id.edtDetalhes);
            botSalvar = (Button) findViewById(R.id.botaoSalvar);
        } // Declarando Variáveis

        criarConexao();

        atualizar = false;

        verificaParametro();

    }

    private void criarConexao() {
        // Criando conexão com o banco de dados
        try {
            cliRep = new ClientesRepositorio(this);

        } catch (SQLiteException ex) { // No caso de não ser possivel criar a conexão
            dlgErro = new AlertDialog.Builder(this);
            dlgErro.setTitle("Erro");
            dlgErro.setMessage(ex.getMessage());
            dlgErro.setNeutralButton("OK", null);
            dlgErro.show();
        }
    }
    private void verificaParametro() {

        cliente = new Cliente();
        Bundle bundle = getIntent().getExtras();

        if ((bundle != null) && (bundle.containsKey("CLIENTE"))) {
            atualizar = true;
            cliente = (Cliente) bundle.getSerializable("CLIENTE");
            id = cliente.id;
            edtNome.setText(cliente.nome);
            edtTelefone.setText(cliente.telefone);
            edtEndereco.setText(cliente.endereço);
            getSupportActionBar().setTitle("Editar Cliente");
            botSalvar.setText("ATUALIZAR");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtNome.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            edtNome.clearFocus();
        } else {
            edtNome.requestFocus();
            abrirTeclado(edtNome);
        }
    }

    public void botSalvarCliente(final View view) {
        if (!validaCampos()) {
            cliente.nome = edtNome.getText().toString().trim();
            cliente.telefone = edtTelefone.getText().toString().trim();
            cliente.endereço = edtEndereco.getText().toString().trim();
            cliente.detalhes = edtDetalhes.getText().toString().trim();

            cliRep = new ClientesRepositorio(this);
            final Cliente clienteExistente = cliRep.buscarClientePeloNome(cliente.nome);
            if (clienteExistente == null){
                inserirAtualizarCliente(cliente, atualizar);
            }else if (clienteExistente.id == cliente.id) {
                inserirAtualizarCliente(cliente, true);
            } else{
                dlgSubs = new AlertDialog.Builder(this);
                dlgSubs.setTitle("Cliente Já Existe");
                dlgSubs.setMessage(String.format("Já existe um cliente com esse nome, deseja substituílo? \n\n Nome do Cliente: %s\n Telefone: %s\n Endereço: %s\n Detalhes: %s",clienteExistente.nome, clienteExistente.telefone, clienteExistente.endereço, clienteExistente.detalhes));
                dlgSubs.setNegativeButton("Não", null);
                dlgSubs.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cliRep.excluir(clienteExistente.id);
                        inserirAtualizarCliente(cliente, true);
                    }
                });
                dlgSubs.show();
            }
        }

    }

    public void inserirAtualizarCliente(Cliente cli, boolean atualizar) {
        if (atualizar) {
            try {
                cli.id = id;

                cliRep.alterar(cli);

            } catch (SQLiteException ex) { // No caso de não ser possivel criar a conexão
                dlgErro.setTitle("Erro");
                dlgErro.setMessage(ex.getMessage());
                dlgErro.setNeutralButton("OK", null);
                dlgErro.show();
            } finally {
                Toast.makeText(this, "Cliente atualizado com sucesso", Toast.LENGTH_LONG).show();
                atualizar = false;
                setResult(8);
                finish();
            }
        } else {
            try {
                cliRep.inserir(cli);

            } catch (SQLiteException ex) { // No caso de não ser possivel criar a conexão
                dlgErro.setTitle("Erro");
                dlgErro.setMessage(ex.getMessage());
                dlgErro.setNeutralButton("OK", null);
                dlgErro.show();
            } finally {
                Toast.makeText(this, "Cliente inserido com sucesso", Toast.LENGTH_LONG).show();
                setResult(8);
                finish();
            }
        }
    }

    public void botLimpar(View view) {
        edtNome.setText("");
        edtTelefone.setText("");
        edtEndereco.setText("");
        edtDetalhes.setText("");
        edtNome.requestFocus();
    }





    public boolean validaCampos() {
        boolean res = false;
        String campVaz = "";
        String exemplo = "";

        {
            if (res = isCampoVazio(edtNome.getText().toString())) {
                edtNome.requestFocus();
                campVaz = "Nome";
                exemplo = edtNome.getHint().toString();
            }
            if (res) {
                dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setTitle("Aviso");
                dlgAlert.setMessage(String.format("O campo %s não está preenchido corretamente \n\n %s", campVaz, exemplo));
                dlgAlert.setNeutralButton("Ok", null);
                dlgAlert.show();
            }

        }
        return res;
    }

    private boolean isCampoVazio(String valor) {
        boolean result = (TextUtils.isEmpty(valor) || valor.trim().isEmpty());
        return result;
    }

    public void abrirTeclado(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public void fecharTeclado(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
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
        }
        return super.onOptionsItemSelected(item);
    }
}

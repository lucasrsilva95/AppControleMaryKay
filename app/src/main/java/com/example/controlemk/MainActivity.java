package com.example.controlemk;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.controlemk.Dominio.Entidades.Pedido;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.PedidosRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.example.controlemk.database.BancoOpenHelper;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Class act;

    private TextView txtNomeVendedor;
    private ImageButton botNomeVendedor;

    private ProdutosRepositorio prodRep;
    private ClientesRepositorio cliRep;
    private VendasRepositorio vendRep;
    private PedidosRepositorio pedRep;

    private String nomeVendedor;

    public static final String nomeSharedPreferences = "APP_MK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        criarConexao();
        definirConfigs();
//        prodRep.limparLista();
//        prodRep.inserirProdutosIniciais();

        if (prodRep.buscarTodos().size() == 0) {
            prodRep.inserirProdutosIniciais();
        }
        if (cliRep.buscarTodos().size() == 0 && vendRep.buscarTodos().size() != 0) {
            cliRep.atualizarClientes();
        }
        if (verificaPermissoes()) {
            pagInicial();
        }
//        this.deleteDatabase("BANCO");

//        prodRep.consertarIdTodosProdutos();
    }

    public void pagInicial(){
        definirPagInicial();
        Intent it = new Intent(MainActivity.this,act);
        startActivity(it);
        finish();
    }

    private void criarConexao() {

        try {
            vendRep = new VendasRepositorio(this);
            prodRep = new ProdutosRepositorio(this);
            cliRep = new ClientesRepositorio(this);
            pedRep = new PedidosRepositorio(this);

        } catch (SQLiteException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }

    public void definirPagInicial(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String pag = prefs.getString("act_ini","Vendas");
        if(pag.contentEquals("Vendas")){
            act = ListaVendas.class;
        }else if(pag.contentEquals("Produtos")){
            act = ListaProdutos.class;
        }else if(pag.contentEquals("Clientes")){
            act = ListaClientes.class;
        }else if(pag.contentEquals("Encomendas")){
            act = ListaEncomendas.class;
        }else if(pag.contentEquals("Débitos")){
            act = Debitos.class;
        }else if(pag.contentEquals("Pedidos")){
            act = ListaPedidos.class;
        }else {
            act = Estoque.class;
        }
    }

    public boolean verificaPermissoes(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
            return false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.RECEIVE_BOOT_COMPLETED},
                    0);
            return false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.READ_CALENDAR},
                    0);
            return false;
        }
        return true;
    }

    public void definirConfigs(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notif = prefs.getBoolean("switch_notific", true);
        String horaConfig = prefs.getString("horaNotif", "erro");
        if(notif){
            if (horaConfig.contentEquals("erro")) {
                cliRep.definirNotificacoes(true);
                prefs.edit().putString("horaNotif","09:00").apply();
            }else{
                cliRep.definirNotificacoes(false);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (verificaPermissoes()) {
            pagInicial();
        }else{
            verificaPermissoes();
        }
    }
}

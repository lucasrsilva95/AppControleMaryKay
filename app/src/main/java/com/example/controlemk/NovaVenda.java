package com.example.controlemk;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.Adapters.ProdsSelVendaAdapter;
import com.example.controlemk.Dominio.Entidades.Cliente;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NovaVenda extends AppCompatActivity {

    private Button botCalendario, botDataParcela1, botDataParcela2, botDataParcela3, botSelecionado;

    private EditText edtNome;
    private TextView txtTotVend, lblNumParc, txtNomeEncomenda, lblDataVenda;
    private EditText edtValParc1, edtValParc2, edtValParc3;
    public ConstraintLayout layoutParc1, layoutParc2, layoutParc3;
    private Spinner spinnerClientes, spinnerParcelas;
    private ImageView botAddCliente;

    private Bundle bundle;

    private Venda venda;
    private VendasRepositorio vendRep;
    private ProdutosRepositorio prodRep;
    private ClientesRepositorio cliRep;
    private SQLiteDatabase conexao;

    private OperacoesDatas opDatas;

    private List<Produto> selecionados;

    private RecyclerView lstProds;
    private ProdsSelVendaAdapter prodAdapter;

    private int numParcelas;
    public static final int PERMISSION_CALENDAR = 1;

    private boolean trocarParcelas;

    private AlertDialog.Builder dlgAlert;

    public List<String> idEventos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova__venda);
        getSupportActionBar().setTitle("Nova Venda");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        {
            edtNome = (EditText) findViewById(R.id.txtNome);
            botCalendario = (Button) findViewById(R.id.txtDataVenda);
            botDataParcela1 = (Button) findViewById(R.id.botParcela1);
            botDataParcela2 = (Button) findViewById(R.id.botParcela2);
            botDataParcela3 = (Button) findViewById(R.id.botParcela3);
            edtValParc1 = (EditText) findViewById(R.id.edtValParc1);
            edtValParc2 = (EditText) findViewById(R.id.edtValParc2);
            edtValParc3 = (EditText) findViewById(R.id.edtValParc3);
            layoutParc1 = (ConstraintLayout) findViewById(R.id.layoutParcela1);
            layoutParc2 = (ConstraintLayout) findViewById(R.id.layoutParcela2);
            layoutParc3 = (ConstraintLayout) findViewById(R.id.layoutParcela3);
            lstProds = (RecyclerView) findViewById(R.id.lstProdsVenda);
            txtTotVend = (TextView) findViewById(R.id.txtValTotVenda2);
            lblNumParc = (TextView) findViewById(R.id.lblNumParc);
            lblDataVenda = (TextView) findViewById(R.id.lblDataVenda);
            txtNomeEncomenda = (TextView) findViewById(R.id.txtNomeEncomenda);
            spinnerClientes = (Spinner) findViewById(R.id.spinnerClientes);
            spinnerParcelas = (Spinner) findViewById(R.id.txtParcelas);
            botAddCliente = (ImageView) findViewById(R.id.botAddCliente);
        }
        trocarParcelas = true;

        criarConexao();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, clientes());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClientes.setAdapter(adapter);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[]{"Á vista", "1x", "2x", "3x"});
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerParcelas.setAdapter(adapter2);
        spinnerParcelas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                numParcelas = position;
                if (trocarParcelas || !getSupportActionBar().getTitle().toString().contentEquals("Editar Venda")) {
                    configParcelas(numParcelas);
                } else {
                    trocarParcelas = true;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        botCalendario.setText(opDatas.dataAtual());
        venda = new Venda();
        venda.dataVenda = botCalendario.getText().toString();
        verificaParametro();
        criarLista();

        edtValParc1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                switch (numParcelas) {
                    case 2:
                        if (!"".contains(edtValParc1.getText().toString()) && !"".contains(edtValParc2.getText().toString())) {
                            float val1 = Float.parseFloat(edtValParc1.getText().toString().replace(",","."));
                            float val2 = Float.parseFloat(edtValParc2.getText().toString().replace(",","."));
                            if (!String.format(Locale.ENGLISH,"%.2f", venda.total).contentEquals(String.format(Locale.ENGLISH,"%.2f", val1 + val2))) {
                                edtValParc2.setText(String.format(Locale.ENGLISH,"%.2f", venda.total - val1));
                            }
                        }
                        break;
                    case 3:
                        if (!"".contains(edtValParc1.getText().toString()) && !"".contains(edtValParc2.getText().toString()) && !"".contains(edtValParc3.getText().toString())) {
                            float val1 = Float.parseFloat(edtValParc1.getText().toString().replace(",","."));
                            float val2 = Float.parseFloat(edtValParc2.getText().toString().replace(",","."));
                            float val3 = Float.parseFloat(edtValParc3.getText().toString().replace(",","."));
                            if (!String.format(Locale.ENGLISH,"%.2f", venda.total).contentEquals(String.format(Locale.ENGLISH,"%.2f", val1 + val2 + val3))) {
                                edtValParc2.setText(String.format(Locale.ENGLISH,"%.2f", (venda.total - val1) / 2));
                            }
                        }
                        break;
                }
            }
        });

        edtValParc2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                switch (numParcelas) {
                    case 2:
                        if (!"".contains(edtValParc1.getText().toString()) && !"".contains(edtValParc2.getText().toString())) {
                            float val1 = Float.parseFloat(edtValParc1.getText().toString().replace(",","."));
                            float val2 = Float.parseFloat(edtValParc2.getText().toString().replace(",","."));
                            if (!String.format(Locale.ENGLISH,"%.2f", venda.total).contentEquals(String.format(Locale.ENGLISH,"%.2f", val1 + val2))) {
                                edtValParc1.setText(String.format(Locale.ENGLISH,"%.2f", venda.total - val2));
                            }
                        }
                        break;
                    case 3:
                        if (!"".contains(edtValParc1.getText().toString()) && !"".contains(edtValParc2.getText().toString()) && !"".contains(edtValParc3.getText().toString())) {
                            float val1 = Float.parseFloat(edtValParc1.getText().toString().replace(",","."));
                            float val2 = Float.parseFloat(edtValParc2.getText().toString().replace(",","."));
                            float val3 = Float.parseFloat(edtValParc3.getText().toString().replace(",","."));
                            if (!String.format(Locale.ENGLISH,"%.2f", venda.total).contentEquals(String.format(Locale.ENGLISH,"%.2f", val1 + val2 + val3))) {
                                edtValParc3.setText(String.format(Locale.ENGLISH,"%.2f", (venda.total - val1 - val2)));
                            }
                        }
                        break;
                }
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

    private void verificaParametro() {

        bundle = getIntent().getExtras();

        if ((bundle != null) && ((bundle.containsKey("VENDA")) || (bundle.containsKey("EDITAR_VENDA")))) {
            if ((bundle.containsKey("VENDA"))) {
                venda = (Venda) bundle.getSerializable("VENDA");
            } else {
                venda = (Venda) bundle.getSerializable("EDITAR_VENDA");
                getSupportActionBar().setTitle("Editar Venda");
                prodRep.atualizarQuantEstoqueProds(venda.produtos, true);
            }
            trocarParcelas = false;
            if (clientes().contains(venda.nome)) {
                spinnerClientes.setSelection(clientes().lastIndexOf(venda.nome));
                edtNome.setVisibility(View.INVISIBLE);
                spinnerClientes.setVisibility(View.VISIBLE);
            } else {
                edtNome.setText(venda.nome);
                edtNome.setVisibility(View.VISIBLE);
                spinnerClientes.setVisibility(View.INVISIBLE);
            }
            botCalendario.setText(venda.dataVenda);
            txtTotVend.setText(String.format(Locale.ENGLISH,"R$%.2f", venda.total));
            if (venda.datasPag.size() > 0) {
                spinnerParcelas.setSelection(venda.datasPag.size());
                configBotParcelas(venda.datasPag.size());
            }
        }

        if ((bundle != null) && (bundle.containsKey("ENCOMENDA"))) {
            venda = (Venda) bundle.getSerializable("ENCOMENDA");
            getSupportActionBar().setTitle("Nova Encomenda");
            if (venda.dataVenda != null) {
                botCalendario.setText(venda.dataVenda);
            }
            txtTotVend.setText(String.format(Locale.ENGLISH,"R$%.2f", venda.total));
            if (venda.nome != null) {
                txtNomeEncomenda.setText(venda.nome);
                txtNomeEncomenda.setVisibility(View.VISIBLE);
                edtNome.setVisibility(View.INVISIBLE);
                spinnerClientes.setVisibility(View.INVISIBLE);
                botAddCliente.setVisibility(View.INVISIBLE);
            }
            lblDataVenda.setText("Data Encomenda:");
            spinnerParcelas.setVisibility(View.INVISIBLE);
            lblNumParc.setVisibility(View.INVISIBLE);
        }
    }

    public void criarLista() {
//        lstProd.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lstProds.setLayoutManager(linearLayoutManager);
        prodAdapter = new ProdsSelVendaAdapter(venda.produtos, getApplicationContext());
        lstProds.setAdapter(prodAdapter);
    }

    private List<String> clientes() {
        List<String> cli = cliRep.buscarNomesDosClientes();
        if (cli.size() == 0) {
            spinnerClientes.setVisibility(View.INVISIBLE);
            botAddCliente.setVisibility(View.INVISIBLE);
            edtNome.setVisibility(View.VISIBLE);
        }
        return cli;
    }

    public void botAddProd(View view) {
        salvarParametros();
        Intent it = new Intent(NovaVenda.this, ListaProdsVenda.class);
        if ((bundle != null) && bundle.containsKey("ENCOMENDA")) {
            it.putExtra("ENCOMENDA", venda);
        } else {
            it.putExtra("VENDA", venda);
        }
        startActivityForResult(it, 15);
//        finish();
    }

    public void salvarParametros() {
        venda.dataVenda = botCalendario.getText().toString();
        if (venda.efetivada) {
            if (edtNome.getVisibility() == View.VISIBLE) {
                venda.nome = edtNome.getText().toString().trim();
            } else {
                venda.nome = spinnerClientes.getSelectedItem().toString();
            }
            idEventos = new ArrayList<>();
            if (venda.datasNaoPagas.size() > 0) {
                for(String parc:venda.datasNaoPagas){
                    if (parc.split("=").length > 2) {
                        idEventos.add(parc.split("=")[2]);
                    }
                }
            }
            venda.datasPag.clear();
            if (layoutParc1.getVisibility() == View.VISIBLE) {
                String data = botDataParcela1.getText().toString() + " = " + edtValParc1.getText().toString();
                if(idEventos.size() >= 1){
                    data = data.concat(" = " + idEventos.get(0));
                }
                venda.datasPag.add(data);
                if (layoutParc2.getVisibility() == View.VISIBLE) {
                    data = botDataParcela2.getText().toString() + " = " + edtValParc2.getText().toString();
                    if(idEventos.size() >= 2){
                        data = data.concat(" = " + idEventos.get(1));
                    }
                    venda.datasPag.add(data);
                    if (layoutParc3.getVisibility() == View.VISIBLE) {
                        data = botDataParcela3.getText().toString() + " = " + edtValParc3.getText().toString();
                        if(idEventos.size() >= 3){
                            data = data.concat(" = " + idEventos.get(2));
                        }
                        venda.datasPag.add(data);
                    }
                }
            }
            venda.datasNaoPagas = venda.datasPag;
            if (cliRep.buscarClientePeloNome(venda.nome) == null) {
                Cliente cliente = new Cliente();
                cliente.nome = venda.nome;
                cliRep.inserir(cliente);
            }
        } else if (venda.nome == null) {
            if (edtNome.getVisibility() == View.VISIBLE) {
                venda.nome = edtNome.getText().toString();
            } else {
                venda.nome = spinnerClientes.getSelectedItem().toString();
            }
        }
    }

    public void botNovoCliente(View view) {
        if (edtNome.getVisibility() == View.INVISIBLE) {
            spinnerClientes.setVisibility(View.INVISIBLE);
            edtNome.setVisibility(View.VISIBLE);
            edtNome.requestFocus();
            abrirTeclado(edtNome);
        } else {
            spinnerClientes.setVisibility(View.VISIBLE);
            edtNome.setVisibility(View.INVISIBLE);
            fecharTeclado(edtNome);
        }
    }

    public void abrirTeclado(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public void fecharTeclado(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void dataCalendario(View view) {
        botSelecionado = ((Button) view);
        String data = botSelecionado.getText().toString();
        String dataVenda = botCalendario.getText().toString();
        String dataParc1 = botDataParcela1.getText().toString();
        String dataParc2 = botDataParcela2.getText().toString();
        if ("00/00/0000".equals(data)) {
            if (!"00/00/0000".equals(dataVenda) && (botSelecionado == botDataParcela1)) {
                data = dataVenda;
            } else if (!"00/00/0000".equals(dataParc1) && (botSelecionado == botDataParcela2)) {
                data = dataParc1;
            } else if (!"00/00/0000".equals(dataParc2) && (botSelecionado == botDataParcela3)) {
                data = dataParc2;
            } else {
                data = opDatas.dataAtual();
            }
        }
        int dia = Integer.parseInt(data.substring(0, 2));
        int mes = Integer.parseInt(data.substring(3, 5));
        int ano = Integer.parseInt(data.substring(6, 10));
        DatePickerDialog datePickerVenda = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        botSelecionado.setText(String.format(Locale.ENGLISH,"%02d/%02d/%04d", dayOfMonth, month + 1, year));
                        salvarParametros();
                    }
                },
                ano, mes - 1, dia
        );
        datePickerVenda.show();
    }

    public void configParcelas(int numParc) {
        configBotParcelas(numParc);
        Float valParc = venda.total / numParc;
        if (numParc >= 1) {
            edtValParc1.setText(String.format(Locale.ENGLISH,"%.2f", valParc));
            if (numParc >= 2) {
                edtValParc2.setText(String.format(Locale.ENGLISH, "%.2f", valParc));
                if (numParc == 3) {
                    edtValParc3.setText(String.format(Locale.ENGLISH,"%.2f", valParc));
                }
            }
        }
    }

    public void configBotParcelas(int numParc) {
        layoutParc1.setVisibility(View.INVISIBLE);
        layoutParc2.setVisibility(View.INVISIBLE);
        layoutParc3.setVisibility(View.INVISIBLE);
        if (numParc >= 1) {
            layoutParc1.setVisibility(View.VISIBLE);
            if (venda.datasPag.size() >= 1) {
                String[] parc1 = venda.datasPag.get(0).split("=");
                botDataParcela1.setText(parc1[0].trim());
                edtValParc1.setText(String.format(Locale.ENGLISH,"%.2f",Float.parseFloat(parc1[1].trim().replace(",","."))));
            }
            if (numParc >= 2) {
                layoutParc2.setVisibility(View.VISIBLE);
                if (venda.datasPag.size() >= 2) {
                    String[] parc2 = venda.datasPag.get(1).split("=");
                    botDataParcela2.setText(parc2[0].trim());
                    edtValParc2.setText(String.format(Locale.ENGLISH,"%.2f",Float.parseFloat(parc2[1].trim().replace(",","."))));
                }
                if (numParc == 3) {
                    layoutParc3.setVisibility(View.VISIBLE);
                    if (venda.datasPag.size() == 3) {
                        String[] parc3 = venda.datasPag.get(2).split("=");
                        botDataParcela3.setText(parc3[0].trim());
                        edtValParc3.setText(String.format(Locale.ENGLISH,"%.2f",Float.parseFloat(parc3[1].trim().replace(",","."))));
                    }
                }
            }
        }
    }

    public boolean validaCampos() {
        boolean res = false;
        String campVaz = "";
        String exemplo = "";

        {
            if (res = (isCampoVazio(edtNome.getText().toString())) && (edtNome.getVisibility() == View.VISIBLE)) {
                edtNome.requestFocus();
                campVaz = "Nome do Cliente";
                exemplo = edtNome.getHint().toString();
            }
            if (res) {
                dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setTitle("Aviso");
                dlgAlert.setMessage(String.format(Locale.ENGLISH,"O campo %s não está preenchido corretamente \n\n %s", campVaz, exemplo));
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

    public boolean verificarDados() {
        boolean res = verificarDatasParcelas();
        String maiorMenor = "";
        float soma = 0.0f;
        if (res && numParcelas >= 1) {
            soma += Float.parseFloat(edtValParc1.getText().toString());
            if (numParcelas >= 2) {
                soma += Float.parseFloat(edtValParc2.getText().toString());
                if (numParcelas == 3) {
                    soma += Float.parseFloat(edtValParc3.getText().toString());
                }
            }

            res = !((venda.total - soma > 0.02f) || (soma - venda.total > 0.02f));


            if (!res) {     // Se a res for false tem algum erro na soma
                if (soma < venda.total) {
                    maiorMenor = "MENOR";
                } else {
                    maiorMenor = "MAIOR";
                }
                dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setTitle("Erro");
                dlgAlert.setMessage(String.format(Locale.ENGLISH,"O valor da soma das parcelas é %s do que o valor total da compra \n\nSoma: R$%.2f\nValor Total da Venda: R$%.2f", maiorMenor, soma, venda.total));
                dlgAlert.setNeutralButton("Ok", null);
                dlgAlert.show();
            }
        }
        return res;
    }


    public boolean verificarDatasParcelas() {
        boolean res = true;
        String mensagem = "";
        OperacoesDatas op = new OperacoesDatas(this);
        if (numParcelas >= 1) {
            int dif1 = op.subtracaoDatas(botDataParcela1.getText().toString(), botCalendario.getText().toString());
            res = (dif1 >= 0);
            mensagem = "A data da 1ª parcela é menor do que a data da venda";
            if (numParcelas >= 2 && res) {
                int dif2 = op.subtracaoDatas(botDataParcela2.getText().toString(), botDataParcela1.getText().toString());
                res = (dif2 > 0);
                mensagem = "A data da 2ª parcela é menor do que a data da 1ª parcela";
                if (numParcelas == 3 && res) {
                    int dif3 = op.subtracaoDatas(botDataParcela3.getText().toString(), botDataParcela2.getText().toString());
                    res = (dif3 > 0);
                    mensagem = "A data da 3ª parcela é menor do que a data da 2ª parcela";
                }
            }
        }
        if (!res) {
            dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setTitle("Erro");
            dlgAlert.setMessage(mensagem);
            dlgAlert.setNeutralButton("Ok", null);
            dlgAlert.show();
        }
        return res;
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
                if(venda.id != 0){
                    prodRep.atualizarQuantEstoqueProds(vendRep.buscarVenda(venda.id).produtos, false);
                }
                finish();
                break;
            case R.id.itemSalvarVenda:
                if (verificarDados()) {
                    salvarParametros();
                    if (venda.id == 0) {
                        vendRep.inserir(venda);
                    } else {
                        vendRep.alterar(venda);
                    }
                    prodRep.atualizarQuantEstoqueProds(venda.produtos, false);
                    cliRep.definirNotificacoes(false);
                    Toast.makeText(this, String.format(Locale.ENGLISH,"%s Salva com Sucesso",getSupportActionBar().getTitle().toString().split(" ")[1]),Toast.LENGTH_SHORT).show();
                    if (venda.datasPagFuturas(this).size() > 0) {
                        String titulo = "Criar Eventos na agenda";
                        String mensagem = "Deseja criar eventos na agenda para cada parcela dessa venda?";
                        if(idEventos.size() == venda.datasNaoPagas.size()){
                            titulo = "Alterar Eventos na agenda";
                            mensagem = "Deseja alterar os eventos da agenda?";
                        }
                        AlertDialog.Builder dlgAgenda = new AlertDialog.Builder(this)
                                .setTitle(titulo)
                                .setMessage(mensagem)
                                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        venda.criarEventosNaAgenda(getApplicationContext());
                                        finish();
                                    }
                                });
                        dlgAgenda.show();
                    } else {
                        finish();
                    }
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            if (data.getExtras().containsKey("VENDA")) {
                venda = (Venda) data.getExtras().getSerializable("VENDA");
            } else {
                venda = (Venda) data.getExtras().getSerializable("ENCOMENDA");
            }
            criarLista();

            txtTotVend.setText(String.format(Locale.ENGLISH,"R$%.2f",venda.total));
            configParcelas(venda.datasPag.size());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivity main = new MainActivity();
        if (main.verificaPermissoes()) {
            venda.criarEventosNaAgenda(this);
        }
//        switch (requestCode){
//            case PERMISSION_CALENDAR:
//                criarEventosNaAgenda(venda);
//                break;
//        }

    }
}

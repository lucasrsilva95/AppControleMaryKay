package com.example.controlemk;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.Adapters.ParcelasVendaAdapter;
import com.example.controlemk.Adapters.ProdsSelVendaAdapter;
import com.example.controlemk.Dominio.Entidades.Pedido;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class DetalhesVenda extends AppCompatActivity {


    public ProdutosRepositorio prodRep;
    public VendasRepositorio vendRep;
    public ClientesRepositorio cliRep;
    public OperacoesDatas opDatas;

    public SQLiteDatabase conexao;

    public TextView txtNome, txtDataVenda, txtValTot, txtParcelas, lblNumParc, lblDataVenda;
    public View dividerParc;
    public Button btnRealizarVenda;

    public Venda venda;

    public RecyclerView lstProdsVenda, lstParcelasVenda;

    private AlertDialog.Builder dlgDel;

    public int id;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private int PERMISSAO_REQUEST = 102;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Uri uriPdf;
    public File filePath;
    public Bitmap bitmap;

    public String tipo_arquivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_venda);
        getSupportActionBar().setTitle("Detalhes da Venda");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        {
            txtNome = (TextView) findViewById(R.id.txtNome);
            txtDataVenda = (TextView) findViewById(R.id.txtDataVenda);
            txtValTot = (TextView) findViewById(R.id.txtValTotVenda2);
            txtParcelas = (TextView) findViewById(R.id.txtParcelas);
            lblNumParc = (TextView) findViewById(R.id.lblNumParc);
            lblDataVenda = (TextView) findViewById(R.id.lblDataVenda);

            lstProdsVenda = (RecyclerView) findViewById(R.id.lstProdsVenda);
            lstParcelasVenda = (RecyclerView) findViewById(R.id.lstParcelasVenda);
            dividerParc = (View) findViewById(R.id.dividerParcelas);

            btnRealizarVenda = (Button) findViewById(R.id.btnRealizarVenda);
        } // Declaração de Variáveis


        criarConexao();
        verificaParametro();
        definirConfigs();
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

    private void verificaParametro(){

        Bundle bundle = getIntent().getExtras();

        if((bundle != null) && (bundle.containsKey("VENDA"))){
            venda = (Venda)bundle.getSerializable("VENDA");
            definirParametrosVenda();
        }
    }

    public void definirParametrosVenda(){
        if(!venda.efetivada){
            getSupportActionBar().setTitle("Detalhes da Encomenda");
            txtParcelas.setVisibility(View.INVISIBLE);
            lblNumParc.setVisibility(View.INVISIBLE);
            dividerParc.setVisibility(View.INVISIBLE);
            lblDataVenda.setText("Data da Encomenda:");
            btnRealizarVenda.setVisibility(View.VISIBLE);
            if(prodRep.prodsIndisponiveisEmEstoque(venda.produtos).size() == 0){
                btnRealizarVenda.setTextColor(getResources().getColor(R.color.corDinheiro));
            }else{
                btnRealizarVenda.setTextColor(Color.RED);
            }
        }
        id = venda.id;
        txtNome.setText(venda.nome);
        txtDataVenda.setText(venda.dataVenda);
        txtValTot.setText(String.format("R$%.2f",venda.total));
        if (venda.datasPag.size() >= 1) {
            txtParcelas.setText(String.format("%dx",venda.datasPag.size()));
        } else {
            txtParcelas.setText("Á vista");
            dividerParc.setVisibility(View.INVISIBLE);
        }

        criarLista();
    }

    public void criarLista() {
        lstProdsVenda.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lstProdsVenda.setLayoutManager(linearLayoutManager);
        List<Produto> dados = venda.produtos;
        ProdsSelVendaAdapter adapter = new ProdsSelVendaAdapter(dados, this);
        lstProdsVenda.setAdapter(adapter);


        lstParcelasVenda.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        lstParcelasVenda.setLayoutManager(linearLayoutManager2);
        List<String> dados2 = venda.datasPag;
        ParcelasVendaAdapter adapter2 = new ParcelasVendaAdapter(dados2, venda, this);
        lstParcelasVenda.setAdapter(adapter2);
    }

    public void verCliente(View view){
        Intent it = new Intent(DetalhesVenda.this, DetalhesCliente.class);
        it.putExtra("CLIENTE", cliRep.buscarClientePeloNome(venda.nome));
        startActivityForResult(it,15);
    }

    public void realizarVenda(View view){
        if (prodRep.prodsIndisponiveisEmEstoque(venda.produtos).size() == 0) {
            Intent it = new Intent(this, NovaVenda.class);
            venda.dataVenda = opDatas.dataAtual();
            venda.efetivada = true;
            it.putExtra("VENDA",venda);
            startActivity(it);
            finish();
        }else{
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this)
                    .setTitle("Produtos Indisponíveis no estoque")
                    .setMessage("Você não tem todos os produtos encomendados no estoque.\n\nGostaria de registrar um pedido com esses produtos?")
                    .setNegativeButton("Não",null)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Pedido pedido = new Pedido();
                            pedido.produtos = prodRep.prodsIndisponiveisEmEstoque(venda.produtos);
                            pedido.calcValoresPedido();
                            Intent it = new Intent(DetalhesVenda.this, NovoPedido.class);
                            it.putExtra("PEDIDO",pedido);
                            startActivityForResult(it, 15);
                        }
                    });
            dlgAlert.show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Uri gerarPDF(){
        bitmap = gerarBitmap();
        PdfDocument documentoPDF = new PdfDocument();
        PdfDocument.PageInfo detalhesPagina = new PdfDocument.PageInfo.Builder(bitmap.getWidth(),bitmap.getHeight(),1).create();

        PdfDocument.Page novaPagina = documentoPDF.startPage(detalhesPagina);

        Canvas canvas = novaPagina.getCanvas();

        Rect rect = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        canvas.drawBitmap(bitmap,null,rect,null);

        documentoPDF.finishPage(novaPagina);


        String nomeArquivo = String.format("Detalhe_Venda_%s",venda.nome);

        File pasta = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/AppMK/tets");
        if(!pasta.exists()){
            pasta.mkdirs();
        }
        filePath = new File(pasta.getAbsolutePath() + File.separator + nomeArquivo + ".pdf");

        try{
            documentoPDF.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this,"PDF criado",Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this,"Erro ao gerar PDF: " + e.toString(),Toast.LENGTH_LONG).show();
        }

        documentoPDF.close();
        Uri uriPdf = FileProvider.getUriForFile(getBaseContext(), getBaseContext().getApplicationContext().getPackageName() + ".fileprovider", filePath);

        return uriPdf;
    }

    public Bitmap gerarBitmap(){
        ConstraintLayout layout = findViewById(R.id.layoutDetalhesVenda2);
        layout.setBackgroundColor(Color.WHITE);

        bitmap = bitmap.createBitmap(layout.getWidth(),layout.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        layout.draw(canvas);
        return bitmap;
    }
    public void compartilharImagem(){

        bitmap = gerarBitmap();

        String path = MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"Detalhes Venda",null);

        Uri uriImagem = Uri.parse(path);

        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType("image/jpeg")
                .putExtra(Intent.EXTRA_STREAM,uriImagem);
        startActivity(Intent.createChooser(intent,"Compartilhar Imagem Venda"));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void compartilharPDF(){
        uriPdf = gerarPDF();
        if(uriPdf != null){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uriPdf);

            startActivityForResult(Intent.createChooser(intent,"Compartilhar os Detalhes da Venda"),1011);
        }
    }

    public void definirConfigs(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        tipo_arquivo = prefs.getString("act_arquivo", "Imagem");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detalhes_venda, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
                Intent it2 = new Intent(DetalhesVenda.this, NovaVenda.class);
                it2.putExtra("EDITAR_VENDA",venda);
                startActivityForResult(it2,2);
                break;
            case R.id.menuItem_del:
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
                        finish();
                    }
                });
                dlgDel.show();
                break;
            case R.id.compartilhar:
                if (tipo_arquivo.contentEquals("Imagem")) {
                    compartilharImagem();
                } else {
                    compartilharPDF();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        vendRep = new VendasRepositorio(this);
        venda = vendRep.buscarVenda(venda.id);
        definirParametrosVenda();

        if (requestCode == 1011){
            File pdf = new File(uriPdf.getPath());
            pdf.delete();
        }
    }
}

package com.example.controlemk.Adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.example.controlemk.R;

import java.util.ArrayList;
import java.util.List;

public class VendaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private TextView txtTotVenda;

    private List<Produto> dados, selecionados;
    private Venda venda;
    private VendasRepositorio vendRep;
    private ViewHolderVenda holderVenda;

    private int id;
    private boolean eVenda;
    private Context context;

    public VendaAdapter(List<Produto> dados, boolean eVenda, List<Produto> selecionados, TextView txtTotVenda, Context context) {
        this.dados = dados;
        this.eVenda = eVenda;
        this.selecionados = selecionados;
        this.txtTotVenda = txtTotVenda;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if(dados.get(position).nome == null){
            return 1;
        }else{
            return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == 0) {
            View view = layoutInflater.inflate(R.layout.linha_venda, parent, false);
            ViewHolderVenda holderVenda = new ViewHolderVenda(view, parent.getContext());
            return holderVenda;
        } else {
            View view = layoutInflater.inflate(R.layout.linha_categ, parent, false);
            ViewHolderCateg holderCateg = new ViewHolderCateg(view);
            return holderCateg;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if ((dados != null) && (dados.size() > 0)) {
            Produto produto = dados.get(position);
//            formatCateg(holder, produto);
            Log.d("COMPRA", "onCreateViewHolder - " + produto.nome);
            switch(holder.getItemViewType()) {
                case 1:
                    ViewHolderCateg holderCateg = (ViewHolderCateg) holder;
                    holderCateg.txtCateg.setText(produto.categoria);
                    break;
                case 0:
                    ViewHolderVenda holderVenda = (ViewHolderVenda) holder;
                    holderVenda.txtCodigo.setText(String.format("%08d",produto.codigo));
                    holderVenda.txtNome.setText(produto.nome);
                    if (prodIndexNaLista(selecionados, produto) == -1) {
                        holderVenda.edtPreco.setText(String.format("%.2f",produto.preço));
                    } else {
                        holderVenda.edtPreco.setText(String.format("%.2f",selecionados.get(prodIndexNaLista(selecionados,produto)).preço));
                    }
                    if (produto.ultimaVenda(context) != null) {
                        holderVenda.txtUltVenda.setText(produto.ultimaVenda(context).dataVenda);
                    } else {
                        holderVenda.txtUltVenda.setText("---");
                    }
                    ArrayAdapter<Integer> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, quants(produto.quantNoEstoque));
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    holderVenda.spinnerQuant.setAdapter(adapter);
                    for (Produto prod : selecionados) {
                        if (prod.codigo == produto.codigo) {
                            holderVenda.select.setChecked(true);
                            holderVenda.lblValTotProd.setVisibility(View.VISIBLE);
                            holderVenda.edtPreco.setVisibility(View.VISIBLE);
                            holderVenda.txtSifrao.setVisibility(View.VISIBLE);
                            holderVenda.spinnerQuant.setVisibility(View.VISIBLE);
                            holderVenda.spinnerQuant.setSelection((int) prod.quantidade - 1);
                            holderVenda.txtValTotProd.setVisibility(View.VISIBLE);
                            holderVenda.lblValTotProd.setVisibility(View.VISIBLE);
                            float tot = prod.quantidade * prod.preço;
                            holderVenda.txtValTotProd.setText(String.format("R$%.2f", tot));
                            break;
                        } else {
                            holderVenda.select.setChecked(false);
                            holderVenda.lblValTotProd.setVisibility(View.INVISIBLE);
                            holderVenda.edtPreco.setVisibility(View.INVISIBLE);
                            holderVenda.spinnerQuant.setVisibility(View.INVISIBLE);
                            holderVenda.txtSifrao.setVisibility(View.INVISIBLE);
                            holderVenda.txtValTotProd.setVisibility(View.INVISIBLE);
                            holderVenda.lblQuant.setVisibility(View.INVISIBLE);
                        }
                    }
                    atualizarTotVenda();
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderVenda extends RecyclerView.ViewHolder {

        public TextView txtNome, txtCodigo, txtSifrao, txtUltVenda, lblUltVenda, lblQuant, txtValTotProd, lblValTotProd, lblCodigo, txtCateg, lblCateg;
        public EditText edtPreco;
        public CheckBox select;
        public Spinner spinnerQuant;
        public ConstraintLayout layoutGeralVenda;
        public LinearLayout linLayout;

        public ViewHolderVenda(@NonNull View itemView, final Context context) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNome);
            txtCodigo = itemView.findViewById(R.id.txtCodigo);
            lblCodigo = itemView.findViewById(R.id.lblCodigo);
            txtSifrao = itemView.findViewById(R.id.txtSifrao);
            edtPreco = itemView.findViewById(R.id.edtPreco);
            txtUltVenda = itemView.findViewById(R.id.txtUltVenda);
            lblUltVenda = itemView.findViewById(R.id.lblUltVenda);
            lblQuant = itemView.findViewById(R.id.lblQuant);
            txtValTotProd = itemView.findViewById(R.id.txtValTotProd);
            lblValTotProd = itemView.findViewById(R.id.lblValTotProd);
            select = (CheckBox) itemView.findViewById(R.id.select);
            spinnerQuant = (Spinner) itemView.findViewById(R.id.spinnerQuant);
            layoutGeralVenda = (ConstraintLayout) itemView.findViewById(R.id.layoutGeralVenda);
            linLayout = (LinearLayout) itemView.findViewById(R.id.linLayout);
            preçoInvisivel();
            select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dados.size() > 0) {
                        Produto produto = dados.get(getLayoutPosition());
                        if (prodIndexNaLista(selecionados, produto) == -1) {
                            produto.quantidade = Integer.parseInt(spinnerQuant.getSelectedItem().toString());
                            selecionados.add(produto);
                            if ("".contains(edtPreco.getText())) {
                                produto.preço = 0.0f;
                            }
                            select.setChecked(true);
                            preçoVisivel();
//                            abrirTeclado(edtPreco);

                        } else {
//                            produto = selecionados.get(prodIndexNaLista(selecionados, produto));
                            selecionados.remove(prodIndexNaLista(selecionados, produto));
                            select.setChecked(false);
                            preçoInvisivel();
                            fecharTeclado(edtPreco);
                        }
                        atualizarTotVenda();
                        float tot = (produto.quantidade * produto.preço);
                        txtValTotProd.setText(String.format("R$%.2f", tot));
                    }
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dados.size() > 0) {
                        Produto produto = dados.get(getLayoutPosition());
                        if (!"".contains(produto.nome)) {
                            if (prodIndexNaLista(selecionados, produto) == -1) {
                                produto.quantidade = Integer.parseInt(spinnerQuant.getSelectedItem().toString());
                                selecionados.add(produto);
                                if ("".contains(edtPreco.getText())) {
                                    produto.preço = 0.0f;
                                }
                                select.setChecked(true);
                                preçoVisivel();
//                                abrirTeclado(edtPreco);

                            } else {
//                                produto = selecionados.get(prodIndexNaLista(selecionados, produto));
                                selecionados.remove(prodIndexNaLista(selecionados, produto));
                                preçoInvisivel();
                                fecharTeclado(edtPreco);
                            }
                            atualizarTotVenda();
                            float tot = (produto.quantidade * produto.preço);
                            txtValTotProd.setText(String.format("R$%.2f", tot));
                        }
                    }
                }
            });
            edtPreco.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    Produto produto = dados.get(getLayoutPosition());
                    if (prodIndexNaLista(selecionados, produto) != -1 ) {
//                        produto = selecionados.get(prodIndexNaLista(selecionados, produto));
                        while (prodIndexNaLista(selecionados, produto) != -1) {
                            selecionados.remove(prodIndexNaLista(selecionados, produto));
                        }
                        if (!" 0.".contains(s.toString())) {
                            produto.preço = Float.parseFloat(s.toString().replace(",", "."));
                        } else {
                            produto.preço = 0.0f;
                            if (".".contains(s.toString()) && !"".contains(s.toString())) {
                                edtPreco.setText("0.");
                            } else {
                                edtPreco.setSelection(s.length());
                            }
                        }
                        selecionados.add(produto);
                        atualizarTotVenda();
                        if (produto.quantidade == 0) {
                            produto.quantidade = 1;
                        }
                        float tot = (produto.quantidade * produto.preço);
                        txtValTotProd.setText(String.format("R$%.2f", tot));
                    }
                }
            });



            spinnerQuant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (select.isChecked() && spinnerQuant.getVisibility() == View.VISIBLE) {
                        Produto produto = dados.get(getLayoutPosition());
//                        produto = selecionados.get(prodIndexNaLista(selecionados, produto));
                        while (prodIndexNaLista(selecionados, produto) != -1) {
                            selecionados.remove(prodIndexNaLista(selecionados, produto));
                        }
                        produto.quantidade = Integer.parseInt(spinnerQuant.getSelectedItem().toString());
                        selecionados.add(produto);
                        atualizarTotVenda();
                        float tot = (produto.quantidade * produto.preço);
                        txtValTotProd.setText(String.format("R$%.2f", tot));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        public void preçoVisivel() {
            select.setChecked(true);
            lblValTotProd.setVisibility(View.VISIBLE);
            edtPreco.setVisibility(View.VISIBLE);
            spinnerQuant.setVisibility(View.VISIBLE);
            txtSifrao.setVisibility(View.VISIBLE);
            txtValTotProd.setVisibility(View.VISIBLE);
            lblQuant.setVisibility(View.VISIBLE);
//            edtPreco.requestFocus();
        }

        public void preçoInvisivel() {
            select.setChecked(false);
            lblValTotProd.setVisibility(View.INVISIBLE);
            edtPreco.setVisibility(View.INVISIBLE);
            spinnerQuant.setVisibility(View.INVISIBLE);
            txtSifrao.setVisibility(View.INVISIBLE);
            txtValTotProd.setVisibility(View.INVISIBLE);
            lblValTotProd.setVisibility(View.INVISIBLE);
            lblQuant.setVisibility(View.INVISIBLE);

        }
    }

    public class ViewHolderCateg extends RecyclerView.ViewHolder{

        public TextView lblCateg, txtCateg;

        public ViewHolderCateg(@NonNull View itemView) {
            super(itemView);

            lblCateg = (TextView) itemView.findViewById(R.id.lblCateg2);
            txtCateg = (TextView) itemView.findViewById(R.id.txtCateg2);
        }
    }

    public List<Produto> produtosSelecionados() {
        return selecionados;
    }

    public int prodIndexNaLista(List<Produto> prods, Produto prod){
        for (Produto p : prods) {
            if (p.codigo == prod.codigo) {
                return prods.indexOf(p);
            }
        }
        return -1;
    }

    public void atualizarTotVenda() {
        double tot = 0.00f;
        for (Produto prod : selecionados) {
            if (prod.quantidade == 0) {
                prod.quantidade = 1;
            }
            tot += (prod.quantidade * prod.preço);
        }
        txtTotVenda.setText(String.format("R$%.2f", tot));
    }

    public List<Integer> quants(int quantNoEstoque) {
        List<Integer> numeros = new ArrayList<>();
        int numMax;
        if (eVenda) {
            numMax = quantNoEstoque;
        } else {
            numMax = 50;
        }
        for (int i = 1; i <= numMax; i++) {
            numeros.add(i);
        }
        return numeros;
    }

    public void abrirTeclado(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public void fecharTeclado(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
}

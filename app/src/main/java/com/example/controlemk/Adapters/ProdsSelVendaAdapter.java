package com.example.controlemk.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.example.controlemk.R;

import java.util.ArrayList;
import java.util.List;

public class ProdsSelVendaAdapter extends RecyclerView.Adapter<ProdsSelVendaAdapter.ViewHolderProduto> {

    private List<Produto> dados, listaCompleta;
    private VendasRepositorio vendRep;
    private Context context;

    public ProdsSelVendaAdapter(List<Produto> dados, Context context) {
        this.dados = dados;
        this.listaCompleta = new ArrayList<>(dados);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_prod_venda, parent, false);

        ViewHolderProduto holderProduto = new ViewHolderProduto(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(ViewHolderProduto holder, final int position) {

        vendRep = new VendasRepositorio(context);
        if ((dados != null) && (dados.size() > 0)){
            Produto produto = dados.get(position);
            Log.d("TAG","onBindViewHolder - " + produto.nome);
            holder.txtNomeProd.setText(produto.nome);
            holder.txtCodigo.setText(String.format("%08d",produto.codigo));
            holder.txtPreço.setText(String.format("R$%.2f",produto.preço));
            holder.txtQuant.setText(String.format("%02d",produto.quantidade));
            holder.txtValTot.setText(String.format("R$%.2f",produto.quantidade*produto.preço));
        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }



    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtNomeProd,txtCodigo, txtQuant, txtPreço, txtValTot;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtNomeProd = itemView.findViewById(R.id.txtNomeProd);
            txtCodigo = itemView.findViewById(R.id.txtCodigo2);
            txtQuant = itemView.findViewById(R.id.txtQuant);
            txtPreço = itemView.findViewById(R.id.txtPreco);
            txtValTot = itemView.findViewById(R.id.txtValTotProd2);
        }
    }
}

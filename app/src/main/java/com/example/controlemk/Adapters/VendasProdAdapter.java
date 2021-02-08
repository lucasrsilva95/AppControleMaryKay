package com.example.controlemk.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.DetalhesVenda;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.example.controlemk.R;

import java.util.List;

public class VendasProdAdapter extends RecyclerView.Adapter<VendasProdAdapter.ViewHolderProduto>{

    private List<Venda> dados;
    private VendasRepositorio vendRep;
    private Context context;

    private int codigoProd;

    public VendasProdAdapter(List<Venda> dados, int codigoProd, Context context) {
        this.dados = dados;
        this.context = context;
        this.codigoProd = codigoProd;
    }

    @NonNull
    @Override
    public VendasProdAdapter.ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_vendasprod, parent, false);

        VendasProdAdapter.ViewHolderProduto holderProduto = new VendasProdAdapter.ViewHolderProduto(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(VendasProdAdapter.ViewHolderProduto holder, final int position) {

        vendRep = new VendasRepositorio(context);
        if ((dados != null) && (dados.size() > 0)){
            Venda venda = dados.get(position);
            holder.txtNome.setText(venda.nome);
            holder.txtData.setText(venda.dataVenda);
            holder.txtValTot.setText(String.format("R$%.2f",venda.total));
            holder.txtPreço.setText(String.format("R$%.2f",venda.prodEmVenda(codigoProd).preço));
            holder.txtQuant.setText(String.format("%02d",venda.prodEmVenda(codigoProd).quantidade));
        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtNome, txtData, txtQuant, txtPreço, txtValTot;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNomeCliente);
            txtData = itemView.findViewById(R.id.txtData);
            txtQuant = itemView.findViewById(R.id.txtQuant);
            txtPreço = itemView.findViewById(R.id.txtPrecoUni);
            txtValTot = itemView.findViewById(R.id.txtValTotVenda);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (dados.size() > 0) {
                        Venda venda = dados.get(getLayoutPosition());
                        Intent it = new Intent(context, DetalhesVenda.class);
                        it.putExtra("VENDA", venda);
                        ((AppCompatActivity) context).startActivityForResult(it, 2);
                    }
                }
            });
        }
    }
}

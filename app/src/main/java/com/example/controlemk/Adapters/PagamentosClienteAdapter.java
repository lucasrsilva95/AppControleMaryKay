package com.example.controlemk.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.DetalhesVenda;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.example.controlemk.OperacoesDatas;
import com.example.controlemk.R;

import java.util.List;

public class PagamentosClienteAdapter extends RecyclerView.Adapter<PagamentosClienteAdapter.ViewHolderCliente>{

    private List<Venda> dados;
    private Context context;

    public PagamentosClienteAdapter(List<Venda> dados, Context context) {
        this.dados = dados;
        this.context = context;
    }

    @NonNull
    @Override
    public PagamentosClienteAdapter.ViewHolderCliente onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_pagamentos_cliente, parent, false);

        PagamentosClienteAdapter.ViewHolderCliente holderProduto = new PagamentosClienteAdapter.ViewHolderCliente(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(final PagamentosClienteAdapter.ViewHolderCliente holder, final int position) {

        VendasRepositorio vendRep = new VendasRepositorio(context);
        if ((dados != null) && (dados.size() > 0)) {
            final Venda venda = dados.get(position);
            holder.txtDataVenda.setText(venda.dataVenda);
            float valParc = Float.parseFloat(venda.datasPag.get(0).split("=")[1]);
            holder.txtValTotPagamento.setText(String.format("R$%.2f", valParc));
            holder.txtDataPagamento.setText(venda.datasPag.get(0).split("=")[0]);
            if (venda.datasNaoPagas.contains(venda.datasPag.get(0))) {
                holder.botaoPag.setChecked(false);
            } else {
                holder.botaoPag.setChecked(true);
            }
            OperacoesDatas op = new OperacoesDatas(context);
            int difDatas = op.subtracaoDatas(venda.datasPag.get(0), op.dataAtual());
            if (holder.botaoPag.isChecked()) {
                holder.txtStatus.setText("PAGO");
                holder.txtStatus.setTextColor(Color.GREEN);
            } else {
                if (difDatas >= 0) {
                    holder.txtStatus.setText("EM DIA");
                    holder.txtStatus.setTextColor(Color.BLACK);
                } else {
                    holder.txtStatus.setText("ATRASADO");
                    holder.txtStatus.setTextColor(Color.RED);
                }
            }
            holder.botaoPag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    VendasRepositorio vendRep = new VendasRepositorio(context);
                    venda.datasNaoPagas = vendRep.buscarVenda(venda.id).datasNaoPagas;
                    if (isChecked) {
                        venda.datasNaoPagas.remove(venda.datasPag.get(0));
                        holder.txtStatus.setText("PAGO");
                        holder.txtStatus.setTextColor(Color.GREEN);
                    } else {
                        venda.datasNaoPagas.add(venda.datasPag.get(0));
                        OperacoesDatas op = new OperacoesDatas(context);
                        int difDatas = op.subtracaoDatas(venda.datasPag.get(0), op.dataAtual());
                        if (difDatas >= 0) {
                            holder.txtStatus.setText("EM DIA");
                            holder.txtStatus.setTextColor(Color.BLACK);
                        } else {
                            holder.txtStatus.setText("ATRASADO");
                            holder.txtStatus.setTextColor(Color.RED);
                        }
                    }
                    List<Venda> vendas = vendRep.buscarTodos();
                    Venda v = vendRep.buscarVenda(venda.id);
                    v.datasNaoPagas = venda.datasNaoPagas;
                    vendRep.alterar(v);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderCliente extends RecyclerView.ViewHolder{

        public TextView txtDataVenda, txtDataPagamento, txtValTotPagamento, txtStatus;
        public ToggleButton botaoPag;

        public ViewHolderCliente(@NonNull View itemView, final Context context) {
            super(itemView);

            txtDataVenda = itemView.findViewById(R.id.txtDataVenda);
            txtDataPagamento = itemView.findViewById(R.id.txtDataPagamento);
            txtValTotPagamento = itemView.findViewById(R.id.txtValPagamento);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            botaoPag = itemView.findViewById(R.id.botaoPagar);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (dados.size() > 0) {
                        VendasRepositorio vendRep = new VendasRepositorio(context);
                        Venda venda = vendRep.buscarVenda(dados.get(getLayoutPosition()).id);
                        Intent it = new Intent(context, DetalhesVenda.class);
                        it.putExtra("VENDA", venda);
                        ((AppCompatActivity) context).startActivityForResult(it, 2);
                    }
                }
            });
        }
    }
}

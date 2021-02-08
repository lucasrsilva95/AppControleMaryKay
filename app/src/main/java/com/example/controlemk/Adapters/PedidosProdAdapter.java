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

import com.example.controlemk.DetalhesPedido;
import com.example.controlemk.Dominio.Entidades.Pedido;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Repositorio.PedidosRepositorio;
import com.example.controlemk.R;

import java.util.List;

public class PedidosProdAdapter extends RecyclerView.Adapter<PedidosProdAdapter.ViewHolderProduto>{

    private List<Pedido> dados;
    private Pedido pedido;
    private Produto produto;
    private int codigoProd;

    private PedidosRepositorio pedRep;
    private Context context;

    public PedidosProdAdapter(int codigoProd, List<Pedido> dados, Context context) {
        this.codigoProd = codigoProd;
        this.dados = dados;
        this.context = context;
    }

    @NonNull
    @Override
    public PedidosProdAdapter.ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_pedidos_prod, parent, false);

        PedidosProdAdapter.ViewHolderProduto holderProduto = new PedidosProdAdapter.ViewHolderProduto(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(ViewHolderProduto holder, final int position) {

        pedRep = new PedidosRepositorio(context);
        if ((dados != null) && (dados.size() > 0)){
            pedido = dados.get(position);
            produto = pedido.prodNoPedido(codigoProd);
            holder.txtData.setText(pedido.data);
            holder.txtPrecoBruto.setText(String.format("R$%.2f", produto.preço));
            holder.txtPrecoCusto.setText(String.format("R$%.2f", produto.preço*(1-pedido.desconto)));
            holder.txtDesconto.setText(((int)(pedido.desconto*100)) + "%");
            holder.txtValDesconto.setText(String.format("R$%.2f",pedido.desconto*produto.preço));
            holder.txtQuant.setText(String.format("%d",produto.quantidade));
        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtData, txtQuant, txtPrecoBruto,txtDesconto, txtPrecoCusto, txtValDesconto;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtData = itemView.findViewById(R.id.txtData);
            txtQuant = itemView.findViewById(R.id.txtQuant);
            txtPrecoBruto = itemView.findViewById(R.id.txtValBruto);
            txtPrecoCusto = itemView.findViewById(R.id.txtCusto);
            txtDesconto = itemView.findViewById(R.id.txtDesconto);
            txtValDesconto = itemView.findViewById(R.id.txtValDesconto);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (dados.size() > 0) {
                        Pedido pedido = dados.get(getLayoutPosition());
                        Intent it = new Intent(context, DetalhesPedido.class);
                        it.putExtra("PEDIDO", pedido);
                        ((AppCompatActivity) context).startActivityForResult(it, 2);
                    }
                }
            });
        }
    }
}

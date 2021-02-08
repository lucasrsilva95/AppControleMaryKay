package com.example.controlemk.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.DetalhesProduto;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.example.controlemk.R;

import java.util.List;

public class ProdEstoqueAdapter extends RecyclerView.Adapter<ProdEstoqueAdapter.ViewHolderProduto> {

    private List<Produto> dados;
    private VendasRepositorio vendRep;
    private Context context;

    public ProdEstoqueAdapter(List<Produto> dados, Context context) {
        this.dados = dados;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG", "onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.linha_prod_estoque, parent, false);

        ViewHolderProduto holderProduto = new ViewHolderProduto(view, parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(ViewHolderProduto holder, final int position) {

        vendRep = new VendasRepositorio(context);
        if ((dados != null) && (dados.size() > 0)) {
            Produto produto = dados.get(position);
            holder.txtNome.setText(produto.nome);
            holder.txtQuant.setText(String.format("%02d",produto.quantNoEstoque));
            if (produto.ultimoPedido(context) != null) {
                holder.txtUltPedido.setText(produto.ultimoPedido(context).data);
            } else {
                holder.txtUltPedido.setText("-----");
            }

        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder {

        public TextView txtNome, txtQuant, txtUltPedido;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNome);
            txtQuant = itemView.findViewById(R.id.txtQuant);
            txtUltPedido = itemView.findViewById(R.id.txtUltPedido);



            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (dados.size() > 0) {
                        Produto produto = dados.get(getLayoutPosition());
                        if (!"".contains(produto.nome)) {
                            Intent it = new Intent(context, DetalhesProduto.class);
                            it.putExtra("PRODUTO", produto);
                            ((AppCompatActivity) context).startActivityForResult(it, 2);
                        }
                    }
                }
            });
        }
    }

//    public void formatCateg(ProdEstoqueAdapter.ViewHolderProduto holder, Produto prod) {
//        if ("".contains(prod.nome)) {
//            holder.layout2.setVisibility(View.GONE);
//            holder.lblCateg.setVisibility(View.VISIBLE);
//            holder.txtCateg.setVisibility(View.VISIBLE);
//            holder.txtCateg.setText(prod.categoria);
////            holder.layout1.setBackgroundColor(Color.rgb(202, 202, 216));
//            holder.layout1.setBackgroundColor(ContextCompat.getColor(context, R.color.corCategoriaEstoque));
//        } else {
//            holder.layout2.setVisibility(View.VISIBLE);
//            holder.lblCateg.setVisibility(View.GONE);
//            holder.txtCateg.setVisibility(View.GONE);
//            holder.layout1.setBackgroundColor(Color.WHITE);
//        }
//    }
}

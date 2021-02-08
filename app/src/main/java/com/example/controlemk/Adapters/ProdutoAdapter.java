package com.example.controlemk.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.ContextMenu;
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
import com.example.controlemk.Dominio.Entidades.Cliente;
import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.example.controlemk.R;

import java.util.List;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ViewHolderProduto> {

    private List<Produto> dados;
    private VendasRepositorio vendRep;
    public static Produto prodContextMenu;

    private Context context;
    public static final int ITEM_EDITAR = 1, ITEM_DELETAR = 2;

    public ProdutoAdapter(List<Produto> dados, Context context) {
        this.dados = dados;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_produto, parent, false);

        ViewHolderProduto holderProduto = new ViewHolderProduto(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(ViewHolderProduto holder, final int position) {

        vendRep = new VendasRepositorio(context);
        if ((dados != null) && (dados.size() > 0)){
            Produto produto = dados.get(position);
            Log.d("TAG","onBindViewHolder - " + produto.nome);
            if (produto.nome != null) {
                holder.txtNome.setText(produto.nome);
                holder.txtCodigo.setText(String.format("%08d",produto.codigo));
                holder.txtPreço.setText(String.format("%.2f",produto.preço));
                holder.txtQtdVend.setText(String.format("%02d",produto.unidadesVendidas(context)));
                if (produto.ultimaVenda(context) != null) {
                    holder.txtUltVenda.setText(produto.ultimaVenda(context).dataVenda);
                } else {
                    holder.txtUltVenda.setText("---");
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtNome, txtCodigo, txtPreço, txtUltVenda, lblCateg, txtCateg, txtQtdVend;
        public ConstraintLayout layout2;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNomeProd);
            txtCodigo = itemView.findViewById(R.id.txtCodigo);
            txtPreço = itemView.findViewById(R.id.txtPreco);
            txtUltVenda = itemView.findViewById(R.id.txtUltVenda);
            txtQtdVend = itemView.findViewById(R.id.txtQtdVendida);

            layout2 = itemView.findViewById(R.id.layout2);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (dados.size() > 0) {
                        Produto produto = dados.get(getLayoutPosition());
                        if (produto.nome != null) {
                            Intent it = new Intent(context, DetalhesProduto.class);
                            it.putExtra("PRODUTO", produto);
                            ((AppCompatActivity) context).startActivityForResult(it, 2);
                        }
                    }
                }
            });
            itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                    prodContextMenu = dados.get(getLayoutPosition());
                    menu.add(getLayoutPosition(),ITEM_EDITAR,0,"Editar");
                    menu.add(getLayoutPosition(),ITEM_DELETAR,1,"Deletar");
                }
            });

        }
    }


    public Produto getContextMenuProduto(){
        return prodContextMenu;
    }
}

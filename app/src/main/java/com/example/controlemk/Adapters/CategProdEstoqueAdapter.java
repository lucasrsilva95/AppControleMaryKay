package com.example.controlemk.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.R;

import java.util.ArrayList;
import java.util.List;

public class CategProdEstoqueAdapter extends RecyclerView.Adapter<CategProdEstoqueAdapter.ViewHolderProduto> {

    private List<String> categorias, categAbertas;
    private List<Produto> produtos, prodsCateg;
    private ProdutosRepositorio prodRep;
    private Context context;

    public CategProdEstoqueAdapter(List<Produto> produtos, Context context) {
        prodRep = new ProdutosRepositorio(context);
        this.categorias = prodRep.listaDeCategorias(produtos);
        this.produtos = produtos;
        this.categAbertas = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_categ_prod, parent, false);

        ViewHolderProduto holderProduto = new ViewHolderProduto(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(ViewHolderProduto holder, final int position) {

        prodRep = new ProdutosRepositorio(context);
        if ((categorias != null) && (categorias.size() > 0)){
            String categ = categorias.get(position);
            if (categ != null) {
                holder.txtCateg.setText(categ);
                if (categAbertas.contains(categ)) {
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    holder.lstProds.setLayoutManager(linearLayoutManager);
                    prodsCateg = prodRep.prodsDaCategoria(categ, produtos);
                    ProdEstoqueAdapter prodAdapter = new ProdEstoqueAdapter(prodsCateg, context);
                    holder.lstProds.setAdapter(prodAdapter);
                    holder.lstProds.setVisibility(View.VISIBLE);
                } else {
                    holder.lstProds.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtCateg;
        public RecyclerView lstProds;
        public ConstraintLayout layoutCateg;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtCateg = itemView.findViewById(R.id.txtCateg2);
            layoutCateg = itemView.findViewById(R.id.layoutCateg);
            lstProds = itemView.findViewById(R.id.lstProdsCateg);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (categorias.size() > 0) {
                        String categ = categorias.get(getLayoutPosition());
                        if (!categAbertas.contains(categ)) {
                            categAbertas.add(categ);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                            lstProds.setLayoutManager(linearLayoutManager);
                            prodsCateg = prodRep.prodsDaCategoria(categ, produtos);
                            ProdEstoqueAdapter prodAdapter = new ProdEstoqueAdapter(prodsCateg, context);
                            lstProds.setAdapter(prodAdapter);
                            lstProds.setVisibility(View.VISIBLE);
                        } else {
                            categAbertas.remove(categ);
                            lstProds.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }

    public List<String> categoriasAbertas(){
        return categAbertas;
    }
}

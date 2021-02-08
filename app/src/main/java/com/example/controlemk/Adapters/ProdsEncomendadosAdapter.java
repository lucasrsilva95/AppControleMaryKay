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

public class ProdsEncomendadosAdapter extends RecyclerView.Adapter<ProdsEncomendadosAdapter.ViewHolderProduto>{

    private List<Venda> dados;
    private Venda venda;

    private VendasRepositorio vendRep;
    private Context context;

    public ProdsEncomendadosAdapter(List<Venda> dados, Context context) {
        this.dados = dados;
        this.context = context;
    }

    @NonNull
    @Override
    public ProdsEncomendadosAdapter.ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_prods_encomendados, parent, false);

        ProdsEncomendadosAdapter.ViewHolderProduto holderProduto = new ProdsEncomendadosAdapter.ViewHolderProduto(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(ProdsEncomendadosAdapter.ViewHolderProduto holder, final int position) {

        vendRep = new VendasRepositorio(context);
        if ((dados != null) && (dados.size() > 0)){
            venda = dados.get(position);
            holder.txtNomeProd.setText(venda.produtos.get(0).nome);
            holder.txtCodigo.setText(String.format("%08d",venda.produtos.get(0).codigo));
            holder.txtData.setText(venda.dataVenda);
            int quant = venda.produtos.get(0).quantidade;
            float preco = venda.produtos.get(0).preço;
            holder.txtQuant.setText(String.format("%02d",quant));
            holder.txtPreco.setText(String.format("R$%.2f",preco));
            holder.txtValTot.setText(String.format("R$%.2f",quant * preco));
        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtNomeProd, txtCodigo, txtData, txtQuant, txtPreco, txtValTot;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtNomeProd = itemView.findViewById(R.id.txtNomeProd);
            txtCodigo = itemView.findViewById(R.id.txtCodigo2);
            txtData = itemView.findViewById(R.id.txtDataEncomenda);
            txtQuant = itemView.findViewById(R.id.txtQuant);
            txtPreco = itemView.findViewById(R.id.txtPreco);
            txtValTot = itemView.findViewById(R.id.txtValTotProd2);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (dados.size() > 0) {
                        Venda venda = dados.get(getLayoutPosition());
                        Intent it = new Intent(context, DetalhesVenda.class);
                        it.putExtra("VENDA", vendRep.buscarVenda(venda.id));
                        ((AppCompatActivity) context).startActivityForResult(it, 2);
                    }
                }
            });
        }
    }
}

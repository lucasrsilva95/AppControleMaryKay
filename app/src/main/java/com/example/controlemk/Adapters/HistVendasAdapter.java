package com.example.controlemk.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
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

public class HistVendasAdapter extends RecyclerView.Adapter<HistVendasAdapter.ViewHolderProduto>{

    private List<Venda> dados;
    private Venda venda;
    public static Venda vendaContextMenu;

    private VendasRepositorio vendRep;
    private Context context;

    public static final int ITEM_EDITAR = 1, ITEM_DELETAR = 2;

    public HistVendasAdapter(List<Venda> dados, Context context) {
        this.dados = dados;
        this.context = context;
    }

    @NonNull
    @Override
    public HistVendasAdapter.ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_hist_venda, parent, false);

        HistVendasAdapter.ViewHolderProduto holderProduto = new HistVendasAdapter.ViewHolderProduto(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(HistVendasAdapter.ViewHolderProduto holder, final int position) {

        vendRep = new VendasRepositorio(context);
        if ((dados != null) && (dados.size() > 0)){
            venda = dados.get(position);
            if(!venda.efetivada){
                holder.lblData.setText("Data da Encomenda:");
                holder.lblValTotVenda.setText("Total da Encomenda:");
            }
            holder.txtNome.setText(venda.nome);
            holder.txtData.setText(venda.dataVenda);
            holder.txtValTot.setText(String.format("R$%.2f",venda.total));
            holder.txtQuant.setText(String.format("%02d",venda.produtos.size()));
        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtNome, txtData, txtQuant, txtValTot, lblData, lblValTotVenda;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNomeCliente);
            txtData = itemView.findViewById(R.id.txtData);
            txtQuant = itemView.findViewById(R.id.txtQuant);
            txtValTot = itemView.findViewById(R.id.txtValTotVenda);
            lblData = itemView.findViewById(R.id.lblData);
            lblValTotVenda = itemView.findViewById(R.id.lblTotVenda);


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

            itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                    vendaContextMenu = dados.get(getLayoutPosition());
                    menu.add(getLayoutPosition(),ITEM_EDITAR,0,"Editar");
                    menu.add(getLayoutPosition(),ITEM_DELETAR,1,"Deletar");
                }
            });

        }
    }

    public Venda getContextMenuVenda(){
        return vendaContextMenu;
    }
}

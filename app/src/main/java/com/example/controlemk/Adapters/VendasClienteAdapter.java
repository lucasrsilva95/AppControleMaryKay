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

public class VendasClienteAdapter extends RecyclerView.Adapter<VendasClienteAdapter.ViewHolderCliente>{

    private List<Venda> dados;
    private VendasRepositorio vendRep;
    private Context context;

    public VendasClienteAdapter(List<Venda> dados, Context context) {
        this.dados = dados;
        this.context = context;
    }

    @NonNull
    @Override
    public VendasClienteAdapter.ViewHolderCliente onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_vendas_cliente, parent, false);

        VendasClienteAdapter.ViewHolderCliente holderProduto = new VendasClienteAdapter.ViewHolderCliente(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(VendasClienteAdapter.ViewHolderCliente holder, final int position) {

        vendRep = new VendasRepositorio(context);
        if ((dados != null) && (dados.size() > 0)){
            Venda venda = dados.get(position);
            holder.txtData.setText(venda.dataVenda);
            holder.txtValTot.setText(String.format("R$%.2f",venda.total));
            holder.txtQuant.setText(String.format("%02d",venda.produtos.size()));
            if (venda.datasPag.size() == 0) {
                holder.txtFormaParc.setText("Á Vista");
            }else{
                holder.txtFormaParc.setText(String.format("Parcelado em %dx",venda.datasPag.size()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderCliente extends RecyclerView.ViewHolder{

        public TextView txtData, txtQuant, txtValTot, txtFormaParc;

        public ViewHolderCliente(@NonNull View itemView, final Context context) {
            super(itemView);

            txtData = itemView.findViewById(R.id.txtData);
            txtQuant = itemView.findViewById(R.id.txtQuant);
            txtValTot = itemView.findViewById(R.id.txtValTotVenda);
            txtFormaParc = itemView.findViewById(R.id.txtFormaParc);


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

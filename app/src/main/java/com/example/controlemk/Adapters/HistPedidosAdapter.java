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

import com.example.controlemk.DetalhesPedido;
import com.example.controlemk.DetalhesVenda;
import com.example.controlemk.Dominio.Entidades.Pedido;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.PedidosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.example.controlemk.R;

import java.util.List;

public class HistPedidosAdapter extends RecyclerView.Adapter<HistPedidosAdapter.ViewHolderProduto>{

    private List<Pedido> dados;
    private Pedido pedido;
    public Pedido pedidoContextMenu;

    private PedidosRepositorio pedRep;
    private Context context;
    public static final int ITEM_EDITAR = 1, ITEM_DELETAR = 2;

    public HistPedidosAdapter(List<Pedido> dados, Context context) {
        this.dados = dados;
        this.context = context;
    }

    @NonNull
    @Override
    public HistPedidosAdapter.ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_pedidos, parent, false);

        HistPedidosAdapter.ViewHolderProduto holderProduto = new HistPedidosAdapter.ViewHolderProduto(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(ViewHolderProduto holder, final int position) {

        pedRep = new PedidosRepositorio(context);
        if ((dados != null) && (dados.size() > 0)){
            pedido = dados.get(position);
            holder.txtData.setText(pedido.data);
            holder.txtPrecoBruto.setText(String.format("R$%.2f",pedido.totalBruto));
            holder.txtPrecoCusto.setText(String.format("R$%.2f",pedido.totalCusto));
            holder.txtDesconto.setText(((int)(pedido.desconto*100)) + "%");
            holder.txtValDesconto.setText(String.format("R$%.2f",pedido.desconto*pedido.totalBruto));
            holder.txtQuant.setText(String.format("%d",pedido.quantProdPedido()));
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

            itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                    pedidoContextMenu = dados.get(getLayoutPosition());
                    menu.add(getLayoutPosition(),ITEM_EDITAR,0,"Editar");
                    menu.add(getLayoutPosition(),ITEM_DELETAR,1,"Deletar");
                }
            });
        }
    }

    public Pedido getPedidoContextMenu(){
        return pedidoContextMenu;
    }
}

package com.example.controlemk.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.DetalhesCliente;
import com.example.controlemk.Dominio.Entidades.Cliente;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.OperacoesDatas;
import com.example.controlemk.R;

import java.util.List;

public class ListaClientesAdapter extends RecyclerView.Adapter<ListaClientesAdapter.ViewHolderProduto>{

    private List<Cliente> dados;
    private Cliente cliente;
    public static Cliente clienteContextMenu;

    private ClientesRepositorio cliRep;
    private Context context;
    public static final int ITEM_EDITAR = 1, ITEM_DELETAR = 2;

    public ListaClientesAdapter(List<Cliente> dados, Context context) {
        this.dados = dados;
        this.context = context;
    }

    @NonNull
    @Override
    public ListaClientesAdapter.ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_lista_clientes, parent, false);

        ListaClientesAdapter.ViewHolderProduto holderCliente = new ListaClientesAdapter.ViewHolderProduto(view,parent.getContext());

        return holderCliente;
    }

    @Override
    public void onBindViewHolder(ListaClientesAdapter.ViewHolderProduto holder, final int position) {

        cliRep = new ClientesRepositorio(context);
        if ((dados != null) && (dados.size() > 0)){
            cliente = dados.get(position);
            holder.txtNome.setText(cliente.nome);
            holder.txtTelefone.setText(cliente.telefone);
            if (cliente.ultimaVendaCliente(context) != null) {
                holder.txtUltCompra.setText(cliente.ultimaVendaCliente(context).dataVenda);
            } else {
                holder.txtUltCompra.setText("---");
            }
            if (cliente.datasPagamentosCliente(context).size() > 0) {
                String data = cliente.proxPagamentoCliente(context);
                holder.txtProxPag.setText(data);
                if (!data.contains("--")) {
                    OperacoesDatas op = new OperacoesDatas(context);
                    if(!op.dataEmDia(data)){
                        holder.txtProxPag.setTextColor(Color.RED);
                    }
                }
            }else{
                holder.txtProxPag.setText("---");
            }
            holder.txtValDebito.setText(String.format("R$%.2f",cliente.valDebitosCliente(context)));

        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtNome, txtTelefone, txtUltCompra, txtProxPag, txtValDebito;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNomeCliente);
            txtTelefone = itemView.findViewById(R.id.txtTelefone);
            txtUltCompra = itemView.findViewById(R.id.txtUltCompra);
            txtProxPag = itemView.findViewById(R.id.txtProxPag);
            txtValDebito = itemView.findViewById(R.id.txtValDebito);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (dados.size() > 0) {
                        Cliente cliente = dados.get(getLayoutPosition());
                        Intent it = new Intent(context, DetalhesCliente.class);
                        it.putExtra("CLIENTE", cliente);
                        ((AppCompatActivity) context).startActivityForResult(it, 2);
                    }
                }
            });

            itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                    clienteContextMenu = dados.get(getLayoutPosition());
                    menu.add(getLayoutPosition(),ITEM_EDITAR,0,"Editar");
                    menu.add(getLayoutPosition(),ITEM_DELETAR,1,"Deletar");
                }
            });

        }
    }


    public Cliente getContextMenuCliente(){
        return clienteContextMenu;
    }
}

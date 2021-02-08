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

public class ParcelasVendaAdapter extends RecyclerView.Adapter<ParcelasVendaAdapter.ViewHolderParcela>{

    private List<String> dados;
    private Venda venda;
    private Context context;

    public ParcelasVendaAdapter(List<String> dados, Venda venda, Context context) {
        this.dados = dados;
        this.venda = venda;
        this.context = context;
    }

    @NonNull
    @Override
    public ParcelasVendaAdapter.ViewHolderParcela onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_parcelas_venda, parent, false);

        ParcelasVendaAdapter.ViewHolderParcela holderParcela = new ParcelasVendaAdapter.ViewHolderParcela(view,parent.getContext());

        return holderParcela;
    }

    @Override
    public void onBindViewHolder(final ParcelasVendaAdapter.ViewHolderParcela holder, final int position) {

        VendasRepositorio vendRep = new VendasRepositorio(context);
        if ((dados != null) && (dados.size() > 0)) {
            final String parcela = dados.get(position);
            holder.lblParcela.setText(String.format("%dª Parcela", position+1));
            final String[] parc = parcela.split("=");
            holder.txtDataParcela.setText(parc[0]);
            holder.txtValParc.setText(String.format("R$%.2f",Float.parseFloat(parc[1].replace(",","."))));
            if (venda.datasNaoPagas.contains(parcela)) {
                holder.botaoPag.setChecked(false);
            } else {
                holder.botaoPag.setChecked(true);
            }
            OperacoesDatas op = new OperacoesDatas(context);
            int difDatas = op.subtracaoDatas(parc[0], op.dataAtual());
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
                    if (isChecked) {
                        venda.datasNaoPagas.remove(parcela);
                        holder.txtStatus.setText("PAGO");
                        holder.txtStatus.setTextColor(Color.GREEN);
                    } else {
                        venda.datasNaoPagas.add(parcela);
                        OperacoesDatas op = new OperacoesDatas(context);
                        int difDatas = op.subtracaoDatas(parc[0], op.dataAtual());
                        if (difDatas >= 0) {
                            holder.txtStatus.setText("EM DIA");
                            holder.txtStatus.setTextColor(Color.BLACK);
                        } else {
                            holder.txtStatus.setText("ATRASADO");
                            holder.txtStatus.setTextColor(Color.RED);
                        }
                    }
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

    public class ViewHolderParcela extends RecyclerView.ViewHolder{

        public TextView lblParcela, txtDataParcela, txtValParc, txtStatus;
        public ToggleButton botaoPag;

        public ViewHolderParcela(@NonNull View itemView, final Context context) {
            super(itemView);

            lblParcela = itemView.findViewById(R.id.lblParcela);
            txtDataParcela = itemView.findViewById(R.id.txtDataParcela);
            txtValParc = itemView.findViewById(R.id.txtValParc);
            txtStatus = itemView.findViewById(R.id.txtStatusParc);
            botaoPag = itemView.findViewById(R.id.botaoPagarParc);
        }
    }
}

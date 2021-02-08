package com.example.controlemk.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.Dominio.Entidades.Produto;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.example.controlemk.OperacoesDatas;
import com.example.controlemk.R;

import java.util.List;

public class MesVendaAdapter extends RecyclerView.Adapter<MesVendaAdapter.ViewHolderProduto> {

    private List<String> meses,mesesAbertos;
    private List<Venda> vendas;
    private List<Venda> vendasMes;
    private OperacoesDatas opDatas;
    private Context context;

    public HistVendasAdapter vendaAdapter;

    private boolean eVenda;

    public MesVendaAdapter(List<Venda> vendas, List<String> mesesAbertos, Context context) {
        opDatas = new OperacoesDatas(context);
        this.meses = opDatas.mesesDatas(vendas);
        this.mesesAbertos = mesesAbertos;
        this.vendas = vendas;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderProduto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_mes_venda, parent, false);

        ViewHolderProduto holderProduto = new ViewHolderProduto(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(ViewHolderProduto holder, final int position) {

        if ((meses != null) && (meses.size() > 0)){
            String mes = meses.get(position);
            VendasRepositorio vendRep = new VendasRepositorio(context);
            if (mes != null) {
                holder.txtMes.setText(mes.split("-")[1]);
                vendasMes = opDatas.vendasDoMes(mes, vendas);
                holder.txtSomaVendas.setText(String.format("R$%.2f",vendRep.somaTotalVendas(vendasMes)));
                if (mesesAbertos.contains(mes)) {
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    holder.lstVendas.setLayoutManager(linearLayoutManager);
                    vendaAdapter = new HistVendasAdapter(vendasMes,context);
                    holder.lstVendas.setAdapter(vendaAdapter);
                    holder.lstVendas.setVisibility(View.VISIBLE);
                } else {
                    holder.lstVendas.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return meses.size();
    }

    public class ViewHolderProduto extends RecyclerView.ViewHolder{

        public TextView txtMes,txtSomaVendas;
        public RecyclerView lstVendas;
        public ConstraintLayout layoutMes;

        public ViewHolderProduto(@NonNull View itemView, final Context context) {
            super(itemView);

            txtMes = itemView.findViewById(R.id.txtMes);
            txtSomaVendas = itemView.findViewById(R.id.txtSomaVendas);
            lstVendas = itemView.findViewById(R.id.lstVendas);
            layoutMes = itemView.findViewById(R.id.layoutMes);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (meses.size() > 0) {
                        String mes = meses.get(getLayoutPosition());
                        if (!mesesAbertos.contains(mes)) {
                            mesesAbertos.add(mes);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                            lstVendas.setLayoutManager(linearLayoutManager);
                            vendasMes = opDatas.vendasDoMes(mes, vendas);
                            HistVendasAdapter vendaAdapter = new HistVendasAdapter(vendasMes,context);
                            lstVendas.setAdapter(vendaAdapter);
                            lstVendas.setVisibility(View.VISIBLE);
                        } else {
                            mesesAbertos.remove(mes);
                            lstVendas.setVisibility(View.GONE);
                        }
                        salvarMesesAbertos();
                    }
                }
            });
        }
    }

    public List<String> mesesAbertos(){
        return mesesAbertos;
    }

    public void salvarMesesAbertos(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String stringMeses = "";
        for(String mes:mesesAbertos){
            stringMeses += mes + ";";
        }
        prefs.edit().putString("MESES_ABERTOS",stringMeses).apply();
    }
}

package com.example.controlemk.Dominio.Entidades;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.example.controlemk.OperacoesDatas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.IllegalFormatException;
import java.util.List;

public class Venda implements Serializable {

    public int id;
    public String dataVenda;
    public String nome;
    public float total;
    public List<Produto> produtos;
    public List<String> datasPag;
    public List<String> datasNaoPagas;
    public boolean efetivada;

    public Venda() {
        this.produtos = new ArrayList<Produto>();
        this.datasPag = new ArrayList<String>();
        this.datasNaoPagas = new ArrayList<String>();
        this.efetivada = true;
    }

    public float calcValTotVenda() {
        float tot = 0.0f;
        for (Produto p : produtos) {
            tot += p.quantidade * p.preço;
        }
        return tot;
    }

    public Produto prodEmVenda(int codigo) {
        List<Produto> prods = produtos;
        for (Produto p : prods) {
            if (p.codigo == codigo) {
                return p;
            }
        }
        return null;
    }


    public String convertProdListParaString(List<Produto> prods){
        String result = "";
        if (prods.size() > 0){
            for(Produto prod:prods){
                result = result.concat(String.format("%d;&&;%08d;&&;%s;&&;%d;&&;%s;&&;%.2f;&&;%s-&&-",prod.id,prod.codigo,prod.nome,prod.quantidade,prod.categoria,prod.preço,prod.detalhes));
            }
            return result;
        }
        return null;
    }

    public List<Produto> convertStringParaProdList(String s) {
        List<Produto> produtos = new ArrayList<>();
        if (!"".contentEquals(s)) {
            s = s.replace(",", ".");
            String[] p = s.split("-&&-");
            for (int i = 0; i < p.length; i++) {
                Produto prod = new Produto();
                String[] cat = p[i].split(";&&;");
                int pos = 0;
                try {
                    prod.id = Integer.parseInt(cat[pos]);
                    prod.codigo = Integer.parseInt(cat[pos + 1]);
                } catch (NumberFormatException ex) {
                    pos = -1;
                    prod.id = 0;
                }
                prod.codigo = Integer.parseInt(cat[pos + 1]);
                prod.nome = cat[pos + 2];
                prod.quantidade = Integer.parseInt(cat[pos + 3]);
                prod.categoria = cat[pos + 4];
                if (cat.length > 5) {
                    prod.preço = Float.parseFloat(cat[pos + 5]);
                } else {
                    prod.preço = 0.0f;
                }
                if (cat.length == pos + 7) {
                    prod.detalhes = cat[pos + 6];
                }
                produtos.add(prod);
            }
        }
        return produtos;
    }

    public String convertDatasPagParaString(List<String> datas) {
        String result = "";
        if (datas.size() > 0) {
            for (String data : datas) {
                result = result.concat(data + "-");
            }
            return result;
        }
        return null;
    }

    public List<String> convertStringParaDatasPag(String datas) {
        List<String> result = new ArrayList<>();
        if (datas != null) {
            String[] datasPag = datas.split("-");
            for (String d : datasPag) {
                result.add(d);
            }
        }
        return result;
    }

    public List<String> datasPagFuturas(Context context) {
        OperacoesDatas opDatas = new OperacoesDatas(context);
        List<String> datasFuturas = new ArrayList<>();
        for (String data : datasPag) {
            if (opDatas.subtracaoDatas(data.split("=")[0], opDatas.dataAtual()) > 0) {
                datasFuturas.add(data);
            }
        }
        return datasFuturas;
    }

    public void criarEventosNaAgenda(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
//            String dataParc = data.split("=")[0];
//            float valor = Float.parseFloat(data.split("=")[1]);
//            Intent calendarIntent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);
//            Calendar beginTime = Calendar.getInstance();
//            beginTime.set(opDatas.getYear(dataParc), opDatas.getMonth(dataParc), opDatas.getDay(dataParc), 8, 30);
//            Calendar endTime = Calendar.getInstance();
//            endTime.set(opDatas.getYear(dataParc), opDatas.getMonth(dataParc), opDatas.getDay(dataParc), 18, 00);
//            calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis());
//            calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
//            calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis());
//            calendarIntent.putExtra(CalendarContract.Events.TITLE, String.format("Receber parcela da/o %s - (R$%.2f)", venda.nome, valor));
////            calendarIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, "Secret dojo");
//            startActivity(calendarIntent);
        VendasRepositorio vendRep = new VendasRepositorio(context);
        OperacoesDatas opDatas = new OperacoesDatas(context);
        ContentResolver cr = context.getContentResolver();
        List<String> novasDatasNaoPag = new ArrayList<>();
        for (String data : datasNaoPagas) {
            String dataParc = data.split("=")[0].trim();
            float valor = Float.parseFloat(data.split("=")[1].trim());
            long idEvento = 0;
            if (data.split("=").length > 2) {
                idEvento = Long.parseLong(data.split("=")[2].trim());
//                deletarEventoCalendario(idEvento);
            }
            Calendar beginTime1 = Calendar.getInstance();
            beginTime1.set(opDatas.getYear(dataParc), opDatas.getMonth(dataParc), opDatas.getDay(dataParc), 0, 0);

//            CalendarContract.Events
            ContentValues cv = new ContentValues();
            cv.put("calendar_id", "1");
            cv.put(CalendarContract.Events.TITLE, String.format("Receber parcela - %s - (R$%.2f)", nome, valor));
            String descricao = String.format(
                    "Cliente: %s \n" +
                            "Valor da Venda: R$%.2f \n\n" +
                            "Parcelas: ", nome, total);
            for (int i = 0; i < datasPag.size(); i++) {
                String dataParcela = datasPag.get(i).split("=")[0];
                float valorParcela = Float.parseFloat(datasPag.get(i).split("=")[1]);

                descricao = descricao.concat(String.format("\n   %dª Parcela:     %s  -  R$%.2f", i + 1, dataParcela, valorParcela));
            }
            cv.put(CalendarContract.Events.DESCRIPTION, descricao);


//                TimeZone timeZone = TimeZone.getDefault();
            cv.put(CalendarContract.Events.DTSTART, beginTime1.getTimeInMillis());
//        Calendar endTime = Calendar.getInstance();
//        endTime.set(opDatas.getYear(dataParc), opDatas.getMonth(dataParc), opDatas.getDay(dataParc), 0, 0);

//                cv.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
            cv.put(CalendarContract.Events.DURATION, "P1D");
            cv.put(CalendarContract.Events.ALL_DAY, 1);
            cv.put(CalendarContract.Events.EVENT_TIMEZONE, Time.TIMEZONE_UTC);

            if(idEvento != 0) {
                deletarEventoCalendario(idEvento, context);
            }
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, cv);
            idEvento = Long.parseLong(uri.getLastPathSegment());
            data = String.format("%s = %.2f = %s",dataParc,valor, Long.toString(idEvento));

            novasDatasNaoPag.add(data);
        }
        datasNaoPagas.clear();
        datasNaoPagas.addAll(novasDatasNaoPag);
        vendRep.alterar(this);


//        Calendar beginTime2 = Calendar.getInstance();
//        beginTime2.set(opDatas.getYear(dataParc), opDatas.getMonth(dataParc), opDatas.getDay(dataParc)+2, 0, 0);
//
//        ContentValues cv2 = new ContentValues();
//        cv2.put(CalendarContract.Events.TITLE, "Teste 2" );
//        cv2.put(CalendarContract.Events.DESCRIPTION, "Descricao Teste 2" );
//
////                TimeZone timeZone = TimeZone.getDefault();
//        cv2.put(CalendarContract.Events.DTSTART, beginTime2.getTimeInMillis());
////        Calendar endTime2 = Calendar.getInstance();
////        endTime.set(opDatas.getYear(dataParc), opDatas.getMonth(dataParc), opDatas.getDay(dataParc)+2, 0, 0);
//
////                cv2.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
//        cv2.put(CalendarContract.Events.DURATION, "P1D");
//        cv2.put(CalendarContract.Events.ALL_DAY, 1);
////        cv2.put(CalendarContract.Events.CALENDAR_ID, "1");
////        cv2.put(CalendarContract.Events.EVENT_TIMEZONE, Time.TIMEZONE_UTC);
//        Uri uri1 = cr.insert(CalendarContract.Events.CONTENT_URI, cv);
////        Uri uri2 = cr.insert(CalendarContract.Events.CONTENT_URI, cv2);
////
//        long event1_ID = Long.parseLong(uri1.getLastPathSegment());
//        long event2_ID = Long.parseLong(uri2.getLastPathSegment());
        Toast.makeText(context,"Evento Criado",Toast.LENGTH_LONG).show();
//                break;
    }
    
    
    public int deletarEventoCalendario (long entryId, Context context){
        int colunasDeletadas = 0;

        Uri uriEventos = CalendarContract.Events.CONTENT_URI;
        Uri uriEvento = ContentUris.withAppendedId(uriEventos, entryId);

        colunasDeletadas = context.getContentResolver().delete(uriEvento, null, null);

        return colunasDeletadas;
    }

    public int atualizarEventoCalendario (long entryId, ContentValues cv, Context context){

        Uri uriEventos = CalendarContract.Events.CONTENT_URI;
        Uri uriEvento = ContentUris.withAppendedId(uriEventos, entryId);
        cv.remove(CalendarContract.Events.DURATION);
        int colunasAlteradas = context.getContentResolver().update(uriEvento, cv,null, null);

        return colunasAlteradas;
    }

    public boolean arrumarValoresParcelas(){
        boolean mod = false;
        for (int i = 0; i < datasPag.size(); i++){
            String parc = datasPag.get(i);
            if(parc.contains(",")){
                parc = parc.replace(",",".");
                datasPag.set(i, parc);
                mod = true;
            }
        }
        for (int i = 0; i < datasNaoPagas.size(); i++){
            String parc = datasNaoPagas.get(i);
            if(parc.contains(",")){
                parc = parc.replace(",",".");
                datasNaoPagas.set(i, parc);
                mod = true;
            }
        }
        return mod;
    }
}

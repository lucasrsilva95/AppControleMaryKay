package com.example.controlemk;

import android.content.Context;

import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OperacoesDatas {

    public Context context;

    public OperacoesDatas(Context context) {
        this.context = context;
    }


    public List<String> ordenarDatasDown(List<String> datas) {
        List<String> datasOrd = datas;

        for (int i1 = datas.size() - 1; i1 >= 1; i1--) {
            for (int i2 = i1 - 1; i2 >= 0; i2--) {
                String data1 = datas.get(i1).split("=")[0];
                String data2 = datas.get(i2).split("=")[0];
                int ano1 = Integer.parseInt(data1.substring(6,10));
                int mes1 = Integer.parseInt(data1.substring(3, 5));
                int dia1 = Integer.parseInt(data1.substring(0, 2));
                int ano2 = Integer.parseInt(data2.substring(6, 10));
                int mes2 = Integer.parseInt(data2.substring(3, 5));
                int dia2 = Integer.parseInt(data2.substring(0, 2));
                int hora1 = 0;
                int minuto1 = 0;
                int hora2 = 0;
                int minuto2 = 0;
                if (data1.length() > 13 && data2.length() > 13) {
                    hora1 = Integer.parseInt(data1.substring(13, 15));
                    minuto1 = Integer.parseInt(data1.substring(16, 18));
                    hora2 = Integer.parseInt(data2.substring(13, 15));
                    minuto2 = Integer.parseInt(data2.substring(16, 18));
                }
                if (ano1 < ano2) {
                    datasOrd.add(i1 + 1, datasOrd.get(i2));
                    datasOrd.remove(i2);
                } else if (ano1 == ano2) {
                    if (mes1 < mes2) {
                        datasOrd.add(i1 + 1, datasOrd.get(i2));
                        datasOrd.remove(i2);
                    } else if (mes1 == mes2) {
                        if (dia1 < dia2) {
                            datasOrd.add(i1 + 1, datasOrd.get(i2));
                            datasOrd.remove(i2);
                        } else if (dia1 == dia2) {
                            if (hora1 < hora2) {
                                datasOrd.add(i1 + 1, datasOrd.get(i2));
                                datasOrd.remove(i2);
                            } else if (hora1 == hora2) {
                                if (minuto1 < minuto2) {
                                    datasOrd.add(i1 + 1, datasOrd.get(i2));
                                    datasOrd.remove(i2);
                                }
                            }
                        }
                    }
                }
            }
        }
        return datasOrd;
    }

    public int subtracaoDatas(String data1, String data2) {
        int dias = 0;
        int diaAtual = Integer.parseInt(data1.substring(0, 2));
        int mesAtual = Integer.parseInt(data1.substring(3, 5));
        int anoAtual = Integer.parseInt(data1.substring(6, 10));
        int dia = Integer.parseInt(data2.substring(0, 2));
        int mes = Integer.parseInt(data2.substring(3, 5));
        int ano = Integer.parseInt(data2.substring(6, 10));

        int diasMeses = 0;
        int diasAno = 0;
        int mesMenor,mesMaior;
        if(mes < mesAtual){
            mesMenor = mes;
            mesMaior = mesAtual;
        }else{
            mesMenor = mesAtual;
            mesMaior = mes;
        }
        for (int i = mesMenor; i < mesMaior; i++) {
            if (("469").contains(Integer.toString(i)) || (i == 11)) {
                diasMeses += 30;
            } else if (i == 2) {
                if (eBissexto(anoAtual)){
                    diasMeses += 29;
                }else{
                    diasMeses += 28;
                }
            }else{
                diasMeses += 31;
            }
        }
        if(mes > mesAtual){
            diasMeses = -diasMeses;
        }
        for(int i = ano; i < anoAtual; i++){
            if (eBissexto(i)){
                diasAno += 366;
            }else{
                diasAno += 365;
            }
        }
        int diaRes = diaAtual - dia;

        dias = diasAno + diasMeses + diaRes;

        return dias;
    }

    public String somaDataDia(String data, int dias){
        int dia = Integer.parseInt(data.substring(0, 2));
        int mes = Integer.parseInt(data.substring(3, 5));
        int ano = Integer.parseInt(data.substring(6, 10));
        while(dias > 0){
            if(!(((dia + dias) < 28) || (((dia + dias) <= 30) && (("469".contains(Integer.toString(mes)) || mes == 11))) || (((dia + dias) <= 31) && (("13578".contains(Integer.toString(mes)) || mes == 12 || mes == 10))) || ((dia + dias) == 29 && mes == 2 && eBissexto(ano)))){
                if(mes == 2){
                    if (eBissexto(ano)) {
                        dias -= 29;
                    } else {
                        dias -= 28;
                    }
                }else if(("13578".contains(Integer.toString(mes)) || mes == 12 || mes == 10)){
                    dias -= 31;

                } else{
                    dias -= 30;
                }
                mes++;
                if(mes > 12){
                    mes = 1;
                    ano++;
                }

            }else{
                break;
            }

        }
        dia += dias;
        String dataRes = String.format("%02d/%02d/%02d%s",dia,mes,ano,data.substring(10));
        return dataRes;
    }

    public boolean dataEmDia(String data){
        return (subtracaoDatas(data,dataAtual()) >= 0);
    }

    public boolean eNotifFutura(String data, int horaNotif, int minNotif){
        if(subtracaoDatas(data,dataAtual()) > 0){
            return true;
        }else if(subtracaoDatas(data,dataAtual()) == 0){
            int horaAtual = Integer.parseInt(horaMinAtual().split(":")[0]);
            int minAtual = Integer.parseInt(horaMinAtual().split(":")[1]);
            if(horaAtual < horaNotif){
                return true;
            }else if(horaAtual == horaNotif && minAtual < minNotif){
                return true;
            }
        }
        return false;
    }

    public List<String> mesesDatas(List<Venda> vendas){
        List<String> meses = new ArrayList<>();
        VendasRepositorio vendRep = new VendasRepositorio(context);
        vendas = vendRep.ordenarVendasPorDataUp(vendas);
        for(Venda venda:vendas){
            if (!meses.contains(formatMesAno(venda.dataVenda))) {
                meses.add(formatMesAno(venda.dataVenda));
            }
        }
        return meses;
    }

    public List<Venda> ordenarDatas(List<Venda> vendas){
        final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        Collections.sort(vendas, new Comparator<Venda>() {
            @Override
            public int compare(Venda o1, Venda o2) {
                Date d1 = null;
                Date d2 = null;
                try {
                    d1 = formato.parse(o1.dataVenda);
                    d2 = formato.parse(o2.dataVenda);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return d1.compareTo(d2);
            }
        });
        return vendas;
    }

    public String formatMesAno(String data){
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        Date dataMes = null;
        try {
            dataMes = formato.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy MMMM", Locale.getDefault());
        String mes = simpleDateFormat.format(dataMes);

        return data.substring(3,10) + "-" + mes;
    }

    public List<Venda> vendasDoMes(String mes, List<Venda> vendas){
        List<Venda> vendasMes = new ArrayList<>();
        VendasRepositorio vendRep = new VendasRepositorio(context);
        for(Venda venda:vendas){
            if(venda.dataVenda.contains(mes.split("-")[0])){
                vendasMes.add(venda);
            }
        }
        return vendRep.ordenarVendasPorDataUp(vendasMes);
    }

    public boolean eBissexto(int ano){
        boolean resp = false;
        if ((ano%400 == 0) || ((ano%4==0) && !(ano%100==0))){
            resp = true;
        }
        return resp;
    }

    public String dataAtual(){
        Date data = new Date(System.currentTimeMillis());
        SimpleDateFormat formatarData = new SimpleDateFormat("dd/MM/yyyy");
        return (formatarData.format(data));
    }

    public String horaMinAtual(){
        Date data = new Date(System.currentTimeMillis());
        SimpleDateFormat formatarData;
        formatarData = new SimpleDateFormat("HH:mm");
        return (formatarData.format(data));
    }

    public int getYear(String data){
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = formato.parse(data.split("=")[0]);
            return date.getYear()+1900;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int getMonth(String data){
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = formato.parse(data.split("=")[0]);
            return date.getMonth();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int getDay(String data){
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = formato.parse(data.split("=")[0]);
            return date.getDate();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

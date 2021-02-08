package com.example.controlemk.database;

public class ScriptDLL {


    public static String getCreateTableProduto(){

        StringBuilder sql = new StringBuilder();

        sql.append("  CREATE TABLE PRODUTOS (");
        sql.append("  ID INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sql.append("  CODIGO INTEGER NOT NULL, ");
        sql.append("  NOME STRING NOT NULL, ");
        sql.append("  CATEGORIA STRING NOT NULL, ");
        sql.append("  QUANTIDADE REAL NOT NULL,  ");
        sql.append("  PRECO REAL NOT NULL, ");
        sql.append("  QUANT_ESTOQUE INTEGER NOT NULL, ");
        sql.append("  DETALHES STRING ) ");

        return sql.toString();
    }

    public static String getCreateTableProduto2(){

        StringBuilder sql = new StringBuilder();

        sql.append("  CREATE TABLE PRODUTOS_2 (");
        sql.append("  ID INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sql.append("  CODIGO INTEGER NOT NULL, ");
        sql.append("  NOME STRING NOT NULL, ");
        sql.append("  CATEGORIA STRING NOT NULL, ");
        sql.append("  QUANTIDADE REAL NOT NULL,  ");
        sql.append("  PRECO REAL NOT NULL, ");
        sql.append("  QUANT_ESTOQUE INTEGER NOT NULL, ");
        sql.append("  DETALHES STRING ) ");

        return sql.toString();
    }

    public static String getCreateTableVendas(){

        StringBuilder sql = new StringBuilder();

        sql.append(" CREATE TABLE VENDAS (");
        sql.append(" ID integer primary key autoincrement, ");
        sql.append(" DATA_VENDA STRING NOT NULL, ");
        sql.append(" NOME STRING NOT NULL, ");
        sql.append(" PRODUTOS STRING NOT NULL, ");
        sql.append(" TOTAL REAL NOT NULL, ");
        sql.append(" DATAS_PAG STRING, ");
        sql.append(" DATAS_NAO_PAGAS STRING , ");
        sql.append(" EFETIVADA BOOLEAN NOT NULL ) ");

        return sql.toString();

    }

    public static String getCreateTableVendas2() {

        StringBuilder sql = new StringBuilder();

        sql.append(" CREATE TABLE VENDAS_2 (");
        sql.append(" ID integer primary key autoincrement, ");
        sql.append(" DATA_VENDA STRING NOT NULL, ");
        sql.append(" NOME STRING NOT NULL, ");
        sql.append(" PRODUTOS STRING NOT NULL, ");
        sql.append(" TOTAL REAL NOT NULL, ");
        sql.append(" DATAS_PAG STRING, ");
        sql.append(" DATAS_NAO_PAGAS STRING , ");
        sql.append(" EFETIVADA BOOLEAN NOT NULL ) ");

        return sql.toString();
    }

    public static String getCreateTableClientes(){

        StringBuilder sql = new StringBuilder();

        sql.append(" CREATE TABLE CLIENTES (");
        sql.append(" ID integer primary key autoincrement, ");
        sql.append(" NOME STRING NOT NULL, ");
        sql.append(" TELEFONE STRING NOT NULL, ");
        sql.append(" ENDERECO STRING NOT NULL, ");
        sql.append(" DETALHES STRING NOT NULL)  ");

        return sql.toString();

    }

    public static String getCreateTableClientes2(){

        StringBuilder sql = new StringBuilder();

        sql.append(" CREATE TABLE CLIENTES (");
        sql.append(" ID integer primary key autoincrement, ");
        sql.append(" NOME STRING NOT NULL, ");
        sql.append(" TELEFONE STRING NOT NULL, ");
        sql.append(" ENDERECO STRING NOT NULL, ");
        sql.append(" DETALHES STRING NOT NULL)  ");

        return sql.toString();

    }

    public static String getCreateTablePedidos(){

        StringBuilder sql = new StringBuilder();

        sql.append(" CREATE TABLE PEDIDOS (");
        sql.append(" ID integer primary key autoincrement, ");
        sql.append(" DATA STRING NOT NULL, ");
        sql.append(" PRODUTOS STRING NOT NULL, ");
        sql.append(" TOTAL_BRUTO REAL NOT NULL, ");
        sql.append(" TOTAL_CUSTO REAL NOT NULL, ");
        sql.append(" DESCONTO REAL NOT NULL ) ");

        return sql.toString();

    }

    public static String getCreateTablePedidos2(){

        StringBuilder sql = new StringBuilder();

        sql.append(" CREATE TABLE PEDIDOS_2 (");
        sql.append(" ID integer primary key autoincrement, ");
        sql.append(" DATA STRING NOT NULL, ");
        sql.append(" PRODUTOS STRING NOT NULL, ");
        sql.append(" TOTAL_BRUTO REAL NOT NULL, ");
        sql.append(" TOTAL_CUSTO REAL NOT NULL, ");
        sql.append(" DESCONTO REAL NOT NULL ) ");

        return sql.toString();

    }

    public static String getCopyTableVendas(){
        StringBuilder sql2 = new StringBuilder();

        sql2.append(" INSERT INTO VENDAS_2 (ID , DATA_VENDA , NOME, PRODUTOS, TOTAL, DATAS_PAG, DATAS_NAO_PAGAS, EFETIVADA)");
        sql2.append(" SELECT  ");
        sql2.append("ID , DATA_VENDA , NOME, PRODUTOS, TOTAL, DATAS_PAG, DATAS_NAO_PAGAS, EFETIVADA");
        sql2.append(" FROM VENDAS; ");


        return sql2.toString();

    }
    public static String getCopyTableProdutos(){
        StringBuilder sql2 = new StringBuilder();

        sql2.append(" INSERT INTO PRODUTOS_2 (ID , CODIGO , NOME, CATEGORIA, QUANTIDADE, PRECO, QUANT_ESTOQUE, DETALHES)");
        sql2.append(" SELECT  ");
        sql2.append("ID , CODIGO , NOME, CATEGORIA, QUANTIDADE, PRECO, QUANT_ESTOQUE, DETALHES");
        sql2.append(" FROM PRODUTOS ; ");


        return sql2.toString();

    }
    public static String getCopyTableClientes(){
        StringBuilder sql2 = new StringBuilder();

        sql2.append(" INSERT INTO CLIENTES (ID , NOME , TELEFONE, ENDERECO, DETALHES)");
        sql2.append(" SELECT  ");
        sql2.append("ID , NOME , TELEFONE, ENDERECO, DETALHES");
        sql2.append(" FROM CLIENTESVENDAS; ");


        return sql2.toString();

    }
    public static String getCopyTablePedidos(){
        StringBuilder sql2 = new StringBuilder();

        sql2.append(" INSERT INTO PEDIDOS_2 (ID , DATA , PRODUTOS, TOTAL_BRUTO, TOTAL_CUSTO, DESCONTO)");
        sql2.append(" SELECT  ");
        sql2.append("ID , DATA , PRODUTOS, TOTAL_BRUTO, TOTAL_CUSTO, DESCONTO");
        sql2.append(" FROM PEDIDOS; ");


        return sql2.toString();

    }
}

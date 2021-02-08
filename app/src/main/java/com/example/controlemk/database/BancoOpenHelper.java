package com.example.controlemk.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.renderscript.ScriptIntrinsic;

public class BancoOpenHelper extends SQLiteOpenHelper {

    public Context context;
    public static SQLiteDatabase db;

    public BancoOpenHelper(Context context) {
        super(context, "BANCO", null, 208);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ScriptDLL.getCreateTableVendas());
        db.execSQL(ScriptDLL.getCreateTableProduto());
        db.execSQL(ScriptDLL.getCreateTableClientes());
        db.execSQL(ScriptDLL.getCreateTablePedidos());

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists VENDAS;");
        db.execSQL("drop table if exists PRODUTOS;");
        db.execSQL("drop table if exists CLIENTES;");
        db.execSQL("drop table if exists PEDIDOS;");
        onCreate(db);
    }

    public void substituirBanco(){
        db = context.openOrCreateDatabase("BANCO",Context.MODE_PRIVATE,null);
        db.execSQL("ATTACH DATABASE '" + context.getDatabasePath("VENDAS").toString() + "' AS VENDAS");
        db.execSQL("ATTACH DATABASE '" + context.getDatabasePath("PRODUTOS").toString() + "' AS PRODUTOS");
        db.execSQL("ATTACH DATABASE '" + context.getDatabasePath("CLIENTESVENDAS").toString() + "' AS CLIENTESVENDAS");
        db.execSQL("ATTACH DATABASE '" + context.getDatabasePath("PEDIDOS").toString() + "' AS PEDIDOS");

        db.execSQL(ScriptDLL.getCopyTableVendas());
        db.execSQL(ScriptDLL.getCopyTableProdutos());
        db.execSQL(ScriptDLL.getCopyTableClientes());
        db.execSQL(ScriptDLL.getCopyTablePedidos());

        context.deleteDatabase("VENDAS");
        context.deleteDatabase("PRODUTOS");
        context.deleteDatabase("CLIENTESVENDAS");
        context.deleteDatabase("PEDIDOS");

        db.execSQL("ALTER TABLE VENDAS_2 RENAME TO VENDAS");
        db.execSQL("ALTER TABLE PRODUTOS_2 RENAME TO PRODUTOS");
        db.execSQL("ALTER TABLE CLIENTES RENAME TO CLIENTESVENDAS");
        db.execSQL("ALTER TABLE PEDIDOS_2 RENAME TO PEDIDOS");
    }
}

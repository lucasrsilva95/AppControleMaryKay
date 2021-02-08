package com.example.controlemk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PedidoOpenHelper extends SQLiteOpenHelper {
    public PedidoOpenHelper(Context context) {
        super(context, "PEDIDOS", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ScriptDLL.getCreateTablePedidos());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table PEDIDOS;");
        onCreate(db);
    }
}

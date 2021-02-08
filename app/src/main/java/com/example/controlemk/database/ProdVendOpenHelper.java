package com.example.controlemk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProdVendOpenHelper extends SQLiteOpenHelper {

    public ProdVendOpenHelper(Context context) {
        super(context, "PRODUTOS", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ScriptDLL.getCreateTableProduto());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table PRODUTOS;");
        onCreate(db);
    }
}

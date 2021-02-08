package com.example.controlemk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ClientesOpenHelper extends SQLiteOpenHelper {
    public ClientesOpenHelper(Context context) {
        super(context, "CLIENTESVENDAS", null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ScriptDLL.getCreateTableClientes());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table CLIENTESVENDAS;");
        onCreate(db);
    }
}

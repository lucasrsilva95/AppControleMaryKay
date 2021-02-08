package com.example.controlemk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class VendOpenHelper extends SQLiteOpenHelper {
    public VendOpenHelper(Context context, @Nullable int version) {
        super(context, "VENDAS", null, version);
    }
    public VendOpenHelper(Context context) {
        super(context, "VENDAS", null, 208);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ScriptDLL.getCreateTableVendas());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table VENDAS;");
        onCreate(db);
    }

//    @Override
//    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        super.onDowngrade(db, oldVersion, newVersion);
//        db.setVersion(oldVersion);
//    }
}

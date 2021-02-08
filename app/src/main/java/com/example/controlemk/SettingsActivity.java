package com.example.controlemk;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.controlemk.Dominio.Repositorio.ClientesRepositorio;
import com.example.controlemk.Dominio.Repositorio.PedidosRepositorio;
import com.example.controlemk.Dominio.Repositorio.ProdutosRepositorio;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.example.controlemk.database.BackupBanco;
import com.example.controlemk.database.BancoOpenHelper;
import com.example.controlemk.database.VendOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingsActivity extends AppCompatActivity  {


    public static boolean criandoBackup = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        getSupportActionBar().setTitle("Configurações");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    public static class SettingsFragment extends PreferenceFragmentCompat implements TimePickerDialog.OnTimeSetListener{

        private Preference horaNotif, salvar_backup, carregar_backup, subsDB;
        private ListPreference tipo_backup;

        private VendasRepositorio vendRep;
        private ProdutosRepositorio prodRep;
        private ClientesRepositorio cliRep;
        private PedidosRepositorio pedRep;
        private BackupBanco backupBanco;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            horaNotif = (Preference) findPreference("horaNotif");
            horaNotif.setSummary(horaNotif.getSharedPreferences().getString("horaNotif", "09:00"));
            salvar_backup = (Preference)findPreference("salvar_backup");
            carregar_backup = (Preference)findPreference("carregar_backup");
            tipo_backup = (ListPreference) findPreference("tipo_backup");
            subsDB = (Preference) findPreference("subsDB");

            vendRep = new VendasRepositorio(getContext());
            prodRep = new ProdutosRepositorio(getContext());
            cliRep = new ClientesRepositorio(getContext());
            pedRep = new PedidosRepositorio(getContext());
            backupBanco = new BackupBanco(getContext());


            horaNotif.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    definirHoraNotif();
                    return true;
                }
            });

            salvar_backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    criandoBackup = true;
                    Date data = new Date(System.currentTimeMillis());
                    SimpleDateFormat formatarData = new SimpleDateFormat("dd/MM/yyyy-HH:mm");
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                            .setType("application/octet-stream")
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .putExtra(Intent.EXTRA_TITLE, "Backup_ControleMK" + formatarData.format(data) + ".db");
                    startActivityForResult(intent, 120);
                    return false;
                }
            });

            carregar_backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    criandoBackup = false;
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                        .setType("application/octet-stream");
                    startActivityForResult(intent, 0);

                    return true;
                }
            });
        }

        public void definirHoraNotif() {
            String horaConfig = horaNotif.getSummary().toString();
            int hora = 0;
            int min = 0;
            if (horaConfig.length() > 3) {
                hora = Integer.parseInt(horaConfig.substring(0, 2));
                min = Integer.parseInt(horaConfig.substring(3, 5));
            }
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), this, hora, min, true);
            timePickerDialog.show();
        }


        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String hora = String.format("%02d:%02d", hourOfDay, minute);
            horaNotif.setSummary(hora);
            horaNotif.getSharedPreferences().edit().putString("horaNotif", hora).apply();
            ClientesRepositorio cliRep = new ClientesRepositorio(getContext());
            cliRep.definirNotificacoes(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {

            case android.R.id.home:
                setResult(10);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BackupBanco backupBanco = new BackupBanco(getApplicationContext());
        if(data != null){
            if (criandoBackup) {
                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(data.getData());
                    backupBanco.salvarBackup(outputStream);
                    Toast.makeText(getApplicationContext(),"Backup salvos com sucesso",Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }else{
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(data.getData());
                        backupBanco.restaurarBackup(inputStream);
                        Toast.makeText(getApplicationContext(),"Backup carregado com sucesso",Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (SQLiteException e){
                        Toast.makeText(getApplicationContext(),"Arquivo de Backup inválido",Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
}
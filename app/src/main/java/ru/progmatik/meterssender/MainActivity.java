package ru.progmatik.meterssender;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ru.progmatik.meterssender.fragments.MainFragment;
import ru.progmatik.meterssender.fragments.SettingsFragment;
import ru.progmatik.meterssender.utils.AddressItem;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private SharedPreferences prefs;
    private List<AddressItem> addresses;
    private Spinner spinner;
    private TextView title;
    private ImageView btnBackBurger;
    private FrameLayout flbtnSettings;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.AppTheme_NoActionBar);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        spinner = (Spinner)findViewById(R.id.spinner);
        btnBackBurger = (ImageView)findViewById(R.id.btnBackBurger);
        flbtnSettings = (FrameLayout)findViewById(R.id.flbtnSettings);
        title = (TextView) findViewById(R.id.title);
        setSupportActionBar(toolbar);

        flbtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        });

        getPreferences();

        if (!addresses.isEmpty()){
            MainFragment fragment = new MainFragment();
            fragment.setAddresses(addresses);
            getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
            btnBackBurger.setVisibility(View.GONE);
            title.setText(R.string.meters);
        }else {
            showCustomDialog();
        }
    }

    private void showCustomDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setTitle("Нет данных");
        alertDialogBuilder.setMessage("Для отображения списка счетчиков необходимо указать адрес.");
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                SettingsFragment fragment = new SettingsFragment();
                                getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                                toolbar.setTitle(R.string.settings);
                                spinner.setVisibility(View.GONE);
                                btnBackBurger.setVisibility(View.VISIBLE);
                                flbtnSettings.setVisibility(View.GONE);
                                title.setText(R.string.settings);
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showAboutDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_main_about_dialog);
        Button btnCloseDialog = (Button) dialog.findViewById(R.id.btnCloseDialog);

        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hide();
            }
        });
        dialog.show();
    }


    private void getPreferences(){
        addresses = new ArrayList<>();
        prefs = getSharedPreferences("Progmatik.MS", MODE_PRIVATE);
        String storedHashMapString = prefs.getString("Progmatik.MS.addresses", "FAILURE");
        if (!storedHashMapString.equals("FAILURE")) {
            java.lang.reflect.Type type = new TypeToken<List<AddressItem>>() {
            }.getType();
            Gson gson = new Gson();
            List<AddressItem> testHashMap = gson.fromJson(storedHashMapString, type);
            for (AddressItem value : testHashMap)
                addresses.add(value);
        }
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, flbtnSettings);
        popupMenu.inflate(R.menu.menu);

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {

                            case R.id.settings:
                                SettingsFragment fragment = new SettingsFragment();
                                getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                                spinner.setVisibility(View.GONE);
                                flbtnSettings.setVisibility(View.GONE);
                                btnBackBurger.setVisibility(View.VISIBLE);
                                title.setText(R.string.settings);
                                return true;
                            case R.id.contacts:
                                showAboutDialog();
                                return true;
                            default:
                                return false;
                        }
                    }
                });

        popupMenu.show();
    }
}

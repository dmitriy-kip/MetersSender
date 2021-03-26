package ru.progmatik.meterssender.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.xmlpull.v1.XmlPullParserException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.progmatik.meterssender.R;
import ru.progmatik.meterssender.adapters.CustomAutoCompleteAdapter;
import ru.progmatik.meterssender.utils.AddressItem;
import ru.progmatik.meterssender.utils.CheckConnection;
import ru.progmatik.meterssender.utils.HttpsRequest;
import ru.progmatik.meterssender.utils.RequestFinishListeners;
import ru.progmatik.meterssender.utils.ZkhphoneLoader;
import ru.progmatik.meterssender.utils.ZkhphoneResponse;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends ListFragment implements RequestFinishListeners, View.OnClickListener {

    private static final int LAYOUT = R.layout.fragment_settings;
    private List<String> addresses = new ArrayList<>();
    private SharedPreferences prefs;
    private Context context;
    private Activity activity;
    private AutoCompleteTextView actvUk, actvCity, actvStreet, actvHouse, actvAppart;
    private ProgressBar ukPBar, cityPBar, streetPBar, housePBar, appartPBar;
    private List<AddressItem> addressesFull;
    private Button dialogButtonOk,dialogButtonCancel;
    private SettingsAdapter myAdapter;
    private ZkhphoneLoader loader = null;
    private String mode="";
    private ArrayMap<String, String> uk;
    private String session="", server="";
    private ZkhphoneResponse response;
    private String val = "";
    private String town_id="", street_id="", house_id="", account_id="";
    private ArrayMap<String,String> townsArrayMap, streetsArrayMap, buildingsArrayMap, acccountsArrayMap;
    private TextView tvError;
    private RelativeLayout rlCity;
    private ImageView btnBackBurger;
    private Dialog dialog;
    private InputMethodManager input;
    private FrameLayout flivUkClose,flivCityClose,flivStreetClose,flivHouseClose,flivAppartClose;

    public SettingsFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        for (AddressItem item : getPreferences()){
            addresses.add(item.getStreet()+item.getRestAddress());
        }
        myAdapter = new SettingsAdapter (activity,R.layout.fragment_settings_row, addresses);
        setListAdapter(myAdapter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(LAYOUT, null);
        context = container.getContext();
        activity = getActivity(); // задаем сразу так как getActivity() может выдавать Null при долгом ответе сервера и смене фрагмента пользователем
        Button ibAdd = (Button) v.findViewById(R.id.imbAdd);
        ibAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addresses.size() <= 2){
                    showAddDialog();
                } else{
                    Toast.makeText(context, "Нельзя добавить больше 3 адресов", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnBackBurger = (ImageView) (activity).findViewById(R.id.btnBackBurger);

        btnBackBurger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<AddressItem> values = getPreferences();
                if (!values.isEmpty()){

                    Spinner spinner = (Spinner) (activity).findViewById(R.id.spinner);
                    spinner.setVisibility(View.VISIBLE);
                    TextView title = (TextView) (activity).findViewById(R.id.title);
                    title.setText(R.string.meters);
                    FrameLayout flbtnSettings = (FrameLayout) (activity).findViewById(R.id.flbtnSettings);
                    flbtnSettings.setVisibility(View.VISIBLE);
                    btnBackBurger.setVisibility(View.INVISIBLE);

                    MainFragment fragment = new MainFragment();
                    fragment.setAddresses(values);
                    getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                }
            }
        });

        if(getPreferences().size() >0)
            btnBackBurger.setEnabled(true);
        return v;
    }

    private void showAddDialog() {
        if (dialog != null){
            dialog.cancel();
        }
        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.fragment_settings_address_dialog);
        dialog.setTitle("Укажите адрес:");

        InputMethodManager input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        input.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

        //region ViewComponents
        ukPBar = (ProgressBar)dialog.findViewById(R.id.ukPBar);
        cityPBar = (ProgressBar)dialog.findViewById(R.id.cityPBar);
        streetPBar = (ProgressBar)dialog.findViewById(R.id.streetPBar);
        housePBar =  (ProgressBar)dialog.findViewById(R.id.housePBar);
        appartPBar =  (ProgressBar)dialog.findViewById(R.id.appartPBar);
        actvUk = (AutoCompleteTextView) dialog.findViewById(R.id.actvUk);
        actvCity = (AutoCompleteTextView) dialog.findViewById(R.id.actvCity);
        actvStreet = (AutoCompleteTextView) dialog.findViewById(R.id.actvStreet);
        actvHouse = (AutoCompleteTextView) dialog.findViewById(R.id.actvHouse);
        actvAppart = (AutoCompleteTextView) dialog.findViewById(R.id.actvAppart);
        dialogButtonOk = (Button) dialog.findViewById(R.id.dialogButtonOk);
        dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
        rlCity = (RelativeLayout)dialog.findViewById(R.id.rlCity);
        tvError = (TextView)dialog.findViewById(R.id.tvError);
        flivUkClose = (FrameLayout) dialog.findViewById(R.id.flivUkClose);
        flivCityClose = (FrameLayout) dialog.findViewById(R.id.flivCityClose);
        flivStreetClose = (FrameLayout) dialog.findViewById(R.id.flivStreetClose);
        flivHouseClose = (FrameLayout) dialog.findViewById(R.id.flivHouseClose);
        flivAppartClose = (FrameLayout) dialog.findViewById(R.id.flivAppartClose);

        dialogButtonCancel.setOnClickListener(this);
        dialogButtonOk.setOnClickListener(this);
        flivUkClose.setOnClickListener(this);
        flivCityClose.setOnClickListener(this);
        flivStreetClose.setOnClickListener(this);
        flivHouseClose.setOnClickListener(this);
        flivAppartClose.setOnClickListener(this);

        //endregion
        //region actvUK

        uk = new ArrayMap<>();
        uk.put("ЗАО «УК «СПАС-ДОМ»","cabinet.spas-dom.ru:643");
        uk.put("ООО УК \"Гурман\"","89.189.186.211");
        uk.put("ООО УК \"Прогресс\"","89.189.186.211");
        uk.put("ООО \"КЖЭК\"Горский\"","cabinet.uk-gorsky.ru:543");
        uk.put("ООО \"УЖК Кировского района\"","cabinet.ugk-nsk.ru:543");
        uk.put("УК Правый Берег","cabinet.uk-pravbereg.ru:543");
        uk.put("УКЭЖ Сибирская Инициатива","cabinet.uk-sibin.ru:543");
        uk.put("ТСЖ \"Родники 3\"","cabinet.uk-sibin.ru:543");
        uk.put("ТСЖ \"Родники 4\"","cabinet.uk-sibin.ru:543");
        uk.put("ОАО \"ЖКХ г. Тогучина\" (ук)","cabinet.gkh-tog.ru:643");

        List<String> list = new ArrayList<>();
        for (String item: uk.keySet()){
            list.add(item);
        }

        actvUk.setThreshold(1);

        CustomAutoCompleteAdapter adapter = new CustomAutoCompleteAdapter(activity.getBaseContext(), list, dialog);  //ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, list);
        actvUk.setAdapter(adapter);
        actvUk.setEnabled(true);
        actvUk.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            actvCity.requestFocus();
            actvUk.setEnabled(false);

            loader = new ZkhphoneLoader();
            loader.setRequestFinishListeners(SettingsFragment.this); //указываем что перехватывать его завершения мы будем в данной activity в функции requestFinished

            ArrayMap<String, String> params = new ArrayMap<>();
            params.put("username", getString(R.string.login));
            params.put("userpswd",getString(R.string.password));
            mode = "/auth/";
            val = parent.getAdapter().getItem(position).toString();
            server = uk.get(val);
            loader.setParams(server, mode, params);
            loader.execute();

            }
        });
        //endregion
        //region actvCity
        actvCity.setThreshold(1);
        actvCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                actvStreet.requestFocus();
                actvCity.setEnabled(false);
                String am = parent.getAdapter().getItem(position).toString();
                actvCity.setText(am);
                town_id = townsArrayMap.get(am);

                loader = new ZkhphoneLoader();
                loader.setRequestFinishListeners(SettingsFragment.this); //указываем что перехватывать его завершения мы будем в данной activity в функции requestFinished
                ArrayMap<String, String> params = new ArrayMap<>();
                params.put("session",session);
                params.put("town_id", town_id);
                mode = "/lists/streets/";
                loader.setParams(server, mode, params);
                loader.execute();
            }
        });
        //endregion
        //region actvStreet
        actvStreet.setThreshold(3);
        actvStreet.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                actvHouse.requestFocus();
                actvStreet.setEnabled(false);
                String am = parent.getAdapter().getItem(position).toString();
                actvStreet.setText(am);
                street_id = streetsArrayMap.get(am);

                loader = new ZkhphoneLoader();
                loader.setRequestFinishListeners(SettingsFragment.this); //указываем что перехватывать его завершения мы будем в данной activity в функции requestFinished
                ArrayMap<String, String> params = new ArrayMap<>();
                params.put("session",session);
                params.put("town_id", town_id);
                params.put("street_id", street_id);
                mode = "/lists/buildings/";
                loader.setParams(server, mode, params);
                loader.execute();
            }
        });
        //endregion
        //region actvHouse
        actvHouse.setThreshold(1);
        actvHouse.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                actvAppart.requestFocus();
                actvHouse.setEnabled(false);
                String am = parent.getAdapter().getItem(position).toString();
                actvHouse.setText(am);
                house_id = buildingsArrayMap.get(am);

                loader = new ZkhphoneLoader();
                loader.setRequestFinishListeners(SettingsFragment.this); //указываем что перехватывать его завершения мы будем в данной activity в функции requestFinished
                ArrayMap<String, String> params = new ArrayMap<>();
                params.put("session",session);
                params.put("town_id", town_id);
                params.put("building_id", house_id);
                mode = "/lists/appartments/";
                loader.setParams(server, mode, params);
                loader.execute();
            }
        });
        //endregion
        //region actvAppart
        actvAppart.setThreshold(1);
        actvAppart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String am = parent.getAdapter().getItem(position).toString();
                actvAppart.setText(am);
                String[] res = am.split(", ЛС: ");
                account_id = res[1]; // здесь забираем акаунт_ид
                if (actvAppart != null) {
                    InputMethodManager input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    input.hideSoftInputFromWindow(actvAppart.getWindowToken(), 0);
                }
                dialogButtonOk.setEnabled(true);
                flivAppartClose.setVisibility(View.VISIBLE);
                actvAppart.setEnabled(false);

            }
        });
        //endregion
        dialog.show();
    }

    //region requestStarted requestFinished methods
    @Override
    public void requestStarted(HttpsRequest httpsRequest) throws XmlPullParserException {

        if (!CheckConnection.checkConnection(context)) {
            Toast.makeText(context, "Отсутствует соединение с интернетом", Toast.LENGTH_SHORT).show();
            loader.cancel(true);
            input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(dialogButtonCancel.getWindowToken(), 0);
            dialog.hide();
        } else {
            if (mode.equals("/auth/")){
                ukPBar.setVisibility(View.VISIBLE);
            } else if (mode.equals("/lists/towns/")){
                ukPBar.setVisibility(View.VISIBLE);
            } else if (mode.equals("/lists/streets/")){
                cityPBar.setVisibility(View.VISIBLE);
            } else if (mode.equals("/lists/buildings/")){
                streetPBar.setVisibility(View.VISIBLE);
            } else if (mode.equals("/lists/appartments/")){
                housePBar.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void requestFinished(HttpsRequest httpsRequest) throws XmlPullParserException {
        if (mode.equals("/auth/")){
            //region "/auth/"
            response = new ZkhphoneResponse();
            response = httpsRequest.Parse();
            if (response != null){
                int code = response.getResult().getCode();
                if (code == 1){
                    session = response.getResult().get_session();

                    ArrayMap<String, String> params = new ArrayMap<>();
                    params.put("session",session);
                    mode = "/lists/towns/";
                    loader = new ZkhphoneLoader();
                    loader.setRequestFinishListeners(SettingsFragment.this); //указываем что перехватывать его завершения мы будем в данной activity в функции requestFinished
                    loader.setParams(server, mode, params);
                    loader.execute();

                } else {
                    String desc = response.getResult().getDesc();
                    Toast.makeText(context, "Ошибка: "+ desc, Toast.LENGTH_SHORT).show();
                    input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    input.hideSoftInputFromWindow(actvUk.getWindowToken(), 0);
                    dialog.hide();
                }

            } else {
                Toast.makeText(context, "Сервер временно не доступен", Toast.LENGTH_SHORT).show();
                input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                input.hideSoftInputFromWindow(actvUk.getWindowToken(), 0);
                dialog.hide();
            }

            //endregion
        } else if (mode.equals("/lists/towns/")){
            //region "/lists/towns/"
            response = new ZkhphoneResponse();
            response = httpsRequest.Parse();
            if (response != null){

                int code = response.getResult().getCode();
                if (code == 1){
                    List<ArrayMap<String, String>> townsList = response.getResult().getList().getItems();
                    townsArrayMap = new ArrayMap<>();
                    for (ArrayMap<String,String> map: townsList){
                        for (Map.Entry set: map.entrySet()){
                            //if(set.getValue().toString().toLowerCase().equals(val.toLowerCase())){
                                townsArrayMap.put(map.get("name"),map.get("id"));
                            //}
                        }
                    }

                    List<String> townNames = new ArrayList<>();
                    for (Map.Entry map: townsArrayMap.entrySet()){
                        townNames.add(map.getKey().toString());
                    }

                    if (townsArrayMap.size()>1){

                        rlCity.setVisibility(View.VISIBLE);
                        CustomAutoCompleteAdapter adapter = new CustomAutoCompleteAdapter(activity.getBaseContext(), townNames, dialog);
                        actvCity.setAdapter(adapter);
                        ukPBar.setVisibility(View.GONE);
                        flivUkClose.setVisibility(View.VISIBLE);
                        actvCity.setEnabled(true);
                        actvCity.requestFocus();

                    } else{
                        //ukPBar.setVisibility(View.GONE);
                        town_id = townsArrayMap.valueAt(0);
                        actvCity.setText(townsArrayMap.keyAt(0));
                        actvCity.setEnabled(false);
                        ArrayMap<String, String> params = new ArrayMap<>();
                        params.put("session",session);
                        params.put("town_id", town_id);
                        mode = "/lists/streets/";
                        if (loader != null) {
                            loader.cancel(true);
                        }
                        loader = new ZkhphoneLoader();
                        loader.setRequestFinishListeners(SettingsFragment.this); //указываем что перехватывать его завершения мы будем в данной activity в функции requestFinished
                        //server = uk.get(val);
                        loader.setParams(server, mode, params);
                        loader.execute();
                    }
                } else{
                    String desc = response.getResult().getDesc();
                    Toast.makeText(context, "Ошибка: "+ desc, Toast.LENGTH_SHORT).show();
                    input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    input.hideSoftInputFromWindow(actvCity.getWindowToken(), 0);
                    dialog.hide();
                }
            } else{
                Toast.makeText(context, "Сервер временно не доступен", Toast.LENGTH_SHORT).show();
                input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                input.hideSoftInputFromWindow(actvCity.getWindowToken(), 0);
                dialog.hide();
            }

            //endregion
        } else if (mode.equals("/lists/streets/")){
            //region "/lists/streets/"
            response = new ZkhphoneResponse();
            response = httpsRequest.Parse();
            if (response!=null){
                int code = response.getResult().getCode();
                if (code == 1){

                    //streetsList = response.getResult().getList().getItems();
                    List<ArrayMap<String, String>> streetsList = response.getResult().getList().getItems();
                    streetPBar.setVisibility(View.GONE);
                    streetsArrayMap = new ArrayMap<>();
                    for (ArrayMap<String,String> map: streetsList){
                        streetsArrayMap.put(map.get("name"),map.get("id"));
                    }

                    List<String> streetNames = new ArrayList<>();
                    for (Map.Entry map: streetsArrayMap.entrySet()){
                        streetNames.add(map.getKey().toString());
                    }

                    CustomAutoCompleteAdapter adapter = new CustomAutoCompleteAdapter(activity.getBaseContext(), streetNames, dialog);
                    actvStreet.setAdapter(adapter);

                    cityPBar.setVisibility(View.GONE);
                    flivCityClose.setVisibility(View.VISIBLE);
                    ukPBar.setVisibility(View.GONE);
                    flivUkClose.setVisibility(View.VISIBLE);

                    actvStreet.setEnabled(true);
                    actvStreet.requestFocus();
                    actvCity.setEnabled(false);
                } else{
                    String desc = response.getResult().getDesc();
                    Toast.makeText(context, "Ошибка: "+ desc, Toast.LENGTH_SHORT).show();
                    input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    input.hideSoftInputFromWindow(actvStreet.getWindowToken(), 0);
                    dialog.hide();
                }
            }else{
                Toast.makeText(context, "Сервер временно не доступен", Toast.LENGTH_SHORT).show();
                input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                input.hideSoftInputFromWindow(actvStreet.getWindowToken(), 0);
                dialog.hide();
            }
            //endregion
        } else if (mode.equals("/lists/buildings/")){
            //region "/lists/buildings/"
            response = new ZkhphoneResponse();
            response = httpsRequest.Parse();
            if (response!=null){
                int code = response.getResult().getCode();
                if (code == 1){
                    List<ArrayMap<String, String>> buildingsList = response.getResult().getList().getItems();

                    buildingsArrayMap = new ArrayMap<>();
                    for (ArrayMap<String,String> map: buildingsList){
                        buildingsArrayMap.put(map.get("name"),map.get("id"));
                    }

                    List<String> buildingsNames = new ArrayList<>();
                    for (Map.Entry map: buildingsArrayMap.entrySet()){
                        buildingsNames.add(map.getKey().toString());
                    }
                    CustomAutoCompleteAdapter adapter = new CustomAutoCompleteAdapter(activity.getBaseContext(), buildingsNames, dialog);
                    actvHouse.setAdapter(adapter);
                    streetPBar.setVisibility(View.GONE);
                    actvHouse.setEnabled(true);
                    actvHouse.requestFocus();
                    flivStreetClose.setVisibility(View.VISIBLE);
                } else{
                    String desc = response.getResult().getDesc();
                    Toast.makeText(context, "Ошибка: "+ desc, Toast.LENGTH_SHORT).show();
                    input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    input.hideSoftInputFromWindow(actvHouse.getWindowToken(), 0);
                    dialog.hide();
                }

            }else{
                Toast.makeText(context, "Сервер временно не доступен", Toast.LENGTH_SHORT).show();
                input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                input.hideSoftInputFromWindow(actvHouse.getWindowToken(), 0);
                dialog.hide();
            }
            //endregion
        } else if (mode.equals("/lists/appartments/")){
            //region "/lists/appartments/"
            response = new ZkhphoneResponse();
            response = httpsRequest.Parse();
            if (response != null){
                int code = response.getResult().getCode();
                if (code == 1){
                    List<ArrayMap<String, String>> appartsList = response.getResult().getList().getItems();

                    acccountsArrayMap = new ArrayMap<>();
                    for (ArrayMap<String,String> map: appartsList){
                        acccountsArrayMap.put(map.get("account"), map.get("name"));
                    }

                    List<String> appartsNames = new ArrayList<>();
                    for (Map.Entry map: acccountsArrayMap.entrySet()){
                        // если есть квартиры в доме (МКД)
                        if (map.getValue() != null){
                            String val = map.getValue().toString();
                            String key = map.getKey().toString();
                            appartsNames.add(val + ", ЛС: " + key);
                        }
                    }

                    if (!appartsNames.isEmpty()){
                        CustomAutoCompleteAdapter adapter = new CustomAutoCompleteAdapter(activity.getBaseContext(), appartsNames, dialog, 0);
                        actvAppart.setAdapter(adapter);
                        //appartPBar.setVisibility(View.GONE);
                        housePBar.setVisibility(View.GONE);
                        actvAppart.setEnabled(true);
                        actvAppart.requestFocus();
                        flivHouseClose.setVisibility(View.VISIBLE);
                        actvHouse.setEnabled(false);
                    } else { // если нет квартир (частный сектор)

                        account_id = acccountsArrayMap.keyAt(0);
                        //appartPBar.setVisibility(View.GONE);
                        housePBar.setVisibility(View.GONE);
                        dialogButtonOk.setEnabled(true);
                        //actvAppart.setEnabled(true);
                        //actvAppart.requestFocus();
                        flivHouseClose.setVisibility(View.VISIBLE);
                        actvHouse.setEnabled(false);
                    }


                } else{
                    String desc = response.getResult().getDesc();
                    Toast.makeText(context, "Ошибка: "+ desc, Toast.LENGTH_SHORT).show();
                    input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    input.hideSoftInputFromWindow(actvAppart.getWindowToken(), 0);
                    dialog.hide();
                }

            } else{
                Toast.makeText(context, "Сервер временно не доступен", Toast.LENGTH_SHORT).show();
                input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                input.hideSoftInputFromWindow(actvAppart.getWindowToken(), 0);
                dialog.hide();
            }
            //endregion
        }
    }
    //endregion

    private List<AddressItem> getPreferences(){
        List<AddressItem> mapResult = new ArrayList<>();
        prefs = activity.getSharedPreferences("Progmatik.MS", MODE_PRIVATE);
        String storedHashMapString = prefs.getString("Progmatik.MS.addresses", "FAILURE");
        if (!storedHashMapString.equals("FAILURE")) {
            java.lang.reflect.Type type = new TypeToken<List<AddressItem>>() {
            }.getType();
            Gson gson = new Gson();
            mapResult = gson.fromJson(storedHashMapString, type);
        }
        return mapResult;
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            //region flivUkClose
            case R.id.flivUkClose:
                actvUk.setEnabled(true);
                actvUk.setText("");
                flivUkClose.setVisibility(View.GONE);
                dialogButtonOk.setEnabled(false);
                tvError.setVisibility(View.INVISIBLE);

                actvCity.setText("");
                actvCity.setEnabled(false);
                flivCityClose.setVisibility(View.GONE);

                actvStreet.setText("");
                actvStreet.setEnabled(false);
                flivStreetClose.setVisibility(View.GONE);

                actvHouse.setText("");
                actvHouse.setEnabled(false);
                flivHouseClose.setVisibility(View.GONE);

                actvAppart.setText("");
                actvAppart.setEnabled(false);
                flivAppartClose.setVisibility(View.GONE);

                actvUk.requestFocus();
                input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                input.showSoftInput(actvUk, 0);
                break;
            //endregion

            //region flivCityClose
            case R.id.flivCityClose:
                actvStreet.setText("");
                actvStreet.setEnabled(false);
                flivStreetClose.setVisibility(View.GONE);

                actvHouse.setText("");
                actvHouse.setEnabled(false);
                flivHouseClose.setVisibility(View.GONE);

                actvAppart.setText("");
                actvAppart.setEnabled(false);
                flivAppartClose.setVisibility(View.GONE);

                actvCity.setEnabled(true);
                actvCity.setText("");
                flivCityClose.setVisibility(View.GONE);
                dialogButtonOk.setEnabled(false);

                tvError.setVisibility(View.INVISIBLE);
                actvCity.requestFocus();
                input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                input.showSoftInput(actvCity, 0);
                break;
            //endregion

            //region flivStreetClose
            case R.id.flivStreetClose:

                actvStreet.setEnabled(true);
                actvStreet.setText("");
                flivStreetClose.setVisibility(View.GONE);
                dialogButtonOk.setEnabled(false);

                actvHouse.setText("");
                actvHouse.setEnabled(false);
                flivHouseClose.setVisibility(View.GONE);

                actvAppart.setText("");
                actvAppart.setEnabled(false);
                flivAppartClose.setVisibility(View.GONE);

                tvError.setVisibility(View.INVISIBLE);
                actvStreet.requestFocus();
                input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                input.showSoftInput(actvStreet, 0);
                break;
            //endregion

            //region flivHouseClose
            case R.id.flivHouseClose:
                actvAppart.setText("");
                actvAppart.setEnabled(false);
                flivAppartClose.setVisibility(View.GONE);

                actvHouse.setEnabled(true);
                actvHouse.setText("");
                flivHouseClose.setVisibility(View.GONE);
                dialogButtonOk.setEnabled(false);

                tvError.setVisibility(View.INVISIBLE);
                actvHouse.requestFocus();
                input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                input.showSoftInput(actvHouse, 0);
                break;
            //endregion

            //region flivAppartClose
            case R.id.flivAppartClose:
                actvAppart.setEnabled(true);
                actvAppart.setText("");
                flivAppartClose.setVisibility(View.GONE);
                dialogButtonOk.setEnabled(false);

                tvError.setVisibility(View.INVISIBLE);
                actvAppart.requestFocus();
                input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                input.showSoftInput(actvAppart, 0);
                break;
            //endregion

            case R.id.dialogButtonCancel:
                if (dialogButtonCancel != null) {
                    input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    input.hideSoftInputFromWindow(dialogButtonCancel.getWindowToken(), 0);
                }
                dialog.hide();
                break;
            case R.id.dialogButtonOk:
                addressesFull = getPreferences();
                String newAddress = actvStreet.getText() + " д. " + actvHouse.getText() + " кв. " + actvAppart.getText();
                int i = 0;
                for (AddressItem item: addressesFull){
                    if ((item.getStreet() + item.getRestAddress()).equals(newAddress))
                        i = 1;
                }
                if (i == 1){
                    tvError.setText("адрес уже добавлен");
                    tvError.setVisibility(View.VISIBLE);

                }else{
                    tvError.setVisibility(View.INVISIBLE);
                    if (addressesFull.isEmpty())
                        addressesFull = new ArrayList<>();
                    AddressItem item = new AddressItem();
                    item.setSession(session);
                    item.setAccount_id(account_id);
                    item.setStreet(actvStreet.getText().toString());
                    String restaddress = " д. " + actvHouse.getText();

                    if (!actvAppart.getText().equals("")){
                        restaddress = restaddress + " кв. " + actvAppart.getText();
                    }
                    item.setRestAddress(restaddress);
                    item.setServer(server);
                    item.setTown_id(town_id);
                    addressesFull.add(item);
                    Gson gson = new Gson();
                    String hashMapString = gson.toJson(addressesFull);
                    prefs.edit().putString("Progmatik.MS.addresses", hashMapString).apply();
                    addresses.clear();
                    for (AddressItem key: addressesFull) {
                        addresses.add(key.getStreet()+key.getRestAddress());
                    }
                    myAdapter.notifyDataSetChanged();
                    btnBackBurger.setEnabled(true);
                    dialog.hide();

                    List<AddressItem> values = getPreferences();
                    if (!values.isEmpty()){

                        Spinner spinner = (Spinner) (activity).findViewById(R.id.spinner);
                        spinner.setVisibility(View.VISIBLE);
                        TextView title = (TextView) (activity).findViewById(R.id.title);
                        title.setText(R.string.meters);
                        FrameLayout flbtnSettings = (FrameLayout) (activity).findViewById(R.id.flbtnSettings);
                        flbtnSettings.setVisibility(View.VISIBLE);
                        btnBackBurger.setVisibility(View.INVISIBLE);

                        MainFragment fragment = new MainFragment();
                        fragment.setAddresses(values);
                        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                    }
                }
        }
    }

    private class SettingsAdapter extends ArrayAdapter<String> {

        private Context mContext;

        public SettingsAdapter (Context context, int textViewResourceId,
                               List<String> objects) {
            super(context, textViewResourceId, objects);
            mContext = context;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            // return super.getView(position, convertView, parent);

            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.fragment_settings_row, parent,
                    false);
            final TextView tvAddress = (TextView) row.findViewById(R.id.tvAddress);
            tvAddress.setText(addresses.get(position));
            ImageView imageButtonDel = (ImageView) row.findViewById(R.id.imbDelete);

            imageButtonDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ArrayList<AddressItem> addressesNew= new ArrayList<>();
                    addressesFull = getPreferences();

                    if (!addressesFull.isEmpty()){

                        for (AddressItem it: addressesFull){
                            if (!(it.getStreet()+it.getRestAddress()).equals(tvAddress.getText())){
                                addressesNew.add(it);
                            }
                        }

                        Gson gson = new Gson();
                        String hashMapString = gson.toJson(addressesNew);
                        prefs.edit().putString("Progmatik.MS.addresses", hashMapString).apply();
                        addresses.clear();
                        for (AddressItem key: addressesNew)
                            addresses.add(key.getStreet()+key.getRestAddress());
                        myAdapter.notifyDataSetChanged();
                        if (addressesNew.size() == 0)
                            btnBackBurger.setEnabled(false);
                    }
                }
            });
            return row;
        }
    }
}

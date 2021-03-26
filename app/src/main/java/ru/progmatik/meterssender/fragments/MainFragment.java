package ru.progmatik.meterssender.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParserException;
import java.util.ArrayList;
import java.util.List;

import ru.progmatik.meterssender.MainActivity;
import ru.progmatik.meterssender.R;
import ru.progmatik.meterssender.adapters.MainFragmentAdapter;
import ru.progmatik.meterssender.utils.AddressItem;
import ru.progmatik.meterssender.utils.CheckConnection;
import ru.progmatik.meterssender.utils.HttpsRequest;
import ru.progmatik.meterssender.utils.RequestFinishListeners;
import ru.progmatik.meterssender.utils.ZkhphoneLoader;
import ru.progmatik.meterssender.utils.ZkhphoneResponse;


public class MainFragment extends Fragment implements RequestFinishListeners {

    private static final int LAYOUT = R.layout.fragment_main;
    private List<AddressItem> addresses;
    protected View view;
    private static LinearLayoutManager lManager;
    private RecyclerView rv;
    private Spinner spinner;
    private ZkhphoneLoader loader = null;
    private ZkhphoneResponse response;
    private String mode="", session="";
    private AddressItem item;
    private SwipeRefreshLayout swipeContainer;
    private MainFragmentAdapter adapter;
    private List<ArrayMap<String, String>> metersList;
    private Context context;
    private Activity activity;

    public MainFragmentAdapter getAdapter() {
        return adapter;
    }

    public void setMetersList(List<ArrayMap<String, String>> metersList) {
        this.metersList = metersList;
    }

    public void setAddresses(List<AddressItem> addresses) {
        this.addresses = addresses;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);
        rv = (RecyclerView)view.findViewById(R.id.recycleView);
        context = container.getContext();
        activity = getActivity(); // задаем сразу так как getActivity() может выдавать Null при долгом ответе сервера и смене фрагмента пользователем

        implementSwipeContainer();

        // region spinner

        spinner = (Spinner) (activity).findViewById(R.id.spinner);
        MySpinnerAdapter myAdapter = new MySpinnerAdapter(context);
        if (addresses != null){ // проверка обязательна так как иногда бывает address==null и всё падает
            spinner.setAdapter(myAdapter);
            if (!addresses.isEmpty()){
                spinner.setSelection(0);
            }
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                item = addresses.get(position);
                if (loader != null) {
                    loader.cancel(true);
                }
                loader = new ZkhphoneLoader();
                loader.setRequestFinishListeners(MainFragment.this); //указываем что перехватывать его завершения мы будем в данной activity в функции requestFinished

                ArrayMap<String, String> params = new ArrayMap<>();
                params.put("session",item.getSession());
                params.put("town_id",item.getTown_id());
                params.put("account_id",item.getAccount_id());
                mode = "/lists/meters/";
                loader.setParams(item.getServer(), mode, params);
                loader.execute();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // endregion

        return view;
    }

    private void implementSwipeContainer(){

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        // спиннер крутится при старте аппликухи
        swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(true);
            }
        });

        swipeContainer.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorPrimary
        );

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                View view = activity.getCurrentFocus();
                if (view!= null)
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                if (loader != null) {
                    loader.cancel(true);
                }
                loader = new ZkhphoneLoader();
                loader.setRequestFinishListeners(MainFragment.this); //указываем что перехватывать его завершения мы будем в данной activity в функции requestFinished

                ArrayMap<String, String> params = new ArrayMap<>();
                params.put("session",item.getSession());
                params.put("town_id",item.getTown_id());
                params.put("account_id",item.getAccount_id());
                mode = "/lists/meters/";
                loader.setParams(item.getServer(), mode, params);
                loader.execute();
            }
        });
    }
    @Override
    public void requestStarted(HttpsRequest httpsRequest) throws XmlPullParserException {
        if (!CheckConnection.checkConnection(context)) {
            Toast.makeText(context, "Отсутствует соединение с интернетом", Toast.LENGTH_SHORT).show();
            loader.cancel(true);
            swipeContainer.setRefreshing(false);
            rv.setVisibility(View.VISIBLE);
        } else{
            rv.setVisibility(View.GONE);
            swipeContainer.setRefreshing(true);
        }

    }
    @Override
    public void requestFinished(HttpsRequest httpsRequest) throws XmlPullParserException {

        if (mode.equals("/auth/")){
            //region "/auth/"
            response = new ZkhphoneResponse();
            response = httpsRequest.Parse();
            if (response != null){
                session = response.getResult().get_session();
                item.setSession(session);

                if (loader != null) {
                    loader.cancel(true);
                }
                loader = new ZkhphoneLoader();
                loader.setRequestFinishListeners(MainFragment.this); //указываем что перехватывать его завершения мы будем в данной activity в функции requestFinished
                ArrayMap<String, String> params = new ArrayMap<>();
                params.put("session",item.getSession());
                params.put("town_id",item.getTown_id());
                params.put("account_id",item.getAccount_id());
                mode = "/lists/meters/";
                loader.setParams(item.getServer(), mode, params);
                loader.execute();
            } else{
                Toast.makeText(context, "Сервер временно недоступен", Toast.LENGTH_SHORT).show();
                swipeContainer.setRefreshing(false);
            }
            //endregion
        } else if (mode.equals("/lists/meters/")){
            response = new ZkhphoneResponse();
            response = httpsRequest.Parse();

            if (response!=null){
                int code = response.getResult().getCode();
                if (code == 1){
                    metersList = response.getResult().getList().getItems();
                    if (metersList.size()==0){
                        Toast.makeText(context, "Счетчики по данному адресу отсутствуют", Toast.LENGTH_SHORT).show();
                        swipeContainer.setRefreshing(false);
                    }else{
                        ArrayMap<String, String> requestItems= new ArrayMap<>();
                        requestItems.put("session",item.getSession());
                        requestItems.put("server",item.getServer());
                        requestItems.put("town_id", item.getTown_id());
                        requestItems.put("account_id", item.getAccount_id());

                        lManager = new LinearLayoutManager(activity);
                        rv.setLayoutManager(lManager);
                        adapter = new MainFragmentAdapter(metersList, activity, requestItems, MainFragment.this);
                        rv.setAdapter(adapter);
                        swipeContainer.setRefreshing(false);
                        rv.setVisibility(View.VISIBLE);
                    }
                }else{ //code!=1
                    String desc = response.getResult().getDesc();
                    if (response.getResult().getDesc().toLowerCase().equals("session expired")){
                        loader = new ZkhphoneLoader();
                        loader.setRequestFinishListeners(MainFragment.this); //указываем что перехватывать его завершения мы будем в данной activity в функции requestFinished

                        ArrayMap<String, String> params = new ArrayMap<>();
                        params.put("username", getString(R.string.login));
                        params.put("userpswd",getString(R.string.password));
                        mode = "/auth/";
                        loader.setParams(item.getServer(), mode, params);
                        loader.execute();

                    } else{

                        Toast.makeText(context, "Ошибка: "+ desc, Toast.LENGTH_SHORT).show();
                        swipeContainer.setRefreshing(false);
                    }
                }
            }else{
                Toast.makeText(context, "Сервер временно недоступен", Toast.LENGTH_SHORT).show();
                swipeContainer.setRefreshing(false);
            }
        }
    }

    private class MySpinnerAdapter extends ArrayAdapter<AddressItem> {

        private MySpinnerAdapter(Context context) {
            super(context, R.layout.spinner_item, addresses);
        }

        @Nullable
        @Override
        public AddressItem getItem(int position) {
            return addresses.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AddressItem item = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate( R.layout.spinner_item, null);
            }
            ((TextView) convertView.findViewById(R.id.tvStreet))
                    .setText("ул. "+ item.getStreet());
            ((TextView) convertView.findViewById(R.id.tvRestAddress))
                    .setText(item.getRestAddress());
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            AddressItem item = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate( R.layout.spinner_item, null);
            }
            ((TextView) convertView.findViewById(R.id.tvStreet))
                    .setText("ул. "+ item.getStreet());
            ((TextView) convertView.findViewById(R.id.tvRestAddress))
                    .setText(item.getRestAddress());
            return convertView;
        }
    }



}

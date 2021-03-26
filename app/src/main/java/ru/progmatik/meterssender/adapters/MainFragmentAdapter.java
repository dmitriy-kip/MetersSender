package ru.progmatik.meterssender.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.progmatik.meterssender.R;
import ru.progmatik.meterssender.fragments.MainFragment;
import ru.progmatik.meterssender.utils.HttpsRequest;
import ru.progmatik.meterssender.utils.RequestFinishListeners;
import ru.progmatik.meterssender.utils.ZkhphoneLoader;
import ru.progmatik.meterssender.utils.ZkhphoneResponse;

/**
 * Created by Admin on 06.02.17.
 */
public class MainFragmentAdapter extends RecyclerView.Adapter<MainFragmentAdapter.MainViewHolder> {

    private List<ArrayMap<String,String>> data;
    private Activity activity;
    private Context ctx;
    private TextView tvCurrentInd, tvCurrentIndUnitName;
    private EditText etNewInd;
    private ArrayMap<String,String> requestItems;
    private MainFragment fragment;
    private Button btnCancel, btnSave;
    private Double i;
    private Integer ind;


    public MainFragmentAdapter(List<ArrayMap<String,String>> data, Activity activity, ArrayMap<String,String> requestItems, MainFragment fragment) {
        this.data = data;
        this.activity = activity;
        this.requestItems = requestItems;
        this.fragment = fragment;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ctx = parent.getContext();
        View view = LayoutInflater.from(ctx).inflate(R.layout.cardview_item_meter, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        ArrayMap<String,String> set = data.get(position);

        holder.tvServiceName.setText(set.get("group"));
        holder.tvMeterNumber.setText("№ "+ set.get("num"));
        //полученные показания делим на 100. При отправке умножаем на 100
        i = Double.valueOf(set.get("ind"))/100;

        holder.tvInd.setText(i.toString());
        holder.tvIndUnit.setText(set.get("unitname"));

        // форматим дату '2001-01-01' для получения нужного вида '1 янв. 2001'
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        Date testDate = null;
        try {
            testDate = sdf.parse(set.get("date"));
        }catch(Exception ex){
            ex.printStackTrace();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String date = formatter.format(testDate);
        holder.tvDate.setText(date);

        holder.session = requestItems.get("session");
        holder.meter_id = set.get("id");
        holder.server = requestItems.get("server");
        holder.town_id = requestItems.get("town_id");
        holder.account_id = requestItems.get("account_id");
        holder.capacity = set.get("capacity");
        String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        if (currentDate.equals(date)){
            holder.btnInd.setText("показания переданы");
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MainViewHolder extends RecyclerView.ViewHolder implements RequestFinishListeners{

        TextView tvServiceName, tvMeterNumber, tvDate, tvInd, tvIndUnit;

        Button btnInd;
        ImageView btnHist;
        String session ="",meter_id="", server ="", mode="", town_id="", account_id="", capacity="";
        Dialog dialog;
        ZkhphoneResponse response;
        SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout)activity.findViewById(R.id.swipeContainer);

        private ZkhphoneLoader loader = null;

        private MainViewHolder(final View itemView) {

            super(itemView);
            tvServiceName = (TextView)itemView.findViewById(R.id.tvServiceName);
            tvMeterNumber = (TextView)itemView.findViewById(R.id.tvMeterNumber);
            tvDate = (TextView)itemView.findViewById(R.id.tvDate);
            tvInd = (TextView)itemView.findViewById(R.id.tvInd);
            tvIndUnit = (TextView)itemView.findViewById(R.id.tvIndUnitName);


            btnHist = (ImageView) itemView.findViewById(R.id.btnHist);
            btnInd = (Button) itemView.findViewById(R.id.btnInd);


            btnHist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(activity, "Список показаний не реализован", Toast.LENGTH_SHORT).show();
                }
            });

            btnInd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
                    if (currentDate.equals(tvDate.getText())){
                        Toast.makeText(activity, "Показания на эту дату уже переданы", Toast.LENGTH_SHORT).show();
                    }else {
                        showAddIndDialog();
                    }
                }
            });
        }

        private void showAddIndDialog(){

            dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.fragment_main_addind_dialog);

            etNewInd = (EditText) dialog.findViewById(R.id.etNewInd);
            tvCurrentInd = (TextView) dialog.findViewById(R.id.tvCurrentInd);
            tvCurrentInd.setText(tvInd.getText());

            tvCurrentIndUnitName = (TextView) dialog.findViewById(R.id.tvCurrentIndUnitName);
            tvCurrentIndUnitName.setText(tvIndUnit.getText());

            final TextView tvError = (TextView)dialog.findViewById(R.id.tvError);


            final TextView tvCurrentDate = (TextView)dialog.findViewById(R.id.tvCurrentDate);
            tvCurrentDate.setText("на "+tvDate.getText());

            btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
            btnSave = (Button) dialog.findViewById(R.id.btnSave);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (btnCancel != null) {
                        InputMethodManager input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        input.hideSoftInputFromWindow(btnCancel.getWindowToken(), 0);
                    }
                    dialog.hide();
                }
            });

            InputMethodManager input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            input.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newInd = etNewInd.getText().toString();
                    //if (Double.valueOf(capacity)== 0){
                    //    capacity = "100000";
                    //}
                    //double maxValue = Double.valueOf(capacity)/4000;

                    if (newInd.equals("")) {
                        tvError.setText("введите показание");
                        tvError.setVisibility(View.VISIBLE);
                    } /*else if ((Double.valueOf(newInd) - Double.valueOf(tvCurrentInd.getText().toString())) > maxValue) {
                        tvError.setText("макс. потребление: "+ String.valueOf(maxValue));
                        tvError.setVisibility(View.VISIBLE);
                    }*/ else {
                        tvError.setVisibility(View.INVISIBLE);
                        loader = new ZkhphoneLoader();
                        loader.setRequestFinishListeners(MainViewHolder.this); //указываем что перехватывать его завершения мы будем в данной activity в функции requestFinished

                        ArrayMap<String, String> params = new ArrayMap<>();
                        params.put("session",session);
                        params.put("trx_id","-1");
                        params.put("terminal_id","-1");
                        params.put("town_id",town_id);
                        params.put("account_id",account_id);
                        params.put("meters_list",meter_id);
                        i = Double.valueOf(etNewInd.getText().toString());
                        //i = i*100;
                        //ind = i.;
                        //params.put("charges_list",i.toString());
                        params.put("charges_list",etNewInd.getText().toString().replace(".","")+ "00");
                        mode = "/register/payments/";
                        loader.setParams(server, mode, params);
                        loader.execute();

                        if (etNewInd != null) {
                            InputMethodManager input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            input.hideSoftInputFromWindow(etNewInd.getWindowToken(), 0);
                        }
                        dialog.hide();
                    }
                }
            });

            dialog.show();
        }

        @Override
        public void requestStarted(HttpsRequest httpsRequest) throws XmlPullParserException {
            swipeContainer.setRefreshing(true);
        }

        @Override
        public void requestFinished(HttpsRequest httpsRequest) throws XmlPullParserException {

            if (mode.equals("/auth/")){
                //region "/auth/"
                response = new ZkhphoneResponse();
                response = httpsRequest.Parse();
                if (response != null){

                    session = response.getResult().get_session();
                    if (loader != null) {
                        loader.cancel(true);
                    }
                    loader = new ZkhphoneLoader();
                    loader.setRequestFinishListeners(MainViewHolder.this); //указываем что перехватывать его завершения мы будем в данной activity в функции requestFinished
                    ArrayMap<String, String> params = new ArrayMap<>();
                    params.put("session",session);
                    params.put("trx_id","-1");
                    params.put("terminal_id","-1");
                    params.put("town_id",town_id);
                    params.put("account_id",account_id);
                    params.put("meters_list",meter_id);
                    i = Double.valueOf(etNewInd.getText().toString());
                    i = i*100;
                    params.put("charges_list",i.toString());

                    mode = "/register/payments/";
                    loader.setParams(server, mode, params);
                    loader.execute();
                } else{
                    Toast.makeText(activity, "Сервер временно недоступен", Toast.LENGTH_SHORT).show();
                    swipeContainer.setRefreshing(false);
                }
                //endregion
            } else if (mode.equals("/register/payments/")){
                response = new ZkhphoneResponse();
                response = httpsRequest.Parse();
                if (response != null){
                    int code = response.getResult().getCode();
                    if (code == 1){
                        Toast.makeText(activity, "Показания переданы успешно", Toast.LENGTH_SHORT).show();
                        //api не передает нужные данные в случае успеха, поэтому обновляем исходя из переданного значения
                        ArrayMap<String, String > dat = data.get(getAdapterPosition());
                        dat.remove("ind");
                        dat.put("ind",i.toString());
                        // здесь обновляем исходя из текущей даты устройства.
                        dat.remove("date");
                        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

                        dat.put("date",currentDate);
                        swipeContainer.setRefreshing(false);
                        fragment.setMetersList(data);
                        fragment.getAdapter().notifyDataSetChanged();

                    } else{ //code!=1
                        String desc = response.getResult().getDesc();
                        if (response.getResult().getDesc().toLowerCase().equals("session expired")){
                            loader = new ZkhphoneLoader();
                            loader.setRequestFinishListeners(MainViewHolder.this); //указываем что перехватывать его завершения мы будем в данной activity в функции requestFinished

                            ArrayMap<String, String> params = new ArrayMap<>();
                            params.put("username",activity.getString(R.string.login));
                            params.put("userpswd",activity.getString(R.string.password));
                            mode = "/auth/";
                            loader.setParams(server, mode, params);
                            loader.execute();

                        } else {
                            Toast.makeText(activity, "Ошибка: "+ desc, Toast.LENGTH_SHORT).show();
                        }
                        swipeContainer.setRefreshing(false);
                    }
                } else{
                    Toast.makeText(activity, "Сервер временно недоступен", Toast.LENGTH_SHORT).show();
                    swipeContainer.setRefreshing(false);
                }
            }
        }
    }
}

package gadget.weathercontroller.controller;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import gadget.component.job.owm.data.City;
import gadget.weatherbox.Client;

import java.util.List;

/**
 * Created by Dustin on 28.12.2015.
 */
public class CitySelectionListener implements TextWatcher {
    private Client client;

    public CitySelectionListener(Client client) {
        this.client = client;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!(s instanceof AutoCompleteTextView)) return;
        List<City> cityList = client.searchCity(s.toString());
        AutoCompleteTextView textView = (AutoCompleteTextView) s;
        textView.setAdapter(new ArrayAdapter<City>(textView.getContext(), android.R.layout.simple_dropdown_item_1line, cityList));
    }
}

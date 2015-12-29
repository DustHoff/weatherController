package gadget.weathercontroller.controller;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import gadget.component.api.data.Config;
import gadget.weatherbox.Client;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConfigDialog.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConfigDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigDialog extends AppCompatDialogFragment {

    private Client client = new Client(WeatherController.URL);
    private OnFragmentInteractionListener mListener;

    public ConfigDialog() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ConfigDialog.
     */
    // TODO: Rename and change types and number of parameters
    public static ConfigDialog newInstance() {
        ConfigDialog fragment = new ConfigDialog();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_configuration_dialog, container, false);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Config config = client.getConfig();
        EditText owmUrl = (EditText) view.findViewById(R.id.owmUrl);
        owmUrl.setText(config.getUrl());
        EditText owmCityDL = (EditText) view.findViewById(R.id.owmCityDL);
        owmCityDL.setText(config.getDlcity());
        EditText owmKey = (EditText) view.findViewById(R.id.owmKey);
        owmKey.setText(config.getKey());
        EditText forecast = (EditText) view.findViewById(R.id.forecast);
        forecast.setText(config.getForecast() + "");
        CheckBox useClouds = (CheckBox) view.findViewById(R.id.useClouds);
        useClouds.setChecked(config.isUseClouds());
        CheckBox useSky = (CheckBox) view.findViewById(R.id.useSkylight);
        useSky.setChecked(config.isUseSky());
        CheckBox useRain = (CheckBox) view.findViewById(R.id.useRain);
        useRain.setChecked(config.isUseRain());
        AutoCompleteTextView citySelect = (AutoCompleteTextView) view.findViewById(R.id.selectedCity);
        citySelect.addTextChangedListener(new CitySelectionListener(client));
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}

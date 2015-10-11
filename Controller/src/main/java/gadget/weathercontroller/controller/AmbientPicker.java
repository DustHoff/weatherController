package gadget.weathercontroller.controller;

import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Toast;
import gadget.component.hardware.data.CloudType;
import gadget.weathercontroller.controller.comm.Api;
import gadget.weathercontroller.controller.comm.ApiException;

/**
 * Created by Dustin on 11.10.2015.
 */
public class AmbientPicker implements SeekBar.OnSeekBarChangeListener, AdapterView.OnItemClickListener {
    private int red;
    private int green;
    private int blue;
    private int rain;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) return;
        if (seekBar.getId() == R.id.red) red = progress;
        if (seekBar.getId() == R.id.green) green = progress;
        if (seekBar.getId() == R.id.blue) blue = progress;
        if (seekBar.getId() == R.id.rain) rain = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            Api.call().setSkylightRGB((short) red, (short) green, (short) blue);
            Api.call().setRainIntensity(rain);
        } catch (ApiException e) {
            Toast.makeText(seekBar.getContext(), R.string.notConnected, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String[] array = view.getContext().getResources().getStringArray(R.array.mist);

        CloudType type = CloudType.valueOf(array[position]);
        try {
            Api.call().setCloudIntensitiy(type);
        } catch (ApiException e) {
            Toast.makeText(view.getContext(), R.string.notConnected, Toast.LENGTH_LONG).show();
        }
    }
}
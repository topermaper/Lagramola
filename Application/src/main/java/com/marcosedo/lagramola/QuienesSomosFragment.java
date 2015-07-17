package com.marcosedo.lagramola;

/**
 * Created by Marcos on 22/06/15.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class QuienesSomosFragment extends Fragment {

    private SharedPreferences preferences;
    private GoogleMap map;
    private ScrollView scrollView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getActivity().getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("ListaEventosFragment", "onCreateView");
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.quienes_somos_fragment, container, false);


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        loadMap();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        System.out.print("Quienes somos fragment onResume");
        super.onResume();
    }



    /**
     * * The mapfragment's id must be removed from the FragmentManager or else if the same
     * it is passed on the next time then app will crash ***
     */

    /*@Override
    public void onDestroyView() {
        super.onDestroyView();
        if (map != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(getChildFragmentManager().findFragmentById(R.id.map)).commitAllowingStateLoss();
            map = null;
        }
    }*/

    void loadMap() {
        /////////////////////////////////////////////////////
        LatLng gramolaPosition = new LatLng(39.485000, -0.3605864);

        map = ((ScrollableMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
        scrollView = (ScrollView) getActivity().findViewById(R.id.scrollView);

        ((ScrollableMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).setListener(new ScrollableMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                scrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        // Move the camera instantly to la gramola
        map.moveCamera(CameraUpdateFactory.newLatLng(gramolaPosition));
        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        //ponemos botones de zoom
        map.getUiSettings().setZoomControlsEnabled(true);
        Marker gramolaMarker = map.addMarker(new MarkerOptions()
                .position(gramolaPosition)
                .title("La Gramola"));
        gramolaMarker.setSnippet("Calle Barón de San Petrillo nº9\n46020 Valencia");
        gramolaMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.markerlagramola));
        gramolaMarker.showInfoWindow();

    }

}


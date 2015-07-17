package com.marcosedo.lagramola;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MostrarImagenFragment extends Fragment {

    private String imagename;
    private ImageView ivImage;

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        configurarToolbar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {

        super.onCreate(savedInstance);

        //Resources res = getResources();
        View view = inflater.inflate(R.layout.mostrar_imagen, container, false);
        ivImage = (ImageView) view.findViewById(R.id.ivImagen);
        Log.i("imagen fragment","entramos en el oncreate view");
        Bundle bundle = getArguments();
        if (bundle != null) {
            imagename = bundle.getString("imagepath");

            new LoadImage().execute(imagename);

            //Bitmap cartelBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            //imageView.setImageBitmap(cartelBitmap);
        }
        else Log.i("TAG","pero el bundle es null");

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (menu != null) {//Escondemos el añadir evento
            menu.setGroupVisible(R.id.tab_listaeventos_admin_group, false);
            menu.setGroupVisible(R.id.base_group,false);
        }
    }
    /////////////////////////////////////////////////////
    ///Async Task para obtener la imagen que queremos mostrar//////////
    /////////////////////////////////////////////////////
    class LoadImage extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading image...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();

            try {
                HttpUploader uploader = new HttpUploader(getActivity().getResources(),BuildConfig.DOMAIN + Constantes.GET_IMAGE_FILE);

                uploader.añadirArgumento("image", args[0]);

                json = uploader.enviar();
            } catch (ConnectionException e) {
                e.printStackTrace();
                try {
                    json.put(Constantes.JSON_MESSAGE, e.getMessage());
                    json.put(Constantes.JSON_SUCCESS, 0);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
            return json;
        }


        protected void onPostExecute(JSONObject json) {

            int success;
            String mensaje;

            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);

                if (success == 1) {//si fue bien
                    // Getting Array of Events
                    String image_string = json.getString("image");
                    byte[] image_byte = Base64.decode(image_string, Base64.DEFAULT);

                    ivImage.setImageBitmap(BitmapFactory.decodeByteArray(image_byte, 0, image_byte.length));

                } else {
                    Toast.makeText(getActivity(), mensaje, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        getVisibility();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onPause(){
        super.onStop();
        lostVisibility();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void lostVisibility(){
        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tabLayout);
        tabLayout.setVisibility(View.VISIBLE);
        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewPager);
        viewPager.setVisibility(View.VISIBLE);
        FrameLayout outTabContent = (FrameLayout) getActivity().findViewById(R.id.outTabContent);
        outTabContent.setVisibility(View.GONE);
    }

    private void getVisibility(){
        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tabLayout);
        tabLayout.setVisibility(View.GONE);
        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewPager);
        viewPager.setVisibility(View.GONE);
        FrameLayout outTabContent = (FrameLayout) getActivity().findViewById(R.id.outTabContent);
        outTabContent.setVisibility(View.VISIBLE);
    }

    private void configurarToolbar() {
        setHasOptionsMenu(true);
        ((Toolbar) (getActivity()).findViewById(R.id.toolbar))
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
    }

}

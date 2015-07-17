/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marcosedo.lagramola;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;


import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements
        ListaEventosFragment.OnEditarEventoListener, ListaEventosFragment.OnMostrarImagenListener, ListaEventosFragment.OnMostrarDetalleEventoListener, EditarEventosFragment.OnEnviarMensajeListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    private SharedPreferences preferences;
    private MostrarDetalleEventoFragment mostrarDetalleEventoFragment;
    private GoogleCloudMessaging gcm;
    private String devid;//devid de nuestro dispositivo

    public MostrarDetalleEventoFragment detalleEventoFragment;

    public String getDevid() {
        return devid;
    }

    public void setDevid(String devid) {
        this.devid = devid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //INICIALIZAMOS PROPIEDADES
        devid = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        preferences = getSharedPreferences(Constantes.SP_FILE, MODE_PRIVATE);

        setContentView(R.layout.activity_main);

        configureToolbarAndTabLayout();

        registerDevice();


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String msg = extras.getString("msg");
            String idevent = extras.getString("idevento");
            String titulo = extras.getString("titulo");

            if (idevent != null) {//if the message has an event id then
                if ((msg != null) && (titulo!= null)) {//show Dialog with a message
                    showDialogMsgArrived(titulo,msg);
                }
                openViewDetail(idevent);
            }
        }
    }


    @Override
    //si cuando se lanza el intent la app estaba abierta se viene aqui porque hemos puesto FLAG_ACTIVITY_SINGLE_TOP
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent");

        Bundle extras = intent.getExtras();
        if (extras != null) {
            String msg = extras.getString("msg");
            String idevent = extras.getString("idevento");
            String titulo = extras.getString("titulo");

            if (idevent != null) {//if the message has an event id then
                if ((msg != null) && (titulo!= null)) {//show Dialog with a message
                    showDialogMsgArrived(titulo,msg);
                }
                openViewDetail(idevent);
            }
        }

        super.onNewIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu from menu resource (res/menu/main)

            getMenuInflater().inflate(R.menu.options_menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }




    public void onEditarEvento(Evento event) {

        EditarEventosFragment editorFragment = new EditarEventosFragment();
        Bundle args = new Bundle();
        args.putParcelable("event", event);
        editorFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.outTabContent, editorFragment, "editor");
        transaction.addToBackStack(null);
        transaction.commit();// Commit the transaction*/
    }

    public void onEnviarMensaje(String mensaje,String idevento) {
        new EnviarMensaje().execute(mensaje, idevento);
    }

    public void onMostrarImagen(String path) {

        MostrarImagenFragment fragment = new MostrarImagenFragment();
        Bundle args = new Bundle();

        //Establecemos los argumentos
        args.putString("imagepath", path);

        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.outTabContent, fragment, "show image");
        transaction.addToBackStack(null);
        transaction.commit();// Commit the transaction
    }

    public void onMostrarDetalleEvento(Evento event) {
        Bundle args = new Bundle();
        args.putParcelable("event", event);

        if (mostrarDetalleEventoFragment == null) {
            mostrarDetalleEventoFragment = new MostrarDetalleEventoFragment();
        }

        mostrarDetalleEventoFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.outTabContent, mostrarDetalleEventoFragment, "mostrar detalle");
        transaction.addToBackStack(null);
        transaction.commit();// Commit the transaction
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, JSONObject>() {
            String regid = null;

            @Override
            protected JSONObject doInBackground(Void... params) {
                Log.i("register in background", "vamos a registrarnos en background");
                JSONObject json = new JSONObject();

                try {
                    Log.i(TAG, "comprobamos si gcm = null");
                    if (gcm == null) {
                        Log.i(TAG, "gcm = null");
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    } else {
                        Log.i(TAG, "gcm no es null");
                    }

                    regid = gcm.register(Constantes.SENDER_ID);//ahora registramos siempre

                    Log.i("GCM REGISTER", "enviamos regid =" + regid);
                    Log.i("GCM REGISTER", "enviamos devid =" + devid);

                    HttpUploader uploader = new HttpUploader(getResources(), BuildConfig.DOMAIN + Constantes.GCMSERVER_FILE);
                    uploader.añadirArgumento("devid", devid);
                    uploader.añadirArgumento("regid", regid);
                    uploader.añadirArgumento("mode", "registro");
                    json = uploader.enviar();
                } catch (ConnectionException e) {
                    e.printStackTrace();
                    try {
                        json.put(Constantes.JSON_MESSAGE, e.getMessage());
                        json.put(Constantes.JSON_SUCCESS, 0);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return json;
            }

            @Override
            protected void onPostExecute(JSONObject json) {
                // Persist the regID - no need to ask for a new one
                try {
                    int success = json.getInt(Constantes.JSON_SUCCESS);
                    String message = json.getString(Constantes.JSON_MESSAGE);

                    if (success == 1) {//si fue bien
                        Log.i(TAG, "Guardamos regid = " + regid + " en preferencias");
                        storeRegistrationId(regid);

                    } else {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute(null, null, null);
    }


    //Stores the registration ID and app versionCode in the application's
    private void storeRegistrationId(String regId) {

        Log.i("storeRegistrationId", "Saving regId = " + regId + " on app version = " + Utilidades.getAppVersion(this));
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constantes.REG_ID, regId);
        editor.putInt(Constantes.APP_VERSION, Utilidades.getAppVersion(this));
        editor.commit();
    }

    private void registerDevice(){
        if (!(preferences.getBoolean(Constantes.GCM_REGISTER_ALREADY_DONE,false))){
            //Check device for Play Services APK.
            Log.i("MainACtivity onCreate", "comprobamos si hay servicio googleplay");
            if (checkPlayServices()) {
                Log.i("MainACtivity onCreate", "hay servicio googleplay");
                gcm = GoogleCloudMessaging.getInstance(this);
                registerInBackground();
            } else {
                Toast.makeText(this, "No valid Google Play Services APK found.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDialogMsgArrived(String titulo, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    //This replace the current fragment with a new detalleEventoFragment
    private void openViewDetail(String idevento) {
        Bundle args = new Bundle();
        args.putString("idevento", idevento);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        //si ya hay un fragment lo destruimos y creamos otro
        //no queremos basura pululando x ahi
        //lo hacemos porque no se pueden cambiar los argumentos una vez creado
        if (detalleEventoFragment != null){
            transaction.remove(detalleEventoFragment);
        }

        detalleEventoFragment = new MostrarDetalleEventoFragment();
        detalleEventoFragment.setArguments(args);
        transaction.replace(R.id.outTabContent, detalleEventoFragment, "detalle evento");
        transaction.addToBackStack(null);
        transaction.commit();// Commit the transaction

    }

    /////////////////////////////////////////////////////
    ///Async Task para envia mensaje////////////////////
    /////////////////////////////////////////////////////
    class EnviarMensaje extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject json = new JSONObject();

            String msg = params[0];
            String idevento = params[1];

            try {
                HttpUploader uploader = new HttpUploader(getResources(), BuildConfig.DOMAIN + Constantes.GCMSERVER_FILE);

                uploader.añadirArgumento("message", msg);
                uploader.añadirArgumento("mode", "mensaje");

                if (idevento != null) uploader.añadirArgumento("idevento", idevento);

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

        @Override
        protected void onPostExecute(JSONObject json) {
            // check for success tag
            int success;

            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                if (success == 1) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.mensaje_enviado)
                            , Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.error_enviando_mensaje)
                            , Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected del mainActivity");
        // Handle presses on the action bar items
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (item.getItemId()) {

            case R.id.action_profile://Perfil de usuario
                ProfileFragment creaPerfilFragment = new ProfileFragment();

                transaction.replace(R.id.outTabContent, creaPerfilFragment, "notificaciones");
                transaction.addToBackStack(null);
                transaction.commit();// Commit the transaction
                break;
            case R.id.action_help:
                HelpFragment helpFragment = new HelpFragment();

                transaction.replace(R.id.outTabContent, helpFragment, "help");
                transaction.addToBackStack(null);
                transaction.commit();// Commit the transaction
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void configureToolbarAndTabLayout(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View w) {
                getVisibilityOnTabs();
            }
        });

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);

        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), MainActivity.this);
        viewPager.setAdapter(pagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        // Iterate over all tabs and set the custom view
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(pagerAdapter.getTabView(i));
        }
    }

    private void getVisibilityOnTabs(){
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setVisibility(View.VISIBLE);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setVisibility(View.VISIBLE);
        FrameLayout outTabContent = (FrameLayout) findViewById(R.id.outTabContent);
        outTabContent.setVisibility(View.GONE);
    }
}

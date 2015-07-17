package com.marcosedo.lagramola;


import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditarEventosFragment extends Fragment implements OnClickListener {

    /////IMAGEN
    public static final int IMAGE_GALLERY = 1;
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    public Bundle bundle;//BUNDLE
    String TAG = "EditarEventosFragment";
    Evento event = new Evento();

    private OnEnviarMensajeListener mCallback;

    // Dialog
    private ProgressDialog pDialog;
    private Dialog dialogo;
    private EditText etFecha;
    private EditText etHora;
    private EditText etTitulo;
    private EditText etDescription;

    private ImageView carteliv;
    private Button okbtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configurarToolbar();

        bundle = getArguments();
        if (bundle != null) {
            event = bundle.getParcelable("event");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getVisibility();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onPause() {
        super.onStop();
        lostVisibility();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        super.onSaveInstanceState(toSave);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.editar_eventos_layout, container, false);

        //INICIALIZAMOS VARIABLES GLOBALES
        etFecha = (EditText) view.findViewById(R.id.etFecha);
        etHora = (EditText) view.findViewById(R.id.etHora);
        etTitulo = (EditText) view.findViewById(R.id.etTitulo);
        etDescription = (EditText) view.findViewById(R.id.etDescription);
        carteliv = (ImageView) view.findViewById(R.id.ivCartel);
        okbtn = (Button) view.findViewById(R.id.btnOk);

        //inicializamos ImageView
        carteliv.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_gallery));

        ////if editing event////////////////////////////
        if (event.getId() != null) {

            byte[] imgByteArray = null;

            if (event.getCartel() != null) {//cogemos la imagen si la hay
                imgByteArray = event.getCartel();
            } else {//y si no probamos con el thumb
                if (event.getThumb() != null)
                    imgByteArray = event.getThumb();
            }
            //ponemos la imagen en el ImageView
            if (imgByteArray != null) {
                carteliv.setImageBitmap(BitmapFactory.decodeByteArray(imgByteArray, 0, imgByteArray.length));
            }

            //rellenamos todos los campos restantes
            etFecha.setText(event.getFecha());
            etHora.setText(event.getHora());
            etTitulo.setText(event.getTitulo());
            etDescription.setText(event.getDescription());

        }

        ///////LISTENERS///////////
        carteliv.setOnClickListener(this);
        okbtn.setOnClickListener(this);
        etFecha.setOnClickListener(this);
        etHora.setOnClickListener(this);

        configurarToolbar();

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnEnviarMensajeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnEnviarMensajeListener");
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (menu != null) {//Escondemos el añadir evento
            menu.setGroupVisible(R.id.tab_listaeventos_admin_group, false);
            menu.setGroupVisible(R.id.base_group, false);
        }
    }

    //Fill the properties of the event
    public void initializeEvent() {
        event.setFecha(etFecha.getText().toString());
        event.setHora(etHora.getText().toString());
        event.setTitulo(etTitulo.getText().toString());
        event.setDescription(etDescription.getText().toString());
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.etHora:
                showTimePickerDialog();
                break;

            case R.id.etFecha:
                showDatePickerDialog();
                break;

            case R.id.ivCartel:
                seleccionarCartel();
                break;

            case R.id.btnOk:

                String fecha = etFecha.getText().toString();
                String hora = etHora.getText().toString();

                if (validarFecha(fecha)) {
                    if (validarHora(hora)) {

                        initializeEvent();//fill the properties of the event
                        new SaveEvent().execute();

                    } else {
                        Toast.makeText(getActivity(), "Time format wrong. Use hh/mm", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Date format wrong. Use dd/mm/yyyy", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
        }
    }

    public void seleccionarCartel() {
        try {
            final String[] items = {"Galería", "Eliminar foto"};
            final Integer[] icons = new Integer[]{R.drawable.ic_menu_gallery,
                    R.drawable.ic_menu_delete};

            ArrayAdapterWithIcon arrayadapter = new ArrayAdapterWithIcon(getActivity(), items, icons, null);

            Builder builder = new Builder(getActivity());
            builder.setTitle("Selecciona imagen");
            builder.setAdapter(arrayadapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0://SELECTING A NEW IMAGE FROM GALLERY
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent, IMAGE_GALLERY);
                            break;
                        case 1://DELETING THE IMAGE
                            Resources res = getActivity().getResources();
                            event.setCartel(null);
                            event.setThumb(null);
                            carteliv.setImageDrawable(res.getDrawable(R.drawable.ic_menu_gallery));
                            break;
                    }
                }
            });

            builder.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("onACtivityResul", "code = " + Integer.toString(requestCode));

        ContentResolver cr = getActivity().getContentResolver();

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {


                case IMAGE_GALLERY:
                    try {
                        Uri selectedImage = data.getData();
                        Bitmap bmOriginal = MediaStore.Images.Media.getBitmap(cr, selectedImage);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmOriginal.compress(Bitmap.CompressFormat.JPEG, Constantes.JPEG_QUALITY_CARTEL, stream);

                        event.setCartel(stream.toByteArray());
                        carteliv.setImageBitmap(bmOriginal);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
            }
        }
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    boolean validarFecha(String fecha) {

        String[] fechaSplit = fecha.split("/");
        int[] fechaSplitInt = new int[3];

        if (fechaSplit.length == 3) {//si longitud 3 es bien :).........SEGUIMOS
            for (int i = 0; i < fechaSplit.length; i++) {//Pasamos los strings a enteros para poder trabajar
                try {
                    fechaSplitInt[i] = Integer.parseInt(fechaSplit[i]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            //1<MES<12
            if ((fechaSplitInt[Constantes.MES] < 1) || (fechaSplitInt[Constantes.MES] > 12)) {
                return false;
            }

            //1<DIA<12
            if ((fechaSplitInt[Constantes.DIA] < 1) || (fechaSplitInt[Constantes.DIA] > 31)) {
                return false;
            }

        }//longitud != 3 es mal...........
        else {
            return false;
        }
        return true;
    }

    boolean validarHora(String hora) {

        String[] horaSplit = hora.split(":");
        int[] horaSplitInt = new int[3];

        if (horaSplit.length == 2) {//si longitud 2 es bien :).........SEGUIMOS
            for (int i = 0; i < horaSplit.length; i++) {//Pasamos los strings a enteros para poder trabajar
                try {
                    horaSplitInt[i] = Integer.parseInt(horaSplit[i]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            //0<HORA<23
            if ((horaSplitInt[Constantes.HOR] < 0) || (horaSplitInt[Constantes.HOR] > 23)) {
                Log.i("valida", horaSplitInt[Constantes.HOR] + "hora");
                return false;
            }

            //0<MIN<59
            if ((horaSplitInt[Constantes.MIN] < 0) || (horaSplitInt[Constantes.MIN] > 59)) {
                Log.i("valida", "dia");
                return false;
            }

        }//longitud != 2 es mal...........
        else {
            Log.i("valida", "return false");
            return false;
        }
        return true;
    }

    private void MostrarDialogoEnvioNotificacion() {


        View view = getActivity().getLayoutInflater().inflate(R.layout.dialogo_enviar_notificacion,null);


        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        final EditText etMessage = (EditText) view.findViewById(R.id.etMessage);


        //If we are editing the event  event.getId will be !=null
        if (event.getId() != null) {
            tvMessage.setText(getResources().getString(R.string.event_has_been_modified));
        } else {
            tvMessage.setText(getResources().getString(R.string.event_has_been_created));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setCancelable(false);
        builder.setTitle("¿Enviar notificación?");

        builder.setPositiveButton("Si",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mCallback.onEnviarMensaje(etMessage.getText().toString(), event.getId());
                        getActivity().getSupportFragmentManager().popBackStack();//esto nos lleva al fragment anterior. es como si apretasemos atras
                    }
                });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                getActivity().getSupportFragmentManager().popBackStack();//esto nos lleva al fragment anterior. es como si apretasemos atras
            }
        });
        builder.show();

    }

    private void lostVisibility() {
        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tabLayout);
        tabLayout.setVisibility(View.VISIBLE);
        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewPager);
        viewPager.setVisibility(View.VISIBLE);
        FrameLayout outTabContent = (FrameLayout) getActivity().findViewById(R.id.outTabContent);
        outTabContent.setVisibility(View.GONE);
    }

    private void getVisibility() {
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

    // Container Activity must implement this interface
    public interface OnEnviarMensajeListener {
        void onEnviarMensaje(String mensaje, String idevento);
    }

    //////////////////////////////////////////////////////
    ///Async Task para guardar un evento en la BBDD///
    //////////////////////////////////////////////////////
    class SaveEvent extends AsyncTask<String, String, JSONObject> {

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage(getResources().getString(R.string.saving_event));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();

            try {
                HttpUploader uploader = new HttpUploader(getActivity().getResources(), BuildConfig.DOMAIN + Constantes.EDITAR_EVENTOS_FILE);

                uploader.añadirArgumento("fecha", event.getFecha());
                uploader.añadirArgumento("hora", event.getHora());
                uploader.añadirArgumento("titulo", event.getTitulo());


                uploader.añadirArgumento("description", event.getDescription());
                /////////////////////////////////////////////////

                if (event.getId() != null) { //parametro que pasamos al script para que cree uno nuevo o modifique uno ya existente
                    uploader.añadirArgumento("editmode", "1");
                    uploader.añadirArgumento("id", event.getId());
                } else {
                    uploader.añadirArgumento("editmode", "0");
                }

                //leemos la imagen del evento tamaño completo(si la tenemos es porque hemos
                //seleccionado una nueva
                if (event.getCartel() != null) {//la enviamos al servidor
                    byte[] imgByteArray = event.getCartel();
                    uploader.addImageAsByteArray("img", imgByteArray);
                } else {//si no la tenemos
                    if (event.getThumb() == null) {//y tampoco tenemo thumb es xq o no hay foto o el usuario quiere quitarla
                        uploader.añadirArgumento("delete_image", "1");
                    }
                }

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
            String mensaje;
            try {
                success = json.getInt(TAG_SUCCESS);
                mensaje = json.getString(TAG_MESSAGE);
                if (success == 1) {
                    Toast.makeText(getActivity(), mensaje, Toast.LENGTH_SHORT).show();
                    event.setId(json.getString("idevento"));
                    MostrarDialogoEnvioNotificacion();

                } else {
                    Toast.makeText(getActivity(), mensaje, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }
    }

}
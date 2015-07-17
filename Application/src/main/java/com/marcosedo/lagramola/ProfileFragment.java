package com.marcosedo.lagramola;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;


public class ProfileFragment extends Fragment implements OnClickListener {

    private static SharedPreferences preferences;
    private EditText etUsername;
    private ImageView ivImage;
    private Uri selectedImage = null;

    private ProgressDialog pDialog; // Progress Dialog

    private byte[] imageByteArray;

    private int SELECT_IMAGE = 1;
    private int TAKE_PICTURE = 2;

    private File fotoperfil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configurarToolbar();

        fotoperfil = new File(getActivity().getFilesDir() + "/images/" + Constantes.IMAGEN_PERFIL_FILENAME);
        preferences = getActivity().getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);

        //si esta guardada la foto de perfil en el telefono cargamos el imageByteArray
        if (fotoperfil.exists()) {
            imageByteArray = Utilidades.getByteArrayFromFile(fotoperfil);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            selectedImage = Uri.parse(savedInstanceState.getString("Uri"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedImage != null)
            outState.putString("Uri", selectedImage.toString());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("onCreateView Config", "Se mete en el onCreateView");

        View view = inflater.inflate(R.layout.perfil_fragment, container, false);

        //INICIALIZAMOS Vistas
        etUsername = (EditText) view.findViewById(R.id.etUname);
        ivImage = (ImageView) view.findViewById(R.id.ivProfileImage);
        Button btnSaveChanges = (Button) view.findViewById(R.id.btnSaveChanges);

        //PONEMOS EL LISTENER EN LOS BOTONES
        ivImage.setOnClickListener(this);
        btnSaveChanges.setOnClickListener(this);

        //carga el nombre de las sharedPref la foto leyendo el archivo devid.jpg del telefono
        loadProfile();


        return view;
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.ivProfileImage: {
                MostrarDialogoImagen();
                break;
            }
            case R.id.btnSaveChanges:
                new SaveProfile().execute();
                break;
        }
    }

    private void MostrarDialogoImagen() {
        try {
            final String[] items = {"Galería", "Cámara", "Eliminar foto"};
            final Integer[] icons = new Integer[]{R.drawable.ic_menu_gallery,
                    R.drawable.ic_menu_camera, R.drawable.ic_menu_delete};

            ArrayAdapterWithIcon adapter = new ArrayAdapterWithIcon(getActivity(), items, icons, null);

            new AlertDialog.Builder(getActivity()).setTitle("Foto de perfil")
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            Intent intent;
                            switch (item) {
                                case 0:
                                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                                    intent.setType("image/*");
                                    startActivityForResult(intent, SELECT_IMAGE);
                                    break;
                                case 1:
                                    Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    File file = new File(Environment.getExternalStorageDirectory(), "camera.jpg");
                                    selectedImage = Uri.fromFile(file);
                                    imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
                                    imageCaptureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                                    startActivityForResult(imageCaptureIntent, TAKE_PICTURE);

                                    break;

                                case 2:
                                    imageByteArray = null;
                                    ivImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_contact_picture));
                                    break;
                            }
                        }
                    }).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("onACtivityResul", "code = " + Integer.toString(requestCode));
        try {
            ContentResolver cr = getActivity().getContentResolver();

            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == SELECT_IMAGE) {
                    selectedImage = data.getData();

                }

                if (requestCode == TAKE_PICTURE) {
                    getActivity().getContentResolver().notifyChange(selectedImage, null);
                }

                Bitmap bmOriginal = MediaStore.Images.Media.getBitmap(cr, selectedImage);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmOriginal.compress(Bitmap.CompressFormat.JPEG, Constantes.JPEG_QUALITY_CARTEL, stream);
                Bitmap bitmap = BitmapFactory.decodeFile(Utilidades.getRealPathFromURI(cr, selectedImage));//nos guarfamos el path a la foto


                //GUARDAMOS EL BYTE ARRAY Y MOSTRAMOS LA IMAGEN EN EL IMAGEVIEW
                imageByteArray = stream.toByteArray();
                ivImage.setImageBitmap(bitmap);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Ponemos nuestros botones en la actionbar
    //onOptionsItemSelected esta en el main. asi tendremosjunto lo de todos los fragments
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (menu != null) {
            menu.setGroupVisible(R.id.tab_listaeventos_admin_group, false);
            menu.setGroupVisible(R.id.base_group, false);
        }
    }

    public void loadProfile() {

        //Cargamos nombre de usuario
        etUsername.setText(preferences.getString(Constantes.USERNAME, ""));

        if (fotoperfil.exists()) {
            ivImage.setImageBitmap(BitmapFactory.decodeFile(getActivity().getFilesDir() + "/images/" + Constantes.IMAGEN_PERFIL_FILENAME));
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        lostVisibility();
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

    //////////////////////////////////////////////////////
///Async Task para guardar el nombre de usuario en la BBDD///
//////////////////////////////////////////////////////
    class SaveProfile extends AsyncTask<String, String, JSONObject> {

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Guardando perfil ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }


        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();
            String username = etUsername.getText().toString();
            ///////////////////////////////////////////////////////////////////
            try {
                HttpUploader uploader = new HttpUploader(getActivity().getResources(), BuildConfig.DOMAIN + Constantes.SAVE_PROFILE);
                uploader.añadirArgumento("uname", username);
                uploader.añadirArgumento("devid", ((MainActivity) getActivity()).getDevid());

                if (imageByteArray != null) uploader.addImageAsByteArray("image", imageByteArray);

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


        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(JSONObject json) {
            // check for success tag
            int success;
            String mensaje;
            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);
                if (success == 1) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(Constantes.USERNAME, etUsername.getText().toString());
                    editor.apply();
                    Toast.makeText(getActivity(), mensaje, Toast.LENGTH_SHORT).show();

                    if (imageByteArray != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                        Utilidades.storeBitmap(bitmap, getActivity().getFilesDir() + "/images/", Constantes.IMAGEN_PERFIL_FILENAME, 0, 0);//bitmapAjsutado ya esta ajustado asi que le pasamos 0,0
                    } else {//borramos foto de perfil del telefono
                        if (fotoperfil.exists()) fotoperfil.delete();
                    }

                } else {
                    etUsername.setText(preferences.getString(Constantes.USERNAME, ""));
                    Toast.makeText(getActivity(), mensaje, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
            //finish();
        }

    }

}


    

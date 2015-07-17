package com.marcosedo.lagramola;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;


public class ListaEventosFragment extends Fragment {

    static final int FRAGMENT_GROUPID = 1;
    public static ListaEventosCustomAdapter adapter;
    private SharedPreferences preferences;
    private JSONArray eventos = null;
    private ProgressDialog pDialog;
    private ListView lv;
    private ProgressBar loadingAnimation;
    private ArrayList<Evento> listaEventos;
    private OnEditarEventoListener mCallback;
    private OnMostrarImagenListener mostrarImagenCallback;
    private OnMostrarDetalleEventoListener mostrarDetalleEventoCallback;
    private int itemposicion;
    private Menu optionsMenu;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList("LIST_INSTANCE_STATE", listaEventos);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);//if true onCreateOptionsMenu will be launched when creating options menu
        preferences = getActivity().getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("ListaEventosFragment", "onCreateView");
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.lista_eventos, container, false);
        lv = (ListView) view.findViewById(android.R.id.list);
        loadingAnimation = (ProgressBar) view.findViewById(R.id.loadingAnimation);
        registerForContextMenu(lv);

        /////////////////LIST VIEW Y ADAPTADOR/////////////////////
        listaEventos = new ArrayList<Evento>();//instanciamos la lv de eventos para que no pete
        adapter = new ListaEventosCustomAdapter(getActivity(), R.layout.linea_lista_eventos, listaEventos, new MyOnClickListener());

        lv.setAdapter(adapter);

        if (savedInstanceState != null) {
            listaEventos = savedInstanceState.getParcelableArrayList("LIST_INSTANCE_STATE");
            //adapter.setSelectedIndex(selectedPosition);//enviamos la posicion del que hay que remarcar
            adapter.notifyDataSetChanged();//este es para que refresque la lv//
        }
        if (listaEventos.isEmpty()) {
            new CargarEventos().execute();
        }


        //LISTENER DEL LIST VIEW esto nos saca el context menu con un solo click
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterview, View v, int position,
                                    long arg3) {
                v.showContextMenu();
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnEditarEventoListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnEditarEventoListener");
        }

        try {
            mostrarImagenCallback = (OnMostrarImagenListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMostrarImagenListener");
        }

        try {
            mostrarDetalleEventoCallback = (OnMostrarDetalleEventoListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMostrarDetalleEventoListener");
        }
    }

    @Override
    //-------------CONTEXT MENU---------------------------
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        Evento evento = listaEventos.get(info.position);
        //Cuando pulsamos nos aparece como título la fecha y la hora para saber cual seleccionamos
        menu.setHeaderTitle(evento.getFecha() + " " + evento.getTitulo());

        byte[] img = evento.getThumb();
        if (img != null) {
            ByteArrayInputStream is = new ByteArrayInputStream(img);
            menu.setHeaderIcon(Drawable.createFromStream(is, "cartel"));
        }
        //inflater.inflate(R.menu.menu_contextual_layout, menu);

        menu.add(FRAGMENT_GROUPID, R.id.action_view_image, 0, "Ver imagen");
        menu.add(FRAGMENT_GROUPID, R.id.action_view_detail, 1, "Ver detalle");

        //si somos supervaca
        if (BuildConfig.SUPERAPP) {
            menu.add(FRAGMENT_GROUPID, R.id.action_editarEvento, 2, "Editar evento");
            menu.add(FRAGMENT_GROUPID, R.id.action_borrarEvento, 3, "Borrar evento");
        }
    }

    //aqui tenemos las opciones de este fragment y en el main las comunes a todos los fragments
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getGroupId() == FRAGMENT_GROUPID) {

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

            switch (item.getItemId()) {
                case R.id.action_añadirEvento:
                    EditarEventosFragment editarFragment = new EditarEventosFragment();
                    transaction.replace(R.id.outTabContent, editarFragment, "editar evento");
                    transaction.addToBackStack(null);
                    transaction.commit();// Commit the transaction
                    break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (menu != null) {
            if (BuildConfig.SUPERAPP) {
                menu.setGroupVisible(R.id.tab_listaeventos_admin_group, true);
            } else {
                menu.setGroupVisible(R.id.tab_listaeventos_admin_group, false);
            }
            optionsMenu = menu;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ///editamos el evento asi que nos vamos a la pantalla del editor
        //y le pasamos el id del evento como argumento
        if (item.getGroupId() == FRAGMENT_GROUPID) {
            ContextMenuInfo menuInfo = item.getMenuInfo();
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            //se guarda la posicion para borrar de la lv directamente y no
            //tener que llamar a la base de datos de nuevo
            itemposicion = info.position;

            final Evento evento = listaEventos.get(itemposicion);
            ///editamos el evento asi que nos vamos a la pantalla del editor
            //y le pasamos el id del evento como argumento

            switch (item.getItemId()) {
                case R.id.action_view_image://verIMAGEN
                    mostrarImagenCallback.onMostrarImagen("cartel" + evento.getId() + ".jpg");
                    return true;

                case R.id.action_view_detail://ver DETALLES
                    mostrarDetalleEventoCallback.onMostrarDetalleEvento(evento);
                    return true;

                case R.id.action_editarEvento://editar evento

                    try {//PEdimos la imagen a tamaño completo
                        mCallback.onEditarEvento(evento);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;

                case R.id.action_borrarEvento://Borramos el evento

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("¿Desea borrar el evento?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Si",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    new BorrarEvento().execute(evento.getId());
                                }
                            });
                    builder.setNegativeButton("No", null);
                    builder.show();
                    return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    public void updateList() {

        showAll();

        showCategoriesDate();

        adapter.notifyDataSetChanged();
    }

    public void showAll() {
        for (int i = 0; i < listaEventos.size(); i++) {
            Evento evento = listaEventos.get(i);
            evento.setHided(false);
        }
    }


    public void showCategoriesDate() {
        Collections.sort(listaEventos, Evento.DateComparator);
        Evento evento;
        String new_cap = null;
        String old_cap = null;
        for (int i = 0; i < listaEventos.size(); i++) {
            evento = listaEventos.get(i);
            evento.setSeparatorText("");

            new_cap = evento.getFecha();


            if (old_cap == null) {//si es null es la primera asi que ponemos chapter
                evento.setSeparatorText(evento.getFecha());
            } else if (!(new_cap.matches(old_cap))) {//si el siguiente elemento  ha cambiado
                evento.setSeparatorText(evento.getFecha());
            }
            old_cap = new_cap;

        }
    }


    // Container Activity must implement this interface
    public interface OnEditarEventoListener {
        void onEditarEvento(Evento evento);
    }

    // Container Activity must implement this interface
    public interface OnMostrarImagenListener {
        void onMostrarImagen(String path);
    }

    // Container Activity must implement this interface
    public interface OnMostrarDetalleEventoListener {
        void onMostrarDetalleEvento(Evento evento);
    }


    //Mi onclicklistener para la estrellita
    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();

            /*switch (listaEventos.get(position).getFollowed()) {

                case 0:
                    //new FollowGroup().execute(listaEventos.get(position).getIdGrupo(), "follow");
                    break;
                case 1:
                    //new FollowGroup().execute(listaEventos.get(position).getIdGrupo(), "unfollow");
                    break;
            }*/
        }
    }


    /////////////////////////////////////////////////////
//////////Async Task para borrar un evento //////////
/////////////////////////////////////////////////////
    class BorrarEvento extends AsyncTask<String, String, JSONObject> {

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Borrando evento ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();

            try {
                HttpUploader uploader = new HttpUploader(getActivity().getResources(), BuildConfig.DOMAIN + Constantes.BORRAR_EVENTOS_FILE);
                uploader.añadirArgumento("_id", args[0]);
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

                if (success == 1) {
                    //si se ha borrado correctamente de la BBDD
                    //quitamos el item de la lv de eventos y rellenamos de nuevo el list view
                    //sin llamar al server
                    listaEventos.remove(itemposicion);
                    //adapter.setSelectedIndex(-1);
                    adapter.notifyDataSetChanged();
                    /*
                    /////////////////LIST VIEW Y ADAPTADOR/////////////////////
                    ListaEventosCustomAdapter adapter = new ListaEventosCustomAdapter(getActivity(),
                            R.layout.linea_lista_layout, listaEventos);
                    lv.setAdapter(adapter);
                    ////////////////////////////////////////7
                    */

                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.event_deleted), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), mensaje, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }
    }

    /////////////////////////////////////////////////////
///Async Task para obtener la lv eventos//////////
/////////////////////////////////////////////////////
    class CargarEventos extends AsyncTask<String, String, JSONObject> {

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingAnimation.setVisibility(View.VISIBLE);
        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();


            try {
                HttpUploader uploader = new HttpUploader(getActivity().getResources(), BuildConfig.DOMAIN + Constantes.LEER_EVENTOS_FILE);
                uploader.añadirArgumento("thumb_width", Integer.toString(Constantes.THUMB_SIZE));
                uploader.añadirArgumento("thumb_height", Integer.toString(Constantes.THUMB_SIZE));
                uploader.añadirArgumento("timezoneoffset", Utilidades.getTimeZoneOffset(getActivity()));

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


        protected void onPostExecute(JSONObject json) {
            Log.i("cargareventos", "on post execute");
            int success;
            String mensaje;

            loadingAnimation.setVisibility(View.GONE);

            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);

                if (success == 1) {//si fue bien
                    // Getting Array of Events
                    eventos = json.getJSONArray(Constantes.JSON_EVENTOS);
                    listaEventos.clear();

                    Boolean editable = preferences.getBoolean(Constantes.USER_IS_ADMIN, false);

                    // loop recorriendo los eventos
                    for (int i = 0; i < eventos.length(); i++) {

                        JSONObject c = eventos.getJSONObject(i);

                        String id = c.getString("_id");
                        String titulo = c.getString("titulo");
                        String description = c.getString("description");
                        String fecha = c.getString("fecha");
                        String hora = c.getString("hora");

                        String cartelBase64 = c.getString("thumb");

                        byte[] thumb = null;
                        if (cartelBase64.compareTo("") != 0) {
                            thumb = Base64.decode(cartelBase64, Base64.DEFAULT);
                        }

                        Evento evento = new Evento(id, null, thumb, titulo, description, fecha, hora);

                        evento.setEditable(editable);//esto es para que se pueda editar y cancelar el evento si somos los propietarios

                        listaEventos.add(evento);

                    }

                    updateList();

                } else {
                    Toast.makeText(getActivity(), mensaje, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}


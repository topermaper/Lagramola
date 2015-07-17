package com.marcosedo.lagramola;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

public class MostrarDetalleEventoFragment extends Fragment implements View.OnClickListener {
    private static final int FRAGMENT_GROUPID = 2;
    private Evento event;//THIS IS THE EVENT WHERE ARE SHOWING
    private TextView tvCartel;
    private ImageView ivCartel;
    private TextView tvFecha;
    private TextView tvHora;
    private TextView tvTitulo;
    private TextView tvDescription;
    private ProgressBar loadingAnimation;

    private Button btnPublish;
    private ListView listView;
    private ArrayList<Comment> listaComments;
    private ListaCommentsCustomAdapter commentsadapter;
    private SharedPreferences preferences;
    private TextView tvCountComments;
    private EditText etCommentTxt;
    private Button btnShowMore;
    private int itemposition;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        listaComments = new ArrayList<Comment>();
        preferences = getActivity().getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);
        commentsadapter = new ListaCommentsCustomAdapter(getActivity(), R.layout.linea_comments, listaComments);

        configurarToolbar();

        Bundle args = getArguments();
        if (args != null) {
            if (args.getParcelable("event") != null) {//this should happens when showing details of a event
                event = args.getParcelable("event");
                //new getImage().execute("cartel" + event.getId() + ".jpg");
            } else {
                if (args.getString("idevento") != null) {//si ha llegado una notificacion con la id del evento que hay que enseñar
                    new CargarEvento().execute(args.getString("idevento"));
                }
            }

            new LoadComments().execute(Integer.toString(Constantes.NUMBER_MAX_COMMENTS_SHOW));

        }


        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("onCreateView", "onCreate mostrar detalle");

        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.mostrar_detalle_evento, container, false);
        View header = getActivity().getLayoutInflater().inflate(R.layout.mostrar_detalle_evento_listheader, null);
        View footer = getActivity().getLayoutInflater().inflate(R.layout.mostrar_detalle_evento_listfooter, null);

        /////////////////LIST VIEW and ADAPTADER/////////////////////
        listView = (ListView) view.findViewById(android.R.id.list);
        listView.addHeaderView(header);
        listView.addFooterView(footer);
        listView.setAdapter(commentsadapter);

        registerForContextMenu(listView);


        /////////////////INITIALIZING VIEWS///////////////////////////
        tvCartel = (TextView) header.findViewById(R.id.tvCartel);
        ivCartel = (ImageView) header.findViewById(R.id.ivCartel);
        loadingAnimation = (ProgressBar) header.findViewById(R.id.loadingAnimation);

        tvFecha = (TextView) header.findViewById(R.id.tvFecha2);
        tvHora = (TextView) header.findViewById(R.id.tvHora2);
        tvTitulo = (TextView) header.findViewById(R.id.tvTitulo2);
        tvDescription = (TextView) header.findViewById(R.id.tvDescription2);
        tvCountComments = (TextView) header.findViewById(R.id.tvCountComments);
        etCommentTxt = (EditText) header.findViewById(R.id.etCommentTxt);
        btnPublish = (Button) header.findViewById(R.id.btnPublish);
        btnShowMore = (Button) footer.findViewById(R.id.btnShowMore);

        //////////////LISTENER///////////////////////////////
        btnShowMore.setOnClickListener(this);
        btnPublish.setOnClickListener(this);//listener del boton de publicar comment
        //LISTENER DEL LIST VIEW esto nos saca el context menu con un solo click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterview, View v, int position,
                                    long arg3) {
                v.showContextMenu();
            }
        });

        setEventsFields();//Fill the form using our event (Event event)

        return view;


    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        itemposition = info.position;
        if (itemposition > 0) itemposition--;//hay que restar 1 porque la lista tiene un header

        Comment comment = listaComments.get(itemposition);
        //Cuando pulsamos nos aparece como título la fecha y la hora para saber cual seleccionamos
        final int MAXLENGHT = 10;
        String txt = comment.getTxt();
        if (txt.length() > MAXLENGHT) {//truncamos por la longitud maxima
            txt = txt.substring(0, MAXLENGHT) + " ...";
        }
        menu.setHeaderTitle(comment.getUsername() + " : \"" + txt + "\"");

        byte[] img = comment.getImage();
        if (img != null) {
            ByteArrayInputStream is = new ByteArrayInputStream(img);
            menu.setHeaderIcon(Drawable.createFromStream(is, "image"));
        }

        //si somos supervaca o somos los dueños del mensaje
        if ((BuildConfig.SUPERAPP) || (comment.getDevid() == Utilidades.getDevId(getActivity()))) {
            menu.add(FRAGMENT_GROUPID, R.id.action_borrar_comentario, 0, "Borrar comentario");//podremos borrar
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case (R.id.btnPublish): {

                if ((preferences.getString(Constantes.USERNAME, "").compareTo("") == 0)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Para poder publicar comentarios necesita un nombre de usuario.\n" +
                            "¿Desea establecer un nombre de usuario ahora?");
                    builder.setCancelable(false);
                    builder.setTitle("Necesita un nombre de usuario");

                    builder.setPositiveButton("Si",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ProfileFragment fragment = new ProfileFragment();
                                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                    transaction.replace(R.id.outTabContent, fragment, "perfil de usuario");
                                    transaction.addToBackStack(null);
                                    transaction.commit();// Commit the transaction
                                }
                            });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                } else {
                    if (etCommentTxt.getText().toString().trim().compareTo("") == 0) {
                        Toast.makeText(getActivity(), "No escribió nada que publicar", Toast.LENGTH_SHORT).show();
                    } else {
                        new PublishComment().execute();
                    }
                }
                break;
            }
            case (R.id.btnShowMore): {
                new LoadComments().execute();
                break;
            }

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

    public void addCommentToList(Comment comment) {
        listaComments.add(0, comment);
        commentsadapter.notifyDataSetChanged();
        if (listaComments.size() == 1) {
            tvCountComments.setText(Integer.toString(listaComments.size()) + " comentario");
        } else {
            tvCountComments.setText(Integer.toString(listaComments.size()) + " comentarios");
        }
    }

    public void removeCommentFromList(int pos) {
        listaComments.remove(itemposition);
        commentsadapter.notifyDataSetChanged();
        if (listaComments.size() == 1) {
            tvCountComments.setText(Integer.toString(listaComments.size()) + " comentario");
        } else {
            tvCountComments.setText(Integer.toString(listaComments.size()) + " comentarios");
        }
    }


    public void setEventsFields() {

        if (event != null) {
            if (event.getFecha() != null) tvFecha.setText(event.getFecha());
            if (event.getHora() != null) tvHora.setText(event.getHora());
            if (event.getTitulo() != null) tvTitulo.setText(event.getTitulo());
            if (event.getDescription() != null) tvDescription.setText(event.getDescription());

            //Puts the banner image
            //First we try to get the good resolution image (getCartel())
            //if is not available we get the thumb
            //if not we hide the picture and we don't show anything
            byte[] image = null;

            if (event.getCartel() != null) {
                loadingAnimation.setVisibility(View.GONE);
                image = event.getCartel();

            } else {//si no hay imagen tamaño completo lo conseguimos
                loadingAnimation.setVisibility(View.VISIBLE);
                new getImage().execute("cartel" + event.getId() + ".jpg");
                if (event.getThumb() != null) {
                    image = event.getThumb();
                }
            }

            if (image != null) {
                ivCartel.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
                ivCartel.setVisibility(View.VISIBLE);
                tvCartel.setVisibility(View.VISIBLE);
            } else {
                ivCartel.setImageBitmap(null);
                ivCartel.setVisibility(View.GONE);
                tvCartel.setVisibility(View.GONE);
            }
        } else {
            loadingAnimation.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.e("context", "entra a la seleccion");

        if (item.getGroupId() == FRAGMENT_GROUPID) {
            ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            //se guarda la posicion para borrar de la lv directamente y no
            //tener que llamar a la base de datos de nuevo
            itemposition = info.position;
            if (itemposition > 0) itemposition--;//hay que restar 1 porque la lista tiene un header

            final Comment comentario = listaComments.get(itemposition);

            switch (item.getItemId()) {
                ///editamos el evento asi que nos vamos a la pantalla del editor
                //y le pasamos el id del evento como argumento

                case R.id.action_borrar_comentario://Borramos el evento

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("¿Desea borrar el comentario?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new BorrarComentario().execute(comentario.getId());
                        }
                    });
                    builder.setNegativeButton("No", null);
                    builder.show();

                    return true;
            }
        }
        return super.onContextItemSelected(item);
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

    /////////////////////////////////////////////////////
//////////Async Task para borrar un evento //////////
/////////////////////////////////////////////////////
    class BorrarComentario extends AsyncTask<String, String, JSONObject> {
        ProgressDialog pDialog;

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Borrando ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();

            try {
                HttpUploader uploader = new HttpUploader(getActivity().getResources(), BuildConfig.DOMAIN + Constantes.BORRAR_COMENTARIO_FILE);
                uploader.añadirArgumento("id", args[0]);
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

                    removeCommentFromList(itemposition);

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

    class LoadComments extends AsyncTask<String, String, JSONObject> {
        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();

            /////////////////////////////////////////////////////
            try {
                HttpUploader uploader = new HttpUploader(getActivity().getResources(), BuildConfig.DOMAIN + Constantes.LOAD_MESSAGES_FILE);
                uploader.añadirArgumento("idevento", event.getId());//le pasamos el codigo del evento del cual estamos mostrando el detalle
                uploader.añadirArgumento("thumb_width", Float.toString(Constantes.THUMB_SIZE));
                uploader.añadirArgumento("thumb_height", Float.toString(Constantes.THUMB_SIZE));
                uploader.añadirArgumento("timezoneoffset", Utilidades.getTimeZoneOffset(getActivity()));

                if ((args != null) && (args.length > 0)) {
                    uploader.añadirArgumento("numerocomments", args[0]);
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

        protected void onPostExecute(JSONObject json) {
            int success;
            String mensaje;
            int totalcount;

            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);

                if (success == 1) {//si fue bien
                    totalcount = json.getInt("totalcount");
                    JSONArray messageList = json.getJSONArray("listmessages");
                    listaComments.clear();

                    for (int i = 0; i < messageList.length(); i++) {//se supone que solo tiene que haber uno

                        JSONObject message = messageList.getJSONObject(i);
                        String id = message.getString("id");
                        String username = message.getString("username");
                        String txt = message.getString("msg");
                        String timestamp = message.getString("timestamp");
                        String cartelBase64 = message.getString("image");
                        String devid = message.getString("devid");
                        byte[] image = Base64.decode(cartelBase64, Base64.DEFAULT);

                        Comment comment = new Comment(image, txt, username, timestamp, devid, id);
                        comment.print("comment = ");
                        listaComments.add(comment);

                    }
                    commentsadapter.notifyDataSetChanged();

                    /////PONEMOS VISIBILIDAD ADECUADA AL BOTON DE SHOW MORE COMMENTS
                    Toast.makeText(getActivity(), Integer.toString(totalcount), Toast.LENGTH_LONG);
                    if (totalcount > Constantes.NUMBER_MAX_COMMENTS_SHOW) {//si es mayor que el limite maximo que queremos cargar
                        if (totalcount == listaComments.size())
                            btnShowMore.setVisibility(View.GONE);//
                        else btnShowMore.setVisibility(View.VISIBLE);
                    } else btnShowMore.setVisibility(View.GONE);
                    /////////////////////////////////////////////////7

                    if (listaComments.size() == 1) {
                        tvCountComments.setText(Integer.toString(messageList.length()) + " comentario");
                    } else {
                        tvCountComments.setText(Integer.toString(messageList.length()) + " comentarios");
                    }

                } else {
                    Toast.makeText(getActivity(), mensaje, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class CargarEvento extends AsyncTask<String, String, JSONObject> {

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();

            try {
                HttpUploader uploader = new HttpUploader(getActivity().getResources(), BuildConfig.DOMAIN + Constantes.LEER_EVENTOS_FILE);
                uploader.añadirArgumento("_id", args[0]);//le pasamos el codigo del evento del cual estamos mostrando el detalle
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
            }
            return json;
        }


        protected void onPostExecute(JSONObject json) {
            Log.i("cargareventos", "on post execute");
            int success;
            String mensaje;

            try {

                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);


                if (success == 1) {//si fue bien
                    event = new Evento();
                    // Getting Array of Events
                    JSONArray listaEventos = json.getJSONArray(Constantes.JSON_EVENTOS);

                    // loop recorriendo los eventos
                    for (int i = 0; i < listaEventos.length(); i++) {//se supone que solo tiene que haber uno

                        JSONObject evento = listaEventos.getJSONObject(i);

                        event.setId(evento.getString("_id"));
                        event.setFecha(evento.getString("fecha"));
                        event.setHora(evento.getString("hora"));
                        event.setTitulo(evento.getString("titulo"));
                        event.setDescription(evento.getString("description"));


                        byte[] image;
                        String cartelBase64 = evento.getString("cartel");
                        if (cartelBase64.compareTo("") != 0) {
                            image = Base64.decode(cartelBase64, Base64.DEFAULT);
                            event.setCartel(image);
                        }

                    }

                    setEventsFields();//Fill the form using our event (Event event)

                } else {
                    Toast.makeText(getActivity(), mensaje, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class PublishComment extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Publicando ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();

            try {
                HttpUploader uploader = new HttpUploader(getActivity().getResources(), BuildConfig.DOMAIN + Constantes.PUBLISH_COMMENT);
                uploader.añadirArgumento("idevento", event.getId());//le pasamos el codigo del evento del cual estamos mostrando el detalle
                uploader.añadirArgumento("message", etCommentTxt.getText().toString().trim());
                uploader.añadirArgumento("devid", Utilidades.getDevId(getActivity()));
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
            }
            return json;
        }

        protected void onPostExecute(JSONObject json) {
            int success;
            String mensaje;
            byte[] imageByteArray = null;

            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);

                if (success == 1) {//si fue bien

                    JSONObject jsoncomment = json.getJSONObject("comment");

                    String imagepath = getActivity().getFilesDir() + "/images/" + Constantes.IMAGEN_PERFIL_FILENAME;
                    File imagefile = new File(imagepath);

                    if (imagefile.exists()) {


                        Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
                        bitmap = Utilidades.redimensionarBitmap(bitmap, Constantes.THUMB_SIZE, Constantes.THUMB_SIZE);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        imageByteArray = stream.toByteArray();
                    }


                    Comment comment = new Comment(imageByteArray, jsoncomment.getString("msg"), jsoncomment.getString("username"),
                            jsoncomment.getString("timestamp"), Utilidades.getDevId(getActivity()), jsoncomment.getString("id"));
                    addCommentToList(comment);
                    commentsadapter.notifyDataSetChanged();
                    etCommentTxt.setText("");//borramos el edittext

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
    ///Async Task para obtener la imagen que queremos mostrar
    /////////////////////////////////////////////////////
    class getImage extends AsyncTask<String, String, JSONObject> {


        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();

            try {
                HttpUploader uploader = new HttpUploader(getActivity().getResources(), BuildConfig.DOMAIN + Constantes.GET_IMAGE_FILE);
                uploader.añadirArgumento("image", args[0]);//args[0] is the name of the image we're trying to get
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
                    event.setCartel(image_byte);

                    setEventsFields();
                    //ivCartel.setImageBitmap(BitmapFactory.decodeByteArray(image_byte, 0, image_byte.length));

                } else {
                    Toast.makeText(getActivity(), mensaje, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
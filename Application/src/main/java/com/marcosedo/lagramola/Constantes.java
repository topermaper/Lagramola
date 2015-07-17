package com.marcosedo.lagramola;


public final class Constantes {
    public static final String IMAGEN_PERFIL_FILENAME = "fotoperfil.jpeg";
    public static final String MY_EMAIL = "marcosedoatienza@gmail.com";
    public static final String GCM_REGISTER_ALREADY_DONE = "isgmregisterdone";
    public static final String USER_IS_ADMIN = "userisadmin";

    public static final String SENDER_ID = "535741840138";//este es el project ID de la consola google api

    public static final String REG_ID = "registration_id";
    public static final String APP_VERSION = "appVersion";
    public static final String USERNAME = "username";
    public static final Integer THUMB_SIZE = 64;

    public static final int NUMBER_MAX_COMMENTS_SHOW = 10;//numero maximo de comments que mostraremos

    //URL
    public static final String GCMSERVER_FILE = "gcmserver_main.php";
    public static final String LEER_EVENTOS_FILE = "leer_eventos.php";

    public static final String LEER_USUARIOS_FILE = "leer_usuarios.php";
    public static final String BORRAR_EVENTOS_FILE = "borrar_evento.php";
    public static final String LOAD_MESSAGES_FILE = "load_messages.php";
    public static final String BORRAR_COMENTARIO_FILE = "borrar_comentario.php";
    public static final String GUARDAR_FOTOPERFIL = "guardar_foto_perfil.php";
    public static final String SAVE_PROFILE = "save_profile.php";
    public static final String PUBLISH_COMMENT = "publish_comment.php";
    public static final String EDITAR_EVENTOS_FILE = "editar_eventos.php";
    public static final String GET_IMAGE_FILE = "get_image.php";




    // JSON Node names
    public static final String JSON_SUCCESS = "success";
    public static final String JSON_ERROR = "error";
    public static final String JSON_MESSAGE = "message";
    public static final String JSON_EVENTOS = "eventos";

    //PERFIL DE USUARIO
    public static final int MAX_NICK_LENGHT = 15;
    public static final int JPEG_QUALITY_FOTO = 90;
    public static final int JPEG_QUALITY_STORED_IMAGES = 60;
    public static final int MAX_WIDTH_FOTO = 640;
    public static final int MAX_HEIGHT_FOTO = 480;
    public static final int RADIOPERCENT = 25;


    //CARTEL ACTUACION
    public static final int JPEG_QUALITY_CARTEL = 50;
    public static final int MAX_WIDTH_CARTEL = 640;
    public static final int MAX_HEIGHT_CARTEL = 480;

    //FECHA Y HORA
    public static final int DIA = 0;
    public static final int MES = 1;
    public static final int AÑO = 2;
    public static final int HOR = 0;
    public static final int MIN = 1;

    //Nombre de SharedPreferences
    public static final String SP_FILE = "gramolaSharedPreferences";

}
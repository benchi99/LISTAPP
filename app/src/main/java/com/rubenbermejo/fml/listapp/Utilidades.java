package com.rubenbermejo.fml.listapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Utilidades {

    Context context;
    ArrayList<ObjetoSetas> datos;

    public Utilidades (Context context) {
        this.context = context;
    }

    //Atributos
    final public static String NOMBRE_COLUMNA = "nombre";
    final public static String DESCRIPCION_COLUMNA = "descripcion";
    final public static String NOMBRECOMUN_COLUMNA = "nombre_comun";
    final public static String URLLINEA_COLUMNA = "url";
    final public static String COMESTIBLE_COLUMNA = "comestible";
    final public static String ID_COLUMNA = "id";
    final public static String FAV_COLUMNA = "favorito";
    final public static String IMG_COLUMNA = "imagen";
    final public static String DIRECCION_REST_LOCAL = "http://localhost/ServicioREST";
    final public static String DIRECCION_REST_DATAUS = "http://datauschwitz-se/practicasRuben/ServicioREST";
    final public static String DIRECCION_REST_MARISMA = "http://dam2.ieslamarisma.net/2019/rubenbermejo";
    final public static String POST_GET_ALL = "/setas";
    final public static String GET_FAV = "/setas/favoritos";
    final public static String GET_PUT_DELETE_ID = "/setas/";


    final public static String NORMAL = "normal";
    final public static String FAVORITOS = "favorito";


    public static ArrayList<ObjetoSetas> obtenerListaMasReciente(Context context, String param) {

        if (param.equals(NORMAL)){

        } else {

        }

        ArrayList<ObjetoSetas> listActual = new ArrayList<>();
        ObjetoSetas seta;

    return listActual;
   }

    public static byte[] convertirImagenABytes(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(175000);
        bmp.compress(Bitmap.CompressFormat.PNG, 0, baos);
        byte[] blob = baos.toByteArray();
        return blob;
    }

    public static Bitmap convertirBytesAImagen(byte[] blob) {
        Bitmap bmp;
        ByteArrayInputStream bais = new ByteArrayInputStream(blob);
        bmp = BitmapFactory.decodeStream(bais);
        return bmp;
    }

    private static boolean intToBool(int val) {

        if (val == 0) {
            return false;
        } else {
            return true;
        }

    }

    public static void delElement(Context context, int id){

    }

}

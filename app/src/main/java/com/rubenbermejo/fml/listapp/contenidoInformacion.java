package com.rubenbermejo.fml.listapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.MenuRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class contenidoInformacion extends AppCompatActivity {

    TextView tvNombre, tvComestibilidad, tvDescripcion, tvNombreComun;
    ImageView imageView;
    int setaId;
    ObjetoSetas setaRecibida;
    HttpClient httpClient = new DefaultHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contenido_informacion);

        tvNombre = findViewById(R.id.tvNombre);
        tvComestibilidad = findViewById(R.id.tvComestibilidad);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvNombreComun = findViewById(R.id.tvNombreComun);
        imageView = findViewById(R.id.imageView);

        setaId = getIntent().getIntExtra("setaId", 0);
        new ObtenSeta().execute(setaId);

        setaRecibida = new ObjetoSetas("Cargando", "Espere, por favor...", "Cargando...", null, false, null);
        cargaInfoEnUI(setaRecibida);
    }

    @Override
    public void onBackPressed() {
        Intent intentDevolver = new Intent();
        Bundle bundleDevuelve = new Bundle();
        bundleDevuelve.putSerializable("devolverSeta", setaRecibida);
        intentDevolver.putExtras(bundleDevuelve);
        setResult(Activity.RESULT_OK, intentDevolver);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contenidoinformacion_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.togglerFav:       //Pone o quita los favoritos.
                //PUT
                break;
            case R.id.share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT ,setaRecibida.toString());
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
                break;
            case R.id.verWeb:
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(setaRecibida.getURLlinea()));
                if (webIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(webIntent);
                }
                break;
            case R.id.enviarCorreo:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, setaRecibida.getNombre());
                emailIntent.putExtra(Intent.EXTRA_TEXT, setaRecibida.getDescripcion());
                if(emailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(emailIntent);
                }
                break;
            case R.id.delSeta:
                AlertDialog.Builder adB = new AlertDialog.Builder(this);
                adB.setTitle(R.string.deleteElement);
                adB.setMessage(R.string.delElementDescr);

                adB.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new EliminarSeta().execute(setaId);
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                });
                adB.setNegativeButton(R.string.no, null);
                AlertDialog dialogo = adB.create();
                dialogo.show();
                break;
            case R.id.modSeta:
                Intent modificar = new Intent(contenidoInformacion.this, editarSeta.class);
                modificar.putExtra("setaMod", setaRecibida);
                startActivityForResult(modificar, 1);

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Establece el título de la seta escogida en el Action Bar.
     *
     * @param tituloActividad
     */

    private void customizaActionBar(String tituloActividad) {
        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle(tituloActividad);
    }

    /**
     * Carga la información de un ObjetoSetas en
     * la interfaz.
     *
     * @param seta Seta a cargar.
     */
    private void cargaInfoEnUI(ObjetoSetas seta) {
        tvNombre.setText(seta.getNombre());
        tvDescripcion.setText(seta.getDescripcion());
        tvNombreComun.setText(seta.getnombreComun());
        if (seta.getComestible()) {
            tvComestibilidad.setText(R.string.edible);
            tvComestibilidad.setTextColor(Color.GREEN);
        } else {
            tvComestibilidad.setText(R.string.notEdible);
            tvComestibilidad.setTextColor(Color.RED);
        }
        imageView.setImageBitmap(seta.getImg());

        customizaActionBar(seta.getNombre());
    }

    /**
     * Obtiene una seta del servicio REST ubicado
     * en dam2.ieslamarisma.net/2019/rubenbermejo
     * según identificador especificado.
     *
     */

    private class ObtenSeta extends AsyncTask<Integer, Integer, ObjetoSetas> {
        @Override
        protected ObjetoSetas doInBackground(Integer... integers) {
            HttpGet getSeta = new HttpGet(Utilidades.DIRECCION_REST_MARISMA + Utilidades.GET_PUT_DELETE_ID + String.valueOf(integers[0]));
            getSeta.setHeader("content-type", "application-json");

            try {
                HttpResponse respuesta = httpClient.execute(getSeta);
                String strResp = EntityUtils.toString(respuesta.getEntity());

                JSONObject json = new JSONObject(strResp);
                ObjetoSetas seta = new ObjetoSetas(json.getString("NOMBRE"), json.getString("DESCRIPCION"), json.getString("NOMBRE_COMUN"), json.getString("URL"), Utilidades.intToBool(json.getInt("COMESTIBLE")), Utilidades.IMG_LOCATION + json.getString("IMAGEN"));
                String imagen = Utilidades.DIRECCION_REST_MARISMA + Utilidades.IMG_LOCATION + json.getString("IMAGEN");
                System.out.println(imagen);
                Bitmap imgAMostrar;
                try {
                    InputStream ins = (InputStream) new URL(imagen).getContent();
                    imgAMostrar = BitmapFactory.decodeStream(ins);
                    ins.close();
                } catch (MalformedURLException badURLe) {
                    Log.e("REST API - IMAGEN", "La URL que ha sido dada está mal formada.");
                    imgAMostrar = null;
                    badURLe.printStackTrace();
                } catch (IOException ioe) {
                    Log.e("REST API - IMAGEN", "Hubo un error al descargar " + imagen);
                    imgAMostrar = null;
                    ioe.printStackTrace();
                }
                seta.setImg(imgAMostrar);
                seta.setId(json.getInt("ID"));
                seta.setFavorito(Utilidades.intToBool(json.getInt("FAVORITO")));

                return seta;
            } catch (IOException ioe) {
                Log.e("REST API", "Error al realizar peticion GET " + Utilidades.DIRECCION_REST_MARISMA + Utilidades.GET_PUT_DELETE_ID + String.valueOf(integers[0]) + ".");
                ioe.printStackTrace();
                return null;
            } catch (JSONException jsone) {
                Log.e("REST API", "Error a la hora de leer JSON.");
                jsone.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ObjetoSetas objetoSetas) {
            super.onPostExecute(objetoSetas);
            cargaInfoEnUI(objetoSetas);
        }
    }

    private class EliminarSeta extends AsyncTask<Integer, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... integers) {
            HttpDelete del = new HttpDelete(Utilidades.DIRECCION_REST_MARISMA + Utilidades.GET_PUT_DELETE_ID + String.valueOf(integers[0]));

            del.setHeader("content-type", "application/json");

            try {
                HttpResponse respuesta = httpClient.execute(del);
                String respStr = EntityUtils.toString(respuesta.getEntity());

                if (!respStr.equals("true")){
                    return false;
                }
            } catch (IOException ioe) {
                Log.e("REST API", "Error al realizar petición DELETE! " + ioe.getMessage());
                return false;
            }
            return true;
        }
    }
}

package com.marcosedo.lagramola;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Marcos on 29/06/15.
 */
public class ComentariosPendientes {


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private  String path;
    private ArrayList<Comment> comentariosPendientes;

    public ComentariosPendientes() {
        comentariosPendientes = new ArrayList<Comment>();
    }

    public ArrayList<Comment> getComentariosPendientes() {
        return comentariosPendientes;
    }

    public void setComentariosPendientes(ArrayList<Comment> comentariosPendientes) {
        this.comentariosPendientes = comentariosPendientes;
    }

    public void addComment(Comment comment) {
        comentariosPendientes.add(comment);
    }

    public void saveToFile(String path) {

        File file = new File(path);

        JSONArray jsonArray = new JSONArray(comentariosPendientes);
        String jsonString = jsonArray.toString();

        try
        {
            FileOutputStream fop = new FileOutputStream(file);
            // if file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // get the content in bytes
            byte[] contentInBytes = jsonString.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

            System.out.println("Archivo de comentarios pendientes guardado");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile(){

        File file = new File(path);
        String jsonString;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            jsonString = stringBuilder.toString();

            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject Object = jsonArray.getJSONObject(i);
            }



        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

}

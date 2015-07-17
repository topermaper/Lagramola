package com.marcosedo.lagramola;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;


public class Evento implements Parcelable {
    public static final Creator<Evento> CREATOR =
            new Creator<Evento>() {
                @Override
                public Evento createFromParcel(Parcel parcel) {
                    return new Evento(parcel);
                }

                @Override
                public Evento[] newArray(int size) {
                    return new Evento[size];
                }
            };
    private String id;
    private byte[] cartel;
    private byte[] thumb; //imagen reducida para no usar tanta red
    private String titulo;
    private String description;
    private String fecha;
    private String hora;

    public static Comparator<Evento> DateComparator = new Comparator<Evento>() {

        @Override
        public int compare(Evento e1, Evento e2) {//Orden ascendentemente for fecha y luego por hora en caso de empate

            final int DIA = 0;
            final int MES = 1;
            final int AÑO = 2;
            final int HOR = 0;
            final int MIN = 1;

            //cogemos la fecha en string y la pasamos a entero para comparar mejor
            String[] fechaString1 = e1.getFecha().split("/");
            String[] fechaString2 = e2.getFecha().split("/");

            Integer[] fechaInt1 = new Integer[3];
            Integer[] fechaInt2 = new Integer[3];

            for (int i = 0; i < 3; i++) {
                fechaInt1[i] = Integer.parseInt(fechaString1[i]);
                fechaInt2[i] = Integer.parseInt(fechaString2[i]);
            }
            ///////////////////////////////////////////////////////////////////
            //cogemos la hora en string y la pasamos a entero para comparar mejor
            String[] horaString1 = e1.getHora().split(":");
            String[] horaString2 = e1.getHora().split(":");

            Integer[] horaInt1 = new Integer[2];
            Integer[] horaInt2 = new Integer[2];

            for (int i = 0; i < 2; i++) {
                horaInt1[i] = Integer.parseInt(horaString1[i]);
                horaInt2[i] = Integer.parseInt(horaString2[i]);
            }
            ///////////////////////////////////////////////////////////////////

            if (fechaInt1[AÑO].compareTo(fechaInt2[AÑO]) == 0) {
                if (fechaInt1[MES].compareTo(fechaInt2[MES]) == 0) {
                    if (fechaInt1[DIA].compareTo(fechaInt2[DIA]) == 0) {
                        if (horaInt1[HOR].compareTo(horaInt2[HOR]) == 0) {
                            return horaInt1[MIN].compareTo(horaInt2[MIN]);
                        } else {
                            return horaInt1[HOR].compareTo(horaInt2[HOR]);
                        }
                    } else {
                        return fechaInt1[DIA].compareTo(fechaInt2[DIA]);
                    }
                } else {
                    return fechaInt1[MES].compareTo(fechaInt2[MES]);
                }
            } else {
                return fechaInt1[AÑO].compareTo(fechaInt2[AÑO]);
            }
        }
    };
    private String separatorText;
    private boolean hided;//if TRUE this event won't be shown
    private boolean editable;//it would be editable if we are the creator
    public Evento() {
        this.id = null;
        this.cartel = null;
        this.thumb = null;
        this.titulo = "";
        this.description = "";
        this.fecha = null;
        this.hora = null;

        this.separatorText = "";
        this.hided = false;
        this.editable = false;
    }


    public Evento(String id, byte[] cartel, byte[] thumb, String titulo, String description,
                  String fecha, String hora) {
        this.id = id;
        this.cartel = cartel;
        this.thumb = thumb;
        this.titulo = titulo;
        this.description = description;
        this.fecha = fecha;
        this.hora = hora;
    }
    //CONSTRUCTOR PARCELABLE
    public Evento(Parcel parcel) {
        //seguir el mismo orden que el usado en el método writeToParcel
        this.id = parcel.readString();
        this.cartel = (byte[]) parcel.readSerializable();
        this.thumb = (byte[]) parcel.readSerializable();
        this.titulo = parcel.readString();
        this.description = parcel.readString();
        this.fecha = parcel.readString();
        this.hora = parcel.readString();
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeByteArray(cartel);
        parcel.writeByteArray(thumb);
        parcel.writeString(titulo);
        parcel.writeString(description);
        parcel.writeString(fecha);
        parcel.writeString(hora);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getCartel() {
        return cartel;
    }

    public void setCartel(byte[] cartel) {
        this.cartel = cartel;
    }

    public byte[] getThumb() {
        return thumb;
    }

    public void setThumb(byte[] thumb) {
        this.thumb = thumb;
    }


    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }


    public String getSeparatorText() {
        return separatorText;
    }

    public void setSeparatorText(String text) {
        this.separatorText = text;
    }

    public boolean getHided() {
        return hided;
    }

    public void setHided(boolean hided) {
        this.hided = hided;
    }

    public boolean getEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }


}
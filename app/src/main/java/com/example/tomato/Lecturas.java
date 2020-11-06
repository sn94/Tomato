package com.example.tomato;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class Lecturas extends AppCompatActivity {



    TextView  valor_actual_humedad= null;
    TextView  valor_actual_temperatura= null;
    TextView  valor_actual_madurez= null;





    //Identificador de servicio
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    //Si se apreta una vez el boton de conectar
    boolean estado = false;
    //Handler es un control para mensajes
    Handler bluetoothIn;
    //Estado del manejador
    final int handlerState = 0;
    //Esto es simplemente un String normal a diferencia que al agregar una sentancia en un bucle se agrega los espacios automaticamente
//for(hasta 20 veces)
//String cadena += " " + "Dato" ---> En un string normal se debe crear el espacio y luego agregar el dato
//Con esto se traduce a = DataStringIN.append(dato);
    private StringBuilder DataStringIN = new StringBuilder();
    //Llama a la sub- clase y llamara los metodos que se encuentran dentro de esta clase
    ConexionThread MyConexionBT;




    //vALORES A MOSTRAR


    String Humedad_Show="", Temperatura_Show= "",  Madurez_Show= "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lectura);

        valor_actual_humedad= (TextView) findViewById( R.id.VALOR_ACTUAL_HUMEDAD);
        valor_actual_temperatura= (TextView) findViewById( R.id.VALOR_ACTUAL_TEMPERATURA);
        valor_actual_madurez= (TextView) findViewById( R.id.VALOR_MADUREZ);


        ////////////////Manejador de mensajes y llamara al metodo Run///////////////////////////////
        bluetoothIn = new Handler(){
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                  //  Toast.makeText(Humedad.this, "Dato Recibido Entero: " + readMessage, Toast.LENGTH_SHORT).show();
                    DataStringIN.append(readMessage);

                    int endOfLineIndex = DataStringIN.indexOf("\n");

                    if (endOfLineIndex > 0) {
                        String dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                        //   Toast.makeText(MainActivity.this, "Dato Recibido: " +dataInPrint, Toast.LENGTH_SHORT).show();
                        DataStringIN.delete(0, DataStringIN.length());
                    }
                }
            } };


    }










    ///////////////////////////////////////////////////


    //BOTON ENVIAR
   public void enviar( View v){
       if(estado ) {
           String dato = "Dato prueba";
           dato += "#";
           MyConexionBT.write(dato);
           Toast.makeText(Lecturas.this, "Dato Enviado: " + dato, Toast.LENGTH_SHORT).show();
       }


       else {
           Toast.makeText(Lecturas.this, "Solo se puede enviar datos si el dispositivo esta vinculado", Toast.LENGTH_SHORT).show();
       }
   }


    //BOTON CONECTAR
   public void conectar( View v){
       btAdapter = BluetoothAdapter.getDefaultAdapter();
       //Direccion mac del dispositivo a conectar
       BluetoothDevice device = btAdapter.getRemoteDevice("00:13:EF:00:A8:7E");
       try
       {
           //Crea el socket sino esta conectado
           if(!estado)
           {  btSocket = createBluetoothSocket(device);
               estado = btSocket.isConnected();
           }
       }
       catch (IOException e)
       {  Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
       }

       // Establece la conexión con el socket Bluetooth.
       try
       {
           //Realiza la conexion si no se a hecho
           if(!estado)
           {
               btSocket.connect();
               estado = true;
               MyConexionBT = new ConexionThread(btSocket);
               MyConexionBT.start();
               Toast.makeText(Lecturas.this, "Conexion Realizada Exitosamente", Toast.LENGTH_SHORT).show();
           }

           else{
               Toast.makeText(Lecturas.this, "Ya esta vinculado", Toast.LENGTH_SHORT).show();

           }
       }

       catch (IOException e)
       {
           try {
               Toast.makeText(Lecturas.this, "Error:", Toast.LENGTH_SHORT).show();
               Toast.makeText(Lecturas.this, e.toString(), Toast.LENGTH_SHORT).show();
               btSocket.close();
           }
           catch (IOException e2) {}
       }
   }




    //Crea el socket
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }




//Se debe crear una sub-clase para tambien heredar los metodos de CompaActivity y Thread juntos
//Ademas  en Run se debe ejecutar el subproceso(interrupcion)
 class ConexionThread extends Thread {
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConexionThread(BluetoothSocket socket) {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }





private String Leer( InputStream  mmInStream, char whattoRead) {
    byte[] buffer = new byte[1];//256
    int bytes;
    String lectura = "";//Humedad temperatura etc
    boolean comienzaCadenaH = false;
while ( true){
    // Se mantiene en modo escucha para determinar el ingreso de datos
    try {
        bytes = mmInStream.read(buffer);
        if (buffer != null && bytes > 0) {
            String readMessage = new String(buffer, 0, bytes);

            for (int i = 0; i < buffer.length; i++) {

                if (buffer[i] == whattoRead) {
                    comienzaCadenaH = true;
                    lectura = "";
                   // lectura += String.valueOf((char) buffer[i]);
                    Log.i("Caracter", String.valueOf((char) buffer[i]));

                } else {
                    if (comienzaCadenaH && buffer[i] != '\0' && buffer[i] != '*') {
                        lectura += String.valueOf((char) buffer[i]);
                        Log.i("Caracter", String.valueOf((char) buffer[i]));
                    }
                }
                if (buffer[i] == '*') {
                    comienzaCadenaH = false;
                    Log.i("Caracter", String.valueOf((char) buffer[i]));
                    // Envia los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, lectura).sendToTarget();
                    return lectura;
                }
            }
        }
    } catch (IOException e) {
        return lectura;
    }
}

}

    public void run() {
        byte[] buffer = new byte[1];//256
        int bytes;
        while (true) {

            Humedad_Show=  Leer( mmInStream, 'h');
            Temperatura_Show=  Leer( mmInStream, 't');
            Madurez_Show= Leer(mmInStream, 'c');
           // Log.i("VALOR MADUREZ",  Madurez_Show);
            Madurez_Show=  Madurez_Show.equals("m")? "MADURO": "INMADURO";
            if( !Humedad_Show.equals(""))
            valor_actual_humedad.setText(  Humedad_Show);
            if( !Temperatura_Show.equals(""))
            valor_actual_temperatura.setText( Temperatura_Show);
            if( !Madurez_Show.equals(""))
           {
               if( Madurez_Show.equals("MADURO"))
                   valor_actual_madurez.setTextColor(Color.RED);
               else
                   valor_actual_madurez.setTextColor(Color.GREEN);
               valor_actual_madurez.setText( Madurez_Show);
           }

        }
    }/** End run **/

    //Enviar los datos
    public void write(String input) {
        try {
            mmOutStream.write(input.getBytes());
        } catch (IOException e) {
            //si no es posible enviar datos se cierra la conexión
            Toast.makeText(  getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
            finish();
        }
    }


}


}
